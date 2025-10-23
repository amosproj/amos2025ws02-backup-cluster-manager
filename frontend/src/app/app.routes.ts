import { Routes } from '@angular/router';
import {Dashboard} from './features/dashboard/dashboard';
import {ExampleRoute} from './features/example-route/example-route';

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
];
