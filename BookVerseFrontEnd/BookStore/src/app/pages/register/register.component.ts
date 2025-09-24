    import { Component } from '@angular/core';
    import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
    import { Router, RouterModule } from '@angular/router';
    import { CommonModule } from '@angular/common';
    import { AuthService } from '../../services/auth.service';

    @Component({
      selector: 'app-register',
      standalone: true,
      imports: [CommonModule, ReactiveFormsModule, RouterModule],
      templateUrl: './register.component.html',
      styleUrls: ['./register.component.css']
    })
    export class RegisterComponent {
      registerForm: FormGroup;
      showPassword = false;
      showConfirmPassword = false;
      isSubmitting = false;
      errorMessage = '';
      successMessage = '';

      constructor(
        private fb: FormBuilder,
        private authService: AuthService,
        private router: Router
      ) {
        this.registerForm = this.fb.group({
          fullName: ['', [Validators.required, Validators.minLength(5)]],
          username: ['', [Validators.required, Validators.minLength(4)]],
          email: ['', [Validators.required, this.emailValidator]],
          mobileNumber: ['', [Validators.required, Validators.pattern('^[0-9]{10}$')]],
          password: ['', [Validators.required, Validators.minLength(8), this.passwordValidator]],
          confirmPassword: ['', Validators.required],
          terms: [false, Validators.requiredTrue]
        }, { validator: this.passwordMatchValidator });
      }

      // Custom email validator to check format: [char]@[char].[char]
      emailValidator(control: any) {
        const email = control.value;
        if (!email) return null;
        
        // Check if email follows the pattern: [char]@[char].[char]
        // This means: characters before @, characters between @ and ., characters after .
        const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
        
        if (!emailPattern.test(email)) {
          return { invalidEmail: true };
        }
        
        return null;
      }

      // Custom password validator for complexity requirements
      passwordValidator(control: any) {
        const password = control.value;
        if (!password) return null;
        
        const hasUpperCase = /[A-Z]/.test(password);
        const hasLowerCase = /[a-z]/.test(password);
        const hasNumber = /[0-9]/.test(password);
        const hasSpecialChar = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password);
        
        if (!hasUpperCase || !hasLowerCase || !hasNumber || !hasSpecialChar) {
          return { weakPassword: true };
        }
        
        return null;
      }

      passwordMatchValidator(form: FormGroup) {
        const password = form.get('password')?.value;
        const confirmPassword = form.get('confirmPassword')?.value;
        return password === confirmPassword ? null : { mismatch: true };
      }

      togglePasswordVisibility() { this.showPassword = !this.showPassword; }
      toggleConfirmPasswordVisibility() { this.showConfirmPassword = !this.showConfirmPassword; }

      onSubmit() {
        if (this.registerForm.valid && !this.isSubmitting) {
          this.isSubmitting = true;
          this.errorMessage = '';
          this.successMessage = '';
          
          const { confirmPassword, terms, ...userData } = this.registerForm.value;
          
          this.authService.register(userData).subscribe({
            next: (user) => {
              this.successMessage = `Welcome to BookVerse, ${user.fullName}! Registration successful.`;
              setTimeout(() => {
                this.router.navigate(['/']);
              }, 2000);
            },
            error: (err) => {
              this.errorMessage = err.message || 'Registration failed. Please try again.';
              this.isSubmitting = false;
            },
            complete: () => {
              // Keep isSubmitting true to prevent multiple submissions during redirect
            }
          });
        } else {
          this.registerForm.markAllAsTouched();
        }
      }

      // Helper method to get form field errors
      getFieldError(fieldName: string): string {
        const field = this.registerForm.get(fieldName);
        if (field?.errors && field?.touched) {
          if (field.errors['required']) return `${this.getFieldDisplayName(fieldName)} is required.`;
          if (field.errors['minlength']) return `${this.getFieldDisplayName(fieldName)} must be at least ${field.errors['minlength'].requiredLength} characters.`;
          if (field.errors['pattern']) return 'Please enter a valid 10-digit mobile number.';
          if (field.errors['invalidEmail']) return 'Email must be in format: [char]@[char].[char] (e.g., user@domain.com).';
          if (field.errors['weakPassword']) return 'Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character.';
        }
        
        // Check for password mismatch
        if (fieldName === 'confirmPassword' && this.registerForm.errors?.['mismatch'] && field?.touched) {
          return 'Passwords do not match.';
        }
        
        return '';
      }

      private getFieldDisplayName(fieldName: string): string {
        const names: { [key: string]: string } = {
          fullName: 'Full name',
          username: 'Username',
          email: 'Email',
          mobileNumber: 'Mobile number',
          password: 'Password',
          confirmPassword: 'Confirm password'
        };
        return names[fieldName] || fieldName;
      }

      // Check if field has error
      hasFieldError(fieldName: string): boolean {
        const field = this.registerForm.get(fieldName);
        return !!(field?.errors && field?.touched) || 
               (fieldName === 'confirmPassword' && this.registerForm.errors?.['mismatch'] && field?.touched);
      }
    }
    