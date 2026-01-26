package com.bcm.shared.config.permissions;

public enum Permission {

    // Permissions

    PERMISSION_READ("permission:read"),

    // Users
    USER_READ("user:read"),
    USER_CREATE("user:create"),
    USER_UPDATE("user:update"),
    USER_DELETE("user:delete"),

    // Nodes
    NODE_READ("node:read"),
    NODE_CREATE("node:create"),
    NODE_UPDATE("node:update"),
    NODE_DELETE("node:delete"),
    NODE_CONTROL("node:control"),

    // Clients
    CLIENT_READ("client:read"),
    CLIENT_CREATE("client:create"),
    CLIENT_UPDATE("client:update"),
    CLIENT_DELETE("client:delete"), 

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

        private static final String IS_DISABLED = "(@environment.getProperty('application.security.enabled', 'false') == 'false')";

        // Permissions

        public static final String PERMISSION_READ = IS_DISABLED + " or hasAuthority('permission:read')";

        // Users
        public static final String USER_READ = IS_DISABLED + " or hasAuthority('user:read')";
        public static final String USER_CREATE = IS_DISABLED + " or hasAuthority('user:create')";
        public static final String USER_UPDATE = IS_DISABLED + " or hasAuthority('user:update')";
        public static final String USER_DELETE = IS_DISABLED + " or hasAuthority('user:delete')";

        // Nodes
        public static final String NODE_READ = IS_DISABLED + " or hasAuthority('node:read')";
        public static final String NODE_CREATE = IS_DISABLED + " or hasAuthority('node:create')";
        public static final String NODE_UPDATE = IS_DISABLED + " or hasAuthority('node:update')";
        public static final String NODE_DELETE = IS_DISABLED + " or hasAuthority('node:delete')";
        public static final String NODE_CONTROL = IS_DISABLED + " or hasAuthority('node:control')";

        // Clients
        public static final String CLIENT_READ = IS_DISABLED + " or hasAuthority('client:read')";
        public static final String CLIENT_CREATE = IS_DISABLED + " or hasAuthority('client:create')";
        public static final String CLIENT_UPDATE = IS_DISABLED + " or hasAuthority('client:update')";
        public static final String CLIENT_DELETE = IS_DISABLED + " or hasAuthority('client:delete')";

        // Backups
        public static final String BACKUP_READ = IS_DISABLED + " or hasAuthority('backup:read')";
        public static final String BACKUP_CREATE = IS_DISABLED + " or hasAuthority('backup:create')";
        public static final String BACKUP_DELETE = IS_DISABLED + " or hasAuthority('backup:delete')";

        // Tasks
        public static final String TASK_READ = IS_DISABLED + " or hasAuthority('task:read')";
        public static final String TASK_CREATE = IS_DISABLED + " or hasAuthority('task:create')";
        public static final String TASK_DELETE = IS_DISABLED + " or hasAuthority('task:delete')";

    }
}