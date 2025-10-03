import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../services/auth.service';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-jwt-test',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="jwt-test-container">
      <h2>JWT Authentication Test</h2>
      
      <div class="test-section">
        <h3>Current Authentication Status</h3>
        <p><strong>Admin User:</strong> {{ authService.currentAdmin()?.username || 'Not logged in' }}</p>
        <p><strong>Customer User:</strong> {{ authService.currentCustomer()?.fullName || 'Not logged in' }}</p>
        <p><strong>Token in Storage:</strong> {{ hasToken ? 'Yes' : 'No' }}</p>
        <p><strong>Token Preview:</strong> {{ tokenPreview }}</p>
      </div>

      <div class="test-section">
        <h3>API Tests</h3>
        <button (click)="testPublicEndpoint()" class="test-btn">Test Public Endpoint</button>
        <button (click)="testAuthEndpoint()" class="test-btn">Test Auth Context</button>
        <button (click)="testJwtClaims()" class="test-btn">Test JWT Claims</button>
        <button (click)="testAdminEndpoint()" class="test-btn">Test Admin Endpoint</button>
      </div>

      <div class="test-section">
        <h3>Test Results</h3>
        <div class="results" [innerHTML]="testResults"></div>
      </div>
    </div>
  `,
  styles: [`
    .jwt-test-container {
      padding: 20px;
      max-width: 800px;
      margin: 0 auto;
    }
    
    .test-section {
      margin-bottom: 30px;
      padding: 20px;
      border: 1px solid #ddd;
      border-radius: 8px;
      background: #f9f9f9;
    }
    
    .test-btn {
      margin: 5px;
      padding: 10px 20px;
      background: #007bff;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
    }
    
    .test-btn:hover {
      background: #0056b3;
    }
    
    .results {
      background: #fff;
      padding: 15px;
      border-radius: 4px;
      border: 1px solid #ddd;
      font-family: monospace;
      white-space: pre-wrap;
      max-height: 400px;
      overflow-y: auto;
    }
  `]
})
export class JwtTestComponent {
  authService = inject(AuthService);
  private http = inject(HttpClient);
  
  testResults = '';

  get hasToken(): boolean {
    return !!localStorage.getItem('bookverse_token') || 
           !!localStorage.getItem('bookverse_admin_token') ||
           !!localStorage.getItem('bookverse_customer_token');
  }

  get tokenPreview(): string {
    const token = localStorage.getItem('bookverse_token') || 
                 localStorage.getItem('bookverse_admin_token') ||
                 localStorage.getItem('bookverse_customer_token');
    return token ? `${token.substring(0, 20)}...` : 'None';
  }

  testPublicEndpoint() {
    this.addResult('Testing public endpoint...');
    this.http.get('http://localhost:8090/api/test/health').subscribe({
      next: (response) => {
        this.addResult('✅ Public endpoint success:', response);
      },
      error: (error) => {
        this.addResult('❌ Public endpoint failed:', error.message);
      }
    });
  }
  
  testAuthEndpoint() {
    this.addResult('Testing auth context endpoint...');
    this.http.get('http://localhost:8090/api/users/profile').subscribe({
      next: (response) => {
        this.addResult('✅ Auth context success:', response);
      },
      error: (error) => {
        this.addResult('❌ Auth context failed:', error.message);
      }
    });
  }
  
  testJwtClaims() {
    this.addResult('Testing JWT claims...');
    const token = localStorage.getItem('bookverse_token') || 
                 localStorage.getItem('bookverse_admin_token') ||
                 localStorage.getItem('bookverse_customer_token');
    
    if (!token) {
      this.addResult('❌ No token found in storage');
      return;
    }

    try {
      const parts = token.split('.');
      if (parts.length !== 3) {
        this.addResult('❌ Invalid token format');
        return;
      }
      
      const payload = JSON.parse(atob(parts[1]));
      this.addResult('✅ JWT claims extracted successfully:', payload);
    } catch (error) {
      this.addResult('❌ Failed to decode JWT:', error);
    }
  }
  
  testAdminEndpoint() {
    this.addResult('Testing admin endpoint...');
    this.http.get('http://localhost:8090/api/admin/users').subscribe({
      next: (response) => {
        this.addResult('✅ Admin endpoint success:', response);
      },
      error: (error) => {
        this.addResult('❌ Admin endpoint failed:', error.message);
      }
    });
  }
  
  private addResult(message: string, data?: any) {
    const timestamp = new Date().toLocaleTimeString();
    let formattedData = '';
    if (data) {
      formattedData = typeof data === 'string' ? data : JSON.stringify(data, null, 2);
    }
    this.testResults += `[${timestamp}] ${message}${formattedData ? '\n' + formattedData : ''}\n\n`;
  }
}
