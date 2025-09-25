import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AdminNotificationsService } from './admin-notifications.service';

describe('AdminNotificationsService', () => {
  let service: AdminNotificationsService;
  let httpMock: HttpTestingController;

  const mockNotification = {
    id: '1',
    type: 'order' as const,
    title: 'New Order',
    message: 'Order #123 has been placed',
    isRead: false,
    createdAt: '2023-01-01T00:00:00Z',
    priority: 'medium' as const,
    data: { orderId: '123', amount: 59.98 }
  };

  const mockNotifications = [
    mockNotification,
    {
      ...mockNotification,
      id: '2',
      type: 'low_stock' as const,
      title: 'Low Stock Alert',
      message: 'Book "Test Book" is running low on stock'
    }
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AdminNotificationsService]
    });
    service = TestBed.inject(AdminNotificationsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getNotifications', () => {
    it('should retrieve admin notifications successfully', () => {
      service.getNotifications().subscribe(notifications => {
        expect(notifications).toEqual(mockNotifications);
      });

      const req = httpMock.expectOne('http://localhost:3000/admin_notifications');
      expect(req.request.method).toBe('GET');
      req.flush(mockNotifications);
    });

    it('should handle errors when retrieving notifications', () => {
      service.getNotifications().subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        }
      });

      const req = httpMock.expectOne('http://localhost:3000/admin_notifications');
      req.error(new ErrorEvent('Network error'));
    });
  });

  describe('createNotification', () => {
    it('should create admin notification successfully', () => {
      const newNotification = { ...mockNotification, id: undefined, createdAt: undefined };
      
      service.createNotification(newNotification).subscribe(notification => {
        expect(notification.id).toBeDefined();
        expect(notification.createdAt).toBeDefined();
        expect(notification.type).toBe(newNotification.type);
        expect(notification.title).toBe(newNotification.title);
        expect(notification.message).toBe(newNotification.message);
      });

      const req = httpMock.expectOne('http://localhost:3000/admin_notifications');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(newNotification);
      
      // Mock the response with generated id and timestamp
      const responseNotification = {
        ...newNotification,
        id: 'generated-id',
        createdAt: '2023-01-01T00:00:00Z'
      };
      req.flush(responseNotification);
    });

    it('should handle errors when creating notification', () => {
      const newNotification = { ...mockNotification, id: undefined, createdAt: undefined };
      
      service.createNotification(newNotification).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        }
      });

      const req = httpMock.expectOne('http://localhost:3000/admin_notifications');
      req.error(new ErrorEvent('Bad Request'), { status: 400 });
    });
  });

  describe('markAsRead', () => {
    it('should mark notification as read successfully', () => {
      const updatedNotification = { ...mockNotification, isRead: true };
      
      service.markAsRead('1').subscribe(notification => {
        expect(notification).toEqual(updatedNotification);
      });

      const req = httpMock.expectOne('http://localhost:3000/admin_notifications/1');
      expect(req.request.method).toBe('PATCH');
      expect(req.request.body).toEqual({ isRead: true });
      req.flush(updatedNotification);
    });

    it('should handle errors when marking notification as read', () => {
      service.markAsRead('1').subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        }
      });

      const req = httpMock.expectOne('http://localhost:3000/admin_notifications/1');
      req.error(new ErrorEvent('Not Found'), { status: 404 });
    });
  });

  describe('deleteNotification', () => {
    it('should delete notification successfully', () => {
      service.deleteNotification('1').subscribe(response => {
        expect(response).toBeTruthy();
      });

      const req = httpMock.expectOne('http://localhost:3000/admin_notifications/1');
      expect(req.request.method).toBe('DELETE');
      req.flush({ success: true });
    });

    it('should handle errors when deleting notification', () => {
      service.deleteNotification('1').subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        }
      });

      const req = httpMock.expectOne('http://localhost:3000/admin_notifications/1');
      req.error(new ErrorEvent('Not Found'), { status: 404 });
    });
  });

  describe('clearAllNotifications', () => {
    it('should clear all notifications successfully', () => {
      service.clearAllNotifications().subscribe(response => {
        expect(response).toBeTruthy();
      });

      const req = httpMock.expectOne('http://localhost:3000/admin_notifications');
      expect(req.request.method).toBe('DELETE');
      req.flush({ success: true });
    });

    it('should handle errors when clearing all notifications', () => {
      service.clearAllNotifications().subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        }
      });

      const req = httpMock.expectOne('http://localhost:3000/admin_notifications');
      req.error(new ErrorEvent('Server Error'), { status: 500 });
    });
  });

  describe('createOrderNotification', () => {
    it('should create order notification with correct data', () => {
      service.createOrderNotification('order123', 'John Doe', 99.99).subscribe(notification => {
        expect(notification.type).toBe('order');
        expect(notification.title).toBe('New Order Received');
        expect(notification.message).toContain('order123');
        expect(notification.message).toContain('John Doe');
        expect(notification.message).toContain('99.99');
        expect(notification.priority).toBe('medium');
      });

      const req = httpMock.expectOne('http://localhost:3000/admin_notifications');
      expect(req.request.method).toBe('POST');
      req.flush(mockNotification);
    });
  });

  describe('createLowStockNotification', () => {
    it('should create low stock notification with correct data', () => {
      service.createLowStockNotification('book1', 'Test Book', 5).subscribe(notification => {
        expect(notification.type).toBe('low_stock');
        expect(notification.title).toBe('Low Stock Alert');
        expect(notification.message).toContain('Test Book');
        expect(notification.message).toContain('5');
        expect(notification.priority).toBe('high');
      });

      const req = httpMock.expectOne('http://localhost:3000/admin_notifications');
      expect(req.request.method).toBe('POST');
      req.flush(mockNotification);
    });
  });

  describe('createSystemNotification', () => {
    it('should create system notification with correct data', () => {
      service.createSystemNotification('System Update', 'System will be down for maintenance', 'high').subscribe(notification => {
        expect(notification.type).toBe('system');
        expect(notification.title).toBe('System Update');
        expect(notification.message).toBe('System will be down for maintenance');
        expect(notification.priority).toBe('high');
      });

      const req = httpMock.expectOne('http://localhost:3000/admin_notifications');
      expect(req.request.method).toBe('POST');
      req.flush(mockNotification);
    });
  });
}); 