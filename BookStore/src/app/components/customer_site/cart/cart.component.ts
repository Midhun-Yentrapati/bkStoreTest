import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CartService, CartItemWithDetails } from '../../../services/cart.service';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.css']
})
export class CartComponent implements OnInit {
  cartItems: CartItemWithDetails[] = [];
  totalPrice: number = 0;
  isLoading: boolean = true;
  
  // Promo code properties
  promoCode: string = '';
  promoCodeError: string = '';
  promoCodeValid: boolean = false;
  appliedPromoCode: string = '';
  isApplyingPromo: boolean = false;

  constructor(
    private cartService: CartService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.isLoading = true;
    this.cartService.cartItemsWithDetails$.subscribe(items => {
      // Filter out any invalid items and ensure book property exists
      this.cartItems = items.filter(item => item && item.book);
      console.log('Cart items loaded:', this.cartItems);
      this.calculateTotalPrice();
      this.isLoading = false;
    });
  }

  // Promo code validation methods
  validatePromoCode(): void {
    if (!this.promoCode.trim()) {
      this.promoCodeError = '';
      this.promoCodeValid = false;
      return;
    }

    if (this.promoCode.trim().length < 3) {
      this.promoCodeError = 'Promo code must be at least 3 characters long.';
      this.promoCodeValid = false;
      return;
    }

    if (this.promoCode.trim().length > 20) {
      this.promoCodeError = 'Promo code cannot exceed 20 characters.';
      this.promoCodeValid = false;
      return;
    }

    // Basic format validation (alphanumeric and hyphens only)
    if (!/^[a-zA-Z0-9-]+$/.test(this.promoCode.trim())) {
      this.promoCodeError = 'Promo code can only contain letters, numbers, and hyphens.';
      this.promoCodeValid = false;
      return;
    }

    this.promoCodeError = '';
    this.promoCodeValid = true;
  }

  applyPromoCode(): void {
    if (!this.promoCodeValid || !this.promoCode.trim()) {
      return;
    }

    this.isApplyingPromo = true;
    
    // Simulate API call for promo code validation
    setTimeout(() => {
      // Mock validation - in real app, this would be an API call
      if (this.promoCode.trim().toLowerCase() === 'welcome10' || 
          this.promoCode.trim().toLowerCase() === 'save20' ||
          this.promoCode.trim().toLowerCase() === 'freeship') {
        this.appliedPromoCode = this.promoCode.trim();
        this.promoCode = '';
        this.promoCodeValid = false;
        this.promoCodeError = '';
        console.log('Promo code applied successfully:', this.appliedPromoCode);
      } else {
        this.promoCodeError = 'Invalid promo code. Please try again.';
        this.promoCodeValid = false;
      }
      this.isApplyingPromo = false;
    }, 1000);
  }

  calculateTotalPrice(): void {
    this.totalPrice = this.cartItems.reduce((sum, item) => sum + ((item?.book?.price || 0) * (item?.quantity || 0)), 0);
  }

  /**
   * Updates the quantity of a cart item when the input field changes.
   * @param item The CartItemWithDetails to update.
   * @param event The change event from the input field.
   */
  updateQuantityFromInput(item: CartItemWithDetails, event: Event): void {
    const inputElement = event.target as HTMLInputElement;
    let newQuantity = parseInt(inputElement.value, 10);

    // Validate newQuantity
    if (isNaN(newQuantity) || newQuantity <= 0) {
      newQuantity = 1; // Default to 1 if invalid input
      inputElement.value = newQuantity.toString(); // Update input field if invalid
    }
    if (item?.book?.stock_display && newQuantity > item.book.stock_display) {
        alert(`Cannot add more than available stock: ${item.book.stock_display}`);
        newQuantity = item.book.stock_display; // Cap at available stock
        inputElement.value = newQuantity.toString(); // Update input field if capped
    }

    // Only update if quantity actually changed to avoid unnecessary API calls
    if (newQuantity !== item.quantity) {
        this.cartService.updateCartItemQuantity(item.id, newQuantity).subscribe({
            next: () => {
                console.log(`Updated ${item.book.title} quantity to ${newQuantity}`);
                // Service automatically reloads, so local state updates
            },
            error: (error) => {
                console.error('Error updating cart item quantity:', error);
                alert('Failed to update quantity. Please try again.');
                // Revert the input value if API call fails
                inputElement.value = item.quantity.toString();
            }
        });
    }
  }

  // Removed duplicate - using existing method below

  /**
   * Changes the quantity of a cart item by a specific amount (+1 or -1).
   * Used by the increment/decrement buttons.
   * @param item The CartItemWithDetails to update.
   * @param change The amount to change the quantity by (e.g., 1 for increment, -1 for decrement).
   */
  changeQuantityBy(item: CartItemWithDetails, change: number): void {
    const newQuantity = item.quantity + change;

    // Apply the same validation logic as updateQuantityFromInput
    if (newQuantity < 1) {
      return; // Prevent quantity from going below 1
    }
    if (item?.book?.stock_display && newQuantity > item.book.stock_display) {
      alert(`Cannot add more than available stock: ${item.book.stock_display}`);
      return; // Prevent quantity from exceeding stock
    }

    this.cartService.updateCartItemQuantity(item.id, newQuantity).subscribe({
      next: () => {
        console.log(`Updated ${item.book.title} quantity to ${newQuantity}`);
      },
      error: (error) => {
        console.error('Error updating cart item quantity:', error);
        alert('Failed to update quantity.');
      }
    });
  }

  removeFromCart(item: CartItemWithDetails): void {
    this.cartService.removeFromCart(item.id).subscribe({
      next: () => {
        console.log(`Removed item ${item.book.title} from cart.`);
      },
      error: (error) => {
        console.error('Error removing item from cart:', error);
        alert('Failed to remove item.');
      }
    });
  }

  checkout(): void {
    if (!this.cartItems || this.cartItems.length === 0) {
      alert('Your cart is empty. Add some books first!');
      return;
    }

    // Basic stock check before checkout
    const outOfStockItems = this.cartItems.filter(item => item?.book?.stock_display && item.quantity > item.book.stock_display);
    if (outOfStockItems.length > 0) {
        alert('Some items in your cart exceed available stock. Please adjust quantities.');
        return;
    }

    // Navigate to checkout page
    this.router.navigate(['/checkout']);
  }

  getTotalItems(): number {
    return this.cartItems.reduce((total, item) => total + (item?.quantity || 0), 0);
  }

  getFinalTotal(): number {
    const totalPrice = this.totalPrice || 0;
    const shipping = totalPrice >= 500 ? 0 : 50;
    return totalPrice + shipping;
  }

  hasOutOfStockItems(): boolean {
    // Add safety check and logging
    if (!this.cartItems || this.cartItems.length === 0) {
      return false;
    }
    
    // Log any problematic items for debugging
    const problematicItems = this.cartItems.filter(item => !item || !item.book);
    if (problematicItems.length > 0) {
      console.warn('Found problematic cart items:', problematicItems);
    }
    
    return this.cartItems.some(item => item?.book?.stock_display <= 0);
  }

  trackByItemId(index: number, item: CartItemWithDetails): string {
    return item?.id || `item-${index}`;
  }

  navigateToBook(bookId: string | number): void {
    if (bookId) {
      this.router.navigate(['/book', bookId.toString()]);
    }
  }

  clearCart(): void {
    if (!this.cartItems || this.cartItems.length === 0) {
      alert('Your cart is already empty.');
      return;
    }
    
    if (confirm('Are you sure you want to clear your entire cart?')) {
      this.cartService.clearCart().subscribe({
        next: () => {
          console.log('Cart cleared successfully');
          // The observable will automatically update the UI
        },
        error: (error) => {
          console.error('Error clearing cart:', error);
          alert('Error clearing cart. Please try again.');
        }
      });
    }
  }
} 