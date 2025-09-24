import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { AdminUser } from '../../../models/admin.model';

@Component({
  selector: 'app-add-user',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, HttpClientModule],
  templateUrl: './add-user.component.html',
  styleUrls: ['./add-user.component.css']
})
export class AddUserComponent implements OnInit {
  userForm!: FormGroup;
  isLoading: boolean = false;
  successMessage: string = '';
  errorMessage: string = '';

  private adminUsersUrl = 'http://localhost:3000/adminUsers';

  constructor(
    private fb: FormBuilder, 
    private router: Router,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.userForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(30), Validators.pattern(/^[a-zA-Z0-9_]+$/)]],
      email: ['', [Validators.required, Validators.email]], 
      passwordHash: ['', [Validators.required, Validators.minLength(8), Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*(),.?":{}|<>])[A-Za-z\d!@#$%^&*(),.?":{}|<>]{8,}$/)]]
    });
  }

  // Password validation helper methods
  hasUppercase(): boolean {
    const password = this.userForm.get('passwordHash')?.value;
    return password && /[A-Z]/.test(password);
  }

  hasLowercase(): boolean {
    const password = this.userForm.get('passwordHash')?.value;
    return password && /[a-z]/.test(password);
  }

  hasNumber(): boolean {
    const password = this.userForm.get('passwordHash')?.value;
    return password && /[0-9]/.test(password);
  }

  hasSpecialChar(): boolean {
    const password = this.userForm.get('passwordHash')?.value;
    return password && /[!@#$%^&*(),.?":{}|<>]/.test(password);
  }

  addUser(): void {
    if (this.userForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';

      const newAdminUser: AdminUser = {
        id: crypto.randomUUID(),
        ...this.userForm.value
      };

      console.log('New Admin User Data:', newAdminUser);
      
      // Save to JSON server
      this.http.post<AdminUser>(this.adminUsersUrl, newAdminUser).subscribe({
        next: (savedUser) => {
          console.log('Admin user saved successfully:', savedUser);
          this.successMessage = 'Admin user created successfully!';
          this.isLoading = false;
          
          // Reset form
          this.userForm.reset({
            username: '',
            email: '',
            passwordHash: ''
          });
          
          // Redirect back to users list after a short delay
          setTimeout(() => {
            this.router.navigate(['/admin/users']);
          }, 2000);
        },
        error: (error) => {
          console.error('Error saving admin user:', error);
          this.errorMessage = 'Failed to create admin user. Please try again.';
          this.isLoading = false;
        }
      });
    } else {
      // Mark all fields as touched to display validation errors
      this.userForm.markAllAsTouched();
      this.errorMessage = 'Please fill in all fields correctly.';
      console.log('Form is invalid. Please check the fields.');
    }
  }

  goBack(): void {
    this.router.navigate(['/admin/users']);
  }
}
