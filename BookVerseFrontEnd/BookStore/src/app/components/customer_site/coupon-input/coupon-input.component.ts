import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { CouponService } from '../../../services/coupon.service';
import { AuthService } from '../../../services/auth.service';
import { Coupon, CouponValidationResponse } from '../../../models/coupon.model';
import { UserModel } from '../../../models/user.model';

@Component({
  selector: 'app-coupon-input',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './coupon-input.component.html',
  styleUrl: './coupon-input.component.css'
})
export class CouponInputComponent implements OnInit {
  @Input() orderAmount: number = 0;
  @Output() couponApplied = new EventEmitter<{ coupon: Coupon; discountAmount: number }>();
  @Output() couponRemoved = new EventEmitter<void>();

  couponForm: FormGroup;
  currentUser: UserModel | null = null;
  appliedCoupon: Coupon | null = null;
  availableCoupons: Coupon[] = [];
  
  loading = false;
  validationMessage = '';
  validationSuccess = false;
  showAvailableCoupons = false;

  constructor(
    private fb: FormBuilder,
    private couponService: CouponService,
    private authService: AuthService
  ) {
    this.couponForm = this.fb.group({
      couponCode: ['']
    });
  }

  ngOnInit() {
    this.currentUser = this.authService.getCurrentCustomer();
    this.loadAvailableCoupons();
  }

  loadAvailableCoupons() {
    this.couponService.getActiveCoupons().subscribe({
      next: (coupons) => {
        this.availableCoupons = coupons.filter(c => 
          (c.minimumOrderAmount || 0) <= this.orderAmount && c.isActive
        );
      },
      error: (error) => {
        console.error('Error loading coupons:', error);
      }
    });
  }

  applyCoupon() {
    const couponCode = this.couponForm.value.couponCode?.trim();
    if (!couponCode || !this.currentUser) {
      this.validationMessage = 'Please enter a coupon code';
      this.validationSuccess = false;
      return;
    }

    this.loading = true;
    this.validationMessage = '';

    this.couponService.applyCoupon(couponCode, this.orderAmount)
      .subscribe({
        next: (result: CouponValidationResponse) => {
          this.loading = false;
          this.validationMessage = result.message || '';
          this.validationSuccess = result.valid;

          if (result.valid && result.coupon && result.discountAmount !== undefined) {
            this.appliedCoupon = result.coupon;
            this.couponApplied.emit({
              coupon: result.coupon,
              discountAmount: result.discountAmount
            });
          }
        },
        error: (error) => {
          this.loading = false;
          this.validationMessage = 'Error applying coupon. Please try again.';
          this.validationSuccess = false;
          console.error('Coupon application error:', error);
        }
      });
  }

  removeCoupon() {
    this.appliedCoupon = null;
    this.couponRemoved.emit();
    this.validationMessage = '';
    this.validationSuccess = false;
    this.couponForm.patchValue({ couponCode: '' });
  }

  selectCoupon(coupon: Coupon) {
    this.couponForm.patchValue({ couponCode: coupon.code });
    this.showAvailableCoupons = false;
    this.applyCoupon();
  }

  toggleAvailableCoupons() {
    this.showAvailableCoupons = !this.showAvailableCoupons;
    if (this.showAvailableCoupons) {
      this.loadAvailableCoupons();
    }
  }

  getDiscountText(coupon: Coupon): string {
    if (coupon.discountType === 'PERCENTAGE') {
      return `${coupon.discountValue}% off`;
    } else {
      return `₹${coupon.discountValue} off`;
    }
  }

  isEligibleForCoupon(coupon: Coupon): boolean {
    return this.orderAmount >= (coupon.minimumOrderAmount || 0);
  }
}
