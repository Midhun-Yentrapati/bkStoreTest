package com.bookverse.bookCatalog.config;

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
            .cors(cors -> cors.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // --- ADMIN/PROTECTED ENDPOINTS ---
                .requestMatchers("/api/books/admin/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_ADMIN", "ROLE_MANAGER")
                .requestMatchers(HttpMethod.POST, "/api/books").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/books/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_ADMIN", "ROLE_MANAGER")
                .requestMatchers(HttpMethod.PATCH, "/api/books/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_ADMIN", "ROLE_MANAGER")
                .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_ADMIN", "ROLE_MANAGER")
                .requestMatchers("/api/reviews/admin/**").hasAnyAuthority("ROLE_ADMIN", "ADMIN", "ROLE_SUPER_ADMIN", "SUPER_ADMIN", "ROLE_MANAGER", "MANAGER")
                
                // --- CUSTOMER ENDPOINTS ---
                .requestMatchers(HttpMethod.POST, "/api/reviews/**").hasAnyAuthority("ROLE_CUSTOMER", "CUSTOMER", "ROLE_ADMIN", "ADMIN", "ROLE_SUPER_ADMIN", "SUPER_ADMIN", "ROLE_MANAGER", "MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/reviews/**").hasAnyAuthority("ROLE_CUSTOMER", "CUSTOMER", "ROLE_ADMIN", "ADMIN", "ROLE_SUPER_ADMIN", "SUPER_ADMIN", "ROLE_MANAGER", "MANAGER")
                .requestMatchers(HttpMethod.DELETE, "/api/reviews/**").hasAnyAuthority("ROLE_CUSTOMER", "CUSTOMER", "ROLE_ADMIN", "ADMIN", "ROLE_SUPER_ADMIN", "SUPER_ADMIN", "ROLE_MANAGER", "MANAGER")

                // --- PUBLIC ENDPOINTS ---
                .requestMatchers(HttpMethod.GET, "/api/books", "/api/books/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/reviews/book/**", "/api/reviews/search/**").permitAll()
                .requestMatchers("/api/books/health", "/actuator/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                // All other requests must be authenticated
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}