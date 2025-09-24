import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { EditPageComponent } from './edit-page.component';
import { BookService } from '../../../services/book.service';
import { CategoryColorService } from '../../../services/category-color.service';
import { BookModel } from '../../../models/book.model';

describe('EditPageComponent', () => {
  let component: EditPageComponent;
  let fixture: ComponentFixture<EditPageComponent>;
  let mockBookService: jasmine.SpyObj<BookService>;
  let mockCategoryColorService: jasmine.SpyObj<CategoryColorService>;
  let mockActivatedRoute: any;

  const mockBook: BookModel = {
    id: '1',
    title: 'Test Book',
    author: 'Test Author',
    description: 'Test Description',
    categories: ['Fiction'],
    price: 29.99,
    stock_display: 10,
    stock_actual: 10,
    image_urls: ['test-image.jpg']
  };

  beforeEach(async () => {
    mockBookService = jasmine.createSpyObj('BookService', ['getBookById', 'updateBook']);
    mockCategoryColorService = jasmine.createSpyObj('CategoryColorService', ['getCategoryColor']);
    mockActivatedRoute = {
      snapshot: {
        paramMap: {
          get: jasmine.createSpy('get').and.returnValue('1')
        }
      }
    };

    await TestBed.configureTestingModule({
      imports: [EditPageComponent],
      providers: [
        { provide: BookService, useValue: mockBookService },
        { provide: CategoryColorService, useValue: mockCategoryColorService },
        { provide: ActivatedRoute, useValue: mockActivatedRoute }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(EditPageComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load book data on init', () => {
    mockBookService.getBookById.and.returnValue(of(mockBook));
    
    component.ngOnInit();
    
    expect(mockBookService.getBookById).toHaveBeenCalledWith('1');
    expect(component.editableBook).toEqual(mockBook);
    expect(component.isLoading).toBeFalse();
  });

  it('should handle error when loading book data', () => {
    mockBookService.getBookById.and.returnValue(throwError(() => new Error('Failed to load')));
    
    component.ngOnInit();
    
    expect(component.errorMessage).toBe('Failed to load data. Please try again later.');
  });

  it('should check if changes can be saved', () => {
    component.editableBook = mockBook;
    component.selectedBookDataCategory = 'Fiction';
    
    const canSave = component.canSaveChanges();
    
    expect(canSave).toBeTrue();
  });

  it('should not allow saving with invalid data', () => {
    component.editableBook = { ...mockBook, title: '' };
    component.selectedBookDataCategory = 'Fiction';
    
    const canSave = component.canSaveChanges();
    
    expect(canSave).toBeFalse();
  });
});
