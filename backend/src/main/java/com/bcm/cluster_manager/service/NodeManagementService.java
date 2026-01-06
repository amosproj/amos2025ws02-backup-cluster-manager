package com.bcm.cluster_manager.service;


import com.bcm.shared.model.api.RegisterRequest;
import com.bcm.shared.pagination.sort.NodeComparators;
import com.bcm.shared.pagination.filter.Filter;
import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.model.api.NodeMode;
import com.bcm.shared.model.api.NodeStatus;
import com.bcm.shared.pagination.PaginationProvider;
import com.bcm.shared.pagination.sort.SortProvider;
import com.bcm.shared.service.NodeHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class NodeManagementService implements PaginationProvider<NodeDTO> {

    private static final Logger logger = LoggerFactory.getLogger(NodeManagementService.class);

    @Autowired
    private NodeHttpClient nodeHttpClient;

    @Autowired
    private RegistryService registry;

    @Autowired
    private SyncService syncService;

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

    private List<NodeDTO> applyFilters(List<NodeDTO> nodes, Filter filter) {
        if (filter == null || filter.getFilters() == null || filter.getFilters().isEmpty()) {
            return nodes;
        }

        var requested = filter.getFilters().stream()
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .map(s -> {
                    try {
                        return NodeStatus.valueOf(s);
                    } catch (IllegalArgumentException ex) {
                        return null;
                    }
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

    private List<NodeDTO> applySearch(List<NodeDTO> nodes, Filter filter) {
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

    public void updateNodeManagedMode(NodeDTO nodeDTO) {
        registry.updateIsManaged(nodeDTO);
    }

    public void deleteNode(Long id) {
        registry.removeNode(id);
    }

    public void registerNode(RegisterRequest req) {
        registry.register(req);
        syncService.syncNodes();

    }

    public Optional<NodeDTO> getNodeById(Long id) {
        return registry.findById(id);
    }

    public Optional<NodeDTO> getNodeByAddress(String address) {
        return registry.findByAddress(address);
    }

    public boolean shutdownNode(Long nodeId) {
        Optional<NodeDTO> nodeOpt = registry.findById(nodeId);
        if (nodeOpt.isEmpty()) {
            logger.warn("Node with id {} not found for shutdown", nodeId);
            return false;
        }

        NodeDTO node = nodeOpt.get();

        // Don't allow shutdown of cluster manager
        if (node.getMode() == NodeMode.CLUSTER_MANAGER) {
            logger.warn("Cannot shutdown cluster manager node {}", node.getAddress());
            return false;
        }

        registry.markShuttingDown(node.getAddress());

        boolean success = nodeHttpClient.postNodeSyncNoResponse(node.getAddress(), "/api/v1/shutdown");
        if (success) {
            logger.info("Shutdown command sent to node {}", node.getAddress());
            // Remove node from cluster since it won't come back
            registry.removeNode(nodeId);
            logger.info("Node {} removed from cluster after shutdown", node.getAddress());
            syncService.syncNodes();
            return true;
        } else {
            logger.error("Failed to send shutdown command to node {}", node.getAddress());
            registry.markInactive(node);
            return false;
        }
    }

    public boolean restartNode(Long nodeId) {
        Optional<NodeDTO> nodeOpt = registry.findById(nodeId);
        if (nodeOpt.isEmpty()) {
            logger.warn("Node with id {} not found for restart", nodeId);
            return false;
        }

        NodeDTO node = nodeOpt.get();

        // Don't allow restart of cluster manager
        if (node.getMode() == NodeMode.CLUSTER_MANAGER) {
            logger.warn("Cannot restart cluster manager node {}", node.getAddress());
            return false;
        }

        registry.markRestarting(node.getAddress());

        boolean success = nodeHttpClient.postNodeSyncNoResponse(node.getAddress(), "/api/v1/restart");
        if (success) {
            logger.info("Restart command sent to node {}", node.getAddress());
            return true;
        } else {
            logger.error("Failed to send restart command to node {}", node.getAddress());
            registry.markInactive(node);
            return false;
        }
    }
}