import { Address } from './address.model';

// Unified OrderItem interface for both admin and customer sides
export interface OrderItem {
  id: string; // Order item ID
  bookId?: string | number; // Reference to book (optional if details are embedded)
  title?: string; // Book title for display
  author?: string; // Book author for display
  quantity: number;
  price: number; // Price at time of order (for historical accuracy)
  image_urls?: string[]; // Book images for display
  category?: string; // Book category for display
  addedAt?: string; // ISO timestamp (optional for embedded items)
}

// Extended interface for UI display (with book details)
export interface OrderItemWithDetails extends OrderItem {
  book?: any; // Full book details fetched separately when needed
}

// Unified Order interface for both admin and customer sides
export interface Order {
  id: string;
  userId: string;
  items: OrderItem[]; // Primary field name - used in JSON
  orderItems?: OrderItem[]; // Alternative field name for compatibility
  shippingAddress: Address;
  billingAddress?: Address;
  orderDate: string;
  orderStatus: 'pending' | 'confirmed' | 'processing' | 'shipped' | 'delivered' | 'cancelled' | 'returned';
  paymentStatus: 'pending' | 'paid' | 'failed' | 'refunded' | 'partially_refunded';
  paymentMethod: string;
  paymentDetails?: PaymentDetails;
  
  // Pricing breakdown
  totalAmount: number; // Sum of all item prices
  platformFee: number;
  shippingFee: number;
  taxes: number;
  discount: number;
  finalAmount: number;
  totalPayable: number; // Alias for finalAmount
  
  // Delivery tracking
  estimatedDelivery?: string;
  actualDelivery?: string;
  trackingId?: string;
  
  // Additional fields
  notes?: string;
  statusHistory: OrderStatusHistory[];
  image_urls?: string[]; // Top-level images for order display
  
  createdAt: string;
  updatedAt: string;
}

// Extended interface for UI display (with book details)
export interface OrderWithDetails extends Omit<Order, 'items' | 'orderItems'> {
  items: OrderItemWithDetails[]; // Order items with book details
  orderItems?: OrderItemWithDetails[]; // Alternative field name for compatibility
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