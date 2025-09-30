// Coupon interface matching backend structure
export interface Coupon {
  id: string;
  code: string;
  title?: string; // Template compatibility field
  description?: string;
  discountType: 'PERCENTAGE' | 'FIXED_AMOUNT';
  discountValue: number; // BigDecimal from backend
  minimumOrderAmount?: number; // BigDecimal from backend
  maximumDiscountAmount?: number; // BigDecimal from backend
  validFrom: string; // LocalDateTime from backend
  validUntil: string; // LocalDateTime from backend
  usageLimit?: number;
  usedCount: number;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
  
  // Template compatibility fields (aliases)
  minOrderAmount?: number; // Alias for minimumOrderAmount
  maxDiscountAmount?: number; // Alias for maximumDiscountAmount
  usageCount?: number; // Alias for usedCount
}

// DTO for creating/updating coupons
export interface CouponDto {
  id?: string;
  code: string;
  title?: string;
  description?: string;
  discountType: 'PERCENTAGE' | 'FIXED_AMOUNT';
  discountValue: number;
  minimumOrderAmount?: number;
  maximumDiscountAmount?: number;
  validFrom: string;
  validUntil: string;
  usageLimit?: number;
  isActive: boolean;
}

// Response interface for coupon validation
export interface CouponValidationResponse {
  valid: boolean;
  coupon?: Coupon;
  discountAmount?: number;
  message?: string;
}
