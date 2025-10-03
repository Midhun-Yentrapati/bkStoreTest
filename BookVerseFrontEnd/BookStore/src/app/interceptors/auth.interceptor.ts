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
    console.log('[AUTH INTERCEPTOR] Token from localStorage:', token ? `${token.substring(0, 20)}...` : 'null');
    if (!token) {
      token = authService.getToken();
      console.log('[AUTH INTERCEPTOR] Token from service:', token ? `${token.substring(0, 20)}...` : 'null');
    }
  } else {
    token = authService.getToken();
  }
  
  // Console logs are now run after the platform check inside the helper function
  // to avoid misleading "localStorage not available" messages during SSR.
  
  // Skip authentication for public endpoints
  if (isPublicEndpoint(req.url)) {
    console.log('[AUTH INTERCEPTOR] Skipping auth for public endpoint:', req.url);
    return next(req);
  }

  // Handle token refresh for expired tokens
  if (isPlatformBrowser(platformId) && !token) {
    const refreshToken = localStorage.getItem('bookverse_refresh_token');
    if (refreshToken) {
      // Attempt token refresh (implement refresh logic here if needed)
      console.log('[AUTH INTERCEPTOR] No access token, but refresh token exists');
    }
  }
  
  // Add token to request if available
  if (token) {
    console.log('[AUTH INTERCEPTOR] Request URL:', req.url);
    console.log('[AUTH INTERCEPTOR] Token exists: true. Adding Bearer token to request');
    
    const authReq = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${token}`)
    });
    
    console.log('[AUTH INTERCEPTOR] Request headers:', authReq.headers.get('Authorization'));
    
    return next(authReq).pipe(
      catchError((error: HttpErrorResponse) => {
        // Only clear tokens for authentication endpoints, not authorization failures
        if (error.status === 401 && req.url.includes('/api/auth/')) {
          console.warn('Authentication failed - clearing tokens');
          if (isPlatformBrowser(platformId)) {
            localStorage.removeItem('bookverse_token');
            localStorage.removeItem('bookverse_refresh_token');
          }
        } else if (error.status === 401) {
          console.warn('Authorization failed for:', req.url, '- keeping tokens');
        }
        return throwError(() => error);
      })
    );
  } else {
    // This runs during SSR or if no token exists
    console.log('[AUTH INTERCEPTOR] Request URL:', req.url);
    console.log(`[AUTH INTERCEPTOR] Platform is browser: ${isPlatformBrowser(platformId)}, Token exists: ${!!token}`);
  }

  // If running on server or no token is found in browser, proceed with original request (unauthenticated)
  return next(req);
};

// FIX: Helper functions now require and use platformId
function getTokenFromStorage(platformId: Object): string | null {
  if (isPlatformBrowser(platformId)) {
    const token = localStorage.getItem('bookverse_token');
    console.log('[AUTH INTERCEPTOR] Token from localStorage:', token ? `${token.substring(0, 20)}...` : 'null');
    return token;
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
  // Allow public book browsing endpoints
  const publicBookPatterns = [
    '/api/books',
    '/api/categories'
  ];
  
  // Check for specific public review endpoints (only specific GET operations)
  if (url.includes('/api/reviews')) {
    // Only allow public GET requests for reading reviews, not user-specific operations
    return url.match(/\/api\/reviews\/book\/\d+$/) !== null || // GET /api/reviews/book/{id}
           url.match(/\/api\/reviews\/book\/\d+\/page/) !== null || // GET /api/reviews/book/{id}/page
           url.match(/\/api\/reviews\/book\/\d+\/stats/) !== null; // GET /api/reviews/book/{id}/stats
  }
  
  return publicBookPatterns.some(pattern => {
    if (pattern === '/api/books') {
      // Allow public book browsing but not admin book operations
      return url.includes('/api/books') && !url.includes('/api/books/admin');
    }
    return url.includes(pattern);
  });
}