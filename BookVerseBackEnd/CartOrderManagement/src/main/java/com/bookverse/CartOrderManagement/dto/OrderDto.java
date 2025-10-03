package com.bookverse.CartOrderManagement.dto;

import com.bookverse.CartOrderManagement.model.Order;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Order data transfer object")
public class OrderDto {
    @Schema(description = "Order ID", example = "ORD-123")
    private String id;
    @Schema(description = "User ID", example = "USER-456", required = true)
    private String userId;
    @Schema(description = "Customer name", example = "John Doe")
    private String customerName;
    @Schema(description = "Customer email", example = "john@example.com")
    private String customerEmail;
    @Schema(description = "Billing address ID", example = "ADDR-789")
    private String billingAddressId;
    @Schema(description = "Shipping address ID", example = "ADDR-790")
    private String shippingAddressId;
    @Schema(description = "Order subtotal", example = "99.99")
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    @Schema(description = "Coupon code", example = "SAVE10")
    private String couponCode;

    private BigDecimal taxAmount;
    private BigDecimal shippingAmount;
    private BigDecimal platformFee;
    @Schema(description = "Order grand total", example = "109.99")
    private BigDecimal grandTotal;
    private String currency;
    private Order.PaymentMethod paymentMethod;
    private Order.PaymentStatus paymentStatus;
    private Order.OrderStatus orderStatus;
    private String trackingId;
    private String notes;
    @Schema(description = "List of order items")
    private List<OrderItemDto> orderItems;
    
    // Timestamp fields
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
    @Schema(description = "Order placed timestamp")
    private LocalDateTime placedAt;
    @Schema(description = "Payment completed timestamp")
    private LocalDateTime paidAt;
    @Schema(description = "Order shipped timestamp")
    private LocalDateTime shippedAt;
    @Schema(description = "Order delivered timestamp")
    private LocalDateTime deliveredAt;
    @Schema(description = "Order cancelled timestamp")
    private LocalDateTime cancelledAt;
    
    // Status history
    @Schema(description = "Order status history")
    private List<OrderStatusHistoryDto> statusHistory;

    public OrderDto() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getBillingAddressId() { return billingAddressId; }
    public void setBillingAddressId(String billingAddressId) { this.billingAddressId = billingAddressId; }

    public String getShippingAddressId() { return shippingAddressId; }
    public void setShippingAddressId(String shippingAddressId) { this.shippingAddressId = shippingAddressId; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }


    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getShippingAmount() { return shippingAmount; }
    public void setShippingAmount(BigDecimal shippingAmount) { this.shippingAmount = shippingAmount; }

    public BigDecimal getPlatformFee() { return platformFee; }
    public void setPlatformFee(BigDecimal platformFee) { this.platformFee = platformFee; }

    public BigDecimal getGrandTotal() { return grandTotal; }
    public void setGrandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Order.PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(Order.PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public Order.PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(Order.PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public Order.OrderStatus getOrderStatus() { return orderStatus; }
    public void setOrderStatus(Order.OrderStatus orderStatus) { this.orderStatus = orderStatus; }

    public String getTrackingId() { return trackingId; }
    public void setTrackingId(String trackingId) { this.trackingId = trackingId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<OrderItemDto> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItemDto> orderItems) { this.orderItems = orderItems; }
    
    // Timestamp getters and setters
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getPlacedAt() { return placedAt; }
    public void setPlacedAt(LocalDateTime placedAt) { this.placedAt = placedAt; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public LocalDateTime getShippedAt() { return shippedAt; }
    public void setShippedAt(LocalDateTime shippedAt) { this.shippedAt = shippedAt; }

    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }

    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    
    // Status history getter and setter
    public List<OrderStatusHistoryDto> getStatusHistory() { return statusHistory; }
    public void setStatusHistory(List<OrderStatusHistoryDto> statusHistory) { this.statusHistory = statusHistory; }
}