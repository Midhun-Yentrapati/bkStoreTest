package com.bookverse.CartOrderManagement.controller;

import com.bookverse.CartOrderManagement.dto.CouponDto;
import com.bookverse.CartOrderManagement.model.Coupon;
import com.bookverse.CartOrderManagement.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Coupon Management", description = "APIs for managing discount coupons")
public class CouponController {
    
    private final CouponService couponService;
    
    @PostMapping
    @Operation(summary = "Create a new coupon", description = "Creates a new discount coupon")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Coupon created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid coupon data")
    })
    public ResponseEntity<CouponDto> createCoupon(@Valid @RequestBody CouponDto couponDto) {
        log.info("Creating new coupon with code: [REDACTED]");
        
        Coupon coupon = convertToEntity(couponDto);
        Coupon savedCoupon = couponService.createCoupon(coupon);
        CouponDto responseDto = convertToDto(savedCoupon);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get coupon by ID", description = "Retrieves a coupon by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Coupon found"),
        @ApiResponse(responseCode = "404", description = "Coupon not found")
    })
    public ResponseEntity<CouponDto> getCouponById(@Parameter(description = "Coupon ID") @PathVariable String id) {
        Optional<Coupon> couponOpt = couponService.findById(id);
        
        if (couponOpt.isPresent()) {
            CouponDto couponDto = convertToDto(couponOpt.get());
            return ResponseEntity.ok(couponDto);
        }
        
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/code/{code}")
    @Operation(summary = "Get coupon by code", description = "Retrieves a coupon by its code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Coupon found"),
        @ApiResponse(responseCode = "404", description = "Coupon not found")
    })
    public ResponseEntity<CouponDto> getCouponByCode(@Parameter(description = "Coupon code") @PathVariable String code) {
        Optional<Coupon> couponOpt = couponService.findByCode(code);
        
        if (couponOpt.isPresent()) {
            CouponDto couponDto = convertToDto(couponOpt.get());
            return ResponseEntity.ok(couponDto);
        }
        
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/active")
    @Operation(summary = "Get all active coupons", description = "Retrieves all currently active coupons")
    @ApiResponse(responseCode = "200", description = "Active coupons retrieved successfully")
    public ResponseEntity<List<CouponDto>> getActiveCoupons() {
        List<Coupon> activeCoupons = couponService.findAllActiveCoupons();
        List<CouponDto> couponDtos = activeCoupons.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(couponDtos);
    }
    
    @GetMapping("/available")
    @Operation(summary = "Get available coupons", description = "Retrieves coupons that haven't reached their usage limit")
    @ApiResponse(responseCode = "200", description = "Available coupons retrieved successfully")
    public ResponseEntity<List<CouponDto>> getAvailableCoupons() {
        List<Coupon> availableCoupons = couponService.findAvailableCoupons();
        List<CouponDto> couponDtos = availableCoupons.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(couponDtos);
    }
    
    @PostMapping("/validate")
    @Operation(summary = "Validate coupon", description = "Validates if a coupon can be applied to an order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Validation completed"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<Map<String, Object>> validateCoupon(
            @Parameter(description = "Coupon code") @RequestParam String code,
            @Parameter(description = "Order amount") @RequestParam BigDecimal orderAmount,
            @Parameter(description = "User ID", required = false) @RequestParam(required = false) String userId) {
        
        boolean isValid = couponService.validateCoupon(code, orderAmount, userId);
        BigDecimal discount = isValid ? couponService.calculateDiscount(code, orderAmount) : BigDecimal.ZERO;
        
        Map<String, Object> response = new HashMap<>();
        response.put("valid", isValid);
        response.put("discount", discount);
        response.put("code", code);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/calculate-discount")
    @Operation(summary = "Calculate discount", description = "Calculates the discount amount for a given coupon and order")
    @ApiResponse(responseCode = "200", description = "Discount calculated successfully")
    public ResponseEntity<Map<String, Object>> calculateDiscount(
            @Parameter(description = "Coupon code") @RequestParam String code,
            @Parameter(description = "Order amount") @RequestParam BigDecimal orderAmount) {
        
        BigDecimal discount = couponService.calculateDiscount(code, orderAmount);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", code);
        response.put("orderAmount", orderAmount);
        response.put("discount", discount);
        response.put("finalAmount", orderAmount.subtract(discount));
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update coupon", description = "Updates an existing coupon")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Coupon updated successfully"),
        @ApiResponse(responseCode = "404", description = "Coupon not found"),
        @ApiResponse(responseCode = "400", description = "Invalid coupon data")
    })
    public ResponseEntity<CouponDto> updateCoupon(
            @Parameter(description = "Coupon ID") @PathVariable String id,
            @Valid @RequestBody CouponDto couponDto) {
        
        try {
            Coupon updatedCoupon = convertToEntity(couponDto);
            Coupon savedCoupon = couponService.updateCoupon(id, updatedCoupon);
            CouponDto responseDto = convertToDto(savedCoupon);
            
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate coupon", description = "Deactivates a coupon without deleting it")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Coupon deactivated successfully"),
        @ApiResponse(responseCode = "404", description = "Coupon not found")
    })
    public ResponseEntity<Map<String, String>> deactivateCoupon(@Parameter(description = "Coupon ID") @PathVariable String id) {
        try {
            couponService.deactivateCoupon(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Coupon deactivated successfully");
            response.put("id", id);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete coupon", description = "Permanently deletes a coupon")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Coupon deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Coupon not found")
    })
    public ResponseEntity<Map<String, String>> deleteCoupon(@Parameter(description = "Coupon ID") @PathVariable String id) {
        try {
            couponService.deleteCoupon(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Coupon deleted successfully");
            response.put("id", id);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    private CouponDto convertToDto(Coupon coupon) {
        CouponDto dto = new CouponDto();
        BeanUtils.copyProperties(coupon, dto);
        return dto;
    }
    
    private Coupon convertToEntity(CouponDto dto) {
        Coupon coupon = new Coupon();
        BeanUtils.copyProperties(dto, coupon);
        return coupon;
    }
} 