import { HttpInterceptorFn } from '@angular/common/http';

/**
 * HTTP Interceptor for session-based authentication
 * Automatically includes credentials (cookies) with all requests
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Clone request and add withCredentials to include session cookies
  // This ensures the session cookie is sent with every request
  const authReq = req.clone({
    withCredentials: true
  });

  return next(authReq);
};

