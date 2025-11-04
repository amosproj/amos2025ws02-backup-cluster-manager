import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  imports: [],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard {
  userCount = 42;
  permissionCount = 15;
  clusterCount = 3;
  nodeCount = 12;
  backupCount = 8;
  cpuUsage = 67;
  memoryUsage = 54;

  constructor(private router: Router) {}

  onButtonClick(action: string) {
    console.log(`Button clicked: ${action}`);

    switch(action) {
      case 'add-user':
      case 'edit-user':
        this.router.navigate(['/users']);
        break;
      case 'add-server':
      case 'edit-server':
        this.router.navigate(['/nodes']);
        break;
      case 'add-permission':
      case 'edit-permission':
        this.router.navigate(['/permissions']);
        break;
    }
  }

  onCardClick(card: string) {

    switch(card) {
      case 'list-users':
        this.router.navigate(['/users']);
        break;
      case 'list-permissions':
        this.router.navigate(['/permissions']);
        break;
      case 'list-cluster':
        this.router.navigate(['/clusters']);
        break;
      case 'list-nodes':
        this.router.navigate(['/nodes']);
        break;
      case 'list-backups':
        this.router.navigate(['/backups']);
        break;
      case 'performance-1':
      case 'performance-2':
        break;
    }
  }

  onLogout() {
    console.log('Logout initiated');
    if (confirm('Are you sure you want to log out?')) {
      console.log('Logging out...');
    }
  }

}
