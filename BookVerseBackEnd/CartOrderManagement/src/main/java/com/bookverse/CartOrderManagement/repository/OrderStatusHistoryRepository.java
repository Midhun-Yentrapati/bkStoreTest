package com.bookverse.CartOrderManagement.repository;

import com.bookverse.CartOrderManagement.model.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, String> {
    
    @Query("SELECT osh FROM OrderStatusHistory osh WHERE osh.orderId = :orderId ORDER BY osh.createdAt ASC")
    List<OrderStatusHistory> findByOrderIdOrderByCreatedAtAsc(@Param("orderId") String orderId);
    
    @Query("SELECT osh FROM OrderStatusHistory osh WHERE osh.orderId = :orderId ORDER BY osh.createdAt DESC")
    List<OrderStatusHistory> findByOrderIdOrderByCreatedAtDesc(@Param("orderId") String orderId);
    
    List<OrderStatusHistory> findByOrderId(String orderId);
} 