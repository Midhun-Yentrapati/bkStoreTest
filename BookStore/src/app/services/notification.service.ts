import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface Notification {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  title: string;
  message: string;
  duration?: number;
  timestamp: Date;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private notificationsSubject = new BehaviorSubject<Notification[]>([]);
  notifications$ = this.notificationsSubject.asObservable();

  constructor() { }

  private addNotification(notification: Omit<Notification, 'id' | 'timestamp'>) {
    const newNotification: Notification = {
      ...notification,
      id: crypto.randomUUID(),
      timestamp: new Date(),
      duration: notification.duration || 5000
    };

    const currentNotifications = this.notificationsSubject.getValue();
    this.notificationsSubject.next([...currentNotifications, newNotification]);

    // Auto-remove notification after duration
    if (newNotification.duration && newNotification.duration > 0) {
      setTimeout(() => {
        this.removeNotification(newNotification.id);
      }, newNotification.duration);
    }

    return newNotification.id;
  }

  success(title: string, message: string, duration?: number): string {
    return this.addNotification({
      type: 'success',
      title,
      message,
      duration
    });
  }

  error(title: string, message: string, duration?: number): string {
    return this.addNotification({
      type: 'error',
      title,
      message,
      duration: duration || 8000 // Error messages stay longer
    });
  }

  warning(title: string, message: string, duration?: number): string {
    return this.addNotification({
      type: 'warning',
      title,
      message,
      duration
    });
  }

  info(title: string, message: string, duration?: number): string {
    return this.addNotification({
      type: 'info',
      title,
      message,
      duration
    });
  }

  removeNotification(id: string): void {
    const currentNotifications = this.notificationsSubject.getValue();
    const filteredNotifications = currentNotifications.filter(n => n.id !== id);
    this.notificationsSubject.next(filteredNotifications);
  }

  clearAll(): void {
    this.notificationsSubject.next([]);
  }

  // Convenience methods for common scenarios
  cartSuccess(bookTitle: string): string {
    return this.success(
      'Added to Cart',
      `"${bookTitle}" has been added to your cart.`
    );
  }

  cartError(bookTitle: string, error?: string): string {
    return this.error(
      'Cart Error',
      error || `Failed to add "${bookTitle}" to cart. Please try again.`
    );
  }

  wishlistSuccess(bookTitle: string): string {
    return this.success(
      'Added to Wishlist',
      `"${bookTitle}" has been added to your wishlist.`
    );
  }

  wishlistError(bookTitle: string, error?: string): string {
    return this.error(
      'Wishlist Error',
      error || `Failed to add "${bookTitle}" to wishlist. Please try again.`
    );
  }

  authError(message: string): string {
    return this.error(
      'Authentication Required',
      message
    );
  }

  networkError(): string {
    return this.error(
      'Network Error',
      'Please check your internet connection and try again.',
      10000
    );
  }
} 