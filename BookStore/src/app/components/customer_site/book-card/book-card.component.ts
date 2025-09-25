import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { BookModel } from '../../../models/book.model';
import { CartService } from '../../../services/cart.service';
import { WishlistService } from '../../../services/wishlist.service';
import { NotificationService } from '../../../services/notification.service';
import { AuthService } from '../../../services/auth.service';
import { Observable, take } from 'rxjs';

@Component({
  selector: 'app-book-card',
  imports: [CommonModule, RouterLink],
  templateUrl: './book-card.component.html',
  styleUrl: './book-card.component.css'
})
export class BookCardComponent implements OnInit {
  @Input() book!: BookModel;
  @Input() cardSize: 'big' | 'compact' = 'compact';
  @Input() showActions: boolean = true;

  isInWishlist$!: Observable<boolean>;
  addingToCart = false;
  addingToWishlist = false;

  constructor(
    private cartService: CartService,
    private wishlistService: WishlistService,
    private notificationService: NotificationService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    if (this.showActions) {
      this.isInWishlist$ = this.wishlistService.isInWishlist(this.book.id);
    }
  }

  isLoggedIn(): boolean {
    return !!this.authService.getCurrentCustomer();
  }

  addToCart(event: Event): void {
    event.preventDefault();
    event.stopPropagation();

    if (!this.isLoggedIn()) {
      this.notificationService.authError('Please login to add items to cart');
      return;
    }

    if (!this.book || this.addingToCart || this.book.stock_actual <= 0) return;
    
    this.addingToCart = true;
    this.cartService.addToCart(this.book).subscribe({
      next: (cartItem) => {
        console.log(`Added ${this.book.title} to cart`);
        this.notificationService.cartSuccess(this.book.title);
        this.addingToCart = false;
      },
      error: (error) => {
        console.error('Error adding to cart:', error);
        this.notificationService.cartError(this.book.title, error.message);
        this.addingToCart = false;
      }
    });
  }

  toggleWishlist(event: Event): void {
    event.preventDefault();
    event.stopPropagation();

    if (!this.isLoggedIn()) {
      this.notificationService.authError('Please login to manage your wishlist');
      return;
    }

    if (!this.book || this.addingToWishlist) return;

    this.addingToWishlist = true;
    
    // Check current wishlist status and toggle
    this.isInWishlist$.pipe(take(1)).subscribe(isInWishlist => {
      if (isInWishlist) {
        this.removeFromWishlist();
      } else {
        this.addToWishlist();
      }
    });
  }

  private addToWishlist(): void {
    this.wishlistService.addToWishlist(this.book).subscribe({
      next: (wishlistItem) => {
        console.log(`Added ${this.book.title} to wishlist`);
        this.notificationService.wishlistSuccess(this.book.title);
        this.addingToWishlist = false;
        // Update wishlist status
        this.isInWishlist$ = this.wishlistService.isInWishlist(this.book.id);
      },
      error: (error) => {
        console.error('Error adding to wishlist:', error);
        this.notificationService.wishlistError(this.book.title, error.message);
        this.addingToWishlist = false;
      }
    });
  }

  private removeFromWishlist(): void {
    // First get the wishlist item by book ID, then remove it
    this.wishlistService.getWishlistItemByBookId(this.book.id).pipe(take(1)).subscribe({
      next: (wishlistItem) => {
        if (wishlistItem) {
          this.wishlistService.removeFromWishlist(wishlistItem.id).subscribe({
            next: () => {
              console.log(`Removed ${this.book.title} from wishlist`);
              this.notificationService.success('Removed from Wishlist', `"${this.book.title}" has been removed from your wishlist.`);
              this.addingToWishlist = false;
              // Update wishlist status
              this.isInWishlist$ = this.wishlistService.isInWishlist(this.book.id);
            },
            error: (error) => {
              console.error('Error removing from wishlist:', error);
              this.notificationService.error('Wishlist Error', 'Failed to remove from wishlist. Please try again.');
              this.addingToWishlist = false;
            }
          });
        } else {
          console.error('Wishlist item not found');
          this.addingToWishlist = false;
        }
      },
      error: (error) => {
        console.error('Error getting wishlist item:', error);
        this.addingToWishlist = false;
      }
    });
  }

  // Utility methods for template
  getBookIdAsString(): string {
    return typeof this.book.id === 'number' ? this.book.id.toString() : this.book.id;
  }

  getFirstImageUrl(): string {
    return this.book.image_urls?.[0] || this.book.images?.[0]?.imageUrl || 'https://placehold.co/150x200?text=No+Image';
  }

  getCategoryNames(): string[] {
    if (!this.book.categories) return [];
    return this.book.categories.map(cat => 
      typeof cat === 'string' ? cat : cat.name
    );
  }

  getStockStatus(): string {
    if (this.book.stock_actual <= 0) return 'out-of-stock';
    if (this.book.stock_actual <= 5) return 'low-stock';
    return 'in-stock';
  }

  isOutOfStock(): boolean {
    return this.book.stock_actual <= 0;
  }
}
