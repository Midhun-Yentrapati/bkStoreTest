import { Component, OnInit } from '@angular/core';
import { CommonModule, NgFor, NgIf } from '@angular/common';
import { RouterModule } from '@angular/router';
import { OrderService } from '../../../services/order.service';
import { Order, OrderWithDetails } from '../../../models/order.model';
import { UserModel } from '../../../models/user.model';
import { BookModel } from '../../../models/book.model';
import { AuthService } from '../../../services/auth.service';
import { BookService } from '../../../services/book.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-admin-orders',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, NgIf, NgFor],
  templateUrl: './admin-orders.component.html',
  styleUrls: ['./admin-orders.component.css']
})
export class AdminOrdersComponent implements OnInit {
  orders: OrderWithDetails[] = [];
  isLoading: boolean = true;
  error: string | null = null;
  selectedOrder: OrderWithDetails | null = null;
  showOrderDetails: boolean = false;

  // Filtering and sorting
  statusFilter: string = 'all';
  dateFilter: string = 'all';
  searchQuery: string = '';

  // Status update
  isUpdatingStatus: boolean = false;
  statusUpdateMessage: string = '';
  statusUpdateType: 'success' | 'error' = 'success';

  // Available order statuses
  availableStatuses: Order['orderStatus'][] = [
    'pending',
    'confirmed', 
    'processing',
    'shipped',
    'delivered',
    'cancelled',
    'returned'
  ];

  constructor(
    private orderService: OrderService,
    private authService: AuthService,
    private bookService: BookService
  ) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.isLoading = true;
    this.error = null;

    // Use getAllOrders for admin to see all orders
    this.orderService.getAllOrders().subscribe({
      next: (orders) => {
        // Convert to OrderWithDetails and fetch additional data
        this.orders = orders.map(order => ({
          ...order,
          items: order.items || order.orderItems || []
        }));
        
        this.isLoading = false;
        console.log('Orders loaded:', this.orders);
      },
      error: (error) => {
        console.error('Error loading orders:', error);
        this.error = 'Failed to load orders. Please try again.';
        this.isLoading = false;
      }
    });
  }

  getFilteredOrders(): OrderWithDetails[] {
    let filtered = this.orders;

    // Status filter
    if (this.statusFilter !== 'all') {
      filtered = filtered.filter(order => order.orderStatus === this.statusFilter);
    }

    // Date filter
    if (this.dateFilter !== 'all') {
      const now = new Date();
      const orderDate = new Date();
      
      switch (this.dateFilter) {
        case 'today':
          filtered = filtered.filter(order => {
            orderDate.setTime(Date.parse(order.orderDate));
            return orderDate.toDateString() === now.toDateString();
          });
          break;
        case 'week':
          const weekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
          filtered = filtered.filter(order => {
            orderDate.setTime(Date.parse(order.orderDate));
            return orderDate >= weekAgo;
          });
          break;
        case 'month':
          const monthAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
          filtered = filtered.filter(order => {
            orderDate.setTime(Date.parse(order.orderDate));
            return orderDate >= monthAgo;
          });
          break;
      }
    }

    // Search filter
    if (this.searchQuery.trim()) {
      const query = this.searchQuery.toLowerCase();
      filtered = filtered.filter(order => 
        order.id.toLowerCase().includes(query) ||
        order.shippingAddress.name.toLowerCase().includes(query) ||
        order.items.some(item => 
          item.title?.toLowerCase().includes(query) ||
          item.author?.toLowerCase().includes(query)
        )
      );
    }

    return filtered;
  }

  getStatusBadgeClass(status: string): string {
    const statusClasses: { [key: string]: string } = {
      'pending': 'badge-warning',
      'confirmed': 'badge-info',
      'processing': 'badge-primary',
      'shipped': 'badge-secondary',
      'delivered': 'badge-success',
      'cancelled': 'badge-danger',
      'returned': 'badge-warning'
    };
    return statusClasses[status] || 'badge-secondary';
  }

  getStatusText(status: string): string {
    return status.charAt(0).toUpperCase() + status.slice(1);
  }

  getPaymentStatusBadgeClass(status: string): string {
    const statusClasses: { [key: string]: string } = {
      'pending': 'badge-warning',
      'paid': 'badge-success',
      'failed': 'badge-danger',
      'refunded': 'badge-info',
      'partially_refunded': 'badge-warning'
    };
    return statusClasses[status] || 'badge-secondary';
  }

  viewOrderDetails(order: OrderWithDetails): void {
    this.selectedOrder = order;
    this.showOrderDetails = true;
    // Clear any previous status update messages
    this.statusUpdateMessage = '';
  }

  closeOrderDetails(): void {
    this.showOrderDetails = false;
    this.selectedOrder = null;
    this.statusUpdateMessage = '';
  }

  updateOrderStatus(orderId: string, newStatus: string, note?: string): void {
    if (!orderId || !newStatus) {
      this.showStatusUpdateMessage('Invalid order ID or status', 'error');
      return;
    }

    // Validate that the new status is a valid order status
    if (!this.availableStatuses.includes(newStatus as Order['orderStatus'])) {
      this.showStatusUpdateMessage('Invalid order status', 'error');
      return;
    }

    const validStatus = newStatus as Order['orderStatus'];

    this.isUpdatingStatus = true;
    this.statusUpdateMessage = '';

    // Create a meaningful note if none provided
    const statusNote = note || `Order status updated to ${validStatus} by admin`;

    this.orderService.updateOrderStatus(orderId, validStatus, statusNote).subscribe({
      next: (updatedOrder) => {
        console.log('Order status updated successfully:', updatedOrder);
        
        // Update the order in the local array
        const orderIndex = this.orders.findIndex(order => order.id === orderId);
        if (orderIndex !== -1) {
          this.orders[orderIndex] = {
            ...this.orders[orderIndex],
            orderStatus: validStatus,
            statusHistory: updatedOrder.statusHistory,
            updatedAt: updatedOrder.updatedAt
          };
        }

        // Update selected order if it's the same one
        if (this.selectedOrder && this.selectedOrder.id === orderId) {
          this.selectedOrder = {
            ...this.selectedOrder,
            orderStatus: validStatus,
            statusHistory: updatedOrder.statusHistory,
            updatedAt: updatedOrder.updatedAt
          };
        }

        this.showStatusUpdateMessage(`Order status successfully updated to ${validStatus}`, 'success');
        this.isUpdatingStatus = false;
      },
      error: (error) => {
        console.error('Error updating order status:', error);
        this.showStatusUpdateMessage('Failed to update order status. Please try again.', 'error');
        this.isUpdatingStatus = false;
      }
    });
  }

  private showStatusUpdateMessage(message: string, type: 'success' | 'error'): void {
    this.statusUpdateMessage = message;
    this.statusUpdateType = type;
    
    // Auto-hide success messages after 3 seconds
    if (type === 'success') {
      setTimeout(() => {
        this.statusUpdateMessage = '';
      }, 3000);
    }
  }

  getTotalItems(order: OrderWithDetails): number {
    return order.items.reduce((total, item) => total + item.quantity, 0);
  }

  getOrderTotal(order: OrderWithDetails): number {
    return order.items.reduce((total, item) => total + (item.price * item.quantity), 0);
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getOrderCountByStatus(status: string): number {
    if (status === 'all') return this.orders.length;
    return this.orders.filter(order => order.orderStatus === status).length;
  }

  getTotalRevenue(): number {
    return this.orders
      .filter(order => order.paymentStatus === 'paid')
      .reduce((total, order) => total + this.getOrderTotal(order), 0);
  }

  // Check if status can be updated (prevent updating delivered/cancelled orders)
  canUpdateStatus(order: OrderWithDetails): boolean {
    return !['delivered', 'cancelled'].includes(order.orderStatus);
  }

  // Get next possible statuses based on current status
  getNextPossibleStatuses(currentStatus: Order['orderStatus']): Order['orderStatus'][] {
    const statusFlow: { [key: string]: Order['orderStatus'][] } = {
      'pending': ['confirmed', 'cancelled'],
      'confirmed': ['processing', 'cancelled'],
      'processing': ['shipped', 'cancelled'],
      'shipped': ['delivered', 'returned'],
      'delivered': ['returned'],
      'cancelled': [],
      'returned': []
    };
    
    return statusFlow[currentStatus] || [];
  }
} 