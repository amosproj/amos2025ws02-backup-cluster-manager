package com.bcm.cluster_manager.service;


import com.bcm.shared.model.api.JoinDTO;
import com.bcm.shared.model.api.RegisterRequest;
import com.bcm.shared.pagination.sort.NodeComparators;
import com.bcm.shared.pagination.filter.Filter;
import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.model.api.NodeMode;
import com.bcm.shared.model.api.NodeStatus;
import com.bcm.shared.pagination.PaginationProvider;
import com.bcm.shared.pagination.sort.SortProvider;
import com.bcm.shared.service.NodeHttpClient;
import com.bcm.shared.util.NodeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing backup nodes: registration, pagination, shutdown/restart, and sync with BN join/leave.
 */
@Service
public class NodeManagementService implements PaginationProvider<NodeDTO> {

    private static final Logger logger = LoggerFactory.getLogger(NodeManagementService.class);

    @Value("${application.cm.public-address:localhost:8080}")
    private String cmPublicAddress;

    @Autowired
    private NodeHttpClient nodeHttpClient;

    @Autowired
    private RegistryService registry;

    @Autowired
    private SyncService syncService;

    private final WebClient webClient;

    /**
     * Creates the node management service with the given WebClient builder.
     *
     * @param webClientBuilder WebClient builder for HTTP calls to nodes
     */
    public NodeManagementService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Returns the total count of nodes after applying filters and search.
     *
     * @param filter filter and search parameters
     * @return total count
     */
    @Override
    public Mono<Long> getTotalItemsCount(Filter filter) {
        // Add SQL query with filter to get the actual count
        return Mono.fromSupplier(() -> {
            List<NodeDTO> allNodes = new ArrayList<>(registry.getAllNodes());
            List<NodeDTO> filteredNodes = applyFilters(allNodes, filter);
            List<NodeDTO> filtered = applySearch(filteredNodes, filter);
            return (long) filtered.size();
        });    }

    /**
     * Returns a page of nodes after filtering, search, and sorting.
     *
     * @param page         page number (1-based)
     * @param itemsPerPage page size
     * @param filter       filter, search, and sort parameters
     * @return list of node DTOs for the page
     */
    @Override
    public Mono<List<NodeDTO>> getDBItems(long page, long itemsPerPage, Filter filter) {
        // Add SQL query with filter and pagination to get the actual items
        return Mono.fromSupplier(() -> {
            List<NodeDTO> allNodes = new ArrayList<>(registry.getAllNodes());
            List<NodeDTO> filteredNodes = applyFilters(allNodes, filter);
            List<NodeDTO> filtered = applySearch(filteredNodes, filter);

            List<NodeDTO> sorted = SortProvider.sort(
                    filtered,
                    filter.getSortBy(),
                    filter.getSortOrder() != null ? filter.getSortOrder().toString() : null,
                    NodeComparators.COMPARATORS
            );

            int fromIndex = (int) ((page - 1) * itemsPerPage);
            int toIndex = Math.min(fromIndex + (int) itemsPerPage, sorted.size());
            if (fromIndex >= toIndex) return List.of();

            return sorted.subList(fromIndex, toIndex);
        });
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

    /**
     * Updates a node's managed mode in the registry.
     *
     * @param nodeDTO node DTO with id and managed flag
     */
    public void updateNodeManagedMode(NodeDTO nodeDTO) {
        registry.updateIsManaged(nodeDTO);
    }

    /**
     * Removes a node from the registry and notifies the node to leave the cluster.
     *
     * @param id node id
     * @return completion when done (or error if node not found)
     */
    public Mono<Void> deleteNode(Long id) {

        // get node before deletion
        Optional<NodeDTO> nodeOpt = registry.findById(id);

        // Node is removed from the cluster regardless of successful notification to the node
        try {
            registry.removeNode(id);
        } catch (IllegalArgumentException e) {
            logger.warn("Must not delete node {}: {}", id, e.getMessage());
            return Mono.error(e);
        }

        // try to notify node about deletion from the cluster

        if (nodeOpt.isEmpty()) {
            logger.warn("Node with id {} not found. Cannot notify of deletion", id);
            return Mono.empty();
        }

        NodeDTO node = nodeOpt.get();
        String nodeAddress = node.getAddress();
        JoinDTO dto = createCMJoinDTO();
        String url = NodeUtils.buildNodeUrl(nodeAddress, "/api/v1/bn/leave");

        return webClient.post()
                .uri(url)
                .bodyValue(dto)
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(10))
                .doOnSuccess(response -> logger.info("Node {} was notified about deletion", nodeAddress))
                .doOnError(e -> logger.warn("Node {} was not notified about deletion: {}", nodeAddress, e.getMessage()))
                .then()
                .onErrorResume(e -> Mono.empty());
    }

    /**
     * Registers a backup node: notifies the node to join, then adds to registry and syncs.
     *
     * @param req registration request (address, managed flag)
     * @return completion when done
     */
    public Mono<Void> registerNode(RegisterRequest req) {
        JoinDTO dto = createCMJoinDTO();
        String url = NodeUtils.buildNodeUrl(req.getAddress(), "/api/v1/bn/join");

        return webClient.post()
                .uri(url)
                .bodyValue(dto)
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(10))
                .doOnSuccess(response -> registry.register(req))
                .then(syncService.syncNodes())
                .doOnError(e -> logger.error("Error registering node: {}", e.getMessage()));
    }

    /**
     * Returns a node by id from the registry.
     *
     * @param id node id
     * @return optional node DTO
     */
    public Optional<NodeDTO> getNodeById(Long id) {
        return registry.findById(id);
    }

    /**
     * Returns a node by address from the registry.
     *
     * @param address node address
     * @return optional node DTO
     */
    public Optional<NodeDTO> getNodeByAddress(String address) {
        return registry.findByAddress(address);
    }

    /**
     * Sends a shutdown command to the given node (BN only; not CM).
     *
     * @param nodeId node id
     * @return true if command was sent successfully
     */
    public Mono<Boolean> shutdownNode(Long nodeId) {
        Optional<NodeDTO> nodeOpt = registry.findById(nodeId);
        if (nodeOpt.isEmpty()) {
            logger.warn("Node with id {} not found for shutdown", nodeId);
            return Mono.just(false);
        }

        NodeDTO node = nodeOpt.get();

        // Don't allow shutdown of cluster manager
        if (node.getMode() == NodeMode.CLUSTER_MANAGER) {
            logger.warn("Cannot shutdown cluster manager node {}", node.getAddress());
            return Mono.just(false);
        }

        registry.markShuttingDown(node.getAddress());

        return nodeHttpClient.postNodeNoResponse(node.getAddress(), "/api/v1/shutdown")
                .doOnNext(success -> {
                    if (success) {
                        logger.info("Shutdown command sent to node {}", node.getAddress());
                    } else {
                        logger.error("Failed to send shutdown command to node {}", node.getAddress());
                    }
                    registry.markInactive(node);
                })
                .flatMap(success -> success ? syncService.syncNodes().thenReturn(true) : Mono.just(false));
    }

    /**
     * Sends a restart command to the given node (BN only; not CM).
     *
     * @param nodeId node id
     * @return true if command was sent successfully
     */
    public Mono<Boolean> restartNode(Long nodeId) {
        Optional<NodeDTO> nodeOpt = registry.findById(nodeId);
        if (nodeOpt.isEmpty()) {
            logger.warn("Node with id {} not found for restart", nodeId);
            return Mono.just(false);
        }

        NodeDTO node = nodeOpt.get();

        // Don't allow restart of cluster manager
        if (node.getMode() == NodeMode.CLUSTER_MANAGER) {
            logger.warn("Cannot restart cluster manager node {}", node.getAddress());
            return Mono.just(false);
        }

        registry.markRestarting(node.getAddress());

        return nodeHttpClient.postNodeNoResponse(node.getAddress(), "/api/v1/restart")
                .doOnNext(success -> {
                    if (success) {
                        logger.info("Restart command sent to node {}", node.getAddress());
                    } else {
                        logger.error("Failed to send restart command to node {}", node.getAddress());
                        registry.markInactive(node);
                    }
                });
    }

    @NonNull
    private JoinDTO createCMJoinDTO() {
        JoinDTO dto = new JoinDTO();
        dto.setCmURL(cmPublicAddress);
        return dto;
    }
}