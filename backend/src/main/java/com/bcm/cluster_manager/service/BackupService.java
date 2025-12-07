package com.bcm.cluster_manager.service;

import com.bcm.shared.model.api.CreateBackupRequest;
import com.bcm.shared.model.api.ExecuteBackupRequest;
import com.bcm.shared.model.database.BackupState;
import com.bcm.shared.repository.BackupMapper;
import com.bcm.shared.service.sort.BackupComparators;
import com.bcm.shared.filter.Filter;
import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.pagination.PaginationProvider;
import com.bcm.shared.pagination.sort.SortProvider;
import com.bcm.shared.util.NodeUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.bcm.shared.mapper.BackupConverter.toLdt;

@Service
public class BackupService implements PaginationProvider<BackupDTO> {

    private final RegistryService registryService;
    private final RestTemplate restTemplate;


    public BackupService(RegistryService registryService,
                         BackupMapper backupMapper,
                         RestTemplate restTemplate) {
        this.registryService = registryService;
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

    //TODO: functionality -> To be tested
    public List<BackupDTO> getAllBackups() {

        List<BackupDTO> backupDTOs = new ArrayList<>();
        var seenIds = new java.util.HashSet<Long>();

        List<String> nodeAddresses = NodeUtils.addresses(registryService.getActiveNodes());

        List<CompletableFuture<BackupDTO[]>> futures = nodeAddresses.stream()
                .map(nodeAddress -> CompletableFuture.supplyAsync(() -> {
                    try {
                        String url = "http://" + nodeAddress + "/api/v1/bn/backups";
                        ResponseEntity<BackupDTO[]> response = restTemplate.getForEntity(url, BackupDTO[].class);
                        return response.getBody();
                    } catch (Exception e) {
                        System.out.println("Fehler beim Abruf von Backups von Node " + nodeAddress + ": " + e.getMessage());
                        return null;
                    }
                }))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        for (CompletableFuture<BackupDTO[]> future : futures) {
            try {
                BackupDTO[] body = future.get();
                if (body == null || body.length == 0) continue;
                for (BackupDTO b : body) {
                    if (b == null) continue;
                    Long id = b.getId();
                    if (id != null) {
                        if (seenIds.add(id)) {
                            backupDTOs.add(b);
                        }
                    } else {
                        backupDTOs.add(b);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException ignored) {
            }
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

    //TODO: functionality to be tested, this is just a draft
    public BackupDTO createBackup(CreateBackupRequest request) {

        BackupDTO backupDTO = new BackupDTO(
                null,
                request.getClientId(),
                request.getTaskId(),
                "Backup creation initiated",
                BackupState.QUEUED,
                request.getSizeBytes(),
                null,
                null,
                toLdt(Instant.now())
        );

        //get active nodes from registry
        List<String> nodeAddresses = NodeUtils.addresses(registryService.getActiveNodes());

        // send a request to backup nodes to create the backup
        for (String nodeAddress : nodeAddresses) {
            try {
                String url = "http://" + nodeAddress + "/api/v1/bn/backups/sync";
                ResponseEntity<BackupDTO> response = restTemplate.postForEntity(url, request, BackupDTO.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    System.out.println("Backup creation request sent to node " + nodeAddress);
                    return backupDTO;
                } else {
                    System.out.println("Failed to send backup creation request to node " + nodeAddress);
                    return null;
                }
            } catch (Exception e) {
                System.out.println("Error sending backup creation request to node " + nodeAddress + ": " + e.getMessage());
                throw new RuntimeException("Error sending backup creation request to node " + nodeAddress, e);
            }
        }
        return backupDTO;
    }

    //TODO: functionality to be tested, this is just a draft
    public void deleteBackup(Long id) {
        try {

            // Notify all active backup nodes to delete the backup
            List<String> nodeAddresses = NodeUtils.addresses(registryService.getActiveNodes());
            for (String nodeAddress : nodeAddresses) {
                try {
                    String url = "http://" + nodeAddress + "/api/v1/bn/backups/" + id;
                    restTemplate.delete(url);
                    System.out.println("Delete request sent to node " + nodeAddress + " for backup " + id);
                } catch (Exception e) {
                    System.out.println("Error sending delete request to node " + nodeAddress + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.out.println("✗ Failed to delete backup: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    //TODO: functionality to be tested, this is just a draft
    public void executeBackup(Long id, Long duration, Boolean shouldSucceed) {
        try {
            // async forwarding
            executeAndUpdate(id, duration, shouldSucceed);

        } catch (Exception e) {
            System.out.println("✗ Failed to execute backup: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Async
    public void executeAndUpdate(Long id, Long duration, Boolean shouldSucceed) {
        try {
            // Build list of node addresses
            List<String> nodes = NodeUtils.addresses(registryService.getActiveNodes());

            if (nodes.isEmpty()) {
                System.out.println("No active nodes available");
                return;
            }

            ExecuteBackupRequest request = new ExecuteBackupRequest(duration, shouldSucceed, nodes);

            // Notify backup nodes
            List<CompletableFuture<Boolean>> futures = nodes.stream().map(nodeAddress -> CompletableFuture.supplyAsync(() -> {
                try {
                    String url = "http://" + nodeAddress + "/api/v1/bn/" + "/backups/" + id + "/execute";

                    ResponseEntity<Void> response = restTemplate.postForEntity(
                                url,
                                request,
                                Void.class
                    );

                        return response.getStatusCode().is2xxSuccessful();
                    } catch (Exception e) {
                        return false;
                    }
            })).toList();

            CompletableFuture<Void> all = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            all.join();

        } catch (Exception e) {
            System.out.println("Failed to notify backup nodes: " + e.getMessage());
        }
    }



}
