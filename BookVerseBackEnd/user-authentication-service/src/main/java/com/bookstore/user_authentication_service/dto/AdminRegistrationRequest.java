package com.bookstore.user_authentication_service.dto;

import com.bookstore.user_authentication_service.entity.Permission;
import com.bookstore.user_authentication_service.entity.UserRole;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminRegistrationRequest {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain letters, numbers, dots, underscores, and hyphens")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", 
             message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
    private String password;
    
    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
    
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Please provide a valid mobile number")
    private String mobileNumber;
    
    @Past(message = "Date of birth must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    
    // Admin-specific fields
    @NotNull(message = "User role is required")
    private UserRole userRole;
    
    @NotBlank(message = "Employee ID is required")
    @Size(min = 3, max = 50, message = "Employee ID must be between 3 and 50 characters")
    private String employeeId;
    
    @NotBlank(message = "Department is required")
    @Size(min = 2, max = 100, message = "Department must be between 2 and 100 characters")
    private String department;
    
    private String managerId;
    
    private Set<Permission> permissions;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;
    
    @DecimalMin(value = "0.0", message = "Salary must be positive")
    private BigDecimal salary;
    
    // Registration metadata
    private String ipAddress;
    private String userAgent;
    private String createdByAdminId;
    
    // Validation method for password confirmation
    @AssertTrue(message = "Password and confirm password must match")
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }
    
    // Validation method for admin role
    @AssertTrue(message = "User role must be an admin role (not CUSTOMER)")
    public boolean isValidAdminRole() {
        return userRole != null && userRole != UserRole.CUSTOMER;
    }
}
