package com.bookverse.CartOrderManagement.repository;

import com.bookverse.CartOrderManagement.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findAllByOrderByCreatedAtDesc();
    
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.orderItems " +
           "LEFT JOIN FETCH o.statusHistory " +
           "WHERE o.userId = :userId " +
           "ORDER BY o.createdAt DESC")
    List<Order> findByUserIdOrderByCreatedAtDesc(@Param("userId") String userId);
    
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.orderItems " +
           "LEFT JOIN FETCH o.statusHistory " +
           "ORDER BY o.createdAt DESC")
    List<Order> findAllWithOrderItemsByOrderByCreatedAtDesc();
    
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems " +
            "LEFT JOIN FETCH o.statusHistory " +
            "WHERE o.userId = :userId ORDER BY o.createdAt DESC")
    List<Order> findByUserIdWithDetails(@Param("userId") String userId);
    
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems " +
            "LEFT JOIN FETCH o.statusHistory " +
            "WHERE o.id = :orderId")
     Optional<Order> findByIdWithDetails(@Param("orderId") String orderId);
     
    
    List<Order> findByUserId(String userId);
    List<Order> findByOrderStatus(Order.OrderStatus orderStatus);
}