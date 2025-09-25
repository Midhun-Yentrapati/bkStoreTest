package com.bookstore.user_authentication_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
    
    @NotBlank(message = "Username or email is required")
    private String usernameOrEmail;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    private String ipAddress;
    private String userAgent;
    private String device;
    private String location;
    private Boolean rememberMe;
}
