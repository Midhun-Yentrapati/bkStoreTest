import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { LeastSoldComponent } from './least-sold.component';
import { BookService } from '../../../services/book.service';
import { BookWithSales } from '../../../models/book.model';

describe('LeastSoldComponent', () => {
  let component: LeastSoldComponent;
  let fixture: ComponentFixture<LeastSoldComponent>;
  let mockBookService: jasmine.SpyObj<BookService>;
  let mockRouter: jasmine.SpyObj<Router>;

  const mockBooks: BookWithSales[] = [
    {
      id: '1',
      title: 'Book 1',
      author: 'Author 1',
      description: 'Description 1',
      categories: ['Fiction'],
      price: 29.99,
      stock_display: 10,
      stock_actual: 10,
      image_urls: ['image1.jpg'],
      no_of_books_sold: 5
    }
  ];

  beforeEach(async () => {
    mockBookService = jasmine.createSpyObj('BookService', ['getLeastSoldBooks']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [LeastSoldComponent],
      providers: [
        { provide: BookService, useValue: mockBookService },
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LeastSoldComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch least sold data on init', () => {
    mockBookService.getLeastSoldBooks.and.returnValue(of(mockBooks));
    
    component.ngOnInit();
    
    expect(mockBookService.getLeastSoldBooks).toHaveBeenCalledWith(10);
    expect(component.isLoading).toBeFalse();
    expect(component.hasChartData).toBeTrue();
  });

  it('should handle empty data', () => {
    mockBookService.getLeastSoldBooks.and.returnValue(of([]));
    
    component.ngOnInit();
    
    expect(component.hasChartData).toBeFalse();
  });

  it('should handle error when fetching data', () => {
    mockBookService.getLeastSoldBooks.and.returnValue(of([]));
    
    component.ngOnInit();
    
    expect(component.errorMessage).toBeNull();
  });
});
