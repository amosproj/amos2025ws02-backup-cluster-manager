package com.bcm.cluster_manager;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import com.bcm.shared.pagination.PaginationProvider;
import com.bcm.shared.pagination.PaginationRequest;
import com.bcm.shared.pagination.PaginationResponse;
import org.springframework.stereotype.Service;

import com.bcm.shared.model.BackupDTO;

@Service
public class BackupService extends PaginationProvider<BackupDTO> {

    @Override
    protected long getTotalItemsCount() {
        // Should make a call to the DB to get the actual count
        return 0;
    }

    @Override
    protected List<BackupDTO> getDBItems(long page, long itemsPerPage) {
        // Should make a call to the DB to get the actual items
        return Arrays.asList();
    }
}
