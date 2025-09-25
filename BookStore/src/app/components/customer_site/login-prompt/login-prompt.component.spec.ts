import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { LoginPromptComponent } from './login-prompt.component';
import { AuthService } from '../../../services/auth.service';
import { UserModel } from '../../../models/user.model';

describe('LoginPromptComponent', () => {
  let component: LoginPromptComponent;
  let fixture: ComponentFixture<LoginPromptComponent>;
  let mockAuthService: jasmine.SpyObj<AuthService>;

  const mockUser: UserModel = {
    id: '1',
    fullName: 'Test User',
    username: 'testuser',
    email: 'test@test.com',
    mobileNumber: '1234567890'
  };

  beforeEach(async () => {
    mockAuthService = jasmine.createSpyObj('AuthService', ['login']);
    // Mock the login method to handle the credentials object overload
    (mockAuthService.login as any).and.returnValue(of(mockUser));

    await TestBed.configureTestingModule({
      imports: [LoginPromptComponent, ReactiveFormsModule],
      providers: [
        { provide: AuthService, useValue: mockAuthService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginPromptComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with empty form', () => {
    expect(component.loginForm.get('identifier')?.value).toBe('');
    expect(component.loginForm.get('password')?.value).toBe('');
  });

  it('should validate required fields', () => {
    const form = component.loginForm;
    expect(form.valid).toBeFalsy();
    
    form.patchValue({ identifier: 'testuser', password: 'password123' });
    expect(form.valid).toBeTruthy();
  });

  it('should handle successful login', () => {
    (mockAuthService.login as any).and.returnValue(of(mockUser));
    
    component.loginForm.patchValue({
      identifier: 'testuser',
      password: 'password123'
    });
    
    component.onSubmit();
    
    expect(mockAuthService.login).toHaveBeenCalled();
    expect(component.successMessage).toBe('Login successful!');
    expect(component.isLoading).toBeFalse();
  });

  it('should handle login error', () => {
    const errorMessage = 'Invalid credentials';
    (mockAuthService.login as any).and.returnValue(throwError(() => ({ message: errorMessage })));
    
    component.loginForm.patchValue({
      identifier: 'testuser',
      password: 'wrongpassword'
    });
    
    component.onSubmit();
    
    expect(component.errorMessage).toBe(errorMessage);
    expect(component.isLoading).toBeFalse();
  });

  it('should not submit if form is invalid', () => {
    component.loginForm.patchValue({
      identifier: '',
      password: ''
    });
    
    component.onSubmit();
    
    expect(mockAuthService.login).not.toHaveBeenCalled();
    expect(component.errorMessage).toBe('Please fill in all fields correctly.');
  });

  it('should emit close event', () => {
    spyOn(component.close, 'emit');
    
    component.onClose();
    
    expect(component.close.emit).toHaveBeenCalled();
  });
});
