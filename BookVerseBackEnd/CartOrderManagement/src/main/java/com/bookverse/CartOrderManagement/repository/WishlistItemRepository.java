package com.bookverse.CartOrderManagement.repository;

import com.bookverse.CartOrderManagement.model.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, String> {
    List<WishlistItem> findByUserId(String userId);
    Optional<WishlistItem> findByUserIdAndBookId(String userId, String bookId);
    void deleteByUserIdAndBookId(String userId, String bookId);
}