import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { BookService } from '../../../services/book.service';
import { CategoryService } from '../../../services/category.service';
import { BookModel } from '../../../models/book.model';
import { CategoryModel } from '../../../models/category.model';
import { VerticalBookGridComponent } from '../vertical-book-grid/vertical-book-grid.component';
import { FilterSidebarComponent } from '../filter-sidebar/filter-sidebar.component';
import { switchMap, tap, map } from 'rxjs/operators';
import { forkJoin, of } from 'rxjs';

@Component({
  selector: 'app-search-results',
  standalone: true,
  // Make sure all necessary components are imported
  imports: [CommonModule, VerticalBookGridComponent, FilterSidebarComponent],
  templateUrl: './search-results.component.html',
  styleUrls: ['./search-results.component.css']
})
export class SearchResultsComponent implements OnInit {
  searchQuery = '';
  searchResults: BookModel[] = [];
  similarBooks: BookModel[] = [];
  totalResults = 0;
  currentFilters: any = {};
  isLoading = true;
  categories: CategoryModel[] = [];

  constructor(
    private route: ActivatedRoute,
    private bookService: BookService,
    private categoryService: CategoryService
  ) {}

  ngOnInit(): void {
    // Load categories first
    this.categoryService.getAllCategories().subscribe(cats => {
      this.categories = cats;
    });

    // This stream will re-run whenever the URL parameter changes
    this.route.paramMap.pipe(
      tap(params => {
        this.isLoading = true;
        this.searchQuery = params.get('query') || '';
        this.searchResults = []; // Clear previous results
        this.similarBooks = [];
        this.totalResults = 0;
      }),
      switchMap(() => this.fetchResults())
    ).subscribe();
  }

  // This method is called by the filter sidebar's (filtersChanged) event
  onFiltersChanged(filters: any) {
    this.isLoading = true;
    
    // Convert category IDs to category names for proper filtering
    if (filters.categories && filters.categories.length > 0) {
      const categoryNames = filters.categories.map((catId: number) => {
        // Find category by numeric ID
        const category = this.categories.find(cat => cat.id === catId);
        return category ? category.name : catId.toString();
      }).filter(Boolean); // Remove any undefined values
      
      this.currentFilters = {
        ...filters,
        categories: categoryNames
      };
    } else {
      this.currentFilters = filters;
    }
    
    this.fetchResults().subscribe();
  }

  private fetchResults() {
    return this.bookService.searchBooks(this.searchQuery, this.currentFilters).pipe(
      tap(response => {
        this.searchResults = response.results;
        this.totalResults = response.total;

        // Fetch similar books only if there are primary results
        if (this.searchResults.length > 0) {
          this.fetchSimilarBooks(this.searchResults[0].id.toString());
        } else {
          this.similarBooks = [];
        }
        this.isLoading = false;
      })
    );
  }

  private fetchSimilarBooks(bookId: string) {
    this.bookService.getSimilarBooks(bookId).subscribe(books => {
      this.similarBooks = books;
    });
  }
}
