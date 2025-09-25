import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { AdminSignupComponent } from './admin-signup.component';
import { AuthService } from '../../../services/auth.service';

describe('AdminSignupComponent', () => {
  let component: AdminSignupComponent;
  let fixture: ComponentFixture<AdminSignupComponent>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    mockAuthService = jasmine.createSpyObj('AuthService', ['signup']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    
    await TestBed.configureTestingModule({
      imports: [AdminSignupComponent],
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: ActivatedRoute, useValue: {} },
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();
    
    fixture = TestBed.createComponent(AdminSignupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form on ngOnInit', () => {
    component.ngOnInit();
    expect(component.signupForm).toBeDefined();
    expect(component.signupForm.get('email')).toBeDefined();
    expect(component.signupForm.get('password')).toBeDefined();
    expect(component.signupForm.get('confirmPassword')).toBeDefined();
  });

  it('should validate password match', () => {
    component.ngOnInit();
    component.signupForm.patchValue({
      password: 'Abc1234!',
      confirmPassword: 'Abc1234!'
    });
    expect(component.signupForm.errors).toBeNull();
    
    component.signupForm.patchValue({
      confirmPassword: 'Different!'
    });
    expect(component.signupForm.errors).toEqual({ mismatch: true });
  });

  it('should not call signup if form is invalid', () => {
    component.ngOnInit();
    component.signupForm.patchValue({
      email: '',
      password: ''
    });
    
    component.onSubmit();
    
    expect(mockAuthService.signup).not.toHaveBeenCalled();
  });

  it('should set error if passwords do not match', () => {
    component.ngOnInit();
    component.signupForm.patchValue({
      username: 'admin',
      email: 'admin@example.com',
      password: 'Abc1234!',
      confirmPassword: 'Different!'
    });
    
    component.onSubmit();
    
    expect(component.errorMessage).toContain('Passwords do not match');
  });

  it('should set successMessage and navigate if signup returns true', () => {
    mockAuthService.signup.and.returnValue(of(true));
    
    component.ngOnInit();
    component.signupForm.patchValue({
      username: 'admin',
      email: 'admin@example.com',
      password: 'Abc1234!',
      confirmPassword: 'Abc1234!'
    });
    
    component.onSubmit();
    
    expect(component.successMessage).toContain('Signup successful');
    expect(mockRouter.navigate).toHaveBeenCalled();
    expect(component.isLoading).toBeFalse();
  });

  it('should set errorMessage if signup returns false', () => {
    mockAuthService.signup.and.returnValue(of(false));
    
    component.ngOnInit();
    component.signupForm.patchValue({
      username: 'admin',
      email: 'admin@example.com',
      password: 'Abc1234!',
      confirmPassword: 'Abc1234!'
    });
    
    component.onSubmit();
    
    expect(component.errorMessage).toContain('Signup failed');
    expect(component.isLoading).toBeFalse();
  });

  it('should handle error from signup observable', () => {
    mockAuthService.signup.and.returnValue(throwError(() => new Error('Signup error')));
    
    component.ngOnInit();
    component.signupForm.patchValue({
      username: 'admin',
      email: 'admin@example.com',
      password: 'Abc1234!',
      confirmPassword: 'Abc1234!'
    });
    
    component.onSubmit();
    
    expect(component.errorMessage).toContain('An error occurred');
    expect(component.isLoading).toBeFalse();
  });

  it('should set loading state during submission', () => {
    mockAuthService.signup.and.returnValue(of(true));
    
    component.ngOnInit();
    component.signupForm.patchValue({
      username: 'admin',
      email: 'admin@example.com',
      password: 'Abc1234!',
      confirmPassword: 'Abc1234!'
    });
    
    component.onSubmit();
    
    expect(component.isLoading).toBeTrue();
  });
});
