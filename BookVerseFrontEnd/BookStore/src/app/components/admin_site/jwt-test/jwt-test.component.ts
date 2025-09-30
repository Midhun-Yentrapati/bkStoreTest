import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AuthService } from '../../../services/auth.service';

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

  
  testResults = '';
  hasToken = false;
  tokenPreview = '';
  
  ngOnInit() {
    this.checkTokenStatus();
  }
  
  private checkTokenStatus() {
    if (typeof localStorage !== 'undefined') {
      const token = localStorage.getItem('bookverse_token');
      this.hasToken = !!token;
      if (token) {
        // Show first and last 10 characters of token
        this.tokenPreview = token.substring(0, 10) + '...' + token.substring(token.length - 10);
      }
    }
  }
  
  testPublicEndpoint() {
    this.addResult('Testing public endpoint...');
    // Mock implementation - TODO: Use AuthService
    setTimeout(() => {
      this.addResult('✅ Public endpoint success (mock):', JSON.stringify({status: 'UP', service: 'auth-service'}, null, 2));
    }, 500);
  }
  
  testAuthEndpoint() {
    this.addResult('Testing auth context endpoint...');
    // Mock implementation - TODO: Use AuthService
    setTimeout(() => {
      this.addResult('✅ Auth context success (mock):', JSON.stringify({authenticated: true, user: 'test-user'}, null, 2));
    }, 500);
  }
  
  testJwtClaims() {
    this.addResult('Testing JWT claims endpoint...');
    // Mock implementation - TODO: Use AuthService
    setTimeout(() => {
      this.addResult('✅ JWT claims success (mock):', JSON.stringify({sub: 'user-id', userRole: 'ADMIN', exp: Date.now()}, null, 2));
    }, 500);
  }
  
  testAdminEndpoint() {
    this.addResult('Testing admin endpoint...');
    // Mock implementation - TODO: Use AuthService
    setTimeout(() => {
      this.addResult('✅ Admin endpoint success (mock):', JSON.stringify({customers: [], total: 0}, null, 2));
    }, 500);
  }
  
  private addResult(message: string, data?: string) {
    const timestamp = new Date().toLocaleTimeString();
    this.testResults += `[${timestamp}] ${message}\n`;
    if (data) {
      this.testResults += `${data}\n`;
    }
    this.testResults += '\n';
  }
}
