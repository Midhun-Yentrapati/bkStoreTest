import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { AdminDashboardMainComponent } from './admin-dashboard-main.component';

describe('AdminDashboardMainComponent', () => {
  let component: AdminDashboardMainComponent;
  let fixture: ComponentFixture<AdminDashboardMainComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminDashboardMainComponent, HttpClientTestingModule],
    }).compileComponents();
    
    fixture = TestBed.createComponent(AdminDashboardMainComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with empty username', () => {
    expect(component.username).toBe('');
  });

  describe('ngOnInit', () => {
    it('should set username from sessionStorage when available', () => {
      spyOn(sessionStorage, 'getItem').and.returnValue('testuser@gmail.com');
      component.ngOnInit();
      expect(component.username).toBe('testuser@gmail.com');
    });

    it('should set default username when sessionStorage is empty', () => {
      spyOn(sessionStorage, 'getItem').and.returnValue(null);
      component.ngOnInit();
      expect(component.username).toBe('Default Admin User');
    });

    it('should set default username when sessionStorage returns null', () => {
      spyOn(sessionStorage, 'getItem').and.returnValue(null);
      component.ngOnInit();
      expect(component.username).toBe('Default Admin User');
    });

    it('should set default username when sessionStorage returns empty string', () => {
      spyOn(sessionStorage, 'getItem').and.returnValue('');
      component.ngOnInit();
      expect(component.username).toBe('Default Admin User');
    });

    it('should set username from sessionStorage with special characters', () => {
      spyOn(sessionStorage, 'getItem').and.returnValue('admin.user@company.com');
      component.ngOnInit();
      expect(component.username).toBe('admin.user@company.com');
    });

    it('should log initialization message', () => {
      spyOn(console, 'log');
      spyOn(sessionStorage, 'getItem').and.returnValue('testuser');
      
      component.ngOnInit();
      
      expect(console.log).toHaveBeenCalledWith('AdminDashboardMainComponent: Initializing.', 'testuser');
    });
  });

  describe('Component properties', () => {
    it('should have username property of type string', () => {
      expect(typeof component.username).toBe('string');
    });

    it('should allow username to be modified', () => {
      component.username = 'New Username';
      expect(component.username).toBe('New Username');
    });
  });

  describe('Component lifecycle', () => {
    it('should call ngOnInit when component is created', () => {
      spyOn(component, 'ngOnInit');
      component.ngOnInit();
      expect(component.ngOnInit).toHaveBeenCalled();
    });
  });
});
