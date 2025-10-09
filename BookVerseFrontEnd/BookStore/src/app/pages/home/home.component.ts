import { Component } from '@angular/core';
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
export class HomeComponent {

  books: BookModel[] = [];
  filtreredBooks: BookModel[] = [];
  limitedBooks: BookModel[] = [];

  newlyLaunchedBooks: BookWithSales[] = [];
  bestSellers: BookWithSales[] = [];
  specialOffers: BookWithSales[] = [];

  categories: String[] = [];

  constructor(private bookService: BookService, private categoryService: CategoryService) { }

  ngOnInit(): void {

    //for 3 sections on home page - now using books_by_category
    this.bookService.getNewlyLaunchedBooks().subscribe(books => this.newlyLaunchedBooks = books);
    this.bookService.getBestSellers().subscribe(books => this.bestSellers = books);
    this.bookService.getSpecialOffers().subscribe(books => this.specialOffers = books);

    //displays categories in the category bar
    this.categoryService.getAllCategories().subscribe(data => {
      const activeCategories = data.filter(cat => cat.isActive).slice(0, 9);
      this.categories = ['All', ...activeCategories.map(cat => cat.name)];
    });

    //displays all books
    this.bookService.getAllBooks().subscribe(books => {
      this.books = books;
      this.filtreredBooks = books;
      this.updateLimitedBooks();
    });
  }

  //for 4th section on home page
  onCategorySelected(category: string): void {
    if (category === 'All') {
      this.filtreredBooks = this.books;
    } else {
      this.filtreredBooks = this.books.filter(book => {
        if (!book.categories) return false;
        return book.categories.some(cat => {
          const categoryName = typeof cat === 'string' ? cat : cat.name;
          return categoryName.trim().toLowerCase() === category.trim().toLowerCase();
        });
      });
    }
    this.updateLimitedBooks();
  }

  updateLimitedBooks(): void {
    const booksToShow = this.filtreredBooks.length > 0 ? this.filtreredBooks : this.books;
    this.limitedBooks = booksToShow.slice(0, 20);
  }
  
}
