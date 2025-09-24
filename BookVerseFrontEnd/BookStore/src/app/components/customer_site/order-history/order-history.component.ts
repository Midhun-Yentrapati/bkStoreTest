import { Component, OnInit } from '@angular/core';
import { Order } from '../../../models/order.model';
import { OrderService } from '../../../services/order.service';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { OrderUtils } from '../../../shared/utils/order.utils';
import { NotificationService } from '../../../services/notification.service';

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
        this.orders = orders;
        this.isLoading = false;
        console.log('Orders loaded:', this.orders);
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
          this.loadOrders(); // Reload orders to reflect the change
          this.notificationService.success('Success', 'Order cancelled successfully');
        },
        error: (error) => {
          console.error('Error cancelling order:', error);
          this.notificationService.error('Error', 'Failed to cancel order. Please try again.');
        }
      });
    }
  }

  // Use shared utility methods
  getStatusBadgeClass = OrderUtils.getStatusBadgeClass;
  getPaymentStatusBadgeClass = OrderUtils.getPaymentStatusBadgeClass;
  canCancelOrder = OrderUtils.canCancelOrder;
  getOrderStatusText = OrderUtils.getOrderStatusText;
  getPaymentStatusText = OrderUtils.getPaymentStatusText;
  formatOrderDate = OrderUtils.formatOrderDate;
}
