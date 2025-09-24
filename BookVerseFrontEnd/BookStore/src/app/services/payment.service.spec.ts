import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { PaymentService, PaymentMethod, UPIValidationResult } from './payment.service';
import { PaymentDetails } from '../models/order.model';
import { of } from 'rxjs';

describe('PaymentService', () => {
  let service: PaymentService;
  let httpMock: HttpTestingController;

  const mockPaymentData = {
    method: 'UPI',
    amount: 1000,
    upiId: 'test@bank'
  };

  const mockPaymentResponse: PaymentDetails = {
    transactionId: 'TXN123456789',
    gateway: 'UPI_GATEWAY',
    method: 'UPI',
    amount: 1000,
    currency: 'INR',
    status: 'success',
    timestamp: '2024-01-01T00:00:00Z',
    gatewayResponse: {
      status: 'SUCCESS',
      message: 'Payment processed successfully'
    }
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [PaymentService]
    });
    service = TestBed.inject(PaymentService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getPaymentMethods', () => {
    it('should return enabled payment methods', () => {
      service.getPaymentMethods().subscribe(methods => {
        expect(methods).toBeDefined();
        expect(methods.length).toBeGreaterThan(0);
        expect(methods.every(method => method.enabled)).toBe(true);
      });
    });

    it('should include UPI payment method', () => {
      service.getPaymentMethods().subscribe(methods => {
        const upiMethod = methods.find(method => method.id === 'UPI');
        expect(upiMethod).toBeDefined();
        expect(upiMethod?.name).toBe('UPI');
        expect(upiMethod?.description).toBe('Pay by any UPI app');
      });
    });

    it('should include card payment method', () => {
      service.getPaymentMethods().subscribe(methods => {
        const cardMethod = methods.find(method => method.id === 'CARD');
        expect(cardMethod).toBeDefined();
        expect(cardMethod?.name).toBe('Credit / Debit / ATM Card');
        expect(cardMethod?.description).toBe('Add and secure cards as per RBI guidelines');
      });
    });

    it('should include COD payment method with max amount limit', () => {
      service.getPaymentMethods().subscribe(methods => {
        const codMethod = methods.find(method => method.id === 'COD');
        expect(codMethod).toBeDefined();
        expect(codMethod?.name).toBe('Cash on Delivery');
        expect(codMethod?.maxAmount).toBe(5000);
      });
    });
  });

  describe('getSupportedBanks', () => {
    it('should return list of supported banks', () => {
      service.getSupportedBanks().subscribe(banks => {
        expect(banks).toBeDefined();
        expect(banks.length).toBeGreaterThan(0);
        expect(banks).toContain('State Bank of India');
        expect(banks).toContain('HDFC Bank');
        expect(banks).toContain('ICICI Bank');
      });
    });
  });

  describe('validateUPIId', () => {
    it('should validate correct UPI ID format', () => {
      service.validateUPIId('test@bank').subscribe(result => {
        expect(result.isValid).toBe(true);
        expect(result.provider).toBe('bank');
        expect(result.message).toBe('UPI ID is valid');
      });
    });

    it('should reject invalid UPI ID format', () => {
      service.validateUPIId('invalid-upi').subscribe(result => {
        expect(result.isValid).toBe(false);
        expect(result.message).toBe('Please enter a valid UPI ID (e.g., username@bank)');
      });
    });

    it('should reject UPI ID without @ symbol', () => {
      service.validateUPIId('testbank').subscribe(result => {
        expect(result.isValid).toBe(false);
      });
    });

    it('should reject UPI ID with short provider', () => {
      service.validateUPIId('test@ab').subscribe(result => {
        expect(result.isValid).toBe(false);
      });
    });

    it('should extract provider from valid UPI ID', () => {
      service.validateUPIId('username@hdfc').subscribe(result => {
        expect(result.isValid).toBe(true);
        expect(result.provider).toBe('hdfc');
      });
    });
  });

  describe('validateCardNumber', () => {
    it('should validate correct card numbers', () => {
      // Test with a valid Luhn algorithm number
      expect(service.validateCardNumber('4532015112830366')).toBe(true);
      expect(service.validateCardNumber('4532 0151 1283 0366')).toBe(true); // With spaces
    });

    it('should reject invalid card numbers', () => {
      expect(service.validateCardNumber('4532015112830367')).toBe(false); // Invalid Luhn
      expect(service.validateCardNumber('123')).toBe(false); // Too short
      expect(service.validateCardNumber('12345678901234567890')).toBe(false); // Too long
      expect(service.validateCardNumber('abc123')).toBe(false); // Non-numeric
    });

    it('should handle card numbers with spaces', () => {
      expect(service.validateCardNumber('4532 0151 1283 0366')).toBe(true);
      expect(service.validateCardNumber('  4532015112830366  ')).toBe(true);
    });
  });

  describe('getCardType', () => {
    it('should identify Visa cards', () => {
      expect(service.getCardType('4532015112830366')).toBe('Visa');
      expect(service.getCardType('4000000000000002')).toBe('Visa');
    });

    it('should identify MasterCard', () => {
      expect(service.getCardType('5105105105105100')).toBe('MasterCard');
      expect(service.getCardType('5555555555554444')).toBe('MasterCard');
    });

    it('should identify American Express', () => {
      expect(service.getCardType('378282246310005')).toBe('American Express');
      expect(service.getCardType('371449635398431')).toBe('American Express');
    });

    it('should identify Discover cards', () => {
      expect(service.getCardType('6011111111111117')).toBe('Discover');
      expect(service.getCardType('6011000000000004')).toBe('Discover');
    });

    it('should identify JCB cards', () => {
      expect(service.getCardType('3566002020360505')).toBe('JCB');
    });

    it('should return Unknown for unrecognized patterns', () => {
      expect(service.getCardType('1234567890123456')).toBe('Unknown');
    });

    it('should handle card numbers with spaces', () => {
      expect(service.getCardType('4532 0151 1283 0366')).toBe('Visa');
      expect(service.getCardType('  5105105105105100  ')).toBe('MasterCard');
    });
  });

  describe('processPayment', () => {
    it('should process UPI payment successfully', (done) => {
      service.processPayment({ method: 'UPI', amount: 1000, upiId: 'test@bank' }).subscribe(payment => {
        expect(payment).toBeDefined();
        expect(payment.transactionId).toMatch(/^TXN\d+[A-Z0-9]+$/);
        expect(payment.gateway).toBe('UPI_GATEWAY');
        expect(payment.method).toBe('UPI');
        expect(payment.amount).toBe(1000);
        expect(payment.currency).toBe('INR');
        expect(payment.status).toBe('success');
        expect(payment.gatewayResponse.status).toBe('SUCCESS');
        done();
      });
    });

    it('should process card payment successfully', (done) => {
      service.processPayment({ method: 'CARD', amount: 500, cardNumber: '4532015112830366' }).subscribe(payment => {
        expect(payment).toBeDefined();
        expect(payment.gateway).toBe('CARD_GATEWAY');
        expect(payment.method).toBe('CARD');
        expect(payment.amount).toBe(500);
        expect(payment.status).toBe('success');
        done();
      });
    });

    it('should process net banking payment successfully', (done) => {
      service.processPayment({ method: 'NETBANKING', amount: 750, bank: 'HDFC Bank' }).subscribe(payment => {
        expect(payment).toBeDefined();
        expect(payment.gateway).toBe('NETBANKING_GATEWAY');
        expect(payment.method).toBe('NETBANKING');
        expect(payment.amount).toBe(750);
        expect(payment.status).toBe('success');
        done();
      });
    });

    it('should generate unique transaction IDs', (done) => {
      let transactionIds: string[] = [];
      
      service.processPayment({ method: 'UPI', amount: 100 }).subscribe(payment1 => {
        transactionIds.push(payment1.transactionId);
        
        service.processPayment({ method: 'UPI', amount: 200 }).subscribe(payment2 => {
          transactionIds.push(payment2.transactionId);
          expect(transactionIds[0]).not.toBe(transactionIds[1]);
          done();
        });
      });
    });
  });

  describe('getPaymentDetails', () => {
    it('should get payment details by transaction ID', () => {
      service.getPaymentDetails('TXN123456789').subscribe(payment => {
        expect(payment).toEqual(mockPaymentResponse);
      });

      const req = httpMock.expectOne('http://localhost:3000/payments/TXN123456789');
      expect(req.request.method).toBe('GET');
      req.flush(mockPaymentResponse);
    });

    it('should handle error when payment not found', () => {
      service.getPaymentDetails('TXN999999999').subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        }
      });

      const req = httpMock.expectOne('http://localhost:3000/payments/TXN999999999');
      req.error(new ErrorEvent('Not found'), { status: 404 });
    });
  });

  describe('initiateRefund', () => {
    it('should initiate refund successfully', () => {
      const refundData = {
        amount: 500,
        reason: 'Customer request',
        timestamp: new Date()
      };

      service.initiateRefund('TXN123456789', 500, 'Customer request').subscribe(response => {
        expect(response).toBeTruthy();
      });

      const req = httpMock.expectOne('http://localhost:3000/payments/TXN123456789/refund');
      expect(req.request.method).toBe('POST');
      expect(req.request.body.amount).toBe(500);
      expect(req.request.body.reason).toBe('Customer request');
      req.flush({ success: true, refundId: 'REF123' });
    });

    it('should handle error when initiating refund', () => {
      service.initiateRefund('TXN123456789', 500, 'Customer request').subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        }
      });

      const req = httpMock.expectOne('http://localhost:3000/payments/TXN123456789/refund');
      req.error(new ErrorEvent('Bad Request'), { status: 400 });
    });
  });

  describe('Payment method properties', () => {
    it('should have correct payment method structure', () => {
      service.getPaymentMethods().subscribe(methods => {
        methods.forEach(method => {
          expect(method.id).toBeDefined();
          expect(method.name).toBeDefined();
          expect(method.description).toBeDefined();
          expect(method.enabled).toBe(true);
          expect(typeof method.icon).toBe('string');
        });
      });
    });

    it('should have appropriate icons for payment methods', () => {
      service.getPaymentMethods().subscribe(methods => {
        const upiMethod = methods.find(method => method.id === 'UPI');
        const cardMethod = methods.find(method => method.id === 'CARD');
        const codMethod = methods.find(method => method.id === 'COD');

        expect(upiMethod?.icon).toBe('account_balance_wallet');
        expect(cardMethod?.icon).toBe('credit_card');
        expect(codMethod?.icon).toBe('local_shipping');
      });
    });
  });
}); 