package com.bookverse.CartOrderManagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Wishlist item data transfer object")
public class WishlistItemDto {
    @Schema(description = "Wishlist item ID", example = "wishlist_123")
    private String id;
    @Schema(description = "User ID", example = "user_123", required = true)
    private String userId;
    @Schema(description = "Book ID", example = "book_456", required = true)
    private String bookId;
    @Schema(description = "Price when added to wishlist", example = "29.99")
    private BigDecimal priceWhenAdded;
    @Schema(description = "Notify when item goes on sale", example = "true")
    private Boolean notifyOnSale;

    public WishlistItemDto() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public BigDecimal getPriceWhenAdded() { return priceWhenAdded; }
    public void setPriceWhenAdded(BigDecimal priceWhenAdded) { this.priceWhenAdded = priceWhenAdded; }

    public Boolean getNotifyOnSale() { return notifyOnSale; }
    public void setNotifyOnSale(Boolean notifyOnSale) { this.notifyOnSale = notifyOnSale; }
}