import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BookModel } from '../../../models/book.model';
import { BookService } from '../../../services/book.service';
import { CartService } from '../../../services/cart.service';
import { WishlistService } from '../../../services/wishlist.service';
import { NotificationService } from '../../../services/notification.service';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { ReviewListComponent } from '../review-list/review-list.component';
import { ReviewFormComponent } from '../review-form/review-form.component';
import { StarRatingComponent } from '../star-rating/star-rating.component';
import { ShareModalComponent } from '../share-modal/share-modal.component';

@Component({
  selector: 'app-book-detail',
  imports: [CommonModule, ReviewListComponent, ReviewFormComponent, ShareModalComponent],
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

  constructor( 
    private bookService: BookService,
    private cartService: CartService,
    private wishlistService: WishlistService,
    private notificationService: NotificationService,
    private route: ActivatedRoute,
    private router: Router
   ) { }

   ngOnInit(): void {
    this.id = String(this.route.snapshot.paramMap.get('id'));
    this.loadBookData();
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
    
    this.bookService.getBookById(this.id).subscribe({
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
}
