import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { OrderService } from './order.service';
import { BookService } from './book.service';
import { CartService } from './cart.service';
import { AuthService } from './auth.service';
import { AdminNotificationsService } from './admin-notifications.service';
import { Order, OrderItem, OrderSummary, PaymentDetails, OrderStatusHistory, OrderWithDetails } from '../models/order.model';
import { Address } from '../models/address.model';
import { BookModel } from '../models/book.model';
import { of, throwError } from 'rxjs';

describe('OrderService', () => {
  let service: OrderService;
  let httpMock: HttpTestingController;
  let bookService: jasmine.SpyObj<BookService>;
  let cartService: jasmine.SpyObj<CartService>;
  let authService: jasmine.SpyObj<AuthService>;
  let adminNotificationsService: jasmine.SpyObj<AdminNotificationsService>;

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

  const mockOrderItem: OrderItem = {
    id: 'item1',
    bookId: '1',
    title: 'Test Book',
    author: 'Test Author',
    quantity: 2,
    price: 29.99,
    image_urls: ['test-image.jpg'],
    category: 'Fiction',
    addedAt: '2024-01-01T00:00:00Z'
  };

  const mockAddress: Address = {
    name: 'Test User',
    phone: '1234567890',
    pincode: '12345',
    address: '123 Test St',
    locality: 'Test Locality',
    city: 'Test City',
    state: 'Test State',
    country: 'Test Country',
    addressType: 'Home'
  };

  const mockOrder: Order = {
    id: 'order1',
    userId: 'user1',
    items: [mockOrderItem],
    shippingAddress: mockAddress,
    orderDate: '2024-01-01T00:00:00Z',
    orderStatus: 'pending',
    paymentMethod: 'credit_card',
    paymentStatus: 'paid',
    totalAmount: 59.98,
    platformFee: 2.99,
    shippingFee: 5.99,
    taxes: 3.59,
    discount: 0,
    finalAmount: 71.55,
    totalPayable: 71.55,
    estimatedDelivery: '2024-01-05T00:00:00Z',
    trackingId: 'TRACK123',
    statusHistory: [{
      status: 'pending',
      timestamp: '2024-01-01T00:00:00Z',
      note: 'Order placed successfully'
    }],
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z'
  };

  const mockOrderWithDetails: OrderWithDetails = {
    ...mockOrder,
    items: [{
      ...mockOrderItem,
      book: mockBook
    }]
  };

  const mockUser = {
    id: 'user1',
    fullName: 'Test User',
    username: 'testuser',
    email: 'test@example.com',
    mobileNumber: '1234567890'
  };

  beforeEach(() => {
    const bookServiceSpy = jasmine.createSpyObj('BookService', ['getBookById', 'updateBookSalesCount', 'updateBookStock']);
    const cartServiceSpy = jasmine.createSpyObj('CartService', ['clearCart'], {
      cartItemsWithDetails$: of([{
        id: 'cart1',
        bookId: '1',
        userId: 'user1',
        quantity: 2,
        addedAt: '2024-01-01T00:00:00Z',
        book: mockBook
      }])
    });
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['getCurrentCustomer']);
    const adminNotificationsServiceSpy = jasmine.createSpyObj('AdminNotificationsService', ['createOrderNotification']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        OrderService,
        { provide: BookService, useValue: bookServiceSpy },
        { provide: CartService, useValue: cartServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        { provide: AdminNotificationsService, useValue: adminNotificationsServiceSpy }
      ]
    });

    service = TestBed.inject(OrderService);
    httpMock = TestBed.inject(HttpTestingController);
    bookService = TestBed.inject(BookService) as jasmine.SpyObj<BookService>;
    cartService = TestBed.inject(CartService) as jasmine.SpyObj<CartService>;
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    adminNotificationsService = TestBed.inject(AdminNotificationsService) as jasmine.SpyObj<AdminNotificationsService>;

    // Setup default return values
    authService.getCurrentCustomer.and.returnValue(mockUser);
    bookService.getBookById.and.returnValue(of(mockBook));
    bookService.updateBookSalesCount.and.returnValue(of({ 
      id: '1', 
      category: 'newly launched', 
      no_of_books_sold: 1 
    }));
    bookService.updateBookStock.and.returnValue(of({
      ...mockBook,
      stock_actual: mockBook.stock_actual - 1
    }));
    cartService.clearCart.and.returnValue(of(null));
    adminNotificationsService.createOrderNotification.and.returnValue(of({ 
      id: '1', 
      type: 'order', 
      title: 'New Order', 
      message: 'New order received',
      isRead: false,
      createdAt: new Date().toISOString(),
      priority: 'medium'
    }));
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getOrders', () => {
    it('should get orders for regular user', () => {
      service.getOrders().subscribe(orders => {
        expect(orders).toEqual([mockOrder]);
      });

      const req = httpMock.expectOne('http://localhost:3000/orders?userId=user1');
      expect(req.request.method).toBe('GET');
      req.flush([mockOrder]);
    });

    it('should get all orders for admin user', () => {
      const adminUser = { ...mockUser, username: 'admin' };
      authService.getCurrentCustomer.and.returnValue(adminUser);

      service.getOrders().subscribe(orders => {
        expect(orders).toEqual([mockOrder]);
      });

      const req = httpMock.expectOne('http://localhost:3000/orders');
      expect(req.request.method).toBe('GET');
      req.flush([mockOrder]);
    });

    it('should throw error when user is not logged in', () => {
      authService.getCurrentCustomer.and.returnValue(null);

      service.getOrders().subscribe({
        error: (error) => {
          expect(error.message).toBe('User not logged in');
        }
      });
    });
  });

  describe('getOrdersWithDetails', () => {
    it('should get orders with book details', () => {
      service.getOrdersWithDetails().subscribe(orders => {
        expect(orders).toEqual([mockOrderWithDetails]);
      });

      const req = httpMock.expectOne('http://localhost:3000/orders?userId=user1');
      expect(req.request.method).toBe('GET');
      req.flush([mockOrder]);
    });

    it('should return empty array when no orders', () => {
      service.getOrdersWithDetails().subscribe(orders => {
        expect(orders).toEqual([]);
      });

      const req = httpMock.expectOne('http://localhost:3000/orders?userId=user1');
      expect(req.request.method).toBe('GET');
      req.flush([]);
    });

    it('should handle errors gracefully', () => {
      service.getOrdersWithDetails().subscribe(orders => {
        expect(orders).toEqual([]);
      });

      const req = httpMock.expectOne('http://localhost:3000/orders?userId=user1');
      req.error(new ErrorEvent('Network error'));
    });
  });

  describe('createOrder', () => {
    it('should create order successfully', () => {
      service.createOrder(mockAddress, 'credit_card').subscribe(order => {
        expect(order).toBeTruthy();
        expect(order.orderStatus).toBe('pending');
        expect(order.paymentStatus).toBe('paid');
      });

      const req = httpMock.expectOne('http://localhost:3000/orders');
      expect(req.request.method).toBe('POST');
      req.flush(mockOrder);
    });

    it('should create order with COD payment method', () => {
      service.createOrder(mockAddress, 'COD').subscribe(order => {
        expect(order.paymentStatus).toBe('pending');
      });

      const req = httpMock.expectOne('http://localhost:3000/orders');
      expect(req.request.method).toBe('POST');
      req.flush({ ...mockOrder, paymentStatus: 'pending' });
    });

    it('should throw error when user is not logged in', () => {
      authService.getCurrentCustomer.and.returnValue(null);

      expect(() => {
        service.createOrder(mockAddress, 'credit_card').subscribe();
      }).toThrowError('User must be logged in to create order');
    });

    it('should throw error when cart is empty', () => {
      cartService.cartItemsWithDetails$ = of([]);

      expect(() => {
        service.createOrder(mockAddress, 'credit_card').subscribe();
      }).toThrowError('Cart is empty');
    });

    it('should clear cart after successful order creation', () => {
      service.createOrder(mockAddress, 'credit_card').subscribe();

      const req = httpMock.expectOne('http://localhost:3000/orders');
      req.flush(mockOrder);

      expect(cartService.clearCart).toHaveBeenCalled();
    });

    it('should create admin notification after successful order', () => {
      service.createOrder(mockAddress, 'credit_card').subscribe();

      const req = httpMock.expectOne('http://localhost:3000/orders');
      req.flush(mockOrder);

      expect(adminNotificationsService.createOrderNotification).toHaveBeenCalled();
    });
  });

  describe('getOrderById', () => {
    it('should get order by id', () => {
      service.getOrderById('order1').subscribe(order => {
        expect(order).toEqual(mockOrder);
      });

      const req = httpMock.expectOne('http://localhost:3000/orders/order1');
      expect(req.request.method).toBe('GET');
      req.flush(mockOrder);
    });

    it('should handle error when order not found', () => {
      service.getOrderById('order999').subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        }
      });

      const req = httpMock.expectOne('http://localhost:3000/orders/order999');
      req.error(new ErrorEvent('Not found'), { status: 404 });
    });
  });

  describe('updateOrderStatus', () => {
    it('should update order status', () => {
      const statusUpdate = 'shipped';

      service.updateOrderStatus('order1', statusUpdate).subscribe(order => {
        expect(order.orderStatus).toBe('shipped');
      });

      const req = httpMock.expectOne('http://localhost:3000/orders/order1');
      expect(req.request.method).toBe('PATCH');
      req.flush({ ...mockOrder, orderStatus: 'shipped' });
    });

    it('should handle error when updating order status', () => {
      const statusUpdate = 'cancelled';

      service.updateOrderStatus('order1', statusUpdate).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        }
      });

      const req = httpMock.expectOne('http://localhost:3000/orders/order1');
      req.error(new ErrorEvent('Bad Request'), { status: 400 });
    });
  });

  describe('cancelOrder', () => {
    it('should cancel order successfully', () => {
      service.cancelOrder('order1', 'Customer request').subscribe(order => {
        expect(order.orderStatus).toBe('cancelled');
      });

      const req = httpMock.expectOne('http://localhost:3000/orders/order1');
      expect(req.request.method).toBe('PATCH');
      req.flush({ ...mockOrder, orderStatus: 'cancelled' });
    });

    it('should handle error when cancelling order', () => {
      service.cancelOrder('order1', 'Customer request').subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        }
      });

      const req = httpMock.expectOne('http://localhost:3000/orders/order1');
      req.error(new ErrorEvent('Bad Request'), { status: 400 });
    });
  });



  describe('getOrderSummary', () => {
    it('should get order summary from cart items', () => {
      service.getOrderSummary().subscribe(summary => {
        expect(summary).toBeTruthy();
        expect(summary.totalAmount).toBe(59.98);
        expect(summary.platformFee).toBe(20);
        expect(summary.shippingFee).toBe(50);
      });
    });

    it('should handle errors when calculating order summary', () => {
      cartService.cartItemsWithDetails$ = throwError(() => new Error('Cart error'));

      service.getOrderSummary().subscribe(summary => {
        expect(summary.totalAmount).toBe(0);
        expect(summary.platformFee).toBe(0);
        expect(summary.shippingFee).toBe(0);
      });
    });
  });
}); 