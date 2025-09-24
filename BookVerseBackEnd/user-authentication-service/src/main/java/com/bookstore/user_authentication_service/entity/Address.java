package com.bookstore.user_authentication_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Address {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    private String name;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Please provide a valid phone number")
    @Column(nullable = false, length = 20)
    private String phone;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Please provide a valid alternate phone number")
    @Column(name = "alternate_phone", length = 20)
    private String alternatePhone;
    
    @Email(message = "Please provide a valid email address")
    @Column(length = 100)
    private String email;
    
    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^[0-9]{5,10}$", message = "Please provide a valid pincode")
    @Column(nullable = false, length = 10)
    private String pincode;
    
    @NotBlank(message = "Address line 1 is required")
    @Size(min = 5, max = 200, message = "Address line 1 must be between 5 and 200 characters")
    @Column(name = "address_line1", nullable = false, length = 200)
    private String addressLine1;
    
    @Size(max = 200, message = "Address line 2 cannot exceed 200 characters")
    @Column(name = "address_line2", length = 200)
    private String addressLine2;
    
    @Size(max = 100, message = "Landmark cannot exceed 100 characters")
    @Column(length = 100)
    private String landmark;
    
    @NotBlank(message = "City is required")
    @Size(min = 2, max = 50, message = "City must be between 2 and 50 characters")
    @Column(nullable = false, length = 50)
    private String city;
    
    @NotBlank(message = "State is required")
    @Size(min = 2, max = 50, message = "State must be between 2 and 50 characters")
    @Column(nullable = false, length = 50)
    private String state;
    
    @NotBlank(message = "Country is required")
    @Size(min = 2, max = 50, message = "Country must be between 2 and 50 characters")
    @Column(nullable = false, length = 50)
    private String country;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false)
    @Builder.Default
    private AddressType addressType = AddressType.HOME;
    
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    @Column(name = "latitude")
    private Double latitude;
    
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
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
    public String getFullAddress() {
        StringBuilder fullAddress = new StringBuilder();
        fullAddress.append(addressLine1);
        
        if (addressLine2 != null && !addressLine2.trim().isEmpty()) {
            fullAddress.append(", ").append(addressLine2);
        }
        
        if (landmark != null && !landmark.trim().isEmpty()) {
            fullAddress.append(", ").append(landmark);
        }
        
        fullAddress.append(", ").append(city)
                  .append(", ").append(state)
                  .append(" - ").append(pincode)
                  .append(", ").append(country);
        
        return fullAddress.toString();
    }
    
    public String getShortAddress() {
        return addressLine1 + ", " + city + ", " + state + " - " + pincode;
    }
    
    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }
}
