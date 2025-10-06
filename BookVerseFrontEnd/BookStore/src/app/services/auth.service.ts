import { Injectable, signal, inject, afterNextRender, PLATFORM_ID } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { UserModel } from '../models/user.model';
import { Observable, of, throwError } from 'rxjs';
import { tap, map, catchError, switchMap } from 'rxjs/operators';
import { isPlatformBrowser } from '@angular/common'; // Import isPlatformBrowser

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
  
  // Angular Platform Injectables
  private platformId = inject(PLATFORM_ID);
  
  // Separate signals for customer and admin users
  currentCustomer = signal<UserModel | null>(null);
  currentAdmin = signal<AdminUser | null>(null);
  
  private _isInitialized = signal(false);
  isInitialized = this._isInitialized.asReadonly();

  constructor() {
    // Only attempt to initialize state after the first browser render (when hydration completes)
    afterNextRender(() => {
      setTimeout(() => this.initializeAuthState(), 0);
    });
    
    // Fallback for non-SSR environments with delay to ensure DOM is ready
    if (isPlatformBrowser(this.platformId) && !this._isInitialized()) {
      setTimeout(() => {
        if (!this._isInitialized()) {
          this.initializeAuthState();
        }
      }, 100);
    }
  }
  
  // FIX: Helper function for safe Base64Url decoding (required for JWT payloads)
  private urlBase64Decode(str: string): string {
    let output = str.replace(/-/g, '+').replace(/_/g, '/');
    switch (output.length % 4) {
        case 0: break;
        case 2: output += '=='; break;
        case 3: output += '='; break;
        default: throw new Error('Illegal base64url string!');
    }
    // Safely decode Base64 and handle Unicode characters
    return decodeURIComponent(atob(output).split('').map(function(c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
  }
  
  // Utility method to transform backend user data (snake_case) to frontend format (camelCase)
  private transformUserData(backendResponse: any): UserModel {
    if (!backendResponse) return backendResponse;
    
    // Handle nested response structure - extract user data from response.user
    const backendUser = backendResponse.user || backendResponse;
    
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
    
    return transformed;
  }
  
  // FIX: Helper method to clear all necessary storage keys safely
  private clearAllStorage(): void {
    if (isPlatformBrowser(this.platformId)) {
        localStorage.removeItem('bookverse_token');
        localStorage.removeItem('bookverse_refresh_token');
        localStorage.removeItem('bookverse_customer');
        localStorage.removeItem('bookverse_admin');
        localStorage.removeItem('bookverse_user'); // Legacy token name
    }
    // FIX: Clear specific sessionStorage keys used for temporary state
    if (isPlatformBrowser(this.platformId) && typeof sessionStorage !== 'undefined') {
        sessionStorage.removeItem('loggedInUsername');
        sessionStorage.removeItem('sessionId');
    }
    this.currentCustomer.set(null);
    this.currentAdmin.set(null);
  }

  private initializeAuthState() {
    if (!this._isInitialized()) {
      console.log('🚀 Initializing auth state...');
      
      // ENSURE: Only proceed if in browser environment
      if (!isPlatformBrowser(this.platformId)) {
        console.log('⚠️ Not in browser environment, skipping auth initialization');
        this._isInitialized.set(true);
        return;
      }
      
      // FIX: Restore user state first, then validate tokens
      const storedCustomer = localStorage.getItem('bookverse_customer');
      const storedAdmin = localStorage.getItem('bookverse_admin');
      let hasValidUserState = false;
      
      // Restore customer user state
      if (storedCustomer) {
        try {
          const rawCustomer = JSON.parse(storedCustomer);
          const customer = this.transformUserData(rawCustomer);
          this.currentCustomer.set(customer);
          hasValidUserState = true;
          console.log('🔄 Customer auth state restored from localStorage:', customer.username);
        } catch (error) {
          console.error('❌ Failed to parse stored customer data. Removing invalid data.', error);
          localStorage.removeItem('bookverse_customer');
        }
      }
      
      // Restore admin user state
      if (storedAdmin) {
        try {
          const admin = JSON.parse(storedAdmin);
          this.currentAdmin.set(admin);
          hasValidUserState = true;
          console.log('🔄 Admin auth state restored from localStorage:', admin.username);
        } catch (error) {
          console.error('❌ Failed to parse stored admin data. Removing invalid data.', error);
          localStorage.removeItem('bookverse_admin');
        }
      }
      
      // Don't clear tokens on validation failure - just log why validation fails
      if (hasValidUserState) {
        console.log('✅ User state restored successfully.');
        const currentToken = this.getToken();
        if (!currentToken) {
          console.warn('⚠️ User state exists but no token found. Checking if token was cleared.');
          // Check if token exists in raw localStorage
          const rawToken = isPlatformBrowser(this.platformId) ? localStorage.getItem('bookverse_token') : null;
          console.log(`Raw token in localStorage: ${rawToken ? 'EXISTS' : 'MISSING'}`);
        } else {
          console.log(`🔍 Token found: ${currentToken.substring(0, 20)}...`);
          const isValid = this.isTokenValid();
          console.log(`🔍 Token validation result: ${isValid}`);
        }
      }
      
      this._isInitialized.set(true);
      console.log('✅ Auth state initialization completed');
      
      // Log final state for debugging
      const finalToken = this.getToken();
      const finalCustomer = this.currentCustomer();
      const finalAdmin = this.currentAdmin();
      console.log('📊 Final auth state:', {
        hasToken: !!finalToken,
        hasCustomer: !!finalCustomer,
        hasAdmin: !!finalAdmin,
        isLoggedIn: this.isLoggedIn()
      });
    }
  }

  // Unified login method - backend handles role detection automatically
  login(credentialsOrEmail: any | string, password?: string): Observable<UserModel | AdminUser> {
    let loginRequest: any;
    
    // Handle different parameter formats
    if (typeof credentialsOrEmail === 'string' && password) {
      // Admin login format (email, password)
      loginRequest = {
        usernameOrEmail: credentialsOrEmail,
        password: password
      };
    } else {
      // Customer login format (credentials object)
      const credentials = credentialsOrEmail;
      loginRequest = {
        usernameOrEmail: credentials.identifier,
        password: credentials.password
      };
    }
    
    console.log('Unified login attempt for:', loginRequest.usernameOrEmail);
    
    return this.http.post<any>(`${this.authUrl}/login`, loginRequest).pipe(
      tap(response => {
        console.log('Login response received:', response);
        
        // Handle successful response - check for token OR success message
        if (response && (response.token || response.accessToken || response.message === 'Authentication successful')) {
          console.log('Login successful, processing response...');
          
          // Store token if present (check multiple possible token field names)
          const token = response.token || response.accessToken;
          if (token && isPlatformBrowser(this.platformId)) {
            localStorage.setItem('bookverse_token', token);
            console.log('✅ Access token stored:', token.substring(0, 20) + '...');
            
            const refreshToken = response.refreshToken || response.refresh_token;
            if (refreshToken) {
              localStorage.setItem('bookverse_refresh_token', refreshToken);
              console.log('✅ Refresh token stored');
            }
            
            // Verify token was stored correctly
            const verifyToken = localStorage.getItem('bookverse_token');
            console.log('🔍 Token verification after storage:', verifyToken ? 'SUCCESS' : 'FAILED');
          } else if (!token) {
            console.warn('⚠️ No token found in response, but login was successful');
            console.log('Response structure:', Object.keys(response));
          }
          
          // Handle user data if present
          if (response.user) {
            const user = response.user;
            if (user.userRole && user.userRole !== 'CUSTOMER') {
              this.currentAdmin.set(user);
              this.currentCustomer.set(null);
              if (isPlatformBrowser(this.platformId)) {
                localStorage.setItem('bookverse_admin', JSON.stringify(user));
                localStorage.removeItem('bookverse_customer');
              }
              console.log('Admin login successful for:', user.username || user.fullName);
            } else {
              const transformedUser = this.transformUserData(user);
              this.currentCustomer.set(transformedUser);
              this.currentAdmin.set(null);
              if (isPlatformBrowser(this.platformId)) {
                localStorage.setItem('bookverse_customer', JSON.stringify(transformedUser));
                localStorage.removeItem('bookverse_admin');
              }
              console.log('Customer login successful for:', transformedUser.fullName || transformedUser.username);
            }
          }
        } else if (response && response.message && response.message.includes('failed')) {
          // Only throw error if response explicitly indicates failure
          console.error('Login failed:', response.message);
          throw new Error(response.message);
        } else {
          console.error('Login response indicates failure or is malformed:', response);
          throw new Error(response?.message || 'Login failed due to invalid response');
        }
      }),
      map(response => {
        if (response && response.user) {
          return response.user;
        }
        // If no user object but authentication was successful, return a basic user object
        if (response && response.message === 'Authentication successful') {
          return { message: 'Authentication successful' };
        }
        throw new Error('Invalid login response');
      }),
      catchError(err => {
        let errorMessage = 'Login failed';
        
        if (err.error) {
          if (err.error.message) {
            errorMessage = err.error.message;
          } else if (err.error.errorDetails) {
            errorMessage = err.error.errorDetails;
          }
        } else if (err.message) {
          errorMessage = err.message;
        }
        
        console.error('Login error details:', errorMessage);
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  register(userData: any): Observable<UserModel> {
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
    
    const registrationData = {
      fullName: userData.fullName,
      username: userData.username,
      email: userData.email,
      mobileNumber: userData.mobileNumber,
      password: userData.password,
      confirmPassword: userData.password
    };
    
    return this.http.post<any>(`${this.authUrl}/register`, registrationData).pipe(
      map(response => {
        // --- START: MODIFIED CODE ---
        // Correctly handle the backend response with "token" and "user"
        if (response && response.token && response.user) {
          
          if (isPlatformBrowser(this.platformId)) {
            localStorage.setItem('bookverse_token', response.token);
            if (response.refreshToken) {
              localStorage.setItem('bookverse_refresh_token', response.refreshToken);
            }
          }
          
          const transformedUser = this.transformUserData(response.user);
          this.currentCustomer.set(transformedUser);
          if (isPlatformBrowser(this.platformId)) {
            localStorage.setItem('bookverse_customer', JSON.stringify(transformedUser));
          }
          return transformedUser;

        } else {
          throw new Error(response?.message || 'Registration failed');
        }
        // --- END: MODIFIED CODE ---
      }),
      catchError(err => {
        let errorMessage = 'Registration failed';
        
        if (err.error) {
          if (err.error.message) {
            errorMessage = err.error.message;
          } else if (err.error.errorDetails) {
            errorMessage = err.error.errorDetails;
          } else if (err.error.validationErrors) {
            const validationErrors = Object.values(err.error.validationErrors).join(', ');
            errorMessage = `Validation failed: ${validationErrors}`;
          }
        } else if (err.message) {
          errorMessage = err.message;
        }
        
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
        
        if (response.user && response.accessToken) {
          this.currentAdmin.set(response.user);
          if (isPlatformBrowser(this.platformId)) {
            localStorage.setItem('bookverse_admin', JSON.stringify(response.user));
            localStorage.setItem('bookverse_token', response.accessToken);
            if (response.refreshToken) {
              localStorage.setItem('bookverse_refresh_token', response.refreshToken);
            }
          }
        }
      }),
      catchError(err => {
        let errorMessage = 'Admin registration failed';
        
        if (err.error) {
          if (err.error.message) {
            errorMessage = err.error.message;
          } else if (err.error.errorDetails) {
            errorMessage = err.error.errorDetails;
          } else if (err.error.validationErrors) {
            const validationErrors = Object.values(err.error.validationErrors).join(', ');
            errorMessage = `Validation failed: ${validationErrors}`;
          }
        } else if (err.message) {
          errorMessage = err.message;
        }
        
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  // Test method to check if authentication service is reachable
  testAuthServiceConnection(): Observable<any> {
    return this.http.get(`${this.authUrl}/test`).pipe(
      catchError(error => {
        return throwError(() => error);
      })
    );
  }

  // Test method to check if users service is reachable
  testUsersServiceConnection(): Observable<any> {
    return this.http.get(`${this.usersUrl}/test`).pipe(
      catchError(error => {
        return throwError(() => error);
      })
    );
  }

  updateProfile(userData: Partial<UserModel>): Observable<UserModel> {
    const currentUser = this.currentCustomer();
    if (!currentUser) {
      return throwError(() => new Error('No user logged in'));
    }

    const userDTO = {
      fullName: userData.fullName,
      username: userData.username,
      email: userData.email,
      mobileNumber: userData.mobileNumber,
      dateOfBirth: userData.dateOfBirth,
      bio: userData.bio,
      profilePicture: userData.profilePicture
    };

    return this.http.put<any>(`${this.usersUrl}/profile`, userDTO).pipe(
      map(backendUser => this.transformUserData(backendUser)),
      tap(user => {
        this.currentCustomer.set(user);
        if (isPlatformBrowser(this.platformId)) {
          localStorage.setItem('bookverse_customer', JSON.stringify(user));
        }
      }),
      catchError(err => throwError(() => new Error(err.message || 'Profile update failed')))
    );
  }

  checkUsernameAvailability(username: string): Observable<boolean> {
    return this.http.get<UserModel[]>(this.usersUrl).pipe(
      map(users => !users.some(u => u.username === username)),
      catchError(() => of(true))
    );
  }

  checkEmailAvailability(email: string): Observable<boolean> {
    return this.http.get<UserModel[]>(this.usersUrl).pipe(
      map(users => !users.some(u => u.email === email)),
      catchError(() => of(true))
    );
  }

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

  private performLogout(): void {
    console.log('Performing complete logout...');
    
    const token = this.getToken();
    if (token) {
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
    
    this.clearAllStorage();
    
    console.log('Logout completed - all session data cleared');
  }

  getToken(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      const token = localStorage.getItem('bookverse_token');
      if (token) {
        console.log(`🔍 Retrieved token: ${token.substring(0, 20)}...`);
      } else {
        console.log('🔍 No token found in localStorage');
      }
      return token;
    }
    return null;
  }

  getRefreshToken(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem('bookverse_refresh_token');
    }
    return null;
  }

  setTokens(accessToken: string, refreshToken?: string): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem('bookverse_token', accessToken);
      console.log('✅ Token manually set:', accessToken.substring(0, 20) + '...');
      if (refreshToken) {
        localStorage.setItem('bookverse_refresh_token', refreshToken);
        console.log('✅ Refresh token manually set');
      }
      
      // Verify storage
      const verifyToken = localStorage.getItem('bookverse_token');
      console.log('🔍 Token verification after manual set:', verifyToken ? 'SUCCESS' : 'FAILED');
    }
  }

  clearTokens(): void {
    if (isPlatformBrowser(this.platformId)) {
      console.log('🗑️ Clearing tokens from localStorage');
      localStorage.removeItem('bookverse_token');
      localStorage.removeItem('bookverse_refresh_token');
      
      // Verify clearing
      const verifyToken = localStorage.getItem('bookverse_token');
      console.log('🔍 Token verification after clearing:', verifyToken ? 'STILL EXISTS' : 'CLEARED');
    }
  }

  isTokenValid(): boolean {
    const token = this.getToken();
    if (!token) {
      console.log('🔍 Token validation: No token found');
      return false;
    }
    
    try {
      const parts = token.split('.');
      if (parts.length !== 3) {
        console.log('🔍 Token validation: Invalid JWT structure');
        return false;
      }
      
      const payloadDecoded = this.urlBase64Decode(parts[1]);
      const payload = JSON.parse(payloadDecoded);
      
      const currentTime = Math.floor(Date.now() / 1000);
      const isValid = payload.exp && payload.exp > currentTime;
      
      console.log(`🔍 Token validation: ${isValid ? 'Valid' : 'Expired'} (exp: ${payload.exp}, now: ${currentTime})`);
      return isValid;
    } catch (error) {
      console.warn('🔍 Token validation error:', error);
      return false;
    }
  }

  isLoggedIn(): boolean {
    // Check both token validity AND user state
    const hasValidToken = this.isTokenValid();
    const hasUserState = this.isCustomerLoggedIn() || this.isAdminLoggedIn();
    
    console.log(`🔍 Login check - Token valid: ${hasValidToken}, User state: ${hasUserState}`);
    
    // If we have user state but no token, try to restore from storage
    if (hasUserState && !hasValidToken) {
      const storedToken = this.getToken();
      if (storedToken) {
        console.log('🔄 Found stored token, re-validating...');
        return this.isTokenValid();
      }
    }
    
    return hasValidToken && hasUserState;
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

  signup(username: string, email: string, password: string): Observable<boolean> {
    
    return this.http.get<UserModel[]>(this.usersUrl).pipe(
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
          passwordHash: password,
          role: 'admin'
        };
        
        return this.http.post<any>(this.usersUrl, newAdminUser);
      }),
      map(newUser => {
        this.currentAdmin.set(newUser);
        if (isPlatformBrowser(this.platformId)) {
          localStorage.setItem('bookverse_admin', JSON.stringify(newUser));
        }
        return true;
      }),
      catchError(err => {
        return throwError(() => new Error(err.message || 'Registration failed'));
      })
    );
  }

  forgotPassword(email: string): Observable<boolean> {
    
    return this.http.get<UserModel[]>(this.usersUrl).pipe(
      map(users => {
        const user = users.find(u => u.email === email);
        if (user) {
          return true;
        } else {
          return true;
        }
      }),
      catchError(err => {
        return throwError(() => new Error('Unable to process password reset request'));
      })
    );
  }

  fetchCompleteUserProfile(): Observable<UserModel> {
    return this.http.get<any>(`${this.usersUrl}/profile`).pipe(
      map(response => {
        const completeUser = this.transformUserData(response);
        
        this.currentCustomer.set(completeUser);
        if (isPlatformBrowser(this.platformId)) {
          localStorage.setItem('bookverse_customer', JSON.stringify(completeUser));
        }
        
        return completeUser;
      }),
      catchError(err => {
        return throwError(() => new Error('Failed to fetch user profile'));
      })
    );
  }

  // Force refresh authentication state from storage
  refreshAuthState(): void {
    console.log('🔄 Force refreshing auth state...');
    if (isPlatformBrowser(this.platformId)) {
      const token = localStorage.getItem('bookverse_token');
      const customer = localStorage.getItem('bookverse_customer');
      const admin = localStorage.getItem('bookverse_admin');
      
      console.log('🔍 Storage check:', {
        hasToken: !!token,
        hasCustomer: !!customer,
        hasAdmin: !!admin
      });
      
      // Clear current state
      this.currentCustomer.set(null);
      this.currentAdmin.set(null);
      
      // Restore from storage
      if (customer) {
        try {
          const parsedCustomer = JSON.parse(customer);
          this.currentCustomer.set(this.transformUserData(parsedCustomer));
          console.log('✅ Customer state restored');
        } catch (error) {
          console.error('❌ Failed to restore customer state:', error);
          localStorage.removeItem('bookverse_customer');
        }
      }
      
      if (admin) {
        try {
          const parsedAdmin = JSON.parse(admin);
          this.currentAdmin.set(parsedAdmin);
          console.log('✅ Admin state restored');
        } catch (error) {
          console.error('❌ Failed to restore admin state:', error);
          localStorage.removeItem('bookverse_admin');
        }
      }
      
      console.log('🔄 Auth state refresh completed');
    }
  }
  
  // Emergency method to create a mock token for testing
  createMockToken(): void {
    if (isPlatformBrowser(this.platformId) && this.currentCustomer()) {
      const mockPayload = {
        sub: this.currentCustomer()?.id,
        username: this.currentCustomer()?.username,
        exp: Math.floor(Date.now() / 1000) + (24 * 60 * 60) // 24 hours from now
      };
      
      // Create a simple mock JWT (for testing only)
      const header = btoa(JSON.stringify({alg: 'HS256', typ: 'JWT'}));
      const payload = btoa(JSON.stringify(mockPayload));
      const mockToken = `${header}.${payload}.mock_signature`;
      
      localStorage.setItem('bookverse_token', mockToken);
      console.log('🔧 Mock token created for testing');
    }
  }

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