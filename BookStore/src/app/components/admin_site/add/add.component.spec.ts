import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AddComponent } from './add.component';
import { BookService } from '../../../services/book.service';
import { CategoryService } from '../../../services/category.service';
import { BookModel } from '../../../models/book.model';
import { CategoryModel } from '../../../models/category.model';

describe('AddComponent', () => {
  let component: AddComponent;
  let fixture: ComponentFixture<AddComponent>;
  let mockBookService: jasmine.SpyObj<BookService>;
  let mockCategoryService: jasmine.SpyObj<CategoryService>;
  let mockRouter: jasmine.SpyObj<Router>;

  const mockBook: BookModel = {
    id: '1',
    title: 'Test Book',
    author: 'Test Author',
    description: 'Test Description',
    categories: [{
      id: 1,
      name: 'Fiction',
      slug: 'fiction',
      isActive: true
    }],
    price: 29.99,
    stock_display: 10,
    stock_actual: 10,
    stockDisplay: 10,
    stockActual: 10,
    image_urls: ['https://example.com/image.jpg']
  };

  beforeEach(async () => {
    mockBookService = jasmine.createSpyObj('BookService', ['createBookWithRelations']);
    mockCategoryService = jasmine.createSpyObj('CategoryService', ['getAllCategories', 'createCategory']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    // Set up default return values
    mockCategoryService.getAllCategories.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [AddComponent, ReactiveFormsModule],
      providers: [
        { provide: BookService, useValue: mockBookService },
        { provide: CategoryService, useValue: mockCategoryService },
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AddComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form on ngOnInit', () => {
    component.ngOnInit();
    expect(component.bookForm).toBeDefined();
    expect(component.bookForm.get('title')).toBeDefined();
  });

  it('should not add book if form is invalid', () => {
    component.bookForm.patchValue({ title: '' });
    component.submitBook();
    expect(mockBookService.createBookWithRelations).not.toHaveBeenCalled();
    expect(component.errorMessage).toBe('Please fix all validation errors before submitting.');
  });

  it('should add book if form is valid', () => {
    mockBookService.createBookWithRelations.and.returnValue(of(mockBook));
    
    component.bookForm.patchValue({
      title: 'Test Book',
      author: 'Test Author',
      description: 'Test Description',
      price: 29.99,
      stockDisplay: 10,
      stockActual: 10,
      salesCategory: 'BEST_SELLING',
      isActive: true,
      isFeatured: false
    });
    
    // Add required category
    component.selectedCategories = [{ id: 1, name: 'Fiction', slug: 'fiction' }];
    
    component.submitBook();
    
    expect(mockBookService.createBookWithRelations).toHaveBeenCalled();
    expect(component.successMessage).toBe('Book created successfully!');
  });

  it('should handle error from createBook', () => {
    mockBookService.createBookWithRelations.and.returnValue(throwError(() => ({ error: { message: 'Error adding book' } })));
    
    component.bookForm.patchValue({
      title: 'Test Book',
      author: 'Test Author',
      description: 'Test Description',
      price: 29.99,
      stockDisplay: 10,
      stockActual: 10,
      salesCategory: 'BEST_SELLING',
      isActive: true,
      isFeatured: false
    });
    
    // Add required category
    component.selectedCategories = [{ id: 1, name: 'Fiction', slug: 'fiction' }];
    
    component.submitBook();
    
    expect(component.errorMessage).toBe('Failed to create book: Error adding book');
  });

  it('should reset form after adding book', () => {
    mockBookService.createBookWithRelations.and.returnValue(of(mockBook));
    
    // Set up valid form data
    component.bookForm.patchValue({
      title: 'Test Book',
      author: 'Test Author',
      description: 'Test Description',
      price: 29.99,
      stockDisplay: 10,
      stockActual: 10,
      salesCategory: 'BEST_SELLING',
      isActive: true,
      isFeatured: false
    });
    
    // Add at least one category (required)
    component.selectedCategories = [{ id: 1, name: 'Fiction', slug: 'fiction' }];
    
    component.submitBook();
    
    expect(component.bookForm.get('title')?.value).toBe('');
    expect(component.bookForm.get('price')?.value).toBe(0);
  });

  it('should navigate back when goBack is called', () => {
    component.goBack();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/admin/dashboard']);
  });
});
