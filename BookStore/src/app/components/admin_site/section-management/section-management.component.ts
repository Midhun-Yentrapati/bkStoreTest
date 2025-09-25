import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BookService } from '../../../services/book.service';
import { BookModel, BookWithSales } from '../../../models/book.model';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-section-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './section-management.component.html',
  styleUrls: ['./section-management.component.css']
})
export class SectionManagementComponent implements OnInit {
  
  allBooks: BookModel[] = [];
  newlyLaunchedBooks: BookWithSales[] = [];
  bestSellers: BookWithSales[] = [];
  specialOffers: BookWithSales[] = [];
  
  categories: ('newly launched' | 'highly rated' | 'special offers')[] = ['newly launched', 'highly rated', 'special offers'];
  
  selectedBookId: string | number = '';
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
    
    // Load all books and section data
    forkJoin({
      allBooks: this.bookService.getAllBooks(),
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

  addBookToSection(): void {
    if (!this.selectedBookId || !this.selectedCategory) {
      this.showMessage('Please select a book and category', 'error');
      return;
    }

    this.isLoading = true;
    this.bookService.addBookToCategory(this.selectedBookId.toString(), this.selectedCategory).subscribe({
      next: () => {
        this.showMessage(`Book added to ${this.selectedCategory} successfully`, 'success');
        this.loadAllData();
        this.selectedBookId = '';
      },
      error: (error) => {
        console.error('Error adding book to section:', error);
        this.showMessage('Error adding book to section', 'error');
        this.isLoading = false;
      }
    });
  }

  removeBookFromSection(bookId: string | number, category: 'newly launched' | 'highly rated' | 'special offers'): void {
    this.isLoading = true;
    this.bookService.removeBookFromCategory(bookId.toString(), category).subscribe({
      next: () => {
        this.showMessage(`Book removed from ${category} successfully`, 'success');
        this.loadAllData();
      },
      error: (error) => {
        console.error('Error removing book from section:', error);
        this.showMessage('Error removing book from section', 'error');
        this.isLoading = false;
      }
    });
  }

  getBooksForCategory(category: 'newly launched' | 'highly rated' | 'special offers'): BookWithSales[] {
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

  private showMessage(message: string, type: 'success' | 'error'): void {
    this.message = message;
    this.messageType = type;
    setTimeout(() => {
      this.message = '';
    }, 3000);
  }
} 