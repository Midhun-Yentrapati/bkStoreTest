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
}

@Injectable({
  providedIn: 'root'
})
export class AnalyticsService {
  private apiUrl = 'http://localhost:8090/api/analytics'; // API Gateway URL
  private adminApiUrl = 'http://localhost:8090/api/admin'; // API Gateway URL

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

  // Statistics Methods
  getTotalRevenueInRange(startDate: string, endDate: string): Observable<number> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);

    return this.http.get<number>(`${this.apiUrl}/stats/total-revenue`, { params }).pipe(
      catchError(this.handleError<number>('getTotalRevenueInRange', 0))
    );
  }

  getTotalOrdersInRange(startDate: string, endDate: string): Observable<number> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);

    return this.http.get<number>(`${this.apiUrl}/stats/total-orders`, { params }).pipe(
      catchError(this.handleError<number>('getTotalOrdersInRange', 0))
    );
  }

  getTotalItemsSoldInRange(startDate: string, endDate: string): Observable<number> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);

    return this.http.get<number>(`${this.apiUrl}/stats/total-items-sold`, { params }).pipe(
      catchError(this.handleError<number>('getTotalItemsSoldInRange', 0))
    );
  }

  getAverageOrderValueInRange(startDate: string, endDate: string): Observable<number> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);

    return this.http.get<number>(`${this.apiUrl}/stats/average-order-value`, { params }).pipe(
      catchError(this.handleError<number>('getAverageOrderValueInRange', 0))
    );
  }

  getTopSellingItemsInRange(startDate: string, endDate: string): Observable<any[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);

    return this.http.get<any[]>(`${this.apiUrl}/stats/top-selling-items`, { params }).pipe(
      catchError(this.handleError<any[]>('getTopSellingItemsInRange', []))
    );
  }

  getLeastSellingItemsInRange(startDate: string, endDate: string): Observable<any[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);

    return this.http.get<any[]>(`${this.apiUrl}/stats/least-selling-items`, { params }).pipe(
      catchError(this.handleError<any[]>('getLeastSellingItemsInRange', []))
    );
  }

  getAnalyticsDashboard(startDate: string, endDate: string): Observable<AnalyticsDashboard> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);

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

  getAllActivityLogs(): Observable<AdminActivityLog[]> {
    return this.http.get<AdminActivityLog[]>(`${this.adminApiUrl}/activity/logs`).pipe(
      catchError(this.handleError<AdminActivityLog[]>('getAllActivityLogs', []))
    );
  }

  getActivityLogsByAdminId(adminId: string): Observable<AdminActivityLog[]> {
    return this.http.get<AdminActivityLog[]>(`${this.adminApiUrl}/activity/logs/admin/${adminId}`).pipe(
      catchError(this.handleError<AdminActivityLog[]>('getActivityLogsByAdminId', []))
    );
  }

  getActivityLogsByActionType(actionType: string): Observable<AdminActivityLog[]> {
    return this.http.get<AdminActivityLog[]>(`${this.adminApiUrl}/activity/logs/action/${actionType}`).pipe(
      catchError(this.handleError<AdminActivityLog[]>('getActivityLogsByActionType', []))
    );
  }

  getActivityLogsByStatus(status: string): Observable<AdminActivityLog[]> {
    return this.http.get<AdminActivityLog[]>(`${this.adminApiUrl}/activity/logs/status/${status}`).pipe(
      catchError(this.handleError<AdminActivityLog[]>('getActivityLogsByStatus', []))
    );
  }

  getActivityLogsByDateRange(startDate: string, endDate: string): Observable<AdminActivityLog[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);

    return this.http.get<AdminActivityLog[]>(`${this.adminApiUrl}/activity/logs/date-range`, { params }).pipe(
      catchError(this.handleError<AdminActivityLog[]>('getActivityLogsByDateRange', []))
    );
  }

  getRecentActivities(): Observable<AdminActivityLog[]> {
    return this.http.get<AdminActivityLog[]>(`${this.adminApiUrl}/activity/logs/recent`).pipe(
      catchError(this.handleError<AdminActivityLog[]>('getRecentActivities', []))
    );
  }

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

  getSlowOperations(minDurationMs: number): Observable<AdminActivityLog[]> {
    const params = new HttpParams().set('minDurationMs', minDurationMs.toString());

    return this.http.get<AdminActivityLog[]>(`${this.adminApiUrl}/activity/logs/slow-operations`, { params }).pipe(
      catchError(this.handleError<AdminActivityLog[]>('getSlowOperations', []))
    );
  }

  getActivityStatistics(): Observable<any> {
    return this.http.get<any>(`${this.adminApiUrl}/activity/stats`).pipe(
      catchError(this.handleError<any>('getActivityStatistics', {}))
    );
  }

  // Utility method to log admin actions automatically
  logAdminAction(actionType: string, actionDescription: string, resourceId?: string, resourceType?: string): void {
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
      error: (error) => console.error('Failed to log activity:', error)
    });
  }

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.error(`${operation} failed:`, error);
      return of(result as T);
    };
  }
} 