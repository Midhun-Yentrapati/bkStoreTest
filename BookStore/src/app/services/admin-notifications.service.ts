import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

export interface AdminNotification {
  id: string;
  type: 'order' | 'low_stock' | 'system';
  title: string;
  message: string;
  isRead: boolean;
  createdAt: string;
  data?: any;
  priority: 'low' | 'medium' | 'high';
}

@Injectable({
  providedIn: 'root'
})
export class AdminNotificationsService {
  private apiUrl = 'http://localhost:8090/api/admin/notifications'; // API Gateway URL

  constructor(private http: HttpClient) {}

  // Create a new notification
  createNotification(notification: Omit<AdminNotification, 'id' | 'createdAt'>): Observable<AdminNotification> {
    const newNotification = {
      ...notification,
      id: crypto.randomUUID(),
      createdAt: new Date().toISOString()
    };

    return this.http.post<AdminNotification>(this.apiUrl, newNotification).pipe(
      catchError(this.handleError<AdminNotification>('createNotification'))
    );
  }

  // Create order notification
  createOrderNotification(orderId: string, customerName: string, totalAmount: number): Observable<AdminNotification> {
    return this.createNotification({
      type: 'order',
      title: 'New Order Received',
      message: `New order #${orderId.substring(0, 8)} from ${customerName} for ₹${totalAmount}`,
      isRead: false,
      priority: 'medium',
      data: { orderId, customerName, totalAmount }
    });
  }

  // Create low stock notification
  createLowStockNotification(bookId: string, bookTitle: string, currentStock: number): Observable<AdminNotification> {
    return this.createNotification({
      type: 'low_stock',
      title: 'Low Stock Alert',
      message: `Book "${bookTitle}" (ID: ${bookId}) has only ${currentStock} units remaining`,
      isRead: false,
      priority: 'high',
      data: { bookId, bookTitle, currentStock }
    });
  }

  // Create system notification
  createSystemNotification(title: string, message: string, priority: 'low' | 'medium' | 'high' = 'low'): Observable<AdminNotification> {
    return this.createNotification({
      type: 'system',
      title,
      message,
      isRead: false,
      priority
    });
  }

  // Get all notifications
  getNotifications(): Observable<AdminNotification[]> {
    return this.http.get<AdminNotification[]>(this.apiUrl).pipe(
      catchError(this.handleError<AdminNotification[]>('getNotifications', []))
    );
  }

  // Mark notification as read
  markAsRead(notificationId: string): Observable<AdminNotification> {
    return this.http.patch<AdminNotification>(`${this.apiUrl}/${notificationId}`, { isRead: true }).pipe(
      catchError(this.handleError<AdminNotification>('markAsRead'))
    );
  }

  // Delete notification
  deleteNotification(notificationId: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${notificationId}`).pipe(
      catchError(this.handleError<any>('deleteNotification'))
    );
  }

  // Clear all notifications
  clearAllNotifications(): Observable<any> {
    return this.http.delete(this.apiUrl).pipe(
      catchError(this.handleError<any>('clearAllNotifications'))
    );
  }

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.error(`${operation} failed:`, error);
      return of(result as T);
    };
  }
} 