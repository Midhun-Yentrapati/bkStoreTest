import { Component, OnInit } from '@angular/core';
import { Order } from '../../../models/order.model';
import { OrderService } from '../../../services/order.service';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { OrderUtils } from '../../../shared/utils/order.utils';
import { NotificationService } from '../../../services/notification.service';
import { Address } from '../../../models/address.model';

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
        // Process orders to ensure proper address display
        this.orders = orders.map(order => ({
          ...order,
          // Ensure orderDate is properly set
          orderDate: order.orderDate || order.createdAt || order.placedAt,
          // Ensure totalAmount is properly set
          totalAmount: order.totalAmount || order.subtotal || order.grandTotal,
          // Handle address data - check if addresses are embedded or need to be fetched
          shippingAddress: order.shippingAddress || {
            name: 'Address not available',
            phone: '',
            addressLine1: 'Please contact support',
            city: '',
            state: '',
            pincode: '',
            country: '',
            addressType: 'HOME' as const
          }
        }));
        this.isLoading = false;
        console.log('Orders loaded and processed:', this.orders);
      },
      error: (error) => {
        console.error('Error loading orders:', error);
        this.notificationService.error('Error', 'Failed to load orders. Please try again.');
        this.isLoading = false;
      }
    });
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
