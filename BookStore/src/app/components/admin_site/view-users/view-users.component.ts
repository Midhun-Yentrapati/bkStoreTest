import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, FormControl } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { AdminUser } from '../../../models/book';
import { UserModel } from '../../../models/user.model';

@Component({
  selector: 'app-view-users',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './view-users.component.html',
  styleUrls: ['./view-users.component.css']
})
export class ViewUsersComponent implements OnInit {
  adminUsers: AdminUser[] = [];
  normalUsers: UserModel[] = [];
  filteredAdminUsers: AdminUser[] = [];
  filteredNormalUsers: UserModel[] = [];
  searchForm!: FormGroup;
  isLoading: boolean = false;
  errorMessage: string = '';

  private apiBaseUrl = 'http://localhost:8090/api'; // API Gateway URL
  private usersUrl = `${this.apiBaseUrl}/users/admin/all`; // Correct backend endpoint
  private customersUrl = `${this.apiBaseUrl}/users/admin/customers`; // Customer users endpoint
  private adminUsersUrl = `${this.apiBaseUrl}/users/admin/admins`; // Admin users endpoint

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.searchForm = this.fb.group({
      query: ['']
    });

    this.loadUsers();
    this.setupSearch();
  }

  private loadUsers(): void {
    this.isLoading = true;
    this.errorMessage = '';

    // Load both customer and admin users from backend
    Promise.all([
      this.loadCustomerUsers(),
      this.loadAdminUsers()
    ]).then(() => {
      this.filteredAdminUsers = [...this.adminUsers];
      this.filteredNormalUsers = [...this.normalUsers];
      
      // Debug logging
      console.log('=== USER DATA LOADED ===');
      console.log('Admin Users:', this.adminUsers);
      console.log('Customer Users:', this.normalUsers);
      console.log('Filtered Admin Users:', this.filteredAdminUsers);
      console.log('Filtered Customer Users:', this.filteredNormalUsers);
      
      console.log('Real users loaded - Admin users:', this.adminUsers.length);
      console.log('Real users loaded - Customer users:', this.normalUsers.length);
      
      this.isLoading = false;
    }).catch(error => {
      console.error('Error loading users:', error);
      this.errorMessage = 'Failed to load users. Please try again.';
      this.isLoading = false;
    });
  }

  private async loadCustomerUsers(): Promise<void> {
    try {
      // Use AuthService to get customer users (this should call /api/users/admin/customers)
      const response = await fetch(this.customersUrl, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${this.authService.getToken()}`,
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) {
        throw new Error(`Failed to fetch customers: ${response.status}`);
      }

      const data = await response.json();
      
      // Handle paginated response
      if (data.content && Array.isArray(data.content)) {
        this.normalUsers = data.content.map((user: any) => {
          const mappedUser = {
            id: user.id,
            username: user.username,
            email: user.email,
            fullName: user.fullName || user.full_name,
            userType: user.userType || user.user_type,
            isActive: user.accountStatus === 'ACTIVE' || user.isActive || true, // Default to true if undefined
            mobileNumber: user.mobileNumber || user.mobile_number,
            password: '',
            createdAt: user.createdAt || user.created_at,
            updatedAt: user.updatedAt || user.updated_at
          } as UserModel;
          
          // Debug logging
          console.log('Customer User Mapped:', {
            id: mappedUser.id,
            username: mappedUser.username,
            isActive: mappedUser.isActive,
            originalAccountStatus: user.accountStatus,
            originalIsActive: user.isActive
          });
          
          return mappedUser;
        });
      } else {
        this.normalUsers = [];
      }
    } catch (error) {
      console.error('Error loading customer users:', error);
      this.normalUsers = [];
      throw error;
    }
  }

  private async loadAdminUsers(): Promise<void> {
    try {
      // Use AuthService to get admin users (this should call /api/users/admin/admins)
      const response = await fetch(this.adminUsersUrl, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${this.authService.getToken()}`,
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) {
        throw new Error(`Failed to fetch admins: ${response.status}`);
      }

      const data = await response.json();
      
      // Handle paginated response
      if (data.content && Array.isArray(data.content)) {
        this.adminUsers = data.content.map((user: any) => {
          const mappedUser = {
            id: user.id,
            username: user.username,
            email: user.email,
            passwordHash: user.password,
            role: 'admin',
            fullName: user.fullName || user.full_name,
            userRole: user.userRole || user.user_role,
            isActive: user.accountStatus === 'ACTIVE' || user.isActive || true // Default to true if undefined
          } as AdminUser;
          
          // Debug logging
          console.log('Admin User Mapped:', {
            id: mappedUser.id,
            username: mappedUser.username,
            userRole: mappedUser.userRole,
            isActive: mappedUser.isActive,
            originalAccountStatus: user.accountStatus,
            originalIsActive: user.isActive
          });
          
          return mappedUser;
        });
      } else {
        this.adminUsers = [];
      }
    } catch (error) {
      console.error('Error loading admin users:', error);
      this.adminUsers = [];
      throw error;
    }
  }

  private setupSearch(): void {
    this.searchForm.get('query')?.valueChanges.subscribe(query => {
      const lowerCaseQuery = query.toLowerCase();
      
      // Filter admin users
      this.filteredAdminUsers = this.adminUsers.filter((user: AdminUser) =>
        user.id.toLowerCase().includes(lowerCaseQuery) ||
        user.username.toLowerCase().includes(lowerCaseQuery) ||
        user.email.toLowerCase().includes(lowerCaseQuery)
      );

      // Filter normal users
      this.filteredNormalUsers = this.normalUsers.filter((user: UserModel) =>
        user.id.toLowerCase().includes(lowerCaseQuery) ||
        user.username.toLowerCase().includes(lowerCaseQuery) ||
        user.email.toLowerCase().includes(lowerCaseQuery) ||
        user.fullName.toLowerCase().includes(lowerCaseQuery) ||
        user.mobileNumber.includes(lowerCaseQuery)
      );
    });
  }

  deleteAdminUser(adminUserId: string): void {
    if (confirm(`Are you sure you want to delete admin user with ID: ${adminUserId}?`)) {
      console.log(`Attempting to delete admin user with ID: ${adminUserId}`);
      
      // Real API call to delete admin user
      const token = this.authService.getToken();
      if (!token) {
        console.error('No authentication token found');
        alert('Authentication required. Please login again.');
        return;
      }

      fetch(`${this.apiBaseUrl}/users/admin/${adminUserId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      .then(response => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
      })
      .then(data => {
        console.log(`Admin user with ID ${adminUserId} deleted successfully:`, data);
        // Remove from local arrays
        this.adminUsers = this.adminUsers.filter((user: AdminUser) => user.id !== adminUserId);
        this.filteredAdminUsers = this.filteredAdminUsers.filter((user: AdminUser) => user.id !== adminUserId);
        alert('Admin user deleted successfully!');
      })
      .catch(error => {
        console.error('Error deleting admin user:', error);
        alert('Failed to delete admin user. Please try again.');
      });
    }
  }

  deleteNormalUser(userId: string): void {
    if (confirm(`Are you sure you want to delete this user? This action cannot be undone and the user will no longer be able to login.`)) {
      console.log(`Attempting to delete normal user with ID: ${userId}`);
      
      // Real API call to delete normal user
      const token = this.authService.getToken();
      if (!token) {
        console.error('No authentication token found');
        alert('Authentication required. Please login again.');
        return;
      }

      fetch(`${this.apiBaseUrl}/users/admin/${userId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      .then(response => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
      })
      .then(data => {
        console.log(`Normal user with ID ${userId} deleted successfully:`, data);
        // Remove from local arrays
        this.normalUsers = this.normalUsers.filter((user: UserModel) => user.id !== userId);
        this.filteredNormalUsers = this.filteredNormalUsers.filter((user: UserModel) => user.id !== userId);
        alert('User deleted successfully!');
      })
      .catch(error => {
        console.error('Error deleting normal user:', error);
        alert('Failed to delete user. Please try again.');
      });
    }
  }

  // Activate user account
  activateUser(userId: string, userRole?: string): void {
    this.updateUserStatus(userId, true, userRole);
  }

  // Deactivate user account
  deactivateUser(userId: string, userRole?: string): void {
    // Prevent disabling ADMIN and SUPER_ADMIN accounts
    if (userRole === 'ADMIN' || userRole === 'SUPER_ADMIN') {
      alert('Cannot disable ADMIN or SUPER_ADMIN accounts for security reasons.');
      return;
    }
    this.updateUserStatus(userId, false, userRole);
  }

  // Update user status in database
  private updateUserStatus(userId: string, isActive: boolean, userRole?: string): void {
    const action = isActive ? 'activate' : 'deactivate';
    const confirmMessage = `Are you sure you want to ${action} this user account?`;
    
    if (confirm(confirmMessage)) {
      console.log(`Attempting to ${action} user with ID: ${userId}`);
      
      const token = this.authService.getToken();
      if (!token) {
        console.error('No authentication token found');
        alert('Authentication required. Please login again.');
        return;
      }

      fetch(`${this.apiBaseUrl}/users/admin/${userId}/toggle-status`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      .then(response => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
      })
      .then(data => {
        console.log(`User status updated successfully:`, data);
        
        // Update the user status in local arrays
        this.updateUserStatusInArrays(userId, data.isActive);
        
        const statusText = data.isActive ? 'activated' : 'deactivated';
        alert(`User account ${statusText} successfully!`);
      })
      .catch(error => {
        console.error('Error updating user status:', error);
        alert('Failed to update user status. Please try again.');
      });
    }
  }

  // Helper method to update user status in local arrays
  private updateUserStatusInArrays(userId: string, isActive: boolean): void {
    // Update admin users
    const adminUser = this.adminUsers.find(user => user.id === userId);
    if (adminUser) {
      adminUser.isActive = isActive;
    }
    
    const filteredAdminUser = this.filteredAdminUsers.find(user => user.id === userId);
    if (filteredAdminUser) {
      filteredAdminUser.isActive = isActive;
    }

    // Update normal users
    const normalUser = this.normalUsers.find(user => user.id === userId);
    if (normalUser) {
      normalUser.isActive = isActive;
    }
    
    const filteredNormalUser = this.filteredNormalUsers.find(user => user.id === userId);
    if (filteredNormalUser) {
      filteredNormalUser.isActive = isActive;
    }
  }

  // Check if user can be disabled based on current admin's role
  canToggleUserStatus(userRole?: string): boolean {
    // If userRole is undefined, treat as regular user (can be toggled)
    if (!userRole) return true;
    
    // Get current admin's role
    const currentAdmin = this.authService.getCurrentAdmin();
    const currentAdminRole = currentAdmin?.userRole;
    
    // Permission logic:
    // 1. SUPER_ADMIN accounts cannot be disabled by anyone
    // 2. ADMIN accounts can only be disabled by SUPER_ADMIN
    // 3. Other roles can be disabled by both SUPER_ADMIN and ADMIN
    
    if (userRole === 'SUPER_ADMIN') {
      return false; // SUPER_ADMIN accounts are always protected
    }
    
    if (userRole === 'ADMIN') {
      return currentAdminRole === 'SUPER_ADMIN'; // Only SUPER_ADMIN can disable ADMIN accounts
    }
    
    // All other roles (MANAGER, MODERATOR, SUPPORT, CUSTOMER) can be toggled
    return true;
  }

  // Get user role display text
  getUserRoleText(userRole?: string): string {
    if (!userRole) return 'Customer';
    
    switch (userRole) {
      case 'SUPER_ADMIN': return 'Super Admin';
      case 'ADMIN': return 'Admin';
      case 'MANAGER': return 'Manager';
      case 'MODERATOR': return 'Moderator';
      case 'SUPPORT': return 'Support';
      case 'CUSTOMER': return 'Customer';
      default: return userRole;
    }
  }

  // Get protection text for disabled toggle buttons
  getProtectionText(userRole?: string): string {
    if (userRole === 'SUPER_ADMIN') {
      return 'Always Protected';
    }
    if (userRole === 'ADMIN') {
      const currentAdmin = this.authService.getCurrentAdmin();
      const currentAdminRole = currentAdmin?.userRole;
      if (currentAdminRole !== 'SUPER_ADMIN') {
        return 'SUPER_ADMIN Only';
      }
    }
    return 'Protected';
  }

  // Get detailed protection reason for tooltip
  getProtectionReason(userRole?: string): string {
    if (userRole === 'SUPER_ADMIN') {
      return 'SUPER_ADMIN accounts cannot be disabled for security reasons';
    }
    if (userRole === 'ADMIN') {
      const currentAdmin = this.authService.getCurrentAdmin();
      const currentAdminRole = currentAdmin?.userRole;
      if (currentAdminRole !== 'SUPER_ADMIN') {
        return 'Only SUPER_ADMIN can disable ADMIN accounts';
      }
    }
    return 'This account is protected from being disabled';
  }
  showAddUserPage(): void {
    console.log('Navigating to add user page');
    this.router.navigate(['/admin/add-user']);
  }
  
  goBack(): void {
    this.router.navigate(['/admin-main']);
  }

  // TrackBy functions for better performance
  trackByAdminUserId(index: number, user: AdminUser): string {
    return user.id;
  }

  trackByNormalUserId(index: number, user: UserModel): string {
    return user.id;
  }

  // Helper method to get form control (fixes the template binding issue)
  getSearchControl(): FormControl {
    return this.searchForm.get('query') as FormControl;
  }

  // Refresh users data
  refreshUsers(): void {
    this.loadUsers();
  }
}
