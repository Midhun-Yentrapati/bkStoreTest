package com.bookverse.CartOrderManagement.service;

import com.bookverse.CartOrderManagement.dto.PaymentDto;
import com.bookverse.CartOrderManagement.exception.ItemNotFoundException;
import com.bookverse.CartOrderManagement.model.Payment;
import com.bookverse.CartOrderManagement.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public List<Payment> getPaymentsByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    public Payment getPaymentById(String paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ItemNotFoundException("Payment not found"));
    }

    public Payment createPayment(PaymentDto paymentDto) {
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID().toString());
        payment.setOrderId(paymentDto.getOrderId());
        payment.setTransactionId(paymentDto.getTransactionId());
        payment.setAmount(paymentDto.getAmount());
        payment.setCurrency(paymentDto.getCurrency());
        payment.setPaymentStatus(Payment.PaymentStatus.Pending);
        payment.setPaymentGateway(paymentDto.getPaymentGateway());
        payment.setPaymentMethod(paymentDto.getPaymentMethod());
        payment.setGatewayResponse(paymentDto.getGatewayResponse());
        
        return paymentRepository.save(payment);
    }

    public Payment updatePaymentStatus(String paymentId, Payment.PaymentStatus status) {
        Payment payment = getPaymentById(paymentId);
        payment.setPaymentStatus(status);
        return paymentRepository.save(payment);
    }
}