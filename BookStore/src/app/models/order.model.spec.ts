import { Order, OrderItem, OrderWithDetails, PaymentDetails, OrderStatusHistory, OrderSummary } from './order.model';
import { Address } from './address.model';

describe('OrderItem', () => {
  let mockOrderItem: OrderItem;

  beforeEach(() => {
    mockOrderItem = {
      id: '1',
      bookId: 'book1',
      title: 'Test Book',
      author: 'Test Author',
      quantity: 2,
      price: 29.99,
      image_urls: ['image1.jpg'],
      category: 'Fiction',
      addedAt: '2023-12-01T10:00:00Z'
    };
  });

  it('should create a valid OrderItem instance', () => {
    expect(mockOrderItem).toBeTruthy();
    expect(mockOrderItem.id).toBe('1');
    expect(mockOrderItem.quantity).toBe(2);
    expect(mockOrderItem.price).toBe(29.99);
  });

  it('should have all required properties', () => {
    expect(mockOrderItem.id).toBeDefined();
    expect(mockOrderItem.quantity).toBeDefined();
    expect(mockOrderItem.price).toBeDefined();
  });

  it('should have correct data types', () => {
    expect(typeof mockOrderItem.id).toBe('string');
    expect(typeof mockOrderItem.quantity).toBe('number');
    expect(typeof mockOrderItem.price).toBe('number');
  });
});

describe('Order', () => {
  let mockOrder: Order;
  let mockAddress: Address;

  beforeEach(() => {
    mockAddress = {
      id: 'addr1',
      name: 'John Doe',
      phone: '1234567890',
      pincode: '12345',
      address: '123 Test St',
      locality: 'Test Area',
      city: 'Test City',
      state: 'Test State',
      addressType: 'Home'
    };

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
      shippingAddress: mockAddress,
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
  });

  it('should create a valid Order instance', () => {
    expect(mockOrder).toBeTruthy();
    expect(mockOrder.id).toBe('order1');
    expect(mockOrder.userId).toBe('user1');
    expect(mockOrder.orderStatus).toBe('pending');
  });

  it('should have valid order status values', () => {
    const validOrderStatuses = ['pending', 'confirmed', 'processing', 'shipped', 'delivered', 'cancelled', 'returned'];
    expect(validOrderStatuses).toContain(mockOrder.orderStatus);
  });

  it('should have valid payment status values', () => {
    const validPaymentStatuses = ['pending', 'paid', 'failed', 'refunded', 'partially_refunded'];
    expect(validPaymentStatuses).toContain(mockOrder.paymentStatus);
  });

  it('should calculate total correctly', () => {
    const expectedTotal = mockOrder.totalAmount + mockOrder.platformFee + mockOrder.shippingFee + mockOrder.taxes - mockOrder.discount;
    expect(mockOrder.finalAmount).toBe(expectedTotal);
  });
});

describe('PaymentDetails', () => {
  let mockPaymentDetails: PaymentDetails;

  beforeEach(() => {
    mockPaymentDetails = {
      transactionId: 'txn123',
      gateway: 'stripe',
      method: 'credit_card',
      amount: 71.55,
      currency: 'USD',
      timestamp: '2023-12-01T10:00:00Z',
      status: 'success'
    };
  });

  it('should create a valid PaymentDetails instance', () => {
    expect(mockPaymentDetails).toBeTruthy();
    expect(mockPaymentDetails.transactionId).toBe('txn123');
    expect(mockPaymentDetails.status).toBe('success');
  });

  it('should have valid status values', () => {
    const validStatuses = ['success', 'failed', 'pending'];
    expect(validStatuses).toContain(mockPaymentDetails.status);
  });
});

describe('OrderStatusHistory', () => {
  let mockStatusHistory: OrderStatusHistory;

  beforeEach(() => {
    mockStatusHistory = {
      status: 'processing',
      timestamp: '2023-12-01T11:00:00Z',
      note: 'Order is being processed',
      updatedBy: 'admin1'
    };
  });

  it('should create a valid OrderStatusHistory instance', () => {
    expect(mockStatusHistory).toBeTruthy();
    expect(mockStatusHistory.status).toBe('processing');
    expect(mockStatusHistory.timestamp).toBe('2023-12-01T11:00:00Z');
  });
});

describe('OrderSummary', () => {
  let mockOrderSummary: OrderSummary;

  beforeEach(() => {
    mockOrderSummary = {
      totalAmount: 59.98,
      platformFee: 2.99,
      shippingFee: 5.99,
      taxes: 3.59,
      discount: 0,
      finalAmount: 71.55,
      totalPayable: 71.55,
      itemCount: 2
    };
  });

  it('should create a valid OrderSummary instance', () => {
    expect(mockOrderSummary).toBeTruthy();
    expect(mockOrderSummary.itemCount).toBe(2);
    expect(mockOrderSummary.finalAmount).toBe(71.55);
  });

  it('should have correct calculation', () => {
    const expectedTotal = mockOrderSummary.totalAmount + mockOrderSummary.platformFee + 
                         mockOrderSummary.shippingFee + mockOrderSummary.taxes - mockOrderSummary.discount;
    expect(mockOrderSummary.finalAmount).toBe(expectedTotal);
  });
}); 