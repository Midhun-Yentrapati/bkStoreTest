package com.bookstore.api_gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;

@Configuration
public class GatewayConfig {

    private static final Logger log = LoggerFactory.getLogger(GatewayConfig.class);

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