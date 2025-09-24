import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { AuthGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';

describe('AuthGuard', () => {
  let guard: AuthGuard;
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;

  const mockRoute = {} as ActivatedRouteSnapshot;
  const mockState = { url: '/protected-route' } as RouterStateSnapshot;

  beforeEach(() => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['isLoggedIn']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        AuthGuard,
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    });

    guard = TestBed.inject(AuthGuard);
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });

  describe('canActivate', () => {
    it('should return true when user is logged in', () => {
      authService.isLoggedIn.and.returnValue(true);

      const result = guard.canActivate(mockRoute, mockState);

      expect(result).toBe(true);
      expect(authService.isLoggedIn).toHaveBeenCalled();
      expect(router.navigate).not.toHaveBeenCalled();
    });

    it('should return false when user is not logged in', () => {
      authService.isLoggedIn.and.returnValue(false);

      const result = guard.canActivate(mockRoute, mockState);

      expect(result).toBe(false);
      expect(authService.isLoggedIn).toHaveBeenCalled();
    });

    it('should navigate to login page when user is not logged in', () => {
      authService.isLoggedIn.and.returnValue(false);

      guard.canActivate(mockRoute, mockState);

      expect(router.navigate).toHaveBeenCalledWith(
        ['/login'],
        { 
          queryParams: { returnUrl: '/protected-route' },
          queryParamsHandling: 'merge'
        }
      );
    });

    it('should store the attempted URL in query params', () => {
      authService.isLoggedIn.and.returnValue(false);
      const differentState = { url: '/another-route' } as RouterStateSnapshot;

      guard.canActivate(mockRoute, differentState);

      expect(router.navigate).toHaveBeenCalledWith(
        ['/login'],
        { 
          queryParams: { returnUrl: '/another-route' },
          queryParamsHandling: 'merge'
        }
      );
    });

    it('should handle different route URLs', () => {
      authService.isLoggedIn.and.returnValue(false);
      const routes = [
        '/profile',
        '/settings',
        '/orders',
        '/wishlist',
        '/admin/dashboard'
      ];

      routes.forEach(routeUrl => {
        const state = { url: routeUrl } as RouterStateSnapshot;
        guard.canActivate(mockRoute, state);
        
        expect(router.navigate).toHaveBeenCalledWith(
          ['/login'],
          { 
            queryParams: { returnUrl: routeUrl },
            queryParamsHandling: 'merge'
          }
        );
      });
    });

    it('should preserve existing query params when navigating to login', () => {
      authService.isLoggedIn.and.returnValue(false);
      const stateWithQueryParams = { 
        url: '/protected-route?param1=value1&param2=value2' 
      } as RouterStateSnapshot;

      guard.canActivate(mockRoute, stateWithQueryParams);

      expect(router.navigate).toHaveBeenCalledWith(
        ['/login'],
        { 
          queryParams: { returnUrl: '/protected-route?param1=value1&param2=value2' },
          queryParamsHandling: 'merge'
        }
      );
    });
  });

  describe('Guard behavior', () => {
    it('should call isLoggedIn method from AuthService', () => {
      authService.isLoggedIn.and.returnValue(true);

      guard.canActivate(mockRoute, mockState);

      expect(authService.isLoggedIn).toHaveBeenCalledTimes(1);
    });

    it('should not call router.navigate when user is authenticated', () => {
      authService.isLoggedIn.and.returnValue(true);

      guard.canActivate(mockRoute, mockState);

      expect(router.navigate).not.toHaveBeenCalled();
    });

    it('should call router.navigate only when user is not authenticated', () => {
      authService.isLoggedIn.and.returnValue(false);

      guard.canActivate(mockRoute, mockState);

      expect(router.navigate).toHaveBeenCalledTimes(1);
    });
  });
}); 