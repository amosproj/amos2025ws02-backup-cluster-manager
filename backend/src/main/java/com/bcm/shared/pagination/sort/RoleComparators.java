package com.bcm.shared.pagination.sort;

import com.bcm.cluster_manager.model.api.RolePermissionDTO;

import java.util.Comparator;
import java.util.Map;

public class RoleComparators {

    /**
     * Registry of all available comparators for PermissionDTO
     */
    public static final Map<String, Comparator<RolePermissionDTO>> COMPARATORS = Map.of(
            "role", SortProvider.comparing(RolePermissionDTO::getRole),
            "permissions", SortProvider.comparingIgnoreCase(RolePermissionDTO::getPermissions)
    );
}
