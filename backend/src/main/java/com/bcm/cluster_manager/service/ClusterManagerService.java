package com.bcm.cluster_manager.service;

import com.bcm.shared.filter.Filter;
import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.pagination.PaginationProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClusterManagerService implements PaginationProvider<NodeDTO> {
    @Override
    public long getTotalItemsCount(Filter filter) {
        // Add SQL query with filter to get the actual count
        return 5;
    }

    @Override
    public List<NodeDTO> getDBItems(long page, long itemsPerPage, Filter filter) {
        // Add SQL query with filter and pagination to get the actual items
        return new ArrayList<>(List.of());
    }
}