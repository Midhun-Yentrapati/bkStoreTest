import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { ForgotPasswordComponent } from './forgot-password.component';
import { AuthService } from '../../../services/auth.service';



describe('ForgotPasswordComponent', () => {
  let component: ForgotPasswordComponent;
  let fixture: ComponentFixture<ForgotPasswordComponent>;

  beforeEach(async () => {
    const mockAuthService = jasmine.createSpyObj('AuthService', ['forgotPassword']);
    mockAuthService.forgotPassword.and.returnValue(of(true));
    await TestBed.configureTestingModule({
      imports: [ForgotPasswordComponent, HttpClientTestingModule, ReactiveFormsModule],
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: ActivatedRoute, useValue: {} }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(ForgotPasswordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form on ngOnInit', () => {
    component.ngOnInit();
    expect(component.forgotPasswordForm).toBeDefined();
    expect(component.forgotPasswordForm.controls['email']).toBeDefined();
  });

  it('should not call forgotPassword if form is invalid', () => {
    component.ngOnInit();
    component.forgotPasswordForm.controls['email'].setValue('');
    const authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    component.onSubmit();
    expect(authService.forgotPassword).not.toHaveBeenCalled();
    expect(component.forgotPasswordForm.touched).toBeTrue();
  });

  it('should set successMessage if forgotPassword returns true', () => {
    component.ngOnInit();
    component.forgotPasswordForm.controls['email'].setValue('test@example.com');
    component.onSubmit();
    expect(component.successMessage).toContain('Password reset link has been sent');
    expect(component.isLoading).toBeFalse();
  });

  it('should set errorMessage if forgotPassword returns false', () => {
    const authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    authService.forgotPassword.and.returnValue(of(false));
    component.ngOnInit();
    component.forgotPasswordForm.controls['email'].setValue('test@example.com');
    component.onSubmit();
    expect(component.errorMessage).toContain('Failed to send password reset link');
    expect(component.isLoading).toBeFalse();
  });

  it('should handle error from forgotPassword observable', () => {
    const authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    authService.forgotPassword.and.returnValue({ subscribe: (o: any) => o.error('err') } as any);
    component.ngOnInit();
    component.forgotPasswordForm.controls['email'].setValue('test@example.com');
    component.onSubmit();
    expect(component.errorMessage).toContain('An error occurred');
    expect(component.isLoading).toBeFalse();
  });

  it('should set loading true while submitting and false after', () => {
    component.ngOnInit();
    component.forgotPasswordForm.controls['email'].setValue('test@example.com');
    const orig = component.onSubmit;
    let loadingDuring = false;
    spyOn(component, 'onSubmit').and.callFake(function(this: ForgotPasswordComponent) {
      loadingDuring = this.isLoading;
      orig.apply(this);
    });
    component.onSubmit();
    expect(loadingDuring).toBeTrue();
    expect(component.isLoading).toBeFalse();
  });
});
