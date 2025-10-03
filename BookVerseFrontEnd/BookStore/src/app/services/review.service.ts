import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, map, catchError } from 'rxjs';
import { CustomerRating } from '../models/book.model';
import { AuthService } from './auth.service';

// Backend DTOs
export interface ReviewRequest {
  bookId: number;
  userId: string;
  userName: string;
  rating: number;
  comment: string;
  orderItemId?: string;
  isVerifiedPurchase?: boolean;
}

export interface ReviewResponse {
  id: number;
  bookId: number;
  userId: string;
  userName: string;
  rating: number;
  comment: string;
  isVerifiedPurchase: boolean;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface ReviewStats {
  averageRating: number;
  reviewCount: number;
  ratingDistribution: { [key: number]: number };
}

@Injectable({
  providedIn: 'root'
})
export class ReviewService {
  private apiBaseUrl = 'http://localhost:8090/api'; // API Gateway URL
  private reviewsUrl = `${this.apiBaseUrl}/reviews`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) { }

  /**
   * Submit a new review for a book
   * @param bookId - Book ID
   * @param rating - Rating (1-5)
   * @param reviewText - Review text
   * @returns Observable of success/failure
   */
  submitReview(bookId: string, rating: number, reviewText: string): Observable<boolean> {
    const currentUser = this.authService.getCurrentCustomer();
    
    if (!currentUser) {
      console.error('User must be logged in to submit a review');
      return of(false);
    }

    const reviewRequest: ReviewRequest = {
      bookId: parseInt(bookId),
      userId: currentUser.id,
      userName: currentUser.fullName || currentUser.username,
      rating: rating,
      comment: reviewText,
      isVerifiedPurchase: false // TODO: Check if user has purchased the book
    };

    return this.http.post<ReviewResponse>(this.reviewsUrl, reviewRequest).pipe(
      map(() => {
        console.log('Review submitted successfully');
        return true;
      }),
      catchError(error => {
        console.error('Failed to submit review:', error);
        return of(false);
      })
    );
  }

  /**
   * Update an existing review
   * @param reviewId - Review ID
   * @param rating - New rating
   * @param reviewText - New review text
   * @returns Observable of success/failure
   */
  updateReview(reviewId: number, rating: number, reviewText: string): Observable<boolean> {
    const currentUser = this.authService.getCurrentCustomer();
    
    if (!currentUser) {
      console.error('User must be logged in to update a review');
      return of(false);
    }

    const reviewRequest: Partial<ReviewRequest> = {
      userName: currentUser.fullName || currentUser.username,
      rating: rating,
      comment: reviewText
    };

    return this.http.put<ReviewResponse>(`${this.reviewsUrl}/${reviewId}`, reviewRequest).pipe(
      map(() => {
        console.log('Review updated successfully');
        return true;
      }),
      catchError(error => {
        console.error('Failed to update review:', error);
        return of(false);
      })
    );
  }

  /**
   * Delete a review
   * @param reviewId - Review ID
   * @returns Observable of success/failure
   */
  deleteReview(reviewId: number): Observable<boolean> {
    const currentUser = this.authService.getCurrentCustomer();
    
    if (!currentUser) {
      console.error('User must be logged in to delete a review');
      return of(false);
    }

    return this.http.delete(`${this.reviewsUrl}/${reviewId}`, {
      params: { userId: currentUser.id }
    }).pipe(
      map(() => {
        console.log('Review deleted successfully');
        return true;
      }),
      catchError(error => {
        console.error('Failed to delete review:', error);
        return of(false);
      })
    );
  }

  /**
   * Get all reviews for a book
   * @param bookId - Book ID
   * @returns Observable of customer ratings array
   */
  getBookReviews(bookId: string): Observable<CustomerRating[]> {
    return this.http.get<ReviewResponse[]>(`${this.reviewsUrl}/book/${bookId}`).pipe(
      map(reviews => reviews.map(this.mapReviewResponseToCustomerRating)),
      catchError(error => {
        console.error('Failed to get book reviews:', error);
        return of([]);
      })
    );
  }

  /**
   * Get reviews for a book with pagination
   * @param bookId - Book ID
   * @param page - Page number (0-based)
   * @param size - Page size
   * @returns Observable of paginated reviews
   */
  getBookReviewsPaginated(bookId: string, page: number = 0, size: number = 10): Observable<{
    content: CustomerRating[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
  }> {
    return this.http.get<any>(`${this.reviewsUrl}/book/${bookId}/page`, {
      params: { page: page.toString(), size: size.toString() }
    }).pipe(
      map(response => ({
        content: response.content.map(this.mapReviewResponseToCustomerRating),
        totalElements: response.totalElements,
        totalPages: response.totalPages,
        number: response.number,
        size: response.size
      })),
      catchError(error => {
        console.error('Failed to get paginated book reviews:', error);
        return of({
          content: [],
          totalElements: 0,
          totalPages: 0,
          number: 0,
          size: 0
        });
      })
    );
  }

  /**
   * Get current user's review for a book
   * @param bookId - Book ID
   * @returns Observable of user's review or null
   */
  getUserReview(bookId: string): Observable<CustomerRating | null> {
    const currentUser = this.authService.getCurrentCustomer();
    
    if (!currentUser) {
      console.log('No user logged in, skipping user review fetch');
      return of(null);
    }

    return this.http.get<ReviewResponse>(`${this.reviewsUrl}/book/${bookId}/user/${currentUser.id}`).pipe(
      map(review => this.mapReviewResponseToCustomerRating(review)),
      catchError(error => {
        // Handle 404 (no review found) vs other errors
        if (error.status === 404) {
          console.log('No existing review found for user');
          return of(null);
        }
        console.error('Failed to get user review:', error);
        return of(null);
      })
    );
  }

  /**
   * Get review statistics for a book
   * @param bookId - Book ID
   * @returns Observable of review statistics
   */
  getReviewStatistics(bookId: string): Observable<ReviewStats> {
    return this.http.get<ReviewStats>(`${this.reviewsUrl}/book/${bookId}/stats`).pipe(
      catchError(error => {
        console.error('Failed to get review statistics:', error);
        return of({
          averageRating: 0,
          reviewCount: 0,
          ratingDistribution: { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 }
        });
      })
    );
  }

  /**
   * Check if current user can review a book
   * @param bookId - Book ID
   * @returns Observable of boolean indicating if user can review
   */
  canUserReview(bookId: string): Observable<boolean> {
    const currentUser = this.authService.getCurrentCustomer();
    
    if (!currentUser) {
      return of(false);
    }

    return this.http.get<{ canReview: boolean }>(`${this.reviewsUrl}/book/${bookId}/user/${currentUser.id}/can-review`).pipe(
      map(response => response.canReview),
      catchError(error => {
        console.error('Failed to check if user can review:', error);
        return of(false);
      })
    );
  }

  /**
   * Get all reviews by current user
   * @returns Observable of user's reviews
   */
  getUserReviews(): Observable<CustomerRating[]> {
    const currentUser = this.authService.getCurrentCustomer();
    
    if (!currentUser) {
      return of([]);
    }

    return this.http.get<ReviewResponse[]>(`${this.reviewsUrl}/user/${currentUser.id}`).pipe(
      map(reviews => reviews.map(this.mapReviewResponseToCustomerRating)),
      catchError(error => {
        console.error('Failed to get user reviews:', error);
        return of([]);
      })
    );
  }

  /**
   * Search reviews by text content
   * @param query - Search query
   * @returns Observable of matching reviews
   */
  searchReviews(query: string): Observable<CustomerRating[]> {
    return this.http.get<ReviewResponse[]>(`${this.reviewsUrl}/search`, {
      params: { q: query }
    }).pipe(
      map(reviews => reviews.map(this.mapReviewResponseToCustomerRating)),
      catchError(error => {
        console.error('Failed to search reviews:', error);
        return of([]);
      })
    );
  }

  /**
   * Map backend ReviewResponse to frontend CustomerRating
   * @param review - Backend review response
   * @returns Frontend customer rating
   */
  private mapReviewResponseToCustomerRating(review: ReviewResponse): CustomerRating {
    return {
      id: review.id,
      userId: review.userId,
      userName: review.userName,
      rating: review.rating,
      comment: review.comment,
      review: review.comment, // For backward compatibility
      createdAt: review.createdAt,
      isVerifiedPurchase: review.isVerifiedPurchase,
      status: review.status,
      bookId: review.bookId
    };
  }

  /**
   * Get average rating for a book (convenience method)
   * @param bookId - Book ID
   * @returns Observable of average rating
   */
  getAverageRating(bookId: string): Observable<number> {
    return this.getReviewStatistics(bookId).pipe(
      map(stats => stats.averageRating)
    );
  }

  /**
   * Format review date for display
   * @param dateString - ISO date string
   * @returns Formatted date string
   */
  formatReviewDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }

  /**
   * Get rating text description
   * @param rating - Rating value (1-5)
   * @returns Text description of rating
   */
  getRatingText(rating: number): string {
    switch (rating) {
      case 1: return 'Poor';
      case 2: return 'Fair';
      case 3: return 'Good';
      case 4: return 'Very Good';
      case 5: return 'Excellent';
      default: return 'No Rating';
    }
  }

  // Admin Methods

  /**
   * Get all reviews for moderation (admin only)
   * @param page - Page number
   * @param size - Page size
   * @returns Observable of paginated reviews
   */
  getAllReviewsForModeration(page: number = 0, size: number = 10): Observable<any> {
    return this.http.get(`${this.reviewsUrl}/admin/moderation`, {
      params: { page: page.toString(), size: size.toString() }
    }).pipe(
      catchError(error => {
        console.error('Failed to get reviews for moderation:', error);
        return of({ content: [], totalElements: 0 });
      })
    );
  }

  /**
   * Moderate a review (admin only)
   * @param reviewId - Review ID
   * @param status - New status
   * @param moderatorId - Moderator ID
   * @returns Observable of updated review
   */
  moderateReview(reviewId: number, status: string, moderatorId: string): Observable<boolean> {
    return this.http.put(`${this.reviewsUrl}/admin/${reviewId}/moderate`, null, {
      params: { status, moderatorId }
    }).pipe(
      map(() => {
        console.log('Review moderated successfully');
        return true;
      }),
      catchError(error => {
        console.error('Failed to moderate review:', error);
        return of(false);
      })
    );
  }
}
