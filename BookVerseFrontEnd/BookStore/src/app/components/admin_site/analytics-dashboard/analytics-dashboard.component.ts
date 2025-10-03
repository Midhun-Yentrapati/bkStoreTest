import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AnalyticsService, AnalyticsDashboard, TopSellingBook } from '../../../services/analytics.service';
import { AdminDashboardCardsComponent } from '../admin-dashboard-cards/admin-dashboard-cards.component';
import { YearlySalesChartComponent } from '../yearly-sales-chart/yearly-sales-chart.component';
import { forkJoin } from 'rxjs';

// Interface for the card data (used for UI display)
interface DashboardCard {
  title: string;
  value: number;
  description: string;
  isCurrency: boolean;
  iconClass: string;
  colorClass: string;
}

@Component({
  selector: 'app-analytics-dashboard',
  standalone: true,
  // Add DatePipe and DecimalPipe for template formatting
  imports: [CommonModule, FormsModule, AdminDashboardCardsComponent, YearlySalesChartComponent, DecimalPipe, DatePipe], 
  templateUrl: './analytics-dashboard.component.html',
  styleUrls: ['./analytics-dashboard.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AnalyticsDashboardComponent implements OnInit {
  
  isLoading = true;
  selectedDateRange = 'last-30-days'; // Default range
  startDate: string = this.getStartDate(this.selectedDateRange);
  endDate: string = this.getEndDate();

  dashboardData: AnalyticsDashboard | null = null;
  topSellingBooks: TopSellingBook[] = [];
  leastSellingBooks: TopSellingBook[] = [];

  // Initialized cards for the numbered statistics
  cards: DashboardCard[] = [
    { title: 'Total Revenue', value: 0, description: 'Revenue in selected period', isCurrency: true, iconClass: 'fas fa-wallet', colorClass: 'bg-green-100 text-green-800' },
    { title: 'Total Orders', value: 0, description: 'Number of orders placed', isCurrency: false, iconClass: 'fas fa-shopping-bag', colorClass: 'bg-blue-100 text-blue-800' },
    { title: 'Items Sold', value: 0, description: 'Total quantity of books sold', isCurrency: false, iconClass: 'fas fa-book', colorClass: 'bg-yellow-100 text-yellow-800' },
    { title: 'Avg Order Value', value: 0, description: 'Average value of an order', isCurrency: true, iconClass: 'fas fa-dollar-sign', colorClass: 'bg-red-100 text-red-800' },
  ];

  constructor(private analyticsService: AnalyticsService) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  /**
   * Called when the date range selection changes.
   */
  onDateRangeChange(): void {
    this.startDate = this.getStartDate(this.selectedDateRange);
    this.endDate = this.getEndDate();
    this.loadDashboardData();
  }

  /**
   * Fetches data for the dashboard from multiple backend endpoints concurrently.
   * - Core stats via /api/analytics/dashboard
   * - Top/Least books via /api/books/highly-sold & /api/books/least-sold
   */
  loadDashboardData(): void {
    this.isLoading = true;

    // Use forkJoin to manage multiple API calls efficiently and concurrently
    forkJoin({
      // Fetches Revenue, Orders, Items Sold, AOV from Admin/Analytics Service
      dashboard: this.analyticsService.getAnalyticsDashboard(this.startDate, this.endDate),
      // Fetches Top Selling Books from Book Catalog Service
      topBooks: this.analyticsService.getTopSellingBooks(5, this.startDate, this.endDate),
      // Fetches Least Selling Books from Book Catalog Service
      leastBooks: this.analyticsService.getLeastSellingBooks(5, this.startDate, this.endDate)
    }).subscribe({
      next: (results) => {
        // The structure of the AnalyticsDashboard interface aligns with the backend Map<String, Object> 
        // returned by AnalyticsController.getAnalyticsDashboard
        this.dashboardData = results.dashboard; 
        
        // This relies on the Angular service mapping the generic backend 'Object[]' response
        // for top/least selling items to the TopSellingBook[] interface.
        this.topSellingBooks = results.topBooks.map(item => ({
            bookId: item.bookId,
            title: item.title,
            author: item.author || 'N/A',
            quantitySold: item.quantitySold,
            revenue: item.revenue || 0,
            category: item.category || 'N/A',
            averageRating: item.averageRating || 0,
        } as TopSellingBook));

        this.leastSellingBooks = results.leastBooks.map(item => ({
          bookId: item.bookId,
          title: item.title,
          author: item.author || 'N/A',
          quantitySold: item.quantitySold,
          revenue: item.revenue || 0,
          category: item.category || 'N/A',
          averageRating: item.averageRating || 0,
        } as TopSellingBook));
        
        this.updateCards();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading analytics dashboard:', error);
        this.isLoading = false;
        // Optionally set default/error state values
        this.dashboardData = {totalRevenue: 0, totalOrders: 0, totalItemsSold: 0, averageOrderValue: 0, topSellingItems: [], leastSellingItems: []};
        this.topSellingBooks = [];
        this.leastSellingBooks = [];
        this.updateCards();
      }
    });
  }

  /**
   * Maps the fetched analytics data to the dashboard cards.
   */
  updateCards(): void {
    if (!this.dashboardData) return;

    // Map properties from the AnalyticsDashboard interface to the card values
    this.cards[0].value = this.dashboardData.totalRevenue;
    this.cards[1].value = this.dashboardData.totalOrders;
    this.cards[2].value = this.dashboardData.totalItemsSold;
    this.cards[3].value = this.dashboardData.averageOrderValue;
  }

  // Utility to generate dates for the API calls (YYYY-MM-DD format)
  private getEndDate(): string {
    return new Date().toISOString().split('T')[0];
  }

  private getStartDate(range: string): string {
    const today = new Date();
    const date = new Date(today);

    switch (range) {
      case 'last-7-days':
        date.setDate(today.getDate() - 7);
        break;
      case 'last-30-days':
        date.setDate(today.getDate() - 30);
        break;
      case 'last-90-days':
        date.setDate(today.getDate() - 90);
        break;
      case 'last-year':
        date.setFullYear(today.getFullYear() - 1);
        break;
      default:
        date.setDate(today.getDate() - 30);
        break;
    }
    return date.toISOString().split('T')[0];
  }
}