import { Component, OnInit, effect } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, FormControl } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { AdminService, User } from '../../../services/admin.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-view-users',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './view-users.component.html',
  styleUrls: ['./view-users.component.css']
})
export class ViewUsersComponent implements OnInit {
  adminUsers: User[] = []; // All admin users from unified table
  normalUsers: User[] = []; // All customer users from unified table
  filteredAdminUsers: User[] = [];
  filteredNormalUsers: User[] = [];
  searchForm!: FormGroup;
  isLoading: boolean = false;
  errorMessage: string = '';
  currentUser: User | null = null;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService,
    private adminService: AdminService
  ) {
    effect(() => {
      this.currentUser = this.authService.currentAdmin() as User | null;
    });
  }

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

    forkJoin({
      adminUsers: this.adminService.getAllAdmins(),
      customerUsers: this.adminService.getAllCustomers()
    }).subscribe({
      next: (result) => {
        console.log('Raw result from AdminService:', result);
        
        const adminUsersData = Array.isArray(result.adminUsers) ? result.adminUsers : [];
        const customerUsersData = Array.isArray(result.customerUsers) ? result.customerUsers : [];
        
        console.log('Admin users data:', adminUsersData);
        console.log('Customer users data:', customerUsersData);
        
        // Process admin users - they come as unified User objects
        this.adminUsers = adminUsersData.map((user: any) => ({
          ...user,
          // Ensure isActive is computed from accountStatus
          isActive: user.isActive !== undefined ? user.isActive : 
                   (user.accountStatus === 'ACTIVE' || user.isAccountActive === true),
          // Ensure userRole is available (fallback to role if needed)
          userRole: user.userRole || user.role || 'ADMIN',
          // Ensure userType is set
          userType: user.userType || 'ADMIN'
        }));

        // Process customer users - they come as unified User objects  
        this.normalUsers = customerUsersData.map((user: any) => ({
          ...user,
          // Ensure isActive is computed from accountStatus
          isActive: user.isActive !== undefined ? user.isActive : 
                   (user.accountStatus === 'ACTIVE' || user.isAccountActive === true),
          // Ensure userRole is available
          userRole: user.userRole || 'CUSTOMER',
          // Ensure userType is set
          userType: user.userType || 'CUSTOMER',
          // Map phoneNumber to mobileNumber if needed
          mobileNumber: user.mobileNumber || user.phoneNumber || ''
        }));

        this.applyFilters();
        
        console.log(`Loaded ${this.adminUsers.length} admin users and ${this.normalUsers.length} customer users`);
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading users:', error);
        this.errorMessage = 'Failed to load users. Please try again.';
        this.isLoading = false;
        
        this.adminUsers = [];
        this.normalUsers = [];
        this.applyFilters();
      }
    });
  }

  private setupSearch(): void {
    this.searchForm.get('query')?.valueChanges.subscribe(() => {
      this.applyFilters();
    });
  }

  private applyFilters(): void {
    const query = this.searchForm.get('query')?.value?.toLowerCase() || '';

    // Filter admin users - handle null values gracefully
    this.filteredAdminUsers = this.adminUsers.filter(user =>
      user.username?.toLowerCase().includes(query) ||
      user.email?.toLowerCase().includes(query) ||
      user.fullName?.toLowerCase().includes(query) ||
      (user.employeeId && user.employeeId.toLowerCase().includes(query)) ||
      (user.department && user.department.toLowerCase().includes(query))
    );

    // Filter customer users - handle null values gracefully
    this.filteredNormalUsers = this.normalUsers.filter(user =>
      user.username?.toLowerCase().includes(query) ||
      user.email?.toLowerCase().includes(query) ||
      user.fullName?.toLowerCase().includes(query) ||
      (user.mobileNumber && user.mobileNumber.includes(query))
    );
  }

  onSearch(): void {
    this.applyFilters();
  }

  clearSearch(): void {
    this.searchForm.get('query')?.setValue('');
  }

  editUser(user: User): void {
    this.router.navigate(['/admin/edit-user', user.id]);
  }

  deleteUser(user: User): void {
    if (confirm(`Are you sure you want to delete user "${user.username}"?`)) {
      this.adminService.deleteUser(user.id).subscribe({
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

  resetPassword(user: User): void {
    if (confirm(`Are you sure you want to reset password for "${user.username}"?`)) {
      this.adminService.resetUserPassword(user.id).subscribe({
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

  viewUserDetails(user: User): void {
    this.router.navigate(['/admin/user-details', user.id]);
  }

  exportUsers(): void {
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

  canDisableUser(user: User): boolean {
    if (!this.currentUser) {
      return false;
    }

    // Use userRole property directly
    const currentUserRole = this.currentUser.userRole;
    const targetUserRole = user.userRole;

    // Super admin can never be disabled
    if (targetUserRole === 'SUPER_ADMIN') {
      return false;
    }

    // A user cannot disable themselves
    if (user.id === this.currentUser.id) {
      return false;
    }

    // SUPER_ADMIN can disable anyone except other SUPER_ADMINs
    if (currentUserRole === 'SUPER_ADMIN') {
      return targetUserRole !== 'SUPER_ADMIN';
    }

    // ADMIN can disable customers but not other admins or super admins
    if (currentUserRole === 'ADMIN') {
      return targetUserRole !== 'SUPER_ADMIN' && targetUserRole !== 'ADMIN';
    }

    // Other roles cannot disable users
    return false;
  }

  getProtectionReason(userRole?: string): string {
    if (userRole === 'SUPER_ADMIN') {
      return 'Super admins are protected and cannot be disabled.';
    }
    if (userRole === 'ADMIN') {
      return 'Admins can only be managed by super admins.';
    }
    return 'This user has a protected role.';
  }

  getProtectionText(userRole?: string): string {
    if (userRole === 'SUPER_ADMIN' || userRole === 'ADMIN') {
      return 'Protected';
    }
    return 'Protected Role';
  }

  toggleUserStatus(user: User): void {
    const newStatus = !user.isActive;
    const action = newStatus ? 'activate' : 'deactivate';
    const confirmation = window.confirm(`Are you sure you want to ${action} the user "${user.username}"?\n\nClick 'OK' to continue or 'Cancel' to abort.`);

    if (confirmation) {
      this.adminService.updateUserStatus(user.id, newStatus).subscribe({
        next: () => {
          // Update the user in both arrays
          const adminUser = this.adminUsers.find(u => u.id === user.id);
          if (adminUser) adminUser.isActive = newStatus;

          const normalUser = this.normalUsers.find(u => u.id === user.id);
          if (normalUser) normalUser.isActive = newStatus;

          this.applyFilters();
          console.log(`User ${user.username} has been successfully ${action}d.`);
        },
        error: (error) => {
          console.error(`Error trying to ${action} user:`, error);
          this.errorMessage = `Failed to ${action} user. Please try again.`;
        }
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/admin/dashboard']);
  }

  getSearchControl(): FormControl {
    return this.searchForm.get('query') as FormControl;
  }

  showAddUserPage(): void {
    console.log('Navigating to add user page');
    this.router.navigate(['/admin/add-user']);
  }

  trackByUserId(index: number, user: User): any {
    return user.id;
  }

  getUserRoleText(userRole?: string): string {
    if (!userRole) return 'Customer';
    
    switch (userRole) {
      case 'SUPER_ADMIN': return 'Super Admin';
      case 'ADMIN': return 'Admin';
      case 'MANAGER': return 'Manager';
      case 'MODERATOR': return 'Moderator';
      case 'CUSTOMER': return 'Customer';
      default: return userRole;
    }
  }

  getUserRoleDisplayName(userRole?: string): string {
    return this.getUserRoleText(userRole);
  }
}
