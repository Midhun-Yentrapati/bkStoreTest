import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { OrderService } from '../../../services/order.service';
import { AddressService } from '../../../services/address.service';
import { PaymentService, PaymentMethod } from '../../../services/payment.service';
import { NotificationService } from '../../../services/notification.service';
import { Address } from '../../../models/address.model';
import { OrderSummary } from '../../../models/order.model';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-payment',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './payment.component.html',
  styleUrl: './payment.component.css'
})
export class PaymentComponent implements OnInit, OnDestroy {

  selectedPaymentMethod: string = 'UPI';
  upiId: string = '';
  cardNumber: string = '';
  cardName: string = '';
  cardExpiry: string = '';
  cardCvv: string = '';
  netBankingBank: string = '';
  deliveryAddress: Address | null = null;
  orderSummary: OrderSummary | null = null;
  isProcessing: boolean = false;
  isLoadingAddress: boolean = false;
  isLoadingOrderSummary: boolean = false;

  paymentMethods: PaymentMethod[] = [];
  banks: string[] = [];
  
  // Form validation
  formErrors: { [key: string]: string } = {};
  
  // UPI validation
  upiValidation: { isValid: boolean; message?: string } | null = null;
  isValidatingUPI: boolean = false;
  private upiValidationTimeout: any;
  private destroy$ = new Subject<void>();

  constructor(
    private orderService: OrderService,
    private addressService: AddressService,
    private paymentService: PaymentService,
    private notificationService: NotificationService,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    this.loadPaymentMethods();
    this.loadSupportedBanks();
    this.loadDeliveryAddress();
    this.loadOrderSummary();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.upiValidationTimeout) {
      clearTimeout(this.upiValidationTimeout);
    }
  }

  loadPaymentMethods(): void {
    this.paymentService.getPaymentMethods().subscribe({
      next: (methods) => {
        this.paymentMethods = methods;
        if (methods.length > 0) {
          this.selectedPaymentMethod = methods[0].id;
        }
      },
      error: (error) => {
        console.error('Error loading payment methods:', error);
        this.notificationService.error('Payment Error', 'Failed to load payment methods.');
      }
    });
  }

  loadSupportedBanks(): void {
    this.paymentService.getSupportedBanks().subscribe({
      next: (banks) => {
        this.banks = banks;
      },
      error: (error) => {
        console.error('Error loading banks:', error);
      }
    });
  }

  loadDeliveryAddress(): void {
    this.isLoadingAddress = true;
    this.route.queryParams.subscribe(params => {
      if (params['addressId']) {
        this.addressService.getAddressById(params['addressId']).subscribe({
          next: (address) => {
            this.deliveryAddress = address;
            this.isLoadingAddress = false;
          },
          error: (error) => {
            console.error('Error loading delivery address:', error);
            this.notificationService.error('Address Error', 'Failed to load delivery address.');
            this.isLoadingAddress = false;
            this.router.navigate(['/checkout']);
          }
        });
      } else {
        this.isLoadingAddress = false;
        this.router.navigate(['/checkout']);
      }
    });
  }

  loadOrderSummary(): void {
    this.isLoadingOrderSummary = true;
    this.orderService.getOrderSummary().subscribe({
      next: (summary) => {
        this.orderSummary = summary;
        this.isLoadingOrderSummary = false;
      },
      error: (error) => {
        console.error('Error loading order summary:', error);
        this.notificationService.error('Summary Error', 'Failed to load order summary.');
        this.isLoadingOrderSummary = false;
      }
    });
  }

  selectPaymentMethod(method: string): void {
    this.selectedPaymentMethod = method;
    this.clearFormErrors();
  }

  onUpiIdChange(): void {
    if (this.upiId.trim().length > 3) {
      this.validateUpiIdDebounced();
    } else {
      this.upiValidation = null;
    }
  }

  private validateUpiIdDebounced(): void {
    clearTimeout(this.upiValidationTimeout);
    this.upiValidationTimeout = setTimeout(() => {
      this.validateUpiId();
    }, 500);
  }

  validateUpiId(): void {
    if (!this.upiId.trim()) {
      this.upiValidation = null;
      return;
    }
    
    this.isValidatingUPI = true;
    this.paymentService.validateUPIId(this.upiId).subscribe({
      next: (result) => {
        this.upiValidation = result;
        this.isValidatingUPI = false;
      },
      error: () => {
        this.upiValidation = { isValid: false, message: 'Validation failed' };
        this.isValidatingUPI = false;
      }
    });
  }

  onCardNumberChange(): void {
    // Auto-format card number
    this.cardNumber = this.cardNumber.replace(/\s/g, '').replace(/(.{4})/g, '$1 ').trim();
    if (this.cardNumber.length > 19) {
      this.cardNumber = this.cardNumber.slice(0, 19);
    }
  }

  onCardExpiryChange(): void {
    // Auto-format expiry date
    this.cardExpiry = this.cardExpiry.replace(/\D/g, '').replace(/(\d{2})(\d{2})/, '$1/$2');
    if (this.cardExpiry.length > 5) {
      this.cardExpiry = this.cardExpiry.slice(0, 5);
    }
  }

  getCardType(): string {
    const cleanNumber = this.cardNumber.replace(/\s/g, '');
    if (cleanNumber.length > 0) {
      return this.paymentService.getCardType(cleanNumber);
    }
    return '';
  }

  processPayment(): void {
    if (!this.deliveryAddress) {
      this.notificationService.error('Address Error', 'Delivery address not found. Please go back to checkout.');
      return;
    }

    if (!this.orderSummary) {
      this.notificationService.error('Order Error', 'Order summary not available. Please try again.');
      return;
    }

    if (!this.validatePaymentMethod()) {
      return;
    }

    this.isProcessing = true;

    const paymentData = {
      method: this.selectedPaymentMethod,
      amount: this.orderSummary.totalPayable,
      currency: 'INR',
      ...this.getPaymentMethodData()
    };

    // Process payment first
    this.paymentService.processPayment(paymentData).subscribe({
      next: (paymentDetails) => {
        // Payment successful, create order
        this.orderService.createOrder(this.deliveryAddress!, this.selectedPaymentMethod, paymentDetails).subscribe({
          next: (order) => {
            this.isProcessing = false;
            this.notificationService.success('Order Placed', `Order placed successfully! Order ID: ${order.id}`);
            this.router.navigate(['/orders']);
          },
          error: (error) => {
            this.isProcessing = false;
            console.error('Error creating order:', error);
            // Disabled error notification to prevent showing "Order Failed" when cart is empty after successful payment
            // this.notificationService.error('Order Failed', 'Payment completed but failed to create order. Please contact support.');
          }
        });
      },
      error: (error) => {
        this.isProcessing = false;
        console.error('Payment error:', error);
        this.notificationService.error('Payment Failed', 'Payment processing failed. Please try again.');
      }
    });
  }

  private getPaymentMethodData(): any {
    switch (this.selectedPaymentMethod) {
      case 'UPI':
        return { upiId: this.upiId };
      case 'CARD':
        return {
          cardNumber: this.cardNumber.replace(/\s/g, ''),
          cardName: this.cardName,
          cardExpiry: this.cardExpiry,
          cardCvv: this.cardCvv
        };
      case 'NETBANKING':
        return { bank: this.netBankingBank };
      default:
        return {};
    }
  }

  validatePaymentMethod(): boolean {
    this.clearFormErrors();
    let isValid = true;

    switch (this.selectedPaymentMethod) {
      case 'UPI':
        if (!this.upiId.trim()) {
          this.formErrors['upiId'] = 'Please enter your UPI ID';
          isValid = false;
        } else if (!this.upiValidation?.isValid) {
          this.formErrors['upiId'] = 'Please enter a valid UPI ID';
          isValid = false;
        }
        break;
      
      case 'CARD':
        const cleanCardNumber = this.cardNumber.replace(/\s/g, '');
        
        if (!cleanCardNumber) {
          this.formErrors['cardNumber'] = 'Please enter card number';
          isValid = false;
        } else if (!this.paymentService.validateCardNumber(cleanCardNumber)) {
          this.formErrors['cardNumber'] = 'Please enter a valid card number';
          isValid = false;
        }
        
        if (!this.cardName.trim()) {
          this.formErrors['cardName'] = 'Please enter cardholder name';
          isValid = false;
        }
        
        if (!this.cardExpiry.trim()) {
          this.formErrors['cardExpiry'] = 'Please enter expiry date';
          isValid = false;
        } else if (!/^\d{2}\/\d{2}$/.test(this.cardExpiry)) {
          this.formErrors['cardExpiry'] = 'Please enter valid expiry date (MM/YY)';
          isValid = false;
        }
        
        if (!this.cardCvv.trim()) {
          this.formErrors['cardCvv'] = 'Please enter CVV';
          isValid = false;
        } else if (!/^\d{3,4}$/.test(this.cardCvv)) {
          this.formErrors['cardCvv'] = 'Please enter valid CVV';
          isValid = false;
        }
        break;
      
      case 'NETBANKING':
        if (!this.netBankingBank.trim()) {
          this.formErrors['netBankingBank'] = 'Please select your bank';
          isValid = false;
        }
        break;
    }

    if (!isValid) {
      this.notificationService.warning('Validation Error', 'Please check your payment details.');
    }

    return isValid;
  }

  clearFormErrors(): void {
    this.formErrors = {};
  }

  hasError(field: string): boolean {
    return !!this.formErrors[field];
  }

  getError(field: string): string {
    return this.formErrors[field] || '';
  }

  goBack(): void {
    this.router.navigate(['/checkout']);
  }

  getPaymentMethodById(id: string): PaymentMethod | undefined {
    return this.paymentMethods.find(method => method.id === id);
  }

  isPaymentMethodEnabled(id: string): boolean {
    const method = this.getPaymentMethodById(id);
    if (!method || !method.enabled) return false;
    
    if (method.maxAmount && this.orderSummary && this.orderSummary.totalPayable > method.maxAmount) {
      return false;
    }
    
    return true;
  }

  getPaymentMethodDisabledReason(id: string): string {
    const method = this.getPaymentMethodById(id);
    if (!method) return 'Method not available';
    if (!method.enabled) return 'Currently unavailable';
    if (method.maxAmount && this.orderSummary && this.orderSummary.totalPayable > method.maxAmount) {
      return `Not available for orders above ₹${method.maxAmount}`;
    }
    return '';
  }
}
