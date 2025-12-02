import { Routes } from '@angular/router';
import {Dashboard} from './features/dashboard/dashboard';
import {ExampleRoute} from './features/example-route/example-route';
import { Users } from './features/users/users';
import { Permissions } from './features/permissions/permissions';
import { Clusters } from './features/clusters/clusters';
import { Nodes } from './features/nodes/nodes';
import { Backups } from './features/backups/backups';
import {Tasks} from './features/tasks/tasks';
import {Login} from './features/login/login';
import { authGuard } from './core/guards/auth.guard';

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
    canActivate: [authGuard],
  },
  {
    path: 'permissions',
    component: Permissions,
    title: 'Permissions',
    canActivate: [authGuard],
  },
  {
    path: 'clusters',
    component: Clusters,
    title: 'Clusters',
    canActivate: [authGuard],
  },
  {
    path: 'nodes',
    component: Nodes,
    title: 'Nodes',
    canActivate: [authGuard],
  },
  {
    path: 'backups',
    component: Backups,
    title: 'Backups',
    canActivate: [authGuard],
  },
  {
    path: 'tasks',
    component: Tasks,
    title: 'Tasks',
    canActivate: [authGuard],
  },
  {
    path: '**',
    redirectTo: '',
    pathMatch: 'full',
  },
];
