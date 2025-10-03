package com.bookstore.api_gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Configuration
public class GatewayConfig {

    private static final Logger log = LoggerFactory.getLogger(GatewayConfig.class);

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.addAllowedOrigin("http://localhost:4200");
        corsConfig.addAllowedOrigin("http://localhost:3000");
        corsConfig.addAllowedMethod("*");
        corsConfig.addAllowedHeader("*");
        corsConfig.setAllowCredentials(true);
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }

    @Bean
    @Order(-2)
    public GlobalFilter corsResponseFilter() {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();
            
            // Handle preflight requests
            if (request.getMethod() == HttpMethod.OPTIONS) {
                response.setStatusCode(HttpStatus.OK);
                response.getHeaders().add("Access-Control-Allow-Origin", "http://localhost:4200");
                response.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                response.getHeaders().add("Access-Control-Allow-Headers", "*");
                response.getHeaders().add("Access-Control-Allow-Credentials", "true");
                response.getHeaders().add("Access-Control-Max-Age", "3600");
                return Mono.empty();
            }
            
            return chain.filter(exchange);
        };
    }

    /**
     * This GlobalFilter logs incoming requests and their Authorization headers.
     * It helps in debugging authentication issues by showing what the gateway is receiving
     * before forwarding the request to a downstream service.
     */
    @Bean
    @Order(-1) // Ensure this filter runs early in the filter chain.
    public GlobalFilter authHeaderLogger() {
        return (exchange, chain) -> {
            HttpHeaders headers = exchange.getRequest().getHeaders();
            String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);

            log.info("[API GATEWAY] Inbound Request: {} {}", exchange.getRequest().getMethod(), exchange.getRequest().getURI());

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                log.info("[API GATEWAY] Authorization header found and is being forwarded.");
            } else {
                log.warn("[API GATEWAY] No Authorization header found for request: {}", exchange.getRequest().getURI());
            }

            // Continue the filter chain. The headers are passed along by default.
            return chain.filter(exchange);
        };
    }
}