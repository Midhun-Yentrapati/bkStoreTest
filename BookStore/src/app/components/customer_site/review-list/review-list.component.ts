import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CustomerRating } from '../../../models/book.model';
import { StarRatingComponent } from '../star-rating/star-rating.component';

@Component({
  selector: 'app-review-list',
  imports: [CommonModule, StarRatingComponent],
  templateUrl: './review-list.component.html',
  styleUrl: './review-list.component.css'
})
export class ReviewListComponent {
  @Input() reviews: CustomerRating[] = [];
  @Input() bookTitle: string = '';

  get reviewsWithText() {
    return this.reviews.filter(review => review.review && review.review.trim().length > 0);
  }

  get averageRating() {
    if (this.reviews.length === 0) return 0;
    const total = this.reviews.reduce((sum, review) => sum + review.rating, 0);
    return Math.round((total / this.reviews.length) * 10) / 10;
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { 
      year: 'numeric', 
      month: 'long', 
      day: 'numeric' 
    });
  }

  getInitials(name: string): string {
    return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
  }
} 