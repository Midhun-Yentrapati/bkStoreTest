package com.bookverse.CartOrderManagement.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Service
public class ExternalDataService {

    private final RestTemplate restTemplate;
    private final String USER_SERVICE_URL = "http://localhost:8090/api/users";
    private final String BOOK_SERVICE_URL = "http://localhost:8090/api/books";
    private final String GATEWAY_URL = "http://localhost:8090";

    public ExternalDataService() {
        this.restTemplate = new RestTemplate();
    }

    public String getCustomerName(String userId) {
        try {
            Map<String, Object> user = restTemplate.getForObject(GATEWAY_URL + "/api/users/" + userId, Map.class);
            if (user != null) {
                String fullName = (String) user.get("fullName");
                String username = (String) user.get("username");
                return fullName != null ? fullName : username;
            }
        } catch (RestClientException e) {
            System.out.println("Failed to fetch user data for userId: " + userId + ", error: " + e.getMessage());
        }
        return "Customer " + (userId != null ? userId.substring(0, Math.min(8, userId.length())) : "Unknown");
    }

    public String getCustomerEmail(String userId) {
        try {
            Map<String, Object> user = restTemplate.getForObject(GATEWAY_URL + "/api/users/" + userId, Map.class);
            if (user != null) {
                return (String) user.get("email");
            }
        } catch (RestClientException e) {
            System.out.println("Failed to fetch user email for userId: " + userId + ", error: " + e.getMessage());
        }
        return userId != null ? userId.substring(0, Math.min(8, userId.length())) + "@example.com" : "unknown@example.com";
    }

    public Map<String, Object> getBookDetails(String bookId) {
        try {
            return restTemplate.getForObject(GATEWAY_URL + "/api/books/" + bookId, Map.class);
        } catch (RestClientException e) {
            System.out.println("Failed to fetch book data for bookId: " + bookId + ", error: " + e.getMessage());
            return null;
        }
    }
}