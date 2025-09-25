package com.bookstore.user_authentication_service.dto;

import com.bookstore.user_authentication_service.entity.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {
    
    private String id;
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain letters, numbers, dots, underscores, and hyphens")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]", 
             message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
    private String password;
    
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Please provide a valid mobile number")
    private String mobileNumber;
    
    @Past(message = "Date of birth must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    
    @Size(max = 500, message = "Bio cannot exceed 500 characters")
    private String bio;
    
    private String profilePictureUrl;
    
    // Role and Type Information
    private UserRole userRole;
    private UserType userType;
    
    // Admin-specific fields (optional for customer accounts)
    private String employeeId;
    private String department;
    private String managerId;
    private Set<Permission> permissions;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;
    
    private BigDecimal salary;
    
    // Account Status and Security
    private AccountStatus accountStatus;
    private Boolean isEmailVerified;
    private Boolean isMobileVerified;
    private Boolean isTwoFactorEnabled;
    private Integer failedLoginAttempts;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime accountLockedUntil;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginAt;
    
    private String lastLoginIp;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime passwordChangedAt;
    
    // Audit Fields
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // Relationships
    private List<AddressDTO> addresses;
    
    // Computed Fields
    private Double profileCompleteness;
    private Boolean isAccountNonLocked;
    private Boolean isAccountActive;
    private Boolean isCustomer;
    private Boolean isAdmin;
    
    // Helper method to exclude password from responses
    public UserDTO excludePassword() {
        this.password = null;
        return this;
    }
    
    // Helper method to include only basic info
    public static UserDTO basic(String id, String username, String email, String fullName, UserRole userRole) {
        return UserDTO.builder()
                .id(id)
                .username(username)
                .email(email)
                .fullName(fullName)
                .userRole(userRole)
                .build();
    }
}
