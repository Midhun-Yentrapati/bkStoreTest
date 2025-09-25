import { Injectable } from '@angular/core';
import { BookModel, BookCategoryData, BookWithSales, CustomerRating } from '../models/book.model';
import { Observable, of, switchMap, map, catchError, forkJoin } from 'rxjs';
import { HttpClient, HttpParams } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class BookService {
  private apiBaseUrl = 'http://localhost:8090/api'; // API Gateway URL
  private booksUrl = `${this.apiBaseUrl}/books`;
  private booksByCategoryUrl = `${this.apiBaseUrl}/books/category`;
  private baseUrl = this.apiBaseUrl;

  constructor(private http: HttpClient) { }

  // Utility function to ensure ID is string for API calls
  private toStringId(id: string | number): string {
    return typeof id === 'number' ? id.toString() : id;
  }


  // returns all books - getAllBooks
  getAllBooks(): Observable<BookModel[]> {
    return this.http.get<BookModel[]>(this.booksUrl).pipe(
      map(books => books.map(book => this.mapBackendBookToFrontend(book))),
      catchError(this.handleError<BookModel[]>('getAllBooks', []))
    );
  }

  // to fetch book details - getBookDetails ; used in book-detail
  getBookById(id: string | number): Observable<BookModel> {
    const stringId = this.toStringId(id);
    return this.http.get<any>(`${this.booksUrl}/${stringId}`).pipe(
      map(book => this.mapBackendBookToFrontend(book)),
      catchError(this.handleError<BookModel>(`getBookById id=${id}`))
    );
  }

  // Get book with full relations (categories and images) - used in edit-page
  getBookWithRelations(id: string | number): Observable<BookModel> {
    const stringId = this.toStringId(id);
    return this.http.get<any>(`${this.booksUrl}/${stringId}`).pipe(
      map(book => this.mapBackendBookToFrontend(book)),
      catchError(this.handleError<BookModel>(`getBookWithRelations id=${id}`))
    );
  }

  // Create new book
  createBook(book: Omit<BookModel, 'id'>): Observable<BookModel> {
    const backendBook = this.mapFrontendBookToBackend(book);
    return this.http.post<any>(this.booksUrl, backendBook).pipe(
      map(createdBook => this.mapBackendBookToFrontend(createdBook)),
      catchError(this.handleError<BookModel>('createBook'))
    );
  }

  // Create new book with relations (categories and images)
  createBookWithRelations(bookData: any): Observable<BookModel> {
    return this.http.post<any>(this.booksUrl, bookData).pipe(
      map(createdBook => this.mapBackendBookToFrontend(createdBook)),
      catchError(this.handleError<BookModel>('createBookWithRelations'))
    );
  }

  // Update existing book (full replacement)
  updateBook(id: string | number, book: Partial<BookModel>): Observable<BookModel> {
    const stringId = this.toStringId(id);
    const backendBook = this.mapFrontendBookToBackend(book);
    
    // 🔍 DEBUG: Log service-level request details
    console.group('🔧 BOOK SERVICE: updateBook() Method Called');
    console.log('📍 Service Method: updateBook');
    console.log('🆔 Book ID (original):', id);
    console.log('🆔 Book ID (string):', stringId);
    console.log('📤 Frontend Book Data:', book);
    console.log('🔄 Mapped Backend Data:', backendBook);
    console.log('📋 JSON Payload:', JSON.stringify(backendBook, null, 2));
    console.log('🔍 JSON Validation Check:');
    console.log('  - categoryIds type:', typeof backendBook.categoryIds, backendBook.categoryIds);
    console.log('  - images type:', typeof backendBook.images, backendBook.images);
    console.log('  - publicationDate type:', typeof backendBook.publicationDate, backendBook.publicationDate);
    console.log('  - salesCategory type:', typeof backendBook.salesCategory, backendBook.salesCategory);
    
    // Test JSON stringification
    try {
      const testJson = JSON.stringify(backendBook);
      console.log('✅ JSON stringification successful, length:', testJson.length);
    } catch (error) {
      console.error('❌ JSON stringification failed:', error);
    }
    console.log('🌐 Request URL:', `${this.booksUrl}/${stringId}`);
    console.log('📡 HTTP Method: PUT');
    console.log('⏰ Request Time:', new Date().toISOString());
    console.groupEnd();
    
    return this.http.put<any>(`${this.booksUrl}/${stringId}`, backendBook).pipe(
      map(updatedBook => {
        // 🔍 DEBUG: Log raw backend response
        console.group('📥 BOOK SERVICE: Backend Response Received');
        console.log('✅ HTTP Status: SUCCESS');
        console.log('📦 Raw Backend Response:', updatedBook);
        console.log('🔄 Response Type:', typeof updatedBook);
        console.log('📊 Response Keys:', Object.keys(updatedBook || {}));
        console.log('⏰ Response Time:', new Date().toISOString());
        console.groupEnd();
        
        const mappedBook = this.mapBackendBookToFrontend(updatedBook);
        
        // 🔍 DEBUG: Log mapped frontend response
        console.group('🔄 BOOK SERVICE: Response Mapping Complete');
        console.log('📤 Mapped Frontend Response:', mappedBook);
        console.log('🔄 Mapping Function: mapBackendBookToFrontend');
        console.log('✅ Service Response Ready for Component');
        console.groupEnd();
        
        return mappedBook;
      }),
      catchError(error => {
        // 🔍 DEBUG: Log service-level errors
        console.group('❌ BOOK SERVICE: Request Failed');
        console.error('💥 HTTP Status: ERROR');
        console.error('🚨 Service Error:', error);
        console.error('📄 Error Message:', error.message);
        console.error('🔢 HTTP Status Code:', error.status);
        console.error('📊 Error Body:', error.error);
        console.error('🌐 Failed URL:', error.url);
        console.error('⏰ Error Time:', new Date().toISOString());
        console.groupEnd();
        
        // Re-throw the error to ensure it reaches the component
        throw error;
      })
    );
  }

  // Patch existing book (partial update)
  patchBook(id: string | number, bookUpdates: Partial<BookModel>): Observable<BookModel> {
    const stringId = this.toStringId(id);
    const backendUpdates = this.mapFrontendBookToBackend(bookUpdates);
    
    // 🔍 DEBUG: Log service-level request details
    console.group('🔧 BOOK SERVICE: patchBook() Method Called');
    console.log('📍 Service Method: patchBook');
    console.log('🆔 Book ID (original):', id);
    console.log('🆔 Book ID (string):', stringId);
    console.log('📤 Frontend Book Data:', bookUpdates);
    console.log('🔄 Mapped Backend Data:', backendUpdates);
    console.log('🌐 Request URL:', `${this.booksUrl}/${stringId}`);
    console.log('📡 HTTP Method: PATCH');
    console.log('⏰ Request Time:', new Date().toISOString());
    console.groupEnd();
    
    return this.http.patch<any>(`${this.booksUrl}/${stringId}`, backendUpdates).pipe(
      map(updatedBook => {
        // 🔍 DEBUG: Log raw backend response
        console.group('📥 BOOK SERVICE: Backend Response Received');
        console.log('✅ HTTP Status: SUCCESS');
        console.log('📦 Raw Backend Response:', updatedBook);
        console.log('🔄 Response Type:', typeof updatedBook);
        console.log('📊 Response Keys:', Object.keys(updatedBook || {}));
        console.log('⏰ Response Time:', new Date().toISOString());
        console.groupEnd();
        
        const mappedBook = this.mapBackendBookToFrontend(updatedBook);
        
        // 🔍 DEBUG: Log mapped frontend response
        console.group('🔄 BOOK SERVICE: Response Mapping Complete');
        console.log('📤 Mapped Frontend Response:', mappedBook);
        console.log('🔄 Mapping Function: mapBackendBookToFrontend');
        console.log('✅ Service Response Ready for Component');
        console.groupEnd();
        
        return mappedBook;
      }),
      catchError(error => {
        // 🔍 DEBUG: Log service-level errors
        console.group('❌ BOOK SERVICE: Request Failed');
        console.error('💥 HTTP Status: ERROR');
        console.error('🚨 Service Error:', error);
        console.error('📄 Error Message:', error.message);
        console.error('🔢 HTTP Status Code:', error.status);
        console.error('📊 Error Body:', error.error);
        console.error('🌐 Failed URL:', error.url);
        console.error('⏰ Error Time:', new Date().toISOString());
        console.groupEnd();
        
        // Re-throw the error to ensure it reaches the component
        throw error;
      })
    );
  }

  // Delete book (soft delete)
  deleteBook(id: string | number): Observable<any> {
    return this.http.delete(`${this.booksUrl}/${id}`).pipe(
      catchError(this.handleError<any>('deleteBook'))
    );
  }

  // Update book stock (decrease when order is placed)
  updateBookStock(bookId: string | number, quantity: number): Observable<any> {
    return this.http.put(`${this.booksUrl}/${bookId}/stock?quantity=${quantity}`, {}).pipe(
      catchError(this.handleError<any>('updateBookStock'))
    );
  }

  // Section Management Methods
  
  // Get section data (newlyLaunchedBooks, bestSellers, specialOffers)
  getSectionData(section: 'newlyLaunchedBooks' | 'bestSellers' | 'specialOffers'): Observable<{id: string}[]> {
    return this.http.get<{id: string}[]>(`${this.baseUrl}/${section}`).pipe(
      catchError(this.handleError<{id: string}[]>('getSectionData', []))
    );
  }

  // Update section data
  updateSectionData(section: 'newlyLaunchedBooks' | 'bestSellers' | 'specialOffers', bookIds: string[]): Observable<{id: string}[]> {
    const sectionData = bookIds.map(id => ({ id }));
    return this.http.put<{id: string}[]>(`${this.baseUrl}/${section}`, sectionData).pipe(
      catchError(this.handleError<{id: string}[]>('updateSectionData', []))
    );
  }

  // Get books for a specific section with full book details
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

  // Add book to section
  addBookToSection(section: 'newlyLaunchedBooks' | 'bestSellers' | 'specialOffers', bookId: string): Observable<{id: string}[]> {
    return this.getSectionData(section).pipe(
      switchMap(currentData => {
        const bookIds = currentData.map(item => item.id);
        if (!bookIds.includes(bookId)) {
          bookIds.push(bookId);
        }
        return this.updateSectionData(section, bookIds);
      }),
      catchError(this.handleError<{id: string}[]>('addBookToSection', []))
    );
  }

  // Remove book from section
  removeBookFromSection(section: 'newlyLaunchedBooks' | 'bestSellers' | 'specialOffers', bookId: string): Observable<{id: string}[]> {
    return this.getSectionData(section).pipe(
      switchMap(currentData => {
        const bookIds = currentData.map(item => item.id).filter(id => id !== bookId);
        return this.updateSectionData(section, bookIds);
      }),
      catchError(this.handleError<{id: string}[]>('removeBookFromSection', []))
    );
  }

  // for 3 sections on the home page
  getBookByIds(section: 'newlyLaunchedBooks' | 'bestSellers' | 'specialOffers'): Observable<BookModel[]> {
    return this.http.get<{ id: string }[]>(`${this.baseUrl}/${section}`).pipe(
      map(data => data.map(item => item.id)),
      switchMap((ids: string[]) => this.getAllBooks().pipe(
        map((allBooks: BookModel[]) => allBooks.filter(b => ids.includes(b.id.toString())))
      )),
      catchError(this.handleError<BookModel[]>('getBookByIds', []))
    );
  }

  searchBooks(query: string, filters?: any): Observable<{ results: BookModel[], total: number }> {
    if (!query || query.trim() === '') {
      return this.getAllBooks().pipe(
        map(books => ({ results: books, total: books.length }))
      );
    }
    
    return this.http.get<any[]>(`${this.booksUrl}/search?query=${encodeURIComponent(query)}`).pipe(
      map(books => {
        const mappedBooks = books.map(book => this.mapBackendBookToFrontend(book));
        let filteredBooks = mappedBooks;
        
        // Apply additional frontend filters if provided
        if (filters) {
          // Apply category filter
          if (filters.categories && filters.categories.length > 0) {
            filteredBooks = filteredBooks.filter(book =>
              book.categories?.some(bookCategory => 
                filters.categories.some((filterCategory: string) => 
                  bookCategory.name?.toLowerCase() === filterCategory.toLowerCase()
                )
              )
            );
          }
          
          // Apply price filter
          if (filters.maxPrice && filters.maxPrice > 0) {
            const maxPrice = Number(filters.maxPrice);
            if (!isNaN(maxPrice)) {
              filteredBooks = filteredBooks.filter(book => book.price <= maxPrice);
            }
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

  getSimilarBooks(bookId: string | number): Observable<BookModel[]> {
    return this.http.get<any[]>(`${this.booksUrl}/${bookId}/similar`).pipe(
      map(books => books.map(book => this.mapBackendBookToFrontend(book))),
      catchError(this.handleError<BookModel[]>('getSimilarBooks', []))
    );
  }

  // New methods for books_by_category management
  
  // Get all books by category data
  getBooksByCategory(): Observable<BookCategoryData[]> {
    return this.http.get<BookCategoryData[]>(this.booksByCategoryUrl).pipe(
      catchError(this.handleError<BookCategoryData[]>('getBooksByCategory', []))
    );
  }

  // Get books for a specific category (newly launched, highly rated, special offers)
  getBooksByCategoryType(category: 'newly launched' | 'highly rated' | 'special offers'): Observable<BookWithSales[]> {
    return this.getBooksByCategory().pipe(
      switchMap(categoryData => {
        const bookIds = categoryData
          .filter(item => item.category === category)
          .map(item => item.id);
        
        if (bookIds.length === 0) {
          return of([]);
        }
        
        return this.getAllBooks().pipe(
          map(allBooks => {
            const booksWithSales = allBooks
              .filter(book => bookIds.includes(book.id.toString()))
              .map(book => {
                const categoryItem = categoryData.find(item => item.id === book.id.toString());
                return {
                  ...book,
                  no_of_books_sold: categoryItem ? categoryItem.no_of_books_sold : 0
                };
              });
            return booksWithSales;
          })
        );
      }),
      catchError(this.handleError<BookWithSales[]>('getBooksByCategoryType', []))
    );
  }

  // Add book to a category
  addBookToCategory(bookId: string, category: 'newly launched' | 'highly rated' | 'special offers'): Observable<BookCategoryData> {
    const newCategoryData: BookCategoryData = {
      id: bookId,
      category: category,
      no_of_books_sold: 0
    };
    
    return this.http.post<BookCategoryData>(this.booksByCategoryUrl, newCategoryData).pipe(
      catchError(this.handleError<BookCategoryData>('addBookToCategory'))
    );
  }

  // Remove book from a category
  removeBookFromCategory(bookId: string, category: 'newly launched' | 'highly rated' | 'special offers'): Observable<any> {
    return this.getBooksByCategory().pipe(
      switchMap(categoryData => {
        const itemToRemove = categoryData.find(item => 
          item.id === bookId && item.category === category
        );
        
        if (itemToRemove) {
          return this.http.delete(`${this.booksByCategoryUrl}/${itemToRemove.id}`);
        }
        return of(null);
      }),
      catchError(this.handleError<any>('removeBookFromCategory'))
    );
  }

  // Update book sales count (called when a book is purchased)
  updateBookSalesCount(bookId: string, category: 'newly launched' | 'highly rated' | 'special offers'): Observable<BookCategoryData | null> {
    return this.getBooksByCategory().pipe(
      switchMap(categoryData => {
        const itemToUpdate = categoryData.find(item => 
          item.id === bookId && item.category === category
        );
        
        if (itemToUpdate) {
          const updatedData = {
            ...itemToUpdate,
            no_of_books_sold: itemToUpdate.no_of_books_sold + 1
          };
          return this.http.put<BookCategoryData>(`${this.booksByCategoryUrl}/${itemToUpdate.id}`, updatedData);
        }
        return of(null);
      }),
      catchError(this.handleError<BookCategoryData | null>('updateBookSalesCount', null))
    );
  }

  // Get highly sold books (for admin dashboard)
  getHighlySoldBooks(limit: number = 10): Observable<BookWithSales[]> {
    return this.http.get<any[]>(`${this.booksUrl}/highly-sold?limit=${limit}`).pipe(
      map(books => books.map(book => {
        const mappedBook = this.mapBackendBookToFrontend(book);
        return {
          ...mappedBook,
          no_of_books_sold: book.noOfBooksSold || 0
        } as BookWithSales;
      })),
      catchError(this.handleError<BookWithSales[]>('getHighlySoldBooks', []))
    );
  }

  // Get least sold books (for admin dashboard)
  getLeastSoldBooks(limit: number = 10): Observable<BookWithSales[]> {
    return this.http.get<any[]>(`${this.booksUrl}/least-sold?limit=${limit}`).pipe(
      map(books => books.map(book => {
        const mappedBook = this.mapBackendBookToFrontend(book);
        return {
          ...mappedBook,
          no_of_books_sold: book.noOfBooksSold || 0
        } as BookWithSales;
      })),
      catchError(this.handleError<BookWithSales[]>('getLeastSoldBooks', []))
    );
  }

  // Updated methods for the three sections on home page
  getNewlyLaunchedBooks(): Observable<BookWithSales[]> {
    return this.http.get<any[]>(`${this.booksUrl}/sales-category/NEWLY_LAUNCHED`).pipe(
      map(books => books.map(book => {
        const mappedBook = this.mapBackendBookToFrontend(book);
        return {
          ...mappedBook,
          no_of_books_sold: book.noOfBooksSold || 0
        } as BookWithSales;
      })),
      catchError(this.handleError<BookWithSales[]>('getNewlyLaunchedBooks', []))
    );
  }

  getBestSellers(): Observable<BookWithSales[]> {
    return this.http.get<any[]>(`${this.booksUrl}/sales-category/BEST_SELLING`).pipe(
      map(books => books.map(book => {
        const mappedBook = this.mapBackendBookToFrontend(book);
        return {
          ...mappedBook,
          no_of_books_sold: book.noOfBooksSold || 0
        } as BookWithSales;
      })),
      catchError(this.handleError<BookWithSales[]>('getBestSellers', []))
    );
  }

  getSpecialOffers(): Observable<BookWithSales[]> {
    return this.http.get<any[]>(`${this.booksUrl}/sales-category/SPECIAL_OFFERS`).pipe(
      map(books => books.map(book => {
        const mappedBook = this.mapBackendBookToFrontend(book);
        return {
          ...mappedBook,
          no_of_books_sold: book.noOfBooksSold || 0
        } as BookWithSales;
      })),
      catchError(this.handleError<BookWithSales[]>('getSpecialOffers', []))
    );
  }

  // Get books by category for admin dashboard
  getBooksByCategoryForAdmin(category: 'newly launched' | 'highly rated' | 'special offers'): Observable<BookWithSales[]> {
    const salesCategory = category === 'newly launched' ? 'NEWLY_LAUNCHED' : 
                         category === 'highly rated' ? 'BEST_SELLING' : 'SPECIAL_OFFERS';
    return this.http.get<any[]>(`${this.booksUrl}/sales-category/${salesCategory}`).pipe(
      map(books => books.map(book => {
        const mappedBook = this.mapBackendBookToFrontend(book);
        return {
          ...mappedBook,
          no_of_books_sold: book.noOfBooksSold || 0
        } as BookWithSales;
      })),
      catchError(this.handleError<BookWithSales[]>('getBooksByCategoryForAdmin', []))
    );
  }

  // Get all books for admin (including inactive ones)
  getAllBooksForAdmin(): Observable<BookModel[]> {
    return this.http.get<any[]>(`${this.booksUrl}/admin/all`).pipe(
      map(books => books.map(book => this.mapBackendBookToFrontend(book))),
      catchError(this.handleError<BookModel[]>('getAllBooksForAdmin', []))
    );
  }

  // Restore a soft-deleted book
  restoreBook(id: string | number): Observable<any> {
    return this.http.put(`${this.booksUrl}/${id}/restore`, {}).pipe(
      catchError(this.handleError<any>('restoreBook'))
    );
  }

  // Get all books with their category information for admin
  getAllBooksWithCategories(): Observable<{ book: BookModel; categories: string[]; salesCount: number }[]> {
    return forkJoin({
      books: this.getAllBooks(),
      categoryData: this.getBooksByCategory()
    }).pipe(
      map(({ books, categoryData }) => {
        return books.map(book => {
          const bookCategories = categoryData.filter(item => item.id === book.id.toString());
          const totalSales = bookCategories.reduce((sum, item) => sum + item.no_of_books_sold, 0);
          return {
            book,
            categories: bookCategories.map(item => item.category),
            salesCount: totalSales
          };
        });
      }),
      catchError(this.handleError<{ book: BookModel; categories: string[]; salesCount: number }[]>('getAllBooksWithCategories', []))
    );
  }

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.error(`${operation} failed: ${error.message}`);
      return of(result as T);
    };
  }

  // Map sales category from backend to frontend format
  private mapSalesCategory(backendCategory: string): 'BEST_SELLING' | 'NEWLY_LAUNCHED' | 'FEATURED' | 'SPECIAL_OFFERS' | undefined {
    switch(backendCategory) {
      case 'NEWLY_LAUNCHED': return 'NEWLY_LAUNCHED';
      case 'SPECIAL_OFFERS': return 'SPECIAL_OFFERS';
      case 'BEST_SELLING': return 'BEST_SELLING';
      default: return 'FEATURED';
    }
  }

  // Map backend BookWithRelations to frontend BookModel
  private mapBackendBookToFrontend(backendBook: any): BookModel {
    return {
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
      // Legacy properties for backward compatibility
      stock_display: backendBook.stockDisplay,
      stock_actual: backendBook.stockActual,
      image_urls: backendBook.images?.map((img: any) => img.imageUrl) || [],
      customerRatings: [] // Will be handled by review service
    };
  }

  // Format date for backend (convert array format to ISO string)
  private formatDateForBackend(date: any): string | null {
    if (!date) return null;
    
    // If it's already a string, return as is
    if (typeof date === 'string') {
      return date;
    }
    
    // If it's an array [year, month, day, hour, minute], convert to LocalDateTime format
    if (Array.isArray(date) && date.length >= 3) {
      const [year, month, day, hour = 0, minute = 0] = date;
      const dateObj = new Date(year, month - 1, day, hour, minute); // month is 0-indexed in JS
      return dateObj.toISOString(); // Return full ISO string for LocalDateTime
    }
    
    // If it's a Date object, convert to ISO string
    if (date instanceof Date) {
      return date.toISOString();
    }
    
    return null;
  }

  // Map frontend BookModel to backend format for create/update
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

    // NOTE: Backend updateBookFromRequest method explicitly skips categories and images
    // From BookService.java lines 240-242:
    // "For now, we'll skip updating categories and images in the update method
    // to avoid JPA relationship complexities. These can be managed separately."
    
    // TODO: Categories and images updates need separate endpoints
    // For now, we only send basic book fields for updates
    
    // Categories and images are NOT supported in book updates by the backend
    // if (frontendBook.categories && frontendBook.categories.length > 0) {
    //   backendData.categoryIds = frontendBook.categories.map(cat => 
    //     typeof cat === 'object' ? cat.id : cat
    //   );
    // }

    // if (frontendBook.images && frontendBook.images.length > 0) {
    //   backendData.images = frontendBook.images.map((img, index) => ({
    //     imageUrl: typeof img === 'object' ? img.imageUrl : img,
    //     isPrimary: typeof img === 'object' ? img.isPrimary : (index === 0),
    //     altText: typeof img === 'object' ? img.altText : frontendBook.title,
    //     displayOrder: index
    //   }));
    // }

    return backendData;
  }

  // Update book categories separately
  updateBookCategories(id: string | number, categoryIds: number[]): Observable<BookModel> {
    const stringId = this.toStringId(id);
    console.group('🏷️ BOOK SERVICE: updateBookCategories() Method Called');
    console.log('📍 Service Method: updateBookCategories');
    console.log('🆔 Book ID:', stringId);
    console.log('🏷️ Category IDs:', categoryIds);
    console.log('🌐 Request URL:', `${this.booksUrl}/${stringId}/categories`);
    console.log('📡 HTTP Method: PUT');
    console.groupEnd();
    
    return this.http.put<any>(`${this.booksUrl}/${stringId}/categories`, categoryIds).pipe(
      map(updatedBook => {
        console.log('✅ Categories updated successfully:', updatedBook);
        return this.mapBackendBookToFrontend(updatedBook);
      }),
      catchError(error => {
        console.error('❌ Failed to update categories:', error);
        throw error;
      })
    );
  }

  // Update book images separately
  updateBookImages(id: string | number, images: any[]): Observable<BookModel> {
    const stringId = this.toStringId(id);
    const backendImages = images.map((img, index) => ({
      imageUrl: typeof img === 'object' ? img.imageUrl : img,
      isPrimary: typeof img === 'object' ? img.isPrimary : (index === 0),
      altText: typeof img === 'object' ? img.altText : 'Book image',
      displayOrder: index
    }));
    
    console.group('🖼️ BOOK SERVICE: updateBookImages() Method Called');
    console.log('📍 Service Method: updateBookImages');
    console.log('🆔 Book ID:', stringId);
    console.log('🖼️ Images:', backendImages);
    console.log('🌐 Request URL:', `${this.booksUrl}/${stringId}/images`);
    console.log('📡 HTTP Method: PUT');
    console.groupEnd();
    
    return this.http.put<any>(`${this.booksUrl}/${stringId}/images`, backendImages).pipe(
      map(updatedBook => {
        console.log('✅ Images updated successfully:', updatedBook);
        return this.mapBackendBookToFrontend(updatedBook);
      }),
      catchError(error => {
        console.error('❌ Failed to update images:', error);
        throw error;
      })
    );
  }

  // Review Management Methods
  
  /**
   * Add a review to a book
   * @param bookId - Book ID
   * @param review - Review data
   * @returns Observable of updated book
   */
  addReview(bookId: string, review: { rating: number; review: string; userId?: string; userName?: string }): Observable<BookModel> {
    return this.getBookById(bookId).pipe(
      switchMap(book => {
        if (!book.customerRatings) {
          book.customerRatings = [];
        }
        
        const newReview = {
          userId: review.userId || 'anonymous',
          userName: review.userName || 'Anonymous User',
          rating: review.rating,
          review: review.review,
          createdAt: new Date().toISOString()
        };
        
        book.customerRatings.push(newReview);
        return this.updateBook(bookId, book);
      }),
      catchError(this.handleError<BookModel>('addReview'))
    );
  }
  
  /**
   * Delete a review from a book
   * @param bookId - Book ID
   * @param userId - User ID who wrote the review
   * @returns Observable of updated book
   */
  deleteReview(bookId: string | number, userId: string): Observable<BookModel> {
    return this.getBookById(bookId).pipe(
      switchMap(book => {
        if (book.customerRatings) {
          book.customerRatings = book.customerRatings.filter(r => r.userId !== userId);
        }
        return this.updateBook(bookId, book);
      }),
      catchError(this.handleError<BookModel>('deleteReview'))
    );
  }
  
  /**
   * Update a review for a book
   * @param bookId - Book ID
   * @param userId - User ID who wrote the review
   * @param updatedReview - Updated review data
   * @returns Observable of updated book
   */
  updateReview(bookId: string, userId: string, updatedReview: { rating: number; review: string }): Observable<BookModel> {
    return this.getBookById(bookId).pipe(
      switchMap(book => {
        if (book.customerRatings) {
          const reviewIndex = book.customerRatings.findIndex(r => r.userId === userId);
          if (reviewIndex !== -1) {
            book.customerRatings[reviewIndex] = {
              ...book.customerRatings[reviewIndex],
              rating: updatedReview.rating,
              review: updatedReview.review,
              createdAt: new Date().toISOString() // Update timestamp
            };
          }
        }
        return this.updateBook(bookId, book);
      }),
      catchError(this.handleError<BookModel>('updateReview'))
    );
  }
  
  /**
   * Get all reviews for a specific book
   * @param bookId - Book ID
   * @returns Observable of customer ratings array
   */
  getBookReviews(bookId: string): Observable<CustomerRating[]> {
    return this.getBookById(bookId).pipe(
      map(book => book.customerRatings || []),
      catchError(this.handleError<CustomerRating[]>('getBookReviews', []))
    );
  }
  
  /**
   * Get average rating for a book
   * @param bookId - Book ID
   * @returns Observable of average rating
   */
  getBookAverageRating(bookId: string): Observable<number> {
    return this.getBookReviews(bookId).pipe(
      map(reviews => {
        if (reviews.length === 0) return 0;
        const total = reviews.reduce((sum, review) => sum + review.rating, 0);
        return Math.round((total / reviews.length) * 10) / 10;
      }),
      catchError(this.handleError<number>('getBookAverageRating', 0))
    );
  }

  /**
   * Get user's review for a specific book
   * @param bookId - Book ID
   * @param userId - User ID
   * @returns Observable of customer rating or null
   */
  getUserReviewForBook(bookId: string, userId: string): Observable<CustomerRating | null> {
    return this.getBookReviews(bookId).pipe(
      map(reviews => reviews.find(r => r.userId === userId) || null),
      catchError(this.handleError<CustomerRating | null>('getUserReviewForBook', null))
    );
  }


}
