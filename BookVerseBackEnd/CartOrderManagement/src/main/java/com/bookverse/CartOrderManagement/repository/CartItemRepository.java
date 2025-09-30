package com.bookverse.CartOrderManagement.repository;

import com.bookverse.CartOrderManagement.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, String> {
    List<CartItem> findByUserId(String userId);
    Optional<CartItem> findByUserIdAndBookId(String userId, String bookId);
    void deleteByUserIdAndBookId(String userId, String bookId);
}