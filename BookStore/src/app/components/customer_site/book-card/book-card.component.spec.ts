import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { BookCardComponent } from './book-card.component';
import { CartService } from '../../../services/cart.service';
import { WishlistService } from '../../../services/wishlist.service';
import { NotificationService } from '../../../services/notification.service';
import { AuthService } from '../../../services/auth.service';
import { BookModel } from '../../../models/book.model';
import { of, throwError } from 'rxjs';
import { RouterTestingModule } from '@angular/router/testing';

describe('BookCardComponent', () => {
  let component: BookCardComponent;
  let fixture: ComponentFixture<BookCardComponent>;
  let cartService: jasmine.SpyObj<CartService>;
  let wishlistService: jasmine.SpyObj<WishlistService>;
  let notificationService: jasmine.SpyObj<NotificationService>;
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

  const mockUser = {
    id: '1',
    fullName: 'Test User',
    username: 'testuser',
    email: 'test@example.com',
    mobileNumber: '1234567890'
  };

  const mockCartItem = {
    id: '1',
    bookId: '1',
    userId: 'user1',
    quantity: 1,
    addedAt: '2024-01-01T00:00:00Z'
  };

  beforeEach(async () => {
    const cartServiceSpy = jasmine.createSpyObj('CartService', ['addToCart']);
    const wishlistServiceSpy = jasmine.createSpyObj('WishlistService', [
      'isInWishlist', 'addToWishlist', 'getWishlistItemByBookId', 'removeFromWishlist'
    ]);
    const notificationServiceSpy = jasmine.createSpyObj('NotificationService', [
      'authError', 'cartSuccess', 'cartError', 'wishlistSuccess', 'wishlistError', 'success', 'error'
    ]);
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['getCurrentCustomer']);

    await TestBed.configureTestingModule({
      imports: [BookCardComponent, RouterTestingModule],
      providers: [
        { provide: CartService, useValue: cartServiceSpy },
        { provide: WishlistService, useValue: wishlistServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: AuthService, useValue: authServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(BookCardComponent);
    component = fixture.componentInstance;
    cartService = TestBed.inject(CartService) as jasmine.SpyObj<CartService>;
    wishlistService = TestBed.inject(WishlistService) as jasmine.SpyObj<WishlistService>;
    notificationService = TestBed.inject(NotificationService) as jasmine.SpyObj<NotificationService>;
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;

    component.book = mockBook;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with default values', () => {
    expect(component.cardSize).toBe('compact');
    expect(component.showActions).toBe(true);
    expect(component.addingToCart).toBe(false);
    expect(component.addingToWishlist).toBe(false);
  });

  it('should set book input correctly', () => {
    expect(component.book).toEqual(mockBook);
  });

  describe('ngOnInit', () => {
    it('should initialize wishlist status when showActions is true', () => {
      wishlistService.isInWishlist.and.returnValue(of(true));
      component.ngOnInit();
      expect(wishlistService.isInWishlist).toHaveBeenCalledWith(mockBook.id);
    });

    it('should not initialize wishlist status when showActions is false', () => {
      component.showActions = false;
      component.ngOnInit();
      expect(wishlistService.isInWishlist).not.toHaveBeenCalled();
    });
  });

  describe('isLoggedIn', () => {
    it('should return true when user is logged in', () => {
      authService.getCurrentCustomer.and.returnValue(mockUser);
      expect(component.isLoggedIn()).toBe(true);
    });

    it('should return false when user is not logged in', () => {
      authService.getCurrentCustomer.and.returnValue(null);
      expect(component.isLoggedIn()).toBe(false);
    });
  });

  describe('addToCart', () => {
    const mockEvent = { preventDefault: jasmine.createSpy(), stopPropagation: jasmine.createSpy() } as any;

    it('should prevent default event behavior', () => {
      component.addToCart(mockEvent);
      expect(mockEvent.preventDefault).toHaveBeenCalled();
      expect(mockEvent.stopPropagation).toHaveBeenCalled();
    });

    it('should show auth error when user is not logged in', () => {
      authService.getCurrentCustomer.and.returnValue(null);
      component.addToCart(mockEvent);
      expect(notificationService.authError).toHaveBeenCalledWith('Please login to add items to cart');
    });

    it('should not add to cart when book is out of stock', () => {
      authService.getCurrentCustomer.and.returnValue(mockUser);
      component.book = { ...mockBook, stock_actual: 0 };
      component.addToCart(mockEvent);
      expect(cartService.addToCart).not.toHaveBeenCalled();
    });

    it('should not add to cart when already adding', () => {
      authService.getCurrentCustomer.and.returnValue(mockUser);
      component.addingToCart = true;
      component.addToCart(mockEvent);
      expect(cartService.addToCart).not.toHaveBeenCalled();
    });

    it('should successfully add book to cart', fakeAsync(() => {
      authService.getCurrentCustomer.and.returnValue(mockUser);
      cartService.addToCart.and.returnValue(of(mockCartItem));
      
      component.addToCart(mockEvent);
      tick();
      
      expect(cartService.addToCart).toHaveBeenCalledWith(mockBook);
      expect(notificationService.cartSuccess).toHaveBeenCalledWith(mockBook.title);
      expect(component.addingToCart).toBe(false);
    }));

    it('should handle cart error', fakeAsync(() => {
      authService.getCurrentCustomer.and.returnValue(mockUser);
      const error = { message: 'Cart error' };
      cartService.addToCart.and.returnValue(throwError(() => error));
      
      component.addToCart(mockEvent);
      tick();
      
      expect(notificationService.cartError).toHaveBeenCalledWith(mockBook.title, error.message);
      expect(component.addingToCart).toBe(false);
    }));
  });

  describe('toggleWishlist', () => {
    const mockEvent = { preventDefault: jasmine.createSpy(), stopPropagation: jasmine.createSpy() } as any;

    it('should prevent default event behavior', () => {
      component.toggleWishlist(mockEvent);
      expect(mockEvent.preventDefault).toHaveBeenCalled();
      expect(mockEvent.stopPropagation).toHaveBeenCalled();
    });

    it('should show auth error when user is not logged in', () => {
      authService.getCurrentCustomer.and.returnValue(null);
      component.toggleWishlist(mockEvent);
      expect(notificationService.authError).toHaveBeenCalledWith('Please login to manage your wishlist');
    });

    it('should not toggle wishlist when already processing', () => {
      authService.getCurrentCustomer.and.returnValue(mockUser);
      component.addingToWishlist = true;
      component.toggleWishlist(mockEvent);
      expect(wishlistService.isInWishlist).not.toHaveBeenCalled();
    });
  });

  describe('getStockStatus', () => {
    it('should return out-of-stock when stock is 0', () => {
      component.book = { ...mockBook, stock_actual: 0 };
      expect(component.getStockStatus()).toBe('out-of-stock');
    });

    it('should return low-stock when stock is 5 or less', () => {
      component.book = { ...mockBook, stock_actual: 5 };
      expect(component.getStockStatus()).toBe('low-stock');
      
      component.book = { ...mockBook, stock_actual: 3 };
      expect(component.getStockStatus()).toBe('low-stock');
    });

    it('should return in-stock when stock is more than 5', () => {
      component.book = { ...mockBook, stock_actual: 6 };
      expect(component.getStockStatus()).toBe('in-stock');
      
      component.book = { ...mockBook, stock_actual: 10 };
      expect(component.getStockStatus()).toBe('in-stock');
    });
  });

  describe('isOutOfStock', () => {
    it('should return true when stock is 0', () => {
      component.book = { ...mockBook, stock_actual: 0 };
      expect(component.isOutOfStock()).toBe(true);
    });

    it('should return false when stock is greater than 0', () => {
      component.book = { ...mockBook, stock_actual: 1 };
      expect(component.isOutOfStock()).toBe(false);
      
      component.book = { ...mockBook, stock_actual: 10 };
      expect(component.isOutOfStock()).toBe(false);
    });
  });

  describe('Input properties', () => {
    it('should accept different card sizes', () => {
      component.cardSize = 'big';
      expect(component.cardSize).toBe('big');
      
      component.cardSize = 'compact';
      expect(component.cardSize).toBe('compact');
    });

    it('should toggle showActions', () => {
      component.showActions = false;
      expect(component.showActions).toBe(false);
      
      component.showActions = true;
      expect(component.showActions).toBe(true);
    });
  });
});
