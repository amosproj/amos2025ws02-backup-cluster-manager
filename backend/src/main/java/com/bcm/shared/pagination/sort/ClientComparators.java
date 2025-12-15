package com.bcm.shared.pagination.sort;

import java.util.Comparator;
import java.util.Map;

import com.bcm.shared.model.api.ClientDTO;

public class ClientComparators {
    /**
     * Registry of all available comparators for ClientDTO
     */
    public static final Map<String, Comparator<ClientDTO>> COMPARATORS = Map.of(
            "id", SortProvider.comparing(ClientDTO::getId),
            "nameOrIp", SortProvider.comparingIgnoreCase(ClientDTO::getNameOrIp),
            "enabled", SortProvider.comparing(ClientDTO::isEnabled));
}
