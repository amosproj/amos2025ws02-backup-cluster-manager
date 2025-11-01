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
      icon:"/users.svg"
    },
    {
      label: "Permissions",
      icon: "/key.svg"
    },
    {
      label: "Cluster",
      icon: "/server.svg"
    },
    {
      label: "Nodes",
      icon: "/hard-drive.svg"
    },
    {
      label:"Backups",
      icon: "/database.svg"
    }
  ]);

  ngOnInit(): void {
    initFlowbite();
  }
}
