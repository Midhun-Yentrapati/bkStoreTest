package com.bookverse.bookCatalog.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "book_reviews")
public class BookReviews {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "book_id", nullable = false)
	@JsonBackReference
	private Books book;
	
	@Column(name="user_id", nullable = false)
	@NotBlank(message = "User ID is required")
	private String userId;
	
	@Column(name = "user_name", nullable = false)
	@NotBlank(message = "User name is required")
	@Size(max = 255, message = "User name must not exceed 255 characters")
	private String userName;
	
	@Column(name = "order_item_id")
	private String orderItemId;
	
	@Column(name="rating", nullable = false)
	@NotNull(message = "Rating is required")
	@Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
	private Integer rating;
	
	@Column(name="comment", columnDefinition = "TEXT")
	@Size(max = 1000, message = "Review text must not exceed 1000 characters")
	private String comment;
	
	@Column(name = "is_verified_purchase")
    private Boolean isVerifiedPurchase = false;
	
	@Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ReviewStatus status = ReviewStatus.ACTIVE;
	
    public enum ReviewStatus {
        ACTIVE, HIDDEN, DELETED
    }
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name="moderated_by")
    private String moderatedBy;
    
    @Column(name="moderated_at")
    private LocalDateTime moderatedAt;
    
    // Constructors
    public BookReviews() {}
    
    public BookReviews(Books book, String userId, String userName, Integer rating, String comment) {
        this.book = book;
        this.userId = userId;
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Books getBook() {
        return book;
    }
    
    public void setBook(Books book) {
        this.book = book;
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
    
    public String getOrderItemId() {
        return orderItemId;
    }
    
    public void setOrderItemId(String orderItemId) {
        this.orderItemId = orderItemId;
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
    
    public ReviewStatus getStatus() {
        return status;
    }
    
    public void setStatus(ReviewStatus status) {
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
