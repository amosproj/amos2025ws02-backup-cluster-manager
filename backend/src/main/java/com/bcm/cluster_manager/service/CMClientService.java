package com.bcm.cluster_manager.service;

import com.bcm.shared.model.api.ClientDTO;
import com.bcm.shared.model.api.TaskDTO;
import com.bcm.shared.model.database.Client;
import com.bcm.shared.pagination.PaginationProvider;
import com.bcm.shared.pagination.filter.Filter;
import com.bcm.shared.pagination.sort.ClientComparators;
import com.bcm.shared.pagination.sort.SortProvider;
import com.bcm.shared.repository.ClientMapper;
import com.bcm.shared.util.NodeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Service
public class CMClientService implements PaginationProvider<ClientDTO> {

    @Autowired
    private RegistryService registryService;

    @Autowired
    private RestTemplate restTemplate;

    public List<ClientDTO> getAllClients() {
        List<String> nodeAddresses = NodeUtils.addresses(registryService.getActiveNodes());
        if (nodeAddresses.isEmpty())
            return List.of();

        List<CompletableFuture<ClientDTO[]>> futures = nodeAddresses.stream()
                .map(address -> CompletableFuture.supplyAsync(() -> {
                    try {
                        String url = "http://" + address + "/api/v1/bn/clients";
                        ResponseEntity<ClientDTO[]> response = restTemplate.getForEntity(url, ClientDTO[].class);
                        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                            return response.getBody();
                        }
                    } catch (Exception e) {
                        System.out.println("Fehler beim Abruf von Tasks von Node " + address);
                    }
                    return null;
                })).toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<ClientDTO> allClients = new ArrayList<>();
        // Set<Long> seenIds = new HashSet<>();

        for (CompletableFuture<ClientDTO[]> future : futures) {
            try {
                ClientDTO[] clients = future.get();
                if (clients != null) {
                    for (ClientDTO client : clients) {
                        // if (client != null && seenIds.add(client.getId())) {
                        allClients.add(client);
                        // }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return allClients;
    }

    @Override
    public List<ClientDTO> getDBItems(long page, long itemsPerPage, Filter filter) {
        List<ClientDTO> allClients = (getAllClients());

        List<ClientDTO> filtered = applyFilters(allClients, filter);
        List<ClientDTO> searched = applySearch(filtered, filter);
        List<ClientDTO> sorted = SortProvider.sort(
                searched,
                filter.getSortBy(),
                filter.getSortOrder() != null ? filter.getSortOrder().toString() : null,
                ClientComparators.COMPARATORS);
        int fromIndex = (int) Math.max(0, (page - 1) * itemsPerPage);
        int toIndex = Math.min(fromIndex + (int) itemsPerPage, sorted.size());
        if (fromIndex >= toIndex) {
            return new ArrayList<>();
        }
        return sorted.subList(fromIndex, toIndex);
    }

    @Override
    public long getTotalItemsCount(Filter filter) {
        List<ClientDTO> base = (getAllClients());
        return applySearch(applyFilters(base, filter), filter).size();
    }

    private List<ClientDTO> applyFilters(List<ClientDTO> clients, Filter filter) {
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
    private List<ClientDTO> applySearch(List<ClientDTO> clients, Filter filter) {
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
