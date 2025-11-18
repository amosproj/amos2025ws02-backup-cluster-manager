package com.bcm.cluster_manager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.bcm.shared.pagination.PaginationProvider;
import com.bcm.shared.pagination.PaginationRequest;
import com.bcm.shared.pagination.PaginationResponse;
import org.springframework.stereotype.Service;

import com.bcm.shared.model.api.BackupDTO;

@Service
public class BackupService extends PaginationProvider<BackupDTO> {
    public List<BackupDTO> exampleBackups;

    public BackupService(){
        int numBackups = 1000;
        List<BackupDTO> list = new ArrayList<>();
        for (int i = 1; i <= numBackups; i++) {
            list.add(new BackupDTO(
                    (long) i,
                    "Backup " + i,
                    "active",
                    LocalDateTime.now().minusDays(i)
            ));
        }
        exampleBackups = list;
    }

    @Override
    protected long getTotalItemsCount() {
        // Should make a call to the DB to get the actual count
        return exampleBackups.size();
    }

    @Override
    protected List<BackupDTO> getDBItems(long page, long itemsPerPage) {
        // Should make a call to the DB to get the actual items
        return exampleBackups.stream()
                .skip((page - 1) * itemsPerPage)
                .limit(itemsPerPage)
                .toList();
    }
}
