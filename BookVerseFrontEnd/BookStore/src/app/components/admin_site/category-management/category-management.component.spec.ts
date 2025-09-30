import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { CategoryManagementComponent } from './category-management.component';
import { CategoryService } from '../../../services/category.service';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { CategoryModel } from '../../../models/category.model';

describe('CategoryManagementComponent', () => {
  let component: CategoryManagementComponent;
  let fixture: ComponentFixture<CategoryManagementComponent>;
  let mockCategoryService: jasmine.SpyObj<CategoryService>;
  let mockRouter: jasmine.SpyObj<Router>;

  const mockCategories: CategoryModel[] = [
    { id: 1, name: 'Fantasy' },
    { id: 2, name: 'Science Fiction' },
    { id: 3, name: 'Mystery' }
  ];

  beforeEach(async () => {
    mockCategoryService = jasmine.createSpyObj('CategoryService', ['getAllCategories', 'updateActiveCategories', 'addCategoryToActive', 'removeCategoryFromActive']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    
    // Setup default return values
    mockCategoryService.getAllCategories.and.returnValue(of(mockCategories));
    mockCategoryService.updateActiveCategories.and.returnValue(of(mockCategories));
    mockCategoryService.addCategoryToActive.and.returnValue(of(mockCategories));
    mockCategoryService.removeCategoryFromActive.and.returnValue(of(mockCategories));

    await TestBed.configureTestingModule({
      imports: [CategoryManagementComponent, HttpClientTestingModule, ReactiveFormsModule],
      providers: [
        { provide: CategoryService, useValue: mockCategoryService },
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();
    
    fixture = TestBed.createComponent(CategoryManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch categories on ngOnInit', () => {
    const spy = spyOn(component, 'fetchCategories');
    component.ngOnInit();
    expect(spy).toHaveBeenCalled();
  });

  it('should fetch categories successfully', () => {
    mockCategoryService.getAllCategories.and.returnValue(of(mockCategories));
    
    component.fetchCategories();
    
    expect(component.categories).toEqual(mockCategories);
    expect(component.isLoading).toBeFalse();
    expect(component.errorMessage).toBeNull();
  });

  it('should handle error in fetchCategories', () => {
    mockCategoryService.getAllCategories.and.returnValue(throwError(() => new Error('fail')));
    spyOn(console, 'error');
    
    component.fetchCategories();
    
    expect(component.errorMessage).toBe('Failed to load categories.');
    expect(component.isLoading).toBeFalse();
    expect(console.error).toHaveBeenCalledWith('Error fetching categories:', jasmine.any(Error));
  });

  it('should generate next category ID with existing categories', () => {
    component.categories = [{ id: '11', name: 'A' }, { id: '12', name: 'B' }];
    const nextId = (component as any)._generateNextCategoryId();
    expect(nextId).toBe('13');
  });

  it('should generate next category ID with no existing categories', () => {
    component.categories = [];
    const nextId = (component as any)._generateNextCategoryId();
    expect(nextId).toBe('11');
  });

  it('should not add category if name is empty', () => {
    component.newCategoryName = '   ';
    
    component.onAddCategory();
    
    expect(component.errorMessage).toBe('Category name cannot be empty.');
    expect(component.categoryInputError).toBeTrue();
  });

  it('should not add category if name is too short', () => {
    component.newCategoryName = 'A';
    
    component.onAddCategory();
    
    expect(component.errorMessage).toBe('Category name must be at least 2 characters long.');
    expect(component.categoryInputError).toBeTrue();
  });

  it('should not add category if name already exists', () => {
    component.categories = [{ id: '1', name: 'Fantasy' }];
    component.newCategoryName = 'fantasy';
    
    component.onAddCategory();
    
    expect(component.errorMessage).toBe('Category with this name already exists.');
    expect(component.categoryInputError).toBeTrue();
  });

  it('should add category successfully', () => {
    component.categories = [];
    component.newCategoryName = 'TestCat';
    
    component.onAddCategory();
    
    expect(component.categories.length).toBe(1);
    expect(component.categories[0].name).toBe('TestCat');
    expect(component.successMessage).toContain('Category added successfully!');
    expect(component.isLoading).toBeFalse();
    expect(component.newCategoryName).toBe('');
  });

  it('should delete category on confirm', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    component.categories = [{ id: '11', name: 'TestCat' }];
    
    component.onDelete('11');
    
    expect(component.categories.length).toBe(0);
    expect(component.successMessage).toContain('Category deleted successfully!');
    expect(component.isLoading).toBeFalse();
  });

  it('should not delete category if confirm is cancelled', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.categories = [{ id: '11', name: 'TestCat' }];
    
    component.onDelete('11');
    
    expect(component.categories.length).toBe(1);
    expect(component.successMessage).toBeNull();
  });

  it('should clear success messages after delay', (done) => {
    component.successMessage = 'Success!';
    component.isLoading = true;
    
    component.clearMessagesAfterDelay(10);
    
    setTimeout(() => {
      expect(component.successMessage).toBeNull();
      expect(component.isLoading).toBeFalse();
      done();
    }, 20);
  });

  it('should clear error messages after delay', (done) => {
    component.errorMessage = 'Error!';
    component.isLoading = true;
    
    component.clearMessagesAfterDelay(10, true);
    
    setTimeout(() => {
      expect(component.errorMessage).toBeNull();
      expect(component.isLoading).toBeFalse();
      done();
    }, 20);
  });

  it('should navigate back when goBack is called', () => {
    component.goBack();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/admin-main']);
  });

  it('should clear messages and set loading state when adding category', () => {
    component.categories = [];
    component.newCategoryName = 'TestCat';
    component.errorMessage = 'Previous error';
    component.successMessage = 'Previous success';
    
    component.onAddCategory();
    
    expect(component.errorMessage).toBeNull();
    expect(component.successMessage).toContain('Category added successfully!');
    expect(component.categoryInputError).toBeFalse();
  });

  it('should clear messages and set loading state when deleting category', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    component.categories = [{ id: '11', name: 'TestCat' }];
    component.errorMessage = 'Previous error';
    component.successMessage = 'Previous success';
    
    component.onDelete('11');
    
    expect(component.errorMessage).toBeNull();
    expect(component.successMessage).toContain('Category deleted successfully!');
  });

  it('should handle category name case-insensitive comparison', () => {
    component.categories = [{ id: '1', name: 'Fantasy' }];
    component.newCategoryName = 'FANTASY';
    
    component.onAddCategory();
    
    expect(component.errorMessage).toBe('Category with this name already exists.');
  });

  it('should generate unique IDs for multiple categories', () => {
    component.categories = [];
    component.newCategoryName = 'First';
    
    component.onAddCategory();
    
    expect(component.categories[0].id).toBe('11');
    
    component.newCategoryName = 'Second';
    component.onAddCategory();
    
    expect(component.categories[1].id).toBe('12');
  });
});
