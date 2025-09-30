package com.bookstore.user_authentication_service.dto;

import com.bookstore.user_authentication_service.entity.AddressType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddressDTO {
    
    private String id;
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Please provide a valid phone number")
    private String phone;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Please provide a valid alternate phone number")
    private String alternatePhone;
    
    @Email(message = "Please provide a valid email address")
    private String email;
    
    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^[0-9]{5,10}$", message = "Please provide a valid pincode")
    private String pincode;
    
    @NotBlank(message = "Address line 1 is required")
    @Size(min = 5, max = 200, message = "Address line 1 must be between 5 and 200 characters")
    private String addressLine1;
    
    @Size(max = 200, message = "Address line 2 cannot exceed 200 characters")
    private String addressLine2;
    
    @Size(max = 100, message = "Landmark cannot exceed 100 characters")
    private String landmark;
    
    @NotBlank(message = "City is required")
    @Size(min = 2, max = 50, message = "City must be between 2 and 50 characters")
    private String city;
    
    @NotBlank(message = "State is required")
    @Size(min = 2, max = 50, message = "State must be between 2 and 50 characters")
    private String state;
    
    @NotBlank(message = "Country is required")
    @Size(min = 2, max = 50, message = "Country must be between 2 and 50 characters")
    private String country;
    
    private AddressType addressType;
    
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double latitude;
    
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double longitude;
    
    private Boolean isDefault;
    private Boolean isActive;
    
    @Size(max = 500, message = "Instructions cannot exceed 500 characters")
    private String instructions;
    
    @Size(max = 20, message = "Access code cannot exceed 20 characters")
    private String accessCode;
    
    // Audit Fields
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // User ID for relationship
    private String userId;
    
    // Computed Fields
    private String fullAddress;
    private String shortAddress;
    private Boolean hasCoordinates;
}
