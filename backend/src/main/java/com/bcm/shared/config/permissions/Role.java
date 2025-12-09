package com.bcm.shared.config.permissions;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static com.bcm.shared.config.permissions.Permission.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public enum Role {
    SUPERUSER(100, Set.of(
            USER_READ, USER_CREATE, USER_UPDATE, USER_DELETE,
            NODE_READ, NODE_CREATE, NODE_DELETE,
            BACKUP_READ, BACKUP_CREATE, BACKUP_DELETE,
            TASK_READ, TASK_CREATE, TASK_DELETE
    )),
    ADMINISTRATORS(50, Set.of(
            USER_READ, USER_CREATE, USER_UPDATE, USER_DELETE,
            NODE_READ, NODE_CREATE, NODE_DELETE,
            BACKUP_READ, BACKUP_CREATE, BACKUP_DELETE,
            TASK_READ, TASK_CREATE, TASK_DELETE
    )),
    OPERATORS(1, Set.of(
            NODE_READ, NODE_CREATE, NODE_DELETE,
            BACKUP_READ, BACKUP_CREATE, BACKUP_DELETE,
            TASK_READ, TASK_CREATE, TASK_DELETE
    )),
    RESTORE_USERS(1, Set.of(
            BACKUP_READ, BACKUP_CREATE
    )),
    BACKUP_USERS(1, Set.of(
            BACKUP_READ, BACKUP_CREATE, BACKUP_DELETE
    ));

    private final int rank;
    private final Set<Permission> permissions;

    Role(int rank, Set<Permission> permissions) {
        this.rank = rank;
        this.permissions = permissions;
    }

    public int getRank() {
        return rank;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }


    // This converts Enum data into what Spring Security understands
    public List<SimpleGrantedAuthority> getAuthorities() {
        // 1. Add all granular permissions (e.g., "user:read")
        var authorities = getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toList());

        // 2. Add the Role itself (e.g., "ROLE_ADMIN")
        // Spring Security usually expects "ROLE_" prefix for role checks
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));

        return authorities;
    }
}
