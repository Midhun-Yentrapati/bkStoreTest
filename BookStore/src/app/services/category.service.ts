import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, catchError, of, switchMap } from 'rxjs';
import { CategoryModel } from '../models/category.model';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  
  private baseUrl = 'http://localhost:8090/api'; // API Gateway URL
  private categoriesUrl = `${this.baseUrl}/categories`; // Categories endpoint via book-catalog service
  private allCategoriesUrl = `${this.baseUrl}/categories/all`; // All available categories

  constructor(private http: HttpClient) {}
    
  // Get active categories (those that appear in customer navigation)
  getActiveCategories(): Observable<CategoryModel[]> {
    return this.http.get<CategoryModel[]>(this.categoriesUrl).pipe(
      catchError(this.handleError<CategoryModel[]>('getActiveCategories', []))
    );
  }

  // Get all available categories (from All_categories)
  getAllAvailableCategories(): Observable<CategoryModel[]> {
    return this.http.get<CategoryModel[]>(this.allCategoriesUrl).pipe(
      catchError(this.handleError<CategoryModel[]>('getAllAvailableCategories', []))
    );
  }

  // Legacy method for backward compatibility
  getAllCategories(): Observable<CategoryModel[]> {
    return this.getActiveCategories();
  }

  // Update active categories list
  updateActiveCategories(categories: CategoryModel[]): Observable<CategoryModel[]> {
    return this.http.put<CategoryModel[]>(this.categoriesUrl, categories).pipe(
      catchError(this.handleError<CategoryModel[]>('updateActiveCategories', []))
    );
  }

  // Add category to active list
  addCategoryToActive(categoryId: number): Observable<CategoryModel[]> {
    return this.getAllAvailableCategories().pipe(
      switchMap(allCategories => {
        const categoryToAdd = allCategories.find(cat => cat.id === categoryId);
        if (!categoryToAdd) {
          throw new Error('Category not found in available categories');
        }
        
        return this.getActiveCategories().pipe(
          switchMap(activeCategories => {
            const isAlreadyActive = activeCategories.some(cat => cat.id === categoryId);
            if (!isAlreadyActive) {
              activeCategories.push(categoryToAdd);
            }
            return this.updateActiveCategories(activeCategories);
          })
        );
      }),
      catchError(this.handleError<CategoryModel[]>('addCategoryToActive', []))
    );
  }

  // Remove category from active list
  removeCategoryFromActive(categoryId: number): Observable<CategoryModel[]> {
    return this.getActiveCategories().pipe(
      switchMap(activeCategories => {
        const filteredCategories = activeCategories.filter(cat => cat.id !== categoryId);
        return this.updateActiveCategories(filteredCategories);
      }),
      catchError(this.handleError<CategoryModel[]>('removeCategoryFromActive', []))
    );
  }

  // Get categories that are available but not currently active
  getInactiveCategories(): Observable<CategoryModel[]> {
    return this.getAllAvailableCategories().pipe(
      switchMap(allCategories => {
        return this.getActiveCategories().pipe(
          map(activeCategories => {
            const activeCategoryIds = activeCategories.map(cat => cat.id);
            return allCategories.filter(cat => !activeCategoryIds.includes(cat.id));
          })
        );
      }),
      catchError(this.handleError<CategoryModel[]>('getInactiveCategories', []))
    );
  }

  // Create new category
  createCategory(categoryData: Partial<CategoryModel>): Observable<CategoryModel> {
    return this.http.post<CategoryModel>(this.categoriesUrl, categoryData).pipe(
      catchError(this.handleError<CategoryModel>('createCategory'))
    );
  }

  // Update existing category
  updateCategory(id: number, categoryData: Partial<CategoryModel>): Observable<CategoryModel> {
    return this.http.put<CategoryModel>(`${this.categoriesUrl}/${id}`, categoryData).pipe(
      catchError(this.handleError<CategoryModel>('updateCategory'))
    );
  }

  // Delete category
  deleteCategory(id: string): Observable<any> {
    return this.http.delete(`${this.categoriesUrl}/${id}`).pipe(
      catchError(this.handleError<any>('deleteCategory'))
    );
  }

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.error(`${operation} failed: ${error.message}`);
      return of(result as T);
    };
  }
}

