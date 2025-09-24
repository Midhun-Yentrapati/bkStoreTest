import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AddUserComponent } from './add-user.component';
import { AdminUser } from '../../../models/book';

describe('AddUserComponent', () => {
  let component: AddUserComponent;
  let fixture: ComponentFixture<AddUserComponent>;
  let mockRouter: jasmine.SpyObj<Router>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    
    await TestBed.configureTestingModule({
      imports: [AddUserComponent, ReactiveFormsModule, HttpClientTestingModule],
      providers: [
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();
    
    fixture = TestBed.createComponent(AddUserComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form on ngOnInit', () => {
    component.ngOnInit();
    expect(component.userForm).toBeDefined();
    expect(component.userForm.get('username')).toBeDefined();
    expect(component.userForm.get('email')).toBeDefined();
    expect(component.userForm.get('passwordHash')).toBeDefined();
  });

  it('should not add user if form is invalid', () => {
    component.userForm.patchValue({ username: '' });
    spyOn(console, 'log');
    spyOn(component.userForm, 'markAllAsTouched');
    
    component.addUser();
    
    expect(component.userForm.markAllAsTouched).toHaveBeenCalled();
    expect(console.log).toHaveBeenCalledWith('Form is invalid. Please check the fields.');
    expect(component.errorMessage).toBe('Please fill in all fields correctly.');
  });

  it('should add user if form is valid', () => {
    const mockUser: AdminUser = {
      id: '1',
      username: 'Test',
      email: 'test@example.com',
      passwordHash: 'TestPass123!'
    };

    component.userForm.patchValue({
      username: 'Test',
      email: 'test@example.com',
      passwordHash: 'TestPass123!'
    });

    spyOn(console, 'log');
    
    component.addUser();

    const req = httpMock.expectOne('http://localhost:3000/adminUsers');
    expect(req.request.method).toBe('POST');
    req.flush(mockUser);

    expect(console.log).toHaveBeenCalledWith('New Admin User Data:', jasmine.objectContaining({
      username: 'Test',
      email: 'test@example.com',
      passwordHash: 'TestPass123!'
    }));
    expect(component.successMessage).toBe('Admin user created successfully!');
  });

  it('should handle error from addUser', () => {
    component.userForm.patchValue({
      username: 'Test',
      email: 'test@example.com',
      passwordHash: 'TestPass123!'
    });

    spyOn(console, 'error');
    
    component.addUser();

    const req = httpMock.expectOne('http://localhost:3000/adminUsers');
    req.error(new ErrorEvent('Network error'));

    expect(console.error).toHaveBeenCalled();
    expect(component.errorMessage).toBe('Failed to create admin user. Please try again.');
  });

  it('should reset form after adding user successfully', () => {
    const mockUser: AdminUser = {
      id: '1',
      username: 'Test',
      email: 'test@example.com',
      passwordHash: 'TestPass123!'
    };

    component.userForm.patchValue({
      username: 'Test',
      email: 'test@example.com',
      passwordHash: 'TestPass123!'
    });
    
    component.addUser();

    const req = httpMock.expectOne('http://localhost:3000/adminUsers');
    req.flush(mockUser);

    expect(component.userForm.get('username')?.value).toBe('');
    expect(component.userForm.get('email')?.value).toBe('');
    expect(component.userForm.get('passwordHash')?.value).toBe('');
  });

  it('should set loading state during user creation', () => {
    const mockUser: AdminUser = {
      id: '1',
      username: 'Test',
      email: 'test@example.com',
      passwordHash: 'TestPass123!'
    };

    component.userForm.patchValue({
      username: 'Test',
      email: 'test@example.com',
      passwordHash: 'TestPass123!'
    });
    
    component.addUser();

    expect(component.isLoading).toBeTrue();

    const req = httpMock.expectOne('http://localhost:3000/adminUsers');
    req.flush(mockUser);

    expect(component.isLoading).toBeFalse();
  });
});
