package com.bookverse.CartOrderManagement.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
public class Payment {
    
    @Id
    private String id;
    
    @Column(name = "order_id", nullable = false)
    private String orderId;
    
    @Column(name = "transaction_id", unique = true)
    private String transactionId;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal amount;
    
    private String currency;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;
    
    @Column(name = "payment_gateway")
    private String paymentGateway;
    
    @Column(name = "payment_method")
    private String paymentMethod;
    
    @Column(name = "refunded_amount", precision = 10, scale = 2)
    private BigDecimal refundedAmount;
    
    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;
    
    @Column(name = "gateway_response", columnDefinition = "JSON")
    private String gatewayResponse;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order order;
    
    public enum PaymentStatus {
        Pending, Paid, Failed, Refunded
    }
}