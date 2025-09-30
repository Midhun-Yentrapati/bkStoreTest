import { Component, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';


@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css']
})
export class SettingsComponent implements OnInit {
  passwordForm: FormGroup;
  preferencesForm: FormGroup;
  // Get current user information
  currentUser = computed(() => this.authService.getCurrentCustomer());
  
  isSubmittingPassword = false;
  isSubmittingPreferences = false;
  successMessage = '';
  errorMessage = '';
  showCurrentPassword = false;
  showNewPassword = false;
  showConfirmPassword = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,

  ) {
    this.passwordForm = this.createPasswordForm();
    this.preferencesForm = this.createPreferencesForm();
  }

  ngOnInit() {
    this.populatePreferencesForm();
  }

  private createPasswordForm(): FormGroup {
    return this.fb.group({
      currentPassword: ['', [Validators.required]],
      newPassword: ['', [
        Validators.required, 
        Validators.minLength(8),
        this.passwordStrengthValidator()
      ]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator.bind(this) });
  }

  private createPreferencesForm(): FormGroup {
    return this.fb.group({
      theme: ['light'],
      emailNotifications: [true],
      pushNotifications: [false],
      marketingEmails: [false],
      language: ['en'],
      currency: ['USD']
    });
  }

  private populatePreferencesForm() {
    // In real app, this would come from user preferences API
    this.preferencesForm.patchValue({
      theme: 'light',
      emailNotifications: true,
      pushNotifications: false,
      marketingEmails: false,
      language: 'en',
      currency: 'USD'
    });
  }

  // Enhanced password strength validator
  private passwordStrengthValidator(): Validators {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;
      
      const password = control.value;
      const hasUpperCase = /[A-Z]/.test(password);
      const hasLowerCase = /[a-z]/.test(password);
      const hasNumbers = /\d/.test(password);
      const hasSpecialChar = /[!@#$%^&*(),.?":{}|<>]/.test(password);
      
      const strength = [hasUpperCase, hasLowerCase, hasNumbers, hasSpecialChar]
        .filter(Boolean).length;
      
      if (strength < 3) {
        return { weakPassword: true };
      }
      
      return null;
    };
  }

  // Fixed validator function
  passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    if (!control) return null;
    
    const newPassword = control.get('newPassword')?.value;
    const confirmPassword = control.get('confirmPassword')?.value;
    
    if (!newPassword || !confirmPassword) {
      return null;
    }
    
    return newPassword === confirmPassword ? null : { mismatch: true };
  }

  // Password strength methods
  getPasswordStrengthClass(): string {
    const password = this.passwordForm.get('newPassword')?.value;
    if (!password) return '';
    
    const strength = this.calculatePasswordStrength(password);
    if (strength >= 4) return 'strength-strong';
    if (strength >= 3) return 'strength-medium';
    if (strength >= 2) return 'strength-weak';
    return 'strength-very-weak';
  }

  getPasswordStrengthText(): string {
    const password = this.passwordForm.get('newPassword')?.value;
    if (!password) return '';
    
    const strength = this.calculatePasswordStrength(password);
    if (strength >= 4) return 'Very Strong';
    if (strength >= 3) return 'Strong';
    if (strength >= 2) return 'Medium';
    return 'Weak';
  }

  private calculatePasswordStrength(password: string): number {
    let strength = 0;
    
    if (password.length >= 8) strength++;
    if (/[A-Z]/.test(password)) strength++;
    if (/[a-z]/.test(password)) strength++;
    if (/\d/.test(password)) strength++;
    if (/[!@#$%^&*(),.?":{}|<>]/.test(password)) strength++;
    
    return strength;
  }

  // Password Change
  onPasswordSubmit() {
    console.log('Password form submitted', {
      valid: this.passwordForm.valid,
      errors: this.passwordForm.errors,
      isSubmitting: this.isSubmittingPassword
    });

    // Always mark all fields as touched to show validation errors
    this.passwordForm.markAllAsTouched();

    if (this.passwordForm.valid && !this.isSubmittingPassword) {
      this.isSubmittingPassword = true;
      this.clearMessages();

      const formData = this.passwordForm.value;
      
      // Call real AuthService changePassword method
      this.authService.changePassword(formData.currentPassword, formData.newPassword).subscribe({
        next: (response) => {
          console.log('Password changed successfully:', response);
          this.successMessage = 'Password changed successfully!';
          this.isSubmittingPassword = false;
          this.passwordForm.reset();
          
          // Clear success message after 3 seconds
          setTimeout(() => {
            this.successMessage = '';
          }, 3000);
        },
        error: (error) => {
          console.error('Password change failed:', error);
          this.errorMessage = error.message || 'Password change failed. Please try again.';
          this.isSubmittingPassword = false;
          
          // Clear error message after 5 seconds
          setTimeout(() => {
            this.errorMessage = '';
          }, 5000);
        }
      });
    } else {
      // Show error message when form is invalid
      this.errorMessage = 'Please fix the form errors before submitting.';
      console.log('Form is invalid:', this.passwordForm.errors);
      
      // Clear error message after 3 seconds
      setTimeout(() => {
        this.errorMessage = '';
      }, 3000);
    }
  }

  // Preferences Update
  onPreferencesSubmit() {
    console.log('Preferences form submitted', {
      valid: this.preferencesForm.valid,
      values: this.preferencesForm.value,
      isSubmitting: this.isSubmittingPreferences
    });

    if (this.preferencesForm.valid && !this.isSubmittingPreferences) {
      this.isSubmittingPreferences = true;
      this.clearMessages();

      // Theme is now fixed to light mode

      // Simulate API call for other preferences
      setTimeout(() => {
        console.log('Preferences updated:', this.preferencesForm.value);
        this.successMessage = 'Preferences updated successfully!';
        this.isSubmittingPreferences = false;
        
        // Clear success message after 3 seconds
        setTimeout(() => {
          this.successMessage = '';
        }, 3000);
      }, 1000);
    } else {
      this.preferencesForm.markAllAsTouched();
    }
  }

  // Toggle password visibility
  togglePasswordVisibility(field: 'current' | 'new' | 'confirm') {
    console.log('Toggling password visibility for:', field);
    switch (field) {
      case 'current':
        this.showCurrentPassword = !this.showCurrentPassword;
        break;
      case 'new':
        this.showNewPassword = !this.showNewPassword;
        break;
      case 'confirm':
        this.showConfirmPassword = !this.showConfirmPassword;
        break;
    }
  }

  // Utility methods
  private clearMessages() {
    this.successMessage = '';
    this.errorMessage = '';
  }

  getPasswordFieldError(fieldName: string): string {
    const field = this.passwordForm.get(fieldName);
    if (field?.errors && field?.touched) {
      if (field.errors['required']) return `${this.getFieldDisplayName(fieldName)} is required.`;
      if (field.errors['minlength']) return `${this.getFieldDisplayName(fieldName)} must be at least ${field.errors['minlength'].requiredLength} characters.`;
      if (field.errors['weakPassword']) return 'Password must contain uppercase, lowercase, numbers, and special characters.';
    }
    
    if (fieldName === 'confirmPassword' && this.passwordForm.errors?.['mismatch'] && field?.touched) {
      return 'Passwords do not match.';
    }
    
    return '';
  }

  private getFieldDisplayName(fieldName: string): string {
    const names: { [key: string]: string } = {
      currentPassword: 'Current password',
      newPassword: 'New password',
      confirmPassword: 'Confirm password'
    };
    return names[fieldName] || fieldName;
  }

  hasPasswordFieldError(fieldName: string): boolean {
    const field = this.passwordForm.get(fieldName);
    return !!(field?.errors && field?.touched) || 
           (fieldName === 'confirmPassword' && this.passwordForm.errors?.['mismatch'] && field?.touched);
  }

  // Check if password form is valid for debugging
  isPasswordFormValid(): boolean {
    console.log('Password form validity check:', {
      valid: this.passwordForm.valid,
      errors: this.passwordForm.errors,
      controls: {
        currentPassword: {
          valid: this.passwordForm.get('currentPassword')?.valid,
          errors: this.passwordForm.get('currentPassword')?.errors,
          value: this.passwordForm.get('currentPassword')?.value
        },
        newPassword: {
          valid: this.passwordForm.get('newPassword')?.valid,
          errors: this.passwordForm.get('newPassword')?.errors,
          value: this.passwordForm.get('newPassword')?.value
        },
        confirmPassword: {
          valid: this.passwordForm.get('confirmPassword')?.valid,
          errors: this.passwordForm.get('confirmPassword')?.errors,
          value: this.passwordForm.get('confirmPassword')?.value
        }
      }
    });
    return this.passwordForm.valid;
  }

  // Account deletion (mock)
  deleteAccount() {
    if (confirm('Are you sure you want to delete your account? This action cannot be undone.')) {
      if (confirm('This will permanently delete all your data. Are you absolutely sure?')) {
        console.log('Account deletion requested');
        this.errorMessage = 'Account deletion requested. In a real app, this would be processed.';
        
        // Clear message after 5 seconds
        setTimeout(() => {
          this.errorMessage = '';
        }, 5000);
      }
    }
  }

  // Test button to verify click events are working
  testButtonClick() {
    console.log('Test button clicked! Component is working correctly.');
    alert('Test button clicked! The component is working correctly.');
    this.successMessage = 'Test button clicked successfully!';
    
    setTimeout(() => {
      this.successMessage = '';
    }, 2000);
  }

  // Test preferences button
  testPreferencesClick() {
    console.log('Test preferences button clicked!', this.preferencesForm.value);
    alert('Preferences test clicked! Check console for values.');
    this.successMessage = 'Preferences test successful!';
    
    setTimeout(() => {
      this.successMessage = '';
    }, 2000);
  }
} 