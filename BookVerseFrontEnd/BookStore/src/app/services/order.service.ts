import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, forkJoin, throwError } from 'rxjs';
import { map, catchError, tap, switchMap } from 'rxjs/operators';
import { Order, OrderItem, OrderDto, OrderItemDto, OrderSummary, PaymentDetails, OrderStatusHistory, OrderWithDetails, OrderItemWithDetails } from '../models/order.model';
import { CartItemWithDetails } from '../models/cart.model';
import { Address } from '../models/address.model';
import { BookService } from './book.service';
import { CartService } from './cart.service';
import { AuthService } from './auth.service';
import { AdminNotificationsService } from './admin-notifications.service';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private apiBaseUrl = 'http://localhost:8090/api' // Temporary: Direct to service
  private apiUrl = `${this.apiBaseUrl}/orders`;

  constructor(
    private http: HttpClient,
    private bookService: BookService,
    private cartService: CartService,
    private authService: AuthService,
    private adminNotificationsService: AdminNotificationsService
  ) {}

  getOrders(): Observable<Order[]> {
    const currentUser = this.authService.getCurrentCustomer();
    if (!currentUser) {
      return throwError(() => new Error('User not logged in'));
    }

    // Use backend endpoint: /api/orders?userId={userId} or /api/orders for admin
    const isAdmin = currentUser.username && currentUser.username.toLowerCase().includes('admin');
    const url = isAdmin ? this.apiUrl : `${this.apiUrl}?userId=${currentUser.id}`;
    
    console.log('Fetching orders from URL:', url);
    
    return this.http.get<any>(url).pipe(
      map(response => {
        console.log('Raw orders response received:', response);
        console.log('Response type:', typeof response);
        console.log('Is array:', Array.isArray(response));
        
        // Handle different response formats
        let orders: Order[];
        if (Array.isArray(response)) {
          orders = response;
        } else if (response && response.data && Array.isArray(response.data)) {
          orders = response.data;
        } else if (response && response.orders && Array.isArray(response.orders)) {
          orders = response.orders;
        } else {
          console.warn('Unexpected response format, treating as empty array');
          orders = [];
        }
        
        console.log('Processed orders:', orders.length);
        return orders;
      }),
      catchError(error => {
        console.error('Orders API error details:', {
          status: error.status,
          statusText: error.statusText,
          url: error.url,
          message: error.message,
          error: error.error
        });
        
        // If it's a 200 response but treated as error, it might be a parsing issue
        if (error.status === 200) {
          console.log('Status 200 but treated as error - likely parsing issue');
          console.log('Error response body:', error.error);
          // Try to return empty array for 200 errors
          return of([]);
        }
        
        return throwError(() => error);
      })
    );
  }

  getOrdersWithDetails(): Observable<OrderWithDetails[]> {
    return this.getOrders().pipe(
      switchMap((orders: Order[]) => {
        if (orders.length === 0) return of([]);
        
        // For each order, fetch book details for all order items
        const orderRequests = orders.map((order: Order) => 
          this.loadOrderWithDetails(order)
        );
        
        return forkJoin(orderRequests);
      }),
      catchError(error => {
        console.error('Error loading orders with details:', error);
        return of([]);
      })
    );
  }

  private loadOrderWithDetails(order: Order): Observable<OrderWithDetails> {
    // Use 'orderItems' as the primary field name (backend structure)
    const orderItems = order.orderItems || order.items || [];
    
    // Populate template compatibility fields
    const enrichedOrder = {
      ...order,
      orderDate: order.placedAt || order.createdAt,
      totalAmount: order.subtotal,
      finalAmount: order.grandTotal,
      items: orderItems
    };
    
    if (orderItems.length === 0) {
      return of({ ...enrichedOrder, items: [] });
    }

    // Check if items already have book details embedded
    const itemsWithDetails = orderItems.map(item => {
      // If item has title, author, imageUrl, etc., it already has book details
      if (item.title && item.author) {
        // Create a book object from the embedded details
        const book = {
          id: item.bookId || item.id,
          title: item.title,
          author: item.author,
          image_urls: item.imageUrl ? [item.imageUrl] : [],
          price: item.price,
          description: '',
          isbn: '',
          publisher: '',
          publicationDate: '',
          language: '',
          pages: 0,
          format: '',
          stock: 0,
          rating: 0,
          reviews: []
        };
        return { ...item, book } as OrderItemWithDetails;
      } else {
        // Fetch book details if not embedded
        if (!item.bookId) {
          console.error('No bookId found for item:', item);
          return of({ ...item, book: null } as OrderItemWithDetails);
        }
        return this.bookService.getBookById(item.bookId).pipe(
          map(book => ({ ...item, book } as OrderItemWithDetails)),
          catchError(error => {
            console.error(`Error fetching book ${item.bookId}:`, error);
            // Return item without book details if fetch fails
            return of({ ...item, book: null } as OrderItemWithDetails);
          })
        );
      }
    });

    // If all items have embedded details, return immediately
    if (itemsWithDetails.every(item => typeof item === 'object' && 'book' in item)) {
      return of({
        ...enrichedOrder,
        items: itemsWithDetails as OrderItemWithDetails[]
      } as OrderWithDetails);
    }

    // Otherwise, fetch book details for items that need them
    const itemRequests = itemsWithDetails.map(item => 
      typeof item === 'object' && 'book' in item ? of(item) : item
    );

    return forkJoin(itemRequests).pipe(
      map(itemsWithDetails => ({
        ...enrichedOrder,
        items: itemsWithDetails
      } as OrderWithDetails)),
      catchError(error => {
        console.error('Error loading order items with details:', error);
        return of({ ...enrichedOrder, items: orderItems as OrderItemWithDetails[] });
      })
    );
  }

  getOrderById(orderId: string): Observable<Order | null> {
    return this.http.get<Order>(`${this.apiUrl}/${orderId}`).pipe(
      catchError(error => {
        console.error('Error fetching order:', error);
        return of(null);
      })
    );
  }

  getOrderByIdWithDetails(orderId: string): Observable<OrderWithDetails | null> {
    return this.getOrderById(orderId).pipe(
      switchMap((order: Order | null) => {
        if (!order) return of(null);
        return this.loadOrderWithDetails(order);
      }),
      catchError(error => {
        console.error('Error loading order with details:', error);
        return of(null);
      })
    );
  }

  createOrder(deliveryAddress: Address, paymentMethod: string, paymentDetails?: PaymentDetails): Observable<Order> {
    const currentUser = this.authService.getCurrentCustomer();
    if (!currentUser) {
      throw new Error('User must be logged in to create order');
    }

    // Get current cart items
    return this.cartService.cartItemsWithDetails$.pipe(
      switchMap((cartItems: CartItemWithDetails[]) => {
        if (cartItems.length === 0) {
          throw new Error('Cart is empty');
        }

        // Convert cart items to order items (matching backend DTO structure)
        const orderItems: OrderItemDto[] = cartItems.map((cartItem: CartItemWithDetails) => ({
          bookId: cartItem.bookId,
          title: cartItem.book.title,
          author: cartItem.book.author,
          price: cartItem.priceWhenAdded, // Use price when added to cart
          quantity: cartItem.quantity,
          subtotal: cartItem.priceWhenAdded * cartItem.quantity,
          imageUrl: cartItem.book.image_urls?.[0]
        }));

        // Calculate order summary
        const orderSummary = this.calculateOrderSummary(cartItems);
        
        // Create order DTO (matching backend OrderDto structure)
        const orderDto: OrderDto = {
          userId: currentUser.id,
          billingAddressId: deliveryAddress.id || undefined,
          shippingAddressId: deliveryAddress.id || undefined,
          subtotal: orderSummary.totalAmount,
          discountAmount: orderSummary.discount || 0,
          taxAmount: orderSummary.taxes || 0,
          shippingAmount: orderSummary.shippingFee || 0,
          platformFee: orderSummary.platformFee || 0,
          grandTotal: orderSummary.finalAmount,
          currency: 'INR',
          paymentMethod: paymentMethod.toUpperCase() as 'COD' | 'Card' | 'UPI' | 'NetBanking',
          paymentStatus: paymentMethod === 'COD' ? 'Pending' : 'Paid',
          orderStatus: 'Pending',
          notes: undefined,
          orderItems: orderItems
        };

        return this.http.post<Order>(this.apiUrl, orderDto).pipe(
          tap((createdOrder) => {
            // Create admin notification for new order (temporarily disabled due to auth issues)
            // this.adminNotificationsService.createOrderNotification(
            //   createdOrder.id,
            //   deliveryAddress.name,
            //   orderSummary.totalPayable
            // ).subscribe({
            //   next: (notification) => {
            //     console.log('Admin notification created for new order:', notification);
            //   },
            //   error: (error) => {
            //     console.error('Error creating admin notification:', error);
            //   }
            // });
            console.log('Order created successfully, admin notification temporarily disabled');
            
            // Clear cart after successful order creation
            // Note: Stock updates should be handled by the backend order service
            this.cartService.clearCart().subscribe({
              next: () => {
                console.log('Cart cleared after successful order creation');
              },
              error: (error) => {
                console.error('Error clearing cart:', error);
              }
            });
          }),
          catchError(error => {
            console.error('Error creating order:', error);
            throw error;
          })
        );
      }),
      catchError(error => {
        console.error('Error in order creation process:', error);
        throw error;
      })
    );
  }

  updateOrderStatus(orderId: string, newStatus: Order['orderStatus'], note?: string): Observable<Order> {
    // Use backend endpoint: PUT /api/orders/{orderId}/status?status={status}
    return this.http.put<Order>(`${this.apiUrl}/${orderId}/status`, null, {
      params: { status: newStatus! }
    }).pipe(
      catchError(error => {
        console.error('Error updating order status:', error);
        throw error;
      })
    );
  }

  updatePaymentStatus(orderId: string, paymentStatus: Order['paymentStatus'], paymentDetails?: PaymentDetails): Observable<Order> {
    // Use backend endpoint: PUT /api/orders/{orderId}/payment-status?paymentStatus={status}
    return this.http.put<Order>(`${this.apiUrl}/${orderId}/payment-status`, null, {
      params: { paymentStatus: paymentStatus! }
    }).pipe(
      catchError(error => {
        console.error('Error updating payment status:', error);
        throw error;
      })
    );
  }

  cancelOrder(orderId: string, reason?: string): Observable<Order> {
    return this.updateOrderStatus(orderId, 'Cancelled', reason);
  }

  returnOrder(orderId: string, reason?: string): Observable<Order> {
    // Note: backend doesn't have 'returned' status, using 'Cancelled' instead
    return this.updateOrderStatus(orderId, 'Cancelled', reason);
  }

  getOrderSummary(): Observable<OrderSummary> {
    return this.cartService.cartItemsWithDetails$.pipe(
      map(cartItems => this.calculateOrderSummary(cartItems)),
      catchError(error => {
        console.error('Error calculating order summary:', error);
        return of({
          totalAmount: 0,
          platformFee: 0,
          shippingFee: 0,
          taxes: 0,
          discount: 0,
          finalAmount: 0,
          totalPayable: 0,
          itemCount: 0
        });
      })
    );
  }

  private calculateOrderSummary(cartItems: CartItemWithDetails[]): OrderSummary {
    const totalAmount = cartItems.reduce((sum, item) => sum + item.subtotal, 0);
    const platformFee = 20; // Fixed platform fee
    const shippingFee = totalAmount >= 500 ? 0 : 50; // Free shipping over ₹500
    const taxes = Math.ceil(totalAmount * 0.18); // 18% GST
    const discount = 0; // No discount for now
    const finalAmount = totalAmount + platformFee + shippingFee + taxes - discount;

    return {
      totalAmount,
      platformFee,
      shippingFee,
      taxes,
      discount,
      finalAmount,
      totalPayable: finalAmount,
      itemCount: cartItems.length
    };
  }

  private calculateEstimatedDelivery(): string {
    const estimatedDate = new Date();
    estimatedDate.setDate(estimatedDate.getDate() + 7); // 7 days from now
    return estimatedDate.toISOString();
  }

  private generateTrackingId(): string {
    return 'BV' + Math.random().toString(36).substr(2, 9).toUpperCase();
  }

  // Method for admins to get all orders
  getAllOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(this.apiUrl).pipe(
      catchError(error => {
        console.error('Error fetching all orders:', error);
        return of([]);
      })
    );
  }

  getOrdersByStatus(status: Order['orderStatus']): Observable<Order[]> {
    const currentUser = this.authService.getCurrentCustomer();
    if (!currentUser) {
      return of([]);
    }

    // Backend doesn't seem to support status filtering directly, so filter client-side
    return this.getOrders().pipe(
      map(orders => orders.filter(order => order.orderStatus === status)),
      catchError(error => {
        console.error('Error fetching orders by status:', error);
        return of([]);
      })
    );
  }

  searchOrders(query: string): Observable<Order[]> {
    const currentUser = this.authService.getCurrentCustomer();
    if (!currentUser) {
      return of([]);
    }

    // Backend doesn't seem to support search directly, so filter client-side
    return this.getOrders().pipe(
      map(orders => orders.filter(order => 
        order.id.toLowerCase().includes(query.toLowerCase()) ||
        order.trackingId?.toLowerCase().includes(query.toLowerCase())
      )),
      catchError(error => {
        console.error('Error searching orders:', error);
        return of([]);
      })
    );
  }

  getOrderStatistics(): Observable<any> {
    const currentUser = this.authService.getCurrentCustomer();
    if (!currentUser) {
      return of({});
    }

    return this.getOrders().pipe(
      map(orders => {
        const totalOrders = orders.length;
        const completedOrders = orders.filter(order => order.orderStatus === 'Delivered').length;
        const pendingOrders = orders.filter(order => ['Pending', 'Confirmed', 'Shipped'].includes(order.orderStatus || '')).length;
        const cancelledOrders = orders.filter(order => order.orderStatus === 'Cancelled').length;
        const totalSpent = orders.reduce((sum, order) => sum + (order.grandTotal || 0), 0);

        return {
          totalOrders,
          completedOrders,
          pendingOrders,
          cancelledOrders,
          totalSpent
        };
      }),
      catchError(error => {
        console.error('Error calculating order statistics:', error);
        return of({});
      })
    );
  }
} 