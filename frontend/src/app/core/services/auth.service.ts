import {Injectable, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, of, catchError, map} from 'rxjs';

import {environment} from '../../../environments/environment';
import {Router} from '@angular/router';
import UserPermissionsEnum from '../../shared/types/Permissions';
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private baseUrl = environment.apiEndpoint + '/auth';
  private isAuthenticatedSignal = signal<boolean>(false);
  private permissionsSignal = signal<string[]>([]);
  private rankSignal = signal<number>(0);
  private roleSignal = signal<string>("");
  private userNameSignal = signal<string>("");

  constructor(
    private router: Router,
    private http: HttpClient
  ) {
    // Auth status will be checked by APP_INITIALIZER before app starts
  }

  getPermissions(): string[]{
    return this.permissionsSignal();
  }

  hasPermission(permission:UserPermissionsEnum): boolean {
    return this.permissionsSignal().includes(permission.toString());
  }

  // Public method to get authentication status
  isAuthenticated(): boolean {
    return this.isAuthenticatedSignal();
  }

  //method to check authentication status
  checkAuthStatus(): Observable<boolean> {
    // TODO: Implement real API call to check session validity
    return this.http.get(`${this.baseUrl}/validate`, {observe: 'response'}).pipe(
      map(response => {
        if (response.status === 200) {
          this.isAuthenticatedSignal.set(true);
          // Should get the permission from the response body
          // const responseBody = response.body && (response.body as any);
          // For now, set dummy permissions
          const metaData = {
            username:"user123",
            role:"ADMIN",
            rank:50,
            permissions:["user:read", "user:create","user:update","user:delete", "node:read", "backup:read", "task:read"] // etc
          }
          this.permissionsSignal.set(metaData.permissions);
          this.rankSignal.set(metaData.rank);
          this.roleSignal.set(metaData.role);
          this.userNameSignal.set(metaData.username);
          console.log("Session valid - Status 200");
          return true;
        } else {
          this.isAuthenticatedSignal.set(false);
          console.log("Session invalid - Status:", response.status);
          return false;
        }
      }),
      catchError(error => {
        console.error('Error checking auth status:', error);
        this.isAuthenticatedSignal.set(false);
        return of(false);
      })
    )
  }

  // Login method - Authenticate with backend
  login(username: string, password: string): Observable<boolean> {
    return this.http.post(`${this.baseUrl}/login`,
      {username, password},
      {observe: 'response'}
    ).pipe(
      map(response => {
        console.log('Login response status:', response.status);
        console.log('Login response body:', response.body);

        // Status 200 = Erfolg
        if (response.status === 200) {
          console.log("Login successful - Status 200");
          this.isAuthenticatedSignal.set(true);
          return true;
        } else {
          console.log("Login failed - Status:", response.status);
          this.isAuthenticatedSignal.set(false);
          return false;
        }
      }),
      catchError(error => {
        throw error;
      })
    );
  }

  // Logout method - Invalidate session on backend
  logout(): Observable<boolean> {
    console.log('Logging out...');

    return this.http.post(`${this.baseUrl}/logout`, {} , {observe: 'response'}
    ).pipe(
      map(response => {
        console.log('Logout response:', response);
        this.isAuthenticatedSignal.set(false);
        this.router.navigate(['/login']);
        return true;
      }),
      catchError(error => {
        console.error('Logout error:', error);
        this.isAuthenticatedSignal.set(false);
        throw error;
      }));
  };
}

