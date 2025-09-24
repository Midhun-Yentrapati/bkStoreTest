import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of, map, catchError } from 'rxjs';
import { AuthService } from './auth.service';

export interface ReviewRequest {
  bookId: number;
  userId: string;
  userName: string;
  rating: number;
  comment?: string;
  orderItemId?: string;
  isVerifiedPurchase?: boolean;
}

export interface ReviewResponse {
  id: number;
  bookId: number;
  bookTitle: string;
  userId: string;
  userName: string;
  rating: number;
  comment?: string;
  isVerifiedPurchase: boolean;
  status: string;
  createdAt: string;
  updatedAt: string;
  moderatedBy?: string;
  moderatedAt?: string;
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

  private baseUrl = 'http://localhost:8080/api/reviews';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) { }

  /**
   * Submit a new review for a book
   */
  submitReview(bookId: number, rating: number, reviewText: string, orderItemId?: string): Observable<ReviewResponse | null> {
    const currentUser = this.authService.getCurrentCustomer();
    
    if (!currentUser) {
      console.error('User must be logged in to submit a review');
      return of(null);
    }

    const reviewRequest: ReviewRequest = {
      bookId: bookId,
      userId: currentUser.id,
      userName: currentUser.fullName || currentUser.username,
      rating: rating,
      comment: reviewText,
      orderItemId: orderItemId,
      isVerifiedPurchase: !!orderItemId
    };

    return this.http.post<ReviewResponse>(this.baseUrl, reviewRequest).pipe(
      catchError(error => {
        console.error('Failed to submit review:', error);
        return of(null);
      })
    );
  }

  /**
   * Update an existing review
   */
  updateReview(reviewId: number, rating: number, reviewText: string): Observable<ReviewResponse | null> {
    const currentUser = this.authService.getCurrentCustomer();
    
    if (!currentUser) {
      console.error('User must be logged in to update a review');
      return of(null);
    }

    const reviewRequest: ReviewRequest = {
      bookId: 0, // Will be ignored in update
      userId: currentUser.id,
      userName: currentUser.fullName || currentUser.username,
      rating: rating,
      comment: reviewText
    };

    return this.http.put<ReviewResponse>(`${this.baseUrl}/${reviewId}`, reviewRequest).pipe(
      catchError(error => {
        console.error('Failed to update review:', error);
        return of(null);
      })
    );
  }

  /**
   * Delete a review
   */
  deleteReview(reviewId: number): Observable<boolean> {
    const currentUser = this.authService.getCurrentCustomer();
    
    if (!currentUser) {
      console.error('User must be logged in to delete a review');
      return of(false);
    }

    const params = new HttpParams().set('userId', currentUser.id);

    return this.http.delete(`${this.baseUrl}/${reviewId}`, { params }).pipe(
      map(() => true),
      catchError(error => {
        console.error('Failed to delete review:', error);
        return of(false);
      })
    );
  }

  /**
   * Get all reviews for a book
   */
  getBookReviews(bookId: number): Observable<ReviewResponse[]> {
    return this.http.get<ReviewResponse[]>(`${this.baseUrl}/book/${bookId}`).pipe(
      catchError(error => {
        console.error('Failed to get book reviews:', error);
        return of([]);
      })
    );
  }

  /**
   * Get reviews for a book with pagination
   */
  getBookReviewsWithPagination(bookId: number, page: number = 0, size: number = 10): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<any>(`${this.baseUrl}/book/${bookId}/page`, { params }).pipe(
      catchError(error => {
        console.error('Failed to get book reviews with pagination:', error);
        return of({ content: [], totalElements: 0, totalPages: 0 });
      })
    );
  }

  /**
   * Get current user's review for a book
   */
  getUserReview(bookId: number): Observable<ReviewResponse | null> {
    const currentUser = this.authService.getCurrentCustomer();
    
    if (!currentUser) {
      return of(null);
    }

    return this.http.get<ReviewResponse>(`${this.baseUrl}/book/${bookId}/user/${currentUser.id}`).pipe(
      catchError(error => {
        // 404 is expected if user hasn't reviewed the book
        return of(null);
      })
    );
  }

  /**
   * Get user's all reviews
   */
  getUserReviews(): Observable<ReviewResponse[]> {
    const currentUser = this.authService.getCurrentCustomer();
    
    if (!currentUser) {
      return of([]);
    }

    return this.http.get<ReviewResponse[]>(`${this.baseUrl}/user/${currentUser.id}`).pipe(
      catchError(error => {
        console.error('Failed to get user reviews:', error);
        return of([]);
      })
    );
  }

  /**
   * Get review statistics for a book
   */
  getReviewStatistics(bookId: number): Observable<ReviewStats> {
    return this.http.get<ReviewStats>(`${this.baseUrl}/book/${bookId}/stats`).pipe(
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
   */
  canUserReview(bookId: number): Observable<boolean> {
    const currentUser = this.authService.getCurrentCustomer();
    
    if (!currentUser) {
      return of(false);
    }

    return this.http.get<{canReview: boolean}>(`${this.baseUrl}/book/${bookId}/user/${currentUser.id}/can-review`).pipe(
      map(response => response.canReview),
      catchError(error => {
        console.error('Failed to check if user can review:', error);
        return of(false);
      })
    );
  }

  /**
   * Search reviews by text content
   */
  searchReviews(searchText: string): Observable<ReviewResponse[]> {
    const params = new HttpParams().set('q', searchText);

    return this.http.get<ReviewResponse[]>(`${this.baseUrl}/search`, { params }).pipe(
      catchError(error => {
        console.error('Failed to search reviews:', error);
        return of([]);
      })
    );
  }

  /**
   * Get rating text description
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

  /**
   * Format review date for display
   */
  formatReviewDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }

  // Admin methods (if needed)
  
  /**
   * Admin: Get all reviews for moderation
   */
  getAllReviewsForModeration(page: number = 0, size: number = 10): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<any>(`${this.baseUrl}/admin/moderation`, { params }).pipe(
      catchError(error => {
        console.error('Failed to get reviews for moderation:', error);
        return of({ content: [], totalElements: 0, totalPages: 0 });
      })
    );
  }

  /**
   * Admin: Moderate a review
   */
  moderateReview(reviewId: number, status: string, moderatorId: string): Observable<ReviewResponse | null> {
    const params = new HttpParams()
      .set('status', status)
      .set('moderatorId', moderatorId);

    return this.http.put<ReviewResponse>(`${this.baseUrl}/admin/${reviewId}/moderate`, null, { params }).pipe(
      catchError(error => {
        console.error('Failed to moderate review:', error);
        return of(null);
      })
    );
  }
}
