import { Injectable, effect } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of, throwError, forkJoin } from 'rxjs';
import { tap, map, catchError } from 'rxjs/operators';
import { BookModel } from '../models/book.model';
import { BookService } from './book.service';
import { AuthService } from './auth.service';

// Simplified WishlistItem interface - only storing IDs and essential data
export interface WishlistItem {
  id: string; // Wishlist item ID
  bookId: string | number; // Reference to book
  userId: string; // Reference to user
  addedAt: string; // ISO timestamp
}

// Extended interface for UI display (with book details)
export interface WishlistItemWithDetails extends WishlistItem {
  book: BookModel; // Full book details fetched separately
}

@Injectable({
  providedIn: 'root'
})
export class WishlistService {
  private apiUrl = 'http://localhost:3000/wishlist';
  private wishlistItemsSubject = new BehaviorSubject<WishlistItem[]>([]);
  private wishlistItemsWithDetailsSubject = new BehaviorSubject<WishlistItemWithDetails[]>([]);
  
  // Expose both raw wishlist items and items with book details
  wishlistItems$ = this.wishlistItemsSubject.asObservable();
  wishlistItemsWithDetails$ = this.wishlistItemsWithDetailsSubject.asObservable();

  constructor(
    private http: HttpClient,
    private bookService: BookService,
    private authService: AuthService
  ) {
    // Load wishlist items when user authentication state changes
    effect(() => {
      const user = this.authService.getCurrentCustomer();
      if (user) {
        this.loadUserWishlistItems(user.id);
      } else {
        this.wishlistItemsSubject.next([]);
        this.wishlistItemsWithDetailsSubject.next([]);
      }
    });

    // Subscribe to wishlist items changes and fetch book details
    this.wishlistItems$.subscribe(wishlistItems => {
      this.loadWishlistItemsWithDetails(wishlistItems);
    });
  }

  /**
   * Loads wishlist items for a specific user from the JSON Server API and updates the BehaviorSubject.
   */
  private loadUserWishlistItems(userId: string): void {
    this.http.get<WishlistItem[]>(`${this.apiUrl}?userId=${userId}`).subscribe({
      next: items => this.wishlistItemsSubject.next(items),
      error: error => {
        console.error('Error loading wishlist items:', error);
        this.wishlistItemsSubject.next([]);
      }
    });
  }

  private loadWishlistItemsWithDetails(wishlistItems: WishlistItem[]): void {
    if (wishlistItems.length === 0) {
      this.wishlistItemsWithDetailsSubject.next([]);
      return;
    }

    // Fetch book details for all wishlist items
    const bookRequests = wishlistItems.map(item => 
      this.bookService.getBookById(item.bookId).pipe(
        map(book => ({ ...item, book } as WishlistItemWithDetails)),
        catchError(error => {
          console.error(`Error fetching book ${item.bookId}:`, error);
          // Return null for failed requests
          return of(null);
        })
      )
    );

    forkJoin(bookRequests).subscribe({
      next: (itemsWithDetails) => {
        // Filter out null values (failed requests)
        const validItems = itemsWithDetails.filter(item => item !== null) as WishlistItemWithDetails[];
        this.wishlistItemsWithDetailsSubject.next(validItems);
      },
      error: (error) => {
        console.error('Error loading wishlist items with details:', error);
        this.wishlistItemsWithDetailsSubject.next([]);
      }
    });
  }

  /**
   * Adds a book to the wishlist. Prevents adding duplicates.
   */
  addToWishlist(book: BookModel): Observable<WishlistItem> {
    const user = this.authService.getCurrentCustomer();
    if (!user) {
      return throwError(() => new Error('User must be logged in to add items to wishlist'));
    }

    const currentItems = this.wishlistItemsSubject.getValue();
    const existingItem = currentItems.find(item => item.bookId === book.id && item.userId === user.id);

    if (existingItem) {
      return throwError(() => new Error('Book is already in your wishlist'));
    }

    const newItem: WishlistItem = {
      id: crypto.randomUUID(),
      bookId: book.id,
      userId: user.id,
      addedAt: new Date().toISOString()
    };

    return this.http.post<WishlistItem>(this.apiUrl, newItem).pipe(
      tap(() => this.loadUserWishlistItems(user.id)),
      map(() => newItem),
      catchError(error => {
        console.error('Error adding wishlist item:', error);
        throw error;
      })
    );
  }

  /**
   * Removes a book from the wishlist by its wishlist item ID.
   */
  removeFromWishlist(wishlistItemId: string): Observable<void> {
    const currentUser = this.authService.getCurrentCustomer();
    if (!currentUser) {
      return throwError(() => new Error('User must be logged in to remove items from wishlist'));
    }

    return this.http.delete<void>(`${this.apiUrl}/${wishlistItemId}`).pipe(
      // Use tap for side effect (reloading wishlist)
      tap(() => this.loadUserWishlistItems(currentUser.id)),
      catchError(error => {
        console.error('Error removing book from wishlist:', error);
        throw error;
      })
    );
  }

  /**
   * Removes a book from wishlist by book ID (for backward compatibility)
   */
  removeBookFromWishlist(bookId: string | number): Observable<void> {
    const currentUser = this.authService.getCurrentCustomer();
    if (!currentUser) {
      return throwError(() => new Error('User must be logged in to remove items from wishlist'));
    }

    const stringId = typeof bookId === 'number' ? bookId.toString() : bookId;
    const currentItems = this.wishlistItemsSubject.getValue();
    const itemToRemove = currentItems.find(item => item.bookId.toString() === stringId && item.userId === currentUser.id);
    
    if (!itemToRemove) {
      return throwError(() => new Error('Item not found in wishlist'));
    }

    return this.removeFromWishlist(itemToRemove.id);
  }

  /**
   * Checks if a book is in the current user's wishlist
   */
  isInWishlist(bookId: string | number): Observable<boolean> {
    const stringId = typeof bookId === 'number' ? bookId.toString() : bookId;
    return this.wishlistItems$.pipe(
      map(items => items.some(item => item.bookId.toString() === stringId))
    );
  }

  /**
   * Gets the wishlist item count for the current user
   */
  getWishlistCount(): Observable<number> {
    return this.wishlistItems$.pipe(
      map(items => items.length)
    );
  }

  /**
   * Clears all items from the current user's wishlist
   */
  clearWishlist(): Observable<any> {
    const currentUser = this.authService.getCurrentCustomer();
    if (!currentUser) {
      return throwError(() => new Error('User must be logged in to clear wishlist'));
    }

    const currentItems = this.wishlistItemsSubject.getValue();
    const userItems = currentItems.filter(item => item.userId === currentUser.id);

    if (userItems.length === 0) {
      return of(null);
    }

    const deleteRequests = userItems.map(item =>
      this.http.delete(`${this.apiUrl}/${item.id}`).pipe(
        catchError(error => {
          console.error(`Failed to delete wishlist item ${item.id}:`, error);
          return of(null);
        })
      )
    );

    return forkJoin(deleteRequests).pipe(
      tap(() => {
        this.wishlistItemsSubject.next([]);
        this.wishlistItemsWithDetailsSubject.next([]);
      }),
      catchError(error => {
        console.error('Error clearing wishlist:', error);
        throw error;
      })
    );
  }

  /**
   * Get wishlist item by book ID
   */
  getWishlistItemByBookId(bookId: string | number): Observable<WishlistItem | null> {
    const stringId = typeof bookId === 'number' ? bookId.toString() : bookId;
    return this.wishlistItems$.pipe(
      map(items => items.find(item => item.bookId.toString() === stringId) || null)
    );
  }
} 