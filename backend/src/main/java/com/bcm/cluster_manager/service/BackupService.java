package com.bcm.cluster_manager.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.bcm.shared.filter.Filter;
import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.pagination.PaginationProvider;
import org.springframework.stereotype.Service;

import com.bcm.shared.model.api.BackupDTO;

@Service
public class BackupService implements PaginationProvider<BackupDTO> {
    @Override
    public long getTotalItemsCount(Filter filter) {
        // Add SQL query with filter to get the actual count
        return 5;
    }

    @Override
    public List<BackupDTO> getDBItems(long page, long itemsPerPage, Filter filter) {
        // Add SQL query with filter and pagination to get the actual items
        return new ArrayList<>(List.of());
    }
}
