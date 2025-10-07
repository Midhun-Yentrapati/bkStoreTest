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
    'Pending',
    'Confirmed', 
    'Shipped',
    'Delivered',
    'Cancelled'
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

    // Use getAllOrders for admin to see all orders with customer details
    this.orderService.getAllOrders().subscribe({
      next: (orders) => {
        console.log('Raw admin orders received:', orders);
        
        // Convert to OrderWithDetails and ensure customer data is available
        this.orders = orders.map(order => ({
          ...order,
          items: order.orderItems || order.items || [],
          orderItems: order.orderItems || order.items || [],
          // Ensure customer data is available
          customerName: (order as any).customerName || 'Customer Name Not Available',
          customerEmail: (order as any).customerEmail || 'Email Not Available',
          customerPhone: (order as any).customerPhone || order.shippingAddress?.phone || 'Phone Not Available'
        }));
        
        this.isLoading = false;
        console.log('Processed admin orders:', this.orders);
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
            const dateToCheck = order.placedAt || order.createdAt;
            if (dateToCheck) {
              orderDate.setTime(Date.parse(dateToCheck));
              return orderDate.toDateString() === now.toDateString();
            }
            return false;
          });
          break;
        case 'week':
          const weekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
          filtered = filtered.filter(order => {
            const dateToCheck = order.placedAt || order.createdAt;
            if (dateToCheck) {
              orderDate.setTime(Date.parse(dateToCheck));
              return orderDate >= weekAgo;
            }
            return false;
          });
          break;
        case 'month':
          const monthAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
          filtered = filtered.filter(order => {
            const dateToCheck = order.placedAt || order.createdAt;
            if (dateToCheck) {
              orderDate.setTime(Date.parse(dateToCheck));
              return orderDate >= monthAgo;
            }
            return false;
          });
          break;
      }
    }

    // Search filter
    if (this.searchQuery.trim()) {
      const query = this.searchQuery.toLowerCase();
      filtered = filtered.filter(order => 
        order.id.toLowerCase().includes(query) ||
        (order.customerName?.toLowerCase().includes(query)) ||
        (order.customerEmail?.toLowerCase().includes(query)) ||
        (order.orderItems || order.items || []).some(item => 
          item.title?.toLowerCase().includes(query) ||
          item.author?.toLowerCase().includes(query)
        )
      );
    }

    return filtered;
  }

  getStatusBadgeClass(status: string): string {
    const statusClasses: { [key: string]: string } = {
      'Pending': 'bg-yellow-100 text-yellow-800',
      'Confirmed': 'bg-blue-100 text-blue-800',
      'Shipped': 'bg-purple-100 text-purple-800',
      'Delivered': 'bg-green-100 text-green-800',
      'Cancelled': 'bg-red-100 text-red-800',
      'pending': 'bg-yellow-100 text-yellow-800',
      'confirmed': 'bg-blue-100 text-blue-800',
      'shipped': 'bg-purple-100 text-purple-800',
      'delivered': 'bg-green-100 text-green-800',
      'cancelled': 'bg-red-100 text-red-800'
    };
    return statusClasses[status] || 'bg-gray-100 text-gray-800';
  }

  getStatusText(status: string): string {
    return status.charAt(0).toUpperCase() + status.slice(1);
  }

  getPaymentStatusBadgeClass(status: string): string {
    const statusClasses: { [key: string]: string } = {
      'Pending': 'bg-yellow-100 text-yellow-800',
      'Paid': 'bg-green-100 text-green-800',
      'Failed': 'bg-red-100 text-red-800',
      'Refunded': 'bg-blue-100 text-blue-800',
      'pending': 'bg-yellow-100 text-yellow-800',
      'paid': 'bg-green-100 text-green-800',
      'failed': 'bg-red-100 text-red-800',
      'refunded': 'bg-blue-100 text-blue-800'
    };
    return statusClasses[status] || 'bg-gray-100 text-gray-800';
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
    const items = order.orderItems || order.items || [];
    return items.reduce((total, item) => total + (item.quantity || 0), 0);
  }

  getOrderTotal(order: OrderWithDetails): number {
    // Use grandTotal from backend if available, otherwise calculate from items
    if (order.grandTotal !== undefined && order.grandTotal !== null) {
      return typeof order.grandTotal === 'number' ? order.grandTotal : parseFloat(String(order.grandTotal));
    }
    const items = order.orderItems || order.items || [];
    return items.reduce((total, item) => total + ((item.price || 0) * (item.quantity || 0)), 0);
  }

  formatDate(dateString: string | undefined): string {
    if (!dateString) return 'N/A';
    try {
      return new Date(dateString).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch (error) {
      return 'Invalid Date';
    }
  }

  getOrderCountByStatus(status: string): number {
    if (status === 'all') return this.orders.length;
    return this.orders.filter(order => order.orderStatus === status).length;
  }

  getTotalRevenue(): number {
    return this.orders
      .filter(order => order.paymentStatus === 'Paid')
      .reduce((total, order) => total + this.getOrderTotal(order), 0);
  }

  // Check if status can be updated (prevent updating delivered/cancelled orders)
  canUpdateStatus(order: OrderWithDetails): boolean {
    return !!order.orderStatus && !['Delivered', 'Cancelled'].includes(order.orderStatus);
  }

  // Get next possible statuses based on current status
  getNextPossibleStatuses(currentStatus: Order['orderStatus']): Order['orderStatus'][] {
    if (!currentStatus) return [];
    
    const statusFlow: { [key: string]: Order['orderStatus'][] } = {
      'Pending': ['Confirmed', 'Cancelled'],
      'Confirmed': ['Shipped', 'Cancelled'],
      'Shipped': ['Delivered', 'Cancelled'],
      'Delivered': [],
      'Cancelled': []
    };
    
    return statusFlow[currentStatus] || [];
  }
} 