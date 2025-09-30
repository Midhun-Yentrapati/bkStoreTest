package com.bookverse.CartOrderManagement.service;

import com.bookverse.CartOrderManagement.dto.WishlistItemDto;
import com.bookverse.CartOrderManagement.exception.ItemNotFoundException;
import com.bookverse.CartOrderManagement.model.WishlistItem;
import com.bookverse.CartOrderManagement.repository.WishlistItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class WishlistService {
    
    private final WishlistItemRepository wishlistItemRepository;

    public WishlistService(WishlistItemRepository wishlistItemRepository) {
        this.wishlistItemRepository = wishlistItemRepository;
    }

    public List<WishlistItem> getWishlistItems(String userId) {
        return wishlistItemRepository.findByUserId(userId);
    }

    public WishlistItem addToWishlist(WishlistItemDto wishlistItemDto) {
        Optional<WishlistItem> existingItem = wishlistItemRepository.findByUserIdAndBookId(
                wishlistItemDto.getUserId(), wishlistItemDto.getBookId());
        
        if (existingItem.isPresent()) {
            return existingItem.get();
        }
        
        WishlistItem wishlistItem = new WishlistItem();
        wishlistItem.setId(java.util.UUID.randomUUID().toString());
        wishlistItem.setUserId(wishlistItemDto.getUserId());
        wishlistItem.setBookId(wishlistItemDto.getBookId());
        wishlistItem.setPriceWhenAdded(wishlistItemDto.getPriceWhenAdded());
        wishlistItem.setNotifyOnSale(wishlistItemDto.getNotifyOnSale());
        // Explicitly set timestamp to ensure it is not null
        wishlistItem.setAddedAt(java.time.LocalDateTime.now());
        
        return wishlistItemRepository.save(wishlistItem);
    }

    @Transactional
    public void removeFromWishlist(String wishlistItemId) {
        if (!wishlistItemRepository.existsById(wishlistItemId)) {
            throw new ItemNotFoundException("Wishlist item not found");
        }
        wishlistItemRepository.deleteById(wishlistItemId);
    }
}