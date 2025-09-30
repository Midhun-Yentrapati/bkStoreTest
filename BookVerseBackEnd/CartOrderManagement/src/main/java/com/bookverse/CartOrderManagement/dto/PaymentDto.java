package com.bookverse.CartOrderManagement.dto;

import com.bookverse.CartOrderManagement.model.Payment;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Payment data transfer object")
public class PaymentDto {
    @Schema(description = "Payment ID", example = "PAY-123")
    private String id;
    @Schema(description = "Order ID", example = "ORD-456", required = true)
    private String orderId;
    @Schema(description = "Transaction ID", example = "TXN-789")
    private String transactionId;
    @Schema(description = "Payment amount", example = "109.99", required = true)
    private BigDecimal amount;
    @Schema(description = "Currency code", example = "USD")
    private String currency;
    private Payment.PaymentStatus paymentStatus;
    @Schema(description = "Payment gateway", example = "STRIPE")
    private String paymentGateway;
    private String paymentMethod;
    private String gatewayResponse;

    public PaymentDto() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Payment.PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(Payment.PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getPaymentGateway() { return paymentGateway; }
    public void setPaymentGateway(String paymentGateway) { this.paymentGateway = paymentGateway; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getGatewayResponse() { return gatewayResponse; }
    public void setGatewayResponse(String gatewayResponse) { this.gatewayResponse = gatewayResponse; }
}