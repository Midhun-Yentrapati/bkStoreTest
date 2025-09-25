package com.bookstore.api_gateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class GatewayConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOriginPatterns(Arrays.asList("http://localhost:4200", "http://localhost:3000"));
        corsConfig.setMaxAge(3600L);
        // Include PATCH method for book updates
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        corsConfig.setAllowedHeaders(Arrays.asList("*"));
        corsConfig.setExposedHeaders(Arrays.asList("*"));
        corsConfig.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }

    @Bean
    public AuthHeaderGatewayFilterFactory authHeaderGatewayFilterFactory() {
        return new AuthHeaderGatewayFilterFactory();
    }

    public static class AuthHeaderGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

        @Override
        public GatewayFilter apply(Object config) {
            return (exchange, chain) -> {
                // Log the incoming request headers for debugging
                HttpHeaders headers = exchange.getRequest().getHeaders();
                String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);
                
                System.out.println("[API GATEWAY] Processing request: " + exchange.getRequest().getURI());
                System.out.println("[API GATEWAY] Authorization header: " + (authHeader != null ? "Bearer ***" : "null"));
                
                // The gateway should automatically forward all headers including Authorization
                // This filter is mainly for logging and debugging
                return chain.filter(exchange);
            };
        }
    }
}
