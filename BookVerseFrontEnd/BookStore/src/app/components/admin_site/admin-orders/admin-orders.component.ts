import { Component, OnInit } from '@angular/core';
import { CommonModule, NgFor, NgIf } from '@angular/common';
import { RouterModule } from '@angular/router';
import { OrderService } from '../../../services/order.service';

import { AdminService } from '../../../services/admin.service';
import { AddressService } from '../../../services/address.service';
import { Order, OrderWithDetails } from '../../../models/order.model';
import { UserModel } from '../../../models/user.model';
import { BookModel } from '../../../models/book.model';
import { AuthService } from '../../../services/auth.service';
import { BookService } from '../../../services/book.service';
import { FormsModule } from '@angular/forms';
import { Address } from '../../../models/address.model';
import { forkJoin, of, firstValueFrom } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';

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
    
    private adminService: AdminService,
    private addressService: AddressService,
    private authService: AuthService,
    private bookService: BookService
  ) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.isLoading = true;
    this.error = null;

    // Use getOrders method which handles admin vs customer logic internally
    this.orderService.getOrders().subscribe({
      next: (orders) => {
        console.log('Raw admin orders received:', orders);
        this.processOrdersWithAddressesAndCustomers(orders);
      },
      error: (error) => {
        console.error('Error loading orders:', error);
        this.error = 'Failed to load orders. Please try again.';
        this.isLoading = false;
      }
    });
  }

  private processOrdersWithAddressesAndCustomers(orders: Order[]): void {
    if (orders.length === 0) {
      this.orders = [];
      this.isLoading = false;
      return;
    }

    console.log(`Processing ${orders.length} admin orders with address and customer fetching...`);
    console.log('Raw orders received:', orders);

    // Create requests to fetch addresses and customer details for each order
    const enrichmentRequests = orders.map((order, index) => {
      console.log(`Processing order ${index + 1}/${orders.length}: ${order.id}`);
      console.log(`Order details:`, {
        id: order.id,
        userId: order.userId,
        shippingAddressId: order.shippingAddressId,
        billingAddressId: order.billingAddressId,
        hasShippingAddress: !!order.shippingAddress,
        customerName: (order as any).customerName,
        customerEmail: (order as any).customerEmail,
        customerPhone: (order as any).customerPhone
      });
      
      const shouldFetchAddress = order.shippingAddressId && !order.shippingAddress;
      const shouldFetchCustomer = order.userId && !(order as any).customerName;
      
      console.log(`Fetch decisions for order ${order.id}:`, {
        shouldFetchAddress,
        shouldFetchCustomer,
        reason: {
          address: shouldFetchAddress ? `Has shippingAddressId: ${order.shippingAddressId}` : 'Already has address or no ID',
          customer: shouldFetchCustomer ? `Has userId: ${order.userId}` : 'Already has customer data or no user ID'
        }
      });
      
      const addressRequest = shouldFetchAddress
        ? (async () => {
            try {
              console.log(`Fetching address for order ${order.id}: ${order.shippingAddressId}`);
              // CORRECT: This calls the admin-specific endpoint
              const address = await firstValueFrom(this.addressService.getAddressByIdForAdmin(order.shippingAddressId!));
              console.log(`Successfully fetched address for order ${order.id}:`, address);
              return address;
            } catch (error: any) {
              console.error(`Error fetching address ${order.shippingAddressId} for order ${order.id}:`, error);
              console.error('Address API Error Details:', {
                status: error.status,
                statusText: error.statusText,
                url: error.url,
                message: error.message
              });
              return this.getDefaultAddress();
            }
          })()
        : (() => {
            console.log(`Order ${order.id} already has address or no address ID`);
            return Promise.resolve(order.shippingAddress || this.getDefaultAddress());
          })();

      const customerRequest = shouldFetchCustomer
        ? (async () => {
            try {
              console.log(`Fetching customer details for order ${order.id}: ${order.userId}`);
              const customer = await firstValueFrom(this.adminService.getCustomerById(order.userId));
              console.log(`Successfully fetched customer for order ${order.id}:`, customer);
              return customer;
            } catch (error: any) {
              console.error(`Error fetching customer ${order.userId} for order ${order.id}:`, error);
              console.error('Customer API Error Details:', {
                status: error.status,
                statusText: error.statusText,
                url: error.url,
                message: error.message
              });
              return {
                id: order.userId,
                fullName: 'Customer Name Not Available',
                email: 'Email Not Available',
                phoneNumber: 'Phone Not Available'
              };
            }
          })()
        : (() => {
            console.log(`Order ${order.id} already has customer details or no user ID`);
            return Promise.resolve({
              id: order.userId,
              fullName: (order as any).customerName || 'Customer Name Not Available',
              email: (order as any).customerEmail || 'Email Not Available',
              phoneNumber: (order as any).customerPhone || 'Phone Not Available'
            });
          })();

      // Combine address and customer requests using Promise.all
      return Promise.all([addressRequest, customerRequest]).then(([address, customer]) => ({
        address,
        customer,
        order
      }));
    });

    // Execute all enrichment requests using Promise.all
    Promise.all(enrichmentRequests)
      .then((results) => {
        console.log(`Successfully processed ${results.length} orders with enriched data`);
        console.log('Enrichment results:', results);
        
        this.orders = results.map(({ order, address, customer }) => {
          const enrichedOrder = {
            ...order,
            items: order.orderItems || order.items || [],
            orderItems: order.orderItems || order.items || [],
            shippingAddress: address,
            customerName: customer.fullName,
            customerEmail: customer.email,
            customerPhone: customer.phoneNumber || address?.phone || 'Phone Not Available',
            // Ensure proper date and amount fields
            orderDate: order.orderDate || order.createdAt || order.placedAt,
            totalAmount: order.finalAmount || order.grandTotal || order.subtotal
          } as OrderWithDetails;
          
          console.log(`Enriched order ${order.id}:`, {
            customerName: enrichedOrder.customerName,
            customerEmail: enrichedOrder.customerEmail,
            customerPhone: enrichedOrder.customerPhone,
            hasAddress: !!enrichedOrder.shippingAddress,
            addressLine1: enrichedOrder.shippingAddress?.addressLine1
          });
          
          return enrichedOrder;
        });

        this.isLoading = false;
        console.log('Admin orders processed successfully with addresses and customer details:', this.orders.length);
      })
      .catch((error) => {
        console.error('Error processing orders with addresses and customers:', error);
        // Fallback: use orders without proper enrichment
        this.orders = orders.map(order => ({
          ...order,
          items: order.orderItems || order.items || [],
          orderItems: order.orderItems || order.items || [],
          shippingAddress: order.shippingAddress || this.getDefaultAddress(),
          customerName: (order as any).customerName || 'Customer Name Not Available',
          customerEmail: (order as any).customerEmail || 'Email Not Available',
          customerPhone: (order as any).customerPhone || order.shippingAddress?.phone || 'Phone Not Available',
          orderDate: order.orderDate || order.createdAt || order.placedAt,
          totalAmount: order.finalAmount || order.grandTotal || order.subtotal
        } as OrderWithDetails));
        this.isLoading = false;
        console.warn('Fallback: Using orders without complete enrichment due to errors');
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