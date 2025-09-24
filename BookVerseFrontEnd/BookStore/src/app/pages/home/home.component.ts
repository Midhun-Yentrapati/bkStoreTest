import { Component, OnInit } from '@angular/core';
import { BookService } from '../../services/book.service';
import { BookModel, BookWithSales } from '../../models/book.model';
import { NgFor, NgIf } from '@angular/common';
import { HorizontalBookSectionComponent } from "../../components/customer_site/horizontal-book-section/horizontal-book-section.component";
import { CategoryNavComponent } from "../../components/customer_site/category-nav/category-nav.component";
import { CategoryService } from '../../services/category.service';
import { CategoryModel } from '../../models/category.model';
import { VerticalBookGridComponent } from '../../components/customer_site/vertical-book-grid/vertical-book-grid.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [HorizontalBookSectionComponent, CategoryNavComponent, VerticalBookGridComponent, NgIf],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  books: BookModel[] = [];
  filtreredBooks: BookModel[] = [];

  newlyLaunchedBooks: BookModel[] = [];
  bestSellers: BookModel[] = [];
  specialOffers: BookModel[] = [];

  categories: CategoryModel[] = [];

  constructor(private bookService: BookService, private categoryService: CategoryService) { }

  ngOnInit(): void {

    //for 3 sections on home page  
    this.bookService.getNewlyLaunchedBooks().subscribe(books => this.newlyLaunchedBooks = books);
    this.bookService.getBestSellers().subscribe(books => this.bestSellers = books);
    this.bookService.getSpecialOffers().subscribe(books => this.specialOffers = books);

    //displays active categories in the category bar (sorted by display order)
    console.log('HomeComponent: Loading active categories...');
    this.categoryService.getActiveCategoriesForNavigation().subscribe({
      next: (data) => {
        console.log('HomeComponent: Received active categories:', data);
        this.categories = [
          { id: 'all', name: 'All', slug: 'all', isActive: true, displayOrder: 0 },
          ...data
        ];
        console.log('HomeComponent: Final categories array:', this.categories);
      },
      error: (error) => {
        console.error('HomeComponent: Failed to load active categories:', error);
        // Fallback to just "All" category if loading fails
        this.categories = [{ id: 'all', name: 'All', slug: 'all', isActive: true, displayOrder: 0 }];
      }
    });

    //displays all books
    this.bookService.getAllBooks().subscribe(books => {
      this.books = books;
      this.filtreredBooks = books;
    });
  }

  //for 4th section on home page
  onCategorySelected(category: string): void {
    console.log('HomeComponent: Category selected:', category);
    if (category === 'All') {
      this.filtreredBooks = this.books;
    } else {
      this.filtreredBooks = this.books.filter(book => {
        return book.categories.some(cat => cat.name === category);
      });
    }
    console.log('HomeComponent: Filtered books count:', this.filtreredBooks.length);
  }

  getLimitedBooks(): BookModel[] {
    const booksToShow = this.filtreredBooks.length > 0 ? this.filtreredBooks : this.books;
    return booksToShow.slice(0, 20); // (5 rows × 4 books)
  }
  
}
