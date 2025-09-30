package com.bookverse.CartOrderManagement.dto;

import com.bookverse.CartOrderManagement.model.OrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Order item data transfer object")
public class OrderItemDto {
    @Schema(description = "Order item ID", example = "ITEM-123")
    private String id;
    private String orderId;
    @Schema(description = "Book ID", example = "BOOK-456")
    private String bookId;
    private String title;
    private String author;
    private BigDecimal price;
    @Schema(description = "Quantity ordered", example = "2")
    private Integer quantity;
    private BigDecimal subtotal;
    private String imageUrl;
    private OrderItem.ItemStatus itemStatus;
}