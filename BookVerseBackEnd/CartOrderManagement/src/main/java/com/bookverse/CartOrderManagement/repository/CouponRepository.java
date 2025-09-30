package com.bookverse.CartOrderManagement.repository;

import com.bookverse.CartOrderManagement.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, String> {
    
    Optional<Coupon> findByCode(String code);
    
    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND c.validFrom <= :now AND c.validUntil >= :now")
    List<Coupon> findActiveCoupons(@Param("now") LocalDateTime now);
    
    @Query("SELECT c FROM Coupon c WHERE c.code = :code AND c.isActive = true AND c.validFrom <= :now AND c.validUntil >= :now")
    Optional<Coupon> findValidCouponByCode(@Param("code") String code, @Param("now") LocalDateTime now);
    
    List<Coupon> findByIsActiveTrue();
    
    List<Coupon> findByCreatedBy(String createdBy);
    
    @Query("SELECT c FROM Coupon c WHERE c.usageCount < c.usageLimit AND c.isActive = true")
    List<Coupon> findAvailableCoupons();
} 