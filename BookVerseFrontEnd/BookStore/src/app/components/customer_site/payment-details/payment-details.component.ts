import { Component, OnInit, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Payment } from '../../../models/payment.model';
import { PaymentService } from '../../../services/payment.service';

@Component({
  selector: 'app-payment-details',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './payment-details.component.html',
  styleUrl: './payment-details.component.css'
})
export class PaymentDetailsComponent implements OnInit {
  @Input() orderId!: string;
  @Input() showRefundOption = false;
  
  payments: Payment[] = [];
  loading = false;
  refundLoading = false;

  constructor(private paymentService: PaymentService) {}

  ngOnInit() {
    if (this.orderId) {
      this.loadPayments();
    }
  }

  loadPayments() {
    this.loading = true;
    this.paymentService.getPaymentsByOrder(this.orderId).subscribe({
      next: (payments) => {
        this.payments = payments;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading payments:', error);
        this.loading = false;
      }
    });
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'paid': return 'text-green-600 bg-green-50';
      case 'pending': return 'text-yellow-600 bg-yellow-50';
      case 'failed': return 'text-red-600 bg-red-50';
      case 'refunded': return 'text-blue-600 bg-blue-50';
      case 'partially_refunded': return 'text-purple-600 bg-purple-50';
      default: return 'text-gray-600 bg-gray-50';
    }
  }

  getStatusIcon(status: string): string {
    switch (status) {
      case 'paid': return '✅';
      case 'pending': return '⏳';
      case 'failed': return '❌';
      case 'refunded': return '🔄';
      case 'partially_refunded': return '🔃';
      default: return '❓';
    }
  }

  requestRefund(paymentId: string, amount?: number) {
    if (confirm('Are you sure you want to request a refund?')) {
      this.refundLoading = true;
      this.paymentService.requestRefund(paymentId, amount).subscribe({
        next: () => {
          this.refundLoading = false;
          this.loadPayments(); // Refresh payments
          alert('Refund requested successfully. You will receive an email confirmation.');
        },
        error: (error) => {
          this.refundLoading = false;
          console.error('Refund error:', error);
          alert('Error processing refund request. Please contact support.');
        }
      });
    }
  }

  getTotalPaid(): number {
    return this.payments
      .filter(p => p.paymentStatus === 'paid')
      .reduce((sum, p) => sum + p.amount, 0);
  }

  getTotalRefunded(): number {
    return this.payments
      .reduce((sum, p) => sum + (p.refundedAmount || 0), 0);
  }

  canRefund(payment: Payment): boolean {
    return payment.paymentStatus === 'paid' && 
           (payment.refundedAmount || 0) < payment.amount;
  }

  getRefundableAmount(payment: Payment): number {
    return payment.amount - (payment.refundedAmount || 0);
  }
}
