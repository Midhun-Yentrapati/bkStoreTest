package com.bookverse.CartOrderManagement.service;

import com.bookverse.CartOrderManagement.dto.OrderDto;
import com.bookverse.CartOrderManagement.dto.OrderItemDto;
import com.bookverse.CartOrderManagement.dto.OrderStatusHistoryDto;
import com.bookverse.CartOrderManagement.dto.PaymentDto;
import com.bookverse.CartOrderManagement.exception.ItemNotFoundException;
import com.bookverse.CartOrderManagement.model.Order;
import com.bookverse.CartOrderManagement.model.OrderItem;
import com.bookverse.CartOrderManagement.model.OrderStatusHistory;
import com.bookverse.CartOrderManagement.model.CartItem;
import com.bookverse.CartOrderManagement.model.Payment;

import com.bookverse.CartOrderManagement.repository.OrderRepository;
import com.bookverse.CartOrderManagement.repository.OrderItemRepository;
import com.bookverse.CartOrderManagement.repository.OrderStatusHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final CartService cartService;
    private final PaymentService paymentService;
    private final CouponService couponService;
    
    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, 
                       OrderStatusHistoryRepository statusHistoryRepository,
                       CartService cartService, PaymentService paymentService, CouponService couponService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.cartService = cartService;
        this.paymentService = paymentService;
        this.couponService = couponService;
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getAllOrders() {
        List<Order> orders = orderRepository.findAllWithOrderItemsByOrderByCreatedAtDesc();
        System.out.println("Found " + orders.size() + " total orders");
        return orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByUserId(String userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        System.out.println("Found " + orders.size() + " orders for user: " + userId);
        return orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public Order createOrder(OrderDto orderDto) {
        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        order.setUserId(orderDto.getUserId());
        order.setBillingAddressId(orderDto.getBillingAddressId());
        order.setShippingAddressId(orderDto.getShippingAddressId());
        order.setSubtotal(orderDto.getSubtotal() != null ? orderDto.getSubtotal() : orderDto.getGrandTotal());
        
        // Handle coupon application
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (orderDto.getCouponCode() != null && !orderDto.getCouponCode().trim().isEmpty()) {
            if (couponService.validateCoupon(orderDto.getCouponCode(), orderDto.getSubtotal(), orderDto.getUserId())) {
                discountAmount = couponService.calculateDiscount(orderDto.getCouponCode(), orderDto.getSubtotal());
                order.setCouponId(orderDto.getCouponCode());
                couponService.applyCoupon(orderDto.getCouponCode());
            }
        }
        order.setDiscountAmount(discountAmount);

        order.setTaxAmount(orderDto.getTaxAmount() != null ? orderDto.getTaxAmount() : BigDecimal.ZERO);
        order.setShippingAmount(orderDto.getShippingAmount() != null ? orderDto.getShippingAmount() : BigDecimal.ZERO);
        order.setPlatformFee(orderDto.getPlatformFee() != null ? orderDto.getPlatformFee() : BigDecimal.ZERO);
        order.setGrandTotal(orderDto.getGrandTotal());
        order.setCurrency(orderDto.getCurrency() != null ? orderDto.getCurrency() : "INR");
        order.setPaymentMethod(orderDto.getPaymentMethod());
        order.setPaymentStatus(Order.PaymentStatus.Pending);
        order.setOrderStatus(Order.OrderStatus.Pending);
        order.setNotes(orderDto.getNotes());
        order.setPlacedAt(LocalDateTime.now());
        
        Order savedOrder = orderRepository.save(order);
        System.out.println("Order created with ID: " + savedOrder.getId() + ", GrandTotal: " + savedOrder.getGrandTotal());
        
        // Create initial status history
        createStatusHistory(savedOrder, null, Order.OrderStatus.Pending, "Order created", "SYSTEM");
        
        // Create payment record for the order
        try {
            createPaymentForOrder(savedOrder);
        } catch (Exception e) {
            System.out.println("Error creating payment: " + e.getMessage());
        }
        
        // Create order items from cart items
        try {
            createOrderItemsFromCart(savedOrder, orderDto.getUserId());
        } catch (Exception e) {
            System.out.println("Error creating order items: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Clear cart after successful order creation
        try {
            cartService.clearCart(orderDto.getUserId());
            System.out.println("Cart cleared for user: " + orderDto.getUserId());
        } catch (Exception e) {
            System.out.println("Error clearing cart: " + e.getMessage());
        }
        
        return savedOrder;
    }
    
    private void createOrderItemsFromCart(Order order, String userId) {
        // Get cart items for the user
        List<CartItem> cartItems = cartService.getCartItems(userId);
        System.out.println("Found " + cartItems.size() + " cart items for user: " + userId);
        
        // Create order items from cart items
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setId(UUID.randomUUID().toString());
            orderItem.setOrder(order);  // Set the order entity instead of just orderId
            orderItem.setBookId(cartItem.getBookId());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getPriceWhenAdded());
            orderItem.setSubtotal(cartItem.getPriceWhenAdded().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            orderItem.setItemStatus(OrderItem.ItemStatus.Pending);
            
            // Set placeholder book details - these should be fetched from book service in a real implementation
            orderItem.setTitle("Book #" + cartItem.getBookId());
            orderItem.setAuthor("Author");
            orderItem.setImageUrl("placeholder.jpg");
            
            OrderItem savedOrderItem = orderItemRepository.save(orderItem);
            System.out.println("Created order item: " + savedOrderItem.getId() + " for book: " + cartItem.getBookId());
        }
    }

    @Transactional
    public OrderDto updateOrderStatus(String orderId, Order.OrderStatus status) {
        // --- FIX IS HERE ---
        // Use findByIdWithDetails, which is designed to find a single order by its ID
        // and returns an Optional<Order>.
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ItemNotFoundException("Order not found with id: " + orderId));
        
        Order.OrderStatus previousStatus = order.getOrderStatus();
        order.setOrderStatus(status);
        
        // Update timestamp based on status
        LocalDateTime now = LocalDateTime.now();
        switch (status) {
            case Confirmed:
                // No specific timestamp field for confirmed
                break;
            case Shipped:
                order.setShippedAt(now);
                break;
            case Delivered:
                order.setDeliveredAt(now);
                break;
            case Cancelled:
                order.setCancelledAt(now);
                break;
		    case Pending:
		    	break;
		    default:
		    	break;
        }
        
        Order savedOrder = orderRepository.save(order);
        
        // Create status history record
        createStatusHistory(orderId, previousStatus, status, "Status updated", "ADMIN");
        
        return convertToDto(savedOrder);
    }

    @Transactional
    public OrderDto updatePaymentStatus(String orderId, Order.PaymentStatus status) {
        // This method is correct as is.
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ItemNotFoundException("Order not found with id: " + orderId));
        order.setPaymentStatus(status);
        
        if (status == Order.PaymentStatus.Paid) {
            order.setPaidAt(LocalDateTime.now());
        }
        
        Order savedOrder = orderRepository.save(order);
        return convertToDto(savedOrder);
    }

    @Transactional
    public OrderDto cancelOrder(String orderId) {
        // This method is correct as is.
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ItemNotFoundException("Order not found with id: " + orderId));
        
        // Check if order can be cancelled
        if (order.getOrderStatus() == Order.OrderStatus.Delivered) {
            throw new IllegalStateException("Cannot cancel a delivered order.");
        }
        if (order.getOrderStatus() == Order.OrderStatus.Cancelled) {
            throw new IllegalStateException("Order is already cancelled.");
        }
        if (order.getOrderStatus() == Order.OrderStatus.Shipped) {
            throw new IllegalStateException("Cannot cancel a shipped order.");
        }
        
        Order.OrderStatus previousStatus = order.getOrderStatus();
        
        // Cancel the order
        order.setOrderStatus(Order.OrderStatus.Cancelled);
        order.setCancelledAt(LocalDateTime.now());
        
        // If payment was made, update payment status to refunded
        if (order.getPaymentStatus() == Order.PaymentStatus.Paid) {
            order.setPaymentStatus(Order.PaymentStatus.Refunded);
        }
        
        Order savedOrder = orderRepository.save(order);
        
        // Create status history record
        createStatusHistory(orderId, previousStatus, Order.OrderStatus.Cancelled, "Order cancelled by user", "USER");
        
        return convertToDto(savedOrder);
    }
    
    private void createStatusHistory(String orderId, Order.OrderStatus previousStatus, 
                                   Order.OrderStatus newStatus, String reason, String updatedBy) {
        // Fetch the order entity to set the relationship properly
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ItemNotFoundException("Order not found with id: " + orderId));
        
        createStatusHistory(order, previousStatus, newStatus, reason, updatedBy);
    }
    
    private void createStatusHistory(Order order, Order.OrderStatus previousStatus, 
                                   Order.OrderStatus newStatus, String reason, String updatedBy) {
        OrderStatusHistory statusHistory = new OrderStatusHistory();
        statusHistory.setId(UUID.randomUUID().toString());
        statusHistory.setOrder(order);  // Set the order entity instead of just orderId
        statusHistory.setPreviousStatus(previousStatus);
        statusHistory.setNewStatus(newStatus);
        statusHistory.setReason(reason);
        statusHistory.setUpdatedBy(updatedBy);
        statusHistory.setNotes(reason);
        
        statusHistoryRepository.save(statusHistory);
        System.out.println("Created status history for order: " + order.getId() + " - " + previousStatus + " -> " + newStatus);
    }
    
    private void createPaymentForOrder(Order order) {
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setOrderId(order.getId());
        paymentDto.setAmount(order.getGrandTotal());
        paymentDto.setCurrency(order.getCurrency());
        paymentDto.setPaymentGateway("PENDING");
        paymentDto.setPaymentMethod(order.getPaymentMethod().toString());
        
        Payment payment = paymentService.createPayment(paymentDto);
        System.out.println("Payment created with ID: " + payment.getId() + " for order: " + order.getId());
    }
    
    // Entity to DTO conversion methods
    private OrderDto convertToDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setBillingAddressId(order.getBillingAddressId());
        dto.setShippingAddressId(order.getShippingAddressId());
        dto.setSubtotal(order.getSubtotal());
        dto.setDiscountAmount(order.getDiscountAmount());
        dto.setCouponCode(order.getCouponId()); // Map couponId to couponCode for frontend
        dto.setTaxAmount(order.getTaxAmount());
        dto.setShippingAmount(order.getShippingAmount());
        dto.setPlatformFee(order.getPlatformFee());
        dto.setGrandTotal(order.getGrandTotal());
        dto.setCurrency(order.getCurrency());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setTrackingId(order.getTrackingId());
        dto.setNotes(order.getNotes());
        
        // Set timestamps
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        dto.setPlacedAt(order.getPlacedAt());
        dto.setPaidAt(order.getPaidAt());
        dto.setShippedAt(order.getShippedAt());
        dto.setDeliveredAt(order.getDeliveredAt());
        dto.setCancelledAt(order.getCancelledAt());
        
        // Convert order items
        if (order.getOrderItems() != null) {
            List<OrderItemDto> orderItemDtos = order.getOrderItems().stream()
                    .map(this::convertOrderItemToDto)
                    .collect(Collectors.toList());
            dto.setOrderItems(orderItemDtos);
        }
        
        // Convert status history
        if (order.getStatusHistory() != null) {
            List<OrderStatusHistoryDto> statusHistoryDtos = order.getStatusHistory().stream()
                    .map(this::convertStatusHistoryToDto)
                    .collect(Collectors.toList());
            dto.setStatusHistory(statusHistoryDtos);
        }
        
        return dto;
    }
    
    private OrderItemDto convertOrderItemToDto(OrderItem orderItem) {
        OrderItemDto dto = new OrderItemDto();
        dto.setId(orderItem.getId());
        dto.setOrderId(orderItem.getOrder() != null ? orderItem.getOrder().getId() : null);
        dto.setBookId(orderItem.getBookId());
        dto.setTitle(orderItem.getTitle());
        dto.setAuthor(orderItem.getAuthor());
        dto.setPrice(orderItem.getPrice());
        dto.setQuantity(orderItem.getQuantity());
        dto.setSubtotal(orderItem.getSubtotal());
        dto.setImageUrl(orderItem.getImageUrl());
        dto.setItemStatus(orderItem.getItemStatus());
        return dto;
    }
    
    private OrderStatusHistoryDto convertStatusHistoryToDto(OrderStatusHistory statusHistory) {
        return new OrderStatusHistoryDto(
                statusHistory.getId(),
                statusHistory.getOrder() != null ? statusHistory.getOrder().getId() : null,
                statusHistory.getPreviousStatus(),
                statusHistory.getNewStatus(),
                statusHistory.getReason(),
                statusHistory.getUpdatedBy(),
                statusHistory.getNotes(),
                statusHistory.getCreatedAt()
        );
    }
    
    
    @Transactional(readOnly = true)
    public OrderDto getOrderById(String orderId) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ItemNotFoundException("Order not found with id: " + orderId));
        return convertToDto(order);
    }
}