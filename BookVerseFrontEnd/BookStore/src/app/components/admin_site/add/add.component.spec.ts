import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AddComponent } from './add.component';
import { BookService } from '../../../services/book.service';
import { BookModel } from '../../../models/book.model';

describe('AddComponent', () => {
  let component: AddComponent;
  let fixture: ComponentFixture<AddComponent>;
  let mockBookService: jasmine.SpyObj<BookService>;
  let mockRouter: jasmine.SpyObj<Router>;

  const mockBook: BookModel = {
    id: '1',
    title: 'Test Book',
    author: 'Test Author',
    description: 'Test Description',
    categories: ['Fiction'],
    price: 29.99,
    stock_display: 10,
    stock_actual: 10,
    image_urls: ['https://example.com/image.jpg']
  };

  beforeEach(async () => {
    mockBookService = jasmine.createSpyObj('BookService', ['createBook']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [AddComponent, ReactiveFormsModule],
      providers: [
        { provide: BookService, useValue: mockBookService },
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
    component.addBook();
    expect(mockBookService.createBook).not.toHaveBeenCalled();
    expect(component.addedBooks.length).toBe(0);
  });

  it('should add book if form is valid', () => {
    mockBookService.createBook.and.returnValue(of(mockBook));
    
    component.bookForm.patchValue({
      id: '1',
      title: 'Test Book',
      author: 'Test Author',
      description: 'Test Description',
      categories: 'Fiction',
      sales_category: 'newly launched',
      price: 29.99,
      stock_display: 10,
      stock_actual: 10,
      image_urls: 'https://example.com/image.jpg'
    });
    
    component.addBook();
    
    expect(mockBookService.createBook).toHaveBeenCalled();
    expect(component.addedBooks.length).toBe(1);
  });

  it('should handle error from createBook', () => {
    mockBookService.createBook.and.returnValue(throwError(() => new Error('Failed')));
    
    component.bookForm.patchValue({
      id: '1',
      title: 'Test Book',
      author: 'Test Author',
      description: 'Test Description',
      categories: 'Fiction',
      sales_category: 'newly launched',
      price: 29.99,
      stock_display: 10,
      stock_actual: 10,
      image_urls: 'https://example.com/image.jpg'
    });
    
    component.addBook();
    
    expect(component.errorMessage).toBe('Failed to add book. Please try again.');
  });

  it('should reset form after adding book', () => {
    mockBookService.createBook.and.returnValue(of(mockBook));
    
    component.bookForm.patchValue({
      id: '1',
      title: 'Test Book',
      author: 'Test Author',
      description: 'Test Description',
      categories: 'Fiction',
      sales_category: 'newly launched',
      price: 29.99,
      stock_display: 10,
      stock_actual: 10,
      image_urls: 'https://example.com/image.jpg'
    });
    
    component.addBook();
    
    expect(component.bookForm.get('id')?.value).toBe('');
    expect(component.bookForm.get('price')?.value).toBe(0);
  });

  it('should navigate back when goBack is called', () => {
    component.goBack();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/admin/inventory']);
  });
});
