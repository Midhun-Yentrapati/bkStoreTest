package com.bookverse.CartOrderManagement.dto;

import com.bookverse.CartOrderManagement.model.Coupon;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponDto {
    
    private String id;
    
    @NotBlank(message = "Coupon code is required")
    @Size(min = 3, max = 50, message = "Coupon code must be between 3 and 50 characters")
    private String code;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    @NotNull(message = "Discount type is required")
    private Coupon.DiscountType discountType;
    
    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Discount value must be greater than 0")
    private BigDecimal discountValue;
    
    @DecimalMin(value = "0.0", message = "Minimum order amount must be positive")
    private BigDecimal minOrderAmount;
    
    @DecimalMin(value = "0.0", message = "Maximum discount amount must be positive")
    private BigDecimal maxDiscountAmount;
    
    @Min(value = 1, message = "Usage limit must be at least 1")
    private Integer usageLimit;
    
    private Integer usageCount;
    
    @Min(value = 1, message = "User limit must be at least 1")
    private Integer userLimit;
    
    @NotNull(message = "Scope is required")
    private Coupon.Scope scope;
    
    private String applicableCategories;
    
    private String applicableBooks;
    
    private Boolean isActive;
    
    @NotNull(message = "Valid from date is required")
    private LocalDateTime validFrom;
    
    @NotNull(message = "Valid until date is required")
    private LocalDateTime validUntil;
    
    private String createdBy;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    // Validation method to ensure validUntil is after validFrom
    @AssertTrue(message = "Valid until date must be after valid from date")
    public boolean isValidDateRange() {
        if (validFrom == null || validUntil == null) {
            return true; // Let other validations handle null values
        }
        return validUntil.isAfter(validFrom);
    }
    
    // Validation method for percentage discount
    @AssertTrue(message = "Percentage discount must be between 1 and 100")
    public boolean isValidPercentageDiscount() {
        if (discountType == null || discountType != Coupon.DiscountType.PERCENTAGE) {
            return true;
        }
        if (discountValue == null) {
            return true; // Let other validations handle null values
        }
        return discountValue.compareTo(BigDecimal.ONE) >= 0 && 
               discountValue.compareTo(new BigDecimal("100")) <= 0;
    }
} 