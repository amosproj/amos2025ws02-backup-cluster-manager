package com.bcm.cluster_manager.service;

import com.bcm.shared.filter.Filter;
import com.bcm.shared.filter.FilterProvider;
import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.pagination.PaginationProvider;
import com.bcm.shared.sort.NodeComparators;
import com.bcm.shared.sort.SortProvider;

import ch.qos.logback.core.pattern.parser.Node;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
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
        if (filter != null && filter.getFilters() != null) {
            // Filters are available, now all of them as a list
            List<String> filters = new ArrayList<>(List.of(filter.getFilters().toLowerCase().split(",")));

            List<NodeDTO> filteredNodes = nodes;
            // Build a pipe to apply all filters after another to the nodes list
            // 1. Active filter
            if( filters.contains("active")) {
                filteredNodes = filteredNodes.stream()
                        .filter(node -> node.getStatus().equalsIgnoreCase("active"))
                        .toList();
            }
            // 2. Inactive filter
            if( filters.contains("inactive")) {
                filteredNodes = filteredNodes.stream()
                        .filter(node -> node.getStatus().equalsIgnoreCase("inactive"))
                        .toList();
            }
            // Add additional filters here if needed

            return filteredNodes;
        }
        return nodes;
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