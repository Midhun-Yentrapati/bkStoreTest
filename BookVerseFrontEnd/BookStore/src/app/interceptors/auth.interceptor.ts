import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpErrorResponse } from '@angular/common/http';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common'; // Import isPlatformBrowser
import { AuthService } from '../services/auth.service';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req: HttpRequest<unknown>, next: HttpHandlerFn) => {
  // Inject necessary services and platform ID
  const authService = inject(AuthService);
  const platformId = inject(PLATFORM_ID);
  
  // Get the JWT token - prioritize direct localStorage access for reliability
  let token: string | null = null;
  if (isPlatformBrowser(platformId)) {
    token = localStorage.getItem('bookverse_token');
    if (!token) {
      token = authService.getToken();
    }
  } else {
    token = authService.getToken();
  }
  
  // Console logs are now run after the platform check inside the helper function
  // to avoid misleading "localStorage not available" messages during SSR.
  
  // Skip authentication for public endpoints
  if (isPublicEndpoint(req.url)) {
    return next(req);
  }

  // Handle token refresh for expired tokens
  if (isPlatformBrowser(platformId) && !token) {
    const refreshToken = localStorage.getItem('bookverse_refresh_token');
    if (refreshToken) {
      // Attempt token refresh (implement refresh logic here if needed)
    }
  }
  
  // Add token to request if available
  if (token) {
    const authReq = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${token}`)
    });
    
    return next(authReq).pipe(
      catchError((error: HttpErrorResponse) => {
        // Only clear tokens for authentication endpoints, not authorization failures
        if (error.status === 401 && req.url.includes('/api/auth/')) {
          if (isPlatformBrowser(platformId)) {
            localStorage.removeItem('bookverse_token');
            localStorage.removeItem('bookverse_refresh_token');
          }
        }
        return throwError(() => error);
      })
    );
  }

  // If running on server or no token is found in browser, proceed with original request (unauthenticated)
  return next(req);
};

// FIX: Helper functions now require and use platformId
function getTokenFromStorage(platformId: Object): string | null {
  if (isPlatformBrowser(platformId)) {
    return localStorage.getItem('bookverse_token');
  }
  return null;
}

function clearTokens(platformId: Object): void {
  if (isPlatformBrowser(platformId)) {
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
    return url.includes(endpoint);
  }) || isPublicBookEndpoint(url);
}

function isPublicBookEndpoint(url: string): boolean {
  // Only allow GET requests to books and categories as public
  // POST, PUT, DELETE require authentication
  if (url.includes('/api/books') && !url.includes('/api/books/admin')) {
    // Only GET requests are public for books
    return false; // Let all book operations require auth
  }
  
  if (url.includes('/api/categories')) {
    // Only GET requests are public for categories
    return false; // Let all category operations require auth
  }
  
  // Allow public review endpoints
  if (url.includes('/api/reviews/book/') && !url.includes('/user/')) {
    return true;
  }
  
  if (url.includes('/api/reviews/search')) {
    return true;
  }
  
  return false;
}