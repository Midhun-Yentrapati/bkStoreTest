package com.bookverse.CartOrderManagement.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Data
@NoArgsConstructor
public class Coupon {
    
    @Id
    private String id;
    
    @Column(unique = true, nullable = false)
    private String code;
    
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type")
    private DiscountType discountType;
    
    @Column(name = "discount_value", precision = 10, scale = 2)
    private BigDecimal discountValue;
    
    @Column(name = "min_order_amount", precision = 10, scale = 2)
    private BigDecimal minOrderAmount;
    
    @Column(name = "max_discount_amount", precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;
    
    @Column(name = "usage_limit")
    private Integer usageLimit;
    
    @Column(name = "usage_count")
    private Integer usageCount = 0;
    
    @Column(name = "user_limit")
    private Integer userLimit;
    
    @Enumerated(EnumType.STRING)
    private Scope scope;
    
    @Column(name = "applicable_categories", columnDefinition = "JSON")
    private String applicableCategories;
    
    @Column(name = "applicable_books", columnDefinition = "JSON")
    private String applicableBooks;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "valid_from")
    private LocalDateTime validFrom;
    
    @Column(name = "valid_until")
    private LocalDateTime validUntil;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum DiscountType {
        PERCENTAGE, FIXED_AMOUNT, FREE_SHIPPING
    }
    
    public enum Scope {
        ALL, CATEGORY, BOOK, USER_SPECIFIC
    }
} 