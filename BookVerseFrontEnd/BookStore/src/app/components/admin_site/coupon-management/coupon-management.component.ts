import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CouponService } from '../../../services/coupon.service';
import { Coupon } from '../../../models/coupon.model';

@Component({
  selector: 'app-coupon-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './coupon-management.component.html',
  styleUrl: './coupon-management.component.css'
})
export class CouponManagementComponent implements OnInit {
  coupons: Coupon[] = [];
  filteredCoupons: Coupon[] = [];
  
  showCreateForm = false;
  editingCoupon: Coupon | null = null;
  
  couponForm: FormGroup;
  searchTerm = '';
  filterStatus = 'all';
  loading = false;
  submitLoading = false;

  constructor(
    private couponService: CouponService,
    private fb: FormBuilder
  ) {
    this.couponForm = this.fb.group({
      code: ['', [Validators.required, Validators.pattern(/^[A-Z0-9]{4,15}$/)]],
      title: ['', [Validators.required, Validators.maxLength(100)]],
      description: ['', [Validators.required, Validators.maxLength(500)]],
      discountType: ['percentage', Validators.required],
      discountValue: [0, [Validators.required, Validators.min(1)]],
      minOrderAmount: [0, [Validators.required, Validators.min(0)]],
      maxDiscountAmount: [0, [Validators.min(0)]],
      usageLimit: [100, [Validators.required, Validators.min(1)]],
      userLimit: [1, [Validators.required, Validators.min(1)]],
      scope: ['order', Validators.required],
      validFrom: ['', Validators.required],
      validUntil: ['', Validators.required],
      isActive: [true]
    });
  }

  ngOnInit() {
    this.loadCoupons();
    this.setupFormValidation();
  }

  setupFormValidation() {
    // Update max discount validation based on discount type
    this.couponForm.get('discountType')?.valueChanges.subscribe(type => {
      const maxDiscountControl = this.couponForm.get('maxDiscountAmount');
      if (type === 'percentage') {
        maxDiscountControl?.setValidators([Validators.required, Validators.min(1)]);
      } else {
        maxDiscountControl?.setValidators([Validators.min(0)]);
      }
      maxDiscountControl?.updateValueAndValidity();
    });

    // Validate date range
    this.couponForm.get('validFrom')?.valueChanges.subscribe(() => {
      this.validateDateRange();
    });
    
    this.couponForm.get('validUntil')?.valueChanges.subscribe(() => {
      this.validateDateRange();
    });
  }

  validateDateRange() {
    const validFrom = this.couponForm.get('validFrom')?.value;
    const validUntil = this.couponForm.get('validUntil')?.value;
    
    if (validFrom && validUntil && new Date(validFrom) >= new Date(validUntil)) {
      this.couponForm.get('validUntil')?.setErrors({ 'dateRange': true });
    }
  }

  loadCoupons() {
    this.loading = true;
    this.couponService.getAllCoupons().subscribe({
      next: (coupons) => {
        this.coupons = coupons;
        this.applyFilters();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading coupons:', error);
        this.loading = false;
      }
    });
  }

  applyFilters() {
    let filtered = [...this.coupons];

    // Search filter
    if (this.searchTerm) {
      filtered = filtered.filter(coupon => 
        coupon.code.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        coupon.title.toLowerCase().includes(this.searchTerm.toLowerCase())
      );
    }

    // Status filter
    if (this.filterStatus !== 'all') {
      const now = new Date();
      filtered = filtered.filter(coupon => {
        switch (this.filterStatus) {
          case 'active':
            return coupon.isActive && new Date(coupon.validFrom) <= now && new Date(coupon.validUntil) >= now;
          case 'inactive':
            return !coupon.isActive;
          case 'expired':
            return new Date(coupon.validUntil) < now;
          case 'upcoming':
            return new Date(coupon.validFrom) > now;
          default:
            return true;
        }
      });
    }

    this.filteredCoupons = filtered;
  }

  onSearchChange(term: string) {
    this.searchTerm = term;
    this.applyFilters();
  }

  onFilterChange(status: string) {
    this.filterStatus = status;
    this.applyFilters();
  }

  showCreateCouponForm() {
    this.showCreateForm = true;
    this.editingCoupon = null;
    this.couponForm.reset({
      discountType: 'percentage',
      scope: 'order',
      usageLimit: 100,
      userLimit: 1,
      isActive: true
    });
  }

  editCoupon(coupon: Coupon) {
    this.editingCoupon = coupon;
    this.showCreateForm = true;
    
    this.couponForm.patchValue({
      ...coupon,
      validFrom: new Date(coupon.validFrom).toISOString().split('T')[0],
      validUntil: new Date(coupon.validUntil).toISOString().split('T')[0]
    });
  }

  cancelForm() {
    this.showCreateForm = false;
    this.editingCoupon = null;
    this.couponForm.reset();
  }

  submitCoupon() {
    if (this.couponForm.invalid) return;

    this.submitLoading = true;
    const formData = { ...this.couponForm.value };
    
    // Convert dates to ISO strings
    formData.validFrom = new Date(formData.validFrom).toISOString();
    formData.validUntil = new Date(formData.validUntil).toISOString();

    if (this.editingCoupon) {
      // Update existing coupon
      this.couponService.updateCoupon(this.editingCoupon.id, formData).subscribe({
        next: () => {
          this.submitLoading = false;
          this.cancelForm();
          this.loadCoupons();
          alert('Coupon updated successfully!');
        },
        error: (error) => {
          console.error('Error updating coupon:', error);
          this.submitLoading = false;
          alert('Error updating coupon. Please try again.');
        }
      });
    } else {
      // Create new coupon
      this.couponService.createCoupon(formData).subscribe({
        next: () => {
          this.submitLoading = false;
          this.cancelForm();
          this.loadCoupons();
          alert('Coupon created successfully!');
        },
        error: (error) => {
          console.error('Error creating coupon:', error);
          this.submitLoading = false;
          alert('Error creating coupon. Please try again.');
        }
      });
    }
  }

  deleteCoupon(coupon: Coupon) {
    if (confirm(`Are you sure you want to delete the coupon "${coupon.code}"? This action cannot be undone.`)) {
      this.couponService.deleteCoupon(coupon.id).subscribe({
        next: () => {
          this.loadCoupons();
          alert('Coupon deleted successfully!');
        },
        error: (error) => {
          console.error('Error deleting coupon:', error);
          alert('Error deleting coupon. Please try again.');
        }
      });
    }
  }

  toggleCouponStatus(coupon: Coupon) {
    const newStatus = !coupon.isActive;
    this.couponService.updateCoupon(coupon.id, { isActive: newStatus }).subscribe({
      next: () => {
        coupon.isActive = newStatus;
        this.applyFilters();
        alert(`Coupon ${newStatus ? 'activated' : 'deactivated'} successfully!`);
      },
      error: (error) => {
        console.error('Error updating coupon status:', error);
        alert('Error updating coupon status. Please try again.');
      }
    });
  }

  getCouponStatusClass(coupon: Coupon): string {
    const now = new Date();
    if (!coupon.isActive) return 'badge-secondary';
    if (new Date(coupon.validUntil) < now) return 'badge-danger';
    if (new Date(coupon.validFrom) > now) return 'badge-warning';
    return 'badge-success';
  }

  getCouponStatusText(coupon: Coupon): string {
    const now = new Date();
    if (!coupon.isActive) return 'Inactive';
    if (new Date(coupon.validUntil) < now) return 'Expired';
    if (new Date(coupon.validFrom) > now) return 'Upcoming';
    return 'Active';
  }

  getDiscountText(coupon: Coupon): string {
    if (coupon.discountType === 'percentage') {
      return `${coupon.discountValue}% off`;
    } else {
      return `₹${coupon.discountValue} off`;
    }
  }

  getUsagePercentage(coupon: Coupon): number {
    return (coupon.usageCount / coupon.usageLimit) * 100;
  }
}
