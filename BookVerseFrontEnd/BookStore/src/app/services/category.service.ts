import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, of, tap } from 'rxjs';
import { CategoryModel } from '../models/category.model';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private baseUrl = 'http://localhost:8080/api';
  private categoriesUrl = `${this.baseUrl}/categories`;
  private fallbackUrl = 'http://localhost:3000/All_categories'; // json-server

  constructor(private http: HttpClient) {}

  // Get all categories with fallback mechanism
  getAllCategories(): Observable<CategoryModel[]> {
    console.log('CategoryService: Fetching categories from:', this.categoriesUrl);
    
    return this.http.get<CategoryModel[]>(this.categoriesUrl).pipe(
      tap(categories => {
        console.log('CategoryService: Successfully fetched categories:', categories);
      }),
      catchError((error: HttpErrorResponse) => {
        console.warn('CategoryService: Primary endpoint failed, trying fallback:', error.message);
        
        // Try fallback endpoint
        return this.http.get<CategoryModel[]>(this.fallbackUrl).pipe(
          tap(categories => {
            console.log('CategoryService: Successfully fetched categories from fallback:', categories);
          }),
          catchError((fallbackError: HttpErrorResponse) => {
            console.error('CategoryService: Both endpoints failed:', fallbackError.message);
            return this.handleError<CategoryModel[]>('getAllCategories', this.getDefaultCategories())(fallbackError);
          })
        );
      })
    );
  }

  // Get default categories as last resort
  private getDefaultCategories(): CategoryModel[] {
    return [
      { id: '1', name: 'Fantasy' },
      { id: '2', name: 'Adventure' },
      { id: '3', name: 'Romance' },
      { id: '4', name: 'Thriller' },
      { id: '5', name: 'Mystery' },
      { id: '6', name: 'Horror' },
      { id: '7', name: 'Fiction' },
      { id: '8', name: 'Biography' },
      { id: '9', name: 'Historical' },
      { id: '10', name: 'Literature' }
    ];
  }

  // Search categories
  searchCategories(query: string): Observable<CategoryModel[]> {
    return this.http.get<CategoryModel[]>(`${this.categoriesUrl}/search?query=${query}`).pipe(
      catchError(this.handleError<CategoryModel[]>('searchCategories', []))
    );
  }

  // Create category
  createCategory(category: CategoryModel): Observable<CategoryModel> {
    return this.http.post<CategoryModel>(this.categoriesUrl, category).pipe(
      catchError(this.handleError<CategoryModel>('createCategory'))
    );
  }

  // Update category
  updateCategory(id: string, category: CategoryModel): Observable<CategoryModel> {
    return this.http.put<CategoryModel>(`${this.categoriesUrl}/${id}`, category).pipe(
      catchError(this.handleError<CategoryModel>('updateCategory'))
    );
  }

  // Delete category
  deleteCategory(id: string): Observable<any> {
    return this.http.delete(`${this.categoriesUrl}/${id}`).pipe(
      catchError(this.handleError<any>('deleteCategory'))
    );
  }

  // Get active categories for navigation (sorted by display order)
  getActiveCategoriesForNavigation(): Observable<CategoryModel[]> {
    return this.http.get<CategoryModel[]>(`${this.categoriesUrl}/active`).pipe(
      catchError(this.handleError<CategoryModel[]>('getActiveCategoriesForNavigation', []))
    );
  }

  // Toggle category active status
  toggleCategoryActiveStatus(id: string): Observable<CategoryModel> {
    return this.http.patch<CategoryModel>(`${this.categoriesUrl}/${id}/toggle-active`, {}).pipe(
      catchError(this.handleError<CategoryModel>('toggleCategoryActiveStatus'))
    );
  }

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.error(`CategoryService: ${operation} failed:`, error);
      
      // Let the app keep running by returning an empty result or default data
      return of(result as T);
    };
  }
}