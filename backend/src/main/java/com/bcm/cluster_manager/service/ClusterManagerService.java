package com.bcm.cluster_manager.service;


import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.pagination.PaginationProvider;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ClusterManagerService implements PaginationProvider<NodeDTO> {
    public List<NodeDTO> exampleNodes;

    public ClusterManagerService(){
        int numBackups = 1000;
        List<NodeDTO> list = new ArrayList<>();
        for (int i = 1; i <= numBackups; i++) {
            list.add(new NodeDTO(
                    (long) i,
                    "Node " + i,
                    "www.google.com",
                    "active",
                    LocalDateTime.now().minusDays(i)
            ));
        }
        exampleNodes = list;
    }

    @Override
    public long getTotalItemsCount() {
        // Should make a call to the DB to get the actual count
        return exampleNodes.size();
    }

    @Override
    public List<NodeDTO> getDBItems(long page, long itemsPerPage) {
        // Should make a call to the DB to get the actual items
        return exampleNodes.stream()
                .skip((page - 1) * itemsPerPage)
                .limit(itemsPerPage)
                .toList();
    }
}
