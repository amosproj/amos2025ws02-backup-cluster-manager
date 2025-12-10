package com.bcm.shared.config.permissions;

public enum Permission {
    // Users
    USER_READ("user:read"),
    USER_CREATE("user:create"),
    USER_UPDATE("user:update"),
    USER_DELETE("user:delete"),

    // Nodes
    NODE_READ("node:read"),
    NODE_CREATE("node:create"),
    NODE_DELETE("node:delete"),

    // Backups
    BACKUP_READ("backup:read"),
    BACKUP_CREATE("backup:create"),
    BACKUP_DELETE("backup:delete"),

    // Tasks
    TASK_READ("task:read"),
    TASK_CREATE("task:create"),
    TASK_DELETE("task:delete");

    private final String permission;

    Permission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }

    // Static constants for use in Annotations
    public static class Require {
        // Users
        public static final String USER_READ = "hasAuthority('user:read')";
        public static final String USER_CREATE = "hasAuthority('user:create')";
        public static final String USER_UPDATE = "hasAuthority('user:update')";
        public static final String USER_DELETE = "hasAuthority('user:delete')";

        // Nodes
        public static final String NODE_READ = "hasAuthority('node:read')";
        public static final String NODE_CREATE = "hasAuthority('node:create')";
        public static final String NODE_DELETE = "hasAuthority('node:delete')";

        // Backups
        public static final String BACKUP_READ = "hasAuthority('backup:read')";
        public static final String BACKUP_CREATE = "hasAuthority('backup:create')";
        public static final String BACKUP_DELETE = "hasAuthority('backup:delete')";

        // Tasks
        public static final String TASK_READ = "hasAuthority('task:read')";
        public static final String TASK_CREATE = "hasAuthority('task:create')";
        public static final String TASK_DELETE = "hasAuthority('task:delete')";

    }
}