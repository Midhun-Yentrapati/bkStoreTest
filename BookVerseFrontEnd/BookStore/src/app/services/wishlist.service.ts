import { Injectable, effect } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, forkJoin, of, throwError } from 'rxjs';
import { tap, map, catchError, switchMap } from 'rxjs/operators';
import { BookModel } from '../models/book.model';
import { WishlistItem, WishlistItemDto, WishlistItemWithDetails } from '../models/wishlist.model';
import { BookService } from './book.service';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class WishlistService {
  private apiBaseUrl = 'http://localhost:8090/api'; // API Gateway URL
  private apiUrl = `${this.apiBaseUrl}/wishlist`;
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

  private loadUserWishlistItems(userId: string): void {
    // Use the backend endpoint: /api/wishlist/user/{userId}
    this.http.get<WishlistItem[]>(`${this.apiUrl}/user/${userId}`).subscribe({
      next: items => {
        console.log('Wishlist items loaded:', items);
        this.wishlistItemsSubject.next(items || []);
      },
      error: error => {
        console.error('Error loading wishlist items:', error);
        // Handle different types of errors
        if (error.status === 0) {
          console.error('Network error - wishlist service may be unavailable');
        } else if (error.status === 403) {
          console.error('Authentication required for wishlist operations');
        } else if (error.status === 503) {
          console.error('Wishlist service temporarily unavailable');
        }
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

  addToWishlist(book: BookModel): Observable<WishlistItem> {
    const currentUser = this.authService.getCurrentCustomer();
    if (!currentUser) {
      return throwError(() => new Error('User must be logged in to add items to wishlist'));
    }

    // Check if item already exists in wishlist
    const currentItems = this.wishlistItemsSubject.getValue();
    const existingItem = currentItems.find(item => item.bookId === book.id.toString() && item.userId === currentUser.id);

    if (existingItem) {
      return throwError(() => new Error('Book is already in wishlist'));
    }

    // Create new wishlist item using backend DTO structure
    const wishlistItemDto: WishlistItemDto = {
      userId: currentUser.id,
      bookId: book.id.toString(),
      priceWhenAdded: book.price,
      notifyOnSale: false
    };

    return this.http.post<WishlistItem>(this.apiUrl, wishlistItemDto).pipe(
      tap(() => this.loadUserWishlistItems(currentUser.id)),
      catchError(error => {
        console.error('Error adding wishlist item:', error);
        throw error;
      })
    );
  }

  removeFromWishlist(wishlistItemId: string): Observable<void> {
    const currentUser = this.authService.getCurrentCustomer();
    if (!currentUser) {
      return throwError(() => new Error('User must be logged in to remove items from wishlist'));
    }

    return this.http.delete<void>(`${this.apiUrl}/${wishlistItemId}`).pipe(
      tap(() => this.loadUserWishlistItems(currentUser.id)),
      catchError(error => {
        console.error('Error removing wishlist item:', error);
        throw error;
      })
    );
  }

  getWishlistItemCount(): Observable<number> {
    return this.wishlistItems$.pipe(
      map(items => items.length)
    );
  }

  // Alias for template compatibility
  getWishlistCount(): Observable<number> {
    return this.getWishlistItemCount();
  }

  // Helper method to check if a book is in wishlist
  isInWishlist(bookId: string | number): Observable<boolean> {
    const stringId = typeof bookId === 'number' ? bookId.toString() : bookId;
    return this.wishlistItems$.pipe(
      map(items => items.some(item => item.bookId === stringId))
    );
  }

  // Get wishlist item by book ID
  getWishlistItemByBookId(bookId: string | number): Observable<WishlistItem | null> {
    const stringId = typeof bookId === 'number' ? bookId.toString() : bookId;
    return this.wishlistItems$.pipe(
      map(items => items.find(item => item.bookId === stringId) || null)
    );
  }

  // Remove from wishlist by book ID (for template compatibility)
  removeBookFromWishlist(bookId: string | number): Observable<void> {
    const currentUser = this.authService.getCurrentCustomer();
    if (!currentUser) {
      return throwError(() => new Error('User must be logged in to remove items from wishlist'));
    }

    const stringId = typeof bookId === 'number' ? bookId.toString() : bookId;
    const currentItems = this.wishlistItemsSubject.getValue();
    const itemToRemove = currentItems.find(item => item.bookId === stringId && item.userId === currentUser.id);
    
    if (!itemToRemove) {
      return throwError(() => new Error('Item not found in wishlist'));
    }

    return this.removeFromWishlist(itemToRemove.id);
  }
} 