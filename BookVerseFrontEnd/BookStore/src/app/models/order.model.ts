import { Address } from './address.model';

// OrderItem interface matching backend structure
export interface OrderItem {
  id: string; // Order item ID
  orderId: string; // Order ID reference
  bookId: string; // Book ID reference
  title: string; // Book title
  author: string; // Book author
  price: number; // Price at time of order (BigDecimal from backend)
  quantity: number; // Quantity ordered
  subtotal: number; // Subtotal for this item (BigDecimal from backend)
  imageUrl?: string; // Single image URL
  image_urls?: string[]; // Alternative field name for compatibility
  category?: string; // Book category
  itemStatus?: 'Pending' | 'Packed' | 'Shipped' | 'Delivered' | 'Cancelled'; // Item status
  createdAt: string; // Creation timestamp
}

// OrderItemDto for creating orders
export interface OrderItemDto {
  id?: string;
  orderId?: string;
  bookId: string;
  title: string;
  author: string;
  price: number;
  quantity: number;
  subtotal: number;
  imageUrl?: string;
}

// Extended interface for UI display (with book details)
export interface OrderItemWithDetails extends OrderItem {
  book?: any; // Full book details fetched separately when needed
}

// Order interface matching backend structure exactly
export interface Order {
  id: string;
  userId: string;
  billingAddressId?: string;
  shippingAddressId?: string;
  
  // Pricing breakdown (all BigDecimal from backend)
  subtotal: number;
  discountAmount?: number;
  couponId?: string;
  taxAmount?: number;
  shippingAmount?: number;
  platformFee?: number;
  grandTotal: number;
  currency?: string;
  
  // Payment and status - matching backend enums exactly
  paymentMethod?: 'COD' | 'Card' | 'UPI' | 'NetBanking';
  paymentStatus?: 'Pending' | 'Paid' | 'Failed' | 'Refunded';
  orderStatus?: 'Pending' | 'Confirmed' | 'Shipped' | 'Delivered' | 'Cancelled';
  
  // Delivery tracking
  trackingId?: string;
  estimatedDelivery?: string;
  actualDelivery?: string;
  
  // Additional fields
  notes?: string;
  
  // Timestamps
  createdAt: string;
  updatedAt: string;
  placedAt?: string;
  paidAt?: string;
  shippedAt?: string;
  deliveredAt?: string;
  cancelledAt?: string;
  
  // Relationships
  orderItems?: OrderItem[];
  items?: OrderItem[]; // Alternative field name for compatibility
  statusHistory?: OrderStatusHistory[];
  payments?: Payment[];
  coupon?: any; // Coupon object
  
  // Template compatibility fields (computed/derived)
  shippingAddress?: Address; // Will be populated from shippingAddressId
  billingAddress?: Address; // Will be populated from billingAddressId
  orderDate?: string; // Alias for createdAt or placedAt
  totalAmount?: number; // Alias for subtotal
  finalAmount?: number; // Alias for grandTotal
}

// OrderDto for creating orders
export interface OrderDto {
  id?: string;
  userId: string;
  billingAddressId?: string;
  shippingAddressId?: string;
  subtotal: number;
  discountAmount?: number;
  couponCode?: string;
  taxAmount?: number;
  shippingAmount?: number;
  platformFee?: number;
  grandTotal: number;
  currency?: string;
  paymentMethod?: 'COD' | 'Card' | 'UPI' | 'NetBanking';
  paymentStatus?: 'Pending' | 'Paid' | 'Failed' | 'Refunded';
  orderStatus?: 'Pending' | 'Confirmed' | 'Shipped' | 'Delivered' | 'Cancelled';
  trackingId?: string;
  notes?: string;
  orderItems: OrderItemDto[];
}

// Extended interface for UI display (with book details)
export interface OrderWithDetails extends Omit<Order, 'items' | 'orderItems'> {
  items: OrderItemWithDetails[]; // Order items with book details
  orderItems?: OrderItemWithDetails[]; // Alternative field name for compatibility
}

// Payment interface matching backend structure
export interface Payment {
  id: string;
  orderId: string;
  transactionId?: string;
  amount: number; // BigDecimal from backend
  currency?: string;
  paymentStatus: 'Pending' | 'Paid' | 'Failed' | 'Refunded';
  paymentGateway?: string;
  paymentMethod?: string;
  refundedAmount?: number; // BigDecimal from backend
  failureReason?: string;
  gatewayResponse?: string; // JSON string from backend
  createdAt: string;
  updatedAt: string;
}

export interface PaymentDetails {
  transactionId: string;
  gateway: string;
  method: string;
  amount: number;
  currency: string;
  timestamp: string;
  status: 'success' | 'failed' | 'pending';
  gatewayResponse?: any;
}

export interface OrderStatusHistory {
  status: string;
  timestamp: string;
  note?: string;
  updatedBy?: string;
}

export interface OrderSummary {
  totalAmount: number;
  platformFee: number;
  shippingFee: number;
  taxes: number;
  discount: number;
  finalAmount: number;
  totalPayable: number;
  itemCount: number;
} 