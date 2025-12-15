package com.bcm.cluster_manager.service.pagination.shared;

import com.bcm.cluster_manager.model.api.BigBackupDTO;
import com.bcm.cluster_manager.model.api.BigTaskDTO;
import com.bcm.shared.model.api.TaskDTO;
import com.bcm.shared.pagination.sort.NodeComparators;
import com.bcm.shared.pagination.sort.SortProvider;
import com.bcm.shared.pagination.sort.TaskComparators;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class BigTaskComparators {

    public static final Map<String, Comparator<BigTaskDTO>> COMPARATORS;
    static {
        Map<String, Comparator<BigTaskDTO>> map = new HashMap<>();
        // Inherit from TaskComparators
        TaskComparators.COMPARATORS.forEach((key, parentComparator) -> {
            map.put(key, parentComparator::compare);
        });

        // Add NodeDTO comparators, wrapped to work on BigBackupDTO
        NodeComparators.COMPARATORS.forEach((key, nodeComparator) -> {
            Comparator<BigTaskDTO> wrappedComparator =
                    Comparator.comparing(BigTaskDTO::getNodeDTO, nodeComparator);

            // This maps "address" -> "address", but "id" -> "node.id" to avoid clashes
            if (map.containsKey(key)) {
                map.put("node." + key, wrappedComparator);
            } else {
                map.put(key, wrappedComparator);
            }
        });

        COMPARATORS = Map.copyOf(map);
    }
}

