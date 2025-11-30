package com.bcm.cluster_manager.service;

import com.bcm.cluster_manager.model.api.BackupDeleteDTO;
import com.bcm.cluster_manager.model.api.CreateBackupRequest;
import com.bcm.shared.model.api.ExecuteBackupRequest;
import com.bcm.cluster_manager.model.database.Backup;
import com.bcm.cluster_manager.model.database.BackupState;
import com.bcm.cluster_manager.repository.BackupMapper;
import com.bcm.cluster_manager.service.sort.BackupComparators;
import com.bcm.shared.filter.Filter;
import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.pagination.PaginationProvider;
import com.bcm.shared.sort.SortProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
public class BackupService implements PaginationProvider<BackupDTO> {

    private final RegistryService registryService;
    private final RestTemplate restTemplate;
    private final BackupMapper backupMapper;

    @Value("${application.bm.public-address:localhost:8082}")
    private String backupManagerBaseUrl;

    public BackupService(RegistryService registryService,
                         BackupMapper backupMapper,
                         RestTemplate restTemplate) {
        this.registryService = registryService;
        this.backupMapper = backupMapper;
        this.restTemplate = restTemplate;
    }

    @Override
    public long getTotalItemsCount(Filter filter) {
        // Add SQL query with filter to get the actual count
        List<BackupDTO> base = (getAllBackups());
        return applySearch(applyFilters(base, filter), filter).size();

    }

    @Override
    public List<BackupDTO> getDBItems(long page, long itemsPerPage, Filter filter) {
        List<BackupDTO> allBackups = (getAllBackups());

        List<BackupDTO> filtered = applyFilters(allBackups, filter);
        List<BackupDTO> searched = applySearch(filtered, filter);
        List<BackupDTO> sorted = SortProvider.sort(
                searched,
                filter.getSortBy(),
                filter.getSortOrder() != null ? filter.getSortOrder().toString() : null,
                BackupComparators.COMPARATORS
        );
        int fromIndex = (int) Math.max(0, (page - 1) * itemsPerPage);
        int toIndex = Math.min(fromIndex + (int) itemsPerPage, sorted.size());
        if (fromIndex >= toIndex) {
            return new ArrayList<>();
        }
        return sorted.subList(fromIndex, toIndex);
    }

    public List<BackupDTO> getAllBackups() {
        List<Backup> backups = backupMapper.findAll();
        List<BackupDTO> backupDTOs = new ArrayList<>();

        for (Backup backup : backups) {
            BackupDTO dto = new BackupDTO(
                    backup.getId(),
                    backup.getClientId(),
                    backup.getTaskId(),
                    backup.getMessage(),
                    backup.getState(),
                    backup.getSizeBytes(),
                    toLdt(backup.getStartTime()),
                    toLdt(backup.getStopTime()),
                    toLdt(backup.getCreatedAt()),
                    null
            );
            backupDTOs.add(dto);
        }

        return backupDTOs;

    }

    // Filters by BackupState parsed from filter.getFilters()
    private List<BackupDTO> applyFilters(List<BackupDTO> backups, Filter filter) {
        if (filter == null || filter.getFilters() == null || filter.getFilters().isEmpty()) {
            return backups;
        }

        var requested = filter.getFilters().stream()
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .map(s -> {
                    try {
                        return BackupState.valueOf(s);
                    } catch (IllegalArgumentException ex) {
                        return null;
                    }
                })
                .filter(state -> state != null)
                .distinct()
                .toList();

        if (requested.isEmpty()) return backups;

        if (requested.size() == BackupState.values().length) return backups;

        return backups.stream()
                .filter(b -> requested.contains(b.getState()))
                .toList();
    }

    // Searches in name, clientId, taskId, state name, and id (if present)
    private List<BackupDTO> applySearch(List<BackupDTO> backups, Filter filter) {
        if (filter != null && StringUtils.hasText(filter.getSearch())) {
            String searchTerm = filter.getSearch().toLowerCase();
            return backups.stream()
                    .filter(b ->
                            (b.getName() != null && b.getName().toLowerCase().contains(searchTerm)) ||
                                    (b.getClientId() != null && b.getClientId().toString().toLowerCase().contains(searchTerm)) ||
                                    (b.getTaskId() != null && b.getTaskId().toString().toLowerCase().contains(searchTerm)) ||
                                    (b.getState() != null && b.getState().name().toLowerCase().contains(searchTerm)) ||
                                    (b.getId() != null && b.getId().toString().toLowerCase().contains(searchTerm))
                    )
                    .toList();
        }
        return backups;
    }


    public BackupDTO createBackup(CreateBackupRequest request) {
        // select nodes
        List<String> activeNodes = registryService.getActiveNodes().stream()
                .map(NodeDTO::getAddress)
                .toList();

        //System.out.println("Active nodes: " + activeNodes);

        if (activeNodes.isEmpty()) {
            throw new RuntimeException("No active nodes available");
        }

        BackupDTO dto = new BackupDTO(
                null,
                request.getClientId(),
                request.getTaskId(),
                "Backup-" + request.getTaskId(),
                BackupState.RUNNING,
                request.getSizeBytes(),
                null,
                null,
                LocalDateTime.now(),
                activeNodes
        );

        try {
            BackupDTO savedDto = store(dto);
            restTemplate.postForEntity(
                    "http://" + backupManagerBaseUrl + "/api/v1/bm/backups",
                    savedDto,
                    Void.class
            );
            return dto;

        } catch (Exception e) {
            System.out.println("Failed to forward to backup_manager: " + e.getMessage());
            throw e;
        }
    }

    public BackupDTO executeBackup(Long id, Long duration, Boolean shouldSucceed) {
        try {
            // Update CM metadata to RUNNING
            Backup b = updateBackupMetadata(id, BackupState.RUNNING, "Backup is running", Instant.now(), null);
            System.out.println("CM metadata updated to RUNNING for backup " + id);

            // async forwarding
            executeAndUpdate(id, duration, shouldSucceed, b);

            return toDTO(b);

        } catch (Exception e) {
            System.out.println("✗ Failed to execute backup: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Async
    public void executeAndUpdate(Long id, Long duration, Boolean shouldSucceed, Backup b) {
        try {
            // Build list of node addresses
            List<String> nodes = registryService.getActiveNodes().stream()
                    .map(NodeDTO::getAddress)
                    .toList();

            if (nodes.isEmpty()) {
                System.out.println("No active nodes available");
                return;
            }

            ExecuteBackupRequest request = new ExecuteBackupRequest(duration, shouldSucceed, nodes);

            // Notify backup_manager
            String url = "http://" + backupManagerBaseUrl + "/api/v1/bm/backups/" + id + "/execute";
            ResponseEntity<BackupDTO> response = restTemplate.postForEntity(url, request, BackupDTO.class);

            if (response.getStatusCode().is2xxSuccessful() && shouldSucceed) {
                // Update CM metadata based on BM result
                updateBackupMetadata(id, BackupState.COMPLETED, "Backup completed successfully", null, Instant.now());
                System.out.println("CM metadata updated to COMPLETED for backup " + id);
            } else {
                updateBackupMetadata(id, BackupState.FAILED, "Backup failed", null, Instant.now());
            }

            System.out.println("Forwarded execute to backup_manager");

        } catch (Exception e) {
            System.out.println("Failed to notify backup_manager: " + e.getMessage());
        }
    }


    private Backup updateBackupMetadata(Long id, BackupState state, String message, Instant startTime, Instant stopTime) {
        try {
            Backup b = backupMapper.findById(id);
            if (b != null) {

                b.setState(state);
                b.setMessage(message);

                if (startTime != null) {
                    b.setStartTime(startTime);
                }

                if (stopTime != null) {
                    b.setStopTime(stopTime);
                }

                backupMapper.update(b);
                return b;
            }
        } catch (Exception e) {
            System.out.println("Failed to update metadata for backup " + id);
            throw e;
        }
        return null;
    }

    private BackupDTO toDTO(Backup metadata) {
        return new BackupDTO(
                metadata.getId(),
                metadata.getClientId(),
                metadata.getTaskId(),
                "Backup-" + metadata.getId(),
                metadata.getState(),
                metadata.getSizeBytes(),
                toLdt(metadata.getStartTime()),
                toLdt(metadata.getStopTime()),
                toLdt(metadata.getCreatedAt()),
                null
        );
    }

    public BackupDTO store(BackupDTO dto) {
        Backup backup = new Backup();
        backup.setClientId(dto.getClientId());
        backup.setTaskId(dto.getTaskId());
        backup.setSizeBytes(dto.getSizeBytes() != null ? dto.getSizeBytes() : 0L);
        backup.setStartTime(Instant.now());
        backup.setState(BackupState.RUNNING);
        backup.setMessage(null);
        backup.setCreatedAt(Instant.now());

        backupMapper.insert(backup);
        return new BackupDTO(
                backup.getId(),
                backup.getClientId(),
                backup.getTaskId(),
                "Backup-" + backup.getTaskId(),
                backup.getState(),
                backup.getSizeBytes(),
                toLdt(backup.getStartTime()),
                toLdt(backup.getStopTime()),
                toLdt(backup.getCreatedAt()),
                dto.getReplicationNodes()
        );

    }

    public void deleteBackup(Long backupId) {

        try {
            // build list of node addresses that should delete this backup
            List<String> nodesToDeleteOn = registryService.getAllNodes().stream()
                    .map(NodeDTO::getAddress)
                    .toList();

            BackupDeleteDTO request = new BackupDeleteDTO(backupId, nodesToDeleteOn);

            // 2. notify backup_manager with backupId + nodes
            String url = "http://" + backupManagerBaseUrl + "/api/v1/bm/backups/delete";
            restTemplate.postForEntity(url, request, Void.class);

            // 3. delete backup metadata from CM
            backupMapper.delete(backupId);

        } catch (Exception e) {
            System.err.println("Failed to delete backup via " + backupManagerBaseUrl + ": " + e.getMessage());
        }
    }

    public static LocalDateTime toLdt(Instant t) {
        return t == null ? null : LocalDateTime.ofInstant(t, ZoneOffset.UTC);
    }

}
