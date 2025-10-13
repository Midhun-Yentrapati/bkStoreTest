package com.bookverse.CartOrderManagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

@Service
public class ExternalDataService {

    private final RestTemplate restTemplate;
    private final String GATEWAY_URL = "http://localhost:8090";

    @Autowired
    public ExternalDataService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getCustomerName(String userId) {
        try {
            Map<String, Object> user = restTemplate.getForObject(GATEWAY_URL + "/api/users/" + userId, Map.class);
            if (user != null) {
                String fullName = (String) user.get("fullName");
                if (fullName == null) fullName = (String) user.get("full_name");
                String username = (String) user.get("username");
                return fullName != null ? fullName : (username != null ? username : "Customer Name N/A");
            }
        } catch (RestClientException e) {
            System.err.println("Failed to fetch user data for userId: " + userId + ", error: " + e.getMessage());
        }
        return "Customer " + (userId != null ? userId.substring(0, Math.min(8, userId.length())) : "Unknown");
    }

    public String getCustomerEmail(String userId) {
        try {
            Map<String, Object> user = restTemplate.getForObject(GATEWAY_URL + "/api/users/" + userId, Map.class);
            if (user != null) {
                return (String) user.getOrDefault("email", "Email N/A");
            }
        } catch (RestClientException e) {
            System.err.println("Failed to fetch user email for userId: " + userId + ", error: " + e.getMessage());
        }
        return userId != null ? userId.substring(0, Math.min(8, userId.length())) + "@example.com" : "unknown@example.com";
    }

    public String getCustomerPhone(String userId) {
        try {
            Map<String, Object> user = restTemplate.getForObject(GATEWAY_URL + "/api/users/" + userId, Map.class);
            if (user != null) {
                String phone = (String) user.get("mobileNumber");
                if (phone == null) phone = (String) user.get("mobile_number");
                if (phone == null) phone = (String) user.get("phoneNumber");
                if (phone == null) phone = (String) user.get("phone_number");
                return phone != null ? phone : "Phone N/A";
            }
        } catch (RestClientException e) {
            System.err.println("Failed to fetch user phone for userId: " + userId + ", error: " + e.getMessage());
        }
        return "Phone N/A";
    }

    public Map<String, String> getCustomerDetails(String userId) {
        Map<String, String> customerDetails = new HashMap<>();
        try {
            Map<String, Object> user = restTemplate.getForObject(GATEWAY_URL + "/api/users/" + userId, Map.class);
            if (user != null) {
                String fullName = (String) user.get("fullName");
                if (fullName == null) fullName = (String) user.get("full_name");
                String username = (String) user.get("username");
                customerDetails.put("name", fullName != null ? fullName : (username != null ? username : "Customer Name N/A"));

                customerDetails.put("email", (String) user.getOrDefault("email", "Email N/A"));

                String phone = (String) user.get("mobileNumber");
                if (phone == null) phone = (String) user.get("mobile_number");
                if (phone == null) phone = (String) user.get("phoneNumber");
                if (phone == null) phone = (String) user.get("phone_number");
                customerDetails.put("phone", phone != null ? phone : "Phone N/A");

                return customerDetails;
            }
        } catch (RestClientException e) {
            System.err.println("Failed to fetch customer details for userId: " + userId + ", error: " + e.getMessage());
            e.printStackTrace();
        }

        customerDetails.put("name", "Customer " + (userId != null ? userId.substring(0, Math.min(8, userId.length())) : "Unknown"));
        customerDetails.put("email", userId != null ? userId.substring(0, Math.min(8, userId.length())) + "@example.com" : "unknown@example.com");
        customerDetails.put("phone", "Phone N/A");
        return customerDetails;
    }

    // =================================================================
    // == NEW METHOD FOR ADDRESS FIX IS HERE
    // =================================================================
    public Map<String, Object> getAddressDetails(String addressId) {
        if (addressId == null || addressId.trim().isEmpty()) {
            return null;
        }
        try {
            // This call is automatically authenticated by the RestTemplate interceptor
            return restTemplate.getForObject(GATEWAY_URL + "/api/users/address/" + addressId, Map.class);
        } catch (RestClientException e) {
            System.err.println("Failed to fetch address details for addressId: " + addressId + ", error: " + e.getMessage());
            return null; // Return null if the address cannot be fetched
        }
    }
    // =================================================================
    // == END OF NEW METHOD
    // =================================================================

    public Map<String, Object> getBookDetails(String bookId) {
        try {
            return restTemplate.getForObject(GATEWAY_URL + "/api/books/" + bookId, Map.class);
        } catch (RestClientException e) {
            System.err.println("Failed to fetch book data for bookId: " + bookId + ", error: " + e.getMessage());
            return null;
        }
    }

    public boolean testUserServiceConnection() {
        try {
            restTemplate.getForObject(GATEWAY_URL + "/api/users/check-username/test", Map.class);
            return true;
        } catch (RestClientException e) {
            System.err.println("User service connection test failed: " + e.getMessage());
            return false;
        }
    }
}