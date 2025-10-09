import { Component, OnInit } from '@angular/core';
import { Order } from '../../../models/order.model';
import { OrderService } from '../../../services/order.service';
import { AddressService } from '../../../services/address.service';
import { AuthService } from '../../../services/auth.service';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { OrderUtils } from '../../../shared/utils/order.utils';
import { NotificationService } from '../../../services/notification.service';
import { Address } from '../../../models/address.model';
import { forkJoin, of } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-order-history',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './order-history.component.html',
  styleUrl: './order-history.component.css'
})
export class OrderHistoryComponent implements OnInit {

  orders: Order[] = [];
  isLoading: boolean = false;

  constructor(
    private orderService: OrderService,
    private addressService: AddressService,
    private authService: AuthService,
    private router: Router,
    private notificationService: NotificationService
  ) { }

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.isLoading = true;
    this.orderService.getOrders().subscribe({
      next: (orders) => {
        // Process orders and fetch addresses for each order
        this.processOrdersWithAddresses(orders);
      },
      error: (error) => {
        console.error('Error loading orders:', error);
        this.notificationService.error('Error', 'Failed to load orders. Please try again.');
        this.isLoading = false;
      }
    });
  }

  private processOrdersWithAddresses(orders: Order[]): void {
    if (orders.length === 0) {
      this.orders = [];
      this.isLoading = false;
      return;
    }

    console.log(`Processing ${orders.length} orders with address fetching...`);
    console.log('Raw customer orders received:', orders);

    // Get current user for address fetching
    const currentUser = this.authService.getCurrentCustomer();
    if (!currentUser) {
      console.error('No current user found');
      this.isLoading = false;
      return;
    }

    // Create requests to fetch addresses for orders that have shipping address IDs
    const addressRequests = orders.map((order, index) => {
      console.log(`Processing customer order ${index + 1}/${orders.length}: ${order.id}`);
      console.log(`Order details:`, {
        id: order.id,
        userId: order.userId,
        shippingAddressId: order.shippingAddressId,
        billingAddressId: order.billingAddressId,
        hasShippingAddress: !!order.shippingAddress
      });
      
      const shouldFetchAddress = order.shippingAddressId && !order.shippingAddress;
      console.log(`Fetch decision for order ${order.id}:`, {
        shouldFetchAddress,
        reason: shouldFetchAddress ? `Has shippingAddressId: ${order.shippingAddressId}` : 'Already has address or no ID'
      });
      
      if (shouldFetchAddress) {
        console.log(`Fetching address for order ${order.id}: ${order.shippingAddressId}`);
        // Fetch the address using the address service
        return this.addressService.getAddressById(order.shippingAddressId!).pipe(
          catchError(error => {
            console.error(`Error fetching address ${order.shippingAddressId} for order ${order.id}:`, error);
            console.error('Address API Error Details:', {
              status: error.status,
              statusText: error.statusText,
              url: error.url,
              message: error.message
            });
            // Return a fallback address if fetch fails
            return of({
              id: order.shippingAddressId,
              name: 'Address not available',
              phone: '',
              addressLine1: 'Unable to fetch address details',
              addressLine2: '',
              city: '',
              state: '',
              pincode: '',
              country: 'India',
              addressType: 'HOME' as const,
              isDefault: false,
              isActive: true
            } as Address);
          })
        );
      } else {
        // Return the existing address or null
        console.log(`Order ${order.id} already has address or no address ID to fetch`);
        return of(order.shippingAddress || null);
      }
    });

    // Execute all address fetch requests
    forkJoin(addressRequests).subscribe({
      next: (addresses) => {
        console.log(`Successfully fetched ${addresses.filter(a => a !== null).length} addresses out of ${addresses.length}`);
        console.log('Address fetch results:', addresses);
        
        // Map the fetched addresses back to orders
        this.orders = orders.map((order, index) => {
          const processedOrder = {
            ...order,
            // Ensure orderDate is properly set
            orderDate: order.orderDate || order.createdAt || order.placedAt,
            // Ensure totalAmount is properly set
            totalAmount: order.finalAmount || order.grandTotal,
            // Set the fetched or existing address
            shippingAddress: addresses[index] || order.shippingAddress || this.getDefaultAddress()
          };

          console.log(`Processed customer order ${order.id}:`, {
            hasAddress: !!processedOrder.shippingAddress,
            addressLine1: processedOrder.shippingAddress?.addressLine1,
            isDefaultAddress: processedOrder.shippingAddress?.addressLine1 === 'Address not available'
          });

          return processedOrder;
        });

        this.isLoading = false;
        console.log('Customer orders loaded and processed with addresses:', this.orders.length);
      },
      error: (error) => {
        console.error('Error processing orders with addresses:', error);
        // Fallback: use orders without proper addresses
        this.orders = orders.map(order => ({
          ...order,
          orderDate: order.orderDate || order.createdAt || order.placedAt,
          totalAmount: order.finalAmount || order.grandTotal,
          shippingAddress: order.shippingAddress || this.getDefaultAddress()
        }));
        this.isLoading = false;
        this.notificationService.warning('Warning', 'Some address details could not be loaded.');
        console.warn('Fallback: Using orders without proper addresses due to errors');
      }
    });
  }

  private getDefaultAddress(): Address {
    return {
      name: 'Address not available',
      phone: '',
      addressLine1: 'Please contact support for address details',
      addressLine2: '',
      city: '',
      state: '',
      pincode: '',
      country: 'India',
      addressType: 'HOME' as const,
      isDefault: false,
      isActive: true
    };
  }

  viewOrderDetails(orderId: string): void {
    this.router.navigate(['/track-order', orderId]);
  }

  cancelOrder(orderId: string): void {
    if (confirm('Are you sure you want to cancel this order?')) {
      this.orderService.cancelOrder(orderId).subscribe({
        next: () => {
          console.log('Order cancelled successfully');
          this.loadOrders();
          this.notificationService.success('Success', 'Order cancelled successfully');
        },
        error: (error) => {
          console.error('Error cancelling order:', error);
          this.notificationService.error('Error', 'Failed to cancel order. Please try again.');
        }
      });
    }
  }

  markPaymentAsPaid(orderId: string): void {
    if (confirm('Confirm that you have paid for this order?')) {
      this.orderService.updatePaymentStatus(orderId, 'Paid').subscribe({
        next: () => {
          this.loadOrders();
          this.notificationService.success('Success', 'Payment status updated successfully');
        },
        error: (error) => {
          console.error('Error updating payment status:', error);
          this.notificationService.error('Error', 'Failed to update payment status. Please try again.');
        }
      });
    }
  }

  canMarkAsPaid(order: Order): boolean {
    return order.paymentMethod === 'COD' && 
           order.paymentStatus === 'Pending' && 
           order.orderStatus === 'Delivered';
  }

  // Use shared utility methods
  getStatusBadgeClass = OrderUtils.getStatusBadgeClass;
  getPaymentStatusBadgeClass = OrderUtils.getPaymentStatusBadgeClass;
  canCancelOrder = OrderUtils.canCancelOrder;
  getOrderStatusText = OrderUtils.getOrderStatusText;
  getPaymentStatusText = OrderUtils.getPaymentStatusText;
  formatOrderDate = OrderUtils.formatOrderDate;
}
