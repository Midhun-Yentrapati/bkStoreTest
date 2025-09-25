import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CartService } from './cart.service';
import { BookService } from './book.service';
import { AuthService } from './auth.service';
import { BookModel } from '../models/book.model';
import { CartItem, CartItemWithDetails } from './cart.service';
import { of, throwError } from 'rxjs';

describe('CartService', () => {
  let service: CartService;
  let httpMock: HttpTestingController;
  let bookService: jasmine.SpyObj<BookService>;
  let authService: jasmine.SpyObj<AuthService>;

  const mockBook: BookModel = {
    id: '1',
    title: 'Test Book',
    author: 'Test Author',
    description: 'Test Description',
    categories: ['Fiction'],
    price: 29.99,
    stock_display: 10,
    stock_actual: 10,
    image_urls: ['test-image.jpg']
  };

  const mockCartItem: CartItem = {
    id: 'cart1',
    bookId: '1',
    userId: 'user1',
    quantity: 2,
    addedAt: '2024-01-01T00:00:00Z'
  };

  const mockCartItemWithDetails: CartItemWithDetails = {
    ...mockCartItem,
    book: mockBook
  };

  const mockUser = {
    id: 'user1',
    fullName: 'Test User',
    username: 'testuser',
    email: 'test@example.com',
    mobileNumber: '1234567890'
  };

  beforeEach(() => {
    const bookServiceSpy = jasmine.createSpyObj('BookService', ['getBookById']);
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['getCurrentCustomer']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        CartService,
        { provide: BookService, useValue: bookServiceSpy },
        { provide: AuthService, useValue: authServiceSpy }
      ]
    });

    service = TestBed.inject(CartService);
    httpMock = TestBed.inject(HttpTestingController);
    bookService = TestBed.inject(BookService) as jasmine.SpyObj<BookService>;
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;

    // Setup default return values
    authService.getCurrentCustomer.and.returnValue(mockUser);
    bookService.getBookById.and.returnValue(of(mockBook));
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('addToCart', () => {
    it('should add new book to cart', () => {
      service.addToCart(mockBook).subscribe(item => {
        expect(item).toEqual(mockCartItem);
      });

      const req = httpMock.expectOne('http://localhost:3000/cart');
      expect(req.request.method).toBe('POST');
      req.flush(mockCartItem);
    });

    it('should update existing cart item quantity', () => {
      // Mock existing item in cart
      const existingItem = { ...mockCartItem, quantity: 1 };
      spyOn(service['cartItemsSubject'], 'getValue').and.returnValue([existingItem]);

      service.addToCart(mockBook).subscribe(item => {
        expect(item.quantity).toBe(2);
      });

      const req = httpMock.expectOne(`http://localhost:3000/cart/${existingItem.id}`);
      expect(req.request.method).toBe('PUT');
      req.flush({ ...existingItem, quantity: 2 });
    });

    it('should throw error when user is not logged in', () => {
      authService.getCurrentCustomer.and.returnValue(null);

      service.addToCart(mockBook).subscribe({
        error: (error) => {
          expect(error.message).toBe('User must be logged in to add items to cart');
        }
      });
    });
  });

  describe('updateCartItemQuantity', () => {
    it('should update cart item quantity', () => {
      spyOn(service['cartItemsSubject'], 'getValue').and.returnValue([mockCartItem]);

      service.updateCartItemQuantity('cart1', 3).subscribe(item => {
        expect(item.quantity).toBe(3);
      });

      const req = httpMock.expectOne('http://localhost:3000/cart/cart1');
      expect(req.request.method).toBe('PUT');
      req.flush({ ...mockCartItem, quantity: 3 });
    });

    it('should throw error when cart item not found', () => {
      spyOn(service['cartItemsSubject'], 'getValue').and.returnValue([]);

      service.updateCartItemQuantity('cart999', 3).subscribe({
        error: (error) => {
          expect(error.message).toBe('Cart item not found');
        }
      });
    });

    it('should throw error when user is not logged in', () => {
      authService.getCurrentCustomer.and.returnValue(null);

      service.updateCartItemQuantity('cart1', 3).subscribe({
        error: (error) => {
          expect(error.message).toBe('User must be logged in to update cart');
        }
      });
    });
  });

  describe('removeFromCart', () => {
    it('should remove item from cart', () => {
      service.removeFromCart('cart1').subscribe(() => {
        expect().nothing();
      });

      const req = httpMock.expectOne('http://localhost:3000/cart/cart1');
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });

    it('should throw error when user is not logged in', () => {
      authService.getCurrentCustomer.and.returnValue(null);

      service.removeFromCart('cart1').subscribe({
        error: (error) => {
          expect(error.message).toBe('User must be logged in to remove items from cart');
        }
      });
    });
  });

  describe('clearCart', () => {
    it('should clear all user cart items', () => {
      spyOn(service['cartItemsSubject'], 'getValue').and.returnValue([mockCartItem]);

      service.clearCart().subscribe(() => {
        expect().nothing();
      });

      const req = httpMock.expectOne('http://localhost:3000/cart/cart1');
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });

    it('should return null when cart is already empty', () => {
      spyOn(service['cartItemsSubject'], 'getValue').and.returnValue([]);

      service.clearCart().subscribe(result => {
        expect(result).toBeNull();
      });
    });

    it('should throw error when user is not logged in', () => {
      authService.getCurrentCustomer.and.returnValue(null);

      service.clearCart().subscribe({
        error: (error) => {
          expect(error.message).toBe('User must be logged in to clear cart');
        }
      });
    });
  });

  describe('getCartItemCount', () => {
    it('should return total quantity of cart items', () => {
      spyOn(service['cartItemsSubject'], 'asObservable').and.returnValue(of([mockCartItem]));

      service.getCartItemCount().subscribe(count => {
        expect(count).toBe(2);
      });
    });

    it('should return 0 when cart is empty', () => {
      spyOn(service['cartItemsSubject'], 'asObservable').and.returnValue(of([]));

      service.getCartItemCount().subscribe(count => {
        expect(count).toBe(0);
      });
    });
  });

  describe('getCartTotal', () => {
    it('should return total price of cart items', () => {
      spyOn(service['cartItemsWithDetailsSubject'], 'asObservable').and.returnValue(of([mockCartItemWithDetails]));

      service.getCartTotal().subscribe(total => {
        expect(total).toBe(59.98); // 29.99 * 2
      });
    });

    it('should return 0 when cart is empty', () => {
      spyOn(service['cartItemsWithDetailsSubject'], 'asObservable').and.returnValue(of([]));

      service.getCartTotal().subscribe(total => {
        expect(total).toBe(0);
      });
    });
  });

  describe('isInCart', () => {
    it('should return true when book is in cart', () => {
      spyOn(service['cartItemsSubject'], 'asObservable').and.returnValue(of([mockCartItem]));

      service.isInCart('1').subscribe(isInCart => {
        expect(isInCart).toBe(true);
      });
    });

    it('should return false when book is not in cart', () => {
      spyOn(service['cartItemsSubject'], 'asObservable').and.returnValue(of([mockCartItem]));

      service.isInCart('999').subscribe(isInCart => {
        expect(isInCart).toBe(false);
      });
    });
  });

  describe('getCartItemByBookId', () => {
    it('should return cart item when book is in cart', () => {
      spyOn(service['cartItemsSubject'], 'asObservable').and.returnValue(of([mockCartItem]));

      service.getCartItemByBookId('1').subscribe(item => {
        expect(item).toEqual(mockCartItem);
      });
    });

    it('should return null when book is not in cart', () => {
      spyOn(service['cartItemsSubject'], 'asObservable').and.returnValue(of([mockCartItem]));

      service.getCartItemByBookId('999').subscribe(item => {
        expect(item).toBeNull();
      });
    });
  });

  describe('Observables', () => {
    it('should expose cartItems$ observable', () => {
      expect(service.cartItems$).toBeDefined();
    });

    it('should expose cartItemsWithDetails$ observable', () => {
      expect(service.cartItemsWithDetails$).toBeDefined();
    });
  });
}); 