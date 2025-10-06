package com.bookverse.CartOrderManagement.controller;

import com.bookverse.CartOrderManagement.dto.WishlistItemDto;
import com.bookverse.CartOrderManagement.model.WishlistItem;
import com.bookverse.CartOrderManagement.service.WishlistService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@Tag(name = "Wishlist Management", description = "Operations for managing user wishlists")
@Slf4j
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Wishlist service is working!");
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<WishlistItem>> getWishlistItems(@PathVariable String userId) {
        try {
            log.info("Fetching wishlist items for user: {}", userId);
            List<WishlistItem> items = wishlistService.getWishlistItems(userId);
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            log.error("Error fetching wishlist items for user {}: {}", userId, e.getMessage());
            throw e;
        }
    }

    @PostMapping
    public ResponseEntity<WishlistItem> addToWishlist(@RequestBody WishlistItemDto wishlistItemDto) {
        try {
            log.info("Adding item to wishlist for user: {}, book: {}", 
                    wishlistItemDto.getUserId(), wishlistItemDto.getBookId());
            WishlistItem item = wishlistService.addToWishlist(wishlistItemDto);
            return ResponseEntity.ok(item);
        } catch (Exception e) {
            log.error("Error adding item to wishlist: {}", e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/{wishlistItemId}")
    public ResponseEntity<Void> removeFromWishlist(@PathVariable String wishlistItemId) {
        try {
            log.info("Removing item from wishlist: {}", wishlistItemId);
            wishlistService.removeFromWishlist(wishlistItemId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error removing item from wishlist {}: {}", wishlistItemId, e.getMessage());
            throw e;
        }
    }
}