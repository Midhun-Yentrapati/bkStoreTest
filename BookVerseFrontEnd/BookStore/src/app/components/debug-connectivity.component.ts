import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-debug-connectivity',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="debug-panel p-4 bg-gray-100 rounded-lg">
      <h3 class="text-lg font-bold mb-4">Backend Connectivity Debug</h3>
      
      <div class="space-y-4">
        <button 
          (click)="testEurekaServer()" 
          class="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600">
          Test Eureka Server (8761)
        </button>
        
        <button 
          (click)="testApiGateway()" 
          class="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600">
          Test API Gateway (8090)
        </button>
        
        <button 
          (click)="testAuthService()" 
          class="bg-purple-500 text-white px-4 py-2 rounded hover:bg-purple-600">
          Test Auth Service via Gateway
        </button>
        
        <button 
          (click)="testDirectAuthService()" 
          class="bg-orange-500 text-white px-4 py-2 rounded hover:bg-orange-600">
          Test Auth Service Direct (8081)
        </button>
        
        <button 
          (click)="testBookCatalogService()" 
          class="bg-teal-500 text-white px-4 py-2 rounded hover:bg-teal-600">
          Test Book Catalog Service
        </button>
      </div>
      
      <div class="mt-6">
        <h4 class="font-semibold mb-2">Test Results:</h4>
        <div class="bg-black text-green-400 p-3 rounded font-mono text-sm max-h-96 overflow-y-auto">
          <div *ngFor="let log of logs">{{ log }}</div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .debug-panel {
      max-width: 800px;
      margin: 20px auto;
    }
  `]
})
export class DebugConnectivityComponent {
  private authService = inject(AuthService);
  private http = inject(HttpClient);
  
  logs: string[] = [];

  private addLog(message: string) {
    const timestamp = new Date().toLocaleTimeString();
    this.logs.push(`[${timestamp}] ${message}`);
    console.log(message);
  }

  testEurekaServer() {
    this.addLog('🔍 Testing Eureka Server...');
    this.http.get('http://localhost:8761/actuator/health').subscribe({
      next: (response) => {
        this.addLog(`✅ Eureka Server is UP: ${JSON.stringify(response)}`);
      },
      error: (error) => {
        this.addLog(`❌ Eureka Server test failed: ${error.message}`);
      }
    });
  }

  testApiGateway() {
    this.addLog('🔍 Testing API Gateway...');
    this.http.get('http://localhost:8090/gateway/api/endpoints').subscribe({
      next: (response) => {
        this.addLog(`✅ API Gateway is UP: ${JSON.stringify(response)}`);
      },
      error: (error) => {
        this.addLog(`❌ API Gateway test failed: ${error.message}`);
      }
    });
  }

  testAuthService() {
    this.addLog('🔍 Testing Auth Service via API Gateway...');
    this.http.get('http://localhost:8090/api/test/health').subscribe({
      next: (response) => {
        this.addLog(`✅ Auth Service via Gateway is UP: ${JSON.stringify(response)}`);
      },
      error: (error) => {
        this.addLog(`❌ Auth Service via Gateway test failed: ${error.message}`);
      }
    });
  }

  testDirectAuthService() {
    this.addLog('🔍 Testing Auth Service directly...');
    this.http.get('http://localhost:8081/api/test/health').subscribe({
      next: (response) => {
        this.addLog(`✅ Auth Service direct is UP: ${JSON.stringify(response)}`);
      },
      error: (error) => {
        this.addLog(`❌ Auth Service direct test failed: ${error.message}`);
      }
    });
  }

  testBookCatalogService() {
    this.addLog('🔍 Testing Book Catalog Service...');
    this.http.get('http://localhost:8090/api/books/health').subscribe({
      next: (response) => {
        this.addLog(`✅ Book Catalog Service is UP: ${JSON.stringify(response)}`);
      },
      error: (error) => {
        this.addLog(`❌ Book Catalog Service test failed: ${error.message}`);
      }
    });
  }
}
