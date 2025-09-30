package com.bookverse.CartOrderManagement.controller;

import com.bookverse.CartOrderManagement.model.Order;
import com.bookverse.CartOrderManagement.model.Payment;
import com.bookverse.CartOrderManagement.repository.OrderRepository;
import com.bookverse.CartOrderManagement.repository.PaymentRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test-data")
public class TestDataController {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    public TestDataController(OrderRepository orderRepository, PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
    }

    @GetMapping("/ids")
    public Map<String, Object> getTestIds() {
        Map<String, Object> testData = new HashMap<>();
        
        List<Order> orders = orderRepository.findAll();
        List<Payment> payments = paymentRepository.findAll();
        
        testData.put("orderIds", orders.stream().map(Order::getId).limit(5).toList());
        testData.put("userIds", orders.stream().map(Order::getUserId).distinct().limit(3).toList());
        testData.put("paymentIds", payments.stream().map(Payment::getId).limit(5).toList());
        testData.put("totalOrders", orders.size());
        testData.put("totalPayments", payments.size());
        
        return testData;
    }
}