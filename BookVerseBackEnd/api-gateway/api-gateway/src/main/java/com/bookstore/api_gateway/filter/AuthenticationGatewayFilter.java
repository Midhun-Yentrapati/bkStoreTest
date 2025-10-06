package com.bookstore.api_gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

// @Component - Disabled for now to focus on CORS issues
public class AuthenticationGatewayFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationGatewayFilter.class);

    // Define public endpoints that don't require authentication
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
        // Authentication endpoints
        "/api/auth/login",
        "/api/auth/register",
        "/api/test",
        
        // Public book browsing
        "/api/books",
        "/api/categories",
        
        // Public review reading (not user-specific)
        "/api/reviews/book",
        "/api/reviews/search",
        
        // Health and documentation
        "/actuator",
        "/swagger-ui",
        "/v3/api-docs"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();
        
        log.info("[GATEWAY AUTH] Processing request: {} {}", method, path);
        
        // Allow OPTIONS requests for CORS
        if (HttpMethod.OPTIONS.equals(method)) {
            log.info("[GATEWAY AUTH] Allowing OPTIONS request for CORS");
            return chain.filter(exchange);
        }
        
        // Check if endpoint is public
        if (isPublicEndpoint(path, method)) {
            log.info("[GATEWAY AUTH] Public endpoint, allowing without authentication: {}", path);
            return chain.filter(exchange);
        }
        
        // Check for authentication header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("[GATEWAY AUTH] Missing or invalid Authorization header for protected endpoint: {}", path);
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        
        log.info("[GATEWAY AUTH] Valid Authorization header found, forwarding request");
        return chain.filter(exchange);
    }
    
    private boolean isPublicEndpoint(String path, HttpMethod method) {
        // Check exact matches and prefixes
        for (String publicEndpoint : PUBLIC_ENDPOINTS) {
            if (path.startsWith(publicEndpoint)) {
                // Special handling for GET requests to books and reviews
                if (path.startsWith("/api/books") || path.startsWith("/api/categories")) {
                    return HttpMethod.GET.equals(method);
                }
                if (path.startsWith("/api/reviews/book") || path.startsWith("/api/reviews/search")) {
                    return HttpMethod.GET.equals(method);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return -100; // Run before other filters
    }
}