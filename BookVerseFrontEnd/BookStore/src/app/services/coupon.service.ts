import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { Coupon, CouponDto, CouponValidationResponse } from '../models/coupon.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class CouponService {
  private apiBaseUrl = 'http://localhost:8090/api'; // API Gateway URL
  private apiUrl = `${this.apiBaseUrl}/coupons`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  getAllCoupons(): Observable<Coupon[]> {
    return this.http.get<Coupon[]>(`${this.apiUrl}/active`).pipe(
      map(coupons => coupons.map(coupon => this.transformCoupon(coupon))),
      catchError(error => {
        console.error('Error fetching coupons:', error);
        return of([]);
      })
    );
  }

  getActiveCoupons(): Observable<Coupon[]> {
    return this.http.get<Coupon[]>(`${this.apiUrl}/active`).pipe(
      map(coupons => coupons.map(coupon => this.transformCoupon(coupon))),
      catchError(error => {
        console.error('Error fetching active coupons:', error);
        return of([]);
      })
    );
  }

  getCouponById(couponId: string): Observable<Coupon | null> {
    return this.http.get<Coupon>(`${this.apiUrl}/${couponId}`).pipe(
      map(coupon => this.transformCoupon(coupon)),
      catchError(error => {
        console.error('Error fetching coupon:', error);
        return of(null);
      })
    );
  }

  validateCoupon(couponCode: string, orderAmount: number): Observable<CouponValidationResponse> {
    return this.http.post<any>(`${this.apiUrl}/validate`, null, {
      params: {
        code: couponCode,
        orderAmount: orderAmount.toString()
      }
    }).pipe(
      map(response => ({
        valid: response.valid || false,
        discountAmount: response.discount || 0,
        message: response.valid ? 'Coupon is valid' : 'Invalid coupon'
      })),
      catchError(error => {
        console.error('Error validating coupon:', error);
        return of({
          valid: false,
          message: 'Error validating coupon. Please try again.'
        });
      })
    );
  }

  applyCoupon(couponCode: string, orderAmount: number): Observable<CouponValidationResponse> {
    const currentUser = this.authService.getCurrentCustomer();
    if (!currentUser) {
      return throwError(() => new Error('User must be logged in to apply coupon'));
    }

    return this.http.post<any>(`${this.apiUrl}/validate`, null, {
      params: {
        code: couponCode,
        orderAmount: orderAmount.toString(),
        userId: currentUser.id
      }
    }).pipe(
      map(response => ({
        valid: response.valid || false,
        discountAmount: response.discount || 0,
        message: response.valid ? 'Coupon applied successfully' : 'Failed to apply coupon'
      })),
      catchError(error => {
        console.error('Error applying coupon:', error);
        return of({
          valid: false,
          message: 'Error applying coupon. Please try again.'
        });
      })
    );
  }

  createCoupon(couponDto: CouponDto): Observable<Coupon> {
    const currentUser = this.authService.getCurrentCustomer() || this.authService.getCurrentAdmin();
    if (!currentUser) {
      return throwError(() => new Error('User must be logged in to create coupon'));
    }

    // Transform frontend DTO to backend format
    const backendCoupon = this.transformToBackend(couponDto);

    return this.http.post<Coupon>(this.apiUrl, backendCoupon).pipe(
      map(coupon => this.transformCoupon(coupon)),
      catchError(error => {
        console.error('Error creating coupon:', error);
        throw error;
      })
    );
  }

  updateCoupon(couponId: string, couponDto: CouponDto): Observable<Coupon> {
    const currentUser = this.authService.getCurrentCustomer() || this.authService.getCurrentAdmin();
    if (!currentUser) {
      return throwError(() => new Error('User must be logged in to update coupon'));
    }

    // Transform frontend DTO to backend format
    const backendCoupon = this.transformToBackend(couponDto);

    return this.http.put<Coupon>(`${this.apiUrl}/${couponId}`, backendCoupon).pipe(
      map(coupon => this.transformCoupon(coupon)),
      catchError(error => {
        console.error('Error updating coupon:', error);
        throw error;
      })
    );
  }

  deleteCoupon(couponId: string): Observable<void> {
    const currentUser = this.authService.getCurrentCustomer() || this.authService.getCurrentAdmin();
    if (!currentUser) {
      return throwError(() => new Error('User must be logged in to delete coupon'));
    }

    return this.http.delete<void>(`${this.apiUrl}/${couponId}`).pipe(
      catchError(error => {
        console.error('Error deleting coupon:', error);
        throw error;
      })
    );
  }

  private transformCoupon(backendCoupon: any): Coupon {
    return {
      id: backendCoupon.id,
      code: backendCoupon.code,
      title: backendCoupon.title,
      description: backendCoupon.description,
      discountType: backendCoupon.discountType === 'PERCENTAGE' ? 'PERCENTAGE' : 'FIXED_AMOUNT',
      discountValue: backendCoupon.discountValue,
      minimumOrderAmount: backendCoupon.minOrderAmount,
      maximumDiscountAmount: backendCoupon.maxDiscountAmount,
      validFrom: backendCoupon.validFrom,
      validUntil: backendCoupon.validUntil,
      usageLimit: backendCoupon.usageLimit,
      usedCount: backendCoupon.usageCount || 0,
      isActive: backendCoupon.isActive,
      createdAt: backendCoupon.createdAt,
      updatedAt: backendCoupon.updatedAt,
      // Aliases for template compatibility
      minOrderAmount: backendCoupon.minOrderAmount,
      maxDiscountAmount: backendCoupon.maxDiscountAmount,
      usageCount: backendCoupon.usageCount || 0
    };
  }

  private transformToBackend(frontendDto: CouponDto): any {
    return {
      code: frontendDto.code,
      title: frontendDto.title,
      description: frontendDto.description,
      discountType: frontendDto.discountType,
      discountValue: frontendDto.discountValue,
      minOrderAmount: frontendDto.minimumOrderAmount,
      maxDiscountAmount: frontendDto.maximumDiscountAmount,
      validFrom: frontendDto.validFrom,
      validUntil: frontendDto.validUntil,
      usageLimit: frontendDto.usageLimit,
      userLimit: 1, // Default user limit
      scope: 'ALL', // Default scope
      isActive: frontendDto.isActive
    };
  }
}

