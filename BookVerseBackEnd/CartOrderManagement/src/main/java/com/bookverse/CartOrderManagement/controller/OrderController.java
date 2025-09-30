package com.bookverse.CartOrderManagement.controller;

import com.bookverse.CartOrderManagement.dto.OrderDto;
import com.bookverse.CartOrderManagement.model.Order;
import com.bookverse.CartOrderManagement.service.OrderService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> getOrders(@RequestParam(required = false) String userId) {
        if (userId != null && !userId.isEmpty()) {
            return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
        } else {
            // Return all orders (for admin users)
            return ResponseEntity.ok(orderService.getAllOrders());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDto>> getOrdersByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderDto orderDto) {
        return ResponseEntity.ok(orderService.createOrder(orderDto));
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable String orderId,
            @RequestParam Order.OrderStatus status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
    }

    @PutMapping("/{orderId}/payment-status")
    public ResponseEntity<OrderDto> updatePaymentStatus(
            @PathVariable String orderId,
            @RequestParam Order.PaymentStatus status) {
        return ResponseEntity.ok(orderService.updatePaymentStatus(orderId, status));
    }

    @PutMapping("/{orderId}/cancel")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> cancelOrder(@PathVariable String orderId) {
        try {
            System.out.println("Cancel order request received for orderId: " + orderId);
            OrderDto cancelledOrder = orderService.cancelOrder(orderId);
            System.out.println("Order cancelled successfully: " + cancelledOrder.getId());
            return ResponseEntity.ok(cancelledOrder);
        } catch (IllegalStateException e) {
            System.out.println("Cancel order failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{orderId}")
    public ResponseEntity<OrderDto> deleteOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }
}