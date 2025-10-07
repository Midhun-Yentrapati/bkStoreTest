import { Order, OrderWithDetails } from '../../models/order.model';

export class OrderUtils {
  
  static getStatusBadgeClass(status: string): string {
    switch (status.toLowerCase()) {
      case 'pending':
        return 'bg-warning text-dark';
      case 'confirmed':
        return 'bg-info text-white';
      case 'processing':
        return 'bg-primary text-white';
      case 'shipped':
        return 'bg-primary text-white';
      case 'delivered':
        return 'bg-success text-white';
      case 'cancelled':
        return 'bg-danger text-white';
      case 'returned':
        return 'bg-secondary text-white';
      default:
        return 'bg-secondary text-white';
    }
  }

  static getPaymentStatusBadgeClass(paymentStatus: string): string {
    switch (paymentStatus.toLowerCase()) {
      case 'pending':
        return 'bg-warning text-dark';
      case 'completed':
      case 'paid':
        return 'bg-success text-white';
      case 'failed':
        return 'bg-danger text-white';
      case 'refunded':
        return 'bg-info text-white';
      default:
        return 'bg-secondary text-white';
    }
  }

  static canCancelOrder(order: Order): boolean {
    const cancellableStatuses = ['pending', 'confirmed', 'processing'];
    return order.orderStatus ? cancellableStatuses.includes(order.orderStatus.toLowerCase()) : false;
  }

  static getOrderStatusText(status: string): string {
    switch (status.toLowerCase()) {
      case 'pending':
        return 'Order Pending';
      case 'confirmed':
        return 'Order Confirmed';
      case 'processing':
        return 'Processing Order';
      case 'shipped':
        return 'Order Shipped';
      case 'delivered':
        return 'Order Delivered';
      case 'cancelled':
        return 'Order Cancelled';
      case 'returned':
        return 'Order Returned';
      default:
        return 'Unknown Status';
    }
  }

  static getPaymentStatusText(paymentStatus: string): string {
    switch (paymentStatus.toLowerCase()) {
      case 'pending':
        return 'Payment Pending';
      case 'completed':
        return 'Payment Completed';
      case 'failed':
        return 'Payment Failed';
      case 'refunded':
        return 'Payment Refunded';
      default:
        return 'Unknown Payment Status';
    }
  }

  static formatOrderDate(date: string | Date): string {
    const orderDate = new Date(date);
    const now = new Date();
    const diffTime = Math.abs(now.getTime() - orderDate.getTime());
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    if (diffDays === 0) {
      return 'Today';
    } else if (diffDays === 1) {
      return 'Yesterday';
    } else if (diffDays < 7) {
      return `${diffDays} days ago`;
    } else {
      return orderDate.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
      });
    }
  }

  static calculateOrderTotals(order: OrderWithDetails): {
    subtotal: number;
    tax: number;
    shipping: number;
    total: number;
  } {
    const subtotal = order.items.reduce((sum: number, item: any) => sum + (item.price * item.quantity), 0);
    const tax = subtotal * 0.18; // 18% GST
    const shipping = subtotal > 500 ? 0 : 50; // Free shipping above ₹500
    const total = subtotal + tax + shipping;

    return {
      subtotal: Math.round(subtotal * 100) / 100,
      tax: Math.round(tax * 100) / 100,
      shipping: Math.round(shipping * 100) / 100,
      total: Math.round(total * 100) / 100
    };
  }

  static getEstimatedDeliveryDate(orderDate: string | Date): string {
    const orderDateObj = new Date(orderDate);
    const estimatedDate = new Date(orderDateObj);
    estimatedDate.setDate(estimatedDate.getDate() + 5); // 5 days delivery

    return estimatedDate.toLocaleDateString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }

  static getOrderProgress(order: Order): number {
    const statusOrder = ['pending', 'confirmed', 'processing', 'shipped', 'delivered'];
    const currentIndex = order.orderStatus ? statusOrder.indexOf(order.orderStatus.toLowerCase()) : -1;
    return currentIndex >= 0 ? ((currentIndex + 1) / statusOrder.length) * 100 : 0;
  }
} 