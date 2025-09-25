import { OrderUtils } from './order.utils';
import { Order, OrderWithDetails } from '../../models/order.model';

describe('OrderUtils', () => {
  let mockOrder: Order;
  let mockOrderWithDetails: OrderWithDetails;

  beforeEach(() => {
    mockOrder = {
      id: 'order1',
      userId: 'user1',
      items: [{
        id: 'item1',
        bookId: 'book1',
        title: 'Test Book',
        quantity: 2,
        price: 29.99
      }],
      shippingAddress: {
        id: 'addr1',
        name: 'John Doe',
        phone: '1234567890',
        pincode: '12345',
        address: '123 Test St',
        locality: 'Test Area',
        city: 'Test City',
        state: 'Test State',
        addressType: 'Home'
      },
      orderDate: '2023-12-01T10:00:00Z',
      orderStatus: 'pending',
      paymentStatus: 'pending',
      paymentMethod: 'credit_card',
      totalAmount: 59.98,
      platformFee: 2.99,
      shippingFee: 5.99,
      taxes: 3.59,
      discount: 0,
      finalAmount: 71.55,
      totalPayable: 71.55,
      statusHistory: [{
        status: 'pending',
        timestamp: '2023-12-01T10:00:00Z'
      }],
      createdAt: '2023-12-01T10:00:00Z',
      updatedAt: '2023-12-01T10:00:00Z'
    };

    mockOrderWithDetails = {
      ...mockOrder,
      items: [{
        id: 'item1',
        bookId: 'book1',
        title: 'Test Book',
        quantity: 2,
        price: 29.99
      }]
    };
  });

  describe('getStatusBadgeClass', () => {
    it('should return correct badge class for pending status', () => {
      const result = OrderUtils.getStatusBadgeClass('pending');
      expect(result).toBe('bg-yellow-100 text-yellow-800 border-yellow-200');
    });

    it('should return correct badge class for confirmed status', () => {
      const result = OrderUtils.getStatusBadgeClass('confirmed');
      expect(result).toBe('bg-blue-100 text-blue-800 border-blue-200');
    });

    it('should return correct badge class for processing status', () => {
      const result = OrderUtils.getStatusBadgeClass('processing');
      expect(result).toBe('bg-purple-100 text-purple-800 border-purple-200');
    });

    it('should return correct badge class for shipped status', () => {
      const result = OrderUtils.getStatusBadgeClass('shipped');
      expect(result).toBe('bg-indigo-100 text-indigo-800 border-indigo-200');
    });

    it('should return correct badge class for delivered status', () => {
      const result = OrderUtils.getStatusBadgeClass('delivered');
      expect(result).toBe('bg-green-100 text-green-800 border-green-200');
    });

    it('should return correct badge class for cancelled status', () => {
      const result = OrderUtils.getStatusBadgeClass('cancelled');
      expect(result).toBe('bg-red-100 text-red-800 border-red-200');
    });

    it('should return correct badge class for returned status', () => {
      const result = OrderUtils.getStatusBadgeClass('returned');
      expect(result).toBe('bg-gray-100 text-gray-800 border-gray-200');
    });

    it('should return default badge class for unknown status', () => {
      const result = OrderUtils.getStatusBadgeClass('unknown');
      expect(result).toBe('bg-gray-100 text-gray-800 border-gray-200');
    });

    it('should handle case-insensitive status', () => {
      const result = OrderUtils.getStatusBadgeClass('PENDING');
      expect(result).toBe('bg-yellow-100 text-yellow-800 border-yellow-200');
    });
  });

  describe('getPaymentStatusBadgeClass', () => {
    it('should return correct badge class for pending payment', () => {
      const result = OrderUtils.getPaymentStatusBadgeClass('pending');
      expect(result).toBe('bg-yellow-100 text-yellow-800 border-yellow-200');
    });

    it('should return correct badge class for completed payment', () => {
      const result = OrderUtils.getPaymentStatusBadgeClass('completed');
      expect(result).toBe('bg-green-100 text-green-800 border-green-200');
    });

    it('should return correct badge class for failed payment', () => {
      const result = OrderUtils.getPaymentStatusBadgeClass('failed');
      expect(result).toBe('bg-red-100 text-red-800 border-red-200');
    });

    it('should return correct badge class for refunded payment', () => {
      const result = OrderUtils.getPaymentStatusBadgeClass('refunded');
      expect(result).toBe('bg-blue-100 text-blue-800 border-blue-200');
    });

    it('should return default badge class for unknown payment status', () => {
      const result = OrderUtils.getPaymentStatusBadgeClass('unknown');
      expect(result).toBe('bg-gray-100 text-gray-800 border-gray-200');
    });
  });

  describe('canCancelOrder', () => {
    it('should return true for cancellable statuses', () => {
      const cancellableStatuses = ['pending', 'confirmed', 'processing'] as const;
      
      cancellableStatuses.forEach(status => {
        const order = { ...mockOrder, orderStatus: status };
        expect(OrderUtils.canCancelOrder(order)).toBe(true);
      });
    });

    it('should return false for non-cancellable statuses', () => {
      const nonCancellableStatuses = ['shipped', 'delivered', 'cancelled', 'returned'] as const;
      
      nonCancellableStatuses.forEach(status => {
        const order = { ...mockOrder, orderStatus: status };
        expect(OrderUtils.canCancelOrder(order)).toBe(false);
      });
    });

    it('should handle case-insensitive status', () => {
      const order = { ...mockOrder, orderStatus: 'PENDING' as any };
      expect(OrderUtils.canCancelOrder(order)).toBe(true);
    });
  });

  describe('getOrderStatusText', () => {
    it('should return correct text for each status', () => {
      const statusTexts = {
        'pending': 'Order Pending',
        'confirmed': 'Order Confirmed',
        'processing': 'Processing Order',
        'shipped': 'Order Shipped',
        'delivered': 'Order Delivered',
        'cancelled': 'Order Cancelled',
        'returned': 'Order Returned'
      };

      Object.entries(statusTexts).forEach(([status, expectedText]) => {
        const result = OrderUtils.getOrderStatusText(status);
        expect(result).toBe(expectedText);
      });
    });

    it('should return unknown status for invalid status', () => {
      const result = OrderUtils.getOrderStatusText('invalid');
      expect(result).toBe('Unknown Status');
    });
  });

  describe('getPaymentStatusText', () => {
    it('should return correct text for each payment status', () => {
      const paymentStatusTexts = {
        'pending': 'Payment Pending',
        'completed': 'Payment Completed',
        'failed': 'Payment Failed',
        'refunded': 'Payment Refunded'
      };

      Object.entries(paymentStatusTexts).forEach(([status, expectedText]) => {
        const result = OrderUtils.getPaymentStatusText(status);
        expect(result).toBe(expectedText);
      });
    });

    it('should return unknown status for invalid payment status', () => {
      const result = OrderUtils.getPaymentStatusText('invalid');
      expect(result).toBe('Unknown Payment Status');
    });
  });

  describe('formatOrderDate', () => {
    it('should return "Today" for current date', () => {
      const today = new Date();
      const result = OrderUtils.formatOrderDate(today);
      expect(result).toBe('Today');
    });

    it('should return "Yesterday" for yesterday', () => {
      const yesterday = new Date();
      yesterday.setDate(yesterday.getDate() - 1);
      const result = OrderUtils.formatOrderDate(yesterday);
      expect(result).toBe('Yesterday');
    });

    it('should return "X days ago" for recent dates', () => {
      const threeDaysAgo = new Date();
      threeDaysAgo.setDate(threeDaysAgo.getDate() - 3);
      const result = OrderUtils.formatOrderDate(threeDaysAgo);
      expect(result).toBe('3 days ago');
    });

    it('should return formatted date for older dates', () => {
      const oldDate = new Date('2023-01-15');
      const result = OrderUtils.formatOrderDate(oldDate);
      expect(result).toMatch(/Jan \d+, 2023/);
    });

    it('should handle string dates', () => {
      const dateString = '2023-12-01T10:00:00Z';
      const result = OrderUtils.formatOrderDate(dateString);
      expect(result).toBeTruthy();
    });
  });

  describe('calculateOrderTotals', () => {
    it('should calculate correct totals for order under ₹500', () => {
      const order = {
        ...mockOrderWithDetails,
        items: [{
          id: 'item1',
          bookId: 'book1',
          title: 'Test Book',
          quantity: 1,
          price: 100
        }]
      };

      const result = OrderUtils.calculateOrderTotals(order);
      
      expect(result.subtotal).toBe(100);
      expect(result.tax).toBe(18); // 18% of 100
      expect(result.shipping).toBe(50); // Shipping fee for orders under ₹500
      expect(result.total).toBe(168); // 100 + 18 + 50
    });

    it('should calculate correct totals for order over ₹500 (free shipping)', () => {
      const order = {
        ...mockOrderWithDetails,
        items: [{
          id: 'item1',
          bookId: 'book1',
          title: 'Test Book',
          quantity: 1,
          price: 600
        }]
      };

      const result = OrderUtils.calculateOrderTotals(order);
      
      expect(result.subtotal).toBe(600);
      expect(result.tax).toBe(108); // 18% of 600
      expect(result.shipping).toBe(0); // Free shipping for orders over ₹500
      expect(result.total).toBe(708); // 600 + 108 + 0
    });

    it('should round totals to 2 decimal places', () => {
      const order = {
        ...mockOrderWithDetails,
        items: [{
          id: 'item1',
          bookId: 'book1',
          title: 'Test Book',
          quantity: 1,
          price: 33.33
        }]
      };

      const result = OrderUtils.calculateOrderTotals(order);
      
      expect(result.subtotal).toBe(33.33);
      expect(result.tax).toBe(6); // 18% of 33.33 = 5.9994, rounded to 6
      expect(result.shipping).toBe(50);
      expect(result.total).toBe(89.33);
    });
  });

  describe('getEstimatedDeliveryDate', () => {
    it('should return date 5 days from order date', () => {
      const orderDate = new Date('2023-12-01');
      const result = OrderUtils.getEstimatedDeliveryDate(orderDate);
      
      const expectedDate = new Date('2023-12-06');
      const expectedFormatted = expectedDate.toLocaleDateString('en-US', {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      });
      
      expect(result).toBe(expectedFormatted);
    });

    it('should handle string dates', () => {
      const orderDateString = '2023-12-01T10:00:00Z';
      const result = OrderUtils.getEstimatedDeliveryDate(orderDateString);
      expect(result).toContain('Wednesday, December 6, 2023');
    });
  });

  describe('getOrderProgress', () => {
    it('should return correct progress percentage for each status', () => {
      const statusProgress = {
        'pending': 20,      // 1/5 * 100
        'confirmed': 40,    // 2/5 * 100
        'processing': 60,   // 3/5 * 100
        'shipped': 80,      // 4/5 * 100
        'delivered': 100    // 5/5 * 100
      };

      Object.entries(statusProgress).forEach(([status, expectedProgress]) => {
        const order = { ...mockOrder, orderStatus: status as any };
        const result = OrderUtils.getOrderProgress(order);
        expect(result).toBe(expectedProgress);
      });
    });

    it('should return 0 for unknown statuses', () => {
      const order = { ...mockOrder, orderStatus: 'unknown' as any };
      const result = OrderUtils.getOrderProgress(order);
      expect(result).toBe(0);
    });

    it('should return 0 for cancelled and returned statuses', () => {
      const cancelledOrder = { ...mockOrder, orderStatus: 'cancelled' as any };
      const returnedOrder = { ...mockOrder, orderStatus: 'returned' as any };
      
      expect(OrderUtils.getOrderProgress(cancelledOrder)).toBe(0);
      expect(OrderUtils.getOrderProgress(returnedOrder)).toBe(0);
    });
  });
}); 