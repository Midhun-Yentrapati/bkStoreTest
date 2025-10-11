import { Injectable, inject } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Observable, of } from 'rxjs';
import { map, delay, take } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  private authService = inject(AuthService);
  private router = inject(Router);

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> | boolean {
    console.log(' AuthGuard: Checking access for route:', state.url);
    
    // If auth service is not initialized, wait for it with timeout
    if (!this.authService.isInitialized()) {
      console.log(' AuthGuard: Auth service not initialized, waiting...');
      
      return new Observable<boolean>(observer => {
        let attempts = 0;
        const maxAttempts = 100; // Maximum 5 seconds (50ms * 100)
        
        const checkInitialization = () => {
          attempts++;
          
          if (this.authService.isInitialized()) {
            console.log(' AuthGuard: Auth service initialized after', attempts, 'attempts');
            const isLoggedIn = this.authService.isLoggedIn();
            
            if (!isLoggedIn) {
              console.log(' AuthGuard: User not logged in after initialization');
              // Only redirect if we're not already on login page
              if (!state.url.includes('/login')) {
                this.router.navigate(['/login'], { 
                  queryParams: { returnUrl: state.url },
                  replaceUrl: true // Use replaceUrl to prevent history issues
                });
              }
              observer.next(false);
            } else {
              console.log(' AuthGuard: User is logged in, allowing access');
              observer.next(true);
            }
            observer.complete();
          } else if (attempts >= maxAttempts) {
            console.error(' AuthGuard: Timeout waiting for auth initialization');
            // Assume not logged in after timeout
            if (!state.url.includes('/login')) {
              this.router.navigate(['/login'], { replaceUrl: true });
            }
            observer.next(false);
            observer.complete();
          } else {
            // Continue checking
            setTimeout(checkInitialization, 50);
          }
        };
        
        checkInitialization();
      });
    }
    
    // Auth service is initialized, check login status immediately
    console.log(' AuthGuard: Auth service already initialized');
    const isLoggedIn = this.authService.isLoggedIn();
    
    if (!isLoggedIn) {
      console.log(' AuthGuard: User not logged in');
      // Only redirect if we're not already on login page
      if (!state.url.includes('/login')) {
        this.router.navigate(['/login'], { 
          queryParams: { returnUrl: state.url },
          replaceUrl: true // Use replaceUrl to prevent history issues
        });
      }
      return false;
    }
    
    console.log(' AuthGuard: User is logged in, allowing access');
    return true;
  }
}