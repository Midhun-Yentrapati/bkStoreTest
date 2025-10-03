import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BookModel, BookUtils, CustomerRating } from '../../../models/book.model';
import { BookService } from '../../../services/book.service';
import { CartService } from '../../../services/cart.service';
import { WishlistService } from '../../../services/wishlist.service';
import { NotificationService } from '../../../services/notification.service';
import { ReviewService } from '../../../services/review.service';
import { AuthService } from '../../../services/auth.service';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { ReviewListComponent } from '../review-list/review-list.component';
import { ReviewFormComponent } from '../review-form/review-form.component';
import { StarRatingComponent } from '../star-rating/star-rating.component';
import { ShareModalComponent } from '../share-modal/share-modal.component';
import { HorizontalBookSectionComponent } from '../horizontal-book-section/horizontal-book-section.component';

@Component({
  selector: 'app-book-detail',
  imports: [CommonModule, ReviewListComponent, ReviewFormComponent, ShareModalComponent, HorizontalBookSectionComponent],
  templateUrl: './book-detail.component.html',
  styleUrl: './book-detail.component.css'
})
export class BookDetailComponent implements OnInit {

  id!: string;
  book!: BookModel;
  similarBooks: BookModel[] = [];
  isInWishlist$!: Observable<boolean>;
  addingToCart = false;
  addingToWishlist = false;
  loadingSimilarBooks = false;

  // Image gallery properties
  selectedImageIndex = 0;
  showImageModal = false;

  // Share modal properties
  showShareModal = false;

  // Review properties
  bookReviews: CustomerRating[] = [];
  userReview: CustomerRating | null = null;
  loadingReviews = false;
  submittingReview = false;
  reviewSubmissionSuccess = false;

  constructor( 
    private bookService: BookService,
    private cartService: CartService,
    private wishlistService: WishlistService,
    private notificationService: NotificationService,
    private reviewService: ReviewService,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
   ) { }

   ngOnInit(): void {
    this.id = String(this.route.snapshot.paramMap.get('id'));
    this.loadBookData();
    this.loadReviews();
   }

   // Image gallery methods
   selectImage(index: number): void {
     if (this.book && this.book.image_urls && index >= 0 && index < this.book.image_urls.length) {
       this.selectedImageIndex = index;
     }
   }

   nextImage(): void {
     if (this.book && this.book.image_urls) {
       const nextIndex = (this.selectedImageIndex + 1) % this.book.image_urls.length;
       this.selectImage(nextIndex);
     }
   }

   previousImage(): void {
     if (this.book && this.book.image_urls) {
       const prevIndex = this.selectedImageIndex === 0 
         ? this.book.image_urls.length - 1 
         : this.selectedImageIndex - 1;
       this.selectImage(prevIndex);
     }
   }

   openImageModal(): void {
     this.showImageModal = true;
   }

   closeImageModal(): void {
     this.showImageModal = false;
   }

   getCurrentImage(): string {
     return this.book?.image_urls?.[this.selectedImageIndex] || this.book?.image_urls?.[0] || '';
   }

   hasMultipleImages(): boolean {
     return (this.book?.image_urls?.length || 0) > 1;
   }

  loadSimilarBooks(): void {
    if (!this.book || !this.book.id) {
      this.loadingSimilarBooks = false;
      return;
    }
    
    this.loadingSimilarBooks = true;
    this.bookService.getSimilarBooks(this.book.id).subscribe({
      next: (books: BookModel[]) => {
        this.similarBooks = books.filter((b: BookModel) => b.id !== this.book.id).slice(0, 10);
        this.loadingSimilarBooks = false;
      },
      error: (error: any) => {
        console.error('Error loading similar books:', error);
        this.loadingSimilarBooks = false;
      }
    });
  }

  addToCart(): void {
    if (!this.book || this.addingToCart) return;
    
    this.addingToCart = true;
    this.cartService.addToCart(this.book).subscribe({
      next: (cartItem) => {
        console.log(`Added ${this.book.title} to cart`);
        this.notificationService.cartSuccess(this.book.title);
        this.addingToCart = false;
      },
      error: (error) => {
        console.error('Error adding to cart:', error);
        this.notificationService.cartError(this.book.title, error.message);
        this.addingToCart = false;
      }
    });
  }

  addToWishlist(): void {
    if (!this.book || this.addingToWishlist) return;
    
    this.addingToWishlist = true;
    this.wishlistService.addToWishlist(this.book).subscribe({
      next: (book) => {
        console.log(`Added ${this.book.title} to wishlist`);
        this.notificationService.wishlistSuccess(this.book.title);
        this.addingToWishlist = false;
        // Update wishlist status
        this.isInWishlist$ = this.wishlistService.isInWishlist(this.book.id);
      },
      error: (error) => {
        console.error('Error adding to wishlist:', error);
        this.notificationService.wishlistError(this.book.title, error.message);
        this.addingToWishlist = false;
      }
    });
  }

  removeFromWishlist(): void {
    if (!this.book || this.addingToWishlist) return;
    
    this.addingToWishlist = true;
    this.wishlistService.removeBookFromWishlist(this.book.id).subscribe({
      next: () => {
        console.log(`Removed ${this.book.title} from wishlist`);
        this.notificationService.success('Removed from Wishlist', `"${this.book.title}" has been removed from your wishlist.`);
        this.addingToWishlist = false;
        // Update wishlist status
        this.isInWishlist$ = this.wishlistService.isInWishlist(this.book.id);
      },
      error: (error) => {
        console.error('Error removing from wishlist:', error);
        this.notificationService.error('Wishlist Error', 'Failed to remove from wishlist. Please try again.');
        this.addingToWishlist = false;
      }
    });
     }

  navigateToBook(bookId: string | number): void {
    const stringId = bookId.toString();
    // Update the URL without refreshing the page
    this.router.navigate(['/book', stringId], { replaceUrl: true });
    
    // Update the current book ID and reload all data
    this.id = stringId;
    this.loadBookData();
    
    // Scroll to top
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
  
  private loadBookData(): void {
    console.log('Loading book data for ID:', this.id);
    
    // Use getBookWithRelations to get complete book data including categories and images
    this.bookService.getBookWithRelations(this.id).subscribe({
      next: (book) => {
        console.log('Book loaded successfully:', book);
        if (book) {
          this.book = book;
          this.isInWishlist$ = this.wishlistService.isInWishlist(this.book.id);
          this.loadSimilarBooks();
          // Reset image selection when book changes
          this.selectedImageIndex = 0;
        } else {
          console.error('Book not found');
          this.router.navigate(['/']);
        }
      },
      error: (error) => {
        console.error('Error loading book:', error);
        if (error.status === 404) {
          // Book not found - redirect to home page
          console.log('BookDetailComponent: Book not found (404), redirecting to home');
          this.router.navigate(['/']);
        } else {
          // Handle other errors - redirect to home page
          console.error('Failed to load book details');
          this.router.navigate(['/']);
        }
      }
    });
  }

  
  // Share modal methods
  openShareModal(): void {
    this.showShareModal = true;
  }

  closeShareModal(): void {
    this.showShareModal = false;
  }

  onShareSuccess(platform: string): void {
    console.log('Shared on:', platform);
    this.notificationService.success('Shared Successfully', `Book shared on ${platform}!`);
    this.closeShareModal();
  }

  // New utility methods for enhanced book details
  getCategoryNames(): string[] {
    return BookUtils.getCategoryNames(this.book);
  }

  formatSalesCategory(category: string): string {
    switch(category) {
      case 'BEST_SELLING': return 'Best Seller';
      case 'NEWLY_LAUNCHED': return 'New Launch';
      case 'SPECIAL_OFFERS': return 'Special Offer';
      default: return category;
    }
  }

  getStarArray(rating: number): number[] {
    return Array(5).fill(0).map((_, i) => i + 1);
  }

  getDiscountPercentage(): number {
    if (!this.book.mrp || !this.book.price) return 0;
    return Math.round(((this.book.mrp - this.book.price) / this.book.mrp) * 100);
  }

  formatDate(dateString: string): string {
    try {
      // Handle different date formats from backend
      let date: Date;
      if (Array.isArray(dateString)) {
        // Handle LocalDateTime array format [year, month, day, hour, minute, second]
        const [year, month, day] = dateString as any;
        date = new Date(year, month - 1, day);
      } else {
        date = new Date(dateString);
      }
      
      return date.toLocaleDateString('en-US', { 
        year: 'numeric', 
        month: 'long', 
        day: 'numeric' 
      });
    } catch (error) {
      return dateString.toString();
    }
  }

  // Review Methods
  loadReviews(): void {
    this.loadingReviews = true;
    
    // Load book reviews (public endpoint)
    this.reviewService.getBookReviews(this.id).subscribe({
      next: (reviews) => {
        this.bookReviews = reviews;
        this.loadingReviews = false;
        console.log('Reviews loaded:', reviews.length);
      },
      error: (error) => {
        console.error('Error loading reviews:', error);
        this.bookReviews = []; // Set empty array on error
        this.loadingReviews = false;
      }
    });

    // Load user's existing review if logged in (requires auth)
    if (this.authService.isLoggedIn()) {
      this.reviewService.getUserReview(this.id).subscribe({
        next: (review) => {
          this.userReview = review;
          console.log('User review loaded:', review);
        },
        error: (error) => {
          console.log('No user review found or error loading user review:', error.status);
          this.userReview = null;
        }
      });
    } else {
      console.log('User not logged in, skipping user review load');
      this.userReview = null;
    }
  }

  onReviewSubmit(reviewData: { rating: number; review: string }): void {
    this.submittingReview = true;
    this.reviewSubmissionSuccess = false;

    if (this.userReview) {
      // Update existing review
      this.reviewService.updateReview(this.userReview.id!, reviewData.rating, reviewData.review).subscribe({
        next: (success) => {
          this.submittingReview = false;
          this.reviewSubmissionSuccess = success;
          if (success) {
            this.notificationService.success('Review Updated', 'Review updated successfully!');
            this.loadReviews(); // Reload reviews
                      } else {
              this.notificationService.error('Update Failed', 'Failed to update review. Please try again.');
            }
          },
          error: (error) => {
            console.error('Error updating review:', error);
            this.submittingReview = false;
            this.notificationService.error('Update Failed', 'Failed to update review. Please try again.');
          }
        });
      } else {
        // Submit new review
        this.reviewService.submitReview(this.id, reviewData.rating, reviewData.review).subscribe({
          next: (success) => {
            this.submittingReview = false;
            this.reviewSubmissionSuccess = success;
            if (success) {
              this.notificationService.success('Review Submitted', 'Review submitted successfully!');
              this.loadReviews(); // Reload reviews
            } else {
              this.notificationService.error('Submission Failed', 'Failed to submit review. Please try again.');
            }
          },
          error: (error) => {
            console.error('Error submitting review:', error);
            this.submittingReview = false;
            this.notificationService.error('Submission Failed', 'Failed to submit review. Please try again.');
          }
        });
      }
    }

    onReviewCancel(): void {
      this.reviewSubmissionSuccess = false;
    }

    deleteUserReview(): void {
      if (!this.userReview) return;

      if (confirm('Are you sure you want to delete your review?')) {
        this.reviewService.deleteReview(this.userReview.id!).subscribe({
          next: (success) => {
            if (success) {
              this.notificationService.success('Review Deleted', 'Review deleted successfully!');
              this.loadReviews(); // Reload reviews
            } else {
              this.notificationService.error('Delete Failed', 'Failed to delete review. Please try again.');
            }
          },
          error: (error) => {
            console.error('Error deleting review:', error);
            this.notificationService.error('Delete Failed', 'Failed to delete review. Please try again.');
          }
        });
      }
    }
}
