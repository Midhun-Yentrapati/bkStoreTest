import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { PaymentDetails } from '../models/order.model';
import { Payment } from '../models/payment.model';

export interface PaymentMethod {
  id: string;
  name: string;
  description: string;
  icon?: string;
  enabled: boolean;
  minAmount?: number;
  maxAmount?: number;
}

export interface UPIValidationResult {
  isValid: boolean;
  provider?: string;
  message?: string;
}

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private apiBaseUrl = 'http://localhost:8090/api'; // API Gateway URL
  private apiUrl = `${this.apiBaseUrl}/payments`;
  private paymentsApiUrl = `${this.apiBaseUrl}/payments`;

  private paymentMethods: PaymentMethod[] = [
    {
      id: 'UPI',
      name: 'UPI',
      description: 'Pay by any UPI app',
      icon: 'account_balance_wallet',
      enabled: true
    },
    {
      id: 'CARD',
      name: 'Credit / Debit / ATM Card',
      description: 'Add and secure cards as per RBI guidelines',
      icon: 'credit_card',
      enabled: true
    },
    {
      id: 'NETBANKING',
      name: 'Net Banking',
      description: 'Pay using your bank account',
      icon: 'account_balance',
      enabled: true
    },
    {
      id: 'WALLET',
      name: 'Digital Wallet',
      description: 'Pay using digital wallets',
      icon: 'account_balance_wallet',
      enabled: true
    },
    {
      id: 'COD',
      name: 'Cash on Delivery',
      description: 'Pay when you receive your order',
      icon: 'local_shipping',
      enabled: true,
      maxAmount: 5000
    }
  ];

  private supportedBanks = [
    'State Bank of India', 'HDFC Bank', 'ICICI Bank', 'Axis Bank', 'Punjab National Bank',
    'Bank of Baroda', 'Canara Bank', 'Union Bank of India', 'Bank of India', 'Central Bank of India',
    'Yes Bank', 'Kotak Mahindra Bank', 'IndusInd Bank', 'Federal Bank', 'South Indian Bank'
  ];

  constructor(private http: HttpClient) { }

  /**
   * Get available payment methods
   */
  getPaymentMethods(): Observable<PaymentMethod[]> {
    return of(this.paymentMethods.filter(method => method.enabled));
  }

  /**
   * Get supported banks for net banking
   */
  getSupportedBanks(): Observable<string[]> {
    return of(this.supportedBanks);
  }

  /**
   * Validate UPI ID
   */
  validateUPIId(upiId: string): Observable<UPIValidationResult> {
    const upiPattern = /^[a-zA-Z0-9._-]+@[a-zA-Z]{3,}$/;
    
    if (!upiPattern.test(upiId)) {
      return of({
        isValid: false,
        message: 'Please enter a valid UPI ID (e.g., username@bank)'
      });
    }

    // Extract provider from UPI ID
    const provider = upiId.split('@')[1];
    
    return of({
      isValid: true,
      provider: provider,
      message: 'UPI ID is valid'
    });
  }

  /**
   * Validate card number using Luhn algorithm
   */
  validateCardNumber(cardNumber: string): boolean {
    const sanitized = cardNumber.replace(/\s+/g, '');
    
    if (!/^\d{13,19}$/.test(sanitized)) {
      return false;
    }

    let sum = 0;
    let isEven = false;

    for (let i = sanitized.length - 1; i >= 0; i--) {
      let digit = parseInt(sanitized.charAt(i), 10);

      if (isEven) {
        digit *= 2;
        if (digit > 9) {
          digit -= 9;
        }
      }

      sum += digit;
      isEven = !isEven;
    }

    return sum % 10 === 0;
  }

  /**
   * Get card type from card number
   */
  getCardType(cardNumber: string): string {
    const sanitized = cardNumber.replace(/\s+/g, '');
    
    if (/^4/.test(sanitized)) return 'Visa';
    if (/^5[1-5]/.test(sanitized)) return 'MasterCard';
    if (/^3[47]/.test(sanitized)) return 'American Express';
    if (/^6/.test(sanitized)) return 'Discover';
    if (/^35/.test(sanitized)) return 'JCB';
    
    return 'Unknown';
  }

  /**
   * Process payment
   */
  processPayment(paymentData: any): Observable<PaymentDetails> {
    // Simulate payment processing
    return new Observable(observer => {
      setTimeout(() => {
        const paymentDetails: PaymentDetails = {
          transactionId: this.generateTransactionId(),
          gateway: this.getGatewayForMethod(paymentData.method),
          method: paymentData.method,
          amount: paymentData.amount,
          currency: 'INR',
          timestamp: new Date().toISOString(),
          status: 'success',
          gatewayResponse: {
            status: 'SUCCESS',
            message: 'Payment processed successfully'
          }
        };

        observer.next(paymentDetails);
        observer.complete();
      }, 2000);
    });
  }

  /**
   * Generate unique transaction ID
   */
  private generateTransactionId(): string {
    const timestamp = Date.now().toString();
    const random = Math.random().toString(36).substr(2, 9);
    return `TXN${timestamp}${random}`.toUpperCase();
  }

  /**
   * Get payment gateway for method
   */
  private getGatewayForMethod(method: string): string {
    switch (method) {
      case 'UPI': return 'UPI_GATEWAY';
      case 'CARD': return 'CARD_GATEWAY';
      case 'NETBANKING': return 'NETBANKING_GATEWAY';
      case 'WALLET': return 'WALLET_GATEWAY';
      case 'COD': return 'COD_SYSTEM';
      default: return 'UNKNOWN_GATEWAY';
    }
  }

  /**
   * Get payment transaction details
   */
  getPaymentDetails(transactionId: string): Observable<PaymentDetails> {
    return this.http.get<PaymentDetails>(`${this.apiUrl}/${transactionId}`);
  }

  /**
   * Initiate refund
   */
  initiateRefund(transactionId: string, amount: number, reason: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${transactionId}/refund`, {
      amount,
      reason,
      timestamp: new Date()
    });
  }

  /**
   * Get payments by order ID
   */
  getPaymentsByOrder(orderId: string): Observable<Payment[]> {
    return this.http.get<Payment[]>(`${this.paymentsApiUrl}/order/${orderId}`);
  }

  /**
   * Get payment by ID
   */
  getPaymentById(paymentId: string): Observable<Payment> {
    return this.http.get<Payment>(`${this.paymentsApiUrl}/${paymentId}`);
  }

  /**
   * Request refund for payment
   */
  requestRefund(paymentId: string, amount?: number): Observable<Payment> {
    return this.http.post<Payment>(`${this.paymentsApiUrl}/${paymentId}/refund`, {
      amount,
      requestedAt: new Date().toISOString()
    });
  }

  /**
   * Update payment status
   */
  updatePaymentStatus(paymentId: string, status: string, gatewayResponse?: any): Observable<Payment> {
    return this.http.put<Payment>(`${this.paymentsApiUrl}/${paymentId}/status`, {
      paymentStatus: status,
      gatewayResponse,
      updatedAt: new Date().toISOString()
    });
  }

  /**
   * Create new payment record
   */
  createPayment(paymentData: Omit<Payment, 'id' | 'createdAt' | 'updatedAt'>): Observable<Payment> {
    return this.http.post<Payment>(`${this.paymentsApiUrl}`, {
      ...paymentData,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    });
  }
}