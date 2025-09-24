// src/app/admin-login/admin-login.component.ts

import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-admin-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './admin-login.component.html',
  styleUrls: ['./admin-login.component.css']
})
export class AdminLoginComponent implements OnInit {
  loginForm!: FormGroup;
  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [
        Validators.required,
        Validators.minLength(8), // Minimum 8 characters
        Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/) // At least one uppercase, one lowercase, one number, one special character
      ]]
    });
  }

  /**
   * Getter for easy access to form fields in the template.
   */
  get f() {
    return this.loginForm.controls;
  }


  onSubmit(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;

    const { email, password } = this.loginForm.value;

    this.authService.login(email, password).subscribe({
      next: (user) => {
        this.isLoading = false;
        if (user) {
          this.successMessage = 'Login successful! Redirecting...';
          console.log('Login successful!');

          // Store the username in sessionStorage
          sessionStorage.setItem('loggedInUsername', user.username);
          console.log('Username stored in sessionStorage:', user.username);

          setTimeout(() => {
            this.router.navigate(['/admin-main']);
          }, 1000);
        }
      },
      error: (err: { message: string; }) => {
        this.isLoading = false;
        this.errorMessage = err.message || 'An unexpected error occurred.';
        console.error('Login error:', err);
      }
    });
  }
}
