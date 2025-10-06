package com.bookverse.CartOrderManagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Health Check", description = "Health check operations for Cart Order Management service")
public class HealthController {

    @GetMapping("/test/health")
    @Operation(summary = "Health Check", description = "Check if the Cart Order Management service is running")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "cart-order-management");
        response.put("timestamp", LocalDateTime.now());
        response.put("version", "1.0.0");
        response.put("description", "Cart Order Management Service is running successfully");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    @Operation(summary = "Service Info", description = "Get information about the Cart Order Management service")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "cart-order-management");
        response.put("version", "1.0.0");
        response.put("description", "Handles cart management, order processing, payments, and coupons");
        response.put("endpoints", new String[]{
            "/api/cart/**", 
            "/api/orders/**", 
            "/api/payments/**", 
            "/api/coupons/**", 
            "/api/wishlist/**"
        });
        response.put("port", 8083);
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
} 