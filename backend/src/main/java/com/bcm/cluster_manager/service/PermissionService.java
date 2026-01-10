package com.bcm.cluster_manager.service;

import com.bcm.cluster_manager.model.api.RolePermissionDTO;
import com.bcm.shared.pagination.PaginationProvider;
import com.bcm.shared.pagination.filter.Filter;
import com.bcm.shared.pagination.sort.RoleComparators;
import com.bcm.shared.pagination.sort.SortProvider;
import com.bcm.shared.config.permissions.Role;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PermissionService implements PaginationProvider<RolePermissionDTO>  {

    Collection<RolePermissionDTO> getAllRolesAndPermissions() {
        return Arrays.stream(Role.values())
                .map(role -> {
                    RolePermissionDTO pv = new RolePermissionDTO();
                    pv.setRole(role.name());
                    pv.setPermissions(String.join(", ", role.getPermissions().toString()));
                    return pv;
                })
                .collect(Collectors.toList());
    }

    @Override
    public long getTotalItemsCount(Filter filter) {
        // Add SQL query with filter to get the actual count
        List<RolePermissionDTO> allNodes = new ArrayList<>(getAllRolesAndPermissions());
        List<RolePermissionDTO> filteredNodes = applyFilters(allNodes, filter);
        List<RolePermissionDTO> filtered = applySearch(filteredNodes, filter);

        return filtered.size();
    }

    @Override
    public List<RolePermissionDTO> getDBItems(long page, long itemsPerPage, Filter filter) {
        // Add SQL query with filter and pagination to get the actual items
        List<RolePermissionDTO> allNodes = new ArrayList<>(getAllRolesAndPermissions());
        List<RolePermissionDTO> filteredNodes = applyFilters(allNodes, filter);
        List<RolePermissionDTO> filtered = applySearch(filteredNodes, filter);
        List<RolePermissionDTO> sorted = SortProvider.sort(filtered, filter.getSortBy(), filter.getSortOrder().toString(), RoleComparators.COMPARATORS);
        // Pagination
        int fromIndex = (int) ((page - 1) * itemsPerPage);
        int toIndex = Math.min(fromIndex + (int) itemsPerPage, sorted.size());
        if (fromIndex > toIndex) {
            return new ArrayList<>();
        }
        sorted = sorted.subList(fromIndex, toIndex);
        return sorted;
    }

    private List<RolePermissionDTO> applyFilters(List<RolePermissionDTO> roles, Filter filter) {
        if (filter == null || filter.getFilters() == null || filter.getFilters().isEmpty()) {
            return roles;
        }

        var requested = filter.getFilters().stream()
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .map(s -> {
                    try {
                        return Role.valueOf(s);
                    } catch (IllegalArgumentException ex) {
                        return null;
                    }
                })
                .filter(ns -> ns != null)
                .distinct()
                .toList();

        // If no valid statuses parsed, return original list
        if (requested.isEmpty()) return roles;

        // If all possible statuses requested, skip filtering
        if (requested.size() == Role.values().length) return roles;

        return roles.stream().toList();
    }

    private List<RolePermissionDTO> applySearch(List<RolePermissionDTO> roles, Filter filter) {
        if (filter != null && StringUtils.hasText(filter.getSearch())) {
            String searchTerm = filter.getSearch().toLowerCase();
            return roles.stream()
                    .filter(role ->
                            (role.getRole() != null && role.getRole().toLowerCase().contains(searchTerm)) ||
                                    (role.getPermissions() != null && role.getPermissions().toLowerCase().contains(searchTerm))
                    )
                    .toList();
        }
        return roles;
    }
}
