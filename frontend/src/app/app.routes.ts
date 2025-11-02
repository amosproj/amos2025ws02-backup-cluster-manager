import { Routes } from '@angular/router';
import {Dashboard} from './features/dashboard/dashboard';
import {ExampleRoute} from './features/example-route/example-route';
import { Users } from './features/users/users';
import { Permissions } from './features/permissions/permissions';
import { Clusters } from './features/clusters/clusters';
import { Nodes } from './features/nodes/nodes';
import { Backups } from './features/backups/backups';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    component: Dashboard,
    title: 'Dashboard',
  },
  {
    path: 'example-route',
    component: ExampleRoute,
    title: 'Example Route',
  },
  {
    path: 'users',
    component: Users,
    title: 'Users',
  },
  {
    path: 'permissions',
    component: Permissions,
    title: 'Permissions',
  },
  {
    path: 'clusters',
    component: Clusters,
    title: 'Clusters',
  },
  {
    path: 'nodes',
    component: Nodes,
    title: 'Nodes',
  },
  {
    path: 'backups',
    component: Backups,
    title: 'Backups',
  },
];
