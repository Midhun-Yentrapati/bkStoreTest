import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { BookService } from '../../../services/book.service';
import { BookModel } from '../../../models/book.model';

@Component({
  selector: 'app-low-stock',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './low-stock.component.html',
  styleUrls: ['./low-stock.component.css']
})
export class LowStockComponent implements OnInit {
  lowStockBooks: BookModel[] = [];
  isLoading: boolean = true;
  error: string | null = null;
  threshold: number = 20;
  
  // Filtering
  searchQuery: string = '';
  categoryFilter: string = 'all';

  constructor(private bookService: BookService) {}

  ngOnInit(): void {
    this.loadLowStockBooks();
  }

  loadLowStockBooks(): void {
    this.isLoading = true;
    this.error = null;

    this.bookService.getAllBooks().subscribe({
      next: (books) => {
        this.lowStockBooks = books.filter(book => book.stock_actual < this.threshold);
        this.isLoading = false;
        console.log('Low stock books loaded:', this.lowStockBooks);
      },
      error: (error) => {
        console.error('Error loading low stock books:', error);
        this.error = 'Failed to load low stock books. Please try again.';
        this.isLoading = false;
      }
    });
  }

  getFilteredBooks(): BookModel[] {
    let filtered = this.lowStockBooks;

    // Search filter
    if (this.searchQuery.trim()) {
      const query = this.searchQuery.toLowerCase();
      filtered = filtered.filter(book => 
        book.title.toLowerCase().includes(query) ||
        book.author.toLowerCase().includes(query) ||
        book.id.toString().toLowerCase().includes(query)
      );
    }

    // Category filter
    if (this.categoryFilter !== 'all') {
      filtered = filtered.filter(book => 
        book.categories?.some(cat => 
          typeof cat === 'string' ? cat === this.categoryFilter : cat.name === this.categoryFilter
        )
      );
    }

    return filtered;
  }

  getStockStatusClass(stock: number): string {
    if (stock === 0) return 'text-red-600 bg-red-100';
    if (stock <= 5) return 'text-orange-600 bg-orange-100';
    if (stock <= 10) return 'text-yellow-600 bg-yellow-100';
    return 'text-blue-600 bg-blue-100';
  }

  getStockStatusText(stock: number): string {
    if (stock === 0) return 'Out of Stock';
    if (stock <= 5) return 'Critical';
    if (stock <= 10) return 'Low';
    return 'Warning';
  }

  getCategories(): string[] {
    const categories = new Set<string>();
    this.lowStockBooks.forEach(book => {
      book.categories?.forEach(category => {
        const categoryName = typeof category === 'string' ? category : category.name;
        categories.add(categoryName);
      });
    });
    return Array.from(categories).sort();
  }

  getTotalValue(): number {
    return this.lowStockBooks.reduce((total, book) => total + (book.price * book.stock_actual), 0);
  }

  getTotalBooks(): number {
    return this.lowStockBooks.reduce((total, book) => total + book.stock_actual, 0);
  }

  navigateToInventory(): void {
    // This would navigate to the inventory page
    console.log('Navigate to inventory for restocking');
  }

  updateThreshold(newThreshold: number): void {
    this.threshold = newThreshold;
    this.loadLowStockBooks();
  }
  
  // Get category display name - handles both string and object categories
  getCategoryDisplayName(category: string | any): string {
    if (typeof category === 'string') {
      return category;
    }
    // If it's an object, try to get the name property
    return category?.name || category?.categoryName || 'Unknown Category';
  }
} 