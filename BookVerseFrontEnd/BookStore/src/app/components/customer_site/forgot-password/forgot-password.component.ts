// src/app/forgot-password/forgot-password.component.ts

import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common'; 
import { AuthService } from '../../../services/auth.service';


@Component({
  selector: 'app-forgot-password',
  standalone: true, 
  imports: [
    CommonModule,
    ReactiveFormsModule, 
    RouterLink 
  ],
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent implements OnInit {
  forgotPasswordForm!: FormGroup;
  resetPasswordForm!: FormGroup;
  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = false;
  showResetForm: boolean = false;
  userEmail: string = '';
  passwordStrength: 'weak' | 'medium' | 'strong' = 'weak';
  showPassword: boolean = false;
  showConfirmPassword: boolean = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.forgotPasswordForm = this.fb.group({
      email: ['', [
        Validators.required, 
        Validators.email,
        Validators.pattern(/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/)
      ]]
    });

    this.resetPasswordForm = this.fb.group({
      newPassword: ['', [
        Validators.required, 
        Validators.minLength(8),
        Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/)
      ]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });

    // Listen to password changes for strength calculation
    this.resetPasswordForm.get('newPassword')?.valueChanges.subscribe(password => {
      this.passwordStrength = this.calculatePasswordStrength(password);
    });
  }

  // Custom validator to check if passwords match
  passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const newPassword = control.get('newPassword');
    const confirmPassword = control.get('confirmPassword');
    
    if (newPassword && confirmPassword && newPassword.value !== confirmPassword.value) {
      return { passwordMismatch: true };
    }
    return null;
  }

  // Calculate password strength
  calculatePasswordStrength(password: string): 'weak' | 'medium' | 'strong' {
    if (!password) return 'weak';
    
    let score = 0;
    
    // Length check
    if (password.length >= 8) score += 1;
    if (password.length >= 12) score += 1;
    
    // Character variety checks
    if (/[a-z]/.test(password)) score += 1;
    if (/[A-Z]/.test(password)) score += 1;
    if (/\d/.test(password)) score += 1;
    if (/[@$!%*?&]/.test(password)) score += 1;
    
    // Additional complexity
    if (password.length > 8 && /[a-z]/.test(password) && /[A-Z]/.test(password) && /\d/.test(password)) score += 1;
    
    if (score >= 5) return 'strong';
    if (score >= 3) return 'medium';
    return 'weak';
  }

  // Get password strength color
  getPasswordStrengthColor(): string {
    switch (this.passwordStrength) {
      case 'strong': return '#28a745';
      case 'medium': return '#ffc107';
      case 'weak': return '#dc3545';
      default: return '#6c757d';
    }
  }

  // Get password strength text
  getPasswordStrengthText(): string {
    switch (this.passwordStrength) {
      case 'strong': return 'Strong Password';
      case 'medium': return 'Medium Password';
      case 'weak': return 'Weak Password';
      default: return 'Enter Password';
    }
  }

  // Toggle password visibility
  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  // Toggle confirm password visibility
  toggleConfirmPasswordVisibility(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  get f() {
    return this.forgotPasswordForm.controls;
  }

  get rf() {
    return this.resetPasswordForm.controls;
  }

  // Get email validation error message
  getEmailErrorMessage(): string {
    const emailControl = this.f['email'];
    if (emailControl.errors) {
      if (emailControl.errors['required']) return 'Email is required.';
      if (emailControl.errors['email']) return 'Please enter a valid email address.';
      if (emailControl.errors['pattern']) return 'Please enter a valid email format (e.g., user@example.com).';
    }
    return '';
  }

  // Get password validation error message
  getPasswordErrorMessage(): string {
    const passwordControl = this.rf['newPassword'];
    if (passwordControl.errors) {
      if (passwordControl.errors['required']) return 'Password is required.';
      if (passwordControl.errors['minlength']) return 'Password must be at least 8 characters long.';
      if (passwordControl.errors['pattern']) return 'Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character (@$!%*?&).';
    }
    return '';
  }

  onSubmit(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (this.forgotPasswordForm.invalid) {
      this.forgotPasswordForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    const { email } = this.forgotPasswordForm.value;

    // For now, simulate forgot password functionality
    // TODO: Implement proper forgot password with AuthService
    setTimeout(() => {
      this.userEmail = email;
      this.showResetForm = true;
      this.successMessage = 'Password reset instructions would be sent to your email. For demo purposes, you can now enter a new password.';
      this.isLoading = false;
    }, 1000);
  }

  onResetPassword(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (this.resetPasswordForm.invalid) {
      this.resetPasswordForm.markAllAsTouched();
      return;
    }

    // Additional validation
    const { newPassword, confirmPassword } = this.resetPasswordForm.value;
    
    if (newPassword !== confirmPassword) {
      this.errorMessage = 'Passwords do not match. Please try again.';
      return;
    }

    if (this.passwordStrength === 'weak') {
      this.errorMessage = 'Please choose a stronger password for better security.';
      return;
    }

    this.isLoading = true;

    // For now, simulate password reset functionality
    // TODO: Implement proper password reset with AuthService
    setTimeout(() => {
      this.successMessage = 'Password updated successfully! You can now login with your new password.';
      this.isLoading = false;
      
      // Clear forms and show success message
      setTimeout(() => {
        this.router.navigate(['/login']);
      }, 3000);
    }, 1000);
  }

  goBack(): void {
    this.showResetForm = false;
    this.userEmail = '';
    this.successMessage = '';
    this.errorMessage = '';
    this.resetPasswordForm.reset();
    this.passwordStrength = 'weak';
  }

  // Check if form is valid for submission
  canSubmitEmailForm(): boolean {
    return this.forgotPasswordForm.valid && !this.isLoading;
  }

  canSubmitPasswordForm(): boolean {
    return this.resetPasswordForm.valid && this.passwordStrength !== 'weak' && !this.isLoading;
  }

  // Password requirement check methods
  hasMinLength(): boolean {
    const password = this.rf['newPassword'].value;
    return password && password.length >= 8;
  }

  hasLowercase(): boolean {
    const password = this.rf['newPassword'].value;
    return password && /[a-z]/.test(password);
  }

  hasUppercase(): boolean {
    const password = this.rf['newPassword'].value;
    return password && /[A-Z]/.test(password);
  }

  hasNumber(): boolean {
    const password = this.rf['newPassword'].value;
    return password && /\d/.test(password);
  }

  hasSpecialChar(): boolean {
    const password = this.rf['newPassword'].value;
    return password && /[@$!%*?&]/.test(password);
  }
}
