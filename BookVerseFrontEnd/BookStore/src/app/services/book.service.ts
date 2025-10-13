import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of, switchMap, map, catchError, forkJoin } from 'rxjs';
import { BookModel, BookCategoryData, BookWithSales } from '../models/book.model';

@Injectable({
  providedIn: 'root'
})
export class BookService {
  private apiBaseUrl = 'http://localhost:8090/api'; // API Gateway URL
  private booksUrl = `${this.apiBaseUrl}/books`;
  private booksByCategoryUrl = `${this.apiBaseUrl}/books/category`;
  private baseUrl = this.apiBaseUrl;

  constructor(private http: HttpClient) { }

  // =================================================================
  // == CORE BOOK CRUD OPERATIONS
  // =================================================================

  // Fetches all active books for the customer-facing site.
  getAllBooks(): Observable<BookModel[]> {
    return this.http.get<BookModel[]>(this.booksUrl).pipe(
      map(books => books.map(book => this.mapBackendBookToFrontend(book))),
      catchError(this.handleError<BookModel[]>('getAllBooks', []))
    );
  }

  // Fetches a single book by its ID for detail pages.
  getBookById(id: string | number): Observable<BookModel> {
    const stringId = this.toStringId(id);
    return this.http.get<any>(`${this.booksUrl}/${stringId}`).pipe(
      map(book => this.mapBackendBookToFrontend(book)),
      catchError(this.handleError<BookModel>(`getBookById id=${id}`))
    );
  }

  // Fetches a single book with all relations for editing.
  getBookWithRelations(id: string | number): Observable<BookModel> {
    const stringId = this.toStringId(id);
    return this.http.get<any>(`${this.booksUrl}/${stringId}`).pipe(
      map(book => this.mapBackendBookToFrontend(book)),
      catchError(this.handleError<BookModel>(`getBookWithRelations id=${id}`))
    );
  }

  // Creates a new book with basic information.
  createBook(book: Omit<BookModel, 'id'>): Observable<BookModel> {
    const backendBook = this.mapFrontendBookToBackend(book);
    return this.http.post<any>(this.booksUrl, backendBook).pipe(
      map(createdBook => this.mapBackendBookToFrontend(createdBook)),
      catchError(error => { throw error; })
    );
  }

  // Creates a new book including its relations (categories, images).
  createBookWithRelations(bookData: any): Observable<BookModel> {
    return this.http.post<any>(this.booksUrl, bookData).pipe(
      map(createdBook => this.mapBackendBookToFrontend(createdBook)),
      catchError(error => { throw error; })
    );
  }

  // Updates an entire book object (PUT operation).
  updateBook(id: string | number, book: Partial<BookModel>): Observable<BookModel> {
    const stringId = this.toStringId(id);
    const backendBook = this.mapFrontendBookToBackend(book);
    return this.http.put<any>(`${this.booksUrl}/${stringId}`, backendBook).pipe(
      map(updatedBook => this.mapBackendBookToFrontend(updatedBook)),
      catchError(error => { throw error; })
    );
  }

  // Partially updates a book's properties (PATCH operation).
  patchBook(id: string | number, bookUpdates: Partial<BookModel>): Observable<BookModel> {
    const stringId = this.toStringId(id);
    const backendUpdates = this.mapFrontendBookToBackend(bookUpdates);
    return this.http.patch<any>(`${this.booksUrl}/${stringId}`, backendUpdates).pipe(
      map(updatedBook => this.mapBackendBookToFrontend(updatedBook)),
      catchError(error => { throw error; })
    );
  }

  // Soft deletes a book by its ID.
  deleteBook(id: string | number): Observable<any> {
    return this.http.delete(`${this.booksUrl}/${id}`).pipe(
      catchError(this.handleError<any>('deleteBook'))
    );
  }

  // =================================================================
  // == SEARCH & DISCOVERY
  // =================================================================

  // Searches books by query and applies filters for category and price.
  searchBooks(query: string, filters?: any): Observable<{ results: BookModel[], total: number }> {
    let params = new HttpParams();

    if (query && query.trim() !== '') {
      params = params.set('query', query);
    }
    if (filters && filters.categories && filters.categories.length > 0) {
      filters.categories.forEach((category: string) => {
        params = params.append('categories', category);
      });
    }

    if (!params.has('query') && !params.has('categories')) {
      return this.getAllBooks().pipe(
        map(books => ({ results: books, total: books.length }))
      );
    }

    return this.http.get<any[]>(`${this.booksUrl}/search`, { params }).pipe(
      map(books => {
        const mappedBooks = books.map(book => this.mapBackendBookToFrontend(book));
        let filteredBooks = mappedBooks;
        if (filters && filters.maxPrice && filters.maxPrice > 0) {
          const maxPrice = Number(filters.maxPrice);
          if (!isNaN(maxPrice)) {
            filteredBooks = filteredBooks.filter(book => book.price <= maxPrice);
          }
        }
        return {
          results: filteredBooks,
          total: filteredBooks.length
        };
      }),
      catchError(this.handleError<{ results: BookModel[], total: number }>('searchBooks', { results: [], total: 0 }))
    );
  }

  // Fetches a list of books similar to the given book ID.
  getSimilarBooks(bookId: string | number): Observable<BookModel[]> {
    return this.http.get<any[]>(`${this.booksUrl}/${bookId}/similar`).pipe(
      map(books => books.map(book => this.mapBackendBookToFrontend(book))),
      catchError(this.handleError<BookModel[]>('getSimilarBooks', []))
    );
  }

  // =================================================================
  // == HOME PAGE SECTIONS & CATEGORIES
  // =================================================================

  // Fetches books for the "Newly Launched" section on the home page.
  getNewlyLaunchedBooks(): Observable<BookWithSales[]> {
    return this.http.get<any[]>(`${this.booksUrl}/sales-category/NEWLY_LAUNCHED`).pipe(
      map(books => books.map(book => ({...this.mapBackendBookToFrontend(book), no_of_books_sold: book.noOfBooksSold || 0 }))),
      catchError(this.handleError<BookWithSales[]>('getNewlyLaunchedBooks', []))
    );
  }

  // Fetches books for the "Best Sellers" section on the home page.
  getBestSellers(): Observable<BookWithSales[]> {
    return this.http.get<any[]>(`${this.booksUrl}/sales-category/BEST_SELLING`).pipe(
      map(books => books.map(book => ({...this.mapBackendBookToFrontend(book), no_of_books_sold: book.noOfBooksSold || 0 }))),
      catchError(this.handleError<BookWithSales[]>('getBestSellers', []))
    );
  }

  // Fetches books for the "Special Offers" section on the home page.
  getSpecialOffers(): Observable<BookWithSales[]> {
    return this.http.get<any[]>(`${this.booksUrl}/sales-category/SPECIAL_OFFERS`).pipe(
      map(books => books.map(book => ({...this.mapBackendBookToFrontend(book), no_of_books_sold: book.noOfBooksSold || 0 }))),
      catchError(this.handleError<BookWithSales[]>('getSpecialOffers', []))
    );
  }

  // [Legacy] Fetches book IDs for a given home page section.
  getSectionData(section: 'newlyLaunchedBooks' | 'bestSellers' | 'specialOffers'): Observable<{id: string}[]> {
    return this.http.get<{id: string}[]>(`${this.baseUrl}/${section}`).pipe(
      catchError(this.handleError<{id: string}[]>('getSectionData', []))
    );
  }

  // [Legacy] Fetches full book details for a home page section using client-side filtering.
  getSectionBooks(section: 'newlyLaunchedBooks' | 'bestSellers' | 'specialOffers'): Observable<BookModel[]> {
    return this.getSectionData(section).pipe(
      switchMap(sectionData => {
        const bookIds = sectionData.map(item => item.id);
        if (bookIds.length === 0) {
          return of([]);
        }
        return this.getAllBooks().pipe(
          map(allBooks => allBooks.filter(book => bookIds.includes(book.id.toString())))
        );
      }),
      catchError(this.handleError<BookModel[]>('getSectionBooks', []))
    );
  }

  // [Legacy] Generic method to get books by section name, mapping to a sales category.
  getBookByIds(section: 'newlyLaunchedBooks' | 'bestSellers' | 'specialOffers'): Observable<BookModel[]> {
    const salesCategoryMap = { 'newlyLaunchedBooks': 'NEWLY_LAUNCHED', 'bestSellers': 'BEST_SELLING', 'specialOffers': 'SPECIAL_OFFERS' };
    const salesCategory = salesCategoryMap[section];
    return this.http.get<any[]>(`${this.booksUrl}/sales-category/${salesCategory}`).pipe(
      map(books => books.map(book => this.mapBackendBookToFrontend(book))),
      catchError(this.handleError<BookModel[]>('getBookByIds', []))
    );
  }

  // =================================================================
  // == SALES, STOCK & ANALYTICS
  // =================================================================

  // Fetches a list of top-selling books for the admin dashboard.
  getHighlySoldBooks(limit: number = 10): Observable<BookWithSales[]> {
    return this.http.get<any[]>(`${this.booksUrl}/highly-sold?limit=${limit}`).pipe(
      map(books => books.map(book => ({ ...this.mapBackendBookToFrontend(book), no_of_books_sold: book.noOfBooksSold || 0 }))),
      catchError(this.handleError<BookWithSales[]>('getHighlySoldBooks', []))
    );
  }

  // Fetches a list of least-selling books (with sales > 0) for the admin dashboard.
  getLeastSoldBooks(limit: number = 10): Observable<BookWithSales[]> {
    return this.http.get<any[]>(`${this.booksUrl}/least-sold?limit=${limit}`).pipe(
      map(books => books
        .map(book => ({ ...this.mapBackendBookToFrontend(book), no_of_books_sold: book.noOfBooksSold || 0 }))
        .filter(book => book.no_of_books_sold > 0)
      ),
      catchError(this.handleError<BookWithSales[]>('getLeastSoldBooks', []))
    );
  }

  // Updates the stock quantity for a book after an order is placed.
  updateBookStock(bookId: string | number, quantity: number): Observable<any> {
    return this.http.put(`${this.booksUrl}/${bookId}/stock?quantity=${quantity}`, {}).pipe(
      catchError(this.handleError<any>('updateBookStock'))
    );
  }

  // Increments the sales count for a book within a specific category.
  updateBookSalesCount(bookId: string, category: 'newly launched' | 'highly rated' | 'special offers'): Observable<BookCategoryData | null> {
    return this.getBooksByCategory().pipe(
      switchMap(categoryData => {
        const itemToUpdate = categoryData.find(item => item.id === bookId && item.category === category);
        if (itemToUpdate) {
          const updatedData = { ...itemToUpdate, no_of_books_sold: itemToUpdate.no_of_books_sold + 1 };
          return this.http.put<BookCategoryData>(`${this.booksByCategoryUrl}/${itemToUpdate.id}`, updatedData);
        }
        return of(null);
      }),
      catchError(this.handleError<BookCategoryData | null>('updateBookSalesCount', null))
    );
  }

  // =================================================================
  // == ADMIN-SPECIFIC OPERATIONS
  // =================================================================

  // Fetches all books for the admin panel, including inactive ones.
  getAllBooksForAdmin(): Observable<BookModel[]> {
    return this.http.get<any[]>(`${this.booksUrl}/admin/all`).pipe(
      map(books => books.map(book => this.mapBackendBookToFrontend(book))),
      catchError(this.handleError<BookModel[]>('getAllBooksForAdmin', []))
    );
  }

  // Restores a previously soft-deleted book.
  restoreBook(id: string | number): Observable<any> {
    return this.http.put(`${this.booksUrl}/${id}/restore`, {}).pipe(
      catchError(this.handleError<any>('restoreBook'))
    );
  }
  
  // Fetches books for a specific sales category for the admin dashboard.
  getBooksByCategoryForAdmin(category: 'newly launched' | 'highly rated' | 'special offers'): Observable<BookWithSales[]> {
    const salesCategory = category === 'newly launched' ? 'NEWLY_LAUNCHED' : category === 'highly rated' ? 'BEST_SELLING' : 'SPECIAL_OFFERS';
    return this.http.get<any[]>(`${this.booksUrl}/sales-category/${salesCategory}`).pipe(
      map(books => books.map(book => ({ ...this.mapBackendBookToFrontend(book), no_of_books_sold: book.noOfBooksSold || 0 }))),
      catchError(this.handleError<BookWithSales[]>('getBooksByCategoryForAdmin', []))
    );
  }

  // Fetches all books combined with their assigned categories and total sales counts.
  getAllBooksWithCategories(): Observable<{ book: BookModel; categories: string[]; salesCount: number }[]> {
    return forkJoin({ books: this.getAllBooks(), categoryData: this.getBooksByCategory() }).pipe(
      map(({ books, categoryData }) => {
        return books.map(book => {
          const bookCategories = categoryData.filter(item => item.id === book.id.toString());
          const totalSales = bookCategories.reduce((sum, item) => sum + item.no_of_books_sold, 0);
          return { book, categories: bookCategories.map(item => item.category), salesCount: totalSales };
        });
      }),
      catchError(this.handleError<{ book: BookModel; categories: string[]; salesCount: number }[]>('getAllBooksWithCategories', []))
    );
  }

  // Updates the categories associated with a specific book.
  updateBookCategories(id: string | number, categoryIds: number[]): Observable<BookModel> {
    const stringId = this.toStringId(id);
    return this.http.put<any>(`${this.booksUrl}/${stringId}/categories`, categoryIds).pipe(
      map(updatedBook => this.mapBackendBookToFrontend(updatedBook)),
      catchError(error => { throw error; })
    );
  }

  // Updates the images associated with a specific book.
  updateBookImages(id: string | number, images: any[]): Observable<BookModel> {
    const stringId = this.toStringId(id);
    const backendImages = images.map((img, index) => ({
      imageUrl: typeof img === 'object' ? img.imageUrl : img,
      isPrimary: typeof img === 'object' ? img.isPrimary : (index === 0),
      altText: typeof img === 'object' ? img.altText : 'Book image',
      displayOrder: index
    }));
    return this.http.put<any>(`${this.booksUrl}/${stringId}/images`, backendImages).pipe(
      map(updatedBook => this.mapBackendBookToFrontend(updatedBook)),
      catchError(error => { throw error; })
    );
  }

  // Fetches all book-to-category relationship data.
  getBooksByCategory(): Observable<BookCategoryData[]> {
    return this.http.get<BookCategoryData[]>(this.booksByCategoryUrl).pipe(
      catchError(this.handleError<BookCategoryData[]>('getBooksByCategory', []))
    );
  }

  // Fetches books of a specific type by filtering all category data on the client-side.
  getBooksByCategoryType(category: 'newly launched' | 'highly rated' | 'special offers'): Observable<BookWithSales[]> {
    return this.getBooksByCategory().pipe(
      switchMap(categoryData => {
        const bookIds = categoryData.filter(item => item.category === category).map(item => item.id);
        if (bookIds.length === 0) return of([]);
        return this.getAllBooks().pipe(
          map(allBooks => {
            return allBooks
              .filter(book => bookIds.includes(book.id.toString()))
              .map(book => {
                const categoryItem = categoryData.find(item => item.id === book.id.toString());
                return { ...book, no_of_books_sold: categoryItem ? categoryItem.no_of_books_sold : 0 };
              });
          })
        );
      }),
      catchError(this.handleError<BookWithSales[]>('getBooksByCategoryType', []))
    );
  }
  
  // Assigns a book to a specific category.
  addBookToCategory(bookId: string, category: 'newly launched' | 'highly rated' | 'special offers'): Observable<BookCategoryData> {
    const newCategoryData: BookCategoryData = { id: bookId, category: category, no_of_books_sold: 0 };
    return this.http.post<BookCategoryData>(this.booksByCategoryUrl, newCategoryData).pipe(
      catchError(this.handleError<BookCategoryData>('addBookToCategory'))
    );
  }

  // Removes a book from a specific category.
  removeBookFromCategory(bookId: string, category: 'newly launched' | 'highly rated' | 'special offers'): Observable<any> {
    return this.getBooksByCategory().pipe(
      switchMap(categoryData => {
        const itemToRemove = categoryData.find(item => item.id === bookId && item.category === category);
        if (itemToRemove) {
          return this.http.delete(`${this.booksByCategoryUrl}/${itemToRemove.id}`);
        }
        return of(null);
      }),
      catchError(this.handleError<any>('removeBookFromCategory'))
    );
  }
  
  // =================================================================
  // == PRIVATE HELPER METHODS
  // =================================================================

  // Centralized error handler to re-throw errors for component-level handling.
  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      throw error;
    };
  }

  // Utility function to ensure book ID is a string for API calls.
  private toStringId(id: string | number): string {
    return typeof id === 'number' ? id.toString() : id;
  }

  // Maps backend sales category strings to a stricter frontend type.
  private mapSalesCategory(backendCategory: string): 'BEST_SELLING' | 'NEWLY_LAUNCHED' | 'FEATURED' | 'SPECIAL_OFFERS' | undefined {
    switch(backendCategory) {
      case 'NEWLY_LAUNCHED': return 'NEWLY_LAUNCHED';
      case 'SPECIAL_OFFERS': return 'SPECIAL_OFFERS';
      case 'BEST_SELLING': return 'BEST_SELLING';
      default: return 'FEATURED';
    }
  }

  // Maps the raw backend book object to the frontend `BookModel`.
  private mapBackendBookToFrontend(backendBook: any): BookModel {
    const mappedImageUrls = backendBook.images?.map((img: any) => img.imageUrl) || [];
    const mappedBook = {
      id: backendBook.id,
      isbn: backendBook.isbn,
      title: backendBook.title,
      author: backendBook.author,
      description: backendBook.description,
      language: backendBook.language,
      format: backendBook.format,
      edition: backendBook.edition,
      publisher: backendBook.publisher,
      publicationDate: backendBook.publicationDate,
      pages: backendBook.pages,
      weight: backendBook.weight,
      dimensions: backendBook.dimensions,
      price: backendBook.price,
      mrp: backendBook.mrp,
      stockDisplay: backendBook.stockDisplay,
      stockActual: backendBook.stockActual,
      noOfBooksSold: backendBook.noOfBooksSold,
      totalRevenue: backendBook.totalRevenue,
      averageRating: backendBook.averageRating,
      reviewCount: backendBook.reviewCount,
      salesCategory: this.mapSalesCategory(backendBook.salesCategory),
      isActive: backendBook.isActive,
      isFeatured: backendBook.isFeatured,
      lastSoldAt: backendBook.lastSoldAt,
      createdAt: backendBook.createdAt,
      updatedAt: backendBook.updatedAt,
      categories: backendBook.categories,
      images: backendBook.images,
      stock_display: backendBook.stockDisplay, // Legacy support
      stock_actual: backendBook.stockActual,   // Legacy support
      image_urls: mappedImageUrls,             // Legacy support
      customerRatings: [] // Handled by ReviewService
    };
    return mappedBook;
  }

  // Maps the frontend book model to the format expected by the backend.
  private mapFrontendBookToBackend(frontendBook: Partial<BookModel>): any {
    const backendData: any = {
      isbn: frontendBook.isbn,
      title: frontendBook.title,
      author: frontendBook.author,
      description: frontendBook.description,
      language: frontendBook.language,
      format: frontendBook.format,
      edition: frontendBook.edition,
      publisher: frontendBook.publisher,
      publicationDate: this.formatDateForBackend(frontendBook.publicationDate),
      pages: frontendBook.pages,
      weight: frontendBook.weight,
      dimensions: frontendBook.dimensions,
      price: frontendBook.price,
      mrp: frontendBook.mrp,
      stockDisplay: frontendBook.stockDisplay || frontendBook.stock_display,
      stockActual: frontendBook.stockActual || frontendBook.stock_actual,
      salesCategory: frontendBook.salesCategory,
      isActive: frontendBook.isActive,
      isFeatured: frontendBook.isFeatured
    };
    return backendData;
  }
  
  // Converts various date formats into a backend-compatible ISO string.
  private formatDateForBackend(date: any): string | null {
    if (!date) return null;
    if (typeof date === 'string') return date;
    if (Array.isArray(date) && date.length >= 3) {
      const [year, month, day, hour = 0, minute = 0] = date;
      const dateObj = new Date(year, month - 1, day, hour, minute);
      return dateObj.toISOString();
    }
    if (date instanceof Date) {
      return date.toISOString();
    }
    return null;
  }
}