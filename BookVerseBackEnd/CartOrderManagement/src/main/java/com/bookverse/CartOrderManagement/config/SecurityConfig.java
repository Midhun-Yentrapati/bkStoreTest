package com.bookverse.CartOrderManagement.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                // Disabled - API Gateway handles CORS
                .cors(cors -> cors.disable()) 
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints - health checks and documentation only
                        .requestMatchers("/api/cart-order/health").permitAll()
                        .requestMatchers("/actuator/**").permitAll()

                        // Documentation endpoints
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()

                        // Public coupon endpoints - users can view available coupons
                        .requestMatchers(HttpMethod.GET, "/api/coupons/available").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/coupons/validate/**").permitAll()

                        // Admin-only endpoints - DEMO MODE: permitAll
                        .requestMatchers("/api/coupons/admin/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/coupons").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/coupons/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/coupons/**").permitAll()
                        .requestMatchers("/api/admin/orders").permitAll()

                        // Admin order management - DEMO MODE: permitAll
                        .requestMatchers("/api/orders/admin/**").permitAll()
                        
                        // User-specific endpoints - DEMO MODE: permitAll
                        .requestMatchers("/api/cart/**").permitAll()
                        .requestMatchers("/api/wishlist/**").permitAll()

                        // All other order and payment operations - DEMO MODE: permitAll
                        .requestMatchers("/api/orders/**").permitAll()
                        .requestMatchers("/api/payments/**").permitAll()

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}