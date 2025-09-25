import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { AdminUser } from '../../../models/book';

@Component({
  selector: 'app-add-user',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './add-user.component.html',
  styleUrls: ['./add-user.component.css']
})
export class AddUserComponent implements OnInit {
  userForm!: FormGroup;
  isLoading: boolean = false;
  successMessage: string = '';
  errorMessage: string = '';

  constructor(
    private fb: FormBuilder, 
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.userForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(30), Validators.pattern(/^[a-zA-Z0-9_]+$/)]],
      email: ['', [Validators.required, Validators.email]], 
      password: ['', [Validators.required, Validators.minLength(8), Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/)]],
      confirmPassword: ['', [Validators.required]],
      fullName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      employeeId: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
      department: ['Administration', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      mobileNumber: ['', [Validators.pattern(/^\+?[1-9]\d{1,14}$/)]],
      dateOfBirth: [''],
      userRole: ['ADMIN'],
      hireDate: [''],
      salary: ['', [Validators.min(0)]]
    }, { validators: this.passwordMatchValidator });
  }

  // Custom validator for password confirmation
  passwordMatchValidator(form: any) {
    const password = form.get('password');
    const confirmPassword = form.get('confirmPassword');
    
    if (password && confirmPassword && password.value !== confirmPassword.value) {
      confirmPassword.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    } else {
      if (confirmPassword?.errors?.['passwordMismatch']) {
        delete confirmPassword.errors['passwordMismatch'];
        if (Object.keys(confirmPassword.errors).length === 0) {
          confirmPassword.setErrors(null);
        }
      }
    }
    return null;
  }

  // Password validation helper methods
  hasUppercase(): boolean {
    const password = this.userForm.get('password')?.value;
    return password && /[A-Z]/.test(password);
  }

  hasLowercase(): boolean {
    const password = this.userForm.get('password')?.value;
    return password && /[a-z]/.test(password);
  }

  hasNumber(): boolean {
    const password = this.userForm.get('password')?.value;
    return password && /[0-9]/.test(password);
  }

  hasSpecialChar(): boolean {
    const password = this.userForm.get('password')?.value;
    return password && /[!@#$%^&*(),.?":{}|<>]/.test(password);
  }

  addUser(): void {
    if (this.userForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';

      const adminRegistrationData = {
        username: this.userForm.value.username,
        email: this.userForm.value.email,
        password: this.userForm.value.password,
        confirmPassword: this.userForm.value.confirmPassword,
        fullName: this.userForm.value.fullName, 
        userRole: this.userForm.value.userRole,
        department: this.userForm.value.department,
        employeeId: this.userForm.value.employeeId,
        mobileNumber: this.userForm.value.mobileNumber,
        dateOfBirth: this.userForm.value.dateOfBirth,
        hireDate: this.userForm.value.hireDate,
        salary: this.userForm.value.salary
      };

      console.log('Admin Registration Data:', adminRegistrationData);
      
      // Use AuthService for admin registration
      this.authService.registerAdmin(adminRegistrationData).subscribe({
        next: (response: any) => {
          console.log('Admin user created successfully:', response);
          this.successMessage = 'Admin user created successfully!';
          this.isLoading = false;
          
          // Reset form
          this.userForm.reset({
            username: '',
            email: '',
            password: '',
            confirmPassword: '',
            fullName: '',
            employeeId: '',
            department: 'Administration',
            mobileNumber: '',
            dateOfBirth: '',
            userRole: 'ADMIN',
            hireDate: '',
            salary: ''
          });
          
          // Redirect back to users list after a short delay
          setTimeout(() => {
            this.router.navigate(['/admin/users']);
          }, 2000);
        },
        error: (error: any) => {
          console.error('Error creating admin user:', error);
          this.errorMessage = error.error?.message || 'Failed to create admin user. Please try again.';
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
