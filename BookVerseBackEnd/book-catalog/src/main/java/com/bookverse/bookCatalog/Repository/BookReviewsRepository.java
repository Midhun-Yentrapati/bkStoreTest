package com.bookverse.bookCatalog.Repository;

import com.bookverse.bookCatalog.Models.BookReviews;
import com.bookverse.bookCatalog.Models.Books;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookReviewsRepository extends JpaRepository<BookReviews, Long> {
    
    // Find reviews by book ID
    List<BookReviews> findByBookAndStatusOrderByCreatedAtDesc(Books book, BookReviews.ReviewStatus status);
    
    // Find reviews by book ID with pagination
    Page<BookReviews> findByBookAndStatusOrderByCreatedAtDesc(Books book, BookReviews.ReviewStatus status, Pageable pageable);
    
    // Find reviews by user ID
    List<BookReviews> findByUserIdAndStatusOrderByCreatedAtDesc(String userId, BookReviews.ReviewStatus status);
    
    // Find review by book and user (for checking if user already reviewed)
    Optional<BookReviews> findByBookAndUserIdAndStatus(Books book, String userId, BookReviews.ReviewStatus status);
    
    // Find all reviews with pagination
    Page<BookReviews> findByStatusOrderByCreatedAtDesc(BookReviews.ReviewStatus status, Pageable pageable);
    
    // Count reviews by book
    long countByBookAndStatus(Books book, BookReviews.ReviewStatus status);
    
    // Count reviews by user ID
    long countByUserIdAndStatus(String userId, BookReviews.ReviewStatus status);
    
    // Find reviews by rating range
    List<BookReviews> findByBookAndRatingBetweenAndStatusOrderByCreatedAtDesc(
        Books book, Integer minRating, Integer maxRating, BookReviews.ReviewStatus status);
    
    // Calculate average rating for a book
    @Query("SELECT AVG(r.rating) FROM BookReviews r WHERE r.book = :book AND r.status = :status")
    Double findAverageRatingByBookAndStatus(@Param("book") Books book, @Param("status") BookReviews.ReviewStatus status);
    
    // Find reviews with text (not just ratings)
    List<BookReviews> findByBookAndCommentIsNotNullAndStatusOrderByCreatedAtDesc(
        Books book, BookReviews.ReviewStatus status);
    
    // Search reviews by text content
    @Query("SELECT r FROM BookReviews r WHERE r.comment LIKE %:searchText% AND r.status = :status ORDER BY r.createdAt DESC")
    List<BookReviews> findByCommentContainingAndStatus(@Param("searchText") String searchText, @Param("status") BookReviews.ReviewStatus status);
    
    // Find reviews by book ID (using book ID directly)
    @Query("SELECT r FROM BookReviews r WHERE r.book.id = :bookId AND r.status = :status ORDER BY r.createdAt DESC")
    List<BookReviews> findByBookIdAndStatus(@Param("bookId") Long bookId, @Param("status") BookReviews.ReviewStatus status);
    
    // Find review by book ID and user ID (for checking if user already reviewed)
    @Query("SELECT r FROM BookReviews r WHERE r.book.id = :bookId AND r.userId = :userId AND r.status = :status")
    Optional<BookReviews> findByBookIdAndUserIdAndStatus(@Param("bookId") Long bookId, @Param("userId") String userId, @Param("status") BookReviews.ReviewStatus status);
    
    // Check if user has purchased the book (verified purchase validation)
    @Query("SELECT COUNT(r) > 0 FROM BookReviews r WHERE r.userId = :userId AND r.book.id = :bookId AND r.orderItemId IS NOT NULL")
    boolean hasUserPurchasedBook(@Param("userId") String userId, @Param("bookId") Long bookId);
    
    // Find all reviews that need moderation (for admin)
    @Query("SELECT r FROM BookReviews r WHERE r.status = :status ORDER BY r.createdAt ASC")
    Page<BookReviews> findReviewsForModeration(@Param("status") BookReviews.ReviewStatus status, Pageable pageable);
    
    // Find reviews moderated by admin
    List<BookReviews> findByModeratedByIsNotNullOrderByModeratedAtDesc();
    
    // Get rating distribution for a book
    @Query("SELECT r.rating, COUNT(r) FROM BookReviews r WHERE r.book.id = :bookId AND r.status = :status GROUP BY r.rating ORDER BY r.rating")
    List<Object[]> getRatingDistributionByBookId(@Param("bookId") Long bookId, @Param("status") BookReviews.ReviewStatus status);
} 