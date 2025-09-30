package com.bookverse.CartOrderManagement.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@Schema(description = "Cart item entity")
public class CartItem {
    
    @Id
    @Schema(description = "Cart item ID", example = "cart_123")
    private String id;
    
    @Column(name = "user_id", nullable = false)
    @Schema(description = "User ID", example = "user_123")
    private String userId;
    
    @Column(name = "book_id")
    @Schema(description = "Book ID", example = "book_456")
    private String bookId;
    
    @Column(nullable = false)
    @Schema(description = "Quantity", example = "2")
    private Integer quantity;
    
    @Column(name = "price_when_added", precision = 10, scale = 2)
    @Schema(description = "Price when added", example = "29.99")
    private BigDecimal priceWhenAdded;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false,updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}