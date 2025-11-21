package com.bcm.cluster_manager.service;

import com.bcm.shared.filter.Filter;
import com.bcm.shared.filter.FilterProvider;
import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.pagination.PaginationProvider;
import com.bcm.shared.sort.NodeComparators;
import com.bcm.shared.sort.SortProvider;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClusterManagerService implements PaginationProvider<NodeDTO> {
    @Autowired
    private RegistryService registry;


    @Override
    public long getTotalItemsCount(Filter filter) {
        // Add SQL query with filter to get the actual count
        List<NodeDTO> filtered = FilterProvider.filterNodes(new ArrayList<>(registry.getAllNodes()), filter);
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


    // Helper Methods goes here
    private List<NodeDTO> applyFilters(List<NodeDTO> nodes, Filter filter) {
        if (filter == null || filter.getFilters() == null || filter.getFilters().isBlank()) {
            return nodes;
        }

        // Normalize filters to a set (lowercase, trimmed) to avoid duplicates
        List<String> raw = List.of(filter.getFilters().split(","));
        var normalized = raw.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .toList();

        boolean hasActive = normalized.contains("active");
        boolean hasInactive = normalized.contains("inactive");
        // If both active & inactive are requested, no status exclusion is needed (include all matching those two)
        boolean filterStatus = hasActive ^ hasInactive; // true if exactly one of them present

        return nodes.stream()
                .filter(node -> {
                    if (filterStatus) {
                        if (hasActive && !node.getStatus().equalsIgnoreCase("active")) return false;
                        if (hasInactive && !node.getStatus().equalsIgnoreCase("inactive")) return false;
                    }
                    // Future filter types can be added here without extra passes over the list.
                    return true;
                })
                .toList();
    }

    // Search implementation
    // Searches in name, address, status and id fields
    private List<NodeDTO> applySearch(List<NodeDTO> nodes, Filter filter){
        if (filter != null && filter.getSearch() != null && !filter.getSearch().isBlank()) {
            String searchTerm = filter.getSearch().toLowerCase();
            return nodes.stream()
                    .filter(node -> node.getName().toLowerCase().contains(searchTerm) ||
                                    node.getAddress().toLowerCase().contains(searchTerm) ||
                                    node.getStatus().toLowerCase().contains(searchTerm) ||
                                    node.getId().toString().contains(searchTerm)
                    )
                    .toList();
        }
        return nodes;
    }
}