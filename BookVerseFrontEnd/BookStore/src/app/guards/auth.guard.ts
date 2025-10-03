import { Injectable, inject } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  private authService = inject(AuthService);
  private router = inject(Router);

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean {
    // Wait for auth service to initialize
    if (!this.authService.isInitialized()) {
      // Redirect to login but don't clear storage
      this.router.navigate(['/login'], { 
        queryParams: { returnUrl: state.url },
        queryParamsHandling: 'merge'
      });
      return false;
    }
    
    const isLoggedIn = this.authService.isLoggedIn();
    
    if (!isLoggedIn) {
      // Store the attempted URL for redirecting after login
      this.router.navigate(['/login'], { 
        queryParams: { returnUrl: state.url },
        queryParamsHandling: 'merge'
      });
      return false;
    }
    
    return true;
  }
} 