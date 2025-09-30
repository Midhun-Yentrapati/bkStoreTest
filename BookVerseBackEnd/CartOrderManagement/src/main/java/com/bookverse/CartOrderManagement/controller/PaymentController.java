package com.bookverse.CartOrderManagement.controller;

import com.bookverse.CartOrderManagement.dto.PaymentDto;
import com.bookverse.CartOrderManagement.model.Payment;
import com.bookverse.CartOrderManagement.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payment Management", description = "APIs for managing payments and payment processing")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(summary = "Get payments by order ID", description = "Retrieve all payments associated with a specific order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payments retrieved successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Payment.class))),
        @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
    })
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Payment>> getPaymentsByOrderId(
            @Parameter(description = "Order ID to retrieve payments for", required = true)
            @PathVariable String orderId) {
        return ResponseEntity.ok(paymentService.getPaymentsByOrderId(orderId));
    }

    @Operation(summary = "Get payment by ID", description = "Retrieve a specific payment by its unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment retrieved successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Payment.class))),
        @ApiResponse(responseCode = "404", description = "Payment not found", content = @Content)
    })
    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> getPaymentById(
            @Parameter(description = "Payment ID to retrieve", required = true)
            @PathVariable String paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentById(paymentId));
    }

    @Operation(summary = "Create new payment", description = "Process a new payment for an order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment created successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Payment.class))),
        @ApiResponse(responseCode = "400", description = "Invalid payment data", content = @Content),
        @ApiResponse(responseCode = "500", description = "Payment processing failed", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Payment> createPayment(
            @Parameter(description = "Payment details", required = true)
            @RequestBody PaymentDto paymentDto) {
        return ResponseEntity.ok(paymentService.createPayment(paymentDto));
    }

    @Operation(summary = "Update payment status", description = "Update the status of an existing payment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment status updated successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Payment.class))),
        @ApiResponse(responseCode = "404", description = "Payment not found", content = @Content),
        @ApiResponse(responseCode = "400", description = "Invalid status transition", content = @Content)
    })
    @PutMapping("/{paymentId}/status")
    public ResponseEntity<Payment> updatePaymentStatus(
            @Parameter(description = "Payment ID to update", required = true)
            @PathVariable String paymentId,
            @Parameter(description = "New payment status", required = true)
            @RequestParam Payment.PaymentStatus status) {
        return ResponseEntity.ok(paymentService.updatePaymentStatus(paymentId, status));
    }
}