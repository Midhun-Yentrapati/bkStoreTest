import { CommonModule } from '@angular/common';
import { Component, Input, OnInit, OnChanges, SimpleChanges, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Book, ChartData } from '../../../models/book';
import { HighlySoldComponent } from '../highly-sold/highly-sold.component';
import { LeastSoldComponent } from '../least-sold/least-sold.component';
import { YearlySalesChartComponent } from '../yearly-sales-chart/yearly-sales-chart.component';
import { Router } from '@angular/router';
import { BookService } from '../../../services/book.service';
import { OrderService } from '../../../services/order.service';
import { AuthService } from '../../../services/auth.service';
import { HttpClient } from '@angular/common/http';

import { forkJoin, of, Observable } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { UserModel } from '../../../models/user.model';
import {
  Chart,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  LineController,
  Title,
  Tooltip,
  Legend,
  Filler
} from 'chart.js';

@Component({
  selector: 'app-admin-dashboard-page',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HighlySoldComponent,
    LeastSoldComponent,
    YearlySalesChartComponent
  ],
  templateUrl: './admin-dashboard-page.component.html',
  styleUrls: ['./modern-dashboard.component.css']
})
export class AdminDashboardPageComponent implements OnInit, OnChanges, AfterViewInit {
  @Input() username: string = '';
  @Input() highlyRatedBooks: Book[] = [];
  @Input() inventory: Book[] = [];
  @Input() error: string | null = null;

  // Dashboard statistics
  totalOrders: number = 0;
  totalBooks: number = 0;
  totalUsers: number = 0;
  lowStockCount: number = 0;

  // Revenue Analytics properties
  selectedPeriod: string = 'all-time';
  totalRevenue: number = 0;
  revenueTrend: number = 0;
  periodOrders: number = 0;
  allOrders: any[] = [];
  Math = Math;

  // Sales Trends Chart properties - Updated for TypeScript recognition
  @ViewChild('salesTrendsChart', { static: false }) salesTrendsCanvas!: ElementRef<HTMLCanvasElement>;
  selectedTrendsPeriod: string = 'yearly';
  isTrendsLoading: boolean = false;
  peakSalesPeriod: string = '';
  totalSalesInPeriod: number = 0;
  salesGrowthRate: number = 0;
  chartDataPoints: number = 0;
  trendsChart: any = null;

  bookListCards: { title: string; description: string; type: 'inventory' | 'editGenere' | 'manageAdminUsers' | 'orders' | 'lowStock' | 'users'; }[] = [];

  constructor(
    private router: Router, 
    private bookService: BookService,
    private orderService: OrderService,
    private authService: AuthService,
    private http: HttpClient
  ) {
    // Register Chart.js components for sales trends chart
    Chart.register(
      CategoryScale,
      LinearScale,
      PointElement,
      LineElement,
      LineController,
      Title,
      Tooltip,
      Legend,
      Filler
    );
  }

  ngOnInit(): void {
    console.log('AdminDashboardPageComponent initialized.');
    this.updateBookListCards();
    this.loadDashboardData();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['highlyRatedBooks'] || changes['inventory'] || changes['error']) {
      this.updateBookListCards();
    }
  }

  private updateBookListCards(): void {
    this.bookListCards = [
      {
        title: 'View Inventory',
        description:'Click to view inventory.',
        type: 'inventory'
      },
      {
        title: 'Orders',
        description: 'Monitor and manage customer orders.',
        type: 'orders'
      },
      {
        title: 'Low Stock Alert',
        description: 'Books that need restocking.',
        type: 'lowStock'
      },
      {
        title: 'Edit Genres',
        description: 'Manage and Edit Genre Titles',
        type: 'editGenere'
      },
      {
        title: 'Manage Admin Users',
        description: 'Manage and Edit Admin Users',
        type: 'manageAdminUsers'
      },
    ];
  }
  
  private checkLowInventory(): void {
    this.bookService.getAllBooks().subscribe({
      next: (books) => {
        const lowInventoryBooks = books.filter(book => book.stock_actual < 10);
        if (lowInventoryBooks.length > 0) {
          const bookList = lowInventoryBooks.map(book => `- ID: ${book.id}, Title: "${book.title}", Quantity: ${book.stock_actual}`).join('\n');
          const message = `The following books have a quantity less than 10 and need to be restocked:\n\n${bookList}`;
          const shouldNavigate = confirm(message + '\n\nDo you want to go to the inventory page to restock them?');

          if (shouldNavigate) {
            this.router.navigate(['/admin/inventory']);
          }
        }
      },
      error: (err) => {
        console.error('Error fetching books for low inventory check:', err);
      }
    });
  }

  onBookListCardClick(cardTitle: string, cardType: 'inventory' | 'editGenere' | 'manageAdminUsers' | 'orders' | 'lowStock' | 'users'): void {
    console.log(`Book List Card "${cardTitle}" (Type: ${cardType}) was clicked.`);
    if (cardType === 'inventory') {
      console.log('Navigation triggered');
      this.router.navigate(['/admin/inventory']);
    }
    if (cardType === 'orders') {
      console.log('Navigation triggered');
      this.router.navigate(['/admin/orders']);
    }
    if (cardType === 'lowStock') {
      console.log('Navigation triggered');
      this.router.navigate(['/admin/low-stock']);
    }
    if (cardType === 'editGenere') {
      this.router.navigate(['/admin/categories']);
    }
    if (cardType === 'manageAdminUsers') {
      this.router.navigate(['/admin/users']);
    }
    if (cardType === 'users') {
      this.router.navigate(['/admin/users']);
    }
  }

  // Utility methods for template
  getCurrentTime(): string {
    return new Date().toLocaleTimeString();
  }

  trackByCardTitle(index: number, card: any): string {
    return card.title;
  }

  navigateToAddBook(): void {
    this.router.navigate(['/admin/add-book']);
  }

  navigateToUsers(): void {
    this.router.navigate(['/admin/users']);
  }

  navigateToCategories(): void {
    this.router.navigate(['/admin/categories']);
  }

  navigateToOrders(): void {
    this.router.navigate(['/admin/orders']);
  }

  navigateToCoupons(): void {
    this.router.navigate(['/admin/coupons']);
  }

  navigateToReviews(): void {
    this.router.navigate(['/admin/reviews']);
  }

  navigateToSectionManagement(): void {
    this.router.navigate(['/admin/section-management']);
  }

  private loadDashboardData(): void {
    // Load all dashboard data simultaneously using services
    forkJoin({
      orders: this.orderService.getAllOrders(),
      books: this.bookService.getAllBooks(),
      totalUsers: this.getTotalUsersCount() // Real API call to get total users
    }).subscribe({
      next: (data) => {
        this.totalOrders = data.orders.length;
        this.totalBooks = data.books.length;
        this.totalUsers = data.totalUsers;
        this.allOrders = data.orders; // Store all orders for revenue calculation
        this.calculateLowStockCount(data.books);
        this.calculateRevenue(); // Calculate initial revenue
        console.log('Dashboard data loaded:', {
          orders: this.totalOrders,
          books: this.totalBooks,
          users: this.totalUsers,
          lowStock: this.lowStockCount,
          revenue: this.totalRevenue
        });
      },
      error: (error) => {
        console.error('Error loading dashboard data:', error);
        // Set fallback values on error
        this.totalOrders = 0;
        this.totalBooks = 0;
        this.totalUsers = 0;
        this.lowStockCount = 0;
      }
    });
  }

  private calculateLowStockCount(books: any[]): void {
    // Count books with low stock (less than 10 items)
    this.lowStockCount = books.filter(book => 
      book.stock_actual < 10
    ).length;
  }

  /**
   * Fetches total count of all users (both admin and customer users)
   * @returns Observable<number> Total count of users
   */
  private getTotalUsersCount(): Observable<number> {
    const apiBaseUrl = 'http://localhost:8090/api'; // API Gateway URL
    const token = this.authService.getToken();
    
    if (!token) {
      console.warn('No authentication token found for user count request');
      return of(0);
    }

    const headers = {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    };

    // Fetch both admin and customer users simultaneously
    return forkJoin({
      adminUsers: this.http.get<any>(`${apiBaseUrl}/users/admin/admins`, { headers }).pipe(
        catchError(error => {
          console.error('Error fetching admin users:', error);
          return of({ content: [] }); // Return empty array on error
        })
      ),
      customerUsers: this.http.get<any>(`${apiBaseUrl}/users/admin/customers`, { headers }).pipe(
        catchError(error => {
          console.error('Error fetching customer users:', error);
          return of({ content: [] }); // Return empty array on error
        })
      )
    }).pipe(
      map(data => {
        const adminCount = data.adminUsers.content ? data.adminUsers.content.length : 0;
        const customerCount = data.customerUsers.content ? data.customerUsers.content.length : 0;
        const totalCount = adminCount + customerCount;
        
        console.log('📊 User count breakdown:', {
          adminUsers: adminCount,
          customerUsers: customerCount,
          totalUsers: totalCount
        });
        
        return totalCount;
      }),
      catchError(error => {
        console.error('Error calculating total users count:', error);
        return of(0); // Return 0 on any error
      })
    );
  }

  getCurrentDate(): string {
    return new Date().toLocaleDateString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }

  // Revenue Analytics Methods
  onPeriodChange(): void {
    this.calculateRevenue();
  }

  private calculateRevenue(): void {
    const now = new Date();
    let filteredOrders = [];
    let previousPeriodOrders = [];

    switch (this.selectedPeriod) {
      case 'all-time':
        filteredOrders = this.allOrders;
        // For comparison, use last year's data
        const lastYear = new Date(now.getFullYear() - 1, now.getMonth(), now.getDate());
        previousPeriodOrders = this.allOrders.filter(order => {
          const orderDate = new Date(order.orderDate || order.createdAt);
          return orderDate < lastYear;
        });
        break;
        
      case 'current-year':
        filteredOrders = this.allOrders.filter(order => {
          const orderDate = new Date(order.orderDate || order.createdAt);
          return orderDate.getFullYear() === now.getFullYear();
        });
        previousPeriodOrders = this.allOrders.filter(order => {
          const orderDate = new Date(order.orderDate || order.createdAt);
          return orderDate.getFullYear() === now.getFullYear() - 1;
        });
        break;
        
      case 'last-year':
        filteredOrders = this.allOrders.filter(order => {
          const orderDate = new Date(order.orderDate || order.createdAt);
          return orderDate.getFullYear() === now.getFullYear() - 1;
        });
        previousPeriodOrders = this.allOrders.filter(order => {
          const orderDate = new Date(order.orderDate || order.createdAt);
          return orderDate.getFullYear() === now.getFullYear() - 2;
        });
        break;
        
      case 'current-month':
        filteredOrders = this.allOrders.filter(order => {
          const orderDate = new Date(order.orderDate || order.createdAt);
          return orderDate.getFullYear() === now.getFullYear() && 
                 orderDate.getMonth() === now.getMonth();
        });
        const prevMonth = new Date(now.getFullYear(), now.getMonth() - 1, 1);
        const prevMonthEnd = new Date(now.getFullYear(), now.getMonth(), 0);
        previousPeriodOrders = this.allOrders.filter(order => {
          const orderDate = new Date(order.orderDate || order.createdAt);
          return orderDate >= prevMonth && orderDate <= prevMonthEnd;
        });
        break;
        
      case 'last-month':
        const lastMonthStart = new Date(now.getFullYear(), now.getMonth() - 1, 1);
        const lastMonthEndDate = new Date(now.getFullYear(), now.getMonth(), 0);
        filteredOrders = this.allOrders.filter(order => {
          const orderDate = new Date(order.orderDate || order.createdAt);
          return orderDate >= lastMonthStart && orderDate <= lastMonthEndDate;
        });
        const twoMonthsAgo = new Date(now.getFullYear(), now.getMonth() - 2, 1);
        const twoMonthsAgoEnd = new Date(now.getFullYear(), now.getMonth() - 1, 0);
        previousPeriodOrders = this.allOrders.filter(order => {
          const orderDate = new Date(order.orderDate || order.createdAt);
          return orderDate >= twoMonthsAgo && orderDate <= twoMonthsAgoEnd;
        });
        break;
    }

    // Calculate current period revenue
    this.totalRevenue = filteredOrders.reduce((total, order) => {
      return total + (order.finalAmount || order.totalAmount || 0);
    }, 0);
    
    this.periodOrders = filteredOrders.length;

    // Calculate previous period revenue for trend
    const previousRevenue = previousPeriodOrders.reduce((total, order) => {
      return total + (order.finalAmount || order.totalAmount || 0);
    }, 0);

    // Calculate trend percentage
    if (previousRevenue > 0) {
      this.revenueTrend = ((this.totalRevenue - previousRevenue) / previousRevenue) * 100;
      this.revenueTrend = Math.round(this.revenueTrend * 100) / 100; // Round to 2 decimal places
    } else {
      this.revenueTrend = this.totalRevenue > 0 ? 100 : 0;
    }
  }

  formatRevenue(amount: number): string {
    if (amount >= 10000000) { // 1 crore
      return (amount / 10000000).toFixed(2) + 'Cr';
    } else if (amount >= 100000) { // 1 lakh
      return (amount / 100000).toFixed(2) + 'L';
    } else if (amount >= 1000) { // 1 thousand
      return (amount / 1000).toFixed(1) + 'K';
    }
    return amount.toFixed(0);
  }

  calculateAverageOrderValue(): string {
    if (this.periodOrders === 0) return '0';
    const avg = this.totalRevenue / this.periodOrders;
    return avg.toFixed(0);
  }

  getPreviousPeriodText(): string {
    switch (this.selectedPeriod) {
      case 'all-time': return 'previous years';
      case 'current-year': return 'last year';
      case 'last-year': return 'year before';
      case 'current-month': return 'last month';
      case 'last-month': return 'previous month';
      default: return 'previous period';
    }
  }

  getPeriodDisplayText(): string {
    const now = new Date();
    switch (this.selectedPeriod) {
      case 'all-time': return 'All Time';
      case 'current-year': return now.getFullYear().toString();
      case 'last-year': return (now.getFullYear() - 1).toString();
      case 'current-month': 
        return now.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });
      case 'last-month':
        const lastMonth = new Date(now.getFullYear(), now.getMonth() - 1);
        return lastMonth.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });
      default: return 'Selected Period';
    }
  }

  // AfterViewInit lifecycle hook
  ngAfterViewInit(): void {
    // Initialize trends chart after view is ready
    setTimeout(() => {
      this.initializeTrendsChart();
    }, 100);
  }

  // Sales Trends Chart Methods - Public methods for template binding
  public onTrendsPeriodChange(): void {
    this.generateTrendsChart();
  }

  private initializeTrendsChart(): void {
    if (this.allOrders.length === 0) {
      // Wait for data to load
      setTimeout(() => this.initializeTrendsChart(), 500);
      return;
    }
    
    // Chart.js is now statically imported and registered
    try {
      this.generateTrendsChart();
    } catch (error) {
      console.warn('Chart.js error, using basic canvas drawing:', error);
      this.drawBasicChart();
    }
  }

  private generateTrendsChart(): void {
    this.isTrendsLoading = true;
    
    try {
      const chartData = this.prepareTrendsData();
      
      if (this.trendsChart) {
        this.trendsChart.destroy();
      }
      
      const canvas = this.salesTrendsCanvas?.nativeElement;
      if (!canvas) return;
      
      const ctx = canvas.getContext('2d');
      if (!ctx) return;
      
      this.trendsChart = new Chart(ctx, {
        type: 'line',
        data: {
          labels: chartData.labels,
          datasets: [{
            label: 'Sales Revenue',
            data: chartData.values,
            borderColor: '#667eea',
            backgroundColor: 'rgba(102, 126, 234, 0.1)',
            borderWidth: 3,
            fill: true,
            tension: 0.4,
            pointBackgroundColor: '#667eea',
            pointBorderColor: '#ffffff',
            pointBorderWidth: 2,
            pointRadius: 6,
            pointHoverRadius: 8
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              display: false
            },
            tooltip: {
              backgroundColor: 'rgba(0, 0, 0, 0.8)',
              titleColor: '#ffffff',
              bodyColor: '#ffffff',
              borderColor: '#667eea',
              borderWidth: 1,
              cornerRadius: 8,
              callbacks: {
                label: (context: any) => {
                  return `Revenue: ₹${this.formatRevenue(context.parsed.y)}`;
                }
              }
            }
          },
          scales: {
            x: {
              grid: {
                color: 'rgba(0, 0, 0, 0.1)'
              },
              ticks: {
                color: '#6b7280',
                font: {
                  size: 12
                }
              }
            },
            y: {
              grid: {
                color: 'rgba(0, 0, 0, 0.1)'
              },
              ticks: {
                color: '#6b7280',
                font: {
                  size: 12
                },
                callback: (value: any) => {
                  return '₹' + this.formatRevenue(value);
                }
              }
            }
          },
          elements: {
            point: {
              hoverBorderWidth: 3
            }
          }
        }
      });
      
      this.calculateTrendsInsights(chartData);
    } catch (error) {
      console.error('Error generating trends chart:', error);
      this.drawBasicChart();
    } finally {
      this.isTrendsLoading = false;
    }
  }

  private prepareTrendsData(): { labels: string[], values: number[] } {
    const now = new Date();
    let labels: string[] = [];
    let values: number[] = [];
    
    if (this.selectedTrendsPeriod === 'yearly') {
      // Get last 5 years of data
      for (let i = 4; i >= 0; i--) {
        const year = now.getFullYear() - i;
        labels.push(year.toString());
        
        const yearlyRevenue = this.allOrders
          .filter(order => {
            const orderDate = new Date(order.orderDate || order.createdAt);
            return orderDate.getFullYear() === year;
          })
          .reduce((total, order) => total + (order.finalAmount || order.totalAmount || 0), 0);
        
        values.push(yearlyRevenue);
      }
    } else {
      // Get current year months
      const monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
      for (let month = 0; month < 12; month++) {
        labels.push(monthNames[month]);
        
        const monthlyRevenue = this.allOrders
          .filter(order => {
            const orderDate = new Date(order.orderDate || order.createdAt);
            return orderDate.getFullYear() === now.getFullYear() && orderDate.getMonth() === month;
          })
          .reduce((total, order) => total + (order.finalAmount || order.totalAmount || 0), 0);
        
        values.push(monthlyRevenue);
      }
    }
    
    this.chartDataPoints = labels.length;
    return { labels, values };
  }

  private calculateTrendsInsights(chartData: { labels: string[], values: number[] }): void {
    // Find peak sales period
    const maxValue = Math.max(...chartData.values);
    const maxIndex = chartData.values.indexOf(maxValue);
    this.peakSalesPeriod = chartData.labels[maxIndex];
    
    // Calculate total sales in period
    this.totalSalesInPeriod = chartData.values.reduce((total, value) => total + value, 0);
    
    // Calculate growth rate (last vs first period)
    const firstValue = chartData.values[0];
    const lastValue = chartData.values[chartData.values.length - 1];
    if (firstValue > 0) {
      this.salesGrowthRate = ((lastValue - firstValue) / firstValue) * 100;
    } else {
      this.salesGrowthRate = lastValue > 0 ? 100 : 0;
    }
  }

  private drawBasicChart(): void {
    // Fallback basic canvas drawing if Chart.js is not available
    const canvas = this.salesTrendsCanvas?.nativeElement;
    if (!canvas) return;
    
    const ctx = canvas.getContext('2d');
    if (!ctx) return;
    
    const chartData = this.prepareTrendsData();
    
    // Clear canvas
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    
    // Draw basic line chart
    ctx.strokeStyle = '#667eea';
    ctx.lineWidth = 2;
    ctx.beginPath();
    
    const padding = 50;
    const chartWidth = canvas.width - (padding * 2);
    const chartHeight = canvas.height - (padding * 2);
    
    const maxValue = Math.max(...chartData.values);
    
    chartData.values.forEach((value, index) => {
      const x = padding + (index * (chartWidth / (chartData.values.length - 1)));
      const y = padding + chartHeight - ((value / maxValue) * chartHeight);
      
      if (index === 0) {
        ctx.moveTo(x, y);
      } else {
        ctx.lineTo(x, y);
      }
    });
    
    ctx.stroke();
    
    // Draw labels
    ctx.fillStyle = '#6b7280';
    ctx.font = '12px sans-serif';
    ctx.textAlign = 'center';
    
    chartData.labels.forEach((label, index) => {
      const x = padding + (index * (chartWidth / (chartData.values.length - 1)));
      ctx.fillText(label, x, canvas.height - 20);
    });
    
    this.calculateTrendsInsights(chartData);
    this.isTrendsLoading = false;
  }
}