import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  loginForm: FormGroup;
  showPassword = false;
  private returnUrl: string = '/';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.loginForm = this.fb.group({
      identifier: ['', [Validators.required]],
      password: ['', [Validators.required]]
    });

    // Get the return URL from query parameters or default to '/'
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
  }

  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }

  onSubmit() {
    if (this.loginForm.valid) {
      console.log('Login form submitted with:', this.loginForm.value);
      console.log('Return URL:', this.returnUrl);
      
      this.authService.login(this.loginForm.value).subscribe({
        next: (user) => {
          console.log('Login successful, user received:', user);
          console.log('Navigating to:', this.returnUrl);
          
          // Check if user is admin and redirect accordingly
          if (user && user.userRole && user.userRole !== 'CUSTOMER') {
            console.log('Admin user detected, redirecting to admin dashboard');
            this.router.navigate(['/admin/dashboard']);
          } else {
            console.log('Customer user detected, redirecting to:', this.returnUrl);
            this.router.navigate([this.returnUrl]);
          }
        },
        error: (err) => {
          console.error('Login failed:', err);
          alert('Login failed: ' + err.message);
        }
      });
    } else {
      console.log('Form is invalid:', this.loginForm.errors);
      this.loginForm.markAllAsTouched();
    }
  }
}
