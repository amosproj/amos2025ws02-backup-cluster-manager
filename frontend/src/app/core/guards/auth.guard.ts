import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { map, take } from 'rxjs';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // First check the cached authentication status
  if (!authService.isAuthenticated()) {
    console.log("AuthGuard: User not authenticated (cached), immediately redirecting to login");
    router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
    return false;
  }

  // If cached status shows authenticated, verify with backend
  return authService.checkAuthStatus().pipe(
    take(1), // Complete after first emission to prevent navigation issues
    map(isAuth => {
      if (isAuth) {
        console.log("AuthGuard: User is authenticated after check, allowing access to", state.url);
        return true;
      } else {
        console.log("AuthGuard: User is not authenticated after check, redirecting to login");
        router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
        return false;
      }
    })
  );
};

