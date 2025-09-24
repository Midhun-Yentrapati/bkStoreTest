import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { BookService } from '../../../services/book.service';
import { CategoryService } from '../../../services/category.service';
import { BookModel, BookCreateRequest, BookImageRequest } from '../../../models/book.model';
import { CategoryModel } from '../../../models/category.model';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';

@Component({
  selector: 'app-edit-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './edit-page.component.html',
  styleUrls: ['./edit-page.component.css']
})
export class EditPageComponent implements OnInit, OnDestroy {
  // Form groups
  bookForm!: FormGroup;
  
  // Data
  allAvailableCategories: CategoryModel[] = [];
  selectedCategories: CategoryModel[] = [];
  editableBook: BookModel | null = null;
  selectedSalesCategory: 'BEST_SELLING' | 'SPECIAL_OFFERS' | 'NEWLY_LAUNCHED' | '' = '';

  // UI state
  isLoading: boolean = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  imageUrlError: boolean = false;
  isEditMode: boolean = false;
  imagePreviewUrl: SafeUrl | string | null = null;
  
  // Image management
  imageInputType: 'url' | 'filepath' = 'url';
  newImageUrl: string = '';
  newFilePath: string = '';

  // Original state for comparison
  private originalBookState: BookModel | null = null;
  private originalSalesCategoryState: 'BEST_SELLING' | 'SPECIAL_OFFERS' | 'NEWLY_LAUNCHED' | '' = '';
  private subscription = new Subscription();

  constructor(
    private fb: FormBuilder,
    private bookService: BookService,
    private categoryService: CategoryService,
    private route: ActivatedRoute,
    private router: Router,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit(): void {
    this.initializeForm();
    const bookId = this.route.snapshot.paramMap.get('id');
    if (bookId) {
      this.loadBookData(bookId);
    } else {
      this.errorMessage = 'No book ID provided';
    }
  }

  private initializeForm(): void {
    this.bookForm = this.fb.group({
      id: [''],
      isbn: ['', [this.isbnValidator]],
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
  }

  // Custom validator for ISBN
  private isbnValidator(control: any) {
    if (!control.value) return null; // ISBN is optional
    
    const isbn = control.value.replace(/[-\s]/g, ''); // Remove hyphens and spaces
    
    // Check if it's either ISBN-10 or ISBN-13
    if (isbn.length === 10) {
      return EditPageComponent.validateISBN10(isbn) ? null : { invalidIsbn: true };
    } else if (isbn.length === 13) {
      return EditPageComponent.validateISBN13(isbn) ? null : { invalidIsbn: true };
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

  // Enhanced error message getter
  getFieldError(fieldName: string): string {
    const field = this.bookForm.get(fieldName);
    if (!field || !field.errors || !field.touched) return '';
    
    const errors = field.errors;
    
    if (errors['required']) return `${this.getFieldLabel(fieldName)} is required.`;
    if (errors['minlength']) return `${this.getFieldLabel(fieldName)} must be at least ${errors['minlength'].requiredLength} characters.`;
    if (errors['maxlength']) return `${this.getFieldLabel(fieldName)} cannot exceed ${errors['maxlength'].requiredLength} characters.`;
    if (errors['min']) return `${this.getFieldLabel(fieldName)} must be at least ${errors['min'].min}.`;
    if (errors['max']) return `${this.getFieldLabel(fieldName)} cannot exceed ${errors['max'].max}.`;
    if (errors['invalidIsbn']) return 'Please enter a valid ISBN-10 or ISBN-13.';
    
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
      'dimensions': 'Dimensions'
    };
    
    return labels[fieldName] || fieldName;
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  private loadBookData(bookId: string): void {
    this.isLoading = true;
    this.errorMessage = null;

    this.subscription.add(
      this.bookService.getBookWithRelations(bookId).subscribe({
        next: (book) => {
          this.editableBook = { ...book };
          this.selectedCategories = [...book.categories];
          this.selectedSalesCategory = book.salesCategory as 'BEST_SELLING' | 'SPECIAL_OFFERS' | 'NEWLY_LAUNCHED';
          this.originalBookState = { ...book };
          this.originalSalesCategoryState = book.salesCategory as 'BEST_SELLING' | 'SPECIAL_OFFERS' | 'NEWLY_LAUNCHED';
          
          // Populate form with book data
          this.bookForm.patchValue({
            id: book.id,
            isbn: book.isbn || '',
            title: book.title,
            author: book.author,
            description: book.description,
            language: book.language || '',
            format: book.format || '',
            edition: book.edition || '',
            publisher: book.publisher || '',
            publicationDate: book.publicationDate || '',
            pages: book.pages || 0,
            weight: book.weight || 0,
            dimensions: book.dimensions || '',
            price: book.price,
            mrp: book.mrp || 0,
            stockDisplay: book.stockDisplay,
            stockActual: book.stockActual,
            salesCategory: book.salesCategory,
            isActive: book.isActive,
            isFeatured: book.isFeatured
          });
          
          // Set image preview
          if (book.images && book.images.length > 0) {
            const primaryImage = book.images.find(img => img.isPrimary) || book.images[0];
            this.imagePreviewUrl = primaryImage.imageUrl;
          }
          
          this.isLoading = false;
        },
        error: (error) => {
          this.isLoading = false;
          this.errorMessage = `Failed to load book data: ${error.message}`;
          console.error('EditBookPage: Load error:', error);
        }
      })
    );
  }

  // Enhanced save changes with form validation
  saveChanges(): void {
    if (!this.bookForm.valid) {
      this.markFormGroupTouched(this.bookForm);
      this.errorMessage = 'Please fix all validation errors before saving.';
      return;
    }

    // Additional cross-field validation
    const formValue = this.bookForm.value;
    if (formValue.mrp > 0 && formValue.mrp < formValue.price) {
      this.errorMessage = 'MRP cannot be less than the selling price.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;
    this.successMessage = null;

    const updatedBook: BookModel = {
      ...this.editableBook!,
      ...formValue,
      categories: this.selectedCategories,
      salesCategory: this.selectedSalesCategory
    };

    this.subscription.add(
      this.bookService.updateBook(updatedBook.id, updatedBook).subscribe({
        next: (response) => {
          this.isLoading = false;
          this.successMessage = 'Book updated successfully!';
          this.isEditMode = false;
          
          // Update the original state
          this.originalBookState = { ...updatedBook };
          this.originalSalesCategoryState = this.selectedSalesCategory;
          
          // Update editableBook with the response
          this.editableBook = { ...response };
          
          setTimeout(() => {
            this.successMessage = null;
          }, 3000);
        },
        error: (error) => {
          this.isLoading = false;
          this.errorMessage = `Failed to update book: ${error.message}`;
          console.error('EditBookPage: Update error:', error);
        }
      })
    );
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

  // Enhanced validation check
  canSaveChanges(): boolean {
    return this.bookForm.valid && this.selectedCategories.length > 0;
  }

  enterEditMode(): void {
    this.isEditMode = true;
    this.errorMessage = null;
    this.successMessage = null;
  }

  cancelEdit(): void {
    if (this.originalBookState) {
      // Reset form to original values
      this.bookForm.patchValue(this.originalBookState);
      this.selectedCategories = [...this.originalBookState.categories];
      this.selectedSalesCategory = this.originalSalesCategoryState;
      this.editableBook = { ...this.originalBookState };
    }
    
    this.isEditMode = false;
    this.errorMessage = null;
    this.successMessage = null;
  }

  onDelete(): void {
    if (!this.editableBook || !this.editableBook.id) {
      this.errorMessage = 'Cannot delete: Book ID is missing.';
      return;
    }

    if (window.confirm('Are you sure you want to delete this book? This action cannot be undone.')) {
      this.isLoading = true;
      this.errorMessage = null;
      this.successMessage = null;

      this.subscription.add(
        this.bookService.deleteBook(this.editableBook.id).subscribe({
          next: () => {
            this.isLoading = false;
            this.successMessage = 'Book has been deleted successfully.';
            console.log(`EditBookPage: Book with ID ${this.editableBook?.id} deleted.`);

            // Navigate after 2 seconds
            setTimeout(() => {
              this.router.navigate(['/admin/inventory']);
            }, 2000);
          },
          error: (error) => {
            this.isLoading = false;
            this.errorMessage = `Failed to delete book: ${error.message}`;
            console.error('EditBookPage: Delete error:', error);
          }
        })
      );
    }
  }

  // Removed file upload functionality - now using image path selection

  onImageUrlChange(): void {
    if (this.editableBook && this.editableBook.images.length > 0) {
      this.imagePreviewUrl = this.editableBook.images[0].imageUrl;
      this.imageUrlError = false;
    }
  }

  // Category management methods
  selectCategory(category: CategoryModel): void {
    if (!this.selectedCategories.find(c => c.id === category.id)) {
      this.selectedCategories.push(category);
    }
  }

  removeCategory(category: CategoryModel): void {
    this.selectedCategories = this.selectedCategories.filter(c => c.id !== category.id);
  }

  // Sales category options
  getSalesCategoryOptions(): ('BEST_SELLING' | 'SPECIAL_OFFERS' | 'NEWLY_LAUNCHED')[] {
    return ['BEST_SELLING', 'NEWLY_LAUNCHED', 'SPECIAL_OFFERS'];
  }

  // Navigation method
  goBack(): void {
    this.router.navigate(['/admin/inventory']);
  }

  // Helper methods for template compatibility
  getCategoryBadgeClasses(category: CategoryModel): string {
    return 'px-2 py-1 rounded-full text-xs bg-blue-100 text-blue-800';
  }

  getCategoriesDisplay(categories: CategoryModel[]): string {
    return categories.map(cat => cat.name).join(', ');
  }

  setCategoriesFromInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    // This would need to be implemented based on your requirements
    console.log('Categories input changed:', input.value);
  }

  getFirstImageUrl(): string {
    if (this.editableBook && this.editableBook.images && this.editableBook.images.length > 0) {
      return this.editableBook.images[0].imageUrl;
    }
    return '';
  }

  setFirstImageUrl(url: string): void {
    if (this.editableBook && this.editableBook.images && this.editableBook.images.length > 0) {
      this.editableBook.images[0].imageUrl = url;
    } else if (this.editableBook) {
      // Create new image if none exists
      this.editableBook.images = [{
        imageUrl: url,
        isPrimary: true,
        altText: this.editableBook.title,
        displayOrder: 0
      }];
    }
  }

  updateFirstImageUrl(url: string): void {
    this.setFirstImageUrl(url);
    this.imagePreviewUrl = url;
  }

  formatDate(dateString: string | undefined): string {
    if (!dateString) return '';
    
    try {
      // Handle different date formats
      let date: Date;
      
      // If it's already a valid date string, parse it
      if (typeof dateString === 'string') {
        // Handle ISO date strings and other formats
        date = new Date(dateString);
        
        // If parsing failed, try to handle YYYY-MM-DD format specifically
        if (isNaN(date.getTime()) && dateString.includes('-')) {
          const parts = dateString.split('-');
          if (parts.length === 3) {
            date = new Date(parseInt(parts[0]), parseInt(parts[1]) - 1, parseInt(parts[2]));
          }
        }
      } else {
        date = new Date(dateString);
      }
      
      // Check if date is valid
      if (isNaN(date.getTime())) {
        console.warn('Invalid date string:', dateString);
        return 'Invalid Date';
      }
      
      return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      });
    } catch (error) {
      console.error('Error formatting date:', error, dateString);
      return dateString || 'Invalid Date';
    }
  }

  // Removed file upload methods - now using image path selection

  // Date handling methods
  formatDateForInput(dateString: string | undefined): string {
    if (!dateString) return '';
    
    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) return '';
      
      // Format as YYYY-MM-DD for HTML date input
      return date.toISOString().split('T')[0];
    } catch (error) {
      return '';
    }
  }

  // Statistics helper methods
  getStockStatus(): string {
    const stock = this.editableBook?.stockActual || 0;
    if (stock === 0) return 'Out of Stock';
    if (stock < 10) return 'Low Stock';
    if (stock < 50) return 'Medium Stock';
    return 'In Stock';
  }

  getStockStatusClass(): string {
    const stock = this.editableBook?.stockActual || 0;
    if (stock === 0) return 'status-danger';
    if (stock < 10) return 'status-warning';
    if (stock < 50) return 'status-info';
    return 'status-success';
  }

  updatePublicationDate(event: any): void {
    if (this.editableBook) {
      this.editableBook.publicationDate = event.target.value;
    }
  }

  // Image input type methods
  onImageInputTypeChange(): void {
    // Reset inputs when switching types
    if (this.imageInputType === 'url') {
      this.newFilePath = '';
    } else {
      this.newImageUrl = '';
    }
  }

  updateImageFromInput(): void {
    const imageUrl = this.imageInputType === 'url' ? this.newImageUrl : this.newFilePath;
    
    if (imageUrl.trim()) {
      this.updateFirstImageUrl(imageUrl);
      this.imagePreviewUrl = imageUrl;
      this.imageUrlError = false;
    }
  }
}