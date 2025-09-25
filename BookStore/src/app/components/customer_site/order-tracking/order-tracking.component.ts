import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { OrderService } from '../../../services/order.service';
import { Order, OrderWithDetails } from '../../../models/order.model';
import { OrderUtils } from '../../../shared/utils/order.utils';
import { NotificationService } from '../../../services/notification.service';

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
    this.orderService.getOrderByIdWithDetails(id).subscribe({
      next: (order) => {
        this.order = order;
        this.isLoading = false;
        console.log('Order details loaded:', this.order);
      },
      error: (error) => {
        console.error('Error loading order details:', error);
        this.notificationService.error('Error', 'Order not found or an error occurred.');
        this.isLoading = false;
        this.router.navigate(['/orders']);
      }
    });
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