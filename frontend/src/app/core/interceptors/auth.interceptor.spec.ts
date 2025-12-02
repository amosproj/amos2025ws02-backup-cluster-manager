import { TestBed } from '@angular/core/testing';
import { HttpRequest, HttpHandlerFn, HttpEvent } from '@angular/common/http';
import { authInterceptor } from './auth.interceptor';
import { Observable, of } from 'rxjs';

describe('authInterceptor', () => {
  let mockRequest: HttpRequest<any>;
  let mockNext: jasmine.Spy<HttpHandlerFn>;

  beforeEach(() => {
    // Create a mock HTTP request
    mockRequest = new HttpRequest('GET', '/api/test');

    // Create a mock next handler that returns an observable
    mockNext = jasmine.createSpy('next').and.returnValue(
      of({} as HttpEvent<any>)
    );
  });

  it('should add withCredentials to the request', () => {
    TestBed.runInInjectionContext(() => {
      authInterceptor(mockRequest, mockNext);

      expect(mockNext).toHaveBeenCalled();
      const modifiedRequest = mockNext.calls.mostRecent().args[0];
      expect(modifiedRequest.withCredentials).toBe(true);
    });
  });

  it('should clone the original request', () => {
    TestBed.runInInjectionContext(() => {
      authInterceptor(mockRequest, mockNext);

      const modifiedRequest = mockNext.calls.mostRecent().args[0];
      expect(modifiedRequest).not.toBe(mockRequest);
      expect(modifiedRequest.url).toBe(mockRequest.url);
      expect(modifiedRequest.method).toBe(mockRequest.method);
    });
  });

  it('should preserve request URL', () => {
    const testUrl = 'http://localhost:8080/api/v1/auth/login';
    mockRequest = new HttpRequest('POST', testUrl, {});

    TestBed.runInInjectionContext(() => {
      authInterceptor(mockRequest, mockNext);

      const modifiedRequest = mockNext.calls.mostRecent().args[0];
      expect(modifiedRequest.url).toBe(testUrl);
    });
  });

  it('should preserve request method', () => {
    mockRequest = new HttpRequest('POST', '/api/test', {});

    TestBed.runInInjectionContext(() => {
      authInterceptor(mockRequest, mockNext);

      const modifiedRequest = mockNext.calls.mostRecent().args[0];
      expect(modifiedRequest.method).toBe('POST');
    });
  });

  it('should preserve request headers', () => {
    mockRequest = new HttpRequest('GET', '/api/test', {
      headers: mockRequest.headers.set('Content-Type', 'application/json')
    });

    TestBed.runInInjectionContext(() => {
      authInterceptor(mockRequest, mockNext);

      const modifiedRequest = mockNext.calls.mostRecent().args[0];
      expect(modifiedRequest.headers.get('Content-Type')).toBe('application/json');
    });
  });

  it('should preserve request body', () => {
    const testBody = { username: 'test', password: 'test123' };
    mockRequest = new HttpRequest('POST', '/api/test', testBody);

    TestBed.runInInjectionContext(() => {
      authInterceptor(mockRequest, mockNext);

      const modifiedRequest = mockNext.calls.mostRecent().args[0];
      expect(modifiedRequest.body).toEqual(testBody);
    });
  });

  it('should work with GET requests', () => {
    mockRequest = new HttpRequest('GET', '/api/users');

    TestBed.runInInjectionContext(() => {
      authInterceptor(mockRequest, mockNext);

      const modifiedRequest = mockNext.calls.mostRecent().args[0];
      expect(modifiedRequest.method).toBe('GET');
      expect(modifiedRequest.withCredentials).toBe(true);
    });
  });

  it('should work with POST requests', () => {
    mockRequest = new HttpRequest('POST', '/api/users', { name: 'John' });

    TestBed.runInInjectionContext(() => {
      authInterceptor(mockRequest, mockNext);

      const modifiedRequest = mockNext.calls.mostRecent().args[0];
      expect(modifiedRequest.method).toBe('POST');
      expect(modifiedRequest.withCredentials).toBe(true);
    });
  });

  it('should work with PUT requests', () => {
    mockRequest = new HttpRequest('PUT', '/api/users/1', { name: 'Jane' });

    TestBed.runInInjectionContext(() => {
      authInterceptor(mockRequest, mockNext);

      const modifiedRequest = mockNext.calls.mostRecent().args[0];
      expect(modifiedRequest.method).toBe('PUT');
      expect(modifiedRequest.withCredentials).toBe(true);
    });
  });

  it('should work with DELETE requests', () => {
    mockRequest = new HttpRequest('DELETE', '/api/users/1');

    TestBed.runInInjectionContext(() => {
      authInterceptor(mockRequest, mockNext);

      const modifiedRequest = mockNext.calls.mostRecent().args[0];
      expect(modifiedRequest.method).toBe('DELETE');
      expect(modifiedRequest.withCredentials).toBe(true);
    });
  });

  it('should work with PATCH requests', () => {
    mockRequest = new HttpRequest('PATCH', '/api/users/1', { status: 'active' });

    TestBed.runInInjectionContext(() => {
      authInterceptor(mockRequest, mockNext);

      const modifiedRequest = mockNext.calls.mostRecent().args[0];
      expect(modifiedRequest.method).toBe('PATCH');
      expect(modifiedRequest.withCredentials).toBe(true);
    });
  });

  it('should return the observable from next handler', (done) => {
    TestBed.runInInjectionContext(() => {
      const result = authInterceptor(mockRequest, mockNext);

      expect(result).toBeInstanceOf(Observable);
      result.subscribe(() => {
        expect(mockNext).toHaveBeenCalled();
        done();
      });
    });
  });

  it('should preserve request params', () => {
    mockRequest = new HttpRequest('GET', '/api/test', {
      params: mockRequest.params.set('page', '1').set('size', '10')
    });

    TestBed.runInInjectionContext(() => {
      authInterceptor(mockRequest, mockNext);

      const modifiedRequest = mockNext.calls.mostRecent().args[0];
      expect(modifiedRequest.params.get('page')).toBe('1');
      expect(modifiedRequest.params.get('size')).toBe('10');
    });
  });

  it('should handle requests with multiple headers', () => {
    mockRequest = new HttpRequest('POST', '/api/test', {}, {
      headers: mockRequest.headers
        .set('Content-Type', 'application/json')
        .set('Accept', 'application/json')
        .set('X-Custom-Header', 'custom-value')
    });

    TestBed.runInInjectionContext(() => {
      authInterceptor(mockRequest, mockNext);

      const modifiedRequest = mockNext.calls.mostRecent().args[0];
      expect(modifiedRequest.headers.get('Content-Type')).toBe('application/json');
      expect(modifiedRequest.headers.get('Accept')).toBe('application/json');
      expect(modifiedRequest.headers.get('X-Custom-Header')).toBe('custom-value');
      expect(modifiedRequest.withCredentials).toBe(true);
    });
  });

  it('should call next handler exactly once', () => {
    TestBed.runInInjectionContext(() => {
      authInterceptor(mockRequest, mockNext);

      expect(mockNext).toHaveBeenCalledTimes(1);
    });
  });

  it('should work with requests to different domains', () => {
    const externalRequest = new HttpRequest('GET', 'https://api.external.com/data');

    TestBed.runInInjectionContext(() => {
      authInterceptor(externalRequest, mockNext);

      const modifiedRequest = mockNext.calls.mostRecent().args[0];
      expect(modifiedRequest.url).toBe('https://api.external.com/data');
      expect(modifiedRequest.withCredentials).toBe(true);
    });
  });
});

