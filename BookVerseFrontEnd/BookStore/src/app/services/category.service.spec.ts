import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CategoryService } from './category.service';
import { CategoryModel } from '../models/category.model';

describe('CategoryService', () => {
  let service: CategoryService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CategoryService]
    });
    service = TestBed.inject(CategoryService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get active categories', () => {
    const mockCategories: CategoryModel[] = [
      { id: 1, name: 'Fiction' },
      { id: 2, name: 'Non-Fiction' },
      { id: 3, name: 'Science Fiction' }
    ];

    service.getActiveCategories().subscribe(categories => {
      expect(categories).toEqual(mockCategories);
    });

    const req = httpMock.expectOne('http://localhost:3000/categories');
    expect(req.request.method).toBe('GET');
    req.flush(mockCategories);
  });

  it('should get all available categories', () => {
    const mockCategories: CategoryModel[] = [
      { id: 1, name: 'Fiction' },
      { id: 2, name: 'Non-Fiction' },
      { id: 3, name: 'Science Fiction' },
      { id: 4, name: 'Mystery' },
      { id: 5, name: 'Romance' }
    ];

    service.getAllAvailableCategories().subscribe(categories => {
      expect(categories).toEqual(mockCategories);
    });

    const req = httpMock.expectOne('http://localhost:3000/All_categories');
    expect(req.request.method).toBe('GET');
    req.flush(mockCategories);
  });

  it('should get all categories (legacy method)', () => {
    const mockCategories: CategoryModel[] = [
      { id: 1, name: 'Fiction' },
      { id: 2, name: 'Non-Fiction' }
    ];

    service.getAllCategories().subscribe(categories => {
      expect(categories).toEqual(mockCategories);
    });

    const req = httpMock.expectOne('http://localhost:3000/categories');
    expect(req.request.method).toBe('GET');
    req.flush(mockCategories);
  });

  it('should update active categories', () => {
    const updatedCategories: CategoryModel[] = [
      { id: 1, name: 'Fiction' },
      { id: 2, name: 'Non-Fiction' }
    ];

    service.updateActiveCategories(updatedCategories).subscribe(categories => {
      expect(categories).toEqual(updatedCategories);
    });

    const req = httpMock.expectOne('http://localhost:3000/categories');
    expect(req.request.method).toBe('PUT');
    req.flush(updatedCategories);
  });

  it('should add category to active list', () => {
    const allCategories: CategoryModel[] = [
      { id: 1, name: 'Fiction' },
      { id: 2, name: 'Non-Fiction' },
      { id: 3, name: 'Science Fiction' }
    ];
    const activeCategories: CategoryModel[] = [
      { id: 1, name: 'Fiction' }
    ];
    const updatedActiveCategories: CategoryModel[] = [
      { id: 1, name: 'Fiction' },
      { id: 3, name: 'Science Fiction' }
    ];

    service.addCategoryToActive(3).subscribe(categories => {
      expect(categories).toEqual(updatedActiveCategories);
    });

    // First request to get all available categories
    const req1 = httpMock.expectOne('http://localhost:3000/All_categories');
    req1.flush(allCategories);

    // Second request to get active categories
    const req2 = httpMock.expectOne('http://localhost:3000/categories');
    req2.flush(activeCategories);

    // Third request to update active categories
    const req3 = httpMock.expectOne('http://localhost:3000/categories');
    expect(req3.request.method).toBe('PUT');
    req3.flush(updatedActiveCategories);
  });

  it('should remove category from active list', () => {
    const activeCategories: CategoryModel[] = [
      { id: 1, name: 'Fiction' },
      { id: 2, name: 'Non-Fiction' }
    ];
    const updatedActiveCategories: CategoryModel[] = [
      { id: 1, name: 'Fiction' }
    ];

    service.removeCategoryFromActive(2).subscribe(categories => {
      expect(categories).toEqual(updatedActiveCategories);
    });

    // First request to get active categories
    const req1 = httpMock.expectOne('http://localhost:3000/categories');
    req1.flush(activeCategories);

    // Second request to update active categories
    const req2 = httpMock.expectOne('http://localhost:3000/categories');
    expect(req2.request.method).toBe('PUT');
    req2.flush(updatedActiveCategories);
  });

  it('should get inactive categories', () => {
    const allCategories: CategoryModel[] = [
      { id: 1, name: 'Fiction' },
      { id: 2, name: 'Non-Fiction' },
      { id: 3, name: 'Science Fiction' }
    ];
    const activeCategories: CategoryModel[] = [
      { id: 1, name: 'Fiction' }
    ];
    const inactiveCategories: CategoryModel[] = [
      { id: 2, name: 'Non-Fiction' },
      { id: 3, name: 'Science Fiction' }
    ];

    service.getInactiveCategories().subscribe(categories => {
      expect(categories).toEqual(inactiveCategories);
    });

    // First request to get all available categories
    const req1 = httpMock.expectOne('http://localhost:3000/All_categories');
    req1.flush(allCategories);

    // Second request to get active categories
    const req2 = httpMock.expectOne('http://localhost:3000/categories');
    req2.flush(activeCategories);
  });
});
