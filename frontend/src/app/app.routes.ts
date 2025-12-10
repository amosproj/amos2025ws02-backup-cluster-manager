import {Routes} from '@angular/router';
import {Dashboard} from './features/dashboard/dashboard';
import {ExampleRoute} from './features/example-route/example-route';
import {Users} from './features/users/users';
import {Nodes} from './features/nodes/nodes';
import {Backups} from './features/backups/backups';
import {Tasks} from './features/tasks/tasks';
import {Login} from './features/login/login';
import {authGuard} from './core/guards/auth.guard';
import {permissionGuard} from './core/guards/permission.guard';
import UserPermissionsEnum from './shared/types/Permissions';

export const routes: Routes = [
  {
    path: 'login',
    component: Login,
    title: 'Login',
  },
  {
    path: '',
    pathMatch: 'full',
    component: Dashboard,
    title: 'Dashboard',
    canActivate: [authGuard],
  },
  {
    path: 'example-route',
    component: ExampleRoute,
    title: 'Example Route',
    canActivate: [authGuard],
  },
  {
    path: 'users',
    component: Users,
    title: 'Users',
    canActivate: [authGuard, permissionGuard(UserPermissionsEnum.UserRead)],
  },
  {
    path: 'nodes',
    component: Nodes,
    title: 'Nodes',
    canActivate: [authGuard, permissionGuard(UserPermissionsEnum.NodeRead)],
  },
  {
    path: 'backups',
    component: Backups,
    title: 'Backups',
    canActivate: [authGuard, permissionGuard(UserPermissionsEnum.BackupRead)],
  },
  {
    path: 'tasks',
    component: Tasks,
    title: 'Tasks',
    canActivate: [authGuard, permissionGuard(UserPermissionsEnum.TaskRead)],
  },
  {
    path: '**',
    redirectTo: '',
    pathMatch: 'full',
  },
];
