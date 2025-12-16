package com.bcm.cluster_manager.service.pagination.shared;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.bcm.cluster_manager.model.api.BigClientDTO;
import com.bcm.shared.pagination.sort.ClientComparators;
import com.bcm.shared.pagination.sort.NodeComparators;

public class BigClientComparators {
    public static final Map<String, Comparator<BigClientDTO>> COMPARATORS;
    static {
        Map<String, Comparator<BigClientDTO>> map = new HashMap<>();
        // Inherit from TaskComparators
        ClientComparators.COMPARATORS.forEach((key, parentComparator) -> {
            map.put(key, parentComparator::compare);
        });

        // Add NodeDTO comparators, wrapped to work on BigBackupDTO
        NodeComparators.COMPARATORS.forEach((key, nodeComparator) -> {
            Comparator<BigClientDTO> wrappedComparator = Comparator.comparing(BigClientDTO::getNodeDTO, nodeComparator);

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
