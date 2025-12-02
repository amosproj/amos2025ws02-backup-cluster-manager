import {Injectable, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, of, tap, catchError, map} from 'rxjs';
import {Router} from '@angular/router';
import {environment} from '../../../environments/environment';
import {ApiService} from './api.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private baseUrl = "http://localhost:8080/api/v1/auth";
  private isAuthenticatedSignal = signal<boolean>(false);
  private checkingAuth = false;

  constructor(
    private router: Router,
    private http: HttpClient,
    private apiService: ApiService
  ) {
    // Check authentication status on service initialization
    this.checkAuthStatus();
  }

  // Public method to get authentication status
  isAuthenticated(): boolean {
    return this.isAuthenticatedSignal();
  }

  // Internal method to check authentication status
  private checkAuthStatus(): void {
    if (this.checkingAuth) return;
    this.checkingAuth = true;

    // Check session validity with backend
    this.http.post(`${this.baseUrl}/check-auth`, {}, {observe: 'response', withCredentials: true})
      .subscribe({
        next: (response) => {
          this.isAuthenticatedSignal.set(response.status === 200);
          this.checkingAuth = false;
        },
        error: () => {
          this.isAuthenticatedSignal.set(false);
          this.checkingAuth = false;
        }
      });
  }

  // Login method - Authenticate with backend
  login(username: string, password: string): Observable<boolean> {
    return this.http.post(`${this.baseUrl}/login`,
      {username, password},
      {observe: 'response', withCredentials: true}
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

    return this.http.post(`${this.baseUrl}/logout`, {} , {observe: 'response', withCredentials: true}
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

