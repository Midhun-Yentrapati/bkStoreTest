package com.bookverse.CartOrderManagement.dto;

import com.bookverse.CartOrderManagement.model.Order;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Order status history data transfer object")
public class OrderStatusHistoryDto {

    @Schema(description = "Status history ID", example = "HIST-123")
    private String id;
    
    @Schema(description = "Order ID", example = "ORD-456")
    private String orderId;
    
    @Schema(description = "Previous status")
    private Order.OrderStatus previousStatus;
    
    @Schema(description = "New status")
    private Order.OrderStatus newStatus;
    
    @Schema(description = "Status as string for compatibility")
    private String status;
    
    @Schema(description = "Reason for status change")
    private String reason;
    
    @Schema(description = "User who updated the status")
    private String updatedBy;
    
    @Schema(description = "Additional notes")
    private String notes;
    
    @Schema(description = "Timestamp when status was changed")
    private LocalDateTime timestamp;
    
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    // Constructors
    public OrderStatusHistoryDto() {}

    public OrderStatusHistoryDto(String id, String orderId, Order.OrderStatus previousStatus, 
                               Order.OrderStatus newStatus, String reason, String updatedBy, 
                               String notes, LocalDateTime createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.status = newStatus != null ? newStatus.toString() : null;
        this.reason = reason;
        this.updatedBy = updatedBy;
        this.notes = notes;
        this.timestamp = createdAt;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Order.OrderStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(Order.OrderStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public Order.OrderStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(Order.OrderStatus newStatus) {
        this.newStatus = newStatus;
        this.status = newStatus != null ? newStatus.toString() : null;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        this.timestamp = createdAt; // Keep timestamp in sync
    }
}