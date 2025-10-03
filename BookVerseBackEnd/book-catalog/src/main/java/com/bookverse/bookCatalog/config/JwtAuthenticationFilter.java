package com.bookverse.bookCatalog.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
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

        log.info("Processing request: {} {}", request.getMethod(), request.getRequestURI());

        String authHeader = request.getHeader("Authorization");
        String userId = request.getHeader("X-User-Id");
        String roles = request.getHeader("X-User-Roles");

        log.debug("Authorization Header: {}", authHeader);
        log.debug("X-User-Id Header: {}", userId);
        log.debug("X-User-Roles Header: {}", roles);

        if (isPublicEndpoint(request)) {
            log.debug("Endpoint is public. Skipping JWT authentication.");
            filterChain.doFilter(request, response);
            return;
        }

        log.debug("Endpoint is protected. Proceeding with JWT authentication.");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);

                if (userId != null && userId != null && roles != null) {
                    List<SimpleGrantedAuthority> authorities = Arrays.stream(roles.split(","))
                            .map(role -> {
                                String trimmedRole = role.trim().toUpperCase();
                                if (!trimmedRole.startsWith("ROLE_")) {
                                    return new SimpleGrantedAuthority("ROLE_" + trimmedRole);
                                }
                                return new SimpleGrantedAuthority(trimmedRole);
                            })
                            .collect(Collectors.toList());

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userId, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Authentication successful for user: {} with roles: {}", userId, authorities);
                } else {
                    log.debug("X-User headers not found. Falling back to JWT decoding.");
                    // Fallback JWT decoding logic...
                     try {
                        String[] tokenParts = token.split("\\.");
                        if (tokenParts.length == 3) {
                            String payload = new String(java.util.Base64.getUrlDecoder().decode(tokenParts[1]));
                            log.debug("JWT payload: {}", payload);
                            
                            if (payload.contains("\"userRole\"")) {
                                String userRole = extractValueFromJson(payload, "userRole");
                                String userNameFromToken = extractValueFromJson(payload, "username");
                                
                                if (userRole != null && userNameFromToken != null) {
                                    String roleWithPrefix = userRole.startsWith("ROLE_") ? userRole.toUpperCase() : "ROLE_" + userRole.toUpperCase();
                                    
                                    List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(roleWithPrefix));
                                    
                                    UsernamePasswordAuthenticationToken authToken = 
                                            new UsernamePasswordAuthenticationToken(userNameFromToken, null, authorities);
                                    
                                    SecurityContextHolder.getContext().setAuthentication(authToken);
                                    log.debug("Authentication successful from JWT for user: {} with role: {}", userNameFromToken, roleWithPrefix);
                                } else {
                                     log.warn("JWT payload missing 'userRole' or 'username'.");
                                }
                            } else {
                                log.warn("JWT payload does not contain 'userRole'.");
                            }
                        }
                    } catch (Exception jwtError) {
                        log.error("Error decoding JWT token: {}", jwtError.getMessage());
                    }
                }
            } catch (Exception e) {
                log.error("Error processing authentication token: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        } else {
            log.warn("No Authorization header found for protected endpoint.");
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if (path.startsWith("/api/books") && (HttpMethod.GET.name().equalsIgnoreCase(method) || HttpMethod.POST.name().equalsIgnoreCase(method))) {
            return true;
        }

        if (path.startsWith("/api/books/") && (HttpMethod.PUT.name().equalsIgnoreCase(method) || HttpMethod.PATCH.name().equalsIgnoreCase(method) || HttpMethod.DELETE.name().equalsIgnoreCase(method))) {
            return true;
        }

        return path.startsWith("/api/categories") ||
               path.startsWith("/actuator") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.equals("/api/books/health") ||
               (path.startsWith("/api/reviews/book/") && !path.contains("/user/"));
    }
    
    // extractValueFromJson method remains the same
    private String extractValueFromJson(String json, String key) {
        try {
            String searchKey = "\"" + key + "\":\"";
            int startIndex = json.indexOf(searchKey);
            if (startIndex == -1) {
                searchKey = "\"" + key + "\":";
                startIndex = json.indexOf(searchKey);
                if (startIndex == -1) return null;
                
                startIndex += searchKey.length();
                int endIndex = json.indexOf(",", startIndex);
                if (endIndex == -1) endIndex = json.indexOf("}", startIndex);
                if (endIndex == -1) return null;
                
                return json.substring(startIndex, endIndex).replace("\"", "").trim();
            } else {
                startIndex += searchKey.length();
                int endIndex = json.indexOf("\"", startIndex);
                if (endIndex == -1) return null;
                
                return json.substring(startIndex, endIndex);
            }
        } catch (Exception e) {
            log.error("Error extracting value from JSON: {}", e.getMessage());
            return null;
        }
    }
}