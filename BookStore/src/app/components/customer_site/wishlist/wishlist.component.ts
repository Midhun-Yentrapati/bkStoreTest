import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { BookService } from '../../../services/book.service';
import { WishlistService, WishlistItemWithDetails } from '../../../services/wishlist.service';
import { CartService } from '../../../services/cart.service';
import { BookModel } from '../../../models/book.model';

@Component({
  selector: 'app-wishlist',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './wishlist.component.html',
  styleUrls: ['./wishlist.component.css']
})
export class WishlistComponent implements OnInit {
  wishlistItems: WishlistItemWithDetails[] = [];

  constructor(
    private wishlistService: WishlistService,
    private cartService: CartService,
    private router: Router
  ) { }

  ngOnInit(): void {
    // Subscribe to wishlistItemsWithDetails$ to get real-time updates with book details
    this.wishlistService.wishlistItemsWithDetails$.subscribe(items => {
      this.wishlistItems = items;
      console.log('Wishlist items loaded:', this.wishlistItems);
      
      // Debug: Log each item's structure
      items.forEach((item, index) => {
        console.log(`Wishlist item ${index}:`, {
          wishlistItemId: item.id,
          bookId: item.bookId,
          bookObject: item.book,
          bookIdFromBook: item.book?.id
        });
      });
    });
  }

  /**
   * Removes a wishlist item.
   * @param item The WishlistItemWithDetails to remove.
   */
  removeFromWishlist(item: WishlistItemWithDetails): void {
    this.wishlistService.removeFromWishlist(item.id).subscribe({
      next: () => {
        console.log(`Removed ${item.book.title} from wishlist.`);
        // Service automatically reloads items, so local state updates
      },
      error: (error) => {
        console.error('Error removing from wishlist:', error);
        alert('Failed to remove from wishlist.');
      }
    });
  }

  /**
   * Moves a book from the wishlist to the cart, then removes it from wishlist.
   * @param item The wishlist item to move.
   */
  moveToCart(item: WishlistItemWithDetails): void {
    // Use the book from the wishlist item
    const book = item.book;

    this.cartService.addToCart(book).subscribe({
      next: (cartItem) => {
        console.log(`Moved ${item.book.title} to cart.`);
        alert(`${item.book.title} moved to cart!`);
        // If successfully added to cart, remove from wishlist
        this.removeFromWishlist(item);
      },
      error: (error) => {
        console.error('Error moving to cart:', error);
        alert('Failed to move to cart. It might already be in your cart.');
      }
    });
  }

  getTotalValue(): number {
    return this.wishlistItems.reduce((total, item) => total + item.book.price, 0);
  }

  trackByBookId(index: number, item: WishlistItemWithDetails): string {
    return item.id;
  }

  navigateToBook(bookId: string | number): void {
    console.log('Navigating to book with ID:', bookId);
    this.router.navigate(['/book', bookId.toString()]);
  }

  moveAllToCart(): void {
    const availableItems = this.wishlistItems.filter(item => item.book.stock_actual > 0);
    
    if (availableItems.length === 0) {
      alert('No items available to add to cart.');
      return;
    }

    let itemsProcessed = 0;
    let successCount = 0;

    availableItems.forEach(item => {
      // Use the book from the wishlist item
      const book = item.book;

      this.cartService.addToCart(book).subscribe({
        next: () => {
          successCount++;
          this.removeFromWishlist(item);
          itemsProcessed++;
          
          if (itemsProcessed === availableItems.length) {
            alert(`${successCount} items added to cart!`);
          }
        },
        error: (error) => {
          console.error('Error adding item to cart:', error);
          itemsProcessed++;
          
          if (itemsProcessed === availableItems.length) {
            alert(`${successCount} items added to cart!`);
          }
        }
      });
    });
  }

  clearWishlist(): void {
    if (confirm('Are you sure you want to clear your entire wishlist?')) {
      this.wishlistService.clearWishlist().subscribe({
        next: () => {
          console.log('Wishlist cleared successfully');
          alert('Wishlist cleared successfully!');
        },
        error: (error) => {
          console.error('Error clearing wishlist:', error);
          alert('Error clearing wishlist. Please try again.');
        }
      });
    }
  }


} 