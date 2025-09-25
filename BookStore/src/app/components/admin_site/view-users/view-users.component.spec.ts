import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { ViewUsersComponent } from './view-users.component';
import { AdminUser } from '../../../models/book';
import { UserModel } from '../../../models/user.model';

describe('ViewUsersComponent', () => {
  let component: ViewUsersComponent;
  let fixture: ComponentFixture<ViewUsersComponent>;
  let mockHttpClient: jasmine.SpyObj<HttpClient>;
  let mockRouter: jasmine.SpyObj<Router>;

  const mockAdminUsers: AdminUser[] = [
    {
      id: '1',
      username: 'admin1',
      email: 'admin1@test.com',
      passwordHash: 'hash1'
    }
  ];

  const mockNormalUsers: UserModel[] = [
    {
      id: '2',
      fullName: 'User One',
      username: 'user1',
      email: 'user1@test.com',
      mobileNumber: '1234567890'
    }
  ];

  beforeEach(async () => {
    mockHttpClient = jasmine.createSpyObj('HttpClient', ['get']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [ViewUsersComponent, ReactiveFormsModule],
      providers: [
        { provide: HttpClient, useValue: mockHttpClient },
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ViewUsersComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize search form', () => {
    component.ngOnInit();
    expect(component.searchForm).toBeDefined();
    expect(component.searchForm.get('query')).toBeDefined();
  });

  it('should load users on init', () => {
    mockHttpClient.get.and.returnValues(
      of(mockAdminUsers),
      of(mockNormalUsers)
    );
    
    component.ngOnInit();
    
    expect(component.adminUsers.length).toBe(1);
    expect(component.normalUsers.length).toBe(1);
  });

  it('should filter users based on search query', () => {
    component.adminUsers = [...mockAdminUsers];
    component.normalUsers = [...mockNormalUsers];
    component.filteredAdminUsers = [...mockAdminUsers];
    component.filteredNormalUsers = [...mockNormalUsers];
    
    component.searchForm.patchValue({ query: 'admin1' });
    
    expect(component.filteredAdminUsers.length).toBe(1);
    expect(component.filteredNormalUsers.length).toBe(0);
  });
});
