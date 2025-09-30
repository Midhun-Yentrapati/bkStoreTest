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
    return this.http.get<Coupon[]>(this.apiUrl).pipe(
      catchError(error => {
        console.error('Error fetching coupons:', error);
        return of([]);
      })
    );
  }

  getActiveCoupons(): Observable<Coupon[]> {
    return this.http.get<Coupon[]>(`${this.apiUrl}/active`).pipe(
      catchError(error => {
        console.error('Error fetching active coupons:', error);
        return of([]);
      })
    );
  }

  getCouponById(couponId: string): Observable<Coupon | null> {
    return this.http.get<Coupon>(`${this.apiUrl}/${couponId}`).pipe(
      catchError(error => {
        console.error('Error fetching coupon:', error);
        return of(null);
      })
    );
  }

  validateCoupon(couponCode: string, orderAmount: number): Observable<CouponValidationResponse> {
    return this.http.post<CouponValidationResponse>(`${this.apiUrl}/validate`, null, {
      params: {
        code: couponCode,
        orderAmount: orderAmount.toString()
      }
    }).pipe(
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

    return this.http.post<CouponValidationResponse>(`${this.apiUrl}/apply`, null, {
      params: {
        code: couponCode,
        userId: currentUser.id,
        orderAmount: orderAmount.toString()
      }
    }).pipe(
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
    const currentUser = this.authService.getCurrentCustomer();
    if (!currentUser) {
      return throwError(() => new Error('User must be logged in to create coupon'));
    }

    // Only admin users can create coupons
    const isAdmin = currentUser.username && currentUser.username.toLowerCase().includes('admin');
    if (!isAdmin) {
      return throwError(() => new Error('Only admin users can create coupons'));
    }

    return this.http.post<Coupon>(this.apiUrl, couponDto).pipe(
      catchError(error => {
        console.error('Error creating coupon:', error);
        throw error;
      })
    );
  }

  updateCoupon(couponId: string, couponDto: CouponDto): Observable<Coupon> {
    const currentUser = this.authService.getCurrentCustomer();
    if (!currentUser) {
      return throwError(() => new Error('User must be logged in to update coupon'));
    }

    // Only admin users can update coupons
    const isAdmin = currentUser.username && currentUser.username.toLowerCase().includes('admin');
    if (!isAdmin) {
      return throwError(() => new Error('Only admin users can update coupons'));
    }

    return this.http.put<Coupon>(`${this.apiUrl}/${couponId}`, couponDto).pipe(
      catchError(error => {
        console.error('Error updating coupon:', error);
        throw error;
      })
    );
  }

  deleteCoupon(couponId: string): Observable<void> {
    const currentUser = this.authService.getCurrentCustomer();
    if (!currentUser) {
      return throwError(() => new Error('User must be logged in to delete coupon'));
    }

    // Only admin users can delete coupons
    const isAdmin = currentUser.username && currentUser.username.toLowerCase().includes('admin');
    if (!isAdmin) {
      return throwError(() => new Error('Only admin users can delete coupons'));
    }

    return this.http.delete<void>(`${this.apiUrl}/${couponId}`).pipe(
      catchError(error => {
        console.error('Error deleting coupon:', error);
        throw error;
      })
    );
  }
}
