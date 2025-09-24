import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { Coupon, CouponValidationResult } from '../models/coupon.model';

@Injectable({
  providedIn: 'root'
})
export class CouponService {
  private apiUrl = 'http://localhost:3000/api';
  private appliedCouponSubject = new BehaviorSubject<Coupon | null>(null);
  public appliedCoupon$ = this.appliedCouponSubject.asObservable();

  constructor(private http: HttpClient) {}

  // Get all active coupons for user
  getActiveCoupons(): Observable<Coupon[]> {
    return this.http.get<Coupon[]>(`${this.apiUrl}/coupons/active`);
  }

  // Validate and apply coupon
  validateCoupon(code: string, orderAmount: number, userId: string): Observable<CouponValidationResult> {
    return this.http.post<CouponValidationResult>(`${this.apiUrl}/coupons/validate`, {
      code,
      orderAmount,
      userId
    });
  }

  // Apply coupon (store in service)
  applyCoupon(coupon: Coupon) {
    this.appliedCouponSubject.next(coupon);
  }

  // Remove applied coupon
  removeCoupon() {
    this.appliedCouponSubject.next(null);
  }

  // Get currently applied coupon
  getAppliedCoupon(): Coupon | null {
    return this.appliedCouponSubject.value;
  }

  // Calculate discount amount
  calculateDiscount(coupon: Coupon, orderAmount: number): number {
    if (coupon.discountType === 'percentage') {
      const discount = (orderAmount * coupon.discountValue) / 100;
      return Math.min(discount, coupon.maxDiscountAmount);
    } else {
      return Math.min(coupon.discountValue, orderAmount);
    }
  }

  // Admin: Create new coupon
  createCoupon(coupon: Omit<Coupon, 'id' | 'usageCount' | 'createdAt' | 'updatedAt'>): Observable<Coupon> {
    return this.http.post<Coupon>(`${this.apiUrl}/admin/coupons`, coupon);
  }

  // Admin: Update coupon
  updateCoupon(couponId: string, coupon: Partial<Coupon>): Observable<Coupon> {
    return this.http.put<Coupon>(`${this.apiUrl}/admin/coupons/${couponId}`, coupon);
  }

  // Admin: Delete coupon
  deleteCoupon(couponId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/admin/coupons/${couponId}`);
  }

  // Admin: Get all coupons
  getAllCoupons(): Observable<Coupon[]> {
    return this.http.get<Coupon[]>(`${this.apiUrl}/admin/coupons`);
  }

  // Get coupon usage statistics
  getCouponStats(couponId: string): Observable<{
    totalUsage: number;
    uniqueUsers: number;
    totalDiscountGiven: number;
    recentUsage: any[];
  }> {
    return this.http.get<{
      totalUsage: number;
      uniqueUsers: number;
      totalDiscountGiven: number;
      recentUsage: any[];
    }>(`${this.apiUrl}/admin/coupons/${couponId}/stats`);
  }

  // Check if user has used coupon before
  hasUserUsedCoupon(userId: string, couponId: string): Observable<{
    hasUsed: boolean;
    usageCount: number;
  }> {
    return this.http.get<{
      hasUsed: boolean;
      usageCount: number;
    }>(`${this.apiUrl}/coupons/${couponId}/user-usage/${userId}`);
  }
}
