package com.bookverse.bookCatalog.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "INVENTORY_ALERTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Books book;
    
    @Enumerated(EnumType.STRING)
    private AlertType alertType;
    
    private int thresholdValue;
    private int currentStock;
    private boolean isResolved = false;
    private String resolvedBy;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;

    public enum AlertType {
        LOW_STOCK, OUT_OF_STOCK, OVER_STOCK
    }
    
    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}