package com.bcm.cluster_manager.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.bcm.shared.pagination.PaginationProvider;
import org.springframework.stereotype.Service;

import com.bcm.shared.model.api.BackupDTO;

@Service
public class BackupService implements PaginationProvider<BackupDTO> {
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
    public long getTotalItemsCount() {
        // Should make a call to the DB to get the actual count
        return exampleBackups.size();
    }

    @Override
    public List<BackupDTO> getDBItems(long page, long itemsPerPage) {
        // Should make a call to the DB to get the actual items
        return exampleBackups.stream()
                .skip((page - 1) * itemsPerPage)
                .limit(itemsPerPage)
                .toList();
    }
}
