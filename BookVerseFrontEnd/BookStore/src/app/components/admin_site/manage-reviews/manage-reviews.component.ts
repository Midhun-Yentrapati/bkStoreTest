import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BookService } from '../../../services/book.service';

interface ReviewRow {
  bookId: string | number;
  bookTitle: string;
  userId: string;
  userName: string;
  rating: number;
  review: string;
  createdAt: string;
}

@Component({
  selector: 'app-manage-reviews',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './manage-reviews.component.html',
  styleUrls: ['./manage-reviews.component.css']
})
export class ManageReviewsComponent {
  showTable = false;
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  rows: ReviewRow[] = [];
  searchTerm: string = '';

  // sorting and pagination
  sortOption: 'none' | 'ratingAsc' | 'ratingDesc' = 'none';
  pageSize: number = 10;
  currentPage: number = 1;

  constructor(private bookService: BookService) {}

  get filteredRows(): ReviewRow[] {
    const term = this.searchTerm.trim().toLowerCase();
    const base = term
      ? this.rows.filter(r => r.bookTitle.toLowerCase().includes(term))
      : this.rows;
    return base;
  }

  get sortedRows(): ReviewRow[] {
    const base = this.filteredRows;
    if (this.sortOption === 'ratingAsc') {
      return base.slice().sort((a, b) => a.rating - b.rating);
    }
    if (this.sortOption === 'ratingDesc') {
      return base.slice().sort((a, b) => b.rating - a.rating);
    }
    return base; // default: keep original recency order
  }

  get totalItems(): number {
    return this.sortedRows.length;
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.totalItems / this.pageSize));
  }

  get pagedRows(): ReviewRow[] {
    const safeCurrent = Math.min(this.currentPage, this.totalPages);
    const start = (safeCurrent - 1) * this.pageSize;
    return this.sortedRows.slice(start, start + this.pageSize);
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  onSearchChange(): void {
    this.currentPage = 1;
  }

  onSortChange(): void {
    this.currentPage = 1;
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
    }
  }

  prevPage(): void {
    if (this.currentPage > 1) this.currentPage -= 1;
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) this.currentPage += 1;
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

    this.bookService.getAllBooks().subscribe({
      next: books => {
        const list: ReviewRow[] = [];
        for (const b of books) {
          for (const r of (b.customerRatings || [])) {
            list.push({
              bookId: b.id,
              bookTitle: b.title,
              userId: r.userId,
              userName: r.userName,
              rating: r.rating,
              review: r.review || '',
              createdAt: r.createdAt
            });
          }
        }
        // default order: newest first
        list.sort((a, b) => b.createdAt.localeCompare(a.createdAt));
        this.rows = list;
        this.currentPage = 1;
      },
      error: err => {
        console.error('Failed to load reviews', err);
        this.errorMessage = 'Failed to load reviews. Please try again later.';
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }

  deleteReview(row: ReviewRow): void {
    if (!confirm(`Delete review by "${row.userName}" for "${row.bookTitle}"?`)) {
      return;
    }
    this.errorMessage = '';
    this.successMessage = '';
    this.bookService.deleteReview(row.bookId, row.userId).subscribe({
      next: () => {
        this.rows = this.rows.filter(r => !(r.bookId === row.bookId && r.userId === row.userId));
        this.successMessage = 'Review deleted successfully.';
        if ((this.currentPage - 1) * this.pageSize >= this.totalItems) {
          this.currentPage = Math.max(1, this.currentPage - 1);
        }
        setTimeout(() => { this.successMessage = ''; }, 2000);
      },
      error: err => {
        console.error('Delete review failed', err);
        this.errorMessage = 'Failed to delete review. Please try again.';
        setTimeout(() => { this.errorMessage = ''; }, 3000);
      }
    });
  }
} 