import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req: HttpRequest<unknown>, next: HttpHandlerFn) => {
  // Inject AuthService using Angular's inject() function
  const authService = inject(AuthService);
  
  // Get the JWT token from storage
  const token = getTokenFromStorage();
  
  console.log('[AUTH INTERCEPTOR] Request URL:', req.url);
  console.log('[AUTH INTERCEPTOR] Token exists:', !!token);
  console.log('[AUTH INTERCEPTOR] Is public endpoint:', isPublicEndpoint(req.url));
  
  // Skip authentication for public endpoints
  if (isPublicEndpoint(req.url)) {
    console.log('[AUTH INTERCEPTOR] Skipping auth for public endpoint');
    return next(req);
  }

  // Add JWT token to request headers if available
  if (token) {
    console.log('[AUTH INTERCEPTOR] Adding Bearer token to request');
    const authReq = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${token}`)
    });
    
    console.log('[AUTH INTERCEPTOR] Request headers:', authReq.headers.get('Authorization'));
    
    return next(authReq).pipe(
      catchError((error: HttpErrorResponse) => {
        // Handle token-related errors
        if (error.status === 401) {
          console.warn('Authentication failed - token may be expired');
          // Clear invalid tokens
          clearTokens();
          // Optionally redirect to login or refresh token
        }
        return throwError(() => error);
      })
    );
  } else {
    console.log('[AUTH INTERCEPTOR] No token found, proceeding without auth');
  }

  return next(req);
};

function getTokenFromStorage(): string | null {
  if (typeof localStorage !== 'undefined') {
    // Use unified token system - single token for all users
    const token = localStorage.getItem('bookverse_token');
    console.log('[AUTH INTERCEPTOR] Token from localStorage:', token ? `${token.substring(0, 20)}...` : 'null');
    return token;
  }
  console.log('[AUTH INTERCEPTOR] localStorage not available');
  return null;
}

function clearTokens(): void {
  if (typeof localStorage !== 'undefined') {
    localStorage.removeItem('bookverse_token');
    localStorage.removeItem('bookverse_refresh_token');
    localStorage.removeItem('bookverse_customer');
    localStorage.removeItem('bookverse_admin');
  }
}

function isPublicEndpoint(url: string): boolean {
  const publicEndpoints = [
    '/api/auth/login',
    '/api/auth/register',
    '/api/auth/check-username',
    '/api/auth/check-email',
    '/api/auth/forgot-password',
    '/api/test/health',
    '/actuator/health'
  ];
  
  // Check for exact matches or specific patterns
  return publicEndpoints.some(endpoint => {
    if (endpoint === '/api/books') {
      // Allow public book browsing but not admin book operations
      return url.includes('/api/books') && !url.includes('/api/books/admin');
    }
    return url.includes(endpoint);
  });
}
