package com.bookverse.CartOrderManagement.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemDto {
    
    @NotBlank(message = "User ID is required")
    @Size(max = 255, message = "User ID must not exceed 255 characters")
    private String userId;
    
    @NotBlank(message = "Book ID is required")
    @Size(max = 255, message = "Book ID must not exceed 255 characters")
    private String bookId;
    
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @Max(value = 100, message = "Quantity cannot exceed 100")
    private Integer quantity;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
    private BigDecimal priceWhenAdded;
}