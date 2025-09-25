import { Component, EventEmitter, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-login-prompt',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login-prompt.component.html',
  styleUrls: ['./login-prompt.component.css']
})
export class LoginPromptComponent {
  @Output() close = new EventEmitter<void>();
  loginForm: FormGroup;
  successMessage: string = '';
  errorMessage: string = '';
  isLoading: boolean = false;

  constructor(private fb: FormBuilder, private authService: AuthService) {
    this.loginForm = this.fb.group({
      identifier: ['', [Validators.required, Validators.minLength(3), Validators.pattern(/^[a-zA-Z0-9@._-]+$/)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
    });
  }

  onClose() {
    this.close.emit();
  }

  onSubmit() {
    if (this.loginForm.valid) {
      this.isLoading = true;
      this.successMessage = '';
      this.errorMessage = '';

      // The subscribe call will now work correctly because authService.login returns an Observable.
      this.authService.login(this.loginForm.value).subscribe({
        next: () => {
          this.successMessage = 'Login successful!';
          this.isLoading = false;
          setTimeout(() => {
            this.onClose(); // Close the prompt on successful login
          }, 1000);
        },
        error: (err) => {
          this.errorMessage = err.message || 'Login failed. Please try again.';
          this.isLoading = false;
        }
      });
    } else {
      this.errorMessage = 'Please fill in all fields correctly.';
      this.markFormGroupTouched();
    }
  }

  markFormGroupTouched(): void {
    Object.keys(this.loginForm.controls).forEach(key => {
      const control = this.loginForm.get(key);
      control?.markAsTouched();
    });
  }
}
