package com.bookstore.user_authentication_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @NotBlank(message = "Session token is required")
    @Column(name = "session_token", nullable = false, unique = true, length = 500)
    private String sessionToken;
    
    @NotBlank(message = "Refresh token is required")
    @Column(name = "refresh_token", nullable = false, unique = true, length = 500)
    private String refreshToken;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(length = 100)
    private String device;
    
    @Column(length = 100)
    private String location;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", nullable = false)
    @Builder.Default
    private SessionType sessionType = SessionType.WEB;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "login_method", nullable = false)
    @Builder.Default
    private LoginMethod loginMethod = LoginMethod.PASSWORD;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "is_two_factor_verified", nullable = false)
    @Builder.Default
    private Boolean isTwoFactorVerified = false;
    
    @NotNull(message = "Expires at is required")
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;
    
    @Column(name = "logged_out_at")
    private LocalDateTime loggedOutAt;
    
    // Audit Fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    // Business Logic Methods
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }
    
    public boolean isValid() {
        return isActive && !isExpired() && loggedOutAt == null && user != null && user.isAccountNonLocked();
    }
    
    public boolean isCustomerSession() {
        return user != null && user.isCustomer();
    }
    
    public boolean isAdminSession() {
        return user != null && user.isAdmin();
    }
    
    public void updateLastAccessed() {
        this.lastAccessedAt = LocalDateTime.now();
    }
    
    /**
     * Logout the session by setting it as inactive and recording logout time
     */
    public void logout() {
        this.isActive = false;
        this.loggedOutAt = LocalDateTime.now();
    }

    /**
     * Extend the session by updating the expiration time
     * @param newExpirationTime the new expiration time for the session
     */
    public void extendSession(LocalDateTime newExpirationTime) {
        if (newExpirationTime == null) {
            throw new IllegalArgumentException("New expiration time cannot be null");
        }
        if (newExpirationTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("New expiration time cannot be in the past");
        }
        this.expiresAt = newExpirationTime;
        this.lastAccessedAt = LocalDateTime.now();
    }

    /**
     * Extend the session by a specific duration from now
     * @param duration the duration to extend the session by
     */
    public void extendSession(Duration duration) {
        if (duration == null || duration.isNegative()) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        extendSession(LocalDateTime.now().plus(duration));
    }
    
    public long getSessionDurationMinutes() {
        if (createdAt == null) return 0;
        
        LocalDateTime endTime = loggedOutAt != null ? loggedOutAt : LocalDateTime.now();
        return java.time.Duration.between(createdAt, endTime).toMinutes();
    }
}
