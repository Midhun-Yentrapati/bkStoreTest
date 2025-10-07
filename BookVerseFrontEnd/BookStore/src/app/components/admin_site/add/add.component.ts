import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { BookService } from '../../../services/book.service';
import { CategoryService } from '../../../services/category.service';
import { BookCreateRequest, BookImageRequest } from '../../../models/book.model';
import { CategoryModel } from '../../../models/category.model';
import { Subscription } from 'rxjs';
import { take } from 'rxjs/operators';

@Component({
  selector: 'app-add',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, FormsModule],
  templateUrl: './add.component.html',
  styleUrls: ['./add.component.css']
})
export class AddComponent implements OnInit, OnDestroy {
  // A single subscription manager to prevent memory leaks by cleaning up all subscriptions on destroy.
  private subscriptions = new Subscription();

  // --- Form Groups ---
  bookForm!: FormGroup;
  categoryForm!: FormGroup;
  imageForm!: FormGroup;

  // --- Step Management ---
  currentStep: number = 1;
  totalSteps: number = 4;

  // --- Data Arrays ---
  availableCategories: CategoryModel[] = [];
  selectedCategories: CategoryModel[] = [];
  selectedImages: BookImageRequest[] = [];
  filteredCategories: CategoryModel[] = [];

  // --- UI State & Messages ---
  categorySearchQuery: string = '';
  showCategoryForm: boolean = false;
  successMessage: string = '';
  errorMessage: string = '';
  isSubmitting: boolean = false;
  
  // Property for the live image preview URL
  imagePreviewUrl: string | null = null;
  
  // FIX: Re-added imageInputType to match the HTML template and resolve compilation errors.
  imageInputType: 'url' | 'filepath' = 'url';

  constructor(
    private fb: FormBuilder,
    private bookService: BookService,
    private categoryService: CategoryService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initializeForms();
    this.loadCategories();
  }

  ngOnDestroy(): void {
    // This is crucial. It unsubscribes from all active subscriptions when the component is destroyed.
    this.subscriptions.unsubscribe();
  }

  // ===================================================================
  // FORM INITIALIZATION
  // ===================================================================

  private initializeForms(): void {
    this.bookForm = this.fb.group({
      isbn: ['', [this.isbnValidator]],
      title: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(200)]],
      author: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      description: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(2000)]],
      language: ['English', [Validators.maxLength(50)]],
      format: ['Paperback'],
      edition: ['', [Validators.maxLength(50)]],
      publisher: ['', [Validators.maxLength(100)]],
      publicationDate: [''],
      pages: [0, [Validators.min(0), Validators.max(10000)]],
      weight: [0, [Validators.min(0), Validators.max(50)]],
      dimensions: ['', [Validators.maxLength(100)]],
      price: [0, [Validators.required, Validators.min(0.01), Validators.max(999999)]],
      mrp: [0, [Validators.min(0), Validators.max(999999)]],
      stockDisplay: [0, [Validators.required, Validators.min(0), Validators.max(999999)]],
      stockActual: [0, [Validators.required, Validators.min(0), Validators.max(999999)]],
      salesCategory: ['', Validators.required],
      isActive: [true],
      isFeatured: [false]
    });

    this.categoryForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      description: ['', [Validators.maxLength(500)]],
      slug: ['', [Validators.maxLength(100), this.slugValidator]],
      image: [''],
      displayOrder: [0, [Validators.min(0)]],
      isActive: [true]
    });

    // REWRITTEN: Re-added 'filePath' to align with the template, but the primary logic will focus on 'imageUrl'.
    this.imageForm = this.fb.group({
      imageUrl: ['', [Validators.pattern(/^(https?:\/\/)?([\da-z\.-]+)\.([a-z\.]{2,6})([\/\w \.-]*)*\/?$/)]],
      filePath: [''], // Added back to prevent template errors
      altText: ['', [Validators.maxLength(200)]],
      isPrimary: [false],
      displayOrder: [0, [Validators.min(0), Validators.max(999)]]
    });
  }
  


  // ===================================================================
  // CUSTOM VALIDATORS (Your well-written validators are preserved)
  // ===================================================================

  private isbnValidator(control: any) {
    if (!control.value) return null;
    const isbn = control.value.replace(/[-\s]/g, '');
    if (isbn.length === 10) return AddComponent.validateISBN10(isbn) ? null : { invalidIsbn: true };
    if (isbn.length === 13) return AddComponent.validateISBN13(isbn) ? null : { invalidIsbn: true };
    return { invalidIsbn: true };
  }
  private static validateISBN10(isbn: string): boolean { 
    if (!/^\d{9}[\dX]$/.test(isbn)) return false;
    let sum = 0;
    for (let i = 0; i < 9; i++) { sum += parseInt(isbn[i]) * (10 - i); }
    const checkDigit = isbn[9] === 'X' ? 10 : parseInt(isbn[9]);
    return (sum + checkDigit) % 11 === 0;
  }
  private static validateISBN13(isbn: string): boolean { 
    if (!/^\d{13}$/.test(isbn)) return false;
    let sum = 0;
    for (let i = 0; i < 12; i++) { sum += parseInt(isbn[i]) * (i % 2 === 0 ? 1 : 3); }
    const checkDigit = (10 - (sum % 10)) % 10;
    return checkDigit === parseInt(isbn[12]);
  }
  private slugValidator(control: any) {
    if (!control.value) return null;
    const slugPattern = /^[a-z0-9]+(?:-[a-z0-9]+)*$/;
    return slugPattern.test(control.value) ? null : { invalidSlug: true };
  }

  // ===================================================================
  // DATA FETCHING
  // ===================================================================

  private loadCategories(): void {
    this.categoryService.getAllCategories().pipe(take(1)).subscribe({
      next: (categories) => {
        this.availableCategories = categories;
        this.filteredCategories = categories;
      },
      error: (err) => {
        console.error('Failed to load categories', err);
        this.errorMessage = 'Failed to load categories';
      }
    });
  }

  // ===================================================================
  // STEP NAVIGATION & VALIDATION
  // ===================================================================

  nextStep(): void {
    if (this.currentStep < this.totalSteps && this.isStepValid(this.currentStep)) {
      this.currentStep++;
    }
  }

  previousStep(): void {
    if (this.currentStep > 1) {
      this.currentStep--;
    }
  }

  isStepValid(step: number): boolean {
    switch (step) {
      case 1:
        return !!(this.bookForm.get('title')?.valid && this.bookForm.get('author')?.valid && this.bookForm.get('description')?.valid);
      case 2:
        const price = this.bookForm.get('price');
        const mrp = this.bookForm.get('mrp');
        const stockDisplay = this.bookForm.get('stockDisplay');
        const stockActual = this.bookForm.get('stockActual');
        const salesCategory = this.bookForm.get('salesCategory');
        const mrpValid = (mrp?.value || 0) === 0 || (mrp?.value || 0) >= (price?.value || 0);
        return !!(price?.valid && stockDisplay?.valid && stockActual?.valid && salesCategory?.valid && mrpValid);
      case 3:
        return this.selectedCategories.length > 0;
      case 4:
        return true; // Images are optional
      default:
        return false;
    }
  }

  // ===================================================================
  // CATEGORY MANAGEMENT
  // ===================================================================

  filterCategories(): void {
    this.filteredCategories = !this.categorySearchQuery.trim()
      ? this.availableCategories
      : this.availableCategories.filter(category =>
          category.name.toLowerCase().includes(this.categorySearchQuery.toLowerCase())
        );
  }

  selectCategory(category: CategoryModel): void {
    if (!this.isCategorySelected(category)) {
      this.selectedCategories.push(category);
    }
  }

  removeCategory(category: CategoryModel): void {
    this.selectedCategories = this.selectedCategories.filter(c => c.id !== category.id);
  }

  isCategorySelected(category: CategoryModel): boolean {
    return this.selectedCategories.some(c => c.id === category.id);
  }

  createCategory(): void {
    if (this.categoryForm.invalid) {
      this.markFormGroupTouched(this.categoryForm);
      return;
    }
    const formValue = this.categoryForm.value;
    const categoryData = {
      ...formValue,
      slug: formValue.slug || this.generateSlug(formValue.name)
    };
    this.categoryService.createCategory(categoryData).pipe(take(1)).subscribe({
      next: (newCategory) => {
        this.availableCategories.push(newCategory);
        this.filterCategories();
        this.selectCategory(newCategory);
        this.categoryForm.reset({ isActive: true, displayOrder: 0 });
        this.showCategoryForm = false;
      },
      error: (err) => {
        console.error('Failed to create category', err);
        this.errorMessage = 'Failed to create category';
      }
    });
  }

  private generateSlug(name: string): string {
    return name.toLowerCase().replace(/[^a-z0-9 -]/g, '').replace(/\s+/g, '-').replace(/-+/g, '-').replace(/^-+|-+$/g, '');
  }
  
  // FIX: Re-added method to handle radio button changes in the template.
  onImageInputTypeChange(): void {
    this.imageForm.reset({
      imageUrl: '',
      filePath: '',
      displayOrder: this.selectedImages.length,
      isPrimary: false
    });
    this.imagePreviewUrl = null;
  }

  // ===================================================================
  // IMAGE MANAGEMENT
  // ===================================================================

  addImage(): void {
    const isUrlType = this.imageInputType === 'url';
    const urlControl = this.imageForm.get('imageUrl');
    const filePathControl = this.imageForm.get('filePath');

    if (isUrlType) {
        urlControl?.setValidators([Validators.required, Validators.pattern(/^(https?:\/\/)?([\da-z\.-]+)\.([a-z\.]{2,6})([\/\w \.-]*)*\/?$/)]);
        filePathControl?.clearValidators();
    } else {
        filePathControl?.setValidators([Validators.required]);
        urlControl?.clearValidators();
    }
    urlControl?.updateValueAndValidity();
    filePathControl?.updateValueAndValidity();

    if (this.imageForm.invalid) {
        this.errorMessage = isUrlType ? 'Please enter a valid image URL.' : 'File path is required.';
        this.markFormGroupTouched(this.imageForm);
        return;
    }
    
    this.errorMessage = '';
    const formValue = this.imageForm.value;
    const newImage: BookImageRequest = {
      imageUrl: isUrlType ? formValue.imageUrl : formValue.filePath,
      altText: formValue.altText,
      isPrimary: formValue.isPrimary,
      displayOrder: formValue.displayOrder
    };

    if (newImage.isPrimary) {
      this.selectedImages.forEach(img => img.isPrimary = false);
    }
    
    this.selectedImages.push(newImage);
    this.selectedImages.sort((a, b) => a.displayOrder - b.displayOrder);

    this.imageForm.reset({
      displayOrder: this.selectedImages.length,
      isPrimary: this.selectedImages.length === 0,
      imageUrl: '',
      filePath: ''
    });
    this.imagePreviewUrl = null;
  }

  removeImage(index: number): void {
    this.selectedImages.splice(index, 1);
  }

  setPrimaryImage(index: number): void {
    this.selectedImages.forEach((img, i) => img.isPrimary = i === index);
  }

  // ===================================================================
  // FORM SUBMISSION
  // ===================================================================

  submitBook(): void {
    this.markFormGroupTouched(this.bookForm);

    if (!this.isStepValid(1) || !this.isStepValid(2) || !this.isStepValid(3)) {
      this.errorMessage = 'Please fix all validation errors in previous steps before submitting.';
      return;
    }

    if (this.selectedImages.length > 0 && !this.selectedImages.some(img => img.isPrimary)) {
        this.errorMessage = 'If you add images, please select one as the primary image.';
        return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    const formValue = this.bookForm.value;

    const bookData: BookCreateRequest = {
      ...formValue,
      publicationDate: formValue.publicationDate ? new Date(formValue.publicationDate).toISOString() : undefined,
      categoryIds: this.selectedCategories.map(cat => cat.id),
      images: this.selectedImages
    };

    this.bookService.createBookWithRelations(bookData).pipe(take(1)).subscribe({
      next: () => {
        this.successMessage = 'Book created successfully!';
        this.resetForms();
      },
      error: (err: any) => {
        console.error('Failed to create book', err);
        this.errorMessage = err.error?.message || 'An unknown error occurred while creating the book.';
      },
      complete: () => {
        this.isSubmitting = false;
      }
    });
  }

  private resetForms(): void {
    this.bookForm.reset({ isActive: true, isFeatured: false, language: 'English', format: 'Paperback' });
    this.categoryForm.reset({ isActive: true, displayOrder: 0 });
    this.imageForm.reset({ displayOrder: 0, isPrimary: false, imageUrl: '', filePath: '' });
    this.selectedCategories = [];
    this.selectedImages = [];
    this.currentStep = 1;
    this.imagePreviewUrl = null;
  }

  // ===================================================================
  // HELPERS
  // ===================================================================
  
  goBack(): void {
    this.router.navigate(['/admin/dashboard']);
  }
  
  onImageError(event: Event): void {
    (event.target as HTMLImageElement).src = 'assets/placeholder-book.png';
  }

  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.values(formGroup.controls).forEach(control => {
      control.markAsTouched();
      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      }
    });
  }

  getStepTitle(step: number): string {
    const titles = ['Basic Information', 'Additional Details', 'Categories', 'Images'];
    return titles[step - 1] || '';
  }

  getFieldError(fieldName: string, formGroup: FormGroup = this.bookForm): string {
    const field = formGroup.get(fieldName);
    if (!field || !field.errors || !field.touched) return '';
    
    const errors = field.errors;
    const label = this.getFieldLabel(fieldName);
    
    if (errors['required']) return `${label} is required.`;
    if (errors['minlength']) return `${label} must be at least ${errors['minlength'].requiredLength} characters.`;
    if (errors['maxlength']) return `${label} cannot exceed ${errors['maxlength'].requiredLength} characters.`;
    if (errors['min']) return `${label} must be at least ${errors['min'].min}.`;
    if (errors['max']) return `${label} cannot exceed ${errors['max'].max}.`;
    if (errors['invalidIsbn']) return 'Please enter a valid ISBN-10 or ISBN-13.';
    if (errors['invalidSlug']) return 'Slug must contain only lowercase letters, numbers, and hyphens.';
    if (errors['pattern']) return 'Please enter a valid URL (e.g., http://example.com/image.png).';
    
    return 'Invalid input.';
  }

  private getFieldLabel(fieldName: string): string {
    const labels: { [key: string]: string } = {
      'title': 'Title', 'author': 'Author', 'description': 'Description', 'isbn': 'ISBN',
      'publisher': 'Publisher', 'language': 'Language', 'edition': 'Edition', 'price': 'Price',
      'mrp': 'MRP', 'stockDisplay': 'Display Stock', 'stockActual': 'Actual Stock',
      'salesCategory': 'Sales Category', 'pages': 'Pages', 'weight': 'Weight', 'dimensions': 'Dimensions',
      'imageUrl': 'Image URL', 'altText': 'Alt Text', 'displayOrder': 'Display Order',
      'name': 'Category Name', 'slug': 'Slug'
    };
    return labels[fieldName] || fieldName;
  }
}

