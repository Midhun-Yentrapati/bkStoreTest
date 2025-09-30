package com.bookverse.CartOrderManagement.controller;

import com.bookverse.CartOrderManagement.dto.CartItemDto;
import com.bookverse.CartOrderManagement.model.CartItem;
import com.bookverse.CartOrderManagement.service.CartService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart Management", description = "Operations related to shopping cart management")
public class CartController {
    
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get cart items by path", description = "Retrieves all items in a user's shopping cart using path variable")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cart items retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<List<CartItem>> getCartItemsByPath(
            @Parameter(description = "User ID", required = true, example = "user_123") 
            @PathVariable String userId) {
        return ResponseEntity.ok(cartService.getCartItems(userId));
    }

    @GetMapping
    @Operation(summary = "Get cart items", description = "Retrieves all items in a user's shopping cart using query parameter")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cart items retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<List<CartItem>> getCartItems(
            @Parameter(description = "User ID", required = true, example = "user_123") 
            @RequestParam String userId) {
        return ResponseEntity.ok(cartService.getCartItems(userId));
    }

    @PostMapping
    public ResponseEntity<CartItem> addToCart(@RequestBody CartItemDto cartItemDto) {
        return ResponseEntity.ok(cartService.addToCart(cartItemDto));
    }

    @PostMapping("/move-from-wishlist")
    public ResponseEntity<CartItem> moveFromWishlistToCart(
            @RequestParam String userId,
            @RequestParam String bookId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.moveFromWishlistToCart(userId, bookId, quantity));
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<CartItem> updateCartItem(
            @PathVariable String cartItemId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.updateCartItem(cartItemId, quantity));
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable String cartItemId) {
        cartService.removeFromCart(cartItemId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/user/{userId}/clear")
    public ResponseEntity<Void> clearCart(@PathVariable String userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok().build();
    }
}