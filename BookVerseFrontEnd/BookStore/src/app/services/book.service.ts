import { Injectable } from '@angular/core';
import { BookModel, BookCreateRequest, BookCategoryData, BookWithSales } from '../models/book.model';
import { Observable, of, switchMap, map, catchError, forkJoin } from 'rxjs';
import { HttpClient, HttpParams } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class BookService {
  private baseUrl = 'http://localhost:8090/api';
  
  private booksUrl = `${this.baseUrl}/books`;
  // private booksByCategoryUrl = 'http://localhost:3000/books_by_category'; // json server old one - REMOVED

  constructor(private http: HttpClient) { }

  // Transform backend book response to frontend model
  private transformBackendBook(backendBook: any): BookModel {
    console.log('Transforming backend book:', backendBook);
    
    return {
      ...backendBook,
      // Map categories from BookWithRelations DTO (already in the correct format)
      categories: backendBook.categories?.map((cat: any) => ({
        id: cat.id?.toString(),
        name: cat.name || 'Unknown',
        slug: cat.slug
      })) || [], // Default to an empty array if no categories exist
      
      // Map images from BookWithRelations DTO or fallback to legacy format
      images: backendBook.images?.map((img: any) => ({
        id: img.id?.toString(),
        imageUrl: img.imageUrl, // BookWithRelations DTO uses 'imageUrl' field name
        isPrimary: img.isPrimary,
        altText: img.altText,
        displayOrder: img.displayOrder || 0
      })) || (backendBook.image_urls?.map((url: string, index: number) => ({
        id: `${backendBook.id}-${index}`,
        imageUrl: url,
        isPrimary: index === 0,
        altText: backendBook.title,
        displayOrder: index
      })) || [])
    };
  }

  // Transform array of backend books
  private transformBackendBooks(backendBooks: any[]): BookModel[] {
    return backendBooks.map(book => this.transformBackendBook(book));
  }

  // Get all active books
  getAllBooks(): Observable<BookModel[]> {
    return this.http.get<any[]>(this.booksUrl).pipe(
      map(books => this.transformBackendBooks(books)),
      catchError(this.handleError<BookModel[]>('getAllBooks', []))
    );
  }

  // Get all books for admin (including inactive ones)
  getAllBooksForAdmin(): Observable<BookModel[]> {
    return this.http.get<any[]>(`${this.booksUrl}/admin/all`).pipe(
      map(books => this.transformBackendBooks(books)),
      catchError(this.handleError<BookModel[]>('getAllBooksForAdmin', []))
    );
  }

  // Get book by ID (basic)
  getBookById(id: string): Observable<BookModel> {
    return this.http.get<any>(`${this.booksUrl}/${id}`).pipe(
      map(book => this.transformBackendBook(book)),
      catchError(this.handleError<BookModel>(`getBookById id=${id}`))
    );
  }

  // Get book with relations (categories and images)
  getBookWithRelations(id: string): Observable<BookModel> {
    return this.http.get<any>(`${this.booksUrl}/${id}`).pipe(
      map(book => this.transformBackendBook(book)),
      catchError(this.handleError<BookModel>(`getBookWithRelations id=${id}`))
    );
  }

  // createBook(book: Omit<BookModel, 'id'>): Observable<BookModel> {
  //   return this.http.post<BookModel>(this.booksUrl, book).pipe(
  //     catchError(this.handleError<BookModel>('createBook'))
  //   );
  // }

  // Create book with relations 
  createBookWithRelations(bookData: BookCreateRequest): Observable<BookModel> {
    // Validate required fields
    if (!bookData.title || !bookData.author || !bookData.description) {
      throw new Error('Title, author, and description are required');
    }
    
    if (!bookData.categoryIds || bookData.categoryIds.length === 0) {
      throw new Error('At least one category must be selected');
    }

    // Log the request for debugging
    console.log('Sending BookCreateRequest to backend:', {
      url: this.booksUrl,
      data: bookData,
      headers: { 'Content-Type': 'application/json' }
    });

    return this.http.post<any>(this.booksUrl, bookData, {
      headers: { 'Content-Type': 'application/json' }
    }).pipe(
      map(response => {
        console.log('Backend response:', response);
        return this.transformBackendBook(response);
      }),
      catchError((error) => {
        console.error('Backend error:', error);
        console.error('Error status:', error.status);
        console.error('Error body:', error.error);
        return this.handleError<BookModel>('createBookWithRelations')(error);
      })
    );
  }

  // Update existing book 
  updateBook(id: string, book: Partial<BookModel>): Observable<BookModel> {
    return this.http.put<any>(`${this.booksUrl}/${id}`, book).pipe(
      map(response => this.transformBackendBook(response)),
      catchError(this.handleError<BookModel>('updateBook'))
    );
  }

  // Update book with relations 
  updateBookWithRelations(id: string, bookData: BookCreateRequest): Observable<BookModel> {
    return this.http.put<any>(`${this.booksUrl}/${id}`, bookData).pipe(
      map(response => this.transformBackendBook(response)),
      catchError(this.handleError<BookModel>('updateBookWithRelations'))
    );
  }

  // Soft delete book 
  deleteBook(id: string): Observable<any> {
    return this.http.delete(`${this.booksUrl}/${id}`).pipe(
      catchError(this.handleError<any>('deleteBook'))
    );
  }

  // Restore soft-deleted book
  restoreBook(id: string): Observable<any> {
    return this.http.put(`${this.booksUrl}/${id}/restore`, {}).pipe(
      catchError(this.handleError<any>('restoreBook'))
    );
  }

  // Update book stock 
  updateBookStock(bookId: string, quantity: number): Observable<any> {
    return this.http.put<any>(`${this.booksUrl}/${bookId}/stock?quantity=${quantity}`, {}).pipe(
      catchError(this.handleError<any>('updateBookStock'))
    );
  }

  // Search books
  searchBooks(query: string, filters?: any): Observable<{ results: BookModel[], total: number }> {
    if (filters) {
      return this.getAllBooks().pipe(
        map(allBooks => {
          let filteredBooks = allBooks;
          const lowerCaseQuery = query.toLowerCase();
          
          // text search
          if (query) {
            filteredBooks = filteredBooks.filter(book =>
              book.title.toLowerCase().includes(lowerCaseQuery) ||
              book.author.toLowerCase().includes(lowerCaseQuery) ||
              book.description.toLowerCase().includes(lowerCaseQuery) ||
              book.categories.some(cat => cat.name.toLowerCase().includes(lowerCaseQuery))
            );
          }
          
          // category 
          if (filters.categories && filters.categories.length > 0) {
            filteredBooks = filteredBooks.filter(book =>
              book.categories.some(bookCategory => 
                filters.categories.some((filterCategory: string) => 
                  bookCategory.name.toLowerCase() === filterCategory.toLowerCase()
                )
              )
            );
          }
          
          // price
          if (filters.maxPrice && filters.maxPrice > 0) {
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
    } else {
      // Use backend search
      return this.http.get<any[]>(`${this.booksUrl}/search?query=${query}`).pipe(
        map(books => ({ 
          results: this.transformBackendBooks(books), 
          total: books.length 
        })),
        catchError(this.handleError<{ results: BookModel[], total: number }>('searchBooks', { results: [], total: 0 }))
      );
    }
  }

  // Get similar books
  getSimilarBooks(bookId: string): Observable<BookModel[]> {
    return this.http.get<any[]>(`${this.booksUrl}/${bookId}/similar`).pipe(
      map(books => this.transformBackendBooks(books)),
      catchError(this.handleError<BookModel[]>('getSimilarBooks', []))
    );
  }

  // Get books by sales category
  getBooksBySalesCategory(salesCategory: string): Observable<BookModel[]> {
    return this.http.get<any[]>(`${this.booksUrl}/sales-category/${salesCategory}`).pipe(
      map(books => this.transformBackendBooks(books)),
      catchError(this.handleError<BookModel[]>('getBooksBySalesCategory', []))
    );
  }

  // Get highly sold books
  getHighlySoldBooks(limit: number = 10): Observable<BookModel[]> {
    return this.http.get<any[]>(`${this.booksUrl}/highly-sold?limit=${limit}`).pipe(
      map(books => this.transformBackendBooks(books)),
      catchError(this.handleError<BookModel[]>('getHighlySoldBooks', []))
    );
  }

  // Get least sold books
  getLeastSoldBooks(limit: number = 10): Observable<BookModel[]> {
    return this.http.get<any[]>(`${this.booksUrl}/least-sold?limit=${limit}`).pipe(
      map(books => this.transformBackendBooks(books)),
      catchError(this.handleError<BookModel[]>('getLeastSoldBooks', []))
    );
  }

  // COMMENTED OUT - Legacy methods for backward compatibility - These are unused and rely on JSON server
  // getBookByIds(section: 'newlyLaunchedBooks' | 'bestSellers' | 'specialOffers'): Observable<BookModel[]> {
  //   return this.http.get<{ id: string }[]>(`${this.baseUrl}/${section}`).pipe(
  //     map(data => data.map(item => item.id)),
  //     switchMap((ids: string[]) => this.getAllBooks().pipe(
  //       map((allBooks: BookModel[]) => allBooks.filter(b => ids.includes(b.id))
  //     )),
  //     catchError(this.handleError<BookModel[]>('getBookByIds', []))
  //   );
  // }

  // COMMENTED OUT - Get books by category data (legacy) - Uses JSON server
  // getBooksByCategory(): Observable<BookCategoryData[]> {
  //   return this.http.get<BookCategoryData[]>(this.booksByCategoryUrl).pipe(
  //     catchError(this.handleError<BookCategoryData[]>('getBooksByCategory', []))
  //   );
  // }

  // COMMENTED OUT - Legacy methods for section management - These use JSON server and are not needed with Spring backend
  // addBookToCategory(bookId: string, category: 'newly launched' | 'highly rated' | 'special offers'): Observable<BookCategoryData> {
  //   const newCategoryData: BookCategoryData = {
  //     id: bookId,
  //     category: category,
  //     no_of_books_sold: 0
  //   };
  //   
  //   return this.http.post<BookCategoryData>(this.booksByCategoryUrl, newCategoryData).pipe(
  //     catchError(this.handleError<BookCategoryData>('addBookToCategory'))
  //   );
  // }

  // removeBookFromCategory(bookId: string, category: 'newly launched' | 'highly rated' | 'special offers'): Observable<any> {
  //   return this.getBooksByCategory().pipe(
  //     switchMap(categoryData => {
  //       const itemToRemove = categoryData.find(item => 
  //         item.id === bookId && item.category === category
  //       );
  //       
  //       if (itemToRemove) {
  //         return this.http.delete(`${this.booksByCategoryUrl}/${itemToRemove.id}`);
  //       }
  //       return of(null);
  //     }),
  //     catchError(this.handleError<any>('removeBookFromCategory'))
  //   );
  // }

  // getBooksByCategoryType(category: 'newly launched' | 'highly rated' | 'special offers'): Observable<BookWithSales[]> {
  //   return this.getBooksByCategory().pipe(
  //     switchMap(categoryData => {
  //       const bookIds = categoryData
  //         .filter(item => item.category === category)
  //         .map(item => item.id);
  //       
  //       if (bookIds.length === 0) {
  //         return of([]);
  //       }
  //       
  //       return this.getAllBooks().pipe(
  //         map(allBooks => {
  //           const booksWithSales = allBooks
  //             .filter(book => bookIds.includes(book.id))
  //             .map(book => {
  //               const categoryItem = categoryData.find(item => item.id === book.id);
  //               return {
  //                 ...book,
  //                 no_of_books_sold: categoryItem ? categoryItem.no_of_books_sold : 0
  //               };
  //             });
  //           return booksWithSales;
  //         })
  //       );
  //     }),
  //     catchError(this.handleError<BookWithSales[]>('getBooksByCategoryType', []))
  //   );
  // }

  // REPLACED WITH SPRING BACKEND EQUIVALENTS - Use getBooksBySalesCategory instead
  getNewlyLaunchedBooks(): Observable<BookModel[]> {
    return this.getBooksBySalesCategory('NEWLY_LAUNCHED');
  }

  getBestSellers(): Observable<BookModel[]> {
    return this.getBooksBySalesCategory('BEST_SELLING');
  }

  getSpecialOffers(): Observable<BookModel[]> {
    return this.getBooksBySalesCategory('SPECIAL_OFFERS');
  }

  // Add review to a book
  addReview(bookId: string, reviewData: { rating: number, review: string, userId: string, userName: string }): Observable<any> {
    return this.http.post<any>(`${this.booksUrl}/${bookId}/reviews`, reviewData).pipe(
      catchError(this.handleError<any>('addReview'))
    );
  }

  // Update review for a book
  updateReview(bookId: string, reviewData: { rating: number, review: string, userId: string, userName: string }): Observable<any> {
    return this.http.put<any>(`${this.booksUrl}/${bookId}/reviews`, reviewData).pipe(
      catchError(this.handleError<any>('updateReview'))
    );
  }

  // Delete review for a book
  deleteReview(bookId: string, userId: string): Observable<any> {
    return this.http.delete<any>(`${this.booksUrl}/${bookId}/reviews/${userId}`).pipe(
      catchError(this.handleError<any>('deleteReview'))
    );
  }

  // Get all reviews for a book
  getBookReviews(bookId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.booksUrl}/${bookId}/reviews`).pipe(
      catchError(this.handleError<any[]>('getBookReviews', []))
    );
  }

  // Get user's review for a specific book
  getUserReviewForBook(bookId: string, userId: string): Observable<any> {
    return this.http.get<any>(`${this.booksUrl}/${bookId}/reviews/${userId}`).pipe(
      catchError(this.handleError<any>('getUserReviewForBook'))
    );
  }

  // Get average rating for a book
  getBookAverageRating(bookId: string): Observable<number> {
    return this.http.get<{ averageRating: number }>(`${this.booksUrl}/${bookId}/rating`).pipe(
      map(response => response.averageRating),
      catchError(this.handleError<number>('getBookAverageRating', 0))
    );
  }

  // COMMENTED OUT - Legacy method for order service compatibility - Not needed with Spring backend
  // updateBookSalesCount(bookId: string, category: 'newly launched' | 'highly rated' | 'special offers'): Observable<any> {
  //   return this.getBooksByCategory().pipe(
  //     switchMap(categoryData => {
  //       const itemToUpdate = categoryData.find item => 
  //         item.id === bookId && item.category === category
  //       );
  //       
  //       if (itemToUpdate) {
  //         const updatedData = {
  //           ...itemToUpdate,
  //           no_of_books_sold: itemToUpdate.no_of_books_sold + 1
  //         };
  //         return this.http.put(`${this.booksByCategoryUrl}/${itemToUpdate.id}`, updatedData);
  //       }
  //       return of(null);
  //     }),
  //     catchError(this.handleError<any>('updateBookSalesCount', null))
  //   );
  // }

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.error(`${operation} failed: ${error.message}`);
      return of(result as T);
    };
  }
}