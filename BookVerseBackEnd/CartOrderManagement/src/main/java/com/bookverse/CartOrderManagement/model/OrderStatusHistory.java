package com.bookverse.CartOrderManagement.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_status_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusHistory {
    
    @Id
    private String id;
    
    @Column(name = "order_id", updatable = false, insertable = false)
    private String orderId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status")
    private Order.OrderStatus previousStatus;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "new_status")
    private Order.OrderStatus newStatus;
    
    private String reason;
    
    @Column(name = "updated_by")
    private String updatedBy;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Order order;
}