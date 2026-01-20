package com.bcm.cluster_manager.service;

import com.bcm.cluster_manager.model.api.BigClientDTO;
import com.bcm.cluster_manager.service.pagination.shared.BigClientComparators;
import com.bcm.shared.pagination.PaginationProvider;
import com.bcm.shared.pagination.filter.Filter;
import com.bcm.shared.pagination.sort.SortProvider;
import com.bcm.shared.model.api.NodeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
public class CMClientService implements PaginationProvider<BigClientDTO> {

    private static final Logger logger = LoggerFactory.getLogger(CMClientService.class);

    @Autowired
    private RegistryService registryService;

    private final WebClient webClient;
    private final CacheManager cacheManager;

    public CMClientService(WebClient.Builder webClientBuilder, CacheManager cacheManager) {
        this.webClient = webClientBuilder.build();
        this.cacheManager = cacheManager;
    }

    public Mono<List<BigClientDTO>> getAllClients() {
        Collection<NodeDTO> nodeAddresses = registryService.getActiveAndManagedNodes();
        if (nodeAddresses.isEmpty()) {
            return Mono.just(List.of());
        }

        return Flux.fromIterable(nodeAddresses)
                .flatMap(node -> fetchClientsFromNode(node)
                        .onErrorResume(e -> {
                            System.out.println("Fehler beim Abruf von Clients von Node " + node.getAddress());
                            return Flux.empty();
                        }))
                .collectList();
    }

    private Flux<BigClientDTO> fetchClientsFromNode(NodeDTO node) {
        String url = "http://" + node.getAddress() + "/api/v1/bn/clients";
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(BigClientDTO.class)
                .map(client -> {
                    BigClientDTO bigClientDto = new BigClientDTO();
                    bigClientDto.setId(client.getId());
                    bigClientDto.setNameOrIp(client.getNameOrIp());
                    bigClientDto.setEnabled(client.isEnabled());
                    bigClientDto.setNodeDTO(node);
                    return bigClientDto;
                });
    }

    @Override
    public Mono<List<BigClientDTO>> getDBItems(long page, long itemsPerPage, Filter filter) {

        String cacheKey = buildCacheKey(page, itemsPerPage, filter);  // Use stable key

        List<BigClientDTO> cached = cacheManager.getCache("clientPages").get(cacheKey, List.class);
        if (cached != null) {
            logger.info("Page cache HIT: {}", cacheKey);
            return Mono.just(cached);
        }

        logger.info("Page cache MISS: {}", cacheKey);

        return getAllClients()
                .map(allClients -> {
                    List<BigClientDTO> filtered = applyFilters(allClients, filter);
                    List<BigClientDTO> searched = applySearch(filtered, filter);
                    List<BigClientDTO> sorted = SortProvider.sort(
                            searched,
                            filter.getSortBy(),
                            filter.getSortOrder() != null ? filter.getSortOrder().toString() : null,
                            BigClientComparators.COMPARATORS
                    );

                    int fromIndex = (int) Math.max(0, (page - 1) * itemsPerPage);
                    int toIndex = Math.min(fromIndex + (int) itemsPerPage, sorted.size());
                    if (fromIndex >= toIndex) return List.of();

                    List<BigClientDTO> result = sorted.subList(fromIndex, toIndex);

                    // Cache the page result
                    cacheManager.getCache("clientPages").put(cacheKey, result);
                    logger.info("Cached page {} ({} items)", page, result.size());
                    return result;

                });
    }

    @Override
    public Mono<Long> getTotalItemsCount(Filter filter) {
        return getAllClients()
                .map(base -> (long) applySearch(applyFilters(base, filter), filter).size());
    }

    private List<BigClientDTO> applyFilters(List<BigClientDTO> clients, Filter filter) {
        if (filter == null || filter.getFilters() == null || filter.getFilters().isEmpty()) {
            return clients;
        }

        var requested = filter.getFilters().stream()
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .map(s -> {
                    try {
                        return Boolean.valueOf(s);
                    } catch (IllegalArgumentException ex) {
                        return null;
                    }
                })
                .filter(state -> state != null)
                .distinct()
                .toList();

        if (requested.isEmpty())
            return clients;

        if (requested.size() == 2)
            return clients;

        return clients.stream()
                .filter(b -> requested.contains(b.isEnabled()))
                .toList();
    }

    // Searches in name, clientId, taskId, state name, and id (if present)
    private List<BigClientDTO> applySearch(List<BigClientDTO> clients, Filter filter) {
        if (filter != null && StringUtils.hasText(filter.getSearch())) {
            String searchTerm = filter.getSearch().toLowerCase();
            return clients.stream()
                    .filter(b -> (b.getNameOrIp() != null && b.getNameOrIp().toLowerCase().contains(searchTerm)) ||
                            (b.getId() != null && b.getId().toString().toLowerCase().contains(searchTerm)))
                    .toList();
        }
        return clients;
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
