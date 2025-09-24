import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HeaderComponent } from './header.component';

import { AuthService } from '../../../services/auth.service';
import { CartService } from '../../../services/cart.service';
import { WishlistService } from '../../../services/wishlist.service';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { RouterTestingModule } from '@angular/router/testing';

describe('HeaderComponent', () => {
  let component: HeaderComponent;
  let fixture: ComponentFixture<HeaderComponent>;
  
  let authService: jasmine.SpyObj<AuthService>;
  let cartService: jasmine.SpyObj<CartService>;
  let wishlistService: jasmine.SpyObj<WishlistService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(async () => {

    const authServiceSpy = jasmine.createSpyObj('AuthService', ['isCustomerLoggedIn', 'getCurrentCustomer', 'logout']);
    const cartServiceSpy = jasmine.createSpyObj('CartService', ['getCartItemCount']);
    const wishlistServiceSpy = jasmine.createSpyObj('WishlistService', ['getWishlistCount']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [HeaderComponent, RouterTestingModule],
      providers: [
  
        { provide: AuthService, useValue: authServiceSpy },
        { provide: CartService, useValue: cartServiceSpy },
        { provide: WishlistService, useValue: wishlistServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(HeaderComponent);
    component = fixture.componentInstance;

    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    cartService = TestBed.inject(CartService) as jasmine.SpyObj<CartService>;
    wishlistService = TestBed.inject(WishlistService) as jasmine.SpyObj<WishlistService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;

    // Setup default return values
    cartService.getCartItemCount.and.returnValue(of(0));
    wishlistService.getWishlistCount.and.returnValue(of(0));
    authService.isCustomerLoggedIn.and.returnValue(false);
    authService.getCurrentCustomer.and.returnValue(null);
    router.navigate.and.returnValue(Promise.resolve(true));

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with default values', () => {
    expect(component.isMenuOpen).toBe(false);
    expect(component.searchQuery).toBe('');
    expect(component.showSettingsDropdown).toBe(false);
  });

  describe('toggleMenu', () => {
    it('should toggle menu state', () => {
      expect(component.isMenuOpen).toBe(false);
      
      component.toggleMenu();
      expect(component.isMenuOpen).toBe(true);
      
      component.toggleMenu();
      expect(component.isMenuOpen).toBe(false);
    });
  });

  describe('toggleSettingsDropdown', () => {
    it('should toggle settings dropdown state', () => {
      expect(component.showSettingsDropdown).toBe(false);
      
      component.toggleSettingsDropdown();
      expect(component.showSettingsDropdown).toBe(true);
      
      component.toggleSettingsDropdown();
      expect(component.showSettingsDropdown).toBe(false);
    });
  });

  describe('closeDropdown', () => {
    it('should close dropdown when open', () => {
      component.showSettingsDropdown = true;
      component.closeDropdown();
      expect(component.showSettingsDropdown).toBe(false);
    });

    it('should not change state when dropdown is already closed', () => {
      component.showSettingsDropdown = false;
      component.closeDropdown();
      expect(component.showSettingsDropdown).toBe(false);
    });
  });

  describe('handleProfileClick', () => {
    it('should close dropdown and navigate to profile', fakeAsync(() => {
      component.showSettingsDropdown = true;
      component.handleProfileClick();
      tick(100);
      
      expect(component.showSettingsDropdown).toBe(false);
      expect(router.navigate).toHaveBeenCalledWith(['/profile']);
    }));
  });

  describe('handleSettingsClick', () => {
    it('should close dropdown and navigate to settings', fakeAsync(() => {
      component.showSettingsDropdown = true;
      component.handleSettingsClick();
      tick(100);
      
      expect(component.showSettingsDropdown).toBe(false);
      expect(router.navigate).toHaveBeenCalledWith(['/settings']);
    }));
  });

  describe('handleLogoutClick', () => {
    it('should close dropdown and logout user', fakeAsync(() => {
      component.showSettingsDropdown = true;
      component.handleLogoutClick();
      tick(100);
      
      expect(component.showSettingsDropdown).toBe(false);
      expect(authService.logout).toHaveBeenCalled();
    }));
  });



  describe('onSearch', () => {
    it('should navigate to search with query and close menu', () => {
      component.searchQuery = 'test book';
      component.isMenuOpen = true;
      component.showSettingsDropdown = true;
      
      component.onSearch();
      
      expect(router.navigate).toHaveBeenCalledWith(['/search', 'test book']);
      expect(component.searchQuery).toBe('');
      expect(component.isMenuOpen).toBe(false);
      expect(component.showSettingsDropdown).toBe(false);
    });

    it('should not search with empty query', () => {
      component.searchQuery = '   ';
      component.onSearch();
      
      expect(router.navigate).not.toHaveBeenCalled();
    });

    it('should not search with whitespace only', () => {
      component.searchQuery = '   ';
      component.onSearch();
      
      expect(router.navigate).not.toHaveBeenCalled();
    });
  });

  describe('navigateToHome', () => {
    it('should navigate to home and close menu', () => {
      component.isMenuOpen = true;
      component.showSettingsDropdown = true;
      
      component.navigateToHome();
      
      expect(router.navigate).toHaveBeenCalledWith(['/']);
      expect(component.isMenuOpen).toBe(false);
      expect(component.showSettingsDropdown).toBe(false);
    });
  });

  describe('onLogin', () => {
    it('should navigate to login and close menu', () => {
      component.isMenuOpen = true;
      component.showSettingsDropdown = true;
      
      component.onLogin();
      
      expect(router.navigate).toHaveBeenCalledWith(['/login']);
      expect(component.isMenuOpen).toBe(false);
      expect(component.showSettingsDropdown).toBe(false);
    });
  });

  describe('onSignup', () => {
    it('should navigate to register and close menu', () => {
      component.isMenuOpen = true;
      component.showSettingsDropdown = true;
      
      component.onSignup();
      
      expect(router.navigate).toHaveBeenCalledWith(['/register']);
      expect(component.isMenuOpen).toBe(false);
      expect(component.showSettingsDropdown).toBe(false);
    });
  });

  describe('goToWishlist', () => {
    it('should navigate to wishlist and close menu', () => {
      component.isMenuOpen = true;
      component.showSettingsDropdown = true;
      
      component.goToWishlist();
      
      expect(router.navigate).toHaveBeenCalledWith(['/wishlist']);
      expect(component.isMenuOpen).toBe(false);
      expect(component.showSettingsDropdown).toBe(false);
    });
  });

  describe('goToCart', () => {
    it('should navigate to cart and close menu', () => {
      component.isMenuOpen = true;
      component.showSettingsDropdown = true;
      
      component.goToCart();
      
      expect(router.navigate).toHaveBeenCalledWith(['/cart']);
      expect(component.isMenuOpen).toBe(false);
      expect(component.showSettingsDropdown).toBe(false);
    });
  });

  describe('goToOrders', () => {
    it('should navigate to orders and close menu', () => {
      component.isMenuOpen = true;
      component.showSettingsDropdown = true;
      
      component.goToOrders();
      
      expect(router.navigate).toHaveBeenCalledWith(['/orders']);
      expect(component.isMenuOpen).toBe(false);
      expect(component.showSettingsDropdown).toBe(false);
    });
  });

  describe('getUserDisplayName', () => {
    it('should return full name when available', () => {
      authService.getCurrentCustomer.and.returnValue({ 
        id: '1', 
        fullName: 'John Doe', 
        username: 'johndoe', 
        email: 'john@example.com', 
        mobileNumber: '1234567890' 
      });
      expect(component.getUserDisplayName()).toBe('John Doe');
    });

    it('should return username when full name is not available', () => {
      authService.getCurrentCustomer.and.returnValue({ 
        id: '1', 
        fullName: '', 
        username: 'johndoe', 
        email: 'john@example.com', 
        mobileNumber: '1234567890' 
      });
      expect(component.getUserDisplayName()).toBe('johndoe');
    });

    it('should return empty string when no user', () => {
      authService.getCurrentCustomer.and.returnValue(null);
      expect(component.getUserDisplayName()).toBe('');
    });
  });

  describe('getDefaultAvatar', () => {
    it('should return avatar URL with full name when available', () => {
      authService.getCurrentCustomer.and.returnValue({ 
        id: '1', 
        fullName: 'John Doe', 
        username: 'johndoe', 
        email: 'john@example.com', 
        mobileNumber: '1234567890' 
      });
      const avatar = component.getDefaultAvatar();
      expect(avatar).toContain('name=John%20Doe');
    });

    it('should return default avatar when no user', () => {
      authService.getCurrentCustomer.and.returnValue(null);
      const avatar = component.getDefaultAvatar();
      expect(avatar).toContain('name=User');
    });
  });

  describe('testDropdownButton', () => {
    it('should log test message', () => {
      spyOn(console, 'log');
      component.testDropdownButton();
      expect(console.log).toHaveBeenCalledWith('🧪 Test button clicked - Dropdown is working!');
    });
  });

  describe('Authentication state', () => {
    it('should reflect logged in state', () => {
      authService.isCustomerLoggedIn.and.returnValue(true);
      authService.getCurrentCustomer.and.returnValue({ 
        id: '1', 
        fullName: 'Test User', 
        username: 'testuser', 
        email: 'test@example.com', 
        mobileNumber: '1234567890' 
      });
      
      // Trigger change detection
      fixture.detectChanges();
      
      expect(component.isLoggedIn()).toBe(true);
    });

    it('should reflect logged out state', () => {
      authService.isCustomerLoggedIn.and.returnValue(false);
      authService.getCurrentCustomer.and.returnValue(null);
      
      // Trigger change detection
      fixture.detectChanges();
      
      expect(component.isLoggedIn()).toBe(false);
    });
  });

  describe('Cart and wishlist counts', () => {
    it('should initialize cart and wishlist observables', () => {
      expect(cartService.getCartItemCount).toHaveBeenCalled();
      expect(wishlistService.getWishlistCount).toHaveBeenCalled();
    });
  });
});
