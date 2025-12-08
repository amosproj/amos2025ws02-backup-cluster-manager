import {Component, signal, OnInit, computed} from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router, NavigationEnd } from '@angular/router';
import { initFlowbite } from 'flowbite';
import { CommonModule } from '@angular/common';
import { filter } from 'rxjs/operators';
import { AuthService } from './core/services/auth.service';
import {Toast} from './shared/components/toast/toast';
import {ToastMessage, ToastTypeEnum} from './shared/types/toast';
import {ToastService} from './core/services/toast.service';
import UserPermissionsEnum from './shared/types/Permissions';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule, Toast],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit{
  constructor(
    private router: Router,
    protected authService: AuthService,
    protected toast: ToastService
  ) {
    // Listen to route changes to hide/show layout
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event) => {
        const navigationEndEvent = event as NavigationEnd;
        // Hide layout on login page
        this.showLayout.set(!navigationEndEvent.url.includes('/login'));
      });
  }

  protected readonly showLayout = signal(true);

  protected readonly allNavigationItems = signal([
    {
      label: "Users",
      icon:"/users.svg",
      route: "users",
      requiredPermission: UserPermissionsEnum.UserRead
    },
    {
      label: "Nodes",
      icon: "/hard-drive.svg",
      route: "nodes",
      requiredPermission: UserPermissionsEnum.NodeRead
    },
    {
      label:"Backups",
      icon: "/database.svg",
      route: "backups",
      requiredPermission: UserPermissionsEnum.BackupRead
    },
    {
      label: "Tasks",
      icon: "/tasks.svg",
      route: "tasks",
      requiredPermission: UserPermissionsEnum.TaskRead
    },
  ]);

  protected readonly navigationItems = computed(()=>{
    const permissions = this.authService.getPermissions();
    return this.allNavigationItems().filter(item=>
      !item.requiredPermission || this.authService.hasPermission(item.requiredPermission)
    )
  })





  ngOnInit(): void {
    initFlowbite();
    // Check initial route
    this.showLayout.set(!this.router.url.includes('/login'));
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigate(['/login']);
        this.toast.show("Logout successfully.", ToastTypeEnum.SUCCESS);
      },
      error: (err) => {
        console.error('Logout failed', err);
        this.toast.show("Logout failed. Please try again.", ToastTypeEnum.ERROR);
      }
    })
  }
}

