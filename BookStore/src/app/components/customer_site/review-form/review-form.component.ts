import { Component, Input, Output, EventEmitter, OnInit, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StarRatingComponent } from '../star-rating/star-rating.component';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-review-form',
  imports: [CommonModule, FormsModule, StarRatingComponent],
  templateUrl: './review-form.component.html',
  styleUrl: './review-form.component.css'
})
export class ReviewFormComponent implements OnInit, OnChanges {
  @Input() bookTitle: string = '';
  @Input() existingRating: number = 0;
  @Input() existingReview: string = '';
  @Input() isSubmitting: boolean = false;
  @Input() submissionSuccess: boolean = false;
  @Output() reviewSubmit = new EventEmitter<{rating: number, review: string}>();
  @Output() cancel = new EventEmitter<void>();

  rating: number = 0;
  review: string = '';
  showForm: boolean = false;

  constructor(private authService: AuthService) {}

  ngOnInit() {
    this.rating = this.existingRating;
    this.review = this.existingReview;
  }

  ngOnChanges() {
    // Close form after successful submission
    if (this.submissionSuccess && this.showForm) {
      this.showForm = false;
      this.rating = this.existingRating;
      this.review = this.existingReview;
    }
  }

  get isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }



  toggleForm() {
    if (!this.isLoggedIn) {
      // Don't toggle form if user is not logged in
      return;
    }
    
    this.showForm = !this.showForm;
    if (this.showForm) {
      // Reset form when opening
      this.rating = this.existingRating;
      this.review = this.existingReview;
    }
  }

  onRatingChange(newRating: number) {
    this.rating = newRating;
  }

  onSubmit() {
    if (this.rating === 0) {
      return;
    }

    this.reviewSubmit.emit({
      rating: this.rating,
      review: this.review.trim()
    });
  }

  onCancel() {
    this.rating = this.existingRating;
    this.review = this.existingReview;
    this.showForm = false;
    this.cancel.emit();
  }

  get isFormValid(): boolean {
    return this.rating > 0;
  }

  get hasChanges(): boolean {
    // For new reviews, consider it as having changes if rating is selected
    if (this.existingRating === 0 && this.rating > 0) {
      return true;
    }
    return this.rating !== this.existingRating || this.review.trim() !== this.existingReview;
  }

  getRatingText(rating: number): string {
    switch (rating) {
      case 1: return 'Poor';
      case 2: return 'Fair';
      case 3: return 'Good';
      case 4: return 'Very Good';
      case 5: return 'Excellent';
      default: return '';
    }
  }
} 