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
                // CORRECT: Disable CORS as it is handled by the API Gateway
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

                        // Admin-only endpoints - coupon management
                        .requestMatchers("/api/coupons/admin/**").hasAnyAuthority("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/coupons").hasAnyAuthority("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/coupons/**").hasAnyAuthority("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/coupons/**").hasAnyAuthority("ADMIN", "SUPER_ADMIN", "MANAGER")

                        // Admin order management
                        .requestMatchers("/api/orders/admin/**").hasAnyAuthority("ADMIN", "SUPER_ADMIN", "MANAGER")
                        
                        // User-specific endpoints for cart and wishlist
                        .requestMatchers("/api/cart/**").hasAnyAuthority("CUSTOMER", "ADMIN", "SUPER_ADMIN", "MANAGER", "ROLE_CUSTOMER", "TYPE_CUSTOMER")
                        .requestMatchers("/api/wishlist/**").hasAnyAuthority("CUSTOMER", "ADMIN", "SUPER_ADMIN", "MANAGER", "ROLE_CUSTOMER", "TYPE_CUSTOMER")

                        // All other order and payment operations require authentication
                        .requestMatchers("/api/orders/**").hasAnyAuthority("CUSTOMER", "ADMIN", "SUPER_ADMIN", "MANAGER", "ROLE_CUSTOMER", "TYPE_CUSTOMER")
                        .requestMatchers("/api/payments/**").hasAnyAuthority("CUSTOMER", "ADMIN", "SUPER_ADMIN", "MANAGER", "ROLE_CUSTOMER", "TYPE_CUSTOMER")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}