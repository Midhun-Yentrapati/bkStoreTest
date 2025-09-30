package com.bookverse.CartOrderManagement.controller;

import com.bookverse.CartOrderManagement.dto.WishlistItemDto;
import com.bookverse.CartOrderManagement.model.WishlistItem;
import com.bookverse.CartOrderManagement.service.WishlistService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@Tag(name = "Wishlist Management", description = "Operations for managing user wishlists")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<WishlistItem>> getWishlistItems(@PathVariable String userId) {
        return ResponseEntity.ok(wishlistService.getWishlistItems(userId));
    }

    @PostMapping
    public ResponseEntity<WishlistItem> addToWishlist(@RequestBody WishlistItemDto wishlistItemDto) {
        return ResponseEntity.ok(wishlistService.addToWishlist(wishlistItemDto));
    }

    @DeleteMapping("/{wishlistItemId}")
    public ResponseEntity<Void> removeFromWishlist(@PathVariable String wishlistItemId) {
        wishlistService.removeFromWishlist(wishlistItemId);
        return ResponseEntity.ok().build();
    }
}