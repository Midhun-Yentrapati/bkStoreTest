import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { Address } from '../models/address.model';

export interface User {
  // Core user fields (always present)
  id: string;
  username: string;
  email: string;
  fullName: string;
  userRole: string; // CUSTOMER, ADMIN, SUPER_ADMIN, etc.
  userType: string; // CUSTOMER, ADMIN
  accountStatus: string; // ACTIVE, INACTIVE, LOCKED, etc.
  createdAt: string;
  updatedAt?: string;
  
  // Optional personal fields
  mobileNumber?: string;
  dateOfBirth?: string;
  bio?: string;
  profilePictureUrl?: string;
  
  // Admin-specific fields (null for customers)
  employeeId?: string;
  department?: string;
  managerId?: string;
  hireDate?: string;
  salary?: number;
  
  // Account security fields
  isEmailVerified?: boolean;
  isMobileVerified?: boolean;
  isTwoFactorEnabled?: boolean;
  failedLoginAttempts?: number;
  accountLockedUntil?: string;
  lastLoginAt?: string;
  lastLoginIp?: string;
  passwordChangedAt?: string;
  deletedAt?: string;
  
  // Computed/UI fields
  isActive?: boolean; // Computed from accountStatus
}

// Keep legacy interfaces for backward compatibility but extend from User
export interface AdminUser extends User {
  // Legacy compatibility - these should map to User fields
  role?: string; // Maps to userRole
  isAccountActive?: boolean; // Maps to computed isActive
}

export interface CustomerUser extends User {
  // Legacy compatibility
  phoneNumber?: string; // Maps to mobileNumber
  address?: string; // Additional field if needed
  isAccountActive?: boolean; // Maps to computed isActive
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = 'http://localhost:8090/api/users/admin'; // Via API Gateway

  constructor(private http: HttpClient) {}

  // Admin User Management
  getAllAdmins(): Observable<AdminUser[]> {
    return this.http.get<any>(`${this.apiUrl}/admins`).pipe(
      map((response: any) => {
        console.log('Raw admin response:', response);
        // Handle both array and paginated response formats
        if (Array.isArray(response)) {
          return response;
        } else if (response && Array.isArray(response.content)) {
          return response.content;
        } else if (response && Array.isArray(response.data)) {
          return response.data;
        } else {
          console.warn('Unexpected admin response structure:', response);
          return [];
        }
      }),
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
    return this.http.get<any>(`${this.apiUrl}/customers`).pipe(
      map((response: any) => {
        console.log('Raw customer response:', response);
        // Handle both array and paginated response formats
        if (Array.isArray(response)) {
          return response;
        } else if (response && Array.isArray(response.content)) {
          return response.content;
        } else if (response && Array.isArray(response.data)) {
          return response.data;
        } else {
          console.warn('Unexpected customer response structure:', response);
          return [];
        }
      }),
      catchError(this.handleError<CustomerUser[]>('getAllCustomers', []))
    );
  }

  getCustomerById(id: string): Observable<CustomerUser> {
    return this.http.get<CustomerUser>(`${this.apiUrl}/customers/${id}`).pipe(
      catchError(this.handleError<CustomerUser>('getCustomerById'))
    );
  }

  // Address Management (Admin)
  getAddressById(id: string): Observable<Address> {
    // This endpoint is for admins to fetch any address by ID
    return this.http.get<Address>(`http://localhost:8090/api/users/admin/addresses/${id}`).pipe(
      catchError(this.handleError<Address>('getAddressById'))
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

  // Combined User Management Methods
  /**
   * Get all users (both admin and customer) - replaces direct HTTP call to /users/admin/all
   */
  getAllUsers(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/all`).pipe(
      catchError(this.handleError<any>('getAllUsers', { adminUsers: [], customerUsers: [] }))
    );
  }

  /**
   * Delete any user (admin or customer) by ID - replaces direct HTTP DELETE /users/{id}
   */
  deleteUser(userId: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${userId}`).pipe(
      catchError(this.handleError<any>('deleteUser'))
    );
  }

  /**
   * Update user status (activate/deactivate) - uses correct backend endpoints /activate and /deactivate
   */
  updateUserStatus(userId: string, isActive: boolean): Observable<any> {
    const endpoint = isActive ? 'activate' : 'deactivate';
    return this.http.put(`${this.apiUrl}/${userId}/${endpoint}`, {}).pipe(
      catchError(this.handleError<any>('updateUserStatus'))
    );
  }

  /**
   * Reset user password - replaces direct HTTP POST /users/{id}/reset-password
   */
  resetUserPassword(userId: string): Observable<any> {
    return this.http.post(`http://localhost:8090/api/users/${userId}/reset-password`, {}).pipe(
      catchError(this.handleError<any>('resetUserPassword'))
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