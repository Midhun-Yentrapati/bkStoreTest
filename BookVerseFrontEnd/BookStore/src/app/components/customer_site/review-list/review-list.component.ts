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
    try {
      // Handle different date formats from backend
      let date: Date;
      
      if (!dateString) {
        return 'Date not available';
      }
      
      // Handle LocalDateTime array format [year, month, day, hour, minute, second]
      if (Array.isArray(dateString)) {
        const [year, month, day] = dateString as any;
        date = new Date(year, month - 1, day);
      } else if (typeof dateString === 'string') {
        // Handle ISO string format
        date = new Date(dateString);
      } else {
        // Handle timestamp or other formats
        date = new Date(dateString);
      }
      
      // Check if date is valid
      if (isNaN(date.getTime())) {
        console.warn('Invalid date format:', dateString);
        return 'Invalid date';
      }
      
      return date.toLocaleDateString('en-US', { 
        year: 'numeric', 
        month: 'long', 
        day: 'numeric' 
      });
    } catch (error) {
      console.error('Error formatting date:', error, 'Original value:', dateString);
      return 'Date unavailable';
    }
  }

  getInitials(name: string): string {
    return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
  }
} 