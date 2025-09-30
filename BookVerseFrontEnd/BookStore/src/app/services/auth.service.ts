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
  userRole?: string; // Added to match backend response
  fullName?: string; // Added for consistency
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  // Point to API Gateway for proper microservices routing
  private apiBaseUrl = 'http://localhost:8090/api'; // API Gateway URL
  private authUrl = `${this.apiBaseUrl}/auth`;
  private usersUrl = `${this.apiBaseUrl}/users`;
  
  private http = inject(HttpClient);
  private router = inject(Router);
  
  // Utility method to transform backend user data (snake_case) to frontend format (camelCase)
  private transformUserData(backendResponse: any): UserModel {
    if (!backendResponse) return backendResponse;
    
    // Debug: Log backend response to see actual structure
    console.log('🔍 Backend response received:', backendResponse);
    
    // Handle nested response structure - extract user data from response.user
    const backendUser = backendResponse.user || backendResponse;
    
    console.log('🔍 Extracted user data:', backendUser);
    
    // Debug: Check all possible DOB field names
    console.log('🔍 DOB field analysis:', {
      date_of_birth: backendUser.date_of_birth,
      dateOfBirth: backendUser.dateOfBirth,
      dob: backendUser.dob,
      birth_date: backendUser.birth_date,
      birthDate: backendUser.birthDate
    });
    
    // Debug: Check all possible bio field names
    console.log('🔍 Bio field analysis:', {
      bio: backendUser.bio,
      description: backendUser.description,
      about: backendUser.about,
      profile_description: backendUser.profile_description
    });
    
    const transformed = {
      id: backendUser.id,
      fullName: backendUser.full_name || backendUser.fullName || backendUser.name,
      username: backendUser.username || backendUser.user_name,
      email: backendUser.email,
      mobileNumber: backendUser.mobile_number || backendUser.mobileNumber || backendUser.phone,
      profilePicture: backendUser.profile_picture_url || backendUser.profilePicture || backendUser.profile_picture,
      dateOfBirth: backendUser.date_of_birth || backendUser.dateOfBirth || backendUser.dob || backendUser.birth_date || backendUser.birthDate || null,
      bio: backendUser.bio !== undefined ? backendUser.bio : (backendUser.description || backendUser.about || backendUser.profile_description || ''),
      createdAt: backendUser.created_at || backendUser.createdAt || backendUser.created,
      lastUpdated: backendUser.updated_at || backendUser.lastUpdated || backendUser.updated,
      userRole: backendUser.user_role || backendUser.userRole || backendUser.role,
      userType: backendUser.user_type || backendUser.userType || backendUser.type,
      isActive: backendUser.accountStatus === 'ACTIVE' || backendUser.isActive || true // Default to true if undefined
    };
    
    // Debug: Log transformed data to see what we're setting
    console.log('✅ Transformed user data:', transformed);
    
    return transformed;
  }
  
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
          const rawCustomer = JSON.parse(storedCustomer);
          console.log('🔄 Raw data from localStorage:', rawCustomer);
          
          // Transform backend format to frontend format
          const customer = this.transformUserData(rawCustomer);
          this.currentCustomer.set(customer);
          console.log('🔄 Customer auth state restored from localStorage:', customer);
          console.log('🔄 DOB and Bio from localStorage:', {
            dateOfBirth: customer.dateOfBirth,
            bio: customer.bio
          });
        } catch (error) {
          console.error('❌ Failed to parse stored customer data:', error);
          localStorage.removeItem('bookverse_customer');
        }
      }
      
      // Initialize admin user state
      const storedAdmin = localStorage.getItem('bookverse_admin');
      if (storedAdmin) {
        try {
          const admin = JSON.parse(storedAdmin);
          console.log('🔄 Raw admin data from localStorage:', admin);
          this.currentAdmin.set(admin);
          console.log('🔄 Admin auth state restored from localStorage:', admin.username);
        } catch (error) {
          console.error('❌ Failed to parse stored admin data:', error);
          localStorage.removeItem('bookverse_admin');
        }
      }
      
      this._isInitialized.set(true);
    }
  }

  // Unified login method - backend handles role detection automatically
  login(credentialsOrEmail: any | string, password?: string): Observable<UserModel | AdminUser> {
    let loginRequest: any;
    
    // Handle different parameter formats
    if (typeof credentialsOrEmail === 'string' && password) {
      // Admin login format (email, password)
      loginRequest = {
        usernameOrEmail: credentialsOrEmail,  // Fixed: use usernameOrEmail instead of identifier
        password: password
      };
    } else {
      // Customer login format (credentials object)
      const credentials = credentialsOrEmail;
      loginRequest = {
        usernameOrEmail: credentials.identifier,  // Fixed: map identifier to usernameOrEmail
        password: credentials.password
      };
    }
    
    console.log('Unified login attempt for:', loginRequest.usernameOrEmail);
    
    // Use single login endpoint - backend determines user type
    return this.http.post<any>(`${this.authUrl}/login`, loginRequest).pipe(
      tap(response => {
        console.log('Login response received:', response);
        
        // Check if login was successful
        if (response && response.success) {
          console.log('Login successful, processing response...');
          
          // Store JWT tokens
          if (typeof localStorage !== 'undefined') {
            if (response.accessToken) {
              localStorage.setItem('bookverse_token', response.accessToken);
              console.log('Access token stored');
            }
            if (response.refreshToken) {
              localStorage.setItem('bookverse_refresh_token', response.refreshToken);
              console.log('Refresh token stored');
            }
          }
          
          // Handle user based on role from backend response
          if (response.user) {
            const user = response.user;
            console.log('User data received:', user);
            
            // Check if user is admin based on role
            if (user.userRole && user.userRole !== 'CUSTOMER') {
              // Admin user
              this.currentAdmin.set(user);
              this.currentCustomer.set(null);
              if (typeof localStorage !== 'undefined') {
                localStorage.setItem('bookverse_admin', JSON.stringify(user));
                localStorage.removeItem('bookverse_customer');
              }
              console.log('Admin login successful for:', user.username || user.fullName);
            } else {
              // Customer user - transform data before storing
              const transformedUser = this.transformUserData(user);
              this.currentCustomer.set(transformedUser);
              this.currentAdmin.set(null);
              if (typeof localStorage !== 'undefined') {
                localStorage.setItem('bookverse_customer', JSON.stringify(transformedUser));
                localStorage.removeItem('bookverse_admin');
              }
              console.log('Customer login successful for:', transformedUser.fullName || transformedUser.username);
            }
          } else {
            console.warn('No user data in response');
          }
        } else {
          console.error('Login response indicates failure:', response);
          throw new Error(response?.message || 'Login failed');
        }
      }),
      map(response => {
        // Return the user data for the component
        if (response && response.success && response.user) {
          return response.user;
        }
        throw new Error('Invalid login response');
      }),
      catchError(err => {
        console.error('Login error details:', err);
        let errorMessage = 'Login failed';
        
        // Handle backend error structure
        if (err.error) {
          if (err.error.message) {
            errorMessage = err.error.message;
          } else if (err.error.errorDetails) {
            errorMessage = err.error.errorDetails;
          }
        } else if (err.message) {
          errorMessage = err.message;
        }
        
        console.error('Final error message:', errorMessage);
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  register(userData: any): Observable<UserModel>;
  register(userData: {
    fullName: string;
    username: string;
    email: string;
    mobileNumber: string;
    password: string;
  }): Observable<UserModel>;
  register(userData: any | {
    fullName: string;
    username: string;
    email: string;
    mobileNumber: string;
    password: string;
  }): Observable<UserModel> {
    console.log('Registering new user:', userData);
    
    // Use the customerRegister method which calls the backend registration endpoint
    return this.customerRegister(userData);
  }

  // Customer registration method
  customerRegister(userData: {
    fullName: string;
    username: string;
    email: string;
    mobileNumber: string;
    password: string;
  }): Observable<UserModel> {
    console.log('Customer registration attempt for:', userData.username, userData.email);
    
    // Create registration request payload matching backend requirements
    const registrationData = {
      fullName: userData.fullName,
      username: userData.username,
      email: userData.email,
      mobileNumber: userData.mobileNumber,
      password: userData.password,
      confirmPassword: userData.password // Backend requires confirmPassword field
    };
    
    // Make HTTP POST request to backend registration endpoint
    return this.http.post<any>(`${this.authUrl}/register`, registrationData).pipe(
      map(response => {
        console.log('Customer registration response:', response);
        
        // Check if registration was successful
        if (response && response.success) {
          console.log('Registration successful, processing response...');
          
          // Store JWT tokens if provided
          if (typeof localStorage !== 'undefined') {
            if (response.accessToken) {
              localStorage.setItem('bookverse_token', response.accessToken);
              console.log('Access token stored after registration');
            }
            if (response.refreshToken) {
              localStorage.setItem('bookverse_refresh_token', response.refreshToken);
              console.log('Refresh token stored after registration');
            }
          }
          
          // Set current user if provided
          if (response.user) {
            this.currentCustomer.set(response.user);
            if (typeof localStorage !== 'undefined') {
              localStorage.setItem('bookverse_customer', JSON.stringify(response.user));
            }
            console.log('User data stored after registration:', response.user.fullName);
            return response.user;
          }
          
          // If no user data in response, throw error
          throw new Error('Registration successful but no user data received');
        } else {
          // Registration failed
          console.error('Registration response indicates failure:', response);
          throw new Error(response?.message || 'Registration failed');
        }
      }),
      catchError(err => {
        console.error('Customer registration error details:', err);
        let errorMessage = 'Registration failed';
        
        // Handle backend error structure
        if (err.error) {
          if (err.error.message) {
            errorMessage = err.error.message;
          } else if (err.error.errorDetails) {
            errorMessage = err.error.errorDetails;
          } else if (err.error.validationErrors) {
            // Handle validation errors from backend
            const validationErrors = Object.values(err.error.validationErrors).join(', ');
            errorMessage = `Validation failed: ${validationErrors}`;
          }
        } else if (err.message) {
          errorMessage = err.message;
        }
        
        console.error('Final registration error message:', errorMessage);
        
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  // Admin registration method
  registerAdmin(adminData: {
    username: string;
    email: string;
    password: string;
    confirmPassword?: string;
    fullName?: string;
    userRole?: string;
    department?: string;
    employeeId?: string;
  }): Observable<any> {
    console.log('Admin registration attempt for:', adminData.username, adminData.email);
    
    // Create admin registration request payload matching backend requirements
    const adminRegistrationData = {
      username: adminData.username,
      email: adminData.email,
      password: adminData.password,
      confirmPassword: adminData.confirmPassword || adminData.password,
      fullName: adminData.fullName || adminData.username,
      userRole: adminData.userRole || 'ADMIN',
      department: adminData.department || 'Administration',
      employeeId: adminData.employeeId || `EMP-${adminData.username.toUpperCase()}`
    };

    return this.http.post<any>(`${this.authUrl}/admin/register`, adminRegistrationData).pipe(
      tap(response => {
        console.log('Admin registration successful:', response);
        
        // If registration includes auto-login, handle the response
        if (response.user && response.accessToken) {
          // Store the admin user and token
          this.currentAdmin.set(response.user);
          if (typeof localStorage !== 'undefined') {
            localStorage.setItem('bookverse_admin', JSON.stringify(response.user));
            localStorage.setItem('bookverse_token', response.accessToken);
            if (response.refreshToken) {
              localStorage.setItem('bookverse_refresh_token', response.refreshToken);
            }
          }
        }
      }),
      catchError(err => {
        console.error('Admin registration error details:', err);
        let errorMessage = 'Admin registration failed';
        
        // Handle backend error structure
        if (err.error) {
          if (err.error.message) {
            errorMessage = err.error.message;
          } else if (err.error.errorDetails) {
            errorMessage = err.error.errorDetails;
          } else if (err.error.validationErrors) {
            // Handle validation errors from backend
            const validationErrors = Object.values(err.error.validationErrors).join(', ');
            errorMessage = `Validation failed: ${validationErrors}`;
          }
        } else if (err.message) {
          errorMessage = err.message;
        }
        
        console.error('Final admin registration error message:', errorMessage);
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  // Test method to check if authentication service is reachable
  testAuthServiceConnection(): Observable<any> {
    console.log('Testing auth service connection to:', `${this.authUrl}/test`);
    return this.http.get(`${this.authUrl}/test`).pipe(
      tap(response => console.log('Auth service connection successful:', response)),
      catchError(error => {
        console.error('Auth service connection failed:', error);
        console.error('Full error details:', {
          status: error.status,
          statusText: error.statusText,
          url: error.url,
          message: error.message
        });
        return throwError(() => error);
      })
    );
  }

  // Test method to check if users service is reachable
  testUsersServiceConnection(): Observable<any> {
    console.log('Testing users service connection to:', `${this.usersUrl}/test`);
    return this.http.get(`${this.usersUrl}/test`).pipe(
      tap(response => console.log('Users service connection successful:', response)),
      catchError(error => {
        console.error('Users service connection failed:', error);
        return throwError(() => error);
      })
    );
  }

  updateProfile(userData: Partial<UserModel>): Observable<UserModel> {
    const currentUser = this.currentCustomer();
    if (!currentUser) {
      return throwError(() => new Error('No user logged in'));
    }

    // Create clean user DTO without timestamps (let backend handle them)
    const userDTO = {
      fullName: userData.fullName,
      username: userData.username,
      email: userData.email,
      mobileNumber: userData.mobileNumber,
      dateOfBirth: userData.dateOfBirth,
      bio: userData.bio,
      profilePicture: userData.profilePicture
    };

    // Use correct backend endpoint for profile update
    return this.http.put<any>(`${this.usersUrl}/profile`, userDTO).pipe(
      map(backendUser => {
        // Transform backend response using shared utility
        return this.transformUserData(backendUser);
      }),
      tap(user => {
        // Update signal and localStorage with transformed data
        this.currentCustomer.set(user);
        if (typeof localStorage !== 'undefined') {
          localStorage.setItem('bookverse_customer', JSON.stringify(user));
        }
        console.log('Profile updated successfully:', user);
      }),
      catchError(err => throwError(() => new Error(err.message || 'Profile update failed')))
    );
  }

  // for username 
  checkUsernameAvailability(username: string): Observable<boolean> {
    return this.http.get<UserModel[]>(this.usersUrl).pipe(
      map(users => !users.some(u => u.username === username)),
      catchError(() => of(true)) // Assume available on error
    );
  }

  // for email
  checkEmailAvailability(email: string): Observable<boolean> {
    return this.http.get<UserModel[]>(this.usersUrl).pipe(
      map(users => !users.some(u => u.email === email)),
      catchError(() => of(true)) // Assume available on error
    );
  }

  // New helper methods for role-based authentication
  isCustomerLoggedIn(): boolean {
    return !!this.currentCustomer();
  }

  isAdminLoggedIn(): boolean {
    return !!this.currentAdmin();
  }

  getCurrentCustomer(): UserModel | null {
    return this.currentCustomer();
  }

  getCurrentAdmin(): AdminUser | null {
    return this.currentAdmin();
  }

  // Role-specific logout methods
  logoutCustomer(): void {
    console.log('Logging out customer user.');
    this.performLogout();
    this.router.navigate(['/']);
  }

  logoutAdmin(): void {
    console.log('Logging out admin user.');
    this.performLogout();
    this.router.navigate(['/admin/login']);
  }

  logout() {
    console.log('Logging out all users.');
    this.performLogout();
    this.router.navigate(['/']);
  }

  // Comprehensive logout that clears all session data
  private performLogout(): void {
    console.log('Performing complete logout...');
    
    // Call backend logout endpoint if token exists
    const token = this.getToken();
    if (token) {
      // Make logout API call to invalidate session on backend
      const headers = {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      };
      
      this.http.post(`${this.authUrl}/logout`, {}, { headers }).subscribe({
        next: (response) => {
          console.log('Backend logout successful:', response);
        },
        error: (error) => {
          console.warn('Backend logout failed (continuing with client logout):', error);
        }
      });
    }
    
    // Clear all authentication data from storage
    if (typeof localStorage !== 'undefined') {
      localStorage.removeItem('bookverse_token');
      localStorage.removeItem('bookverse_refresh_token');
      localStorage.removeItem('bookverse_customer');
      localStorage.removeItem('bookverse_admin');
      localStorage.removeItem('bookverse_user'); // Legacy token name
    }
    
    if (typeof sessionStorage !== 'undefined') {
      sessionStorage.removeItem('loggedInUsername');
      sessionStorage.clear();
    }
    
    // Reset user state
    this.currentCustomer.set(null);
    this.currentAdmin.set(null);
    
    console.log('Logout completed - all session data cleared');
  }

  // Token management methods
  getToken(): string | null {
    if (typeof localStorage !== 'undefined') {
      return localStorage.getItem('bookverse_token');
    }
    return null;
  }

  getRefreshToken(): string | null {
    if (typeof localStorage !== 'undefined') {
      return localStorage.getItem('bookverse_refresh_token');
    }
    return null;
  }

  setTokens(accessToken: string, refreshToken?: string): void {
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem('bookverse_token', accessToken);
      if (refreshToken) {
        localStorage.setItem('bookverse_refresh_token', refreshToken);
      }
    }
  }

  clearTokens(): void {
    if (typeof localStorage !== 'undefined') {
      localStorage.removeItem('bookverse_token');
      localStorage.removeItem('bookverse_refresh_token');
    }
  }

  isTokenValid(): boolean {
    const token = this.getToken();
    if (!token) {
      return false;
    }
    
    try {
      // Basic JWT structure validation
      const parts = token.split('.');
      if (parts.length !== 3) {
        return false;
      }
      
      // Decode payload to check expiration
      const payload = JSON.parse(atob(parts[1]));
      const currentTime = Math.floor(Date.now() / 1000);
      
      return payload.exp && payload.exp > currentTime;
    } catch (error) {
      console.error('Token validation error:', error);
      return false;
    }
  }

  isLoggedIn(): boolean {
    return this.isCustomerLoggedIn() || this.isAdminLoggedIn();
  }

  getCurrentUserDisplayName(): string {
    const customer = this.currentCustomer();
    const admin = this.currentAdmin();
    
    if (customer) {
      return customer.fullName || customer.username;
    }
    if (admin) {
      return admin.username || admin.email;
    }
    return '';
  }

  // Admin-specific signup method (for compatibility with admin components)
  signup(username: string, email: string, password: string): Observable<boolean> {
    console.log('Admin signup attempt for:', username, email);
    
    // Check if username or email already exists
    return this.http.get<any[]>(this.usersUrl).pipe(
      switchMap(users => {
        const existingUser = users.find(u => 
          u.username === username || u.email === email
        );
        
        if (existingUser) {
          if (existingUser.username === username) {
            throw new Error('Username already taken.');
          }
          if (existingUser.email === email) {
            throw new Error('Email already registered.');
          }
        }
        
        const newUserId = crypto.randomUUID();
        const newAdminUser: AdminUser = {
          id: newUserId,
          username,
          email,
          passwordHash: password, // AdminUsers use passwordHash instead of password
          role: 'admin'
        };
        
        return this.http.post<any>(this.usersUrl, newAdminUser);
      }),
      map(newUser => {
        console.log('Admin registration successful for:', newUser.username);
        // Auto-login the admin user after registration
        this.currentAdmin.set(newUser);
        if (typeof localStorage !== 'undefined') {
          localStorage.setItem('bookverse_admin', JSON.stringify(newUser));
        }
        return true;
      }),
      catchError(err => {
        console.error('Admin signup error:', err);
        return throwError(() => new Error(err.message || 'Registration failed'));
      })
    );
  }

  // Password reset functionality
  forgotPassword(email: string): Observable<boolean> {
    console.log('Forgot password request for:', email);
    
    // Check if user exists with this email
    return this.http.get<UserModel[]>(this.usersUrl).pipe(
      map(users => {
        const user = users.find(u => u.email === email);
        if (user) {
          // In a real application, this would send an email to the user
          // For this demo, we'll just log the success and return true
          console.log('Password reset email would be sent to:', email);
          return true;
        } else {
          // For security reasons, we still return true even if user doesn't exist
          // This prevents email enumeration attacks
          console.log('No user found with email:', email, '(returning true for security)');
          return true;
        }
      }),
      catchError(err => {
        console.error('Error in forgot password:', err);
        return throwError(() => new Error('Unable to process password reset request'));
      })
    );
  }

  // Public method to fetch complete user profile (for components)
  fetchCompleteUserProfile(): Observable<UserModel> {
    return this.http.get<any>(`${this.usersUrl}/profile`).pipe(
      map(response => {
        console.log('🔍 Fresh profile data fetched:', response);
        const completeUser = this.transformUserData(response);
        
        // Update current user data
        this.currentCustomer.set(completeUser);
        if (typeof localStorage !== 'undefined') {
          localStorage.setItem('bookverse_customer', JSON.stringify(completeUser));
        }
        
        return completeUser;
      }),
      catchError(err => {
        console.error('❌ Failed to fetch fresh profile data:', err);
        return throwError(() => new Error('Failed to fetch user profile'));
      })
    );
  }

  // Change password method
  changePassword(currentPassword: string, newPassword: string): Observable<any> {
    const currentUser = this.currentCustomer();
    if (!currentUser) {
      return throwError(() => new Error('No user logged in'));
    }

    const changePasswordData = {
      currentPassword: currentPassword,
      newPassword: newPassword
    };

    return this.http.post<any>(`${this.usersUrl}/change-password`, changePasswordData).pipe(
      tap(response => {
        console.log('Password changed successfully:', response);
      }),
      catchError(err => {
        console.error('Password change error:', err);
        let errorMessage = 'Password change failed';
        
        if (err.error) {
          if (err.error.message) {
            errorMessage = err.error.message;
          } else if (err.error.errorDetails) {
            errorMessage = err.error.errorDetails;
          }
        } else if (err.message) {
          errorMessage = err.message;
        }
        
        return throwError(() => new Error(errorMessage));
      })
    );
  }
}
