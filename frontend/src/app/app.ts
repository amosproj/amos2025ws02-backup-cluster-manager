import { Component, signal } from '@angular/core';
import { RouterOutlet, RouterLink } from '@angular/router';
import { OnInit } from '@angular/core';
import { initFlowbite } from 'flowbite';
import {routes} from './app.routes';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit{
  protected readonly title = signal('frontend');

  // Routes should be added here, or directly in app.routes.ts
  protected readonly navigationItems = signal([
    {
      label: "Users",
      icon:"/users.svg",
      route: "users"
    },
    {
      label: "Permissions",
      icon: "/key.svg",
      route: "permissions"
    },
    {
      label: "Clusters",
      icon: "/server.svg",
      route: "clusters"
    },
    {
      label: "Nodes",
      icon: "/hard-drive.svg",
      route: "nodes"
    },
    {
      label:"Backups",
      icon: "/database.svg",
      route: "backups"
    }
  ]);

  ngOnInit(): void {
    initFlowbite();
  }
}
