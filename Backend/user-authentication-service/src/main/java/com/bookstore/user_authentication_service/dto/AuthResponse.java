package com.bookstore.user_authentication_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {
    
    private Boolean success;
    private String message;
    private String accessToken;
    private String refreshToken;
    private Long expiresIn; // in seconds
    private String tokenType;
    private UserDTO user;
    private String sessionId;
    
    // Error details
    private String errorCode;
    private String errorDetails;
    
    // Success response
    public static AuthResponse success(String message, String accessToken, String refreshToken, 
                                     Long expiresIn, UserDTO user, String sessionId) {
        return AuthResponse.builder()
                .success(true)
                .message(message)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .tokenType("Bearer")
                .user(user.excludePassword())
                .sessionId(sessionId)
                .build();
    }
    
    // Error response
    public static AuthResponse error(String message, String errorCode, String errorDetails) {
        return AuthResponse.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .errorDetails(errorDetails)
                .build();
    }
    
    // Simple error response
    public static AuthResponse error(String message) {
        return AuthResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
    
    // Inner DTO classes for request handling
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RefreshTokenRequest {
        private String refreshToken;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LogoutRequest {
        private String sessionId;
        private Boolean logoutAllSessions;
    }
}
