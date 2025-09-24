import { Component, Input, Output, EventEmitter, OnInit, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StarRatingComponent } from '../star-rating/star-rating.component';
import { AuthService } from '../../../services/auth.service';
import { ReviewService, ReviewResponse } from '../../../services/review.service';

@Component({
  selector: 'app-review-form',
  imports: [CommonModule, FormsModule, StarRatingComponent],
  templateUrl: './review-form.component.html',
  styleUrl: './review-form.component.css'
})
export class ReviewFormComponent implements OnInit, OnChanges {
  @Input() bookId: number = 0;
  @Input() bookTitle: string = '';
  @Input() existingReview: ReviewResponse | null = null;
  @Input() orderItemId?: string; // For verified purchase
  @Output() reviewSubmitted = new EventEmitter<ReviewResponse>();
  @Output() reviewUpdated = new EventEmitter<ReviewResponse>();
  @Output() reviewDeleted = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();

  rating: number = 0;
  review: string = '';
  showForm: boolean = false;
  isSubmitting: boolean = false;
  submissionSuccess: boolean = false;
  submissionError: string = '';

  constructor(
    private authService: AuthService,
    private reviewService: ReviewService
  ) {}

  ngOnInit() {
    this.loadExistingReview();
  }

  ngOnChanges() {
    this.loadExistingReview();
    // Close form after successful submission
    if (this.submissionSuccess && this.showForm) {
      setTimeout(() => {
        this.showForm = false;
        this.submissionSuccess = false;
      }, 2000);
    }
  }

  private loadExistingReview() {
    if (this.existingReview) {
      this.rating = this.existingReview.rating;
      this.review = this.existingReview.comment || '';
    } else {
      this.rating = 0;
      this.review = '';
    }
  }

  get isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  get isEditMode(): boolean {
    return this.existingReview !== null;
  }

  toggleForm() {
    if (!this.isLoggedIn) {
      return;
    }
    
    this.showForm = !this.showForm;
    if (this.showForm) {
      this.loadExistingReview();
      this.submissionError = '';
    }
  }

  onRatingChange(newRating: number) {
    this.rating = newRating;
  }

  async onSubmit() {
    if (this.rating === 0 || this.isSubmitting) {
      return;
    }

    this.isSubmitting = true;
    this.submissionError = '';

    try {
      if (this.isEditMode && this.existingReview) {
        // Update existing review
        const updatedReview = await this.reviewService.updateReview(
          this.existingReview.id, 
          this.rating, 
          this.review.trim()
        ).toPromise();

        if (updatedReview) {
          this.submissionSuccess = true;
          this.reviewUpdated.emit(updatedReview);
        } else {
          this.submissionError = 'Failed to update review. Please try again.';
        }
      } else {
        // Create new review
        const newReview = await this.reviewService.submitReview(
          this.bookId, 
          this.rating, 
          this.review.trim(),
          this.orderItemId
        ).toPromise();

        if (newReview) {
          this.submissionSuccess = true;
          this.reviewSubmitted.emit(newReview);
        } else {
          this.submissionError = 'Failed to submit review. Please try again.';
        }
      }
    } catch (error) {
      this.submissionError = 'An error occurred. Please try again.';
      console.error('Review submission error:', error);
    } finally {
      this.isSubmitting = false;
    }
  }

  async onDelete() {
    if (!this.existingReview || this.isSubmitting) {
      return;
    }

    if (!confirm('Are you sure you want to delete this review?')) {
      return;
    }

    this.isSubmitting = true;
    this.submissionError = '';

    try {
      const success = await this.reviewService.deleteReview(this.existingReview.id).toPromise();
      
      if (success) {
        this.reviewDeleted.emit();
        this.showForm = false;
      } else {
        this.submissionError = 'Failed to delete review. Please try again.';
      }
    } catch (error) {
      this.submissionError = 'An error occurred. Please try again.';
      console.error('Review deletion error:', error);
    } finally {
      this.isSubmitting = false;
    }
  }

  onCancel() {
    this.loadExistingReview();
    this.showForm = false;
    this.submissionError = '';
    this.cancel.emit();
  }

  get isFormValid(): boolean {
    return this.rating > 0;
  }

  get hasChanges(): boolean {
    if (!this.existingReview) {
      return this.rating > 0 || this.review.trim().length > 0;
    }
    return this.rating !== this.existingReview.rating || 
           this.review.trim() !== (this.existingReview.comment || '');
  }

  getRatingText(rating: number): string {
    return this.reviewService.getRatingText(rating);
  }
} 