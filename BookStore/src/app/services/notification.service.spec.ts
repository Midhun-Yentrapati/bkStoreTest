import { TestBed } from '@angular/core/testing';
import { NotificationService, Notification } from './notification.service';

describe('NotificationService', () => {
  let service: NotificationService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [NotificationService]
    });
    service = TestBed.inject(NotificationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('success notifications', () => {
    it('should create success notification', () => {
      const id = service.success('Success Title', 'Success message');
      expect(id).toBeDefined();
      expect(typeof id).toBe('string');
    });

    it('should create success notification with custom duration', () => {
      const id = service.success('Success Title', 'Success message', 3000);
      expect(id).toBeDefined();
    });

    it('should add success notification to notifications list', (done) => {
      service.notifications$.subscribe(notifications => {
        if (notifications.length > 0) {
          const notification = notifications[notifications.length - 1];
          expect(notification.type).toBe('success');
          expect(notification.title).toBe('Test Success');
          expect(notification.message).toBe('Test success message');
          done();
        }
      });

      service.success('Test Success', 'Test success message');
    });
  });

  describe('error notifications', () => {
    it('should create error notification', () => {
      const id = service.error('Error Title', 'Error message');
      expect(id).toBeDefined();
      expect(typeof id).toBe('string');
    });

    it('should create error notification with custom duration', () => {
      const id = service.error('Error Title', 'Error message', 10000);
      expect(id).toBeDefined();
    });

    it('should use default duration for error notifications', (done) => {
      service.notifications$.subscribe(notifications => {
        if (notifications.length > 0) {
          const notification = notifications[notifications.length - 1];
          expect(notification.duration).toBe(8000);
          done();
        }
      });

      service.error('Test Error', 'Test error message');
    });

    it('should add error notification to notifications list', (done) => {
      service.notifications$.subscribe(notifications => {
        if (notifications.length > 0) {
          const notification = notifications[notifications.length - 1];
          expect(notification.type).toBe('error');
          expect(notification.title).toBe('Test Error');
          expect(notification.message).toBe('Test error message');
          done();
        }
      });

      service.error('Test Error', 'Test error message');
    });
  });

  describe('warning notifications', () => {
    it('should create warning notification', () => {
      const id = service.warning('Warning Title', 'Warning message');
      expect(id).toBeDefined();
      expect(typeof id).toBe('string');
    });

    it('should create warning notification with custom duration', () => {
      const id = service.warning('Warning Title', 'Warning message', 4000);
      expect(id).toBeDefined();
    });

    it('should add warning notification to notifications list', (done) => {
      service.notifications$.subscribe(notifications => {
        if (notifications.length > 0) {
          const notification = notifications[notifications.length - 1];
          expect(notification.type).toBe('warning');
          expect(notification.title).toBe('Test Warning');
          expect(notification.message).toBe('Test warning message');
          done();
        }
      });

      service.warning('Test Warning', 'Test warning message');
    });
  });

  describe('info notifications', () => {
    it('should create info notification', () => {
      const id = service.info('Info Title', 'Info message');
      expect(id).toBeDefined();
      expect(typeof id).toBe('string');
    });

    it('should create info notification with custom duration', () => {
      const id = service.info('Info Title', 'Info message', 6000);
      expect(id).toBeDefined();
    });

    it('should add info notification to notifications list', (done) => {
      service.notifications$.subscribe(notifications => {
        if (notifications.length > 0) {
          const notification = notifications[notifications.length - 1];
          expect(notification.type).toBe('info');
          expect(notification.title).toBe('Test Info');
          expect(notification.message).toBe('Test info message');
          done();
        }
      });

      service.info('Test Info', 'Test info message');
    });
  });

  describe('removeNotification', () => {
    it('should remove specific notification by id', (done) => {
      const id = service.success('Test', 'Test message');
      
      service.notifications$.subscribe(notifications => {
        if (notifications.length === 0) {
          done();
        }
      });

      service.removeNotification(id);
    });

    it('should not affect other notifications when removing one', (done) => {
      const id1 = service.success('Test 1', 'Test message 1');
      const id2 = service.success('Test 2', 'Test message 2');
      
      service.notifications$.subscribe(notifications => {
        if (notifications.length === 1) {
          expect(notifications[0].id).toBe(id2);
          done();
        }
      });

      service.removeNotification(id1);
    });
  });

  describe('clearAll', () => {
    it('should remove all notifications', (done) => {
      service.success('Test 1', 'Test message 1');
      service.success('Test 2', 'Test message 2');
      
      service.notifications$.subscribe(notifications => {
        if (notifications.length === 0) {
          done();
        }
      });

      service.clearAll();
    });
  });

  describe('convenience methods', () => {
    it('should create cart success notification', () => {
      const id = service.cartSuccess('Test Book');
      expect(id).toBeDefined();
      
      service.notifications$.subscribe(notifications => {
        if (notifications.length > 0) {
          const notification = notifications[notifications.length - 1];
          expect(notification.type).toBe('success');
          expect(notification.title).toBe('Added to Cart');
          expect(notification.message).toBe('"Test Book" has been added to your cart.');
        }
      });
    });

    it('should create cart error notification', () => {
      const id = service.cartError('Test Book', 'Custom error message');
      expect(id).toBeDefined();
      
      service.notifications$.subscribe(notifications => {
        if (notifications.length > 0) {
          const notification = notifications[notifications.length - 1];
          expect(notification.type).toBe('error');
          expect(notification.title).toBe('Cart Error');
          expect(notification.message).toBe('Custom error message');
        }
      });
    });

    it('should create cart error notification with default message', () => {
      const id = service.cartError('Test Book');
      expect(id).toBeDefined();
      
      service.notifications$.subscribe(notifications => {
        if (notifications.length > 0) {
          const notification = notifications[notifications.length - 1];
          expect(notification.type).toBe('error');
          expect(notification.title).toBe('Cart Error');
          expect(notification.message).toBe('Failed to add "Test Book" to cart. Please try again.');
        }
      });
    });

    it('should create wishlist success notification', () => {
      const id = service.wishlistSuccess('Test Book');
      expect(id).toBeDefined();
      
      service.notifications$.subscribe(notifications => {
        if (notifications.length > 0) {
          const notification = notifications[notifications.length - 1];
          expect(notification.type).toBe('success');
          expect(notification.title).toBe('Added to Wishlist');
          expect(notification.message).toBe('"Test Book" has been added to your wishlist.');
        }
      });
    });

    it('should create wishlist error notification', () => {
      const id = service.wishlistError('Test Book', 'Custom error message');
      expect(id).toBeDefined();
      
      service.notifications$.subscribe(notifications => {
        if (notifications.length > 0) {
          const notification = notifications[notifications.length - 1];
          expect(notification.type).toBe('error');
          expect(notification.title).toBe('Wishlist Error');
          expect(notification.message).toBe('Custom error message');
        }
      });
    });

    it('should create wishlist error notification with default message', () => {
      const id = service.wishlistError('Test Book');
      expect(id).toBeDefined();
      
      service.notifications$.subscribe(notifications => {
        if (notifications.length > 0) {
          const notification = notifications[notifications.length - 1];
          expect(notification.type).toBe('error');
          expect(notification.title).toBe('Wishlist Error');
          expect(notification.message).toBe('Failed to add "Test Book" to wishlist. Please try again.');
        }
      });
    });

    it('should create auth error notification', () => {
      const id = service.authError('Please log in to continue');
      expect(id).toBeDefined();
      
      service.notifications$.subscribe(notifications => {
        if (notifications.length > 0) {
          const notification = notifications[notifications.length - 1];
          expect(notification.type).toBe('error');
          expect(notification.title).toBe('Authentication Required');
          expect(notification.message).toBe('Please log in to continue');
        }
      });
    });

    it('should create network error notification', () => {
      const id = service.networkError();
      expect(id).toBeDefined();
      
      service.notifications$.subscribe(notifications => {
        if (notifications.length > 0) {
          const notification = notifications[notifications.length - 1];
          expect(notification.type).toBe('error');
          expect(notification.title).toBe('Network Error');
          expect(notification.message).toBe('Please check your internet connection and try again.');
          expect(notification.duration).toBe(10000);
        }
      });
    });
  });

  describe('notification properties', () => {
    it('should generate unique ids for notifications', () => {
      const id1 = service.success('Test 1', 'Message 1');
      const id2 = service.success('Test 2', 'Message 2');
      expect(id1).not.toBe(id2);
    });

    it('should set timestamp for notifications', (done) => {
      service.notifications$.subscribe(notifications => {
        if (notifications.length > 0) {
          const notification = notifications[notifications.length - 1];
          expect(notification.timestamp).toBeInstanceOf(Date);
          done();
        }
      });

      service.success('Test', 'Test message');
    });

    it('should use default duration for notifications', (done) => {
      service.notifications$.subscribe(notifications => {
        if (notifications.length > 0) {
          const notification = notifications[notifications.length - 1];
          expect(notification.duration).toBe(5000);
          done();
        }
      });

      service.success('Test', 'Test message');
    });

    it('should use custom duration when provided', (done) => {
      service.notifications$.subscribe(notifications => {
        if (notifications.length > 0) {
          const notification = notifications[notifications.length - 1];
          expect(notification.duration).toBe(3000);
          done();
        }
      });

      service.success('Test', 'Test message', 3000);
    });
  });

  describe('observable', () => {
    it('should expose notifications$ observable', () => {
      expect(service.notifications$).toBeDefined();
    });

    it('should emit notifications when added', (done) => {
      let count = 0;
      service.notifications$.subscribe(notifications => {
        count++;
        if (count === 1) {
          expect(notifications.length).toBe(1);
          done();
        }
      });

      service.success('Test', 'Test message');
    });
  });
}); 