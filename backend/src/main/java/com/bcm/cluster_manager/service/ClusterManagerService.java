package com.bcm.cluster_manager.service;

import com.bcm.shared.filter.Filter;
import com.bcm.shared.filter.FilterProvider;
import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.pagination.PaginationProvider;
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
        List<NodeDTO> filtered = FilterProvider.filterNodes(new ArrayList<>(registry.getAllNodes()), filter);
        List<NodeDTO> sorted = SortProvider.sort(filtered, filter.getSortBy(), filter.getSortOrder().toString(), null); // Add sorting logic if needed
        return sorted;
    }
}