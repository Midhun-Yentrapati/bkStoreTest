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
import { AnalyticsService } from '../../../services/analytics.service';
import { CategoryService } from '../../../services/category.service';

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

  private apiBaseUrl = 'http://localhost:8090/api'; // API Gateway URL

  constructor(
    private router: Router, 
    private bookService: BookService,
    private orderService: OrderService,
    private authService: AuthService,
    private http: HttpClient,
    private analyticsService: AnalyticsService,
    private categoryService: CategoryService
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
    // Log dashboard access
    this.analyticsService.logAdminAction('VIEW', 'Admin dashboard accessed');
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
    console.log('Navigating to user management...');
    this.analyticsService.logAdminAction('NAVIGATE', 'Navigated to user management');
    this.router.navigate(['/admin/users']).catch(error => {
      console.error('Navigation to users failed:', error);
    });
  }

  navigateToCategories(): void {
    console.log('Navigating to category management...');
    this.analyticsService.logAdminAction('NAVIGATE', 'Navigated to category management');
    this.router.navigate(['/admin/categories']).catch(error => {
      console.error('Navigation to categories failed:', error);
    });
  }

  navigateToOrders(): void {
    console.log('Navigating to order management...');
    this.analyticsService.logAdminAction('NAVIGATE', 'Navigated to order management');
    this.router.navigate(['/admin/orders']).catch(error => {
      console.error('Navigation to orders failed:', error);
    });
  }

  navigateToCoupons(): void {
    console.log('Navigating to coupon management...');
    this.analyticsService.logAdminAction('NAVIGATE', 'Navigated to coupon management');
    this.router.navigate(['/admin/coupons']).catch(error => {
      console.error('Navigation to coupons failed:', error);
    });
  }

  navigateToReviews(): void {
    this.router.navigate(['/admin/reviews']);
  }

  navigateToSectionManagement(): void {
    this.router.navigate(['/admin/section-management']);
  }

  navigateToAnalytics(): void {
    console.log('Navigating to analytics dashboard...');
    this.analyticsService.logAdminAction('NAVIGATE', 'Navigated to analytics dashboard');
    this.router.navigate(['/admin/analytics']).catch(error => {
      console.error('Navigation to analytics failed:', error);
    });
  }

  private loadDashboardData(): void {
    // Load dashboard stats from analytics service
    this.analyticsService.getDashboardStats().subscribe({
      next: (stats) => {
        this.totalOrders = stats.totalOrders || 0;
        this.totalRevenue = stats.totalRevenue || 0;
        console.log('Dashboard stats loaded:', stats);
      },
      error: (error) => {
        console.error('Error loading dashboard stats:', error);
        this.totalOrders = 0;
        this.totalRevenue = 0;
      }
    });

    // Load books and users separately
    this.bookService.getAllBooks().subscribe({
      next: (books) => {
        this.totalBooks = books.length;
        this.calculateLowStockCount(books);
      },
      error: (error) => {
        console.error('Error loading books:', error);
        this.totalBooks = 0;
      }
    });

    this.getTotalUsersCount().subscribe({
      next: (count) => {
        this.totalUsers = count;
      },
      error: (error) => {
        console.error('Error loading users count:', error);
        this.totalUsers = 0;
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
      adminUsers: this.http.get<any>(`${this.apiBaseUrl}/users/admin/admins`, { headers }).pipe(
        catchError(error => {
          console.error('Error fetching admin users:', error);
          return of({ content: [] }); // Return empty array on error
        })
      ),
      customerUsers: this.http.get<any>(`${this.apiBaseUrl}/users/admin/customers`, { headers }).pipe(
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



  // Test all admin functionalities
  testAdminFunctionalities(): void {
    console.log('🧪 Testing Admin Functionalities...');
    
    // Test 1: Book Catalog Management
    this.testBookCatalogFunctionality();
    
    // Test 2: Order Management
    this.testOrderManagementFunctionality();
    
    // Test 3: User Management
    this.testUserManagementFunctionality();
    
    // Test 4: Analytics and Charts
    this.testAnalyticsFunctionality();
    
    // Test 5: Category Management
    this.testCategoryManagementFunctionality();
    
    // Test 6: Coupon Management
    this.testCouponManagementFunctionality();
  }

  private testBookCatalogFunctionality(): void {
    console.log('📚 Testing Book Catalog Functionality...');
    
    this.bookService.getAllBooks().subscribe({
      next: (books) => {
        console.log('✅ Book catalog loaded successfully:', books.length, 'books');
        
        // Test book details fetch
        if (books.length > 0) {
          this.bookService.getBookById(books[0].id).subscribe({
            next: (book) => console.log('✅ Book details fetch successful:', book.title),
            error: (error) => console.error('❌ Book details fetch failed:', error)
          });
        }
      },
      error: (error) => {
        console.error('❌ Book catalog loading failed:', error);
      }
    });
  }

  private testOrderManagementFunctionality(): void {
    console.log('📦 Testing Order Management Functionality...');
    
    this.orderService.getAllOrders().subscribe({
      next: (orders) => {
        console.log('✅ Orders loaded successfully:', orders.length, 'orders');
        
        // Test order status update if orders exist
        if (orders.length > 0) {
          const testOrder = orders[0];
          console.log('📋 Order management features available for order:', testOrder.id);
        }
      },
      error: (error) => {
        console.error('❌ Order management loading failed:', error);
      }
    });
  }

  private testUserManagementFunctionality(): void {
    console.log('👥 Testing User Management Functionality...');
    
    this.http.get(`${this.apiBaseUrl}/users/admin/all`).subscribe({
      next: (users: any) => {
        console.log('✅ User management loaded successfully:', Array.isArray(users) ? users.length : 0, 'users');
      },
      error: (error) => {
        console.error('❌ User management loading failed:', error);
      }
    });
  }

  private testAnalyticsFunctionality(): void {
    console.log('📊 Testing Analytics Functionality...');
    
    const endDate = new Date().toISOString().split('T')[0];
    const startDate = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];
    
    this.analyticsService.getAnalyticsDashboard(startDate, endDate).subscribe({
      next: (dashboard) => {
        console.log('✅ Analytics dashboard loaded successfully:', dashboard);
      },
      error: (error) => {
        console.error('❌ Analytics dashboard loading failed:', error);
      }
    });
  }

  private testCategoryManagementFunctionality(): void {
    console.log('🏷️ Testing Category Management Functionality...');
    
    this.categoryService.getAllCategories().subscribe({
      next: (categories) => {
        console.log('✅ Category management loaded successfully:', categories.length, 'categories');
      },
      error: (error) => {
        console.error('❌ Category management loading failed:', error);
      }
    });
  }

  private testCouponManagementFunctionality(): void {
    console.log('🎫 Testing Coupon Management Functionality...');
    
    this.http.get(`${this.apiBaseUrl}/coupons`).subscribe({
      next: (coupons: any) => {
        console.log('✅ Coupon management loaded successfully:', Array.isArray(coupons) ? coupons.length : 0, 'coupons');
      },
      error: (error) => {
        console.error('❌ Coupon management loading failed:', error);
      }
    });
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