import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReviewService, ReviewResponse } from '../../../services/review.service';
import { AuthService } from '../../../services/auth.service';

interface ReviewRow {
  id: number;
  bookId: number;
  bookTitle?: string;
  userId: string;
  userName: string;
  rating: number;
  comment: string;
  status: string;
  isVerifiedPurchase: boolean;
  createdAt: string;
  updatedAt: string;
}

@Component({
  selector: 'app-manage-reviews',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './manage-reviews.component.html',
  styleUrls: ['./manage-reviews.component.css']
})
export class ManageReviewsComponent implements OnInit {
  showTable = false;
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  rows: ReviewRow[] = [];
  searchTerm: string = '';

  // Sorting and pagination
  sortOption: 'none' | 'ratingAsc' | 'ratingDesc' | 'dateAsc' | 'dateDesc' = 'dateDesc';
  pageSize: number = 10;
  currentPage: number = 1;
  totalElements: number = 0;
  totalPages: number = 0;

  // Filter options
  statusFilter: string = 'all';
  ratingFilter: number = 0;

  // Available statuses for moderation
  availableStatuses = [
    { value: 'PENDING', label: 'Pending' },
    { value: 'APPROVED', label: 'Approved' },
    { value: 'REJECTED', label: 'Rejected' },
    { value: 'FLAGGED', label: 'Flagged' }
  ];

  constructor(
    private reviewService: ReviewService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadReviews();
  }

  get filteredRows(): ReviewRow[] {
    let filtered = this.rows;

    // Apply text search
    const term = this.searchTerm.trim().toLowerCase();
    if (term) {
      filtered = filtered.filter(r => 
        r.userName.toLowerCase().includes(term) ||
        r.comment.toLowerCase().includes(term) ||
        (r.bookTitle && r.bookTitle.toLowerCase().includes(term))
      );
    }

    // Apply status filter
    if (this.statusFilter !== 'all') {
      filtered = filtered.filter(r => r.status === this.statusFilter);
    }

    // Apply rating filter
    if (this.ratingFilter > 0) {
      filtered = filtered.filter(r => r.rating === this.ratingFilter);
    }

    return filtered;
  }

  get sortedRows(): ReviewRow[] {
    const base = this.filteredRows;
    switch (this.sortOption) {
      case 'ratingAsc':
        return base.slice().sort((a, b) => a.rating - b.rating);
      case 'ratingDesc':
        return base.slice().sort((a, b) => b.rating - a.rating);
      case 'dateAsc':
        return base.slice().sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime());
      case 'dateDesc':
        return base.slice().sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
      default:
        return base;
    }
  }

  get totalItems(): number {
    return this.sortedRows.length;
  }

  get pagedRows(): ReviewRow[] {
    const safeCurrent = Math.min(this.currentPage, Math.max(1, Math.ceil(this.totalItems / this.pageSize)));
    const start = (safeCurrent - 1) * this.pageSize;
    return this.sortedRows.slice(start, start + this.pageSize);
  }

  get pages(): number[] {
    const totalPages = Math.max(1, Math.ceil(this.totalItems / this.pageSize));
    return Array.from({ length: totalPages }, (_, i) => i + 1);
  }

  onSearchChange(): void {
    this.currentPage = 1;
  }

  onSortChange(): void {
    this.currentPage = 1;
  }

  onFilterChange(): void {
    this.currentPage = 1;
  }

  goToPage(page: number): void {
    const totalPages = Math.max(1, Math.ceil(this.totalItems / this.pageSize));
    if (page >= 1 && page <= totalPages) {
      this.currentPage = page;
    }
  }

  prevPage(): void {
    if (this.currentPage > 1) this.currentPage -= 1;
  }

  nextPage(): void {
    const totalPages = Math.max(1, Math.ceil(this.totalItems / this.pageSize));
    if (this.currentPage < totalPages) this.currentPage += 1;
  }

  toggleReviews(): void {
    this.showTable = !this.showTable;
    if (this.showTable && this.rows.length === 0) {
      this.loadReviews();
    }
  }

  private loadReviews(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.rows = [];

    // Use the admin endpoint to get all reviews for moderation
    this.reviewService.getAllReviewsForModeration(0, 100).subscribe({
      next: (response) => {
        if (response.content && Array.isArray(response.content)) {
          this.rows = response.content.map((review: ReviewResponse) => ({
            id: review.id,
            bookId: review.bookId,
            bookTitle: `Book ID: ${review.bookId}`, // TODO: Fetch book title
            userId: review.userId,
            userName: review.userName,
            rating: review.rating,
            comment: review.comment,
            status: review.status,
            isVerifiedPurchase: review.isVerifiedPurchase,
            createdAt: review.createdAt,
            updatedAt: review.updatedAt
          }));
          this.totalElements = response.totalElements || this.rows.length;
          this.totalPages = response.totalPages || Math.ceil(this.rows.length / this.pageSize);
        } else {
          this.rows = [];
        }
        this.currentPage = 1;
        console.log('Reviews loaded for moderation:', this.rows.length);
      },
      error: (error) => {
        console.error('Failed to load reviews for moderation:', error);
        this.errorMessage = 'Failed to load reviews. Please try again later.';
        this.isLoading = false;
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }

  moderateReview(reviewId: number, newStatus: string): void {
    const currentAdmin = this.authService.getCurrentAdmin();
    if (!currentAdmin) {
      this.errorMessage = 'Admin authentication required';
      return;
    }

    this.reviewService.moderateReview(reviewId, newStatus, currentAdmin.id).subscribe({
      next: (success) => {
        if (success) {
          // Update the review status in the local array
          const review = this.rows.find(r => r.id === reviewId);
          if (review) {
            review.status = newStatus;
          }
          this.successMessage = `Review ${newStatus.toLowerCase()} successfully.`;
          setTimeout(() => { this.successMessage = ''; }, 3000);
        } else {
          this.errorMessage = 'Failed to moderate review. Please try again.';
          setTimeout(() => { this.errorMessage = ''; }, 3000);
        }
      },
      error: (error) => {
        console.error('Failed to moderate review:', error);
        this.errorMessage = 'Failed to moderate review. Please try again.';
        setTimeout(() => { this.errorMessage = ''; }, 3000);
      }
    });
  }

  deleteReview(row: ReviewRow): void {
    if (!confirm(`Delete review by "${row.userName}" for book "${row.bookTitle}"?`)) {
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';

    this.reviewService.deleteReview(row.id).subscribe({
      next: (success) => {
        if (success) {
          this.rows = this.rows.filter(r => r.id !== row.id);
          this.successMessage = 'Review deleted successfully.';
          
          // Adjust current page if necessary
          const totalPages = Math.max(1, Math.ceil(this.rows.length / this.pageSize));
          if (this.currentPage > totalPages) {
            this.currentPage = totalPages;
          }
          
          setTimeout(() => { this.successMessage = ''; }, 2000);
        } else {
          this.errorMessage = 'Failed to delete review. Please try again.';
          setTimeout(() => { this.errorMessage = ''; }, 3000);
        }
      },
      error: (error) => {
        console.error('Delete review failed:', error);
        this.errorMessage = 'Failed to delete review. Please try again.';
        setTimeout(() => { this.errorMessage = ''; }, 3000);
      }
    });
  }

  refreshReviews(): void {
    this.loadReviews();
  }

  getStatusBadgeClass(status: string): string {
    switch (status.toUpperCase()) {
      case 'APPROVED': return 'badge bg-success';
      case 'PENDING': return 'badge bg-warning';
      case 'REJECTED': return 'badge bg-danger';
      case 'FLAGGED': return 'badge bg-info';
      default: return 'badge bg-secondary';
    }
  }

  getRatingStars(rating: number): string {
    return '★'.repeat(rating) + '☆'.repeat(5 - rating);
  }

  formatDate(dateString: string): string {
    try {
      return new Date(dateString).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch (error) {
      return dateString;
    }
  }

  showFullReview(row: ReviewRow): void {
    alert(`Full Review by ${row.userName}:\n\n${row.comment}`);
  }
} 