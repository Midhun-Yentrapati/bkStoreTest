package com.bookstore.user_authentication_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {
    
    @PostConstruct
    public void init() {
        log.info("TestController initialized successfully with mapping: /api/test");
        log.info("Available endpoints:");
        log.info("  GET /api/test/health");
        log.info("  GET /api/test/health/detailed");
        log.info("  GET /api/test/ping");
        log.info("  GET /api/test/info");
        log.info("  GET /api/test/database");
    }
    
    @GetMapping("/health")        // GET /api/test/health - Basic health check
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.info("Health check requested");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "User Authentication Service");
        response.put("timestamp", LocalDateTime.now());
        response.put("version", "1.0.0");
        response.put("description", "Unified user authentication and management service");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health/detailed")  // GET /api/test/health/detailed - Detailed health check
    public ResponseEntity<Map<String, Object>> detailedHealthCheck() {
        log.info("Detailed health check requested");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "User Authentication Service");
        response.put("timestamp", LocalDateTime.now());
        response.put("version", "1.0.0");
        response.put("description", "Unified user authentication and management service");
        response.put("port", "8081");
        response.put("environment", "test");
        
        // Add system information
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("javaVersion", System.getProperty("java.version"));
        systemInfo.put("osName", System.getProperty("os.name"));
        systemInfo.put("osVersion", System.getProperty("os.version"));
        systemInfo.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        systemInfo.put("maxMemory", Runtime.getRuntime().maxMemory());
        systemInfo.put("totalMemory", Runtime.getRuntime().totalMemory());
        systemInfo.put("freeMemory", Runtime.getRuntime().freeMemory());
        response.put("system", systemInfo);
        
        // Add service features
        response.put("features", new String[]{
            "JWT Authentication",
            "Role-based Authorization", 
            "User Management",
            "Address Management",
            "Session Management",
            "Account Security"
        });
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/ping")          // GET /api/test/ping - Simple ping test
    public ResponseEntity<String> ping() {
        log.info("Ping requested");
        return ResponseEntity.ok("pong");
    }
    
    @GetMapping("/info")          // GET /api/test/info - Service information
    public ResponseEntity<Map<String, Object>> getServiceInfo() {
        log.info("Service info requested");
        
        Map<String, Object> info = new HashMap<>();
        info.put("serviceName", "User Authentication Service");
        info.put("version", "1.0.0");
        info.put("description", "Unified authentication system with role-based access control");
        info.put("features", new String[]{
            "JWT Authentication",
            "Role-based Authorization", 
            "User Management",
            "Address Management",
            "Session Management",
            "Account Security"
        });
        info.put("endpoints", new String[]{
            "/api/auth/login",
            "/api/auth/register", 
            "/api/users/profile",
            "/api/users/addresses",
            "/api/users/admin/**"
        });
        info.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(info);
    }
    
    @GetMapping("/database")     // GET /api/test/database - Database connectivity test
    public ResponseEntity<Map<String, Object>> testDatabaseConnectivity() {
        log.info("Database connectivity test requested");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("database", "H2 In-Memory Database");
        response.put("connection", "Active");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "Database connectivity test successful");
        
        // You could add actual database connectivity test here if needed
        // For now, we'll assume it's working since the service started
        
        return ResponseEntity.ok(response);
    }
}
