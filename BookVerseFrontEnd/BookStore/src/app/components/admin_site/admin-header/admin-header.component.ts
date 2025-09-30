import { CommonModule } from '@angular/common';
import { Component, Input, Output, EventEmitter, inject } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { AdminNotificationsComponent } from '../admin-notifications/admin-notifications.component';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-admin-header',
  standalone: true,
  imports: [CommonModule, AdminNotificationsComponent],
  templateUrl: './admin-header.component.html',
  styleUrl: './admin-header.component.css'
})
export class AdminHeaderComponent {
  @Input() username: string = '';

  @Output() logoutClicked = new EventEmitter<void>();

  showUsername: boolean = true;
  showBackIcon: boolean = false;

  private authService = inject(AuthService);

  constructor(private router: Router) {
    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        const url = event.urlAfterRedirects;
        this.showUsername = !url.includes('admin-login');
        this.showBackIcon = !(url.includes('admin-login') || url.includes('admin-main'));
      }
    });
  }

  goBack(): void {
    // Try to go back, fallback to /admin-main if not possible
    if (window.history.length > 1) {
      window.history.back();
    } else {
      this.router.navigate(['/admin-main']);
    }
  }

  navigateToAdminDashboard(): void {
    console.log('Navigating to admin dashboard');
    this.router.navigate(['/admin-main']);
  }



  onLogoutClick(): void {
    console.log('Admin logout clicked');
    
    // Emit logout event for parent components
    this.logoutClicked.emit();
    
    // Use AuthService for proper logout with backend session termination
    this.authService.logoutAdmin();
  }
}

