import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AnalyticsService, DailySalesSummary, AdminActivityLog, AnalyticsDashboard } from '../../../services/analytics.service';
import { Subscription } from 'rxjs';
import {
  Chart,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  BarController,
  LineController,
  Title,
  Tooltip,
  Legend,
  Filler
} from 'chart.js';

@Component({
  selector: 'app-analytics-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './analytics-dashboard.component.html',
  styleUrl: './analytics-dashboard.component.css'
})
export class AnalyticsDashboardComponent implements OnInit, OnDestroy, AfterViewInit {
  @ViewChild('revenueChart', { static: false }) revenueChartCanvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('activityChart', { static: false }) activityChartCanvas!: ElementRef<HTMLCanvasElement>;

  // Analytics Data
  analyticsDashboard: AnalyticsDashboard | null = null;
  recentSalesSummaries: DailySalesSummary[] = [];
  recentActivities: AdminActivityLog[] = [];
  topActiveAdmins: any[] = [];
  commonActions: any[] = [];
  errorLogs: AdminActivityLog[] = [];

  // Date Range Selection
  selectedStartDate: string = '';
  selectedEndDate: string = '';
  selectedPeriod: string = 'last7days';

  // Loading States
  isLoadingDashboard = false;
  isLoadingActivities = false;
  isLoadingStats = false;

  // Charts
  revenueChart: any = null;
  activityChart: any = null;

  // Subscriptions
  private subscriptions: Subscription[] = [];

  constructor(private analyticsService: AnalyticsService) {
    // Register Chart.js components
    Chart.register(
      CategoryScale,
      LinearScale,
      PointElement,
      LineElement,
      BarElement,
      BarController,
      LineController,
      Title,
      Tooltip,
      Legend,
      Filler
    );

    // Set default date range (last 7 days)
    this.setDateRange();
  }

  ngOnInit(): void {
    this.loadAnalyticsDashboard();
    this.loadRecentActivities();
    this.loadActivityStats();
  }

  ngAfterViewInit(): void {
    // Initialize charts after view is initialized
    setTimeout(() => {
      this.initializeCharts();
    }, 100);
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
    if (this.revenueChart) {
      this.revenueChart.destroy();
    }
    if (this.activityChart) {
      this.activityChart.destroy();
    }
  }

  setDateRange(): void {
    const today = new Date();
    const endDate = today.toISOString().split('T')[0];
    
    let startDate: Date;
    switch (this.selectedPeriod) {
      case 'last7days':
        startDate = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000);
        break;
      case 'last30days':
        startDate = new Date(today.getTime() - 30 * 24 * 60 * 60 * 1000);
        break;
      case 'last90days':
        startDate = new Date(today.getTime() - 90 * 24 * 60 * 60 * 1000);
        break;
      case 'thisMonth':
        startDate = new Date(today.getFullYear(), today.getMonth(), 1);
        break;
      case 'lastMonth':
        startDate = new Date(today.getFullYear(), today.getMonth() - 1, 1);
        const lastMonth = new Date(today.getFullYear(), today.getMonth(), 0);
        this.selectedEndDate = lastMonth.toISOString().split('T')[0];
        break;
      default:
        startDate = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000);
    }

    this.selectedStartDate = startDate.toISOString().split('T')[0];
    if (this.selectedPeriod !== 'lastMonth') {
      this.selectedEndDate = endDate;
    }
  }

  onPeriodChange(): void {
    this.setDateRange();
    this.refreshData();
  }

  onCustomDateChange(): void {
    if (this.selectedStartDate && this.selectedEndDate) {
      this.selectedPeriod = 'custom';
      this.refreshData();
    }
  }

  refreshData(): void {
    this.loadAnalyticsDashboard();
    this.loadSalesSummariesByDateRange();
    this.updateCharts();
  }

  loadAnalyticsDashboard(): void {
    this.isLoadingDashboard = true;
    const subscription = this.analyticsService.getAnalyticsDashboard(
      this.selectedStartDate, 
      this.selectedEndDate
    ).subscribe({
      next: (dashboard) => {
        this.analyticsDashboard = dashboard;
        this.isLoadingDashboard = false;
      },
      error: (error) => {
        console.error('Error loading analytics dashboard:', error);
        this.isLoadingDashboard = false;
      }
    });
    this.subscriptions.push(subscription);
  }

  loadSalesSummariesByDateRange(): void {
    const subscription = this.analyticsService.getDailySalesSummariesByDateRange(
      this.selectedStartDate, 
      this.selectedEndDate
    ).subscribe({
      next: (summaries) => {
        this.recentSalesSummaries = summaries;
        this.updateRevenueChart();
      },
      error: (error) => {
        console.error('Error loading sales summaries:', error);
      }
    });
    this.subscriptions.push(subscription);
  }

  loadRecentActivities(): void {
    this.isLoadingActivities = true;
    const subscription = this.analyticsService.getRecentActivities().subscribe({
      next: (activities) => {
        this.recentActivities = activities;
        this.isLoadingActivities = false;
        this.updateActivityChart();
      },
      error: (error) => {
        console.error('Error loading recent activities:', error);
        this.isLoadingActivities = false;
      }
    });
    this.subscriptions.push(subscription);
  }

  loadActivityStats(): void {
    this.isLoadingStats = true;
    
    // Load multiple stats in parallel
    const topAdminsSubscription = this.analyticsService.getTopActiveAdmins().subscribe({
      next: (admins) => this.topActiveAdmins = admins,
      error: (error) => console.error('Error loading top admins:', error)
    });

    const commonActionsSubscription = this.analyticsService.getMostCommonActions().subscribe({
      next: (actions) => this.commonActions = actions,
      error: (error) => console.error('Error loading common actions:', error)
    });

    const errorLogsSubscription = this.analyticsService.getLogsWithErrors().subscribe({
      next: (logs) => {
        this.errorLogs = logs;
        this.isLoadingStats = false;
      },
      error: (error) => {
        console.error('Error loading error logs:', error);
        this.isLoadingStats = false;
      }
    });

    this.subscriptions.push(topAdminsSubscription, commonActionsSubscription, errorLogsSubscription);
  }

  initializeCharts(): void {
    this.initializeRevenueChart();
    this.initializeActivityChart();
  }

  initializeRevenueChart(): void {
    if (this.revenueChartCanvas && this.revenueChartCanvas.nativeElement) {
      const ctx = this.revenueChartCanvas.nativeElement.getContext('2d');
      if (ctx) {
        this.revenueChart = new Chart(ctx, {
          type: 'line',
          data: {
            labels: [],
            datasets: [{
              label: 'Daily Revenue',
              data: [],
              borderColor: '#4F46E5',
              backgroundColor: 'rgba(79, 70, 229, 0.1)',
              fill: true,
              tension: 0.4
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              title: {
                display: true,
                text: 'Revenue Trends'
              },
              legend: {
                display: true
              }
            },
            scales: {
              y: {
                beginAtZero: true,
                ticks: {
                  callback: function(value) {
                    return '₹' + value.toLocaleString();
                  }
                }
              }
            }
          }
        });
      }
    }
  }

  initializeActivityChart(): void {
    if (this.activityChartCanvas && this.activityChartCanvas.nativeElement) {
      const ctx = this.activityChartCanvas.nativeElement.getContext('2d');
      if (ctx) {
        this.activityChart = new Chart(ctx, {
          type: 'bar',
          data: {
            labels: [],
            datasets: [{
              label: 'Admin Activities',
              data: [],
              backgroundColor: '#10B981',
              borderColor: '#059669',
              borderWidth: 1
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              title: {
                display: true,
                text: 'Admin Activity Distribution'
              }
            },
            scales: {
              y: {
                beginAtZero: true
              }
            }
          }
        });
      }
    }
  }

  updateCharts(): void {
    this.updateRevenueChart();
    this.updateActivityChart();
  }

  updateRevenueChart(): void {
    if (this.revenueChart && this.recentSalesSummaries.length > 0) {
      const labels = this.recentSalesSummaries.map(summary => 
        new Date(summary.salesDate).toLocaleDateString()
      );
      const data = this.recentSalesSummaries.map(summary => summary.totalRevenue);

      this.revenueChart.data.labels = labels;
      this.revenueChart.data.datasets[0].data = data;
      this.revenueChart.update();
    }
  }

  updateActivityChart(): void {
    if (this.activityChart && this.commonActions.length > 0) {
      const labels = this.commonActions.map(action => action[0]); // Action type
      const data = this.commonActions.map(action => action[1]); // Count

      this.activityChart.data.labels = labels;
      this.activityChart.data.datasets[0].data = data;
      this.activityChart.update();
    }
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('en-IN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }

  formatDateTime(dateString: string): string {
    return new Date(dateString).toLocaleString('en-IN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR'
    }).format(amount);
  }

  getStatusBadgeClass(status: string): string {
    switch (status.toLowerCase()) {
      case 'success':
        return 'badge-success';
      case 'error':
      case 'failed':
        return 'badge-error';
      case 'warning':
        return 'badge-warning';
      default:
        return 'badge-info';
    }
  }

  getActionTypeIcon(actionType: string): string {
    switch (actionType.toLowerCase()) {
      case 'login':
        return '🔐';
      case 'logout':
        return '🚪';
      case 'create':
        return '➕';
      case 'update':
        return '✏️';
      case 'delete':
        return '🗑️';
      case 'view':
        return '👁️';
      default:
        return '📋';
    }
  }
} 