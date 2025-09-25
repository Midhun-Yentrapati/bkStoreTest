import { Injectable } from '@angular/core';
import { Observable, of, map, catchError } from 'rxjs';
import { BookService } from './book.service';
import { CustomerRating } from '../models/book.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class ReviewService {

  constructor(
    private bookService: BookService,
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
    const currentUser = this.authService.currentCustomer();
    
    if (!currentUser) {
      console.error('User must be logged in to submit a review');
      return of(false);
    }

    const reviewData = {
      rating: rating,
      review: reviewText,
      userId: currentUser.id,
      userName: currentUser.fullName || currentUser.username
    };

    return this.bookService.addReview(bookId, reviewData).pipe(
      map(() => true),
      catchError(error => {
        console.error('Failed to submit review:', error);
        return of(false);
      })
    );
  }

  /**
   * Update an existing review
   * @param bookId - Book ID
   * @param rating - New rating
   * @param reviewText - New review text
   * @returns Observable of success/failure
   */
  updateReview(bookId: string, rating: number, reviewText: string): Observable<boolean> {
    const currentUser = this.authService.currentCustomer();
    
    if (!currentUser) {
      console.error('User must be logged in to update a review');
      return of(false);
    }

    return this.bookService.updateReview(bookId, currentUser.id, { rating, review: reviewText }).pipe(
      map(() => true),
      catchError(error => {
        console.error('Failed to update review:', error);
        return of(false);
      })
    );
  }

  /**
   * Delete a review
   * @param bookId - Book ID
   * @returns Observable of success/failure
   */
  deleteReview(bookId: string): Observable<boolean> {
    const currentUser = this.authService.currentCustomer();
    
    if (!currentUser) {
      console.error('User must be logged in to delete a review');
      return of(false);
    }

    return this.bookService.deleteReview(bookId, currentUser.id).pipe(
      map(() => true),
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
    return this.bookService.getBookReviews(bookId);
  }

  /**
   * Get current user's review for a book
   * @param bookId - Book ID
   * @returns Observable of user's review or null
   */
  getUserReview(bookId: string): Observable<CustomerRating | null> {
    const currentUser = this.authService.currentCustomer();
    
    if (!currentUser) {
      return of(null);
    }

    return this.bookService.getUserReviewForBook(bookId, currentUser.id);
  }

  /**
   * Get average rating for a book
   * @param bookId - Book ID
   * @returns Observable of average rating
   */
  getAverageRating(bookId: string): Observable<number> {
    return this.bookService.getBookAverageRating(bookId);
  }

  /**
   * Check if current user can review a book
   * @param bookId - Book ID
   * @returns Boolean indicating if user can review
   */
  canUserReview(bookId: string): boolean {
    const currentUser = this.authService.currentCustomer();
    return currentUser !== null;
  }

  /**
   * Get review statistics for a book
   * @param bookId - Book ID
   * @returns Observable of review statistics
   */
  getReviewStatistics(bookId: string): Observable<{
    totalReviews: number;
    averageRating: number;
    ratingDistribution: { [key: number]: number };
  }> {
    return this.getBookReviews(bookId).pipe(
      map(reviews => {
        const totalReviews = reviews.length;
        const averageRating = totalReviews > 0 
          ? Math.round((reviews.reduce((sum, r) => sum + r.rating, 0) / totalReviews) * 10) / 10 
          : 0;
        
        const ratingDistribution: { [key: number]: number } = { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 };
        reviews.forEach(review => {
          ratingDistribution[review.rating] = (ratingDistribution[review.rating] || 0) + 1;
        });

        return {
          totalReviews,
          averageRating,
          ratingDistribution
        };
      }),
      catchError(error => {
        console.error('Failed to get review statistics:', error);
        return of({ totalReviews: 0, averageRating: 0, ratingDistribution: { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 } });
      })
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
}
