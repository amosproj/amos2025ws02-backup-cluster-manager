package com.bcm.cluster_manager.service;

import com.bcm.cluster_manager.model.api.BigBackupDTO;
import com.bcm.cluster_manager.service.pagination.shared.BigBackupComparators;
import com.bcm.shared.model.api.*;
import com.bcm.shared.model.database.BackupState;
import com.bcm.shared.service.NodeHttpClient;
import com.bcm.shared.pagination.filter.Filter;
import com.bcm.shared.pagination.PaginationProvider;
import com.bcm.shared.pagination.sort.SortProvider;
import com.bcm.shared.util.NodeUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;

import static com.bcm.shared.mapper.BackupConverter.toLdt;

@Service
public class CMBackupService implements PaginationProvider<BigBackupDTO> {

    @Autowired
    private  RegistryService registryService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private NodeHttpClient nodeHttpClient;

    private static final String BACKUPS_ENDPOINT = "/api/v1/bn/backups";

    private static final Logger logger =LoggerFactory.getLogger(CMBackupService.class);


    @Override
    public long getTotalItemsCount(Filter filter) {
        // Add SQL query with filter to get the actual count
        List<BigBackupDTO> base = (getAllBackups());
        return applySearch(applyFilters(base, filter), filter).size();

    }

    @Override
    public List<BigBackupDTO> getDBItems(long page, long itemsPerPage, Filter filter) {
        List<BigBackupDTO> allBackups = (getAllBackups());

        List<BigBackupDTO> filtered = applyFilters(allBackups, filter);
        List<BigBackupDTO> searched = applySearch(filtered, filter);
        List<BigBackupDTO> sorted = SortProvider.sort(
                searched,
                filter.getSortBy(),
                filter.getSortOrder() != null ? filter.getSortOrder().toString() : null,
                BigBackupComparators.COMPARATORS
        );
        int fromIndex = (int) Math.max(0, (page - 1) * itemsPerPage);
        int toIndex = Math.min(fromIndex + (int) itemsPerPage, sorted.size());
        if (fromIndex >= toIndex) {
            return new ArrayList<>();
        }
        return sorted.subList(fromIndex, toIndex);
    }

    public List<BigBackupDTO> getAllBackups() {
        Collection<NodeDTO> nodes = registryService.getActiveNodes();
        if (nodes.isEmpty()) return List.of();

        List<CompletableFuture<BigBackupDTO[]>> futures = nodes.stream().map(node -> CompletableFuture.supplyAsync(() -> {
            try {
                String url = "http://" + node.getAddress() + BACKUPS_ENDPOINT;
                ResponseEntity<BackupDTO[]> response = restTemplate.getForEntity(url, BackupDTO[].class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return Arrays.stream(response.getBody())
                            .map(taskDto -> {
                                BigBackupDTO bigBackupDTO = new BigBackupDTO();
                                bigBackupDTO.setId(taskDto.getId());
                                bigBackupDTO.setClientId(taskDto.getClientId());
                                bigBackupDTO.setTaskId(taskDto.getTaskId());
                                bigBackupDTO.setName(taskDto.getName());
                                bigBackupDTO.setState(taskDto.getState());
                                bigBackupDTO.setSizeBytes(taskDto.getSizeBytes());
                                bigBackupDTO.setStartTime(taskDto.getStartTime());
                                bigBackupDTO.setStopTime(taskDto.getStopTime());
                                bigBackupDTO.setCreatedAt(taskDto.getCreatedAt());

                                bigBackupDTO.setNodeDTO(node);

                                return bigBackupDTO;
                            })
                            .toArray(BigBackupDTO[]::new);
                }
            } catch (Exception e) {
                logger.info("Fehler beim Abruf von Backups von Node " + node.getAddress() + ". Error: " + (e.getMessage()));
            }
            return null;
        })).toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<BigBackupDTO> allBackups = new ArrayList<>();
        //Set<Long> seenIds = new HashSet<>();

        for (CompletableFuture<BigBackupDTO[]> future : futures) {
            try {
                BigBackupDTO[] backups = future.get();
                if (backups != null) {
                    allBackups.addAll(Arrays.asList(backups));
                }
            } catch (Exception ignored) {
            }
        }

        return allBackups;
    }

    // Filters by BackupState parsed from filter.getFilters()
    private List<BigBackupDTO> applyFilters(List<BigBackupDTO> backups, Filter filter) {
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
    private List<BigBackupDTO> applySearch(List<BigBackupDTO> backups, Filter filter) {
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

    public BigBackupDTO createBackup(BigBackupDTO request) {

        BackupDTO backupDTO = new BackupDTO();
        backupDTO.setClientId(request.getClientId());
        backupDTO.setTaskId(request.getTaskId());
        backupDTO.setName("Backup for task " + request.getTaskId());
        backupDTO.setState(BackupState.QUEUED);
        backupDTO.setSizeBytes(request.getSizeBytes());
        backupDTO.setCreatedAt(toLdt(Instant.now()));

        String targetAddress = request.getNodeDTO().getAddress();

        registryService.getActiveNodes().forEach(n ->
                logger.info("Active node: id={}, address={}", n.getId(), n.getAddress())
        );

        Optional<BigBackupDTO> result = registryService.getActiveNodes().stream()
                .filter(node -> node.getAddress().equals(targetAddress))
                .findFirst()
                .map(node -> {
                    try {
                        String url = "http://" + node.getAddress() + "/api/v1/bn/backups/sync";
                        ResponseEntity<BackupDTO> response =
                                restTemplate.postForEntity(url, backupDTO, BackupDTO.class);

                        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                            BigBackupDTO dto = getBigBackupDTO(response);
                            dto.setNodeDTO(node);   // important for the frontend
                            return dto;
                        } else {
                            logger.error("Node {} responded with status {} and body {} when creating backup",
                                    node.getAddress(), response.getStatusCode(), response.getBody());
                        }
                    } catch (Exception e) {
                        logger.error("Fehler beim Hinzufügen von Backup an Node {}", node.getAddress(), e);
                        throw e;
                    }
                    return null;
                });

        if (result.isPresent()) {
            return result.get();
        }

        logger.error("Target node for new Backup not found or error occurred. targetAddress={}", targetAddress);
        return null;
    }

    private static BigBackupDTO getBigBackupDTO(ResponseEntity<BackupDTO> response) {
        BackupDTO dto = response.getBody();

        BigBackupDTO createdBackup = new BigBackupDTO();
        createdBackup.setId(dto.getId());
        createdBackup.setClientId(dto.getClientId());
        createdBackup.setTaskId(dto.getTaskId());
        createdBackup.setName(dto.getName());
        createdBackup.setState(dto.getState());
        createdBackup.setSizeBytes(dto.getSizeBytes());
        createdBackup.setStartTime(dto.getStartTime());
        createdBackup.setStopTime(dto.getStopTime());
        createdBackup.setCreatedAt(dto.getCreatedAt());
        return createdBackup;
    }

    public void deleteBackup(Long id, String nodeAddress) {
        try {

            var nodeOpt = registryService.getActiveNodes().stream()
                    .filter(node -> node.getAddress().equals(nodeAddress))
                    .findFirst();

            if (nodeOpt.isEmpty()) {
                logger.warn("Cannot delete backup {}: node {} is not active or not found", id, nodeAddress);
                return;
            }

            String url = "http://" + nodeAddress + "/api/v1/bn/backups/" + id;

            restTemplate.delete(url);

            logger.info("Deleted backup {} from node {}", id, nodeAddress);

        } catch (Exception e) {
            logger.error("Error deleting backup {} from node {}: {}", id, nodeAddress, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /*
    //TODO: functionality to be tested, this is just a draft
    public void executeBackup(Long id, Long duration, Boolean shouldSucceed) {
        try {
            // async forwarding
            executeAndUpdate(id, duration, shouldSucceed);

        } catch (Exception e) {
            logger.error("Error executing backup {} from node {}", id, e.getMessage());
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

            ExecuteBackupRequest request = new ExecuteBackupRequest();

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
            logger.error("Error executing backup {} from node {}", id, e.getMessage());
        }
    }
    */


}
