package com.bookstore.user_authentication_service.controller;

import com.bookstore.user_authentication_service.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
@Slf4j
public class DebugController {
    
    private final JwtService jwtService;
    
    @GetMapping("/auth-context")
    public ResponseEntity<?> getAuthContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("hasAuthentication", auth != null);
        response.put("isAuthenticated", auth != null ? auth.isAuthenticated() : false);
        response.put("principal", auth != null ? auth.getPrincipal() : null);
        response.put("authorities", auth != null ? auth.getAuthorities() : null);
        response.put("authType", auth != null ? auth.getClass().getSimpleName() : null);
        
        log.info("[DEBUG] Authentication context: {}", response);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/jwt-claims")
    public ResponseEntity<?> getJwtClaims(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwt = authHeader.substring(7);
                
                Map<String, Object> response = new HashMap<>();
                response.put("userId", jwtService.extractUserId(jwt));
                response.put("userType", jwtService.extractUserType(jwt));
                response.put("userRole", jwtService.extractUserRole(jwt));
                response.put("isValid", jwtService.isTokenValid(jwt, null));
                response.put("isExpired", jwtService.isTokenExpired(jwt));
                response.put("allClaims", jwtService.extractAllClaims(jwt));
                
                log.info("[DEBUG] JWT claims: {}", response);
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "No Bearer token provided"));
            }
        } catch (Exception e) {
            log.error("[DEBUG] Error extracting JWT claims: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/test-admin")
    public ResponseEntity<?> testAdminEndpoint() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Admin endpoint accessed successfully");
        response.put("principal", auth != null ? auth.getPrincipal() : null);
        response.put("authorities", auth != null ? auth.getAuthorities() : null);
        
        log.info("[DEBUG] Admin endpoint accessed by: {}", auth);
        
        return ResponseEntity.ok(response);
    }
}
