package com.bcm.cluster_manager.service;


import com.bcm.shared.model.api.BackupDTO;
import com.bcm.shared.filter.Filter;
import com.bcm.shared.model.api.BackupDeleteDTO;
import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.model.database.BackupState;
import com.bcm.shared.repository.BackupMapper;
import com.bcm.shared.service.BackupStorageService;
import org.springframework.beans.factory.annotation.Value;
import com.bcm.shared.pagination.PaginationProvider;
import com.bcm.shared.model.api.NodeStatus;
import com.bcm.shared.sort.NodeComparators;
import com.bcm.shared.sort.SortProvider;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.bcm.cluster_manager.dto.CreateBackupRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;

import java.util.List;
import org.springframework.util.StringUtils;

@Service
public class ClusterManagerService implements PaginationProvider<NodeDTO> {
    @Autowired
    private RegistryService registry;

    @Autowired
    private BackupStorageService backupStorageService;


    @Value("${application.bm.public-address:localhost:8082}")
    private String backupManagerBaseUrl;

    public ClusterManagerService(RegistryService registryService,
                                 BackupMapper backupMapper,
                                 RestTemplate restTemplate) {
        this.registryService = registryService;
        this.backupMapper = backupMapper;
        this.restTemplate = restTemplate;
    }

    @Override
    public long getTotalItemsCount(Filter filter) {
        // Add SQL query with filter to get the actual count
        List<NodeDTO> allNodes = new ArrayList<>(registry.getAllNodes());
        List<NodeDTO> filteredNodes = applyFilters(allNodes, filter);
        List<NodeDTO> filtered = applySearch(filteredNodes, filter);

        return filtered.size();
    }

    @Override
    public List<NodeDTO> getDBItems(long page, long itemsPerPage, Filter filter) {
        // Add SQL query with filter and pagination to get the actual items
        List<NodeDTO> allNodes = new ArrayList<>(registry.getAllNodes());
        List<NodeDTO> filteredNodes = applyFilters(allNodes, filter);
        List<NodeDTO> filtered = applySearch(filteredNodes, filter);
        List<NodeDTO> sorted = SortProvider.sort(filtered, filter.getSortBy(), filter.getSortOrder().toString(), NodeComparators.COMPARATORS);
        // Pagination
        int fromIndex = (int) ((page - 1) * itemsPerPage);
        int toIndex = Math.min(fromIndex + (int) itemsPerPage, sorted.size());
        if (fromIndex > toIndex) {
            return new ArrayList<>();
        }
        sorted = sorted.subList(fromIndex, toIndex);
        return sorted;
    }

    private final RegistryService registryService;
    private final RestTemplate restTemplate;
    private final BackupMapper backupMapper;


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
            BackupDTO savedDto = backupStorageService.store(dto);
            restTemplate.postForEntity(
                    "http://" + backupManagerBaseUrl + "/api/v1/backups",
                    savedDto,
                    Void.class
            );
        } catch (Exception e) {
            System.out.println("Failed to forward to backup_manager: " + e.getMessage());
            throw e;
        }

        return dto;
    }

    public void deleteBackup(Long backupId) {

        try {
            // build list of node addresses that should delete this backup
            List<String> nodesToDeleteOn = registryService.getAllNodes().stream()
                    .map(n -> n.getAddress())
                    .toList();

            BackupDeleteDTO request = new BackupDeleteDTO(backupId, nodesToDeleteOn);

            // 2. notify backup_manager with backupId + nodes
            String url = "http://" + backupManagerBaseUrl + "/api/v1/backups/delete";
            restTemplate.postForEntity(url, request, Void.class);

            // 3. delete backup metadata from CM
            backupMapper.delete(backupId);

        } catch (Exception e) {
            System.err.println("Failed to delete backup via " + backupManagerBaseUrl + ": " + e.getMessage());
        }
    }

    private List<NodeDTO> applyFilters(List<NodeDTO> nodes, Filter filter) {
        if (filter == null || filter.getFilters() == null || filter.getFilters().isEmpty()) {
            return nodes;
        }

        var requested = filter.getFilters().stream()
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .map(s -> {
                    try { return NodeStatus.valueOf(s); } catch (IllegalArgumentException ex) { return null; }
                })
                .filter(ns -> ns != null)
                .distinct()
                .toList();

        // If no valid statuses parsed, return original list
        if (requested.isEmpty()) return nodes;

        // If all possible statuses requested, skip filtering
        if (requested.size() == NodeStatus.values().length) return nodes;

        return nodes.stream()
                .filter(n -> requested.contains(n.getStatus()))
                .toList();
    }

    private List<NodeDTO> applySearch(List<NodeDTO> nodes, Filter filter){
        if (filter != null && StringUtils.hasText(filter.getSearch())) {
            String searchTerm = filter.getSearch().toLowerCase();
            return nodes.stream()
                    .filter(node ->
                        (node.getName() != null && node.getName().toLowerCase().contains(searchTerm)) ||
                        (node.getAddress() != null && node.getAddress().toLowerCase().contains(searchTerm)) ||
                        (node.getId() != null && node.getId().toString().contains(searchTerm))
                    )
                    .toList();
        }
        return nodes;
    }
}