package com.bcm.shared.pagination.sort;

import java.util.Comparator;
import java.util.Map;

import com.bcm.cluster_manager.model.api.BigClientDTO;

public class ClientComparators {
    /**
     * Registry of all available comparators for ClientDTO
     */
    public static final Map<String, Comparator<BigClientDTO>> COMPARATORS = Map.of(
            "id", SortProvider.comparing(BigClientDTO::getId),
            "nameOrIp", SortProvider.comparingIgnoreCase(BigClientDTO::getNameOrIp),
            "enabled", SortProvider.comparing(BigClientDTO::isEnabled)
        );
}
