package com.bookverse.CartOrderManagement.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wishlist_items")
@Data
@NoArgsConstructor
public class WishlistItem {
    
    @Id
    private String id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "book_id")
    private String bookId;
    
    @Column(name = "price_when_added", precision = 10, scale = 2)
    private BigDecimal priceWhenAdded;
    
    @Column(name = "notify_on_sale")
    private Boolean notifyOnSale;
    
    @CreationTimestamp
    @Column(name = "added_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime addedAt;
    
    @PrePersist
    protected void onCreate() {
        if (addedAt == null) {
            addedAt = LocalDateTime.now();
        }
    }
}