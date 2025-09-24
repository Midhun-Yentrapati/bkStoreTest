import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, FormControl } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { AdminUser } from '../../../models/user.model';
import { UserModel } from '../../../models/user.model';

@Component({
  selector: 'app-view-users',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, HttpClientModule],
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

  private usersUrl = 'http://localhost:3000/users';
  private adminUsersUrl = 'http://localhost:3000/adminUsers';

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private http: HttpClient
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

    // Load both admin users and normal users
    Promise.all([
      this.loadAdminUsers(),
      this.loadNormalUsers()
    ]).finally(() => {
      this.isLoading = false;
    });
  }

  private loadAdminUsers(): Promise<void> {
    return this.http.get<AdminUser[]>(this.adminUsersUrl).toPromise()
      .then(users => {
        this.adminUsers = users || [];
        this.filteredAdminUsers = [...this.adminUsers];
      })
      .catch(error => {
        console.error('Error loading admin users:', error);
        this.errorMessage = 'Failed to load admin users';
      });
  }

  private loadNormalUsers(): Promise<void> {
    return this.http.get<UserModel[]>(this.usersUrl).toPromise()
      .then(users => {
        // Filter out admin users from normal users (those with passwordHash field)
        this.normalUsers = (users || []).filter(user => !('passwordHash' in user));
        this.filteredNormalUsers = [...this.normalUsers];
      })
      .catch(error => {
        console.error('Error loading normal users:', error);
        this.errorMessage = 'Failed to load normal users';
      });
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
      
      this.http.delete(`${this.adminUsersUrl}/${adminUserId}`).subscribe({
        next: () => {
          console.log(`Admin user with ID ${adminUserId} deleted successfully.`);
          // Remove from local arrays
          this.adminUsers = this.adminUsers.filter((user: AdminUser) => user.id !== adminUserId);
          this.filteredAdminUsers = this.filteredAdminUsers.filter((user: AdminUser) => user.id !== adminUserId);
        },
        error: (error) => {
          console.error('Error deleting admin user:', error);
          this.errorMessage = 'Failed to delete admin user';
        }
      });
    }
  }

  deleteNormalUser(userId: string): void {
    if (confirm(`Are you sure you want to delete this user? This action cannot be undone and the user will no longer be able to login.`)) {
      console.log(`Attempting to delete normal user with ID: ${userId}`);
      
      this.http.delete(`${this.usersUrl}/${userId}`).subscribe({
        next: () => {
          console.log(`Normal user with ID ${userId} deleted successfully.`);
          // Remove from local arrays
          this.normalUsers = this.normalUsers.filter((user: UserModel) => user.id !== userId);
          this.filteredNormalUsers = this.filteredNormalUsers.filter((user: UserModel) => user.id !== userId);
        },
        error: (error) => {
          console.error('Error deleting normal user:', error);
          this.errorMessage = 'Failed to delete normal user';
        }
      });
    }
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
