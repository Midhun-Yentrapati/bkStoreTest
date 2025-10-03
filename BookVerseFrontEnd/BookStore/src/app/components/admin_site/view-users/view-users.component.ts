import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, FormControl } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
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
    private http: HttpClient,
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
      this.isLoading = false;
      this.applyFilters();
    }).catch(error => {
      console.error('Error loading users:', error);
      this.errorMessage = 'Failed to load users. Please try again.';
      this.isLoading = false;
    });
  }

  private async loadCustomerUsers(): Promise<void> {
    return this.http.get<any>(this.customersUrl).toPromise().then(data => {
      // Handle paginated response
      if (data.content && Array.isArray(data.content)) {
        this.normalUsers = data.content.map((user: any) => {
          const mappedUser = {
            id: user.id,
            username: user.username,
            email: user.email,
            fullName: user.fullName || user.full_name,
            userType: user.userType || user.user_type,
            mobileNumber: user.mobileNumber || user.mobile_number,
            dateOfBirth: user.dateOfBirth || user.date_of_birth,
            bio: user.bio,
            isActive: user.isActive !== undefined ? user.isActive : true,
            createdAt: user.createdAt || user.created_at,
            lastUpdated: user.lastUpdated || user.last_updated,
            userRole: user.userRole || user.user_role
          };
          return mappedUser;
        });
      } else if (Array.isArray(data)) {
        // Handle direct array response
        this.normalUsers = data.map((user: any) => ({
          id: user.id,
          username: user.username,
          email: user.email,
          fullName: user.fullName || user.full_name,
          userType: user.userType || user.user_type,
          mobileNumber: user.mobileNumber || user.mobile_number,
          dateOfBirth: user.dateOfBirth || user.date_of_birth,
          bio: user.bio,
          isActive: user.isActive !== undefined ? user.isActive : true,
          createdAt: user.createdAt || user.created_at,
          lastUpdated: user.lastUpdated || user.last_updated,
          userRole: user.userRole || user.user_role
        }));
      } else {
        this.normalUsers = [];
      }
      
      console.log('Loaded customer users:', this.normalUsers.length);
    }).catch(error => {
      console.error('Error loading customer users:', error);
      this.normalUsers = [];
      throw error;
    });
  }

  private async loadAdminUsers(): Promise<void> {
    return this.http.get<any>(this.adminUsersUrl).toPromise().then(data => {
      // Handle paginated response
      if (data.content && Array.isArray(data.content)) {
        this.adminUsers = data.content.map((user: any) => ({
          id: user.id,
          username: user.username,
          email: user.email,
          fullName: user.fullName || user.full_name,
          userType: user.userType || user.user_type,
          userRole: user.userRole || user.user_role,
          employeeId: user.employeeId || user.employee_id,
          department: user.department,
          isActive: user.isActive !== undefined ? user.isActive : true,
          createdAt: user.createdAt || user.created_at,
          lastUpdated: user.lastUpdated || user.last_updated
        }));
      } else if (Array.isArray(data)) {
        // Handle direct array response
        this.adminUsers = data.map((user: any) => ({
          id: user.id,
          username: user.username,
          email: user.email,
          fullName: user.fullName || user.full_name,
          userType: user.userType || user.user_type,
          userRole: user.userRole || user.user_role,
          employeeId: user.employeeId || user.employee_id,
          department: user.department,
          isActive: user.isActive !== undefined ? user.isActive : true,
          createdAt: user.createdAt || user.created_at,
          lastUpdated: user.lastUpdated || user.last_updated
        }));
      } else {
        this.adminUsers = [];
      }
      
      console.log('Loaded admin users:', this.adminUsers.length);
    }).catch(error => {
      console.error('Error loading admin users:', error);
      this.adminUsers = [];
      throw error;
    });
  }

  private setupSearch(): void {
    this.searchForm.get('query')?.valueChanges.subscribe(() => {
      this.applyFilters();
    });
  }

  private applyFilters(): void {
    const query = this.searchForm.get('query')?.value?.toLowerCase() || '';

    // Filter admin users
    this.filteredAdminUsers = this.adminUsers.filter(user =>
      user.username.toLowerCase().includes(query) ||
      user.email.toLowerCase().includes(query) ||
      user.fullName.toLowerCase().includes(query) ||
      (user.employeeId && user.employeeId.toLowerCase().includes(query)) ||
      (user.department && user.department.toLowerCase().includes(query))
    );

    // Filter normal users
    this.filteredNormalUsers = this.normalUsers.filter(user =>
      user.username.toLowerCase().includes(query) ||
      user.email.toLowerCase().includes(query) ||
      user.fullName.toLowerCase().includes(query) ||
      (user.mobileNumber && user.mobileNumber.includes(query))
    );
  }

  onSearch(): void {
    this.applyFilters();
  }

  clearSearch(): void {
    this.searchForm.get('query')?.setValue('');
  }

  editUser(user: UserModel | AdminUser): void {
    // Navigate to user edit page
    this.router.navigate(['/admin/edit-user', user.id]);
  }

  deleteUser(user: UserModel | AdminUser): void {
    if (confirm(`Are you sure you want to delete user "${user.username}"?`)) {
      const deleteUrl = `${this.apiBaseUrl}/users/${user.id}`;
      
      this.http.delete(deleteUrl).subscribe({
        next: () => {
          console.log('User deleted successfully');
          this.loadUsers(); // Reload the users list
        },
        error: (error) => {
          console.error('Error deleting user:', error);
          this.errorMessage = 'Failed to delete user. Please try again.';
        }
      });
    }
  }

  toggleUserStatus(user: UserModel | AdminUser): void {
    const newStatus = !user.isActive;
    const updateUrl = `${this.apiBaseUrl}/users/${user.id}/status`;
    
    this.http.put(updateUrl, { isActive: newStatus }).subscribe({
      next: () => {
        user.isActive = newStatus;
        console.log(`User ${user.username} status updated to ${newStatus ? 'active' : 'inactive'}`);
      },
      error: (error) => {
        console.error('Error updating user status:', error);
        this.errorMessage = 'Failed to update user status. Please try again.';
      }
    });
  }

  resetPassword(user: UserModel | AdminUser): void {
    if (confirm(`Are you sure you want to reset password for "${user.username}"?`)) {
      const resetUrl = `${this.apiBaseUrl}/users/${user.id}/reset-password`;
      
      this.http.post(resetUrl, {}).subscribe({
        next: () => {
          console.log('Password reset successfully');
          alert('Password reset email has been sent to the user.');
        },
        error: (error) => {
          console.error('Error resetting password:', error);
          this.errorMessage = 'Failed to reset password. Please try again.';
        }
      });
    }
  }

  viewUserDetails(user: UserModel | AdminUser): void {
    // Navigate to user details page
    this.router.navigate(['/admin/user-details', user.id]);
  }

  exportUsers(): void {
    // Export users data
    const allUsers = [...this.normalUsers, ...this.adminUsers];
    const csvData = this.convertToCSV(allUsers);
    this.downloadCSV(csvData, 'users_export.csv');
  }

  private convertToCSV(data: any[]): string {
    if (data.length === 0) return '';
    
    const headers = Object.keys(data[0]);
    const csvContent = [
      headers.join(','),
      ...data.map(row => headers.map(header => `"${row[header] || ''}"`).join(','))
    ].join('\n');
    
    return csvContent;
  }

  private downloadCSV(csvContent: string, filename: string): void {
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    if (link.download !== undefined) {
      const url = URL.createObjectURL(blob);
      link.setAttribute('href', url);
      link.setAttribute('download', filename);
      link.style.visibility = 'hidden';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    }
  }

  refreshUsers(): void {
    this.loadUsers();
  }

  // Methods needed by the template that were removed
  goBack(): void {
    this.router.navigate(['/admin-main']);
  }

  getSearchControl(): FormControl {
    return this.searchForm.get('query') as FormControl;
  }

  showAddUserPage(): void {
    console.log('Navigating to add user page');
    this.router.navigate(['/admin/add-user']);
  }

  trackByAdminUserId(index: number, user: AdminUser): string {
    return user.id;
  }

  trackByNormalUserId(index: number, user: UserModel): string {
    return user.id;
  }

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

  deleteAdminUser(adminUserId: string): void {
    this.deleteUser({ id: adminUserId } as AdminUser);
  }

  deleteNormalUser(userId: string): void {
    this.deleteUser({ id: userId } as UserModel);
  }

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

  activateUser(userId: string, userRole?: string): void {
    this.updateUserStatus(userId, true, userRole);
  }

  deactivateUser(userId: string, userRole?: string): void {
    // Prevent disabling ADMIN and SUPER_ADMIN accounts
    if (userRole === 'ADMIN' || userRole === 'SUPER_ADMIN') {
      alert('Cannot disable ADMIN or SUPER_ADMIN accounts for security reasons.');
      return;
    }
    this.updateUserStatus(userId, false, userRole);
  }

  private updateUserStatus(userId: string, isActive: boolean, userRole?: string): void {
    const action = isActive ? 'activate' : 'deactivate';
    const confirmMessage = `Are you sure you want to ${action} this user account?`;
    
    if (confirm(confirmMessage)) {
      console.log(`Attempting to ${action} user with ID: ${userId}`);
      
      const updateUrl = `${this.apiBaseUrl}/users/${userId}/status`;
      
      this.http.put(updateUrl, { isActive }).subscribe({
        next: (data: any) => {
          console.log(`User status updated successfully:`, data);
          
          // Update the user status in local arrays
          this.updateUserStatusInArrays(userId, data.isActive);
          
          const statusText = data.isActive ? 'activated' : 'deactivated';
          alert(`User account ${statusText} successfully!`);
        },
        error: (error) => {
          console.error('Error updating user status:', error);
          alert('Failed to update user status. Please try again.');
        }
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

  // Utility methods for template
  getStatusText(isActive: boolean): string {
    return isActive ? 'Active' : 'Inactive';
  }

  getStatusClass(isActive: boolean): string {
    return isActive ? 'status-active' : 'status-inactive';
  }

  getRoleDisplayName(role: string): string {
    switch (role) {
      case 'SUPER_ADMIN': return 'Super Admin';
      case 'ADMIN': return 'Admin';
      case 'MANAGER': return 'Manager';
      case 'CUSTOMER': return 'Customer';
      default: return role;
    }
  }

  getTypeDisplayName(type: string): string {
    switch (type) {
      case 'ADMIN': return 'Admin';
      case 'CUSTOMER': return 'Customer';
      default: return type;
    }
  }
}
