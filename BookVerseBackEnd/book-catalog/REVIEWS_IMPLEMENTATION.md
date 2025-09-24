# BookVerse Review and Rating System

This document describes the implementation of the review and rating functionality for the BookVerse application.

## Overview

The review system allows users to:
- Submit reviews and ratings (1-5 stars) for books
- Update and delete their own reviews
- View reviews from other users
- See average ratings and rating distributions
- Purchase validation (verified purchase indicator)
- Admin moderation capabilities

## Backend Implementation

### Database Schema

The system uses a `book_reviews` table with the following structure:

```sql
CREATE TABLE book_reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    user_name VARCHAR(255) NOT NULL,
    order_item_id VARCHAR(100),           -- For purchase validation
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    is_verified_purchase BOOLEAN DEFAULT FALSE,
    status ENUM('ACTIVE', 'HIDDEN', 'DELETED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    moderated_by VARCHAR(100),            -- Admin who moderated
    moderated_at TIMESTAMP NULL,
    
    -- Constraints and indexes
    UNIQUE KEY unique_user_book (user_id, book_id),
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
);
```

### Key Components

#### 1. Entity Model (`BookReviews.java`)
- JPA entity with proper validation annotations
- Enum for review status (ACTIVE, HIDDEN, DELETED)
- Relationships to Books entity
- Automatic timestamp management

#### 2. Repository (`BookReviewsRepository.java`)
- Custom query methods for various operations
- Support for pagination and filtering
- Rating statistics queries
- Purchase validation queries

#### 3. Service Layer (`BookReviewsService.java`)
- Business logic for CRUD operations
- Purchase validation
- Rating statistics calculation
- Admin moderation features
- Automatic book rating updates

#### 4. REST Controller (`BookReviewsController.java`)
- RESTful API endpoints
- Input validation
- Error handling
- Admin-specific endpoints

### API Endpoints

#### Customer Endpoints
```
POST   /api/reviews                    - Submit new review
GET    /api/reviews/book/{bookId}      - Get reviews for a book
GET    /api/reviews/book/{bookId}/page - Get reviews with pagination
GET    /api/reviews/user/{userId}      - Get user's reviews
GET    /api/reviews/{reviewId}         - Get specific review
PUT    /api/reviews/{reviewId}         - Update review
DELETE /api/reviews/{reviewId}         - Delete review
GET    /api/reviews/book/{bookId}/stats - Get rating statistics
GET    /api/reviews/book/{bookId}/user/{userId}/can-review - Check if user can review
GET    /api/reviews/search?q={query}  - Search reviews
```

#### Admin Endpoints
```
GET    /api/reviews/admin/moderation   - Get reviews for moderation
PUT    /api/reviews/admin/{reviewId}/moderate - Moderate review
```

### Request/Response DTOs

#### ReviewRequest
```json
{
  "bookId": 1,
  "userId": "user123",
  "userName": "John Doe",
  "rating": 5,
  "comment": "Great book!",
  "orderItemId": "order_item_123",
  "isVerifiedPurchase": true
}
```

#### ReviewResponse
```json
{
  "id": 1,
  "bookId": 1,
  "bookTitle": "Sample Book",
  "userId": "user123",
  "userName": "John Doe",
  "rating": 5,
  "comment": "Great book!",
  "isVerifiedPurchase": true,
  "status": "ACTIVE",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00",
  "moderatedBy": null,
  "moderatedAt": null
}
```

## Frontend Implementation

### Services

#### ReviewService (`review.service.ts`)
- HTTP client integration with backend API
- Type-safe interfaces for requests/responses
- Error handling and user feedback
- Admin methods for moderation

### Components

#### 1. ReviewFormComponent
- Interactive star rating input
- Text area for review comments
- Create, update, and delete functionality
- Form validation and error handling
- Support for existing review editing

**Usage:**
```html
<app-review-form
  [bookId]="bookId"
  [bookTitle]="bookTitle"
  [existingReview]="userReview"
  [orderItemId]="orderItemId"
  (reviewSubmitted)="onReviewSubmitted($event)"
  (reviewUpdated)="onReviewUpdated($event)"
  (reviewDeleted)="onReviewDeleted()">
</app-review-form>
```

#### 2. ReviewListComponent
- Display list of reviews
- Star rating display
- Verified purchase indicators
- User avatars and timestamps
- Empty state handling

**Usage:**
```html
<app-review-list
  [reviews]="reviews"
  [bookTitle]="bookTitle">
</app-review-list>
```

#### 3. StarRatingComponent
- Interactive and display-only modes
- Configurable size (small, medium, large)
- Hover effects for interaction
- Support for half-star ratings

### Integration Example

The review system is integrated into the book detail page:

```typescript
// Component properties
reviews: ReviewResponse[] = [];
userReview: ReviewResponse | null = null;
reviewStats: ReviewStats | null = null;

// Load reviews when book is loaded
private loadReviews(): void {
  const bookId = parseInt(this.book.id);
  
  // Load all reviews
  this.reviewService.getBookReviews(bookId).subscribe(reviews => {
    this.reviews = reviews;
  });
  
  // Load user's review if logged in
  if (this.authService.isLoggedIn()) {
    this.reviewService.getUserReview(bookId).subscribe(userReview => {
      this.userReview = userReview;
    });
  }
  
  // Load statistics
  this.reviewService.getReviewStatistics(bookId).subscribe(stats => {
    this.reviewStats = stats;
  });
}
```

## Features

### 1. Purchase Validation
- Reviews can be marked as "Verified Purchase" using `orderItemId`
- Verified purchases display a special badge
- Can be used to restrict reviewing to purchasers only

### 2. Rating Statistics
- Automatic calculation of average ratings
- Rating distribution (1-5 stars breakdown)
- Review count tracking
- Updates book entity with current stats

### 3. Admin Moderation
- Admins can hide or delete inappropriate reviews
- Moderation history tracking
- Bulk moderation capabilities
- Status-based filtering

### 4. User Experience
- One review per user per book
- Edit and delete own reviews
- Real-time form validation
- Success/error notifications
- Responsive design

## Security Considerations

1. **Input Validation**: All inputs are validated on both frontend and backend
2. **User Authorization**: Users can only modify their own reviews
3. **Admin Authorization**: Admin endpoints require proper authentication
4. **SQL Injection Prevention**: Using parameterized queries
5. **XSS Prevention**: Proper HTML escaping in templates

## Performance Optimizations

1. **Database Indexes**: Proper indexing on frequently queried columns
2. **Pagination**: Large review lists are paginated
3. **Caching**: Consider implementing caching for rating statistics
4. **Lazy Loading**: Reviews loaded separately from book details

## Future Enhancements

1. **Review Voting**: Allow users to vote on review helpfulness
2. **Review Images**: Support for image attachments
3. **Review Replies**: Allow authors/admins to reply to reviews
4. **Advanced Filtering**: Filter reviews by rating, date, verified purchase
5. **Review Analytics**: Detailed analytics for admins
6. **Notification System**: Notify users of review responses/moderation

## Testing

### Backend Testing
- Unit tests for service methods
- Integration tests for API endpoints
- Database constraint testing
- Validation testing

### Frontend Testing
- Component unit tests
- Service integration tests
- User interaction testing
- Accessibility testing

## Deployment Notes

1. Run the database schema script to create the `book_reviews` table
2. Ensure proper database permissions for the application user
3. Configure CORS settings for frontend-backend communication
4. Set up proper logging for review operations
5. Consider implementing rate limiting for review submissions

## Troubleshooting

### Common Issues

1. **Foreign Key Constraints**: Ensure books exist before creating reviews
2. **Unique Constraint Violations**: Handle duplicate review attempts gracefully
3. **Permission Issues**: Verify user authentication for review operations
4. **CORS Errors**: Check frontend-backend URL configuration

### Monitoring

Monitor the following metrics:
- Review submission rate
- Average rating trends
- Moderation queue size
- API response times
- Error rates

This implementation provides a complete, production-ready review and rating system for the BookVerse application. 