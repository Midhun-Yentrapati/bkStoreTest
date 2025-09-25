package com.bookstore.user_authentication_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    // Basic User Information
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain letters, numbers, dots, underscores, and hyphens")
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Column(unique = true, nullable = false, length = 100)
    private String email;
    
    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;
    
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Please provide a valid mobile number")
    @Column(name = "mobile_number", length = 20)
    private String mobileNumber;
    
    @Past(message = "Date of birth must be in the past")
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    
    @Size(max = 500, message = "Bio cannot exceed 500 characters")
    @Column(columnDefinition = "TEXT")
    private String bio;
    
    @Column(name = "profile_picture_url")
    private String profilePictureUrl;
    
    // Role and Type Information
    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    @Builder.Default
    private UserRole userRole = UserRole.CUSTOMER;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    @Builder.Default
    private UserType userType = UserType.CUSTOMER;
    
    // Admin-specific fields (optional for customer accounts)
    @Column(name = "employee_id", length = 50)
    private String employeeId;
    
    @Column(length = 100)
    private String department;
    
    @Column(name = "manager_id")
    private String managerId;
    
    @ElementCollection(targetClass = Permission.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_permissions", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "permission")
    private Set<Permission> permissions;
    
    @Column(name = "hire_date")
    private LocalDate hireDate;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal salary;
    
    // Account Status and Security
    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    @Builder.Default
    private AccountStatus accountStatus = AccountStatus.ACTIVE;
    
    @Column(name = "is_email_verified", nullable = false)
    @Builder.Default
    private Boolean isEmailVerified = false;
    
    @Column(name = "is_mobile_verified", nullable = false)
    @Builder.Default
    private Boolean isMobileVerified = false;
    
    @Column(name = "is_two_factor_enabled", nullable = false)
    @Builder.Default
    private Boolean isTwoFactorEnabled = false;
    
    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private Integer failedLoginAttempts = 0;
    
    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;
    
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;
    
    // Audit Fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    // Relationships
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Address> addresses;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserSession> sessions;
    
    // Business Logic Methods
    public boolean isCustomer() {
        return UserRole.CUSTOMER.equals(userRole);
    }
    
    public boolean isAdmin() {
        return UserRole.ADMIN.equals(userRole) || 
               UserRole.SUPER_ADMIN.equals(userRole) ||
               UserRole.MANAGER.equals(userRole) ||
               UserRole.MODERATOR.equals(userRole) ||
               UserRole.SUPPORT.equals(userRole);
    }
    
    public boolean isSuperAdmin() {
        return UserRole.SUPER_ADMIN.equals(userRole);
    }
    
    public boolean hasPermission(Permission permission) {
        return permissions != null && permissions.contains(permission);
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return accountLockedUntil == null || accountLockedUntil.isBefore(LocalDateTime.now());
    }
    
    public boolean isAccountActive() {
        return AccountStatus.ACTIVE.equals(accountStatus);
    }
    
    public boolean isDeleted() {
        return deletedAt != null;
    }
    
    public double getProfileCompleteness() {
        int totalFields = 10;
        int completedFields = 0;
        
        if (fullName != null && !fullName.trim().isEmpty()) completedFields++;
        if (email != null && !email.trim().isEmpty()) completedFields++;
        if (mobileNumber != null && !mobileNumber.trim().isEmpty()) completedFields++;
        if (dateOfBirth != null) completedFields++;
        if (bio != null && !bio.trim().isEmpty()) completedFields++;
        if (profilePictureUrl != null && !profilePictureUrl.trim().isEmpty()) completedFields++;
        if (isEmailVerified) completedFields++;
        if (isMobileVerified) completedFields++;
        if (addresses != null && !addresses.isEmpty()) completedFields += 2;
        
        return (double) completedFields / totalFields * 100;
    }
    
    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts = (this.failedLoginAttempts == null ? 0 : this.failedLoginAttempts) + 1;
        
        // Lock account after 5 failed attempts
        if (this.failedLoginAttempts >= 5) {
            this.accountLockedUntil = LocalDateTime.now().plusHours(1); // Lock for 1 hour
        }
    }
    
    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.accountLockedUntil = null;
    }
    
    public void updateLastLogin(String ipAddress) {
        this.lastLoginAt = LocalDateTime.now();
        this.lastLoginIp = ipAddress;
        resetFailedLoginAttempts();
    }
    
    // Spring Security UserDetails interface methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convert userType to Spring Security authority
        String authority = "ROLE_" + (userType != null ? userType.name() : "USER");
        return Collections.singletonList(new SimpleGrantedAuthority(authority));
    }
    
    @Override
    public String getUsername() {
        return this.username;
    }
    
    @Override
    public String getPassword() {
        return this.password;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        // Account never expires in our system
        return true;
    }
    
    // isAccountNonLocked() method already exists above - no need to duplicate
    
    @Override
    public boolean isCredentialsNonExpired() {
        // Credentials never expire in our system
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        // Account is enabled if status is ACTIVE and email is verified
        return accountStatus == AccountStatus.ACTIVE && Boolean.TRUE.equals(isEmailVerified);
    }
}
