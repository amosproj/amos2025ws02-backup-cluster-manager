package com.bcm.cluster_manager.service;

import com.bcm.cluster_manager.model.api.BigClientDTO;
import com.bcm.cluster_manager.service.pagination.shared.BigClientComparators;
import com.bcm.shared.pagination.PaginationProvider;
import com.bcm.shared.pagination.filter.Filter;
import com.bcm.shared.pagination.sort.SortProvider;
import com.bcm.shared.model.api.NodeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
public class CMClientService implements PaginationProvider<BigClientDTO> {

    @Autowired
    private RegistryService registryService;

    private final WebClient webClient;

    public CMClientService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
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

                    return sorted.subList(fromIndex, toIndex);
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
}
