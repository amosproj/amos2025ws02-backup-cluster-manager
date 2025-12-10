enum UserPermissionsEnum {
  // User Page Permissions
  UserRead ="user:read",
  UserCreate ="user:create",
  UserUpdate ="user:update",
  UserDelete ="user:delete",

  // Nodes Page Permissions
  NodeRead ="node:read",
  NodeCreate="node:create",
  NodeDelete="node:delete",

  // Backups Page Permissions
  BackupRead ="backup:read",
  BackupCreate ="backup:create",
  BackupDelete ="backup:delete",

  // Tasks Page Permissions
  TaskRead ="task:read",
  TaskCreate ="task:create",
  TaskDelete ="task:delete",
}

export default UserPermissionsEnum;
