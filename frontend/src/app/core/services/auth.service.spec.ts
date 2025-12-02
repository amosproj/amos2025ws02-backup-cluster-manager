import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { ApiService } from './api.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let router: jasmine.SpyObj<Router>;
  let apiService: jasmine.SpyObj<ApiService>;

  beforeEach(() => {
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    const apiServiceSpy = jasmine.createSpyObj('ApiService', ['get', 'post']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        AuthService,
        { provide: Router, useValue: routerSpy },
        { provide: ApiService, useValue: apiServiceSpy }
      ]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    apiService = TestBed.inject(ApiService) as jasmine.SpyObj<ApiService>;
  });

  afterEach(() => {
    httpMock.verify(); // Verify that no unmatched requests remain
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('isAuthenticated', () => {
    it('should return authentication status', () => {
      expect(service.isAuthenticated()).toBe(false); // Initially false until checkAuthStatus is called
    });

    it('should return true after successful checkAuthStatus', (done) => {
      const validateUrl = 'http://localhost:8080/api/v1/cm/auth/validate';

      service.checkAuthStatus().subscribe(() => {
        expect(service.isAuthenticated()).toBe(true);
        done();
      });

      const req = httpMock.expectOne(validateUrl);
      req.flush({}, { status: 200, statusText: 'OK' });
    });
  });

  describe('checkAuthStatus', () => {
    const validateUrl = 'http://localhost:8080/api/v1/cm/auth/validate';

    it('should send validation request to backend', () => {
      service.checkAuthStatus().subscribe();

      const req = httpMock.expectOne(validateUrl);
      expect(req.request.method).toBe('GET');

      req.flush({}, { status: 200, statusText: 'OK' });
    });

    it('should return true and set authenticated status on successful validation (status 200)', (done) => {
      service.checkAuthStatus().subscribe({
        next: (result) => {
          expect(result).toBe(true);
          expect(service.isAuthenticated()).toBe(true);
          done();
        }
      });

      const req = httpMock.expectOne(validateUrl);
      req.flush({}, { status: 200, statusText: 'OK' });
    });

    it('should return false and set authenticated status to false on failed validation', (done) => {
      spyOn(console, 'error');

      service.checkAuthStatus().subscribe({
        next: (result) => {
          expect(result).toBe(false);
          expect(service.isAuthenticated()).toBe(false);
          expect(console.error).toHaveBeenCalledWith('Error checking auth status:', jasmine.any(Object));
          done();
        }
      });

      const req = httpMock.expectOne(validateUrl);
      req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });
    });

    it('should handle network errors gracefully', (done) => {
      spyOn(console, 'error');

      service.checkAuthStatus().subscribe({
        next: (result) => {
          expect(result).toBe(false);
          expect(service.isAuthenticated()).toBe(false);
          done();
        }
      });

      const req = httpMock.expectOne(validateUrl);
      req.error(new ProgressEvent('Network error'));
    });
  });

  describe('login', () => {
    const mockUsername = 'testuser';
    const mockPassword = 'testpass';
    const loginUrl = 'http://localhost:8080/api/v1/cm/auth/login';

    it('should send login request with correct credentials', () => {
      service.login(mockUsername, mockPassword).subscribe();

      const req = httpMock.expectOne(loginUrl);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ username: mockUsername, password: mockPassword });

      req.flush({}, { status: 200, statusText: 'OK' });
    });

    it('should return true and set authenticated status on successful login (status 200)', (done) => {
      spyOn(console, 'log');

      service.login(mockUsername, mockPassword).subscribe({
        next: (result) => {
          expect(result).toBe(true);
          expect(service.isAuthenticated()).toBe(true);
          expect(console.log).toHaveBeenCalledWith('Login successful - Status 200');
          done();
        }
      });

      const req = httpMock.expectOne(loginUrl);
      req.flush({ message: 'Login successful' }, { status: 200, statusText: 'OK' });
    });

    it('should throw error on 401 unauthorized status', (done) => {
      const errorMessage = 'Unauthorized';

      service.login(mockUsername, mockPassword).subscribe({
        next: () => fail('should have failed with 401'),
        error: (error) => {
          expect(error.status).toBe(401);
          done();
        }
      });

      const req = httpMock.expectOne(loginUrl);
      req.flush({ message: errorMessage }, { status: 401, statusText: 'Unauthorized' });
    });

    it('should handle login errors', (done) => {
      const errorMessage = 'Network error';

      service.login(mockUsername, mockPassword).subscribe({
        next: () => fail('should have failed'),
        error: (error) => {
          expect(error.status).toBe(500);
          done();
        }
      });

      const req = httpMock.expectOne(loginUrl);
      req.flush(errorMessage, { status: 500, statusText: 'Server Error' });
    });
  });

  describe('logout', () => {
    const logoutUrl = 'http://localhost:8080/api/v1/cm/auth/logout';

    it('should send logout request', () => {
      service.logout().subscribe();

      const req = httpMock.expectOne(logoutUrl);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({});

      req.flush({}, { status: 200, statusText: 'OK' });
    });

    it('should set authenticated status to false on successful logout', (done) => {
      spyOn(console, 'log');

      service.logout().subscribe({
        next: (result) => {
          expect(result).toBe(true);
          expect(service.isAuthenticated()).toBe(false);
          expect(console.log).toHaveBeenCalledWith('Logging out...');
          done();
        }
      });

      const req = httpMock.expectOne(logoutUrl);
      req.flush({}, { status: 200, statusText: 'OK' });
    });

    it('should navigate to login page after logout', (done) => {
      service.logout().subscribe({
        next: () => {
          expect(router.navigate).toHaveBeenCalledWith(['/login']);
          done();
        }
      });

      const req = httpMock.expectOne(logoutUrl);
      req.flush({}, { status: 200, statusText: 'OK' });
    });

    it('should handle logout errors and set authenticated status to false', (done) => {
      spyOn(console, 'error');

      service.logout().subscribe({
        next: () => fail('should have failed'),
        error: (error) => {
          expect(error.status).toBe(500);
          expect(service.isAuthenticated()).toBe(false);
          expect(console.error).toHaveBeenCalledWith('Logout error:', jasmine.any(Object));
          done();
        }
      });

      const req = httpMock.expectOne(logoutUrl);
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });
});

