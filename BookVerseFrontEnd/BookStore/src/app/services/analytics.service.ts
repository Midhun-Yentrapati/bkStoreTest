import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

export interface DailySalesSummary {
  id?: number;
  salesDate: string;
  totalRevenue: number;
  totalOrders: number;
  totalItemsSold: number;
  averageOrderValue: number;
  topSellingItem: string;
  topSellingItemQuantity: number;
  leastSellingItem: string;
  leastSellingItemQuantity: number;
  // Additional fields from backend
  cancelledOrders?: number;
  deliveredOrders?: number;
  pendingOrders?: number;
  refundedOrders?: number;
  topSellingCategory?: string;
  topSellingBookId?: string;
  topSellingBookTitle?: string;
  leastSellingBookId?: string;
  leastSellingBookTitle?: string;
  newCustomers?: number;
  returningCustomers?: number;
  conversionRate?: number;
  peakHour?: number;
  peakHourOrders?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface AdminActivityLog {
  id: string;
  adminId: string;
  adminUsername: string;
  actionType: string;
  actionDescription: string;
  status: string;
  createdAt: string;
  serviceName?: string;
  endpoint?: string;
  httpMethod?: string;
  requestIp?: string;
  userAgent?: string;
  requestPayload?: string;
  responseStatus?: number;
  executionTimeMs?: number;
  errorMessage?: string;
  resourceId?: string;
  resourceType?: string;
  sessionId?: string;
}

export interface AnalyticsDashboard {
  totalRevenue: number;
  totalOrders: number;
  totalItemsSold: number;
  averageOrderValue: number;
  topSellingItems: any[];
  leastSellingItems: any[];
  // Additional comprehensive analytics
  salesTrend?: {
    daily: { date: string; revenue: number; orders: number }[];
    weekly: { week: string; revenue: number; orders: number }[];
    monthly: { month: string; revenue: number; orders: number }[];
  };
  customerAnalytics?: {
    totalCustomers: number;
    newCustomers: number;
    returningCustomers: number;
    retentionRate: number;
    topSpenders: { customerId: string; name: string; totalSpent: number }[];
  };
  productAnalytics?: {
    totalProducts: number;
    lowStockProducts: number;
    outOfStockProducts: number;
    topCategories: { category: string; sales: number; revenue: number }[];
  };
  orderAnalytics?: {
    ordersByStatus: { status: string; count: number }[];
    ordersByPaymentMethod: { method: string; count: number; revenue: number }[];
    averageDeliveryTime: number;
    cancellationRate: number;
  };
}

// Enhanced analytics interfaces
export interface SalesTrendData {
  labels: string[];
  datasets: {
    label: string;
    data: number[];
    borderColor?: string;
    backgroundColor?: string;
  }[];
}

export interface TopSellingBook {
  bookId: string;
  title: string;
  author: string;
  quantitySold: number;
  revenue: number;
  category: string;
  averageRating?: number;
}

export interface CategoryPerformance {
  categoryId: string;
  categoryName: string;
  totalSales: number;
  totalRevenue: number;
  averageOrderValue: number;
  topBooks: TopSellingBook[];
}

@Injectable({
  providedIn: 'root'
})
export class AnalyticsService {
  private apiUrl = 'http://localhost:8090/api/analytics'; // API Gateway URL for Admin/Analytics Service
  private adminApiUrl = 'http://localhost:8090/api/admin'; // API Gateway URL for Admin Activity
  private bookApiUrl = 'http://localhost:8090/api/books'; // API Gateway URL for Book Catalog Service

  constructor(private http: HttpClient) {}

  // Daily Sales Summary Methods
  processDailySalesSummary(summary: DailySalesSummary): Observable<DailySalesSummary> {
    return this.http.post<DailySalesSummary>(`${this.apiUrl}/daily-sales/process`, summary).pipe(
      catchError(this.handleError<DailySalesSummary>('processDailySalesSummary'))
    );
  }

  getRecentDailySalesSummaries(): Observable<DailySalesSummary[]> {
    return this.http.get<DailySalesSummary[]>(`${this.apiUrl}/daily-sales/recent`).pipe(
      catchError(this.handleError<DailySalesSummary[]>('getRecentDailySalesSummaries', []))
    );
  }

  getDailySalesSummaryByDate(salesDate: string): Observable<DailySalesSummary> {
    return this.http.get<DailySalesSummary>(`${this.apiUrl}/daily-sales/${salesDate}`).pipe(
      catchError(this.handleError<DailySalesSummary>('getDailySalesSummaryByDate'))
    );
  }

  getDailySalesSummariesByDateRange(startDate: string, endDate: string): Observable<DailySalesSummary[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);

    return this.http.get<DailySalesSummary[]>(`${this.apiUrl}/daily-sales/date-range`, { params }).pipe(
      catchError(this.handleError<DailySalesSummary[]>('getDailySalesSummariesByDateRange', []))
    );
  }

  // Enhanced Analytics Methods
  getDashboardStats(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/dashboard-stats`).pipe(
      catchError(this.handleError<any>('getDashboardStats', {
        totalOrders: 0,
        pendingOrders: 0,
        deliveredOrders: 0,
        totalRevenue: 0
      }))
    );
  }

  getSalesTrendsData(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/sales-trends`).pipe(
      catchError(this.handleError<any>('getSalesTrendsData', { yearlyRevenue: {} }))
    );
  }

  getTopSellingBooksData(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/top-selling-books`).pipe(
      catchError(this.handleError<any[]>('getTopSellingBooksData', []))
    );
  }

  getLeastSellingBooksData(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/least-selling-books`).pipe(
      catchError(this.handleError<any[]>('getLeastSellingBooksData', []))
    );
  }

  getTotalOrders(startDate?: string, endDate?: string): Observable<number> {
    let params = new HttpParams();
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);

    return this.http.get<number>(`${this.apiUrl}/stats/total-orders`, { params }).pipe(
      catchError(this.handleError<number>('getTotalOrders', 0))
    );
  }

  getTopSellingBooks(limit: number = 10, startDate?: string, endDate?: string): Observable<TopSellingBook[]> {
    let params = new HttpParams().set('limit', limit.toString());
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);

    // CORRECTED URL: Points to Book Catalog service
    return this.http.get<TopSellingBook[]>(`${this.bookApiUrl}/highly-sold`, { params }).pipe(
      catchError(this.handleError<TopSellingBook[]>('getTopSellingBooks', []))
    );
  }

  getLeastSellingBooks(limit: number = 10, startDate?: string, endDate?: string): Observable<TopSellingBook[]> {
    let params = new HttpParams().set('limit', limit.toString());
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);

    // CORRECTED URL: Points to Book Catalog service
    return this.http.get<TopSellingBook[]>(`${this.bookApiUrl}/least-sold`, { params }).pipe(
      catchError(this.handleError<TopSellingBook[]>('getLeastSellingBooks', []))
    );
  }

  getCategoryPerformance(startDate?: string, endDate?: string): Observable<CategoryPerformance[]> {
    let params = new HttpParams();
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);

    return this.http.get<CategoryPerformance[]>(`${this.apiUrl}/categories/performance`, { params }).pipe(
      catchError(this.handleError<CategoryPerformance[]>('getCategoryPerformance', []))
    );
  }

  getSalesTrend(period: 'daily' | 'weekly' | 'monthly' = 'daily', startDate?: string, endDate?: string): Observable<SalesTrendData> {
    let params = new HttpParams().set('period', period);
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);

    return this.http.get<SalesTrendData>(`${this.apiUrl}/sales-trend`, { params }).pipe(
      catchError(this.handleError<SalesTrendData>('getSalesTrend', { labels: [], datasets: [] }))
    );
  }

  getAnalyticsDashboard(startDate: string, endDate: string): Observable<AnalyticsDashboard> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);

    // This method hits the combined stats endpoint on the Admin/Analytics service
    return this.http.get<AnalyticsDashboard>(`${this.apiUrl}/dashboard`, { params }).pipe(
      catchError(this.handleError<AnalyticsDashboard>('getAnalyticsDashboard', {
        totalRevenue: 0,
        totalOrders: 0,
        totalItemsSold: 0,
        averageOrderValue: 0,
        topSellingItems: [],
        leastSellingItems: []
      }))
    );
  }

  // Admin Activity Log Methods
  logActivity(activityData: Partial<AdminActivityLog>): Observable<AdminActivityLog> {
    return this.http.post<AdminActivityLog>(`${this.adminApiUrl}/activity/log`, activityData).pipe(
      catchError(this.handleError<AdminActivityLog>('logActivity'))
    );
  }

  logSuccessActivity(activityData: Partial<AdminActivityLog>): Observable<AdminActivityLog> {
    return this.http.post<AdminActivityLog>(`${this.adminApiUrl}/activity/log-success`, activityData).pipe(
      catchError(this.handleError<AdminActivityLog>('logSuccessActivity'))
    );
  }

  logFailureActivity(activityData: Partial<AdminActivityLog>): Observable<AdminActivityLog> {
    return this.http.post<AdminActivityLog>(`${this.adminApiUrl}/activity/log-failure`, activityData).pipe(
      catchError(this.handleError<AdminActivityLog>('logFailureActivity'))
    );
  }

  getRecentActivities(limit: number = 50): Observable<AdminActivityLog[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<AdminActivityLog[]>(`${this.adminApiUrl}/activity/recent`, { params }).pipe(
      catchError(this.handleError<AdminActivityLog[]>('getRecentActivities', []))
    );
  }

  getActivitiesByAdmin(adminId: string, limit: number = 50): Observable<AdminActivityLog[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<AdminActivityLog[]>(`${this.adminApiUrl}/activity/admin/${adminId}`, { params }).pipe(
      catchError(this.handleError<AdminActivityLog[]>('getActivitiesByAdmin', []))
    );
  }

  getActivitiesByAction(action: string, limit: number = 50): Observable<AdminActivityLog[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<AdminActivityLog[]>(`${this.adminApiUrl}/activity/action/${action}`, { params }).pipe(
      catchError(this.handleError<AdminActivityLog[]>('getActivitiesByAction', []))
    );
  }

  getActivitiesByStatus(status: string, limit: number = 50): Observable<AdminActivityLog[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<AdminActivityLog[]>(`${this.adminApiUrl}/activity/status/${status}`, { params }).pipe(
      catchError(this.handleError<AdminActivityLog[]>('getActivitiesByStatus', []))
    );
  }

  getActivitiesByDateRange(startDate: string, endDate: string): Observable<AdminActivityLog[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);

    return this.http.get<AdminActivityLog[]>(`${this.adminApiUrl}/activity/date-range`, { params }).pipe(
      catchError(this.handleError<AdminActivityLog[]>('getActivitiesByDateRange', []))
    );
  }

  getActivityStatistics(): Observable<any> {
    return this.http.get<any>(`${this.adminApiUrl}/activity/statistics`).pipe(
      catchError(this.handleError<any>('getActivityStatistics', {}))
    );
  }

  // Enhanced utility methods
  exportAnalyticsData(startDate: string, endDate: string, format: 'csv' | 'excel' = 'csv'): Observable<Blob> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate)
      .set('format', format);

    return this.http.get(`${this.apiUrl}/export`, { 
      params, 
      responseType: 'blob' 
    }).pipe(
      catchError(this.handleError<Blob>('exportAnalyticsData'))
    );
  }

  // Utility method to log admin actions automatically
  logAdminAction(actionType: string, actionDescription: string, resourceId?: string, resourceType?: string): void {
    // Get admin info from auth service or session storage
    const adminId = sessionStorage.getItem('loggedInUserId') || 'unknown';
    const adminUsername = sessionStorage.getItem('loggedInUsername') || 'unknown';
    const sessionId = sessionStorage.getItem('sessionId') || '';

    const activityData = {
      adminId,
      adminUsername,
      actionType,
      actionDescription,
      status: 'SUCCESS',
      sessionId,
      resourceId,
      resourceType,
      createdAt: new Date().toISOString()
    };

    this.logActivity(activityData).subscribe({
      next: (log) => console.log('Activity logged:', log),
      error: (error) => {
        console.error('logActivity failed:', error);
        // Fallback: try direct API call
        this.http.post(`${this.adminApiUrl}/activity/log`, activityData).subscribe({
          next: (result) => console.log('Direct activity log successful:', result),
          error: (directError) => console.error('Direct activity log also failed:', directError)
        });
      }
    });
  }

  // Legacy methods for backward compatibility
  getTopActiveAdmins(): Observable<any[]> {
    return this.http.get<any[]>(`${this.adminApiUrl}/activity/stats/top-admins`).pipe(
      catchError(this.handleError<any[]>('getTopActiveAdmins', []))
    );
  }

  getMostCommonActions(): Observable<any[]> {
    return this.http.get<any[]>(`${this.adminApiUrl}/activity/stats/common-actions`).pipe(
      catchError(this.handleError<any[]>('getMostCommonActions', []))
    );
  }

  getLogsWithErrors(): Observable<AdminActivityLog[]> {
    return this.http.get<AdminActivityLog[]>(`${this.adminApiUrl}/activity/logs/errors`).pipe(
      catchError(this.handleError<AdminActivityLog[]>('getLogsWithErrors', []))
    );
  }

  // Error handling
  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.error(`${operation} failed:`, error);
      return of(result as T);
    };
  }
}