export interface Payment {
  id: string;
  orderId: string;
  amount: number;
  currency: string;
  paymentStatus: 'pending' | 'paid' | 'failed' | 'refunded' | 'partially_refunded';
  paymentGatewayId?: string;
  refundedAmount?: number;
  failureReason?: string;
  createdAt: string;
  updatedAt: string;
}

export interface PaymentGatewayResponse {
  transactionId: string;
  gateway: string;
  method: string;
  amount: number;
  currency: string;
  timestamp: string;
  status: 'success' | 'failed' | 'pending';
  gatewayResponse?: any;
}
