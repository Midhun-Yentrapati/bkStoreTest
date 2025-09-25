import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AdminNotificationsService, AdminNotification } from '../../../services/admin-notifications.service';
import { interval, Subscription } from 'rxjs';




@Component({
  selector: 'app-admin-notifications',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './admin-notifications.component.html',
  styleUrls: ['./admin-notifications.component.css']
})
export class AdminNotificationsComponent implements OnInit, OnDestroy {
  notifications: AdminNotification[] = [];
  isLoading: boolean = true;
  error: string | null = null;
  showNotifications: boolean = false;
  unreadCount: number = 0;
  
  // Filtering
  typeFilter: string = 'all';
  priorityFilter: string = 'all';
  searchQuery: string = '';

  private refreshSubscription?: Subscription;

  constructor(private adminNotificationsService: AdminNotificationsService) {}

  ngOnInit(): void {
    this.loadNotifications();
    this.startAutoRefresh();
  }

  ngOnDestroy(): void {
    if (this.refreshSubscription) {
      this.refreshSubscription.unsubscribe();
    }
  }

  startAutoRefresh(): void {
    // Refresh notifications every 30 seconds
    this.refreshSubscription = interval(30000).subscribe(() => {
      this.loadNotifications();
    });
  }

  loadNotifications(): void {
    this.isLoading = true;
    this.error = null;

    this.adminNotificationsService.getNotifications().subscribe({
      next: (notifications: AdminNotification[]) => {
        this.notifications = notifications.sort((a: AdminNotification, b: AdminNotification) => 
          new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        );
        this.updateUnreadCount();
        this.isLoading = false;
        console.log('Notifications loaded:', this.notifications);
      },
      error: (error: any) => {
        console.error('Error loading notifications:', error);
        
        // Handle different error types
        if (error.status === 503) {
          this.error = 'Notification service is temporarily unavailable.';
          console.warn('Notification service unavailable (503) - continuing without notifications');
        } else if (error.status === 404) {
          this.error = 'Notification service not found.';
          console.warn('Notification service not found (404) - continuing without notifications');
        } else {
          this.error = 'Failed to load notifications. Please try again.';
        }
        
        // Set empty notifications array to prevent UI issues
        this.notifications = [];
        this.unreadCount = 0;
        this.isLoading = false;
        
        // Don't show error in UI for service unavailable - just log it
        if (error.status === 503 || error.status === 404) {
          this.error = null; // Hide error message from UI
        }
      }
    });
  }

  getFilteredNotifications(): AdminNotification[] {
    let filtered = this.notifications;

    // Type filter
    if (this.typeFilter !== 'all') {
      filtered = filtered.filter(notification => notification.type === this.typeFilter);
    }

    // Priority filter
    if (this.priorityFilter !== 'all') {
      filtered = filtered.filter(notification => notification.priority === this.priorityFilter);
    }

    // Search filter
    if (this.searchQuery.trim()) {
      const query = this.searchQuery.toLowerCase();
      filtered = filtered.filter(notification => 
        notification.title.toLowerCase().includes(query) ||
        notification.message.toLowerCase().includes(query)
      );
    }

    return filtered;
  }

  markAsRead(notificationId: string): void {
    const notification = this.notifications.find(n => n.id === notificationId);
    if (notification && !notification.isRead) {
      notification.isRead = true;
      this.updateUnreadCount();
      
      // Update in backend
      this.adminNotificationsService.markAsRead(notificationId).subscribe({
        error: (error: any) => {
          console.error('Error marking notification as read:', error);
          // Revert if update fails
          notification.isRead = false;
          this.updateUnreadCount();
        }
      });
    }
  }

  markAllAsRead(): void {
    const unreadNotifications = this.notifications.filter(n => !n.isRead);
    if (unreadNotifications.length === 0) return;

    unreadNotifications.forEach(notification => {
      notification.isRead = true;
    });
    this.updateUnreadCount();

    // Update all in backend
    const updatePromises = unreadNotifications.map(notification =>
      this.adminNotificationsService.markAsRead(notification.id).toPromise()
    );

    Promise.all(updatePromises).catch((error: any) => {
      console.error('Error marking all notifications as read:', error);
      // Revert if update fails
      unreadNotifications.forEach(notification => {
        notification.isRead = false;
      });
      this.updateUnreadCount();
    });
  }

  deleteNotification(notificationId: string): void {
    if (confirm('Are you sure you want to delete this notification?')) {
      this.adminNotificationsService.deleteNotification(notificationId).subscribe({
        next: () => {
          this.notifications = this.notifications.filter(n => n.id !== notificationId);
          this.updateUnreadCount();
        },
        error: (error: any) => {
          console.error('Error deleting notification:', error);
          alert('Failed to delete notification. Please try again.');
        }
      });
    }
  }

  clearAllNotifications(): void {
    if (confirm('Are you sure you want to clear all notifications? This action cannot be undone.')) {
      this.adminNotificationsService.clearAllNotifications().subscribe({
        next: () => {
          this.notifications = [];
          this.updateUnreadCount();
        },
        error: (error: any) => {
          console.error('Error clearing notifications:', error);
          alert('Failed to clear notifications. Please try again.');
        }
      });
    }
  }

  private updateUnreadCount(): void {
    this.unreadCount = this.notifications.filter(n => !n.isRead).length;
  }

  getPriorityBadgeClass(priority: string): string {
    const priorityClasses: { [key: string]: string } = {
      'low': 'badge-info',
      'medium': 'badge-warning',
      'high': 'badge-danger'
    };
    return priorityClasses[priority] || 'badge-secondary';
  }

  getTypeIcon(type: string): string {
    const icons: { [key: string]: string } = {
      'order': 'M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2',
      'low_stock': 'M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z',
      'system': 'M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z'
    };
    return icons[type] || icons['system'];
  }

  getTypeColor(type: string): string {
    const colors: { [key: string]: string } = {
      'order': 'text-blue-600 bg-blue-100',
      'low_stock': 'text-red-600 bg-red-100',
      'system': 'text-gray-600 bg-gray-100'
    };
    return colors[type] || colors['system'];
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diffInHours = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60));

    if (diffInHours < 1) {
      return 'Just now';
    } else if (diffInHours < 24) {
      return `${diffInHours} hour${diffInHours > 1 ? 's' : ''} ago`;
    } else {
      return date.toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    }
  }

  toggleNotifications(): void {
    this.showNotifications = !this.showNotifications;
  }

  closeNotifications(): void {
    this.showNotifications = false;
  }

  getNotificationCountByType(type: string): number {
    if (type === 'all') return this.notifications.length;
    return this.notifications.filter(n => n.type === type).length;
  }

  getNotificationCountByPriority(priority: string): number {
    if (priority === 'all') return this.notifications.length;
    return this.notifications.filter(n => n.priority === priority).length;
  }
} 