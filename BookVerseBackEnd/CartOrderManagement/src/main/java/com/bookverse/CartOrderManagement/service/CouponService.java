package com.bookverse.CartOrderManagement.service;

import com.bookverse.CartOrderManagement.exception.CouponException;
import com.bookverse.CartOrderManagement.model.Coupon;
import com.bookverse.CartOrderManagement.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CouponService {
    
    private final CouponRepository couponRepository;
    
    public Coupon createCoupon(Coupon coupon) {
        coupon.setId(UUID.randomUUID().toString());
        coupon.setUsageCount(0);
        coupon.setCreatedAt(LocalDateTime.now());
        coupon.setUpdatedAt(LocalDateTime.now());
        
        log.info("Creating new coupon with code: {}", coupon.getCode());
        return couponRepository.save(coupon);
    }
    
    public Optional<Coupon> findById(String id) {
        return couponRepository.findById(id);
    }
    
    public Optional<Coupon> findByCode(String code) {
        return couponRepository.findByCode(code);
    }
    
    public List<Coupon> findAllActiveCoupons() {
        return couponRepository.findActiveCoupons(LocalDateTime.now());
    }
    
    public List<Coupon> findAvailableCoupons() {
        return couponRepository.findAvailableCoupons();
    }
    
    public boolean validateCoupon(String code, BigDecimal orderAmount, String userId) {
        Optional<Coupon> couponOpt = couponRepository.findValidCouponByCode(code, LocalDateTime.now());
        
        if (couponOpt.isEmpty()) {
            log.warn("Coupon not found or expired: {}", code);
            return false;
        }
        
        Coupon coupon = couponOpt.get();
        
        // Check minimum order amount
        if (coupon.getMinOrderAmount() != null && orderAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
            log.warn("Order amount {} is less than minimum required {} for coupon {}", 
                    orderAmount, coupon.getMinOrderAmount(), code);
            return false;
        }
        
        // Check usage limit
        if (coupon.getUsageLimit() != null && coupon.getUsageCount() >= coupon.getUsageLimit()) {
            log.warn("Coupon {} has reached usage limit", code);
            return false;
        }
        
        // Check if coupon is active
        if (!coupon.getIsActive()) {
            log.warn("Coupon {} is inactive", code);
            return false;
        }
        
        return true;
    }
    
    public BigDecimal calculateDiscount(String couponCode, BigDecimal orderAmount) {
        Optional<Coupon> couponOpt = findByCode(couponCode);
        
        if (couponOpt.isEmpty() || !validateCoupon(couponCode, orderAmount, null)) {
            return BigDecimal.ZERO;
        }
        
        Coupon coupon = couponOpt.get();
        BigDecimal discount = BigDecimal.ZERO;
        
        switch (coupon.getDiscountType()) {
            case PERCENTAGE:
                discount = orderAmount.multiply(coupon.getDiscountValue()).divide(new BigDecimal("100"));
                break;
            case FIXED_AMOUNT:
                discount = coupon.getDiscountValue();
                break;
            case FREE_SHIPPING:
                // This would be handled separately for shipping costs
                discount = BigDecimal.ZERO;
                break;
        }
        
        // Apply maximum discount limit
        if (coupon.getMaxDiscountAmount() != null && discount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
            discount = coupon.getMaxDiscountAmount();
        }
        
        // Ensure discount doesn't exceed order amount
        if (discount.compareTo(orderAmount) > 0) {
            discount = orderAmount;
        }
        
        return discount;
    }
    
    public void applyCoupon(String couponCode) {
        Optional<Coupon> couponOpt = findByCode(couponCode);
        
        if (couponOpt.isPresent()) {
            Coupon coupon = couponOpt.get();
            coupon.setUsageCount(coupon.getUsageCount() + 1);
            coupon.setUpdatedAt(LocalDateTime.now());
            couponRepository.save(coupon);
            
            log.info("Applied coupon: {}, new usage count: {}", couponCode, coupon.getUsageCount());
        }
    }
    
    public Coupon updateCoupon(String id, Coupon updatedCoupon) {
        Optional<Coupon> existingCouponOpt = findById(id);
        
        if (existingCouponOpt.isPresent()) {
            Coupon existingCoupon = existingCouponOpt.get();
            existingCoupon.setTitle(updatedCoupon.getTitle());
            existingCoupon.setDescription(updatedCoupon.getDescription());
            existingCoupon.setDiscountType(updatedCoupon.getDiscountType());
            existingCoupon.setDiscountValue(updatedCoupon.getDiscountValue());
            existingCoupon.setMinOrderAmount(updatedCoupon.getMinOrderAmount());
            existingCoupon.setMaxDiscountAmount(updatedCoupon.getMaxDiscountAmount());
            existingCoupon.setUsageLimit(updatedCoupon.getUsageLimit());
            existingCoupon.setUserLimit(updatedCoupon.getUserLimit());
            existingCoupon.setScope(updatedCoupon.getScope());
            existingCoupon.setApplicableCategories(updatedCoupon.getApplicableCategories());
            existingCoupon.setApplicableBooks(updatedCoupon.getApplicableBooks());
            existingCoupon.setIsActive(updatedCoupon.getIsActive());
            existingCoupon.setValidFrom(updatedCoupon.getValidFrom());
            existingCoupon.setValidUntil(updatedCoupon.getValidUntil());
            existingCoupon.setUpdatedAt(LocalDateTime.now());
            
            return couponRepository.save(existingCoupon);
        }
        
        throw new CouponException("Coupon not found with id: " + id);
    }
    
    public void deleteCoupon(String id) {
        if (couponRepository.existsById(id)) {
            couponRepository.deleteById(id);
            log.info("Deleted coupon with id: {}", id);
        } else {
            throw new CouponException("Coupon not found with id: " + id);
        }
    }
    
    public void deactivateCoupon(String id) {
        Optional<Coupon> couponOpt = findById(id);
        
        if (couponOpt.isPresent()) {
            Coupon coupon = couponOpt.get();
            coupon.setIsActive(false);
            coupon.setUpdatedAt(LocalDateTime.now());
            couponRepository.save(coupon);
            
            log.info("Deactivated coupon with id: {}", id);
        } else {
            throw new CouponException("Coupon not found with id: " + id);
        }
    }
} 