import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { HomeComponent } from './home.component';
import { BookService } from '../../services/book.service';
import { CategoryService } from '../../services/category.service';
import { BookModel, BookWithSales } from '../../models/book.model';
import { CategoryModel } from '../../models/category.model';
import { of } from 'rxjs';

describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;
  let bookService: jasmine.SpyObj<BookService>;
  let categoryService: jasmine.SpyObj<CategoryService>;

  const mockBooks: BookModel[] = [
    { id: '1', title: 'Book 1', author: 'Author 1', description: 'Desc 1', categories: ['Fiction'], price: 10, stock_display: 5, stock_actual: 5, image_urls: ['img1.jpg'] },
    { id: '2', title: 'Book 2', author: 'Author 2', description: 'Desc 2', categories: ['Non-Fiction'], price: 15, stock_display: 3, stock_actual: 3, image_urls: ['img2.jpg'] },
    { id: '3', title: 'Book 3', author: 'Author 3', description: 'Desc 3', categories: ['Fiction', 'Adventure'], price: 20, stock_display: 8, stock_actual: 8, image_urls: ['img3.jpg'] }
  ];

  const mockNewlyLaunchedBooks: BookWithSales[] = [
    { id: '1', title: 'New Book 1', author: 'Author 1', description: 'Desc 1', categories: ['Fiction'], price: 10, stock_display: 5, stock_actual: 5, image_urls: ['img1.jpg'], no_of_books_sold: 50 }
  ];

  const mockBestSellers: BookWithSales[] = [
    { id: '2', title: 'Best Seller 1', author: 'Author 2', description: 'Desc 2', categories: ['Non-Fiction'], price: 15, stock_display: 3, stock_actual: 3, image_urls: ['img2.jpg'], no_of_books_sold: 200 }
  ];

  const mockSpecialOffers: BookWithSales[] = [
    { id: '3', title: 'Special Offer 1', author: 'Author 3', description: 'Desc 3', categories: ['Fiction'], price: 20, stock_display: 8, stock_actual: 8, image_urls: ['img3.jpg'], no_of_books_sold: 100 }
  ];

  const mockCategories: CategoryModel[] = [
    { id: 1, name: 'Fiction' },
    { id: 2, name: 'Non-Fiction' },
    { id: 3, name: 'Adventure' }
  ];

  beforeEach(async () => {
    const bookServiceSpy = jasmine.createSpyObj('BookService', [
      'getNewlyLaunchedBooks',
      'getBestSellers', 
      'getSpecialOffers',
      'getAllBooks'
    ]);
    const categoryServiceSpy = jasmine.createSpyObj('CategoryService', ['getAllCategories']);

    // Setup default return values
    bookServiceSpy.getNewlyLaunchedBooks.and.returnValue(of(mockNewlyLaunchedBooks));
    bookServiceSpy.getBestSellers.and.returnValue(of(mockBestSellers));
    bookServiceSpy.getSpecialOffers.and.returnValue(of(mockSpecialOffers));
    bookServiceSpy.getAllBooks.and.returnValue(of(mockBooks));
    categoryServiceSpy.getAllCategories.and.returnValue(of(mockCategories));

    await TestBed.configureTestingModule({
      imports: [HomeComponent, HttpClientTestingModule],
      providers: [
        { provide: BookService, useValue: bookServiceSpy },
        { provide: CategoryService, useValue: categoryServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;
    bookService = TestBed.inject(BookService) as jasmine.SpyObj<BookService>;
    categoryService = TestBed.inject(CategoryService) as jasmine.SpyObj<CategoryService>;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with empty arrays', () => {
    expect(component.books).toEqual([]);
    expect(component.filtreredBooks).toEqual([]);
    expect(component.newlyLaunchedBooks).toEqual([]);
    expect(component.bestSellers).toEqual([]);
    expect(component.specialOffers).toEqual([]);
    expect(component.categories).toEqual([]);
  });

  describe('ngOnInit', () => {
    it('should load newly launched books', () => {
      component.ngOnInit();
      expect(bookService.getNewlyLaunchedBooks).toHaveBeenCalled();
      expect(component.newlyLaunchedBooks).toEqual(mockNewlyLaunchedBooks);
    });

    it('should load best sellers', () => {
      component.ngOnInit();
      expect(bookService.getBestSellers).toHaveBeenCalled();
      expect(component.bestSellers).toEqual(mockBestSellers);
    });

    it('should load special offers', () => {
      component.ngOnInit();
      expect(bookService.getSpecialOffers).toHaveBeenCalled();
      expect(component.specialOffers).toEqual(mockSpecialOffers);
    });

    it('should load all books', () => {
      component.ngOnInit();
      expect(bookService.getAllBooks).toHaveBeenCalled();
      expect(component.books).toEqual(mockBooks);
      expect(component.filtreredBooks).toEqual(mockBooks);
    });

    it('should load categories and add "All" option', () => {
      component.ngOnInit();
      expect(categoryService.getAllCategories).toHaveBeenCalled();
      expect(component.categories).toEqual(['All', 'Fiction', 'Non-Fiction', 'Adventure']);
    });
  });

  describe('onCategorySelected', () => {
    beforeEach(() => {
      component.books = mockBooks;
      component.filtreredBooks = mockBooks;
    });

    it('should show all books when "All" category is selected', () => {
      component.onCategorySelected('All');
      expect(component.filtreredBooks).toEqual(mockBooks);
    });

    it('should filter books by selected category', () => {
      component.onCategorySelected('Fiction');
      expect(component.filtreredBooks).toEqual([mockBooks[0], mockBooks[2]]);
    });

    it('should filter books by case-insensitive category match', () => {
      component.onCategorySelected('fiction');
      expect(component.filtreredBooks).toEqual([mockBooks[0], mockBooks[2]]);
    });

    it('should filter books by exact category match', () => {
      component.onCategorySelected('Non-Fiction');
      expect(component.filtreredBooks).toEqual([mockBooks[1]]);
    });

    it('should handle category with multiple categories', () => {
      component.onCategorySelected('Adventure');
      expect(component.filtreredBooks).toEqual([mockBooks[2]]);
    });

    it('should return empty array for non-existent category', () => {
      component.onCategorySelected('Romance');
      expect(component.filtreredBooks).toEqual([]);
    });

    it('should handle empty category string', () => {
      component.onCategorySelected('');
      expect(component.filtreredBooks).toEqual([]);
    });
  });

  describe('getLimitedBooks', () => {
    beforeEach(() => {
      component.books = mockBooks;
      component.filtreredBooks = mockBooks;
    });

    it('should return limited books from filtered books when available', () => {
      const result = component.getLimitedBooks();
      expect(result).toEqual(mockBooks);
      expect(result.length).toBeLessThanOrEqual(20);
    });

    it('should return limited books from all books when filtered books is empty', () => {
      component.filtreredBooks = [];
      const result = component.getLimitedBooks();
      expect(result).toEqual(mockBooks);
      expect(result.length).toBeLessThanOrEqual(20);
    });

    it('should limit books to 20 when more than 20 books are available', () => {
      const manyBooks = Array.from({ length: 25 }, (_, i) => ({
        ...mockBooks[0],
        id: `book${i}`,
        title: `Book ${i}`
      }));
      component.filtreredBooks = manyBooks;
      
      const result = component.getLimitedBooks();
      expect(result.length).toBe(20);
      expect(result[0].id).toBe('book0');
      expect(result[19].id).toBe('book19');
    });

    it('should return all books when less than 20 books are available', () => {
      const fewBooks = mockBooks.slice(0, 2);
      component.filtreredBooks = fewBooks;
      
      const result = component.getLimitedBooks();
      expect(result).toEqual(fewBooks);
      expect(result.length).toBe(2);
    });
  });

  describe('Component properties', () => {
    it('should have correct property types', () => {
      expect(Array.isArray(component.books)).toBe(true);
      expect(Array.isArray(component.filtreredBooks)).toBe(true);
      expect(Array.isArray(component.newlyLaunchedBooks)).toBe(true);
      expect(Array.isArray(component.bestSellers)).toBe(true);
      expect(Array.isArray(component.specialOffers)).toBe(true);
      expect(Array.isArray(component.categories)).toBe(true);
    });
  });
});
