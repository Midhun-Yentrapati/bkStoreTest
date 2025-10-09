import { Component, computed, effect, inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';

import { AuthService } from '../../../services/auth.service';
import { CartService } from '../../../services/cart.service';
import { WishlistService } from '../../../services/wishlist.service';

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

  // Cart and wishlist item counts as signals
  cartItemCount = computed(() => 0);
  wishlistItemCount = computed(() => 0);

  // Inject Router, AuthService, CartService, and WishlistService
  constructor(
    private router: Router,
    private authService: AuthService,
    private cartService: CartService,
    private wishlistService: WishlistService
  ) {
    // Initialize signals after services are injected
    this.cartItemCount = toSignal(this.cartService.getCartItemCount(), { initialValue: 0 });
    this.wishlistItemCount = toSignal(this.wishlistService.getWishlistCount(), { initialValue: 0 });
    effect(() => {
      if (isPlatformBrowser(this.platformId)) {
        const user = this.currentUser();
        // User state changed
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
  }

  // Close settings dropdown
  closeDropdown(): void {
    if (this.showSettingsDropdown) {
      this.showSettingsDropdown = false;
    }
  }

  // New simplified event handlers for dropdown buttons
  handleProfileClick(): void {
    this.closeDropdown();
    this.router.navigate(['/profile']);
  }

  handleSettingsClick(): void {
    this.closeDropdown();
    this.router.navigate(['/settings']);
  }

  handleLogoutClick(): void {
    this.closeDropdown();
    try {
      this.authService.logout();
    } catch (error) {
      this.router.navigate(['/']);
    }
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
    this.router.navigate(['/login']);
    this.isMenuOpen = false;
    this.closeSettingsDropdown();
  }

  onSignup(): void {
    this.router.navigate(['/register']); // Fixed: navigate to /register instead of /signup
    this.isMenuOpen = false;
    this.closeSettingsDropdown();
  }

  goToWishlist(): void {
    this.router.navigate(['/wishlist']);
    this.isMenuOpen = false;
    this.closeSettingsDropdown();
  }

  goToCart(): void {
    this.router.navigate(['/cart']);
    this.isMenuOpen = false;
    this.closeSettingsDropdown();
  }

  goToOrders(): void {
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
    // Test button functionality
  }
}
