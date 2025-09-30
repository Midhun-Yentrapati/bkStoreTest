package com.bookverse.CartOrderManagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Cart item data transfer object")
public class CartItemDto {
    @Schema(description = "Cart item ID", example = "cart_123")
    private String id;
    @Schema(description = "User ID who owns the cart", example = "user_123", required = true)
    private String userId;
    @Schema(description = "Book ID to add to cart", example = "book_456", required = true)
    private String bookId;
    @Schema(description = "Quantity of books", example = "2", required = true)
    private Integer quantity;
    @Schema(description = "Price when item was added to cart", example = "29.99", required = true)
    private BigDecimal priceWhenAdded;

    public CartItemDto() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getPriceWhenAdded() { return priceWhenAdded; }
    public void setPriceWhenAdded(BigDecimal priceWhenAdded) { this.priceWhenAdded = priceWhenAdded; }
}