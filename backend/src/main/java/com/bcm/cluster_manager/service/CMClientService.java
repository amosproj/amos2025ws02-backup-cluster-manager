package com.bcm.cluster_manager.service;

import com.bcm.cluster_manager.model.api.BigClientDTO;
import com.bcm.cluster_manager.service.pagination.shared.BigClientComparators;
import com.bcm.shared.pagination.PaginationProvider;
import com.bcm.shared.pagination.filter.Filter;
import com.bcm.shared.pagination.sort.SortProvider;
import com.bcm.shared.model.api.NodeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class CMClientService implements PaginationProvider<BigClientDTO> {

    @Autowired
    private RegistryService registryService;

    @Autowired
    private RestTemplate restTemplate;

    public List<BigClientDTO> getAllClients() {
        Collection<NodeDTO> nodeAddresses = registryService.getActiveNodes();
        if (nodeAddresses.isEmpty()) return List.of();

        List<CompletableFuture<BigClientDTO[]>> futures = nodeAddresses.stream().map(node -> CompletableFuture.supplyAsync(() -> {
            try {
                String url = "http://" + node.getAddress() + "/api/v1/bn/clients";
                ResponseEntity<BigClientDTO[]> response = restTemplate.getForEntity(url, BigClientDTO[].class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return Arrays.stream(response.getBody()).map(BigClientDTO -> {;
                        BigClientDTO bigClientDto = new BigClientDTO();
                        bigClientDto.setId(BigClientDTO.getId());
                        bigClientDto.setNameOrIp(BigClientDTO.getNameOrIp());
                        bigClientDto.setEnabled(BigClientDTO.isEnabled());
                        bigClientDto.setNodeDTO(node);
                        return bigClientDto;
                    }).toArray(BigClientDTO[]::new);
                }
            } catch (Exception e) {
                System.out.println("Fehler beim Abruf von Tasks von Node " + node.getAddress());
            }
            return null;
        })).toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<BigClientDTO> allClients = new ArrayList<>();
        //Set<Long> seenIds = new HashSet<>();

        for (CompletableFuture<BigClientDTO[]> future : futures) {
            try {
                BigClientDTO[] clients = future.get();
                if (clients != null) {
                    for (BigClientDTO client : clients) {
                        //if (client != null && seenIds.add(client.getId())) {
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
    public List<BigClientDTO> getDBItems(long page, long itemsPerPage, Filter filter) {
        List<BigClientDTO> allClients = (getAllClients());

        List<BigClientDTO> filtered = applyFilters(allClients, filter);
        List<BigClientDTO> searched = applySearch(filtered, filter);
        List<BigClientDTO> sorted = SortProvider.sort(
                searched,
                filter.getSortBy(),
                filter.getSortOrder() != null ? filter.getSortOrder().toString() : null,
                BigClientComparators.COMPARATORS);
        int fromIndex = (int) Math.max(0, (page - 1) * itemsPerPage);
        int toIndex = Math.min(fromIndex + (int) itemsPerPage, sorted.size());
        if (fromIndex >= toIndex) {
            return new ArrayList<>();
        }
        return sorted.subList(fromIndex, toIndex);
    }

    @Override
    public long getTotalItemsCount(Filter filter) {
        List<BigClientDTO> base = (getAllClients());
        return applySearch(applyFilters(base, filter), filter).size();
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
