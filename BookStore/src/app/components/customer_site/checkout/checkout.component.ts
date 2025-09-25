import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { CartService, CartItemWithDetails } from '../../../services/cart.service';
import { AddressService } from '../../../services/address.service';
import { OrderService } from '../../../services/order.service';
import { Address } from '../../../models/address.model';
import { OrderSummary } from '../../../models/order.model';
import { NotificationService } from '../../../services/notification.service';
import { AuthService } from '../../../services/auth.service';
import { Subject, takeUntil } from 'rxjs';
import { CouponInputComponent } from '../coupon-input/coupon-input.component';
import { Coupon } from '../../../models/coupon.model';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, CouponInputComponent],
  templateUrl: './checkout.component.html',
  styleUrl: './checkout.component.css'
})
export class CheckoutComponent implements OnInit, OnDestroy {

  cartItems: CartItemWithDetails[] = [];
  addresses: Address[] = [];
  selectedAddress: Address | null = null;
  orderSummary: OrderSummary | null = null;
  currentStep: number = 2; // Start with delivery address since login is handled by auth guard
  isLoading: boolean = false;
  isLoadingAddresses: boolean = false;
  private destroy$ = new Subject<void>();
  
  steps = [
    { number: 1, title: 'LOGIN', status: 'completed' },
    { number: 2, title: 'DELIVERY ADDRESS', status: 'active' },
    { number: 3, title: 'ORDER SUMMARY', status: 'pending' },
    { number: 4, title: 'PAYMENT', status: 'pending' }
  ];

  constructor(
    private cartService: CartService,
    private addressService: AddressService,
    private orderService: OrderService,
    private router: Router,
    private notificationService: NotificationService,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.loadCartItems();
    this.loadAddresses();
    this.loadOrderSummary();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadCartItems(): void {
    this.cartService.cartItemsWithDetails$
      .pipe(takeUntil(this.destroy$))
      .subscribe((items: CartItemWithDetails[]) => {
        this.cartItems = items;
        if (items.length === 0) {
          this.notificationService.warning('Empty Cart', 'Your cart is empty. Please add items to proceed.');
          this.router.navigate(['/cart']);
        }
      });
  }

  loadAddresses(): void {
    this.isLoadingAddresses = true;
    this.addressService.getAddresses().subscribe({
      next: (addresses) => {
        this.addresses = addresses;
        // Select default address if available
        const defaultAddress = addresses.find(addr => addr.isDefault);
        if (defaultAddress) {
          this.selectedAddress = defaultAddress;
        } else if (addresses.length > 0) {
          this.selectedAddress = addresses[0];
        }
        this.isLoadingAddresses = false;
      },
      error: (error) => {
        console.error('Error loading addresses:', error);
        this.notificationService.error('Address Error', 'Failed to load addresses. Please try again.');
        this.isLoadingAddresses = false;
      }
    });
  }

  loadOrderSummary(): void {
    this.orderService.getOrderSummary().subscribe({
      next: (summary) => {
        this.orderSummary = summary;
      },
      error: (error) => {
        console.error('Error loading order summary:', error);
        this.notificationService.error('Summary Error', 'Failed to load order summary.');
      }
    });
  }

  selectAddress(address: Address): void {
    this.selectedAddress = address;
  }

  addNewAddress(): void {
    this.router.navigate(['/address-management']);
  }

  editAddress(address: Address): void {
    this.router.navigate(['/address-management'], { 
      queryParams: { edit: address.id } 
    });
  }

  setAsDefaultAddress(address: Address): void {
    if (address.id) {
      this.addressService.setDefaultAddress(address.id).subscribe({
        next: () => {
          this.notificationService.success('Success', 'Default address updated successfully.');
          this.loadAddresses(); // Reload addresses to reflect changes
        },
        error: () => {
          this.notificationService.error('Error', 'Failed to set default address.');
        }
      });
    }
  }

  deleteAddress(address: Address): void {
    if (address.id && confirm('Are you sure you want to delete this address?')) {
      this.addressService.deleteAddress(address.id).subscribe({
        next: () => {
          this.notificationService.success('Success', 'Address deleted successfully.');
          this.loadAddresses(); // Reload addresses
          if (this.selectedAddress?.id === address.id) {
            this.selectedAddress = null; // Clear selection if deleted address was selected
          }
        },
        error: () => {
          this.notificationService.error('Error', 'Failed to delete address.');
        }
      });
    }
  }

  deliverHere(): void {
    if (!this.selectedAddress) {
      this.notificationService.warning('Address Required', 'Please select a delivery address.');
      return;
    }
    this.currentStep = 3; // Move to order summary
    this.updateStepStatus();
  }

  proceedToPayment(): void {
    if (!this.selectedAddress) {
      this.notificationService.warning('Address Required', 'Please select a delivery address.');
      return;
    }
    this.router.navigate(['/payment'], {
      queryParams: { addressId: this.selectedAddress.id }
    });
  }

  goBack(): void {
    this.router.navigate(['/cart']);
  }

  updateStepStatus(): void {
    this.steps.forEach((step, index) => {
      if (step.number < this.currentStep) {
        step.status = 'completed';
      } else if (step.number === this.currentStep) {
        step.status = 'active';
      } else {
        step.status = 'pending';
      }
    });
  }

  getCurrentUser() {
    return this.authService.getCurrentCustomer();
  }

  getStepClass(step: any): string {
    switch (step.status) {
      case 'completed': return 'step-completed';
      case 'active': return 'step-active';
      default: return 'step-pending';
    }
  }

  canProceedToPayment(): boolean {
    return this.currentStep >= 3 && this.selectedAddress !== null && this.cartItems.length > 0;
  }

  trackByAddressId(index: number, address: Address): string | undefined {
    return address.id;
  }

  trackByItemId(index: number, item: CartItemWithDetails): string {
    return item.id;
  }

  // Coupon handling methods
  onCouponApplied(event: { coupon: Coupon; discountAmount: number }) {
    // Update order summary with discount
    if (this.orderSummary) {
      this.orderSummary.discount = event.discountAmount;
      this.orderSummary.finalAmount = this.orderSummary.totalAmount - event.discountAmount + this.orderSummary.taxes + this.orderSummary.shippingFee;
      this.orderSummary.totalPayable = this.orderSummary.finalAmount;
    }
    
    this.notificationService.success('Coupon Applied', `Coupon ${event.coupon.code} applied successfully! You saved ₹${event.discountAmount}`);
  }

  onCouponRemoved() {
    // Remove discount from order summary
    if (this.orderSummary) {
      this.orderSummary.discount = 0;
      this.orderSummary.finalAmount = this.orderSummary.totalAmount + this.orderSummary.taxes + this.orderSummary.shippingFee;
      this.orderSummary.totalPayable = this.orderSummary.finalAmount;
    }
    
    this.notificationService.info('Coupon Removed', 'Coupon removed successfully.');
  }
}
