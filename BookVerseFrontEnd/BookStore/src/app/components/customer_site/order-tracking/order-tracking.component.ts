import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { OrderService } from '../../../services/order.service';
import { AddressService } from '../../../services/address.service';
import { AuthService } from '../../../services/auth.service';
import { Order, OrderWithDetails } from '../../../models/order.model';
import { OrderUtils } from '../../../shared/utils/order.utils';
import { NotificationService } from '../../../services/notification.service';
import { Address } from '../../../models/address.model';
import { switchMap, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

@Component({
  selector: 'app-order-tracking',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './order-tracking.component.html',
  styleUrl: './order-tracking.component.css'
})
export class OrderTrackingComponent implements OnInit {
  order: OrderWithDetails | null = null;
  orderId: string | null = null;
  isLoading: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private orderService: OrderService,
    private addressService: AddressService,
    private authService: AuthService,
    private notificationService: NotificationService
  ) { }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.orderId = params.get('id');
      if (this.orderId) {
        this.loadOrderDetails(this.orderId);
      } else {
        console.warn('Order ID not found in route parameters.');
        this.notificationService.error('Error', 'Order ID not found.');
        this.router.navigate(['/orders']);
      }
    });
  }

  loadOrderDetails(id: string): void {
    this.isLoading = true;
    this.orderService.getOrderByIdWithDetails(id).pipe(
      switchMap(order => {
        if (!order) {
          return of(null);
        }

        // If order has shippingAddressId but no shippingAddress, fetch it
        if (order.shippingAddressId && !order.shippingAddress) {
          return this.addressService.getAddressById(order.shippingAddressId).pipe(
            switchMap(address => {
              // Attach the fetched address to the order
              const orderWithAddress = {
                ...order,
                shippingAddress: address
              };
              return of(orderWithAddress);
            }),
            catchError(error => {
              console.error(`Error fetching address ${order.shippingAddressId}:`, error);
              // Return order with fallback address
              const orderWithFallbackAddress = {
                ...order,
                shippingAddress: this.getDefaultAddress()
              };
              return of(orderWithFallbackAddress);
            })
          );
        } else {
          // Order already has address or no address ID, ensure it has a valid address
          const orderWithAddress = {
            ...order,
            shippingAddress: order.shippingAddress || this.getDefaultAddress()
          };
          return of(orderWithAddress);
        }
      }),
      catchError(error => {
        console.error('Error loading order details:', error);
        return of(null);
      })
    ).subscribe({
      next: (order) => {
        if (order) {
          this.order = order;
          console.log('Order details loaded with address:', this.order);
        } else {
          console.warn('Order not found or failed to load.');
          this.notificationService.error('Error', 'Order not found or failed to load.');
          this.router.navigate(['/orders']);
        }
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Unexpected error loading order details:', error);
        this.notificationService.error('Error', 'Failed to load order details. Please try again.');
        this.isLoading = false;
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

  goBack(): void {
    this.router.navigate(['/orders']);
  }

  // Use shared utility methods
  getStatusBadgeClass = OrderUtils.getStatusBadgeClass;
  getPaymentStatusBadgeClass = OrderUtils.getPaymentStatusBadgeClass;
  getOrderStatusText = OrderUtils.getOrderStatusText;
  getPaymentStatusText = OrderUtils.getPaymentStatusText;
  formatOrderDate = OrderUtils.formatOrderDate;
  calculateOrderTotals = OrderUtils.calculateOrderTotals;
}