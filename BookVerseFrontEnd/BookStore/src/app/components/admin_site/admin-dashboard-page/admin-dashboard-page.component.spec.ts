import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AdminDashboardPageComponent } from './admin-dashboard-page.component';
import { BookService } from '../../../services/book.service';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';

class MockBookService {
  getAllBooks = jasmine.createSpy().and.returnValue(of([]));
}

class MockRouter {
  navigate = jasmine.createSpy();
}

describe('AdminDashboardPageComponent', () => {
  let component: AdminDashboardPageComponent;
  let fixture: ComponentFixture<AdminDashboardPageComponent>;
  let bookService: MockBookService;
  let router: MockRouter;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminDashboardPageComponent],
      providers: [
        { provide: BookService, useClass: MockBookService },
        { provide: Router, useClass: MockRouter }
      ]
    }).compileComponents();
    
    fixture = TestBed.createComponent(AdminDashboardPageComponent);
    component = fixture.componentInstance;
    bookService = TestBed.inject(BookService) as any;
    router = TestBed.inject(Router) as any;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize and call updateBookListCards and checkLowInventory', () => {
    spyOn<any>(component, 'updateBookListCards');
    spyOn<any>(component, 'checkLowInventory');
    component.ngOnInit();
    expect(component['updateBookListCards']).toHaveBeenCalled();
    expect(component['checkLowInventory']).toHaveBeenCalled();
  });

  it('should call updateBookListCards on ngOnChanges for relevant changes', () => {
    spyOn<any>(component, 'updateBookListCards');
    component.ngOnChanges({ 
      highlyRatedBooks: { currentValue: [], previousValue: [], firstChange: false, isFirstChange: () => false } 
    });
    expect(component['updateBookListCards']).toHaveBeenCalled();
  });

  it('should update bookListCards in updateBookListCards', () => {
    (component as any).updateBookListCards();
    expect(component.bookListCards.length).toBe(5);
    expect(component.bookListCards[0].title).toBe('View Inventory');
    expect(component.bookListCards[0].type).toBe('inventory');
  });

  it('should call getAllBooks and handle empty inventory in checkLowInventory', () => {
    component['checkLowInventory']();
    expect(bookService.getAllBooks).toHaveBeenCalled();
  });

  it('should handle error in checkLowInventory', () => {
    bookService.getAllBooks = jasmine.createSpy().and.returnValue(throwError(() => new Error('Fail')));
    spyOn(console, 'error');
    expect(() => component['checkLowInventory']()).not.toThrow();
  });

  it('should navigate on onBookListCardClick for inventory type', () => {
    component.onBookListCardClick('View Inventory', 'inventory');
    expect(router.navigate).toHaveBeenCalledWith(['/admin/inventory']);
  });

  it('should navigate on onBookListCardClick for orders type', () => {
    component.onBookListCardClick('Orders', 'orders');
    expect(router.navigate).toHaveBeenCalledWith(['/admin/orders']);
  });

  it('should navigate on onBookListCardClick for lowStock type', () => {
    component.onBookListCardClick('Low Stock Alert', 'lowStock');
    expect(router.navigate).toHaveBeenCalledWith(['/admin/low-stock']);
  });

  it('should navigate on onBookListCardClick for editGenere type', () => {
    component.onBookListCardClick('Edit Genres', 'editGenere');
    expect(router.navigate).toHaveBeenCalledWith(['/admin/categories']);
  });

  it('should navigate on onBookListCardClick for manageAdminUsers type', () => {
    component.onBookListCardClick('Manage Admin Users', 'manageAdminUsers');
    expect(router.navigate).toHaveBeenCalledWith(['/admin/users']);
  });

  it('should log card click information', () => {
    spyOn(console, 'log');
    component.onBookListCardClick('View Inventory', 'inventory');
    expect(console.log).toHaveBeenCalledWith('Book List Card "View Inventory" (Type: inventory) was clicked.');
  });

  it('should get current time', () => {
    const time = component.getCurrentTime();
    expect(time).toBeDefined();
    expect(typeof time).toBe('string');
  });

  it('should track by card title', () => {
    const card = { title: 'Test Card' };
    const result = component.trackByCardTitle(0, card);
    expect(result).toBe('Test Card');
  });

  it('should navigate to add book', () => {
    component.navigateToAddBook();
    expect(router.navigate).toHaveBeenCalledWith(['/admin/add-book']);
  });

  it('should navigate to users', () => {
    component.navigateToUsers();
    expect(router.navigate).toHaveBeenCalledWith(['/admin/users']);
  });

  it('should navigate to categories', () => {
    component.navigateToCategories();
    expect(router.navigate).toHaveBeenCalledWith(['/admin/categories']);
  });
});
