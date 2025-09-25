-- Database schema for BookVerse Book Catalog Service
-- This includes the book_reviews table for the review functionality

-- Create the book_reviews table
CREATE TABLE IF NOT EXISTS book_reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    user_name VARCHAR(255) NOT NULL,
    order_item_id VARCHAR(100),
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    is_verified_purchase BOOLEAN DEFAULT FALSE,
    status ENUM('ACTIVE', 'HIDDEN', 'DELETED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    moderated_by VARCHAR(100),
    moderated_at TIMESTAMP NULL,
    
    -- Indexes for performance
    INDEX idx_book_id (book_id),
    INDEX idx_user_id (user_id),
    INDEX idx_rating (rating),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    
    -- Unique constraint to prevent duplicate reviews from same user for same book
    UNIQUE KEY unique_user_book (user_id, book_id),
    
    -- Foreign key constraint (assuming books table exists)
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
);

-- Sample data for testing (optional)
INSERT INTO book_reviews (book_id, user_id, user_name, rating, comment, is_verified_purchase, status) VALUES
(1, 'user123', 'John Doe', 5, 'Excellent book! Highly recommended.', true, 'ACTIVE'),
(1, 'user456', 'Jane Smith', 4, 'Good read, enjoyed the storyline.', false, 'ACTIVE'),
(2, 'user123', 'John Doe', 3, 'Average book, not what I expected.', true, 'ACTIVE'),
(3, 'user789', 'Bob Wilson', 5, 'Amazing content and well written!', true, 'ACTIVE');

-- Query to get average rating and review count for a book
-- SELECT AVG(rating) as average_rating, COUNT(*) as review_count 
-- FROM book_reviews 
-- WHERE book_id = ? AND status = 'ACTIVE';

-- Query to get rating distribution for a book
-- SELECT rating, COUNT(*) as count 
-- FROM book_reviews 
-- WHERE book_id = ? AND status = 'ACTIVE' 
-- GROUP BY rating 
-- ORDER BY rating; 