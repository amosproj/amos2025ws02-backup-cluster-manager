package com.bcm.cluster_manager.service.pagination.shared;

import com.bcm.cluster_manager.model.api.BigBackupDTO;
import com.bcm.shared.pagination.sort.BackupComparators;
import com.bcm.shared.pagination.sort.NodeComparators;
import com.bcm.shared.pagination.sort.SortProvider;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class BigBackupComparators{

    public static final Map<String, Comparator<BigBackupDTO>> COMPARATORS;
    static {
        Map<String, Comparator<BigBackupDTO>> map = new HashMap<>();

        // Inherit from BackupComparators
        BackupComparators.COMPARATORS.forEach((key, parentComparator) -> {
            map.put(key, parentComparator::compare);
        });

        // Add NodeDTO comparators, wrapped to work on BigBackupDTO
        NodeComparators.COMPARATORS.forEach((key, nodeComparator) -> {
            Comparator<BigBackupDTO> wrappedComparator =
                    Comparator.comparing(BigBackupDTO::getNodeDTO, nodeComparator);

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

