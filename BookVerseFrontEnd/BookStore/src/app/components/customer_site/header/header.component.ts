import { Component, computed, effect, inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common'; // Use CommonModule for NgIf
import { FormsModule } from '@angular/forms'; // For [(ngModel)]
import { Router, RouterLink, RouterLinkActive } from '@angular/router'; // For navigation and routerLink directive

import { AuthService } from '../../../services/auth.service'; // Import AuthService
import { CartService } from '../../../services/cart.service'; // Import CartService
import { WishlistService } from '../../../services/wishlist.service'; // Import WishlistService
import { Observable } from 'rxjs';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    CommonModule, // Provides *ngIf
    FormsModule
  ],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent {
  // Property for the mobile menu state
  isMenuOpen = false;

  // Property for the search bar
  searchQuery: string = '';

  // Settings dropdown state
  showSettingsDropdown = false;

  // Platform injection for SSR compatibility
  private platformId = inject(PLATFORM_ID);

  // Authentication state
  isLoggedIn = computed(() => this.authService.isCustomerLoggedIn());
  currentUser = computed(() => this.authService.getCurrentCustomer());

  // Cart and wishlist item counts
  cartItemCount$: Observable<number>;
  wishlistItemCount$: Observable<number>;

  // Inject Router, AuthService, CartService, and WishlistService
  constructor(
    private router: Router,

    private authService: AuthService,
    private cartService: CartService,
    private wishlistService: WishlistService
  ) {
    // Initialize cart and wishlist count observables
    this.cartItemCount$ = this.cartService.getCartItemCount();
    this.wishlistItemCount$ = this.wishlistService.getWishlistCount();
    // Fixed: Add platform check to prevent SSR hydration logging issue
    effect(() => {
      if (isPlatformBrowser(this.platformId)) {
        const user = this.currentUser();
        if (user) {
          console.log('User logged in:', user.fullName);
        } else {
          console.log('User logged out');
        }
      }
    });
  }

  // Toggles the mobile menu for smaller screens
  toggleMenu(): void {
    this.isMenuOpen = !this.isMenuOpen;
  }

  // Toggle settings dropdown
  toggleSettingsDropdown(): void {
    this.showSettingsDropdown = !this.showSettingsDropdown;
    console.log('🔄 Dropdown toggled:', this.showSettingsDropdown);
  }

  // Close settings dropdown
  closeDropdown(): void {
    if (this.showSettingsDropdown) {
      this.showSettingsDropdown = false;
      console.log('❌ Dropdown closed');
    }
  }

  // New simplified event handlers for dropdown buttons
  handleProfileClick(): void {
    console.log('🎯 Profile clicked from dropdown');
    this.closeDropdown();
    
    // Use setTimeout to ensure dropdown closes before navigation
    setTimeout(() => {
      this.router.navigate(['/profile']).then(success => {
        if (success) {
          console.log('✅ Profile navigation successful');
        } else {
          console.error('❌ Profile navigation failed');
        }
      }).catch(error => {
        console.error('❌ Profile navigation error:', error);
      });
    }, 100);
  }

  handleSettingsClick(): void {
    console.log('🎯 Settings clicked from dropdown');
    this.closeDropdown();
    
    // Use setTimeout to ensure dropdown closes before navigation
    setTimeout(() => {
      this.router.navigate(['/settings']).then(success => {
        if (success) {
          console.log('✅ Settings navigation successful');
        } else {
          console.error('❌ Settings navigation failed');
        }
      }).catch(error => {
        console.error('❌ Settings navigation error:', error);
      });
    }, 100);
  }

  handleLogoutClick(): void {
    console.log('🎯 Logout clicked from dropdown');
    this.closeDropdown();
    
    // Use setTimeout to ensure dropdown closes before logout
    setTimeout(() => {
      try {
        this.authService.logout(); // This already navigates to '/' in AuthService
        console.log('✅ Logout successful - user redirected to home');
      } catch (error) {
        console.error('❌ Logout error:', error);
        // Fallback navigation if logout fails
        this.router.navigate(['/']);
      }
    }, 100);
  }

  // Legacy methods for backward compatibility (keep for now)
  closeSettingsDropdown(): void {
    this.closeDropdown();
  }

  // This method now calls the service to handle the theme change globally.


  // --- All your other navigation and action methods ---

  onSearch(): void {
    const query = this.searchQuery.trim();
    if (query) {
      this.router.navigate(['/search', query]);
      this.searchQuery = '';
      this.isMenuOpen = false; // Close menu after search on mobile
      this.closeSettingsDropdown();
    }
  }

  navigateToHome(): void {
    this.router.navigate(['/']);
    this.isMenuOpen = false; // Close menu after navigation on mobile
    this.closeSettingsDropdown();
  }

  onLogin(): void {
    console.log('Login clicked');
    this.router.navigate(['/login']);
    this.isMenuOpen = false;
    this.closeSettingsDropdown();
  }

  onSignup(): void {
    console.log('Signup clicked');
    this.router.navigate(['/register']); // Fixed: navigate to /register instead of /signup
    this.isMenuOpen = false;
    this.closeSettingsDropdown();
  }

  goToWishlist(): void {
    console.log('Wishlist clicked');
    this.router.navigate(['/wishlist']);
    this.isMenuOpen = false;
    this.closeSettingsDropdown();
  }

  goToCart(): void {
    console.log('Cart clicked');
    this.router.navigate(['/cart']);
    this.isMenuOpen = false;
    this.closeSettingsDropdown();
  }

  goToOrders(): void {
    console.log('Orders clicked');
    this.router.navigate(['/orders']);
    this.isMenuOpen = false;
    this.closeSettingsDropdown();
  }

  // Get user display name
  getUserDisplayName(): string {
    const user = this.currentUser();
    return user ? user.fullName || user.username : '';
  }

  // Get default avatar
  getDefaultAvatar(): string {
    const user = this.currentUser();
    if (user?.fullName) {
      return `https://ui-avatars.com/api/?name=${encodeURIComponent(user.fullName)}&background=7F60A1&color=fff&size=200`;
    }
    return 'https://ui-avatars.com/api/?name=User&background=7F60A1&color=fff&size=200';
  }

  // Test dropdown button functionality
  testDropdownButton(): void {
    console.log('🧪 Test button clicked - Dropdown is working!');
    console.log('🔍 Check browser developer tools (F12) for navigation logs when clicking other buttons');
  }
}
