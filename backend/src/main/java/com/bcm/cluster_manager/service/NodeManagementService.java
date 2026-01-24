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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public NodeManagementService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @Override
    public Mono<Long> getTotalItemsCount(Filter filter) {
        // Add SQL query with filter to get the actual count
        return Mono.fromSupplier(() -> {
            List<NodeDTO> allNodes = new ArrayList<>(registry.getAllNodes());
            List<NodeDTO> filteredNodes = applyFilters(allNodes, filter);
            List<NodeDTO> filtered = applySearch(filteredNodes, filter);
            return (long) filtered.size();
        });    }

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

    public void updateNodeManagedMode(NodeDTO nodeDTO) {
        registry.updateIsManaged(nodeDTO);
    }

    public void deleteNode(Long id) {
        registry.removeNode(id);
    }

    public Mono<Void> registerNode(RegisterRequest req) {
        JoinDTO dto = new JoinDTO();
        dto.setCmURL("http://" + cmPublicAddress);
        String url = "http://" + req.getAddress() + "/api/v1/bn/join";

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

    public Optional<NodeDTO> getNodeById(Long id) {
        return registry.findById(id);
    }

    public Optional<NodeDTO> getNodeByAddress(String address) {
        return registry.findByAddress(address);
    }

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
}