import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, forkJoin, throwError } from 'rxjs';
import { map, catchError, tap, switchMap } from 'rxjs/operators';
import { Order, OrderItem, OrderSummary, PaymentDetails, OrderStatusHistory, OrderWithDetails, OrderItemWithDetails } from '../models/order.model';
import { Address } from '../models/address.model';
import { BookService } from './book.service';
import { CartService, CartItemWithDetails } from './cart.service';
import { AuthService } from './auth.service';
import { AdminNotificationsService } from './admin-notifications.service';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private apiBaseUrl = 'http://localhost:8090/api'; // API Gateway URL
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

    // Check if user is admin (for admin dashboard)
    const isAdmin = currentUser.username && currentUser.username.toLowerCase().includes('admin');
    const url = isAdmin ? this.apiUrl : `${this.apiUrl}?userId=${currentUser.id}`;
    
    return this.http.get<Order[]>(url);
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
    // Use 'items' as the primary field name
    const orderItems = order.items || order.orderItems || [];
    
    if (orderItems.length === 0) {
      return of({ ...order, items: [] });
    }

    // Check if items already have book details embedded
    const itemsWithDetails = orderItems.map(item => {
      // If item has title, author, image_urls, etc., it already has book details
      if (item.title && item.author && item.image_urls) {
        // Create a book object from the embedded details
        const book = {
          id: item.bookId || item.id,
          title: item.title,
          author: item.author,
          image_urls: item.image_urls,
          category: item.category,
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
        ...order,
        items: itemsWithDetails as OrderItemWithDetails[]
      } as OrderWithDetails);
    }

    // Otherwise, fetch book details for items that need them
    const itemRequests = itemsWithDetails.map(item => 
      typeof item === 'object' && 'book' in item ? of(item) : item
    );

    return forkJoin(itemRequests).pipe(
      map(itemsWithDetails => ({
        ...order,
        items: itemsWithDetails
      } as OrderWithDetails)),
      catchError(error => {
        console.error('Error loading order items with details:', error);
        return of({ ...order, items: order.items as OrderItemWithDetails[] });
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

        // Convert cart items to order items
        const orderItems: OrderItem[] = cartItems.map((cartItem: CartItemWithDetails) => ({
          id: crypto.randomUUID(),
          bookId: cartItem.bookId || cartItem.book.id,
          title: cartItem.book.title,
          author: cartItem.book.author,
          quantity: cartItem.quantity,
          price: cartItem.book.price, // Store current price for historical accuracy
          image_urls: cartItem.book.image_urls,
          category: cartItem.book.categories?.[0] ? 
            (typeof cartItem.book.categories[0] === 'string' ? cartItem.book.categories[0] : cartItem.book.categories[0].name) 
            : 'Unknown',
          addedAt: new Date().toISOString()
        }));

        // Calculate order summary
        const orderSummary = this.calculateOrderSummary(cartItems);
        
        // Create order object
        const newOrder: any = {
          id: crypto.randomUUID(),
          userId: currentUser.id,
          items: orderItems, // Use 'items' to match JSON structure
          shippingAddress: deliveryAddress,
          orderDate: new Date().toISOString(),
          orderStatus: 'pending',
          paymentMethod: paymentMethod,
          paymentStatus: paymentMethod === 'COD' ? 'pending' : 'paid',
          paymentDetails: paymentDetails,
          totalAmount: orderSummary.totalAmount,
          platformFee: orderSummary.platformFee,
          shippingFee: orderSummary.shippingFee,
          taxes: orderSummary.taxes,
          discount: orderSummary.discount,
          finalAmount: orderSummary.finalAmount,
          totalPayable: orderSummary.totalPayable,
          estimatedDelivery: this.calculateEstimatedDelivery(),
          trackingId: this.generateTrackingId(),
          statusHistory: [{
            status: 'pending',
            timestamp: new Date().toISOString(),
            note: 'Order placed successfully'
          }],
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        };

        return this.http.post<Order>(this.apiUrl, newOrder).pipe(
          tap(() => {
            // Clear cart after successful order creation
            this.cartService.clearCart().subscribe();
            
            // Create admin notification for new order
            this.adminNotificationsService.createOrderNotification(
              newOrder.id,
              deliveryAddress.name,
              orderSummary.totalPayable
            ).subscribe({
              next: (notification) => {
                console.log('Admin notification created for new order:', notification);
              },
              error: (error) => {
                console.error('Error creating admin notification:', error);
              }
            });
            
            // Update sales counts and decrease stock for all books in the order
            orderItems.forEach(item => {
              if (item.bookId) {
                // Try to update sales count for each category the book might be in
                const categories: ('newly launched' | 'highly rated' | 'special offers')[] = ['newly launched', 'highly rated', 'special offers'];
                categories.forEach(category => {
                  this.bookService.updateBookSalesCount(item.bookId!.toString(), category).subscribe({
                    next: (result) => {
                      if (result) {
                        console.log(`Updated sales count for book ${item.bookId} in ${category}`);
                      }
                    },
                    error: (error) => {
                      // Book might not be in this category, which is fine
                      console.log(`Book ${item.bookId} not found in ${category} category`);
                    }
                  });
                });

                // Decrease book stock
                this.bookService.updateBookStock(item.bookId!, item.quantity).subscribe({
                  next: (result) => {
                    console.log(`Decreased stock for book ${item.bookId} by ${item.quantity}`);
                    
                    // Check if stock is now low and create notification
                    if (result.stock_actual < 20) {
                      this.adminNotificationsService.createLowStockNotification(
                        result.id,
                        result.title,
                        result.stock_actual
                      ).subscribe({
                        next: (notification) => {
                          console.log('Low stock notification created:', notification);
                        },
                        error: (error) => {
                          console.error('Error creating low stock notification:', error);
                        }
                      });
                    }
                  },
                  error: (error) => {
                    console.error(`Error decreasing stock for book ${item.bookId}:`, error);
                  }
                });
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
    return this.getOrderById(orderId).pipe(
      switchMap((order: Order | null) => {
        if (!order) {
          throw new Error('Order not found');
        }

        // Add to status history
        const statusUpdate: OrderStatusHistory = {
          status: newStatus,
          timestamp: new Date().toISOString(),
          note: note
        };

        const updatedOrder = {
          ...order,
          orderStatus: newStatus,
          statusHistory: [...order.statusHistory, statusUpdate],
          updatedAt: new Date().toISOString()
        };

        return this.http.put<Order>(`${this.apiUrl}/${orderId}`, updatedOrder).pipe(
          catchError(error => {
            console.error('Error updating order status:', error);
            throw error;
          })
        );
      }),
      catchError(error => {
        console.error('Error in order status update process:', error);
        throw error;
      })
    );
  }

  updatePaymentStatus(orderId: string, paymentStatus: Order['paymentStatus'], paymentDetails?: PaymentDetails): Observable<Order> {
    return this.getOrderById(orderId).pipe(
      switchMap((order: Order | null) => {
        if (!order) {
          throw new Error('Order not found');
        }

        const updatedOrder = {
          ...order,
          paymentStatus: paymentStatus,
          paymentDetails: paymentDetails || order.paymentDetails,
          updatedAt: new Date().toISOString()
        };

        return this.http.put<Order>(`${this.apiUrl}/${orderId}`, updatedOrder).pipe(
          catchError(error => {
            console.error('Error updating payment status:', error);
            throw error;
          })
        );
      }),
      catchError(error => {
        console.error('Error in payment status update process:', error);
        throw error;
      })
    );
  }

  cancelOrder(orderId: string, reason?: string): Observable<Order> {
    return this.updateOrderStatus(orderId, 'cancelled', reason);
  }

  returnOrder(orderId: string, reason?: string): Observable<Order> {
    return this.updateOrderStatus(orderId, 'returned', reason);
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
    const totalAmount = cartItems.reduce((sum, item) => sum + (item.book.price * item.quantity), 0);
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

    // If user is admin, get all orders by status; otherwise get user-specific orders by status
    const isAdmin = currentUser.username && currentUser.username.toLowerCase().includes('admin');
    const url = isAdmin ? `${this.apiUrl}?orderStatus=${status}` : `${this.apiUrl}?userId=${currentUser.id}&orderStatus=${status}`;
    
    return this.http.get<Order[]>(url).pipe(
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

    return this.http.get<Order[]>(`${this.apiUrl}?userId=${currentUser.id}&q=${query}`).pipe(
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
        const completedOrders = orders.filter(order => order.orderStatus === 'delivered').length;
        const pendingOrders = orders.filter(order => ['pending', 'confirmed', 'processing', 'shipped'].includes(order.orderStatus)).length;
        const cancelledOrders = orders.filter(order => order.orderStatus === 'cancelled').length;
        const totalSpent = orders.reduce((sum, order) => sum + order.finalAmount, 0);

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