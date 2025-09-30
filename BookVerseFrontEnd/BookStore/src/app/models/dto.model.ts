// DTOs matching backend structure for API communication

export interface CartItemDto {
  id?: string;
  userId: string;
  bookId: string;
  quantity: number;
  priceWhenAdded: number;
}

export interface OrderDto {
  id?: string;
  userId: string;
  billingAddressId?: string;
  shippingAddressId?: string;
  subtotal: number;
  discountAmount?: number;
  couponId?: string;
  taxAmount?: number;
  shippingAmount?: number;
  platformFee?: number;
  grandTotal: number;
  currency?: string;
  paymentMethod?: 'COD' | 'Card' | 'UPI' | 'NetBanking';
  paymentStatus?: 'Pending' | 'Paid' | 'Failed' | 'Refunded';
  orderStatus?: 'Pending' | 'Confirmed' | 'Shipped' | 'Delivered' | 'Cancelled';
  trackingId?: string;
  estimatedDelivery?: string;
  actualDelivery?: string;
  notes?: string;
  orderItems: OrderItemDto[];
}

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
  itemStatus?: 'Pending' | 'Packed' | 'Shipped' | 'Delivered' | 'Cancelled';
}

export interface PaymentDto {
  id?: string;
  orderId: string;
  transactionId?: string;
  amount: number;
  currency?: string;
  paymentStatus: 'Pending' | 'Paid' | 'Failed' | 'Refunded';
  paymentGateway?: string;
  paymentMethod?: string;
  refundedAmount?: number;
  failureReason?: string;
  gatewayResponse?: string;
}

export interface WishlistItemDto {
  id?: string;
  userId: string;
  bookId: string;
  priceWhenAdded?: number;
  notifyOnSale?: boolean;
}

// Response DTOs
export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  message?: string;
  error?: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
} 