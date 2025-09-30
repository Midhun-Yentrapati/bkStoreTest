import { Injectable, effect } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, forkJoin, of, throwError, combineLatest } from 'rxjs';
import { tap, map, catchError, switchMap } from 'rxjs/operators';
import { BookModel } from '../models/book.model';
import { CartItem, CartItemDto, CartItemWithDetails } from '../models/cart.model';
import { BookService } from './book.service';
import { AuthService } from './auth.service';

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
    // Use the correct backend endpoint: /api/cart?userId={userId}
    this.http.get<CartItem[]>(`${this.apiUrl}?userId=${userId}`).subscribe({
      next: items => {
        console.log('Cart items loaded:', items);
        this.cartItemsSubject.next(items || []);
      },
      error: error => {
        console.error('Error loading cart items:', error);
        // Handle different types of errors
        if (error.status === 0) {
          console.error('Network error - cart service may be unavailable');
        } else if (error.status === 403) {
          console.error('Authentication required for cart operations');
        } else if (error.status === 503) {
          console.error('Cart service temporarily unavailable');
        }
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
        map(book => ({ 
          ...item, 
          book,
          subtotal: item.priceWhenAdded * item.quantity
        } as CartItemWithDetails)),
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
    const existingItem = currentItems.find(item => item.bookId === book.id.toString() && item.userId === currentUser.id);

    if (existingItem) {
      // Update quantity of existing item using backend endpoint
      return this.updateCartItemQuantity(existingItem.id, existingItem.quantity + 1);
    } else {
      // Create new cart item using backend DTO structure
      const cartItemDto: CartItemDto = {
        userId: currentUser.id,
        bookId: book.id.toString(),
        quantity: 1,
        priceWhenAdded: book.price
      };

      return this.http.post<CartItem>(this.apiUrl, cartItemDto).pipe(
        tap(() => this.loadUserCartItems(currentUser.id)),
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

    // Use backend endpoint: PUT /api/cart/{cartItemId}?quantity={quantity}
    return this.http.put<CartItem>(`${this.apiUrl}/${cartItemId}?quantity=${newQuantity}`, {}).pipe(
      tap(() => this.loadUserCartItems(currentUser.id)),
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

    // Use backend endpoint: DELETE /api/cart/user/{userId}/clear
    return this.http.delete(`${this.apiUrl}/user/${currentUser.id}/clear`).pipe(
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
      map(items => items.reduce((total, item) => total + item.subtotal, 0))
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

  // Move item from wishlist to cart (backend endpoint)
  moveFromWishlistToCart(bookId: string, quantity: number = 1): Observable<CartItem> {
    const currentUser = this.authService.getCurrentCustomer();
    if (!currentUser) {
      return throwError(() => new Error('User must be logged in'));
    }

    return this.http.post<CartItem>(`${this.apiUrl}/move-from-wishlist`, null, {
      params: {
        userId: currentUser.id,
        bookId: bookId,
        quantity: quantity.toString()
      }
    }).pipe(
      tap(() => this.loadUserCartItems(currentUser.id)),
      catchError(error => {
        console.error('Error moving item from wishlist to cart:', error);
        throw error;
      })
    );
  }
} 