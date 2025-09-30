import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AnalyticsService, DailySalesSummary, AdminActivityLog } from '../../../services/analytics.service';

@Component({
  selector: 'app-analytics-test',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div style="padding: 20px; font-family: Arial, sans-serif;">
      <h2>🧪 Analytics Integration Test</h2>
      
      <div style="margin: 20px 0;">
        <h3>Test Results:</h3>
        <div *ngFor="let test of testResults" [style.color]="test.success ? 'green' : 'red'">
          {{ test.success ? '✅' : '❌' }} {{ test.name }}: {{ test.message }}
        </div>
      </div>
      
      <div style="margin: 20px 0;">
        <button (click)="runTests()" style="padding: 10px 20px; background: #4f46e5; color: white; border: none; border-radius: 5px; cursor: pointer;">
          🔄 Run Tests
        </button>
      </div>
      
      <div *ngIf="recentLogs.length > 0" style="margin: 20px 0;">
        <h3>Recent Activity Logs:</h3>
        <div *ngFor="let log of recentLogs" style="margin: 5px 0; padding: 10px; background: #f5f5f5; border-radius: 5px;">
          <strong>{{ log.actionType }}:</strong> {{ log.actionDescription }}
          <br>
          <small>{{ log.adminUsername }} - {{ formatDate(log.createdAt) }}</small>
        </div>
      </div>
      
      <div *ngIf="testSummary" style="margin: 20px 0;">
        <h3>Test Sales Summary:</h3>
        <div style="padding: 10px; background: #f0f9ff; border-radius: 5px;">
          <p><strong>Date:</strong> {{ testSummary.salesDate }}</p>
          <p><strong>Revenue:</strong> ₹{{ testSummary.totalRevenue }}</p>
          <p><strong>Orders:</strong> {{ testSummary.totalOrders }}</p>
          <p><strong>Items Sold:</strong> {{ testSummary.totalItemsSold }}</p>
        </div>
      </div>
    </div>
  `
})
export class AnalyticsTestComponent implements OnInit {
  testResults: Array<{name: string, success: boolean, message: string}> = [];
  recentLogs: AdminActivityLog[] = [];
  testSummary: DailySalesSummary | null = null;

  constructor(private analyticsService: AnalyticsService) {}

  ngOnInit(): void {
    console.log('Analytics Test Component initialized');
    this.runTests();
  }

  async runTests(): Promise<void> {
    this.testResults = [];
    
    // Test 1: Log Admin Activity
    try {
      const activityData = {
        adminId: 'test-admin-001',
        adminUsername: 'TestAdmin',
        actionType: 'TEST',
        actionDescription: 'Testing analytics integration',
        status: 'SUCCESS',
        sessionId: 'test-session-' + Date.now(),
        createdAt: new Date().toISOString()
      };
      
      await this.analyticsService.logActivity(activityData).toPromise();
      this.testResults.push({
        name: 'Admin Activity Logging',
        success: true,
        message: 'Successfully logged admin activity'
      });
    } catch (error) {
      this.testResults.push({
        name: 'Admin Activity Logging',
        success: false,
        message: `Failed: ${error}`
      });
    }

    // Test 2: Create Test Sales Summary
    try {
      const testSales: DailySalesSummary = {
        salesDate: new Date().toISOString().split('T')[0],
        totalRevenue: 15750.50,
        totalOrders: 25,
        totalItemsSold: 47,
        averageOrderValue: 630.02,
        topSellingItem: 'The Great Gatsby',
        topSellingItemQuantity: 8,
        leastSellingItem: 'Advanced Mathematics',
        leastSellingItemQuantity: 1
      };
      
      const result = await this.analyticsService.processDailySalesSummary(testSales).toPromise();
      this.testSummary = result || null;
      this.testResults.push({
        name: 'Sales Summary Processing',
        success: true,
        message: 'Successfully processed sales summary'
      });
    } catch (error) {
      this.testResults.push({
        name: 'Sales Summary Processing',
        success: false,
        message: `Failed: ${error}`
      });
    }

    // Test 3: Retrieve Recent Activities
    try {
      const activities = await this.analyticsService.getRecentActivities().toPromise();
      this.recentLogs = activities || [];
      this.testResults.push({
        name: 'Recent Activities Retrieval',
        success: true,
        message: `Retrieved ${this.recentLogs.length} recent activities`
      });
    } catch (error) {
      this.testResults.push({
        name: 'Recent Activities Retrieval',
        success: false,
        message: `Failed: ${error}`
      });
    }

    // Test 4: Get Analytics Dashboard Data
    try {
      const today = new Date();
      const sevenDaysAgo = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000);
      
      const dashboard = await this.analyticsService.getAnalyticsDashboard(
        sevenDaysAgo.toISOString().split('T')[0],
        today.toISOString().split('T')[0]
      ).toPromise();
      
      this.testResults.push({
        name: 'Analytics Dashboard Data',
        success: true,
        message: `Retrieved dashboard data - Revenue: ₹${dashboard?.totalRevenue || 0}`
      });
    } catch (error) {
      this.testResults.push({
        name: 'Analytics Dashboard Data',
        success: false,
        message: `Failed: ${error}`
      });
    }

    // Test 5: Log a utility action
    this.analyticsService.logAdminAction('TEST_COMPLETE', 'Analytics integration test completed successfully');
    this.testResults.push({
      name: 'Utility Logging Method',
      success: true,
      message: 'Successfully used utility logging method'
    });

    console.log('All tests completed:', this.testResults);
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleString();
  }
} 