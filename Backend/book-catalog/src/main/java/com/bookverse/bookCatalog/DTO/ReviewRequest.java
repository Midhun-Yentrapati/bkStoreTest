package com.bookverse.bookCatalog.DTO;

import jakarta.validation.constraints.*;

public class ReviewRequest {
    
    @NotNull(message = "Book ID is required")
    private Long bookId;
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotBlank(message = "User name is required")
    @Size(max = 255, message = "User name must not exceed 255 characters")
    private String userName;
    
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;
    
    @Size(max = 1000, message = "Review text must not exceed 1000 characters")
    private String comment;
    
    private String orderItemId;
    
    private Boolean isVerifiedPurchase = false;
    
    // Constructors
    public ReviewRequest() {}
    
    public ReviewRequest(Long bookId, String userId, String userName, Integer rating, String comment) {
        this.bookId = bookId;
        this.userId = userId;
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
    }
    
    // Getters and Setters
    public Long getBookId() {
        return bookId;
    }
    
    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public Integer getRating() {
        return rating;
    }
    
    public void setRating(Integer rating) {
        this.rating = rating;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public String getOrderItemId() {
        return orderItemId;
    }
    
    public void setOrderItemId(String orderItemId) {
        this.orderItemId = orderItemId;
    }
    
    public Boolean getIsVerifiedPurchase() {
        return isVerifiedPurchase;
    }
    
    public void setIsVerifiedPurchase(Boolean isVerifiedPurchase) {
        this.isVerifiedPurchase = isVerifiedPurchase;
    }
} 