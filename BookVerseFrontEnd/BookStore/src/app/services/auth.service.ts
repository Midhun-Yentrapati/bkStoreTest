import { Injectable, signal, inject, afterNextRender } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { UserModel } from '../models/user.model';
import { Observable, of, throwError } from 'rxjs';
import { tap, map, catchError, switchMap } from 'rxjs/operators';

// Interface for admin users
interface AdminUser {
  id: string;
  username: string;
  email: string;
  passwordHash: string;
  role: 'admin';
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiBaseUrl = 'http://localhost:8090/api';
  private authUrl = `${this.apiBaseUrl}/auth`;
  private usersUrl = `${this.apiBaseUrl}/users`;
  private adminUsersUrl = `${this.apiBaseUrl}/admin`; 

  private http = inject(HttpClient);
  private router = inject(Router);
  
  // Separate signals for customer and admin users
  currentCustomer = signal<UserModel | null>(null);
  currentAdmin = signal<AdminUser | null>(null);
  
  private _isInitialized = signal(false);
  isInitialized = this._isInitialized.asReadonly();

  constructor() {
    afterNextRender(() => {
      this.initializeAuthState();
    });
    
    // Fallback for non-SSR environments
    if (typeof localStorage !== 'undefined') {
      this.initializeAuthState();
    }
  }

  private initializeAuthState() {
    if (typeof localStorage !== 'undefined' && !this._isInitialized()) {
      // Initialize customer user state
      const storedCustomer = localStorage.getItem('bookverse_customer');
      if (storedCustomer) {
        try {
          const customerData = JSON.parse(storedCustomer);
          this.currentCustomer.set(customerData);
        } catch (error) {
          console.error('Error parsing stored customer data:', error);
          localStorage.removeItem('bookverse_customer');
        }
      }

      // Initialize admin user state
      const storedAdmin = localStorage.getItem('bookverse_admin');
      if (storedAdmin) {
        try {
          const adminData = JSON.parse(storedAdmin);
          this.currentAdmin.set(adminData);
        } catch (error) {
          console.error('Error parsing stored admin data:', error);
          localStorage.removeItem('bookverse_admin');
        }
      }

      this._isInitialized.set(true);
    }
  }

  // Customer authentication methods
  loginCustomer(email: string, password: string): Observable<UserModel> {
    return this.http.post<any>(`${this.authUrl}/login`, { email, password })
      .pipe(
        tap(response => {
          console.log('Login response:', response);
          
          // Store token
          if (response.token) {
            localStorage.setItem('bookverse_token', response.token);
          }
          
          // Store user data
          if (response.user) {
            this.currentCustomer.set(response.user);
            localStorage.setItem('bookverse_customer', JSON.stringify(response.user));
          }
        }),
        map(response => response.user),
        catchError(error => {
          console.error('Login error:', error);
          return throwError(() => error);
        })
      );
  }

  registerCustomer(userData: any): Observable<UserModel> {
    return this.http.post<any>(`${this.authUrl}/register`, userData)
      .pipe(
        tap(response => {
          console.log('Registration response:', response);
          
          // Store token
          if (response.token) {
            localStorage.setItem('bookverse_token', response.token);
          }
          
          // Store user data
          if (response.user) {
            this.currentCustomer.set(response.user);
            localStorage.setItem('bookverse_customer', JSON.stringify(response.user));
          }
        }),
        map(response => response.user),
        catchError(error => {
          console.error('Registration error:', error);
          return throwError(() => error);
        })
      );
  }

  // Admin authentication methods
  loginAdmin(username: string, password: string): Observable<AdminUser> {
    return this.http.post<any>(`${this.authUrl}/admin/login`, { username, password })
      .pipe(
        tap(response => {
          console.log('Admin login response:', response);
          
          // Store token
          if (response.token) {
            localStorage.setItem('bookverse_token', response.token);
          }
          
          // Store admin data
          if (response.admin) {
            this.currentAdmin.set(response.admin);
            localStorage.setItem('bookverse_admin', JSON.stringify(response.admin));
          }
        }),
        map(response => response.admin),
        catchError(error => {
          console.error('Admin login error:', error);
          return throwError(() => error);
        })
      );
  }

  registerAdmin(adminData: any): Observable<AdminUser> {
    return this.http.post<any>(`${this.authUrl}/admin/register`, adminData)
      .pipe(
        tap(response => {
          console.log('Admin registration response:', response);
          
          // Store token
          if (response.token) {
            localStorage.setItem('bookverse_token', response.token);
          }
          
          // Store admin data
          if (response.admin) {
            this.currentAdmin.set(response.admin);
            localStorage.setItem('bookverse_admin', JSON.stringify(response.admin));
          }
        }),
        map(response => response.admin),
        catchError(error => {
          console.error('Admin registration error:', error);
          return throwError(() => error);
        })
      );
  }

  // Logout methods
  logoutCustomer(): void {
    this.currentCustomer.set(null);
    localStorage.removeItem('bookverse_customer');
    localStorage.removeItem('bookverse_token');
    this.router.navigate(['/login']);
  }

  logoutAdmin(): void {
    this.currentAdmin.set(null);
    localStorage.removeItem('bookverse_admin');
    localStorage.removeItem('bookverse_token');
    this.router.navigate(['/admin/login']);
  }

  // Universal logout
  logout(): void {
    this.currentCustomer.set(null);
    this.currentAdmin.set(null);
    localStorage.removeItem('bookverse_customer');
    localStorage.removeItem('bookverse_admin');
    localStorage.removeItem('bookverse_token');
    this.router.navigate(['/']);
  }

  // Check if user is authenticated
  isAuthenticated(): boolean {
    return !!(this.currentCustomer() || this.currentAdmin());
  }

  // Check if user is admin
  isAdmin(): boolean {
    return !!this.currentAdmin();
  }

  // Check if user is customer
  isCustomer(): boolean {
    return !!this.currentCustomer();
  }

  // Get current user (customer or admin)
  getCurrentUser(): UserModel | AdminUser | null {
    return this.currentCustomer() || this.currentAdmin();
  }

  // Get current customer
  getCurrentCustomer(): UserModel | null {
    return this.currentCustomer();
  }

  // Get current admin
  getCurrentAdmin(): AdminUser | null {
    return this.currentAdmin();
  }

  // Check username availability
  checkUsernameAvailability(username: string): Observable<boolean> {
    return this.http.get<{available: boolean}>(`${this.authUrl}/check-username/${username}`)
      .pipe(
        map(response => response.available),
        catchError(error => {
          console.error('Username check error:', error);
          return of(false);
        })
      );
  }

  // Check email availability
  checkEmailAvailability(email: string): Observable<boolean> {
    return this.http.get<{available: boolean}>(`${this.authUrl}/check-email/${email}`)
      .pipe(
        map(response => response.available),
        catchError(error => {
          console.error('Email check error:', error);
          return of(false);
        })
      );
  }

  // Forgot password
  forgotPassword(email: string): Observable<any> {
    return this.http.post(`${this.authUrl}/forgot-password`, { email })
      .pipe(
        catchError(error => {
          console.error('Forgot password error:', error);
          return throwError(() => error);
        })
      );
  }

  // Reset password
  resetPassword(token: string, newPassword: string): Observable<any> {
    return this.http.post(`${this.authUrl}/reset-password`, { token, newPassword })
      .pipe(
        catchError(error => {
          console.error('Reset password error:', error);
          return throwError(() => error);
        })
      );
  }
}
