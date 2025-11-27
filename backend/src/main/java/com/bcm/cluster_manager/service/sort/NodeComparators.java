package com.bcm.cluster_manager.service.sort;

import com.bcm.shared.model.api.NodeDTO;
import com.bcm.shared.sort.SortProvider;

import java.util.Comparator;
import java.util.Map;

public class NodeComparators {
    
    /**
     * Registry of all available comparators for NodeDTO
     */
    public static final Map<String, Comparator<NodeDTO>> COMPARATORS = Map.of(
        "id", SortProvider.comparing(NodeDTO::getId),
        "name", SortProvider.comparingIgnoreCase(NodeDTO::getName),
        "status", SortProvider.comparing(NodeDTO::getStatus),
        "address", SortProvider.comparingIgnoreCase(NodeDTO::getAddress),
        "createdat", SortProvider.comparing(NodeDTO::getCreatedAt)
    );
}
