package com.bookverse.bookCatalog.Service;

import com.bookverse.bookCatalog.Models.BookReviews;
import com.bookverse.bookCatalog.Models.Books;
import com.bookverse.bookCatalog.Repository.BookReviewsRepository;
import com.bookverse.bookCatalog.Repository.BookRepository;
import com.bookverse.bookCatalog.DTO.ReviewRequest;
import com.bookverse.bookCatalog.DTO.ReviewResponse;
import com.bookverse.bookCatalog.Exception.BookNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookReviewsService {
    
    @Autowired
    private BookReviewsRepository bookReviewsRepository;
    
    @Autowired
    private BookRepository bookRepository;
    
    /**
     * Create a new review with purchase validation
     */
    public ReviewResponse createReview(ReviewRequest reviewRequest) {
        // Get the book
        Books book = bookRepository.findById(reviewRequest.getBookId())
            .orElseThrow(() -> new BookNotFoundException(reviewRequest.getBookId()));
        
        // Check if user already reviewed this book
        Optional<BookReviews> existingReview = bookReviewsRepository
            .findByBookAndUserIdAndStatus(book, reviewRequest.getUserId(), BookReviews.ReviewStatus.ACTIVE);
        
        if (existingReview.isPresent()) {
            throw new RuntimeException("User has already reviewed this book");
        }
        
        // Create new review
        BookReviews review = new BookReviews();
        review.setBook(book);
        review.setUserId(reviewRequest.getUserId());
        review.setUserName(reviewRequest.getUserName());
        review.setRating(reviewRequest.getRating());
        review.setComment(reviewRequest.getComment());
        review.setOrderItemId(reviewRequest.getOrderItemId());
        review.setStatus(BookReviews.ReviewStatus.ACTIVE);
        
        // Set verified purchase if order item ID is provided
        if (reviewRequest.getOrderItemId() != null && !reviewRequest.getOrderItemId().trim().isEmpty()) {
            review.setIsVerifiedPurchase(true);
        } else {
            review.setIsVerifiedPurchase(false);
        }
        
        BookReviews savedReview = bookReviewsRepository.save(review);
        
        // Update book's average rating and review count
        updateBookRatingStats(book);
        
        return new ReviewResponse(savedReview);
    }
    
    /**
     * Update an existing review
     */
    public ReviewResponse updateReview(Long reviewId, ReviewRequest reviewRequest) {
        BookReviews review = bookReviewsRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));
        
        // Check if user owns this review
        if (!review.getUserId().equals(reviewRequest.getUserId())) {
            throw new RuntimeException("User can only update their own reviews");
        }
        
        // Update review
        review.setRating(reviewRequest.getRating());
        review.setComment(reviewRequest.getComment());
        
        BookReviews updatedReview = bookReviewsRepository.save(review);
        
        // Update book's average rating and review count
        updateBookRatingStats(review.getBook());
        
        return new ReviewResponse(updatedReview);
    }
    
    /**
     * Delete a review (soft delete)
     */
    public void deleteReview(Long reviewId, String userId) {
        BookReviews review = bookReviewsRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));
        
        // Check if user owns this review
        if (!review.getUserId().equals(userId)) {
            throw new RuntimeException("User can only delete their own reviews");
        }
        
        review.setStatus(BookReviews.ReviewStatus.DELETED);
        bookReviewsRepository.save(review);
        
        // Update book's average rating and review count
        updateBookRatingStats(review.getBook());
    }
    
    /**
     * Get all reviews for a book
     */
    public List<ReviewResponse> getReviewsByBookId(Long bookId) {
        Books book = bookRepository.findById(bookId)
            .orElseThrow(() -> new BookNotFoundException(bookId));
        
        List<BookReviews> reviews = bookReviewsRepository
            .findByBookAndStatusOrderByCreatedAtDesc(book, BookReviews.ReviewStatus.ACTIVE);
        
        return reviews.stream()
            .map(ReviewResponse::new)
            .collect(Collectors.toList());
    }
    
    /**
     * Get reviews for a book with pagination
     */
    public Page<ReviewResponse> getReviewsByBookId(Long bookId, Pageable pageable) {
        Books book = bookRepository.findById(bookId)
            .orElseThrow(() -> new BookNotFoundException(bookId));
        
        Page<BookReviews> reviews = bookReviewsRepository
            .findByBookAndStatusOrderByCreatedAtDesc(book, BookReviews.ReviewStatus.ACTIVE, pageable);
        
        return reviews.map(ReviewResponse::new);
    }
    
    /**
     * Get user's reviews
     */
    public List<ReviewResponse> getReviewsByUserId(String userId) {
        List<BookReviews> reviews = bookReviewsRepository
            .findByUserIdAndStatusOrderByCreatedAtDesc(userId, BookReviews.ReviewStatus.ACTIVE);
        
        return reviews.stream()
            .map(ReviewResponse::new)
            .collect(Collectors.toList());
    }
    
    /**
     * Get review by ID
     */
    public Optional<ReviewResponse> getReviewById(Long reviewId) {
        return bookReviewsRepository.findById(reviewId)
            .map(ReviewResponse::new);
    }
    
    /**
     * Get user's review for a specific book
     */
    public Optional<ReviewResponse> getUserReviewForBook(Long bookId, String userId) {
        Books book = bookRepository.findById(bookId)
            .orElseThrow(() -> new BookNotFoundException(bookId));
        
        return bookReviewsRepository
            .findByBookAndUserIdAndStatus(book, userId, BookReviews.ReviewStatus.ACTIVE)
            .map(ReviewResponse::new);
    }
    
    /**
     * Get average rating for a book
     */
    public Double getAverageRatingByBookId(Long bookId) {
        Books book = bookRepository.findById(bookId)
            .orElseThrow(() -> new BookNotFoundException(bookId));
        
        Double averageRating = bookReviewsRepository
            .findAverageRatingByBookAndStatus(book, BookReviews.ReviewStatus.ACTIVE);
        
        return averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0;
    }
    
    /**
     * Get review count for a book
     */
    public long getReviewCountByBookId(Long bookId) {
        Books book = bookRepository.findById(bookId)
            .orElseThrow(() -> new BookNotFoundException(bookId));
        
        return bookReviewsRepository.countByBookAndStatus(book, BookReviews.ReviewStatus.ACTIVE);
    }
    
    /**
     * Get rating distribution for a book
     */
    public Map<Integer, Long> getRatingDistribution(Long bookId) {
        List<Object[]> distribution = bookReviewsRepository
            .getRatingDistributionByBookId(bookId, BookReviews.ReviewStatus.ACTIVE);
        
        Map<Integer, Long> result = new HashMap<>();
        // Initialize all ratings to 0
        for (int i = 1; i <= 5; i++) {
            result.put(i, 0L);
        }
        
        // Fill with actual data
        for (Object[] row : distribution) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            result.put(rating, count);
        }
        
        return result;
    }
    
    /**
     * Check if user can review a book (has purchased it)
     */
    public boolean canUserReviewBook(String userId, Long bookId) {
        // Check if user already has a review for this book
        Optional<BookReviews> existingReview = bookReviewsRepository
            .findByBookIdAndUserIdAndStatus(bookId, userId, BookReviews.ReviewStatus.ACTIVE);
        
        if (existingReview.isPresent()) {
            return false; // User already reviewed
        }
        
        // For now, allow all users to review (can be enhanced with order validation)
        return true;
    }
    
    /**
     * Admin: Moderate a review (hide/show/delete)
     */
    public ReviewResponse moderateReview(Long reviewId, BookReviews.ReviewStatus newStatus, String moderatorId) {
        BookReviews review = bookReviewsRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));
        
        review.setStatus(newStatus);
        review.setModeratedBy(moderatorId);
        review.setModeratedAt(LocalDateTime.now());
        
        BookReviews moderatedReview = bookReviewsRepository.save(review);
        
        // Update book's rating stats
        updateBookRatingStats(review.getBook());
        
        return new ReviewResponse(moderatedReview);
    }
    
    /**
     * Admin: Get all reviews for moderation
     */
    public Page<ReviewResponse> getAllReviewsForModeration(Pageable pageable) {
        Page<BookReviews> reviews = bookReviewsRepository
            .findByStatusOrderByCreatedAtDesc(BookReviews.ReviewStatus.ACTIVE, pageable);
        
        return reviews.map(ReviewResponse::new);
    }
    
    /**
     * Search reviews by text content
     */
    public List<ReviewResponse> searchReviews(String searchText) {
        List<BookReviews> reviews = bookReviewsRepository
            .findByCommentContainingAndStatus(searchText, BookReviews.ReviewStatus.ACTIVE);
        
        return reviews.stream()
            .map(ReviewResponse::new)
            .collect(Collectors.toList());
    }
    
    /**
     * Update book's average rating and review count
     */
    private void updateBookRatingStats(Books book) {
        Double averageRating = bookReviewsRepository
            .findAverageRatingByBookAndStatus(book, BookReviews.ReviewStatus.ACTIVE);
        long reviewCount = bookReviewsRepository
            .countByBookAndStatus(book, BookReviews.ReviewStatus.ACTIVE);
        
        book.setAverageRating(averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0);
        book.setReviewCount((int) reviewCount);
        
        bookRepository.save(book);
    }
} 