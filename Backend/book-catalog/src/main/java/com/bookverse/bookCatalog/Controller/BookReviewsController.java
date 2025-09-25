package com.bookverse.bookCatalog.Controller;

import com.bookverse.bookCatalog.Models.BookReviews;
import com.bookverse.bookCatalog.Service.BookReviewsService;
import com.bookverse.bookCatalog.DTO.ReviewRequest;
import com.bookverse.bookCatalog.DTO.ReviewResponse;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class BookReviewsController {
    
    @Autowired
    private BookReviewsService bookReviewsService;
    
    /**
     * Submit a new review
     */
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody ReviewRequest reviewRequest) {
        try {
            ReviewResponse review = bookReviewsService.createReview(reviewRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(review);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get all reviews for a book
     */
    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByBookId(@PathVariable Long bookId) {
        try {
            List<ReviewResponse> reviews = bookReviewsService.getReviewsByBookId(bookId);
            return ResponseEntity.ok(reviews);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get reviews for a book with pagination
     */
    @GetMapping("/book/{bookId}/page")
    public ResponseEntity<Page<ReviewResponse>> getReviewsByBookIdWithPagination(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ReviewResponse> reviews = bookReviewsService.getReviewsByBookId(bookId, pageable);
            return ResponseEntity.ok(reviews);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get user's reviews
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByUserId(@PathVariable String userId) {
        List<ReviewResponse> reviews = bookReviewsService.getReviewsByUserId(userId);
        return ResponseEntity.ok(reviews);
    }
    
    /**
     * Get user's review for a specific book
     */
    @GetMapping("/book/{bookId}/user/{userId}")
    public ResponseEntity<ReviewResponse> getUserReviewForBook(
            @PathVariable Long bookId, 
            @PathVariable String userId) {
        try {
            Optional<ReviewResponse> review = bookReviewsService.getUserReviewForBook(bookId, userId);
            return review.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get review by ID
     */
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReviewById(@PathVariable Long reviewId) {
        Optional<ReviewResponse> review = bookReviewsService.getReviewById(reviewId);
        return review.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Update a review
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long reviewId, 
            @Valid @RequestBody ReviewRequest reviewRequest) {
        try {
            ReviewResponse review = bookReviewsService.updateReview(reviewId, reviewRequest);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Delete a review
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Map<String, String>> deleteReview(
            @PathVariable Long reviewId, 
            @RequestParam String userId) {
        try {
            bookReviewsService.deleteReview(reviewId, userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Review deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get average rating and review count for a book
     */
    @GetMapping("/book/{bookId}/stats")
    public ResponseEntity<Map<String, Object>> getBookReviewStats(@PathVariable Long bookId) {
        try {
            Double averageRating = bookReviewsService.getAverageRatingByBookId(bookId);
            long reviewCount = bookReviewsService.getReviewCountByBookId(bookId);
            Map<Integer, Long> ratingDistribution = bookReviewsService.getRatingDistribution(bookId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("averageRating", averageRating);
            response.put("reviewCount", reviewCount);
            response.put("ratingDistribution", ratingDistribution);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Check if user can review a book
     */
    @GetMapping("/book/{bookId}/user/{userId}/can-review")
    public ResponseEntity<Map<String, Boolean>> canUserReviewBook(
            @PathVariable Long bookId, 
            @PathVariable String userId) {
        boolean canReview = bookReviewsService.canUserReviewBook(userId, bookId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("canReview", canReview);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Search reviews by text content
     */
    @GetMapping("/search")
    public ResponseEntity<List<ReviewResponse>> searchReviews(@RequestParam String q) {
        List<ReviewResponse> reviews = bookReviewsService.searchReviews(q);
        return ResponseEntity.ok(reviews);
    }
    
    // Admin Endpoints
    
    /**
     * Admin: Get all reviews for moderation
     */
    @GetMapping("/admin/moderation")
    public ResponseEntity<Page<ReviewResponse>> getAllReviewsForModeration(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewResponse> reviews = bookReviewsService.getAllReviewsForModeration(pageable);
        return ResponseEntity.ok(reviews);
    }
    
    /**
     * Admin: Moderate a review (change status)
     */
    @PutMapping("/admin/{reviewId}/moderate")
    public ResponseEntity<ReviewResponse> moderateReview(
            @PathVariable Long reviewId,
            @RequestParam String status,
            @RequestParam String moderatorId) {
        try {
            BookReviews.ReviewStatus reviewStatus = BookReviews.ReviewStatus.valueOf(status.toUpperCase());
            ReviewResponse review = bookReviewsService.moderateReview(reviewId, reviewStatus, moderatorId);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Book Reviews Service");
        return ResponseEntity.ok(response);
    }
} 