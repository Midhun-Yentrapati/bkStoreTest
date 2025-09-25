import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { BookService } from '../../../services/book.service';
import { CategoryService } from '../../../services/category.service';
import { BookModel, BookCreateRequest, BookImageRequest } from '../../../models/book.model';
import { CategoryModel } from '../../../models/category.model';

@Component({
  selector: 'app-add',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, FormsModule],
  templateUrl: './add.component.html',
  styleUrls: ['./add.component.css']
})
export class AddComponent implements OnInit {
  // Form groups
  bookForm!: FormGroup;
  categoryForm!: FormGroup;
  imageForm!: FormGroup;
  
  // Step management
  currentStep: number = 1;
  totalSteps: number = 4;
  
  // Data arrays
  availableCategories: CategoryModel[] = [];
  selectedCategories: CategoryModel[] = [];
  selectedImages: BookImageRequest[] = [];
  filteredCategories: CategoryModel[] = [];
  
  // Search and UI state
  categorySearchQuery: string = '';
  showCategoryForm: boolean = false;
  
  // Messages
  successMessage: string = '';
  errorMessage: string = '';
  isSubmitting: boolean = false;

  // Image input type selection
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

  private initializeForms(): void {
    // Main book form with ALL backend fields and enhanced validations
    this.bookForm = this.fb.group({
      isbn: ['', [this.isbnValidator]], // Custom ISBN validator
      title: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(200)]],
      author: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      description: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(2000)]],
      language: ['', [Validators.maxLength(50)]],
      format: [''],
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

    // Category form for creating new categories with validations
    this.categoryForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      description: ['', [Validators.maxLength(500)]],
      slug: ['', [Validators.maxLength(100), this.slugValidator]]
    });

    // Image form for adding images with enhanced validations
    this.imageForm = this.fb.group({
      imageUrl: ['', [this.conditionalUrlValidator()]],
      filePath: ['', [this.conditionalFilePathValidator()]],
      altText: ['', [Validators.maxLength(200)]],
      isPrimary: [false],
      displayOrder: [0, [Validators.min(0), Validators.max(999)]]
    });
  }

  // Custom validator for ISBN
  private isbnValidator(control: any) {
    if (!control.value) return null; // ISBN is optional
    
    const isbn = control.value.replace(/[-\s]/g, ''); // Remove hyphens and spaces
    
    // Check if it's either ISBN-10 or ISBN-13
    if (isbn.length === 10) {
      return AddComponent.validateISBN10(isbn) ? null : { invalidIsbn: true };
    } else if (isbn.length === 13) {
      return AddComponent.validateISBN13(isbn) ? null : { invalidIsbn: true };
    } else {
      return { invalidIsbn: true };
    }
  }

  // ISBN-10 validation
  private static validateISBN10(isbn: string): boolean {
    if (!/^\d{9}[\dX]$/.test(isbn)) return false;
    
    let sum = 0;
    for (let i = 0; i < 9; i++) {
      sum += parseInt(isbn[i]) * (10 - i);
    }
    
    const checkDigit = isbn[9] === 'X' ? 10 : parseInt(isbn[9]);
    return (sum + checkDigit) % 11 === 0;
  }

  // ISBN-13 validation
  private static validateISBN13(isbn: string): boolean {
    if (!/^\d{13}$/.test(isbn)) return false;
    
    let sum = 0;
    for (let i = 0; i < 12; i++) {
      sum += parseInt(isbn[i]) * (i % 2 === 0 ? 1 : 3);
    }
    
    const checkDigit = (10 - (sum % 10)) % 10;
    return checkDigit === parseInt(isbn[12]);
  }

  // Custom validator for slug
  private slugValidator(control: any) {
    if (!control.value) return null;
    
    const slugPattern = /^[a-z0-9]+(?:-[a-z0-9]+)*$/;
    return slugPattern.test(control.value) ? null : { invalidSlug: true };
  }

  // Conditional URL validator
  private conditionalUrlValidator() {
    return (control: any) => {
      if (this.imageInputType === 'url' && control.value) {
        const urlPattern = /^(https?:\/\/)?([\da-z\.-]+)\.([a-z\.]{2,6})([\/\w \.-]*)*\/?$/;
        return urlPattern.test(control.value) ? null : { invalidUrl: true };
      }
      return null;
    };
  }

  // Conditional file path validator
  private conditionalFilePathValidator() {
    return (control: any) => {
      if (this.imageInputType === 'filepath' && control.value) {
        const filePathPattern = /^[a-zA-Z0-9\/\._-]+\.(jpg|jpeg|png|gif|webp)$/i;
        return filePathPattern.test(control.value) ? null : { invalidFilePath: true };
      }
      return null;
    };
  }

  private loadCategories(): void {
    this.categoryService.getAllCategories().subscribe({
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

  // Image input type methods
  onImageInputTypeChange(): void {
    // Reset form validation based on input type
    const imageUrlControl = this.imageForm.get('imageUrl');
    const filePathControl = this.imageForm.get('filePath');
    
    if (this.imageInputType === 'url') {
      imageUrlControl?.setValidators([Validators.required, Validators.pattern(/^https?:\/\/.+/)]);
      filePathControl?.clearValidators();
      filePathControl?.setValue('');
    } else {
      filePathControl?.setValidators([Validators.required]);
      imageUrlControl?.clearValidators();
      imageUrlControl?.setValue('');
    }
    
    imageUrlControl?.updateValueAndValidity();
    filePathControl?.updateValueAndValidity();
  }

  // Step navigation methods
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

  // Category management methods
  filterCategories(): void {
    if (!this.categorySearchQuery.trim()) {
      this.filteredCategories = this.availableCategories;
    } else {
      this.filteredCategories = this.availableCategories.filter(category =>
        category.name.toLowerCase().includes(this.categorySearchQuery.toLowerCase())
      );
    }
  }

  selectCategory(category: CategoryModel): void {
    if (!this.selectedCategories.find(c => c.id === category.id)) {
      this.selectedCategories.push(category);
    }
  }

  removeCategory(category: CategoryModel): void {
    this.selectedCategories = this.selectedCategories.filter(c => c.id !== category.id);
  }

  createCategory(): void {
    if (this.categoryForm.valid) {
      const categoryData = this.categoryForm.value;
      this.categoryService.createCategory(categoryData).subscribe({
        next: (newCategory) => {
          this.availableCategories.push(newCategory);
          this.filteredCategories.push(newCategory);
          this.selectedCategories.push(newCategory);
          this.categoryForm.reset();
          this.showCategoryForm = false;
        },
        error: (err) => {
          console.error('Failed to create category', err);
          this.errorMessage = 'Failed to create category';
        }
      });
    }
  }

  // Image management methods
  addImage(): void {
    // Validate based on input type
    const isValid = this.imageInputType === 'url' 
      ? this.imageForm.get('imageUrl')?.valid && this.imageForm.get('imageUrl')?.value
      : this.imageForm.get('filePath')?.valid && this.imageForm.get('filePath')?.value;

    if (isValid) {
      const imageData = this.imageForm.value;
      
      // Set the final imageUrl based on input type
      if (this.imageInputType === 'filepath') {
        imageData.imageUrl = imageData.filePath;
      }
      
      this.selectedImages.push({
        imageUrl: imageData.imageUrl,
        isPrimary: imageData.isPrimary,
        altText: imageData.altText,
        displayOrder: imageData.displayOrder
      });
      
      this.imageForm.reset();
      
      // Set default display order for next image
      this.imageForm.patchValue({
        displayOrder: this.selectedImages.length,
        isPrimary: false
      });
    } else {
      this.errorMessage = this.imageInputType === 'url' 
        ? 'Please enter a valid image URL' 
        : 'Please enter a valid file path';
    }
  }

  removeImage(index: number): void {
    this.selectedImages.splice(index, 1);
  }

  setPrimaryImage(index: number): void {
    this.selectedImages.forEach((img, i) => {
      img.isPrimary = i === index;
    });
  }

  // Enhanced step validation
  isStepValid(step: number): boolean {
    switch (step) {
      case 1:
        const basicFields = ['title', 'author', 'description'];
        return basicFields.every(field => {
          const control = this.bookForm.get(field);
          return control && control.valid;
        });
      
      case 2:
        const detailFields = ['price', 'stockDisplay', 'stockActual', 'salesCategory'];
        const allValid = detailFields.every(field => {
          const control = this.bookForm.get(field);
          return control && control.valid;
        });
        
        // Additional validation: MRP should be >= Price if both are provided
        const price = this.bookForm.get('price')?.value || 0;
        const mrp = this.bookForm.get('mrp')?.value || 0;
        const mrpValid = mrp === 0 || mrp >= price;
        
        return allValid && mrpValid;
      
      case 3:
        return this.selectedCategories.length > 0;
      
      case 4:
        return true; // Images are optional
      
      default:
        return false;
    }
  }

  // Enhanced form submission with better validation
  submitBook(): void {
    // Mark all form fields as touched to show validation errors
    this.markFormGroupTouched(this.bookForm);
    
    if (!this.bookForm.valid) {
      this.errorMessage = 'Please fix all validation errors before submitting.';
      return;
    }
    
    if (this.selectedCategories.length === 0) {
      this.errorMessage = 'Please select at least one category.';
      return;
    }
    
    // Additional cross-field validation
    const price = this.bookForm.get('price')?.value || 0;
    const mrp = this.bookForm.get('mrp')?.value || 0;
    
    if (mrp > 0 && mrp < price) {
      this.errorMessage = 'MRP cannot be less than the selling price.';
      return;
    }
    
    this.isSubmitting = true;
    this.errorMessage = '';
    this.successMessage = '';

      // Construct BookCreateRequest with proper validation
      const bookData: BookCreateRequest = {
        isbn: this.bookForm.value.isbn || undefined,
        title: this.bookForm.value.title,
        author: this.bookForm.value.author,
        description: this.bookForm.value.description,
        language: this.bookForm.value.language || undefined,
        format: this.bookForm.value.format || undefined,
        edition: this.bookForm.value.edition || undefined,
        publisher: this.bookForm.value.publisher || undefined,
        publicationDate: this.bookForm.value.publicationDate ? new Date(this.bookForm.value.publicationDate).toISOString() : undefined,
        pages: this.bookForm.value.pages && this.bookForm.value.pages > 0 ? Number(this.bookForm.value.pages) : undefined,
        weight: this.bookForm.value.weight && this.bookForm.value.weight > 0 ? Number(this.bookForm.value.weight) : undefined,
        dimensions: this.bookForm.value.dimensions || undefined,
        price: Number(this.bookForm.value.price),
        mrp: this.bookForm.value.mrp && this.bookForm.value.mrp > 0 ? Number(this.bookForm.value.mrp) : undefined,
        stockDisplay: Number(this.bookForm.value.stockDisplay),
        stockActual: Number(this.bookForm.value.stockActual),
        salesCategory: this.bookForm.value.salesCategory as 'BEST_SELLING' | 'SPECIAL_OFFERS' | 'NEWLY_LAUNCHED',
        isActive: Boolean(this.bookForm.value.isActive),
        isFeatured: Boolean(this.bookForm.value.isFeatured),
        categoryIds: this.selectedCategories.map(cat => cat.id), // All category IDs are numbers
        images: this.selectedImages.map(img => ({
          imageUrl: img.imageUrl,
          isPrimary: img.isPrimary,
          altText: img.altText || undefined,
          displayOrder: img.displayOrder
        }))
      };

      // Debug: Log the data being sent
      console.log('BookCreateRequest being sent:', JSON.stringify(bookData, null, 2));
      console.log('Selected Categories:', this.selectedCategories);
      console.log('Selected Images:', this.selectedImages);

      this.bookService.createBookWithRelations(bookData).subscribe({
        next: (response: any) => {
          console.log('Book created successfully:', response);
          this.successMessage = 'Book created successfully!';
          this.resetForms();
          this.isSubmitting = false;
        },
        error: (err: any) => {
          console.error('Failed to create book', err);
          console.error('Error details:', err.error);
          this.errorMessage = `Failed to create book: ${err.error?.message || err.message || 'Unknown error'}`;
          this.isSubmitting = false;
        }
      });
  }

  private resetForms(): void {
    this.bookForm.reset();
    this.categoryForm.reset();
    this.imageForm.reset();
    this.selectedCategories = [];
    this.selectedImages = [];
    this.currentStep = 1;
  }

  goBack(): void {
    this.router.navigate(['/admin/dashboard']);
  }

  // Validation helpers
  getStepTitle(step: number): string {
    switch (step) {
      case 1: return 'Basic Information';
      case 2: return 'Additional Details';
      case 3: return 'Categories';
      case 4: return 'Images';
      default: return '';
    }
  }

  // Helper method for template
  isCategorySelected(category: CategoryModel): boolean {
    return !!this.selectedCategories.find(c => c.id === category.id);
  }

  // Image error handler
  onImageError(event: Event): void {
    const imgElement = event.target as HTMLImageElement;
    if (imgElement) {
      imgElement.src = 'assets/placeholder-book.png';
    }
  }

  // Helper method to mark all fields as touched
  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      control?.markAsTouched();
      
      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      }
    });
  }

  // Enhanced error message getter
  getFieldError(fieldName: string, formGroup: FormGroup = this.bookForm): string {
    const field = formGroup.get(fieldName);
    if (!field || !field.errors || !field.touched) return '';
    
    const errors = field.errors;
    
    if (errors['required']) return `${this.getFieldLabel(fieldName)} is required.`;
    if (errors['minlength']) return `${this.getFieldLabel(fieldName)} must be at least ${errors['minlength'].requiredLength} characters.`;
    if (errors['maxlength']) return `${this.getFieldLabel(fieldName)} cannot exceed ${errors['maxlength'].requiredLength} characters.`;
    if (errors['min']) return `${this.getFieldLabel(fieldName)} must be at least ${errors['min'].min}.`;
    if (errors['max']) return `${this.getFieldLabel(fieldName)} cannot exceed ${errors['max'].max}.`;
    if (errors['invalidIsbn']) return 'Please enter a valid ISBN-10 or ISBN-13.';
    if (errors['invalidSlug']) return 'Slug must contain only lowercase letters, numbers, and hyphens.';
    if (errors['invalidUrl']) return 'Please enter a valid URL.';
    if (errors['invalidFilePath']) return 'Please enter a valid file path with supported image extension.';
    
    return 'Invalid input.';
  }

  // Helper method to get user-friendly field labels
  private getFieldLabel(fieldName: string): string {
    const labels: { [key: string]: string } = {
      'title': 'Title',
      'author': 'Author',
      'description': 'Description',
      'isbn': 'ISBN',
      'publisher': 'Publisher',
      'language': 'Language',
      'edition': 'Edition',
      'price': 'Price',
      'mrp': 'MRP',
      'stockDisplay': 'Display Stock',
      'stockActual': 'Actual Stock',
      'salesCategory': 'Sales Category',
      'pages': 'Pages',
      'weight': 'Weight',
      'dimensions': 'Dimensions',
      'imageUrl': 'Image URL',
      'filePath': 'File Path',
      'altText': 'Alt Text',
      'displayOrder': 'Display Order',
      'name': 'Category Name',
      'slug': 'Slug'
    };
    
    return labels[fieldName] || fieldName;
  }
}