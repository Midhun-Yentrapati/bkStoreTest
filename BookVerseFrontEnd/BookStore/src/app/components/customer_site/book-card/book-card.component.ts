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

    if (!this.book || this.addingToCart || this.book.stockActual <= 0) return;
    
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

  getStockStatus(): string {
    if (this.book.stockActual <= 0) return 'out-of-stock';
    if (this.book.stockActual <= 5) return 'low-stock';
    return 'in-stock';
  }

  isOutOfStock(): boolean {
    return this.book.stockActual <= 0;
  }

  getBookImage(): string {
    // Debug: Log book data only once per book
    if (!(this.book as any)._imageLogged) {
      console.log('Book:', this.book.title, 'Images:', this.book.images);
      (this.book as any)._imageLogged = true;
    }
    
    if (this.book.images && this.book.images.length > 0) {
      let imageUrl: string;
      
      if (this.book.images.length > 1) {
        // If multiple images, get the primary image or fallback to first
        const primaryImage = this.book.images.find(img => img.isPrimary) || this.book.images[0];
        imageUrl = primaryImage.imageUrl;
      } else {
        // If only one image, use it regardless of isPrimary status
        imageUrl = this.book.images[0].imageUrl;
      }
      
      // Ensure the image URL is properly formatted
      if (imageUrl && !imageUrl.startsWith('http') && !imageUrl.startsWith('/')) {
        imageUrl = `/${imageUrl}`;
      }
      
      return imageUrl || 'https://placehold.co/200x300?text=No+Image';
    }
    
    return 'https://placehold.co/200x300?text=No+Image';
  }

  onImageError(event: any): void {
    console.log('Image failed to load for book:', this.book.title, 'URL:', event.target.src);
    event.target.src = 'https://placehold.co/200x300?text=No+Image';
  }
}
