package com.bookverse.bookCatalog.DTO;

import com.bookverse.bookCatalog.Models.BookReviews;
import java.time.LocalDateTime;

public class ReviewResponse {
    
    private Long id;
    private Long bookId;
    private String bookTitle;
    private String userId;
    private String userName;
    private Integer rating;
    private String comment;
    private Boolean isVerifiedPurchase;
    private BookReviews.ReviewStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String moderatedBy;
    private LocalDateTime moderatedAt;
    
    // Constructors
    public ReviewResponse() {}
    
    public ReviewResponse(BookReviews review) {
        this.id = review.getId();
        this.bookId = review.getBook().getId();
        this.bookTitle = review.getBook().getTitle();
        this.userId = review.getUserId();
        this.userName = review.getUserName();
        this.rating = review.getRating();
        this.comment = review.getComment();
        this.isVerifiedPurchase = review.getIsVerifiedPurchase();
        this.status = review.getStatus();
        this.createdAt = review.getCreatedAt();
        this.updatedAt = review.getUpdatedAt();
        this.moderatedBy = review.getModeratedBy();
        this.moderatedAt = review.getModeratedAt();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getBookId() {
        return bookId;
    }
    
    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }
    
    public String getBookTitle() {
        return bookTitle;
    }
    
    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
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
    
    public Boolean getIsVerifiedPurchase() {
        return isVerifiedPurchase;
    }
    
    public void setIsVerifiedPurchase(Boolean isVerifiedPurchase) {
        this.isVerifiedPurchase = isVerifiedPurchase;
    }
    
    public BookReviews.ReviewStatus getStatus() {
        return status;
    }
    
    public void setStatus(BookReviews.ReviewStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getModeratedBy() {
        return moderatedBy;
    }
    
    public void setModeratedBy(String moderatedBy) {
        this.moderatedBy = moderatedBy;
    }
    
    public LocalDateTime getModeratedAt() {
        return moderatedAt;
    }
    
    public void setModeratedAt(LocalDateTime moderatedAt) {
        this.moderatedAt = moderatedAt;
    }
} 