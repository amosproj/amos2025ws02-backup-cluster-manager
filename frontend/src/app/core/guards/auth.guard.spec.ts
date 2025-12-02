import { TestBed } from '@angular/core/testing';
import { Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { authGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';
import { of, Observable } from 'rxjs';

describe('authGuard', () => {
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;
  let mockRoute: ActivatedRouteSnapshot;
  let mockState: RouterStateSnapshot;

  beforeEach(() => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['isAuthenticated', 'checkAuthStatus']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    });

    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;

    // Create mock route and state objects
    mockRoute = {} as ActivatedRouteSnapshot;
    mockState = { url: '/protected-route' } as RouterStateSnapshot;
  });

  it('should allow access when user is authenticated', (done) => {
    authService.isAuthenticated.and.returnValue(true);
    authService.checkAuthStatus.and.returnValue(of(true));

    const result = TestBed.runInInjectionContext(() =>
      authGuard(mockRoute, mockState)
    );

    // Result is now an Observable
    if (typeof result === 'boolean') {
      fail('Expected Observable but got boolean');
    } else if (result instanceof Observable) {
      result.subscribe((canActivate: any) => {
        expect(canActivate).toBe(true);
        expect(authService.isAuthenticated).toHaveBeenCalled();
        expect(authService.checkAuthStatus).toHaveBeenCalled();
        expect(router.navigate).not.toHaveBeenCalled();
        done();
      });
    } else {
      fail('Expected Observable but got: ' + typeof result);
    }
  });

  it('should deny access and redirect to login when user is not authenticated', () => {
    authService.isAuthenticated.and.returnValue(false);

    const result = TestBed.runInInjectionContext(() =>
      authGuard(mockRoute, mockState)
    );

    // When not authenticated, guard returns false synchronously
    expect(result).toBe(false);
    expect(authService.isAuthenticated).toHaveBeenCalled();
    expect(authService.checkAuthStatus).not.toHaveBeenCalled(); // Should not call async check
    expect(router.navigate).toHaveBeenCalledWith(
      ['/login'],
      { queryParams: { returnUrl: '/protected-route' } }
    );
  });

  it('should include correct returnUrl in query params', () => {
    authService.isAuthenticated.and.returnValue(false);
    const customState = { url: '/custom/protected/path' } as RouterStateSnapshot;

    TestBed.runInInjectionContext(() =>
      authGuard(mockRoute, customState)
    );

    expect(router.navigate).toHaveBeenCalledWith(
      ['/login'],
      { queryParams: { returnUrl: '/custom/protected/path' } }
    );
  });

  it('should handle root route correctly', () => {
    authService.isAuthenticated.and.returnValue(false);
    const rootState = { url: '/' } as RouterStateSnapshot;

    TestBed.runInInjectionContext(() =>
      authGuard(mockRoute, rootState)
    );

    expect(router.navigate).toHaveBeenCalledWith(
      ['/login'],
      { queryParams: { returnUrl: '/' } }
    );
  });

  it('should handle deep nested routes correctly', () => {
    authService.isAuthenticated.and.returnValue(false);
    const nestedState = { url: '/dashboard/users/123/edit' } as RouterStateSnapshot;

    TestBed.runInInjectionContext(() =>
      authGuard(mockRoute, nestedState)
    );

    expect(router.navigate).toHaveBeenCalledWith(
      ['/login'],
      { queryParams: { returnUrl: '/dashboard/users/123/edit' } }
    );
  });

  it('should handle routes with query parameters', () => {
    authService.isAuthenticated.and.returnValue(false);
    const stateWithParams = { url: '/dashboard?tab=settings&view=details' } as RouterStateSnapshot;

    TestBed.runInInjectionContext(() =>
      authGuard(mockRoute, stateWithParams)
    );

    expect(router.navigate).toHaveBeenCalledWith(
      ['/login'],
      { queryParams: { returnUrl: '/dashboard?tab=settings&view=details' } }
    );
  });



  it('should redirect to login when backend check fails despite cached auth status', (done) => {
    authService.isAuthenticated.and.returnValue(true);
    authService.checkAuthStatus.and.returnValue(of(false)); // Backend says not authenticated

    const result = TestBed.runInInjectionContext(() =>
      authGuard(mockRoute, mockState)
    );

    if (typeof result === 'boolean') {
      fail('Expected Observable but got boolean');
    } else if (result instanceof Observable) {
      result.subscribe((canActivate: any) => {
        expect(canActivate).toBe(false);
        expect(router.navigate).toHaveBeenCalledWith(
          ['/login'],
          { queryParams: { returnUrl: '/protected-route' } }
        );
        done();
      });
    } else {
      fail('Expected Observable but got: ' + typeof result);
    }
  });


});

