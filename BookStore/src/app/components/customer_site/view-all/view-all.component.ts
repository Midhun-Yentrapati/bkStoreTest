import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { BookModel } from '../../../models/book.model';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { BookService } from '../../../services/book.service';
import { CategoryColorService } from '../../../services/category-color.service';

@Component({
  selector: 'app-view-all',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CommonModule
  ],
  templateUrl: './view-all.component.html',
  styleUrls: ['./view-all.component.css']
})
export class ViewAllComponent implements OnInit {
  books: BookModel[] = [];
  filteredBooks: BookModel[] = [];
  paginatedBooks: BookModel[] = [];
  searchForm!: FormGroup;
  displayedColumns: string[] = ['id', 'title', 'author', 'description', 'categories', 'price', 'stock_display', 'stock_actual', 'actions'];
  
  // Pagination properties
  currentPage: number = 1;
  itemsPerPage: number = 10;
  totalPages: number = 0;
  totalItems: number = 0;
  
  // Math for template
  Math = Math;

  // Loading state
  loading: boolean = true;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private bookService: BookService,
    private router: Router,
    public categoryColorService: CategoryColorService
  ) {}

  ngOnInit(): void {
    // Initialize form with advanced filtering options
    this.searchForm = this.fb.group({
      searchById: [''],
      searchByTitle: [''],
      searchByAuthor: [''],
      query: [''],  // Keeping the original query field for backward compatibility
      minPrice: [''],
      maxPrice: [''],
      sortBy: ['id'],  // Set ID as default sort field
      sortOrder: ['asc']
    });

    // Load books from the backend
    this.loading = true;
    this.bookService.getAllBooks().subscribe({
      next: (data: BookModel[]) => {
        this.books = data;
        this.filteredBooks = data;
        
        // Apply default sorting by ID when data is loaded
        if (this.searchForm.get('sortBy')?.value === 'id') {
          this.filteredBooks = this.sortBooks([...this.filteredBooks], 'id', this.searchForm.get('sortOrder')?.value || 'asc');
        }
        
        this.updatePagination();
        this.loading = false;
      },
      error: err => {
        console.error('Error fetching books:', err);
        this.error = 'Failed to load books. Please try again later.';
        this.loading = false;
      }
    });

    // Basic filter logic triggered on original query input change
    this.searchForm.get('query')?.valueChanges.subscribe(query => {
      if (query) {
        const lower = query.toLowerCase();
        this.filteredBooks = this.books.filter(book =>
          book.id.toString().includes(lower) ||
          book.title.toLowerCase().includes(lower) ||
          book.author.toLowerCase().includes(lower)
        );
        this.updatePagination();
      } else {
        // If query is cleared and no other filters are active, show all books
        this.applyFiltersAndSort();
      }
    });

    // Advanced filter logic
    this.searchForm.valueChanges.subscribe(() => {
      // Skip if the main query is being used
      if (!this.searchForm.get('query')?.value) {
        this.applyFiltersAndSort();
      }
    });
  }

  // Apply all filters and sorting
  applyFiltersAndSort(): void {
    const formValues = this.searchForm.value;
    
    // Start with all books
    let filtered = [...this.books];

    // Apply ID filter
    if (formValues.searchById && formValues.searchById.trim()) {
      const idQuery = formValues.searchById.toLowerCase().trim();
      filtered = filtered.filter(book => 
        book.id.toString().toLowerCase().includes(idQuery)
      );
    }

    // Apply title filter
    if (formValues.searchByTitle && formValues.searchByTitle.trim()) {
      const titleQuery = formValues.searchByTitle.toLowerCase().trim();
      filtered = filtered.filter(book => 
        book.title.toLowerCase().includes(titleQuery)
      );
    }

    // Apply author filter
    if (formValues.searchByAuthor && formValues.searchByAuthor.trim()) {
      const authorQuery = formValues.searchByAuthor.toLowerCase().trim();
      filtered = filtered.filter(book => 
        book.author.toLowerCase().includes(authorQuery)
      );
    }

    // Apply price range filter
    if (formValues.minPrice !== null && formValues.minPrice !== '') {
      const minPrice = parseFloat(formValues.minPrice);
      if (!isNaN(minPrice)) {
        filtered = filtered.filter(book => book.price >= minPrice);
      }
    }

    if (formValues.maxPrice !== null && formValues.maxPrice !== '') {
      const maxPrice = parseFloat(formValues.maxPrice);
      if (!isNaN(maxPrice)) {
        filtered = filtered.filter(book => book.price <= maxPrice);
      }
    }

    // Apply sorting
    if (formValues.sortBy && formValues.sortBy !== 'none') {
      filtered = this.sortBooks(filtered, formValues.sortBy, formValues.sortOrder);
    }

    this.filteredBooks = filtered;
    this.currentPage = 1; // Reset to first page when filtering
    this.updatePagination();
  }

  // Sort books based on criteria
  sortBooks(books: BookModel[], sortBy: string, sortOrder: string): BookModel[] {
    return books.sort((a, b) => {
      let comparison = 0;

      switch (sortBy) {
        case 'price':
          comparison = a.price - b.price;
          break;
        case 'stock_display':
          comparison = a.stock_display - b.stock_display;
          break;
        case 'stock_actual':
          comparison = a.stock_actual - b.stock_actual;
          break;
        case 'title':
          comparison = a.title.localeCompare(b.title);
          break;
        case 'author':
          comparison = a.author.localeCompare(b.author);
          break;
        case 'id':
          // Convert string IDs to numbers for proper numerical sorting
          const idA = parseInt(a.id.toString(), 10);
          const idB = parseInt(b.id.toString(), 10);
          comparison = idA - idB;
          break;
        default:
          return 0;
      }

      return sortOrder === 'desc' ? -comparison : comparison;
    });
  }

  // Clear all filters
  clearAllFilters(): void {
    this.searchForm.reset({
      searchById: '',
      searchByTitle: '',
      searchByAuthor: '',
      query: '',
      minPrice: '',
      maxPrice: '',
      sortBy: 'id',  // Maintain ID as default sort field
      sortOrder: 'asc'
    });
    this.filteredBooks = [...this.books];
    this.updatePagination();
  }

  // Get price range info
  getPriceRangeInfo(): { min: number, max: number } {
    if (this.books.length === 0) return { min: 0, max: 0 };
    
    const prices = this.books.map(book => book.price);
    return {
      min: Math.min(...prices),
      max: Math.max(...prices)
    };
  }

  // Update pagination calculations and paginated books
  updatePagination(): void {
    this.totalItems = this.filteredBooks.length;
    this.totalPages = Math.ceil(this.totalItems / this.itemsPerPage);
    
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    this.paginatedBooks = this.filteredBooks.slice(startIndex, endIndex);
  }

  // Go to specific page
  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.updatePagination();
    }
  }

  // Go to previous page
  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.updatePagination();
    }
  }

  // Go to next page
  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.updatePagination();
    }
  }

  // Get array of page numbers for pagination buttons
  getPageNumbers(): number[] {
    const pages: number[] = [];
    const maxVisiblePages = 5;
    
    if (this.totalPages <= maxVisiblePages) {
      for (let i = 1; i <= this.totalPages; i++) {
        pages.push(i);
      }
    } else {
      const half = Math.floor(maxVisiblePages / 2);
      let start = Math.max(1, this.currentPage - half);
      let end = Math.min(this.totalPages, start + maxVisiblePages - 1);
      
      if (end - start < maxVisiblePages - 1) {
        start = Math.max(1, end - maxVisiblePages + 1);
      }
      
      for (let i = start; i <= end; i++) {
        pages.push(i);
      }
    }
    
    return pages;
  }

  // Change items per page
  changeItemsPerPage(event: Event): void {
    const target = event.target as HTMLSelectElement;
    this.itemsPerPage = parseInt(target.value);
    this.currentPage = 1;
    this.updatePagination();
  }

  // Action button handlers
  goToEditPage(bookId: string | number): void {
    this.router.navigate(['/admin/edit-book', bookId.toString()]);
  }

  showAddPage(): void {
    this.router.navigate(['/admin/add-book']);
  }

  goBack(): void {
    this.router.navigate(['/admin-main']);
  }

  // TrackBy function for better performance
  trackByBookId(index: number, book: BookModel): string {
    return book.id.toString();
  }
  
  // Get category badge classes using the CategoryColorService
  getCategoryBadgeClasses(category: string | any): string {
    const categoryName = typeof category === 'string' ? category : category.name;
    return this.categoryColorService.getCategoryBadgeClasses(categoryName);
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
