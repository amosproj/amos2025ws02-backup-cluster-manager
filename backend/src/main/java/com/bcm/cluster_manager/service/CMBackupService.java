package com.bcm.cluster_manager.service;

import com.bcm.cluster_manager.model.api.BigBackupDTO;
import com.bcm.cluster_manager.service.pagination.shared.BigBackupComparators;
import com.bcm.shared.model.api.*;
import com.bcm.shared.model.database.BackupState;
import com.bcm.shared.service.NodeHttpClient;
import com.bcm.shared.pagination.filter.Filter;
import com.bcm.shared.pagination.PaginationProvider;
import com.bcm.shared.pagination.sort.SortProvider;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.*;

import org.slf4j.Logger;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CMBackupService implements PaginationProvider<BigBackupDTO> {

    private final RegistryService registryService;

    private final WebClient webClient;
    private final NodeHttpClient nodeHttpClient;

    private static final String BACKUPS_ENDPOINT = "/api/v1/bn/backups";

    private static final Logger logger =LoggerFactory.getLogger(CMBackupService.class);
    private final CacheManager cacheManager;

    public CMBackupService(RegistryService registryService,
                           WebClient.Builder webClientBuilder,
                           NodeHttpClient nodeHttpClient, CacheManager cacheManager) {
        this.registryService = registryService;
        this.webClient = webClientBuilder.build();
        this.nodeHttpClient = nodeHttpClient;
        this.cacheManager = cacheManager;
    }


    @Override
    public Mono<Long> getTotalItemsCount(Filter filter) {
        return getAllBackups(filter)
                .map(list -> (long) applySearch(applyFilters(list, filter), filter).size());

    }

    @Override
    public Mono<List<BigBackupDTO>> getDBItems(long page, long itemsPerPage, Filter filter) {

        String cacheKey = buildCacheKey(page, itemsPerPage, filter);  // Use stable key

        List<BigBackupDTO> cached = cacheManager.getCache("backupPages").get(cacheKey, List.class);
        if (cached != null) {
            logger.debug("Page cache HIT: {}", cacheKey);
            return Mono.just(cached);
        }

        logger.debug("Page cache MISS: {}", cacheKey);

        return getAllBackups(filter)
                .map(allBackups -> {
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
                    if (fromIndex >= toIndex) return List.of();

                    List<BigBackupDTO> result = sorted.subList(fromIndex, toIndex);

                    // Cache the page result
                    cacheManager.getCache("backupPages").put(cacheKey, result);
                    logger.debug("Cached page {} ({} items)", page, result.size());
                    return result;

                });
    }


    public Mono<List<BigBackupDTO>> getAllBackups(Filter filter) {
        Collection<NodeDTO> nodes = registryService.getActiveAndManagedNodes();
        if (nodes.isEmpty()) return Mono.just(List.of());

        return Flux.fromIterable(nodes)
                .flatMap(node ->{
                    String url = "http://" + node.getAddress() + BACKUPS_ENDPOINT;

                    return webClient.get()
                            .uri(url)
                            .retrieve()
                            .bodyToFlux(BackupDTO.class)
                            .map(dto -> {
                                BigBackupDTO big = new BigBackupDTO();
                                big.setId(dto.getId());
                                big.setClientId(dto.getClientId());
                                big.setTaskId(dto.getTaskId());
                                big.setName(dto.getName());
                                big.setState(dto.getState());
                                big.setSizeBytes(dto.getSizeBytes());
                                big.setStartTime(dto.getStartTime());
                                big.setStopTime(dto.getStopTime());
                                big.setCreatedAt(dto.getCreatedAt());
                                big.setNodeDTO(node);
                                return big;
                            })
                            .onErrorResume(e -> {
                                logger.info("Fehler beim Abruf von Backups von Node {}. Error: {}",
                                        node.getAddress(), e.getMessage());
                                return Flux.empty();
                            });
                })
                .collectList();

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
                .filter(Objects::nonNull)
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

    public Mono<BigBackupDTO> createBackup(BigBackupDTO request) {

        BackupDTO backupDTO = new BackupDTO();
        backupDTO.setClientId(request.getClientId());
        backupDTO.setTaskId(request.getTaskId());
        backupDTO.setName("Backup for task " + request.getTaskId());
        backupDTO.setState(BackupState.QUEUED);
        backupDTO.setSizeBytes(request.getSizeBytes());
        backupDTO.setCreatedAt(Instant.now());

        String targetAddress = request.getNodeDTO().getAddress();

        Collection<NodeDTO> nodes = registryService.getActiveAndManagedNodes();

        nodes.forEach(n -> logger.info("Active node: id={}, address={}", n.getId(), n.getAddress()));

        NodeDTO targetNode = nodes.stream()
                .filter(n -> n.getAddress().equals(targetAddress))
                .findFirst()
                .orElse(null);

        if (targetNode == null) {
            logger.error("Target node for new Backup not found. targetAddress={}", targetAddress);
            return Mono.error(new IllegalArgumentException("Target node not found: " + targetAddress));
        }

        String url = "http://" + targetNode.getAddress() + "/api/v1/bn/backups/sync";

        return webClient.post()
                .uri(url)
                .bodyValue(backupDTO)
                .retrieve()
                .bodyToMono(BackupDTO.class)
                .map(dto -> {
                    BigBackupDTO big = new BigBackupDTO();
                    big.setId(dto.getId());
                    big.setClientId(dto.getClientId());
                    big.setTaskId(dto.getTaskId());
                    big.setName(dto.getName());
                    big.setState(dto.getState());
                    big.setSizeBytes(dto.getSizeBytes());
                    big.setStartTime(dto.getStartTime());
                    big.setStopTime(dto.getStopTime());
                    big.setCreatedAt(dto.getCreatedAt());
                    big.setNodeDTO(targetNode);
                    return big;
                })
                .doOnError(e -> logger.error("Fehler beim Hinzufügen von Backup an Node {}", targetNode.getAddress(), e));
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

    public Mono<Void> deleteBackup(Long id, String nodeAddress) {
        boolean nodeIsActive = registryService.getActiveAndManagedNodes().stream()
                .anyMatch(node -> node.getAddress().equals(nodeAddress));

        if (!nodeIsActive) {
            logger.warn("Cannot delete backup {}: node {} is not active or not found", id, nodeAddress);
            return Mono.error(new IllegalArgumentException("Node not active or not found: " + nodeAddress));
        }

        String url = "http://" + nodeAddress + "/api/v1/bn/backups/" + id;

        return webClient.delete()
                .uri(url)
                .retrieve()
                .toBodilessEntity()
                .then()
                .doOnSuccess(v -> logger.info("Deleted backup {} from node {}", id, nodeAddress))
                .doOnError(e -> logger.error("Error deleting backup {} from node {}: {}", id, nodeAddress, e.getMessage(), e));
    }
    private String buildCacheKey(long page, long itemsPerPage, Filter filter) {
        StringBuilder key = new StringBuilder();
        key.append("page-").append(page);
        key.append("-size-").append(itemsPerPage);

        // Add filter components
        if (filter != null) {
            if (filter.getFilters() != null && !filter.getFilters().isEmpty()) {
                key.append("-filters-").append(String.join(",", filter.getFilters().stream().sorted().toList()));
            }
            if (filter.getSearch() != null && !filter.getSearch().isEmpty()) {
                key.append("-search-").append(filter.getSearch());
            }
            if (filter.getSortBy() != null) {
                key.append("-sort-").append(filter.getSortBy());
            }
            if (filter.getSortOrder() != null) {
                key.append("-order-").append(filter.getSortOrder());
            }
        }

        return key.toString();
    }

}
