import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { WishlistService, WishlistItem, WishlistItemWithDetails } from './wishlist.service';
import { BookService } from './book.service';
import { AuthService } from './auth.service';
import { BookModel } from '../models/book.model';
import { of, throwError } from 'rxjs';

describe('WishlistService', () => {
  let service: WishlistService;
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

  const mockWishlistItem: WishlistItem = {
    id: 'wish1',
    bookId: '1',
    userId: 'user1',
    addedAt: '2024-01-01T00:00:00Z'
  };

  const mockWishlistItemWithDetails: WishlistItemWithDetails = {
    ...mockWishlistItem,
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
        WishlistService,
        { provide: BookService, useValue: bookServiceSpy },
        { provide: AuthService, useValue: authServiceSpy }
      ]
    });

    service = TestBed.inject(WishlistService);
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

  describe('addToWishlist', () => {
    it('should add new book to wishlist', () => {
      service.addToWishlist(mockBook).subscribe(item => {
        expect(item).toEqual(mockWishlistItem);
      });

      const req = httpMock.expectOne('http://localhost:3000/wishlist');
      expect(req.request.method).toBe('POST');
      req.flush(mockWishlistItem);
    });

    it('should throw error when book is already in wishlist', () => {
      spyOn(service['wishlistItemsSubject'], 'getValue').and.returnValue([mockWishlistItem]);

      service.addToWishlist(mockBook).subscribe({
        error: (error) => {
          expect(error.message).toBe('Book is already in your wishlist');
        }
      });
    });

    it('should throw error when user is not logged in', () => {
      authService.getCurrentCustomer.and.returnValue(null);

      service.addToWishlist(mockBook).subscribe({
        error: (error) => {
          expect(error.message).toBe('User must be logged in to add items to wishlist');
        }
      });
    });
  });

  describe('removeFromWishlist', () => {
    it('should remove item from wishlist by wishlist item ID', () => {
      service.removeFromWishlist('wish1').subscribe(() => {
        expect().nothing();
      });

      const req = httpMock.expectOne('http://localhost:3000/wishlist/wish1');
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });

    it('should throw error when user is not logged in', () => {
      authService.getCurrentCustomer.and.returnValue(null);

      service.removeFromWishlist('wish1').subscribe({
        error: (error) => {
          expect(error.message).toBe('User must be logged in to remove items from wishlist');
        }
      });
    });
  });

  describe('removeBookFromWishlist', () => {
    it('should remove book from wishlist by book ID', () => {
      spyOn(service['wishlistItemsSubject'], 'getValue').and.returnValue([mockWishlistItem]);

      service.removeBookFromWishlist('1').subscribe(() => {
        expect().nothing();
      });

      const req = httpMock.expectOne('http://localhost:3000/wishlist/wish1');
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });

    it('should throw error when book is not in wishlist', () => {
      spyOn(service['wishlistItemsSubject'], 'getValue').and.returnValue([]);

      service.removeBookFromWishlist('999').subscribe({
        error: (error) => {
          expect(error.message).toBe('Item not found in wishlist');
        }
      });
    });

    it('should throw error when user is not logged in', () => {
      authService.getCurrentCustomer.and.returnValue(null);

      service.removeBookFromWishlist('1').subscribe({
        error: (error) => {
          expect(error.message).toBe('User must be logged in to remove items from wishlist');
        }
      });
    });
  });

  describe('isInWishlist', () => {
    it('should return true when book is in wishlist', () => {
      spyOn(service['wishlistItemsSubject'], 'asObservable').and.returnValue(of([mockWishlistItem]));

      service.isInWishlist('1').subscribe(isInWishlist => {
        expect(isInWishlist).toBe(true);
      });
    });

    it('should return false when book is not in wishlist', () => {
      spyOn(service['wishlistItemsSubject'], 'asObservable').and.returnValue(of([mockWishlistItem]));

      service.isInWishlist('999').subscribe(isInWishlist => {
        expect(isInWishlist).toBe(false);
      });
    });
  });

  describe('getWishlistCount', () => {
    it('should return wishlist item count', () => {
      spyOn(service['wishlistItemsSubject'], 'asObservable').and.returnValue(of([mockWishlistItem]));

      service.getWishlistCount().subscribe(count => {
        expect(count).toBe(1);
      });
    });

    it('should return 0 when wishlist is empty', () => {
      spyOn(service['wishlistItemsSubject'], 'asObservable').and.returnValue(of([]));

      service.getWishlistCount().subscribe(count => {
        expect(count).toBe(0);
      });
    });
  });

  describe('clearWishlist', () => {
    it('should clear all user wishlist items', () => {
      spyOn(service['wishlistItemsSubject'], 'getValue').and.returnValue([mockWishlistItem]);

      service.clearWishlist().subscribe(() => {
        expect().nothing();
      });

      const req = httpMock.expectOne('http://localhost:3000/wishlist/wish1');
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });

    it('should return null when wishlist is already empty', () => {
      spyOn(service['wishlistItemsSubject'], 'getValue').and.returnValue([]);

      service.clearWishlist().subscribe(result => {
        expect(result).toBeNull();
      });
    });

    it('should throw error when user is not logged in', () => {
      authService.getCurrentCustomer.and.returnValue(null);

      service.clearWishlist().subscribe({
        error: (error) => {
          expect(error.message).toBe('User must be logged in to clear wishlist');
        }
      });
    });
  });

  describe('getWishlistItemByBookId', () => {
    it('should return wishlist item when book is in wishlist', () => {
      spyOn(service['wishlistItemsSubject'], 'asObservable').and.returnValue(of([mockWishlistItem]));

      service.getWishlistItemByBookId('1').subscribe(item => {
        expect(item).toEqual(mockWishlistItem);
      });
    });

    it('should return null when book is not in wishlist', () => {
      spyOn(service['wishlistItemsSubject'], 'asObservable').and.returnValue(of([mockWishlistItem]));

      service.getWishlistItemByBookId('999').subscribe(item => {
        expect(item).toBeNull();
      });
    });
  });

  describe('Observables', () => {
    it('should expose wishlistItems$ observable', () => {
      expect(service.wishlistItems$).toBeDefined();
    });

    it('should expose wishlistItemsWithDetails$ observable', () => {
      expect(service.wishlistItemsWithDetails$).toBeDefined();
    });
  });

  describe('loadWishlistItemsWithDetails', () => {
    it('should load wishlist items with book details', () => {
      spyOn(service['wishlistItemsSubject'], 'asObservable').and.returnValue(of([mockWishlistItem]));

      service['loadWishlistItemsWithDetails']([mockWishlistItem]);

      service.wishlistItemsWithDetails$.subscribe(items => {
        if (items.length > 0) {
          expect(items[0]).toEqual(mockWishlistItemWithDetails);
        }
      });
    });

    it('should handle empty wishlist items', () => {
      service['loadWishlistItemsWithDetails']([]);

      service.wishlistItemsWithDetails$.subscribe(items => {
        expect(items).toEqual([]);
      });
    });

    it('should handle book service errors gracefully', () => {
      bookService.getBookById.and.returnValue(throwError(() => new Error('Book not found')));

      service['loadWishlistItemsWithDetails']([mockWishlistItem]);

      service.wishlistItemsWithDetails$.subscribe(items => {
        expect(items).toEqual([]);
      });
    });
  });

  describe('loadUserWishlistItems', () => {
    it('should load user wishlist items from API', () => {
      service['loadUserWishlistItems']('user1');

      const req = httpMock.expectOne('http://localhost:3000/wishlist?userId=user1');
      expect(req.request.method).toBe('GET');
      req.flush([mockWishlistItem]);
    });

    it('should handle API errors gracefully', () => {
      service['loadUserWishlistItems']('user1');

      const req = httpMock.expectOne('http://localhost:3000/wishlist?userId=user1');
      req.error(new ErrorEvent('Network error'));

      service.wishlistItems$.subscribe(items => {
        expect(items).toEqual([]);
      });
    });
  });
}); 