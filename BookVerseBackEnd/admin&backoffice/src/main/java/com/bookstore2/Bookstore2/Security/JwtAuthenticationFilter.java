package com.bookstore2.Bookstore2.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Skip filter for public endpoints
        String path = request.getRequestURI();
        if (isPublicEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                // Extract user info from custom headers set by the API Gateway
                String userId = request.getHeader("X-User-Id");
                String username = request.getHeader("X-User-Name");
                String roles = request.getHeader("X-User-Roles");
                String userType = request.getHeader("X-User-Type");
                
                // Fallback: decode JWT roles if gateway headers are not present
                if (userId == null || username == null || roles == null) {
                    String token = authHeader.substring(7);
                    try {
                        // Very lightweight parsing: split JWT to get payload and decode base64 JSON
                        String[] parts = token.split("\\.");
                        if (parts.length == 3) {
                            String payloadJson = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                            // Extract common claim names for roles and subject
                            // This is a minimal JSON parse to avoid extra dependencies
                            if (username == null) {
                                username = extractJsonValue(payloadJson, "sub");
                            }
                            if (userId == null) {
                                userId = extractJsonValue(payloadJson, "userId");
                                if (userId == null) userId = extractJsonValue(payloadJson, "uid");
                            }
                            if (roles == null) {
                                String realmRoles = extractJsonArrayAsCsv(payloadJson, "roles");
                                if (realmRoles == null) realmRoles = extractJsonArrayAsCsv(payloadJson, "authorities");
                                if (realmRoles == null) realmRoles = extractJsonArrayAsCsv(payloadJson, "scope");
                                roles = realmRoles;
                            }
                            if (userType == null) {
                                userType = extractJsonValue(payloadJson, "userType");
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Failed to parse JWT for fallback role extraction: {}", e.getMessage());
                    }
                }
                
                if (userId != null && username != null && roles != null) {
                    // Validate that user has admin privileges
                    if (!"ADMIN".equals(userType) && !roles.contains("ADMIN") && !roles.contains("SUPER_ADMIN") && !roles.contains("MANAGER")) {
                        log.warn("Non-admin user {} attempted to access admin endpoint: {}", username, path);
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                    
                    List<SimpleGrantedAuthority> authorities = Arrays.stream(roles.split(","))
                            .map(role -> new SimpleGrantedAuthority(role.trim()))
                            .collect(Collectors.toList());
                    
                    UsernamePasswordAuthenticationToken authToken = 
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Admin authentication set for user: {} with roles: {}", username, roles);
                } else {
                    log.warn("Missing required headers/claims for admin authentication");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            } catch (Exception e) {
                log.error("Error processing JWT token: {}", e.getMessage());
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } else {
            log.warn("Missing or invalid Authorization header for admin endpoint: {}", path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/actuator/") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs/") ||
               path.startsWith("/swagger-resources/") ||
               path.startsWith("/webjars/");
    }

    // Minimal JSON helpers to avoid adding dependencies
    private String extractJsonValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\"\s*:\s*\""; // "key":"
            int idx = json.indexOf(pattern);
            if (idx >= 0) {
                int start = idx + pattern.length();
                int end = json.indexOf("\"", start);
                if (end > start) {
                    return json.substring(start, end);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String extractJsonArrayAsCsv(String json, String key) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*\\[([^\\]]+)\\]"; 
            int idx = json.indexOf(pattern);
            if (idx >= 0) {
                int start = idx + pattern.length();
                int end = json.indexOf("]", start);
                if (end > start) {
                    String arrayContent = json.substring(start, end);
                    // Remove quotes and whitespace, join by comma
                    String[] items = Arrays.stream(arrayContent.split(","))
                            .map(s -> s.replace("\"", "").trim())
                            .filter(s -> !s.isEmpty())
                            .toArray(String[]::new);
                    return String.join(",", items);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }
} 