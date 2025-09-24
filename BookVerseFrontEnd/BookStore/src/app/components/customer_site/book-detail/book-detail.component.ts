import { Component, OnInit } from '@angular/core';
import { CommonModule, NgIf } from '@angular/common';
import { RouterModule } from '@angular/router';
import { BookModel } from '../../../models/book.model';
import { BookService } from '../../../services/book.service';
import { CartService } from '../../../services/cart.service';
import { WishlistService } from '../../../services/wishlist.service';
import { NotificationService } from '../../../services/notification.service';
import { ReviewService, ReviewResponse, ReviewStats } from '../../../services/review.service';
import { AuthService } from '../../../services/auth.service';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { ReviewFormComponent } from '../review-form/review-form.component';
import { ReviewListComponent } from '../review-list/review-list.component';
import { ShareModalComponent } from '../share-modal/share-modal.component';
import { BookCardComponent } from '../book-card/book-card.component';

@Component({
  selector: 'app-book-detail',
  imports: [CommonModule, RouterModule, ReviewFormComponent, ReviewListComponent, ShareModalComponent, BookCardComponent, NgIf],
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
  loading = true;

  // Review properties
  reviews: ReviewResponse[] = [];
  userReview: ReviewResponse | null = null;
  reviewStats: ReviewStats | null = null;
  loadingReviews = false;
  canUserReview = false;

  // Image gallery properties
  selectedImageIndex = 0;
  showImageModal = false;

  // Share modal properties
  showShareModal = false;

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
    console.log('BookDetailComponent: Loading book with ID:', this.id);
    
    // Use getBookWithRelations to get complete book data including categories and images
    this.bookService.getBookWithRelations(this.id).subscribe({
      next: (book) => {
        console.log('BookDetailComponent: Book loaded successfully:', book);
        if (book) {
          this.book = book;
          this.loading = false;
          this.isInWishlist$ = this.wishlistService.isInWishlist(this.book.id);
          this.loadSimilarBooks();
          this.loadReviews();
          // Reset image selection when book changes
          this.selectedImageIndex = 0;
        } else {
          console.error('Book not found');
          this.loading = false;
          this.router.navigate(['/']);
        }
      },
      error: (error) => {
        console.error('BookDetailComponent: Error fetching book:', error);
        this.loading = false;
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

  // Utility methods for new features
  getDiscountPercentage(): number {
    if (this.book?.mrp && this.book?.price && this.book.mrp > this.book.price) {
      return Math.round(((this.book.mrp - this.book.price) / this.book.mrp) * 100);
    }
    return 0;
  }

  formatDate(dateString: string | undefined): string {
    if (!dateString) return '';
    
    try {
      // Handle different date formats
      let date: Date;
      
      // If it's already a valid date string, parse it
      if (typeof dateString === 'string') {
        // Handle ISO date strings and other formats
        date = new Date(dateString);
        
        // If parsing failed, try to handle YYYY-MM-DD format specifically
        if (isNaN(date.getTime()) && dateString.includes('-')) {
          const parts = dateString.split('-');
          if (parts.length === 3) {
            date = new Date(parseInt(parts[0]), parseInt(parts[1]) - 1, parseInt(parts[2]));
          }
        }
      } else {
        date = new Date(dateString);
      }
      
      // Check if date is valid
      if (isNaN(date.getTime())) {
        console.warn('Invalid date string:', dateString);
        return 'Invalid Date';
      }
      
      return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      });
    } catch (error) {
      console.error('Error formatting date:', error, dateString);
      return dateString || 'Invalid Date';
    }
  }

  formatSalesCategory(category: string): string {
    if (!category) return '';
    return category
      .split('_')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
      .join(' ');
  }

   // Review methods
   private loadReviews(): void {
     if (!this.book) return;

     this.loadingReviews = true;
     const bookId = parseInt(this.book.id);

     // Load reviews
     this.reviewService.getBookReviews(bookId).subscribe({
       next: (reviews) => {
         this.reviews = reviews;
         this.loadingReviews = false;
       },
       error: (error) => {
         console.error('Failed to load reviews:', error);
         this.loadingReviews = false;
       }
     });

     // Load review stats
     this.reviewService.getReviewStatistics(bookId).subscribe({
       next: (stats) => {
         this.reviewStats = stats;
       },
       error: (error) => {
         console.error('Failed to load review stats:', error);
       }
     });

     // Load user's review if logged in
     if (this.authService.isLoggedIn()) {
       this.reviewService.getUserReview(bookId).subscribe({
         next: (userReview) => {
           this.userReview = userReview;
         },
         error: (error) => {
           console.error('Failed to load user review:', error);
         }
       });

       // Check if user can review
       this.reviewService.canUserReview(bookId).subscribe({
         next: (canReview) => {
           this.canUserReview = canReview;
         },
         error: (error) => {
           console.error('Failed to check if user can review:', error);
         }
       });
     }
   }

   onReviewSubmitted(review: ReviewResponse): void {
     this.userReview = review;
     this.canUserReview = false;
     this.loadReviews(); // Refresh reviews list
     this.notificationService.success('Review Submitted', 'Your review has been submitted successfully!');
   }

   onReviewUpdated(review: ReviewResponse): void {
     this.userReview = review;
     this.loadReviews(); // Refresh reviews list
     this.notificationService.success('Review Updated', 'Your review has been updated successfully!');
   }

   onReviewDeleted(): void {
     this.userReview = null;
     this.canUserReview = true;
     this.loadReviews(); // Refresh reviews list
     this.notificationService.success('Review Deleted', 'Your review has been deleted successfully!');
   }

   get isLoggedIn(): boolean {
     return this.authService.isLoggedIn();
   }

   getBookIdAsNumber(): number {
     return parseInt(this.book.id);
   }

   // Image gallery methods
   selectImage(index: number): void {
     if (this.book && this.book.images && index >= 0 && index < this.book.images.length) {
       this.selectedImageIndex = index;
     }
   }

   nextImage(): void {
         if (this.book && this.book.images) {
      const nextIndex = (this.selectedImageIndex + 1) % this.book.images.length;
       this.selectImage(nextIndex);
     }
   }

   previousImage(): void {
         if (this.book && this.book.images) {
      const prevIndex = this.selectedImageIndex === 0 
        ? this.book.images.length - 1 
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
     if (this.book?.images && this.book.images.length > 0) {
      const selectedImage = this.book.images[this.selectedImageIndex] || this.book.images[0];
      return selectedImage.imageUrl;
    }
    return '';
   }

   hasMultipleImages(): boolean {
     return this.book?.images?.length > 1;
   }

  getSimilarBookImage(book: BookModel): string {
    if (book.images && book.images.length > 0) {
      if (book.images.length > 1) {
        // If multiple images, get the primary image or fallback to first
        const primaryImage = book.images.find(img => img.isPrimary) || book.images[0];
        return primaryImage.imageUrl;
      } else {
        // If only one image, use it regardless of isPrimary status
        return book.images[0].imageUrl;
      }
    }
    return 'https://placehold.co/200x300?text=No+Image';
  }

  loadSimilarBooks(): void {
    if (!this.book || !this.book.id) {
      this.loadingSimilarBooks = false;
      return;
    }
    
    this.loadingSimilarBooks = true;
    this.bookService.getSimilarBooks(this.book.id).subscribe({
      next: (books: BookModel[]) => {
        this.similarBooks = books.filter((b: BookModel) => b.id !== this.book.id).slice(0, 6);
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

  navigateToBook(bookId: string): void {
    // Update the URL without refreshing the page
    this.router.navigate(['/book', bookId], { replaceUrl: true });
    
    // Update the current book ID and reload all data
    this.id = bookId;
    this.loadBookData();
    
    // Scroll to top
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
  
  private loadBookData(): void {
    console.log('Loading book data for ID:', this.id);
    
    // Use getBookWithRelations to get complete book data
    this.bookService.getBookWithRelations(this.id).subscribe({
      next: (book) => {
        console.log('Book loaded successfully:', book);
        if (book) {
          this.book = book;
          this.isInWishlist$ = this.wishlistService.isInWishlist(this.book.id);
          this.loadSimilarBooks();
          this.loadReviews(); // Reload reviews when book changes
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

  // Review handling methods
  onReviewSubmit(reviewData: { rating: number; review: string }): void {
    console.log('Review submitted:', reviewData);
    // In a real implementation, you would call a review service here
    // For now, just show a success message
    this.notificationService.success('Review Submitted', 'Thank you for your review!');
  }

  onReviewCancel(): void {
    console.log('Review cancelled');
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

  onImageError(event: any): void {
    console.log('Image failed to load for similar book. URL:', event.target.src);
    event.target.src = 'https://placehold.co/200x300?text=No+Image';
  }
}
