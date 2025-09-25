import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AdminLoginComponent } from './admin-login.component';
import { AuthService } from '../../../services/auth.service';

describe('AdminLoginComponent', () => {
  let component: AdminLoginComponent;
  let fixture: ComponentFixture<AdminLoginComponent>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockRouter: jasmine.SpyObj<Router>;

  const mockAdminUser = {
    id: '1',
    username: 'admin',
    email: 'admin@test.com',
    passwordHash: 'password123',
    role: 'admin' as const
  };

  beforeEach(async () => {
    mockAuthService = jasmine.createSpyObj('AuthService', ['login']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [AdminLoginComponent, ReactiveFormsModule],
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminLoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with empty form', () => {
    expect(component.loginForm.get('email')?.value).toBe('');
    expect(component.loginForm.get('password')?.value).toBe('');
  });

  it('should validate required fields', () => {
    const form = component.loginForm;
    expect(form.valid).toBeFalsy();
    
    form.patchValue({ email: 'test@test.com', password: 'Password123!' });
    expect(form.valid).toBeTruthy();
  });

  it('should validate email format', () => {
    const emailControl = component.loginForm.get('email');
    emailControl?.setValue('invalid-email');
    expect(emailControl?.errors?.['email']).toBeTruthy();
    
    emailControl?.setValue('valid@email.com');
    expect(emailControl?.errors?.['email']).toBeFalsy();
  });

  it('should validate password requirements', () => {
    const passwordControl = component.loginForm.get('password');
    
    passwordControl?.setValue('weak');
    expect(passwordControl?.errors?.['minlength']).toBeTruthy();
    
    passwordControl?.setValue('Password123!');
    expect(passwordControl?.errors).toBeFalsy();
  });

  it('should handle successful login', () => {
    mockAuthService.login.and.returnValue(of(mockAdminUser));
    
    component.loginForm.patchValue({
      email: 'admin@test.com',
      password: 'Password123!'
    });
    
    component.onSubmit();
    
    expect(mockAuthService.login).toHaveBeenCalledWith('admin@test.com', 'Password123!');
    expect(component.successMessage).toContain('Login successful');
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/admin-main']);
  });

  it('should handle login error', () => {
    const errorMessage = 'Invalid credentials';
    mockAuthService.login.and.returnValue(throwError(() => ({ message: errorMessage })));
    
    component.loginForm.patchValue({
      email: 'admin@test.com',
      password: 'wrongpassword'
    });
    
    component.onSubmit();
    
    expect(component.errorMessage).toBe(errorMessage);
    expect(component.isLoading).toBeFalse();
  });

  it('should not submit if form is invalid', () => {
    component.loginForm.patchValue({
      email: '',
      password: ''
    });
    
    component.onSubmit();
    
    expect(mockAuthService.login).not.toHaveBeenCalled();
  });

  it('should show loading state during login', () => {
    mockAuthService.login.and.returnValue(of(mockAdminUser));
    
    component.loginForm.patchValue({
      email: 'admin@test.com',
      password: 'Password123!'
    });
    
    component.onSubmit();
    
    expect(component.isLoading).toBeTrue();
  });
});
