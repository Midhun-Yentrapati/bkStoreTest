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