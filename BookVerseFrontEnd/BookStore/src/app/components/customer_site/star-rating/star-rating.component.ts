import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { NgFor, NgClass, NgIf } from '@angular/common';

@Component({
  selector: 'app-star-rating',
  imports: [NgFor, NgClass, NgIf],
  templateUrl: './star-rating.component.html',
  styleUrl: './star-rating.component.css'
})
export class StarRatingComponent implements OnInit {
  @Input() rating: number = 0;
  @Input() maxStars: number = 5;
  @Input() interactive: boolean = false;
  @Input() size: 'small' | 'medium' | 'large' = 'medium';
  @Output() ratingChange = new EventEmitter<number>();

  stars: number[] = [];
  hoverRating: number = 0;

  ngOnInit() {
    this.stars = Array.from({length: this.maxStars}, (_, i) => i + 1);
  }

  onStarClick(star: number) {
    if (this.interactive) {
      this.rating = star;
      this.ratingChange.emit(this.rating);
    }
  }

  onStarHover(star: number) {
    if (this.interactive) {
      this.hoverRating = star;
    }
  }

  onStarLeave() {
    if (this.interactive) {
      this.hoverRating = 0;
    }
  }

  getStarState(star: number): 'filled' | 'half' | 'empty' {
    const currentRating = this.hoverRating || this.rating;
    
    if (currentRating >= star) {
      return 'filled';
    } else if (currentRating >= star - 0.5) {
      return 'half';
    } else {
      return 'empty';
    }
  }
} 