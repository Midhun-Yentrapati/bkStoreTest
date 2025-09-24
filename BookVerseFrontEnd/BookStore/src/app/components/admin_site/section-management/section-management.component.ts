import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BookService } from '../../../services/book.service';
import { BookModel } from '../../../models/book.model';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-section-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './section-management.component.html',
  styleUrls: ['./section-management.component.css']
})
export class SectionManagementComponent implements OnInit {
  
  allBooks: BookModel[] = []
  
  // UPDATED: Changed from BookWithSales[] to BookModel[] to match new service return type
  newlyLaunchedBooks: BookModel[] = [];
  bestSellers: BookModel[] = [];
  specialOffers: BookModel[] = [];

  selectedBookId: string = '';
  selectedCategory: 'newly launched' | 'highly rated' | 'special offers' = 'newly launched';

  isLoading: boolean = false;
  message: string = '';
  messageType: 'success' | 'error' = 'success';

  constructor(private bookService: BookService) { }

  ngOnInit(): void {
    this.loadAllData();
  }

  loadAllData(): void {
    this.isLoading = true;
    
    // Load all books and section data (admin sees all books including inactive)
    forkJoin({
      allBooks: this.bookService.getAllBooksForAdmin(),
      newlyLaunched: this.bookService.getNewlyLaunchedBooks(),
      bestSellers: this.bookService.getBestSellers(),
      specialOffers: this.bookService.getSpecialOffers()
    }).subscribe({
      next: (data) => {
        this.allBooks = data.allBooks;
        this.newlyLaunchedBooks = data.newlyLaunched;
        this.bestSellers = data.bestSellers;
        this.specialOffers = data.specialOffers;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading data:', error);
        this.isLoading = false;
        this.showMessage('Error loading data', 'error');
      }
    });
  }

  // DISABLED: These methods are no longer available with Spring backend
  // The Spring backend manages sales categories differently
  addBookToSection(): void {
    this.showMessage('Section management is now handled through the Spring backend. Please update book sales categories directly in the book edit form.', 'error');
    
    // COMMENTED OUT - Old implementation using JSON server
    // if (!this.selectedBookId || !this.selectedCategory) {
    //   this.showMessage('Please select both a book and category', 'error');
    //   return;
    // }
    
    // this.isLoading = true;
    // this.bookService.addBookToCategory(this.selectedBookId, this.selectedCategory).subscribe({
    //   next: () => {
    //     this.showMessage('Book added to section successfully!', 'success');
    //     this.loadAllData();
    //     this.selectedBookId = '';
    //   },
    //   error: (error) => {
    //     console.error('Error adding book to section:', error);
    //     this.showMessage('Error adding book to section', 'error');
    //     this.isLoading = false;
    //   }
    // });
  }

  removeBookFromSection(bookId: string, category: 'newly launched' | 'highly rated' | 'special offers'): void {
    this.showMessage('Section management is now handled through the Spring backend. Please update book sales categories directly in the book edit form.', 'error');
    
    // COMMENTED OUT - Old implementation using JSON server
    // this.isLoading = true;
    // this.bookService.removeBookFromCategory(bookId, category).subscribe({
    //   next: () => {
    //     this.showMessage('Book removed from section successfully!', 'success');
    //     this.loadAllData();
    //   },
    //   error: (error) => {
    //     console.error('Error removing book from section:', error);
    //     this.showMessage('Error removing book from section', 'error');
    //     this.isLoading = false;
    //   }
    // });
  }

  private showMessage(message: string, type: 'success' | 'error'): void {
    this.message = message;
    this.messageType = type;
    setTimeout(() => {
      this.message = '';
    }, 5000);
  }

  // Helper methods for template
  getAvailableBooks(): BookModel[] {
    return this.allBooks.filter(book => 
      !this.newlyLaunchedBooks.some(nb => nb.id === book.id) &&
      !this.bestSellers.some(bs => bs.id === book.id) &&
      !this.specialOffers.some(so => so.id === book.id)
    );
  }

  getCategoryDisplayName(category: string): string {
    switch(category) {
      case 'newly launched': return 'Newly Launched';
      case 'highly rated': return 'Best Sellers';
      case 'special offers': return 'Special Offers';
      default: return category;
    }
  }

  // Add missing properties and methods for template compatibility
  categories: ('newly launched' | 'highly rated' | 'special offers')[] = ['newly launched', 'highly rated', 'special offers'];

  getBooksForCategory(category: 'newly launched' | 'highly rated' | 'special offers'): BookModel[] {
    switch (category) {
      case 'newly launched':
        return this.newlyLaunchedBooks;
      case 'highly rated':
        return this.bestSellers;
      case 'special offers':
        return this.specialOffers;
      default:
        return [];
    }
  }

  getAvailableBooksForCategory(category: 'newly launched' | 'highly rated' | 'special offers'): BookModel[] {
    const currentBookIds = this.getBooksForCategory(category).map(book => book.id);
    return this.allBooks.filter(book => !currentBookIds.includes(book.id));
  }
} 