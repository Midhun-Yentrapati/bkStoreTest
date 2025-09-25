import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { UserModel } from '../models/user.model';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should register user', () => {
    const userData = { fullName: 'Test User', username: 'testuser', email: 'test@test.com', mobileNumber: '1234567890', password: 'password123' };
    const registeredUser: UserModel = { id: '1', ...userData };

    service.register(userData).subscribe(user => {
      expect(user).toEqual(registeredUser);
    });

    const req = httpMock.expectOne('http://localhost:3000/users');
    expect(req.request.method).toBe('GET');
    req.flush([]); // First request checks for existing users

    const postReq = httpMock.expectOne('http://localhost:3000/users');
    expect(postReq.request.method).toBe('POST');
    postReq.flush(registeredUser);
  });

  it('should login customer user', () => {
    const loginData = { identifier: 'testuser', password: 'password123' };
    const mockUser: UserModel = { id: '1', fullName: 'Test User', username: 'testuser', email: 'test@test.com', mobileNumber: '1234567890' };

    service.login(loginData).subscribe(user => {
      expect(user).toEqual(mockUser);
    });

    const req = httpMock.expectOne('http://localhost:3000/users');
    expect(req.request.method).toBe('GET');
    req.flush([{ ...mockUser, password: 'password123' }]);
  });

  it('should login admin user', () => {
    const mockAdminUser = { id: 'admin1', username: 'admin', email: 'admin@test.com', passwordHash: 'admin123', role: 'admin' as const };

    service.login('admin@test.com', 'admin123').subscribe(user => {
      expect(user).toEqual(mockAdminUser);
    });

    const req = httpMock.expectOne('http://localhost:3000/adminUsers');
    expect(req.request.method).toBe('GET');
    req.flush([mockAdminUser]);
  });

  it('should get current customer', () => {
    const mockUser: UserModel = { id: '1', fullName: 'Test User', username: 'testuser', email: 'test@test.com', mobileNumber: '1234567890' };
    service.currentCustomer.set(mockUser);
    
    expect(service.getCurrentCustomer()).toEqual(mockUser);
  });

  it('should get current admin', () => {
    const mockAdminUser = { id: 'admin1', username: 'admin', email: 'admin@test.com', passwordHash: 'admin123', role: 'admin' as const };
    service.currentAdmin.set(mockAdminUser);
    
    expect(service.getCurrentAdmin()).toEqual(mockAdminUser);
  });

  it('should logout user', () => {
    const mockUser: UserModel = { id: '1', fullName: 'Test User', username: 'testuser', email: 'test@test.com', mobileNumber: '1234567890' };
    service.currentCustomer.set(mockUser);
    
    service.logout();
    
    expect(service.currentCustomer()).toBeNull();
    expect(service.currentAdmin()).toBeNull();
  });

  it('should check if user is logged in', () => {
    expect(service.isLoggedIn()).toBe(false);
    
    const mockUser: UserModel = { id: '1', fullName: 'Test User', username: 'testuser', email: 'test@test.com', mobileNumber: '1234567890' };
    service.currentCustomer.set(mockUser);
    
    expect(service.isLoggedIn()).toBe(true);
  });

  it('should check if admin is logged in', () => {
    expect(service.isAdminLoggedIn()).toBe(false);
    
    const mockAdminUser = { id: 'admin1', username: 'admin', email: 'admin@test.com', passwordHash: 'admin123', role: 'admin' as const };
    service.currentAdmin.set(mockAdminUser);
    
    expect(service.isAdminLoggedIn()).toBe(true);
  });
}); 