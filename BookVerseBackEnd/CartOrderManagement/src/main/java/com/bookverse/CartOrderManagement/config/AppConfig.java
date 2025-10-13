package com.bookverse.CartOrderManagement.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.Collections;
import java.util.Objects;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        // Add our custom interceptor to the RestTemplate
        restTemplate.setInterceptors(Collections.singletonList(new JwtHeaderInterceptor()));
        return restTemplate;
    }
}

/**
 * A RestTemplate interceptor that automatically adds the Authorization header
 * from the current request to all outgoing requests.
 */
class JwtHeaderInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        // Check if there is a current request context
        if (RequestContextHolder.getRequestAttributes() != null) {
            HttpServletRequest currentRequest = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
            String authHeader = currentRequest.getHeader(HttpHeaders.AUTHORIZATION);

            // If the header exists, add it to the outgoing request
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                request.getHeaders().add(HttpHeaders.AUTHORIZATION, authHeader);
            }
        } else {
            // Handle cases where the call is not made in the context of an HTTP request
            // (e.g., background jobs). You might log a warning here.
            System.out.println("No active HTTP request context, cannot propagate Authorization header.");
        }

        return execution.execute(request, body);
    }
}