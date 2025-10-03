import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

export interface AdminUser {
  id: string;
  username: string;
  email: string;
  fullName: string;
  role: string;
  department: string;
  isActive: boolean;
  createdAt: string;
  lastLoginAt?: string;
}

export interface CustomerUser {
  id: string;
  username: string;
  email: string;
  fullName: string;
  phoneNumber?: string;
  address?: string;
  isActive: boolean;
  createdAt: string;
  lastLoginAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = 'http://localhost:8090/api/users/admin'; // Via API Gateway

  constructor(private http: HttpClient) {}

  // Admin User Management
  getAllAdmins(): Observable<AdminUser[]> {
    return this.http.get<AdminUser[]>(`${this.apiUrl}/admins`).pipe(
      catchError(this.handleError<AdminUser[]>('getAllAdmins', []))
    );
  }

  getAdminById(id: string): Observable<AdminUser> {
    return this.http.get<AdminUser>(`${this.apiUrl}/admins/${id}`).pipe(
      catchError(this.handleError<AdminUser>('getAdminById'))
    );
  }

  createAdmin(admin: Omit<AdminUser, 'id' | 'createdAt'>): Observable<AdminUser> {
    return this.http.post<AdminUser>(`${this.apiUrl}/admins`, admin).pipe(
      catchError(this.handleError<AdminUser>('createAdmin'))
    );
  }

  updateAdmin(id: string, admin: Partial<AdminUser>): Observable<AdminUser> {
    return this.http.put<AdminUser>(`${this.apiUrl}/admins/${id}`, admin).pipe(
      catchError(this.handleError<AdminUser>('updateAdmin'))
    );
  }

  deleteAdmin(id: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/admins/${id}`).pipe(
      catchError(this.handleError<any>('deleteAdmin'))
    );
  }

  // Customer User Management
  getAllCustomers(): Observable<CustomerUser[]> {
    return this.http.get<CustomerUser[]>(`${this.apiUrl}/customers`).pipe(
      catchError(this.handleError<CustomerUser[]>('getAllCustomers', []))
    );
  }

  getCustomerById(id: string): Observable<CustomerUser> {
    return this.http.get<CustomerUser>(`${this.apiUrl}/customers/${id}`).pipe(
      catchError(this.handleError<CustomerUser>('getCustomerById'))
    );
  }

  updateCustomer(id: string, customer: Partial<CustomerUser>): Observable<CustomerUser> {
    return this.http.put<CustomerUser>(`${this.apiUrl}/customers/${id}`, customer).pipe(
      catchError(this.handleError<CustomerUser>('updateCustomer'))
    );
  }

  deactivateCustomer(id: string): Observable<any> {
    return this.http.patch(`${this.apiUrl}/customers/${id}/deactivate`, {}).pipe(
      catchError(this.handleError<any>('deactivateCustomer'))
    );
  }

  activateCustomer(id: string): Observable<any> {
    return this.http.patch(`${this.apiUrl}/customers/${id}/activate`, {}).pipe(
      catchError(this.handleError<any>('activateCustomer'))
    );
  }

  // User Statistics
  getUserStatistics(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/statistics`).pipe(
      catchError(this.handleError<any>('getUserStatistics', {}))
    );
  }

  // Search Users
  searchUsers(query: string, userType: 'admin' | 'customer' | 'all' = 'all'): Observable<any[]> {
    const params = new HttpParams()
      .set('query', query)
      .set('userType', userType);

    return this.http.get<any[]>(`${this.apiUrl}/search`, { params }).pipe(
      catchError(this.handleError<any[]>('searchUsers', []))
    );
  }

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.error(`${operation} failed:`, error);
      return of(result as T);
    };
  }
}