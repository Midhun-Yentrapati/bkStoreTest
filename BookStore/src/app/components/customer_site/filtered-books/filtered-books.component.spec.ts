import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { of, throwError, Subject } from 'rxjs';
import { FilteredBooksComponent } from './filtered-books.component';
import { BookService } from '../../../services/book.service';

const mockBooks = [
  { id: '1', title: 'Book1', author: 'Author1', description: 'desc1', categories: ['cat1'], price: 10, stock_display: 5, stock_actual: 5, image_urls: ['img1.png'] },
  { id: '2', title: 'Book2', author: 'Author2', description: 'desc2', categories: ['cat2'], price: 20, stock_display: 2, stock_actual: 2, image_urls: ['img2.png'] }
];

class MockBookService {
  getAllBooks = jasmine.createSpy().and.returnValue(of(mockBooks));
  getBooksByCategoryFiltered = jasmine.createSpy().and.returnValue(of([mockBooks[0]]));
}
class MockRouter {
  navigate = jasmine.createSpy();
}
class MockActivatedRoute {
  queryParams = of({ category: 'cat1' } as any);
}

describe('FilteredBooksComponent', () => {
  let component: FilteredBooksComponent;
  let fixture: ComponentFixture<FilteredBooksComponent>;
  let bookService: MockBookService;
  let router: MockRouter;
  let route: MockActivatedRoute;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FilteredBooksComponent, ReactiveFormsModule, HttpClientTestingModule],
      providers: [
        { provide: BookService, useClass: MockBookService },
        { provide: Router, useClass: MockRouter },
        { provide: ActivatedRoute, useClass: MockActivatedRoute },
        FormBuilder
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(FilteredBooksComponent);
    component = fixture.componentInstance;
    bookService = TestBed.inject(BookService) as any;
    router = TestBed.inject(Router) as any;
    route = TestBed.inject(ActivatedRoute) as any;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch books by category if filter present', () => {
    component.ngOnInit(); // Trigger initialization manually
    expect(bookService.getBooksByCategoryFiltered).toHaveBeenCalledWith('cat1');
    expect(component.books.length).toBe(1);
  });

  it('should fetch all books if no category filter', fakeAsync(() => {
    route.queryParams = of({});
    bookService.getBooksByCategoryFiltered.calls.reset();
    bookService.getAllBooks.calls.reset();
    component.ngOnInit();
    tick();
    expect(bookService.getAllBooks).toHaveBeenCalled();
  }));

  it('should handle error from getAllBooks', fakeAsync(() => {
    route.queryParams = of({});
    bookService.getBooksByCategoryFiltered.and.returnValue(of([]));
    bookService.getAllBooks.and.returnValue(throwError(() => new Error('Failed')));
    spyOn(console, 'error');
    component.ngOnInit();
    tick();
    expect(console.error).toHaveBeenCalled();
  }));

  it('should filter books based on search query', fakeAsync(() => {
    component.books = [...mockBooks];
    component.filteredBooks = [...mockBooks];
    component.searchForm.get('query')?.setValue('Book1');
    tick();
    expect(component.filteredBooks.length).toBe(1);
    expect(component.filteredBooks[0].title).toBe('Book1');
  }));

  it('should handle search query with no matches', fakeAsync(() => {
    component.books = [...mockBooks];
    component.filteredBooks = [...mockBooks];
    component.searchForm.get('query')?.setValue('nonexistent');
    tick();
    expect(component.filteredBooks.length).toBe(0);
  }));

  it('should handle empty books list', () => {
    component.books = [];
    component.filteredBooks = [];
    component.searchForm.get('query')?.setValue('any');
    expect(component.filteredBooks.length).toBe(0);
  });

  it('should navigate to edit page', () => {
    component.goToEditPage('1');
    expect(router.navigate).toHaveBeenCalledWith(['/edit-page', '1']);
  });

  it('should navigate to add page', () => {
    component.showAddPage();
    expect(router.navigate).toHaveBeenCalledWith(['/add-page']);
  });

  it('should navigate back to admin-main', () => {
    component.goBack();
    expect(router.navigate).toHaveBeenCalledWith(['/admin-main']);
  });

  it('should handle error from bookService', fakeAsync(() => {
    bookService.getBooksByCategoryFiltered.and.returnValue(throwError(() => new Error('Failed')));
    spyOn(console, 'error');
    component.ngOnInit();
    tick();
    expect(console.error).toHaveBeenCalled();
  }));
});
