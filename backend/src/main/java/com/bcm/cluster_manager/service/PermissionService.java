package com.bcm.cluster_manager.service;

import com.bcm.cluster_manager.model.api.RolePermissionDTO;
import com.bcm.shared.config.permissions.Permission;
import com.bcm.shared.pagination.PaginationProvider;
import com.bcm.shared.pagination.filter.Filter;
import com.bcm.shared.pagination.sort.RoleComparators;
import com.bcm.shared.pagination.sort.SortProvider;
import com.bcm.shared.config.permissions.Role;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PermissionService implements PaginationProvider<RolePermissionDTO> {

    Flux<RolePermissionDTO> getAllRolesAndPermissions() {
        return Flux.fromArray(Role.values())
                .map(role -> {
                    RolePermissionDTO pv = new RolePermissionDTO();
                    pv.setRole(role.name());
                    String perms = role.getPermissions().stream()
                            .sorted()
                            .map(Permission::getPermission)
                            .collect(Collectors.joining(", "));
                    pv.setPermissions(perms);
                    return pv;
                });
    }

    @Override
    public Mono<Long> getTotalItemsCount(Filter filter) {
        // Add SQL query with filter to get the actual count
        return getAllRolesAndPermissions()
                .transform(flux -> applyFilters(flux, filter))
                .transform(flux -> applySearch(flux, filter))
                .count();
    }

    @Override
    public Mono<List<RolePermissionDTO>> getDBItems(long page, long itemsPerPage, Filter filter) {
        // Add SQL query with filter and pagination to get the actual items
        return getAllRolesAndPermissions()
                .transform(flux -> applyFilters(flux, filter))
                .transform(flux -> applySearch(flux, filter))
                // Sorting is easier to do on the full list unless you have a custom reactive sorter
                .collectList()
                .map(list -> {
                    // Apply Sorting
                    List<RolePermissionDTO> sorted = SortProvider.sort(
                            list,
                            filter.getSortBy(),
                            filter.getSortOrder().toString(),
                            RoleComparators.COMPARATORS
                    );

                    // Apply Pagination logic
                    int fromIndex = (int) ((page - 1) * itemsPerPage);
                    if (fromIndex >= sorted.size()) {
                        return Collections.<RolePermissionDTO>emptyList();
                    }

                    int toIndex = Math.min(fromIndex + (int) itemsPerPage, sorted.size());
                    return sorted.subList(fromIndex, toIndex);
                });
    }

    private Flux<RolePermissionDTO> applyFilters(Flux<RolePermissionDTO> roles, Filter filter) {
        if (filter == null || filter.getFilters() == null || filter.getFilters().isEmpty()) {
            return roles;
        }

        Set<String> requestedRoles = filter.getFilters().stream()
                .filter(StringUtils::hasText)
                .map(String::toUpperCase)
                .collect(Collectors.toSet());

        if (requestedRoles.isEmpty()) return roles;

        return roles.filter(dto -> requestedRoles.contains(dto.getRole().toUpperCase()));
    }

    private Flux<RolePermissionDTO> applySearch(Flux<RolePermissionDTO> roles, Filter filter) {
        if (filter == null || !StringUtils.hasText(filter.getSearch())) {
            return roles;
        }
        String searchTerm = filter.getSearch().toLowerCase();
        return roles.filter(role ->
                (role.getRole() != null && role.getRole().toLowerCase().contains(searchTerm)) ||
                        (role.getPermissions() != null && role.getPermissions().toLowerCase().contains(searchTerm))
        );
    }
}
