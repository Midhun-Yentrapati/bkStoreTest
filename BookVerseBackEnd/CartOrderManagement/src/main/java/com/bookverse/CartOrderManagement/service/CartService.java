package com.bookverse.CartOrderManagement.service;

import com.bookverse.CartOrderManagement.dto.CartItemDto;
import com.bookverse.CartOrderManagement.exception.ItemNotFoundException;
import com.bookverse.CartOrderManagement.exception.WishlistToCartException;
import com.bookverse.CartOrderManagement.model.CartItem;
import com.bookverse.CartOrderManagement.model.WishlistItem;
import com.bookverse.CartOrderManagement.repository.CartItemRepository;
import com.bookverse.CartOrderManagement.repository.WishlistItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final WishlistItemRepository wishlistItemRepository;

    public CartService(CartItemRepository cartItemRepository, WishlistItemRepository wishlistItemRepository) {
        this.cartItemRepository = cartItemRepository;
        this.wishlistItemRepository = wishlistItemRepository;
    }

    public List<CartItem> getCartItems(String userId) {
        return cartItemRepository.findByUserId(userId);
    }

    public CartItem addToCart(CartItemDto cartItemDto) {
        Optional<CartItem> existingItem = cartItemRepository.findByUserIdAndBookId(
                cartItemDto.getUserId(), cartItemDto.getBookId());
        
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + cartItemDto.getQuantity());
            return cartItemRepository.save(item);
        }
        
        CartItem cartItem = new CartItem();
        cartItem.setId(java.util.UUID.randomUUID().toString());
        cartItem.setUserId(cartItemDto.getUserId());
        cartItem.setBookId(cartItemDto.getBookId());
        cartItem.setQuantity(cartItemDto.getQuantity());
        cartItem.setPriceWhenAdded(cartItemDto.getPriceWhenAdded());
        // Explicitly set timestamps to ensure they are not null
        cartItem.setCreatedAt(java.time.LocalDateTime.now());
        cartItem.setUpdatedAt(java.time.LocalDateTime.now());
        
        return cartItemRepository.save(cartItem);
    }

    @Transactional
    public CartItem moveFromWishlistToCart(String userId, String bookId, Integer quantity) {
        Optional<WishlistItem> wishlistItem = wishlistItemRepository.findByUserIdAndBookId(userId, bookId);
        
        if (wishlistItem.isEmpty()) {
            throw new WishlistToCartException("Item not found in wishlist");
        }
        
        WishlistItem item = wishlistItem.get();
        
        CartItemDto cartItemDto = new CartItemDto();
        cartItemDto.setUserId(userId);
        cartItemDto.setBookId(bookId);
        cartItemDto.setQuantity(quantity);
        cartItemDto.setPriceWhenAdded(item.getPriceWhenAdded());
        
        CartItem cartItem = addToCart(cartItemDto);
        
        // Remove from wishlist after successfully adding to cart
        wishlistItemRepository.deleteByUserIdAndBookId(userId, bookId);
        
        return cartItem;
    }

    public CartItem updateCartItem(String cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ItemNotFoundException("Cart item not found"));
        
        cartItem.setQuantity(quantity);
        return cartItemRepository.save(cartItem);
    }

    @Transactional
    public void removeFromCart(String cartItemId) {
        if (!cartItemRepository.existsById(cartItemId)) {
            throw new ItemNotFoundException("Cart item not found");
        }
        cartItemRepository.deleteById(cartItemId);
    }

    @Transactional
    public void clearCart(String userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        cartItemRepository.deleteAll(cartItems);
    }
}