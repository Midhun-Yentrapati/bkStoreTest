import { Injectable, effect } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, forkJoin, of, throwError, combineLatest } from 'rxjs';
import { tap, map, catchError, switchMap } from 'rxjs/operators';
import { BookModel } from '../models/book.model';
import { BookService } from './book.service';
import { AuthService } from './auth.service';

// Simplified CartItem interface - only storing IDs and essential data
export interface CartItem {
  id: string; // Cart item ID
  bookId: string | number; // Reference to book
  userId: string; // Reference to user
  quantity: number;
  addedAt: string; // ISO timestamp
}

// Extended interface for UI display (with book details)
export interface CartItemWithDetails extends CartItem {
  book: BookModel; // Full book details fetched separately
}

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private apiBaseUrl = 'http://localhost:8090/api'; // API Gateway URL
  private apiUrl = `${this.apiBaseUrl}/cart`;
  private cartItemsSubject = new BehaviorSubject<CartItem[]>([]);
  private cartItemsWithDetailsSubject = new BehaviorSubject<CartItemWithDetails[]>([]);
  
  // Expose both raw cart items and items with book details
  cartItems$ = this.cartItemsSubject.asObservable();
  cartItemsWithDetails$ = this.cartItemsWithDetailsSubject.asObservable();

  constructor(
    private http: HttpClient,
    private bookService: BookService,
    private authService: AuthService
  ) {
    // Load cart items when user authentication state changes
    effect(() => {
      const user = this.authService.getCurrentCustomer();
      if (user) {
        this.loadUserCartItems(user.id);
      } else {
        this.cartItemsSubject.next([]);
        this.cartItemsWithDetailsSubject.next([]);
      }
    });

    // Subscribe to cart items changes and fetch book details
    this.cartItems$.subscribe(cartItems => {
      this.loadCartItemsWithDetails(cartItems);
    });
  }

  private loadUserCartItems(userId: string): void {
    this.http.get<CartItem[]>(`${this.apiUrl}?userId=${userId}`).subscribe({
      next: items => this.cartItemsSubject.next(items),
      error: error => {
        console.error('Error loading cart items:', error);
        this.cartItemsSubject.next([]);
      }
    });
  }

  private loadCartItemsWithDetails(cartItems: CartItem[]): void {
    if (cartItems.length === 0) {
      this.cartItemsWithDetailsSubject.next([]);
      return;
    }

    // Fetch book details for all cart items
    const bookRequests = cartItems.map(item => 
      this.bookService.getBookById(item.bookId).pipe(
        map(book => ({ ...item, book } as CartItemWithDetails)),
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
        const validItems = itemsWithDetails.filter(item => item !== null) as CartItemWithDetails[];
        this.cartItemsWithDetailsSubject.next(validItems);
      },
      error: (error) => {
        console.error('Error loading cart items with details:', error);
        this.cartItemsWithDetailsSubject.next([]);
      }
    });
  }

  addToCart(book: BookModel): Observable<CartItem> {
    const currentUser = this.authService.getCurrentCustomer();
    if (!currentUser) {
      return throwError(() => new Error('User must be logged in to add items to cart'));
    }

    const currentItems = this.cartItemsSubject.getValue();
    const existingItem = currentItems.find(item => item.bookId === book.id && item.userId === currentUser.id);

    if (existingItem) {
      // Update quantity of existing item
      const updatedItem = { ...existingItem, quantity: existingItem.quantity + 1 };
      return this.http.put<CartItem>(`${this.apiUrl}/${existingItem.id}`, updatedItem).pipe(
        tap(() => this.loadUserCartItems(currentUser.id)),
        map(() => updatedItem),
        catchError(error => {
          console.error('Error updating cart item:', error);
          throw error;
        })
      );
    } else {
      // Create new cart item
      const newItem: CartItem = {
        id: crypto.randomUUID(),
        bookId: book.id,
        userId: currentUser.id,
        quantity: 1,
        addedAt: new Date().toISOString()
      };

      return this.http.post<CartItem>(this.apiUrl, newItem).pipe(
        tap(() => this.loadUserCartItems(currentUser.id)),
        map(() => newItem),
        catchError(error => {
          console.error('Error adding cart item:', error);
          throw error;
        })
      );
    }
  }

  updateCartItemQuantity(cartItemId: string, newQuantity: number): Observable<CartItem> {
    const currentUser = this.authService.getCurrentCustomer();
    if (!currentUser) {
      return throwError(() => new Error('User must be logged in to update cart'));
    }

    const currentItems = this.cartItemsSubject.getValue();
    const item = currentItems.find(item => item.id === cartItemId && item.userId === currentUser.id);
    
    if (!item) {
      return throwError(() => new Error('Cart item not found'));
    }

    const updatedItem = { ...item, quantity: newQuantity };

    return this.http.put<CartItem>(`${this.apiUrl}/${cartItemId}`, updatedItem).pipe(
      tap(() => this.loadUserCartItems(currentUser.id)),
      map(() => updatedItem),
      catchError(error => {
        console.error('Error updating cart item quantity:', error);
        throw error;
      })
    );
  }

  removeFromCart(cartItemId: string): Observable<void> {
    const currentUser = this.authService.getCurrentCustomer();
    if (!currentUser) {
      return throwError(() => new Error('User must be logged in to remove items from cart'));
    }

    return this.http.delete<void>(`${this.apiUrl}/${cartItemId}`).pipe(
      tap(() => this.loadUserCartItems(currentUser.id)),
      catchError(error => {
        console.error('Error removing cart item:', error);
        throw error;
      })
    );
  }

  clearCart(): Observable<any> {
    const currentUser = this.authService.getCurrentCustomer();
    if (!currentUser) {
      return throwError(() => new Error('User must be logged in to clear cart'));
    }

    const currentItems = this.cartItemsSubject.getValue();
    const userItems = currentItems.filter(item => item.userId === currentUser.id);

    if (userItems.length === 0) {
      return of(null);
    }

    const deleteRequests = userItems.map(item =>
      this.http.delete(`${this.apiUrl}/${item.id}`).pipe(
        catchError(error => {
          console.error(`Failed to delete cart item ${item.id}:`, error);
          return of(null);
        })
      )
    );

    return forkJoin(deleteRequests).pipe(
      tap(() => {
        this.cartItemsSubject.next([]);
        this.cartItemsWithDetailsSubject.next([]);
      }),
      catchError(error => {
        console.error('Error clearing cart:', error);
        throw error;
      })
    );
  }

  getCartItemCount(): Observable<number> {
    return this.cartItems$.pipe(
      map(items => items.reduce((count, item) => count + item.quantity, 0))
    );
  }

  getCartTotal(): Observable<number> {
    return this.cartItemsWithDetails$.pipe(
      map(items => items.reduce((total, item) => total + (item.book.price * item.quantity), 0))
    );
  }

  // Helper method to check if a book is in cart
  isInCart(bookId: string): Observable<boolean> {
    return this.cartItems$.pipe(
      map(items => items.some(item => item.bookId === bookId))
    );
  }

  // Get cart item by book ID
  getCartItemByBookId(bookId: string): Observable<CartItem | null> {
    return this.cartItems$.pipe(
      map(items => items.find(item => item.bookId === bookId) || null)
    );
  }
} 