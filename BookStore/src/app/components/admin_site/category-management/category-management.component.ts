
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CategoryService } from '../../../services/category.service';
import { CategoryModel } from '../../../models/category.model';
import { catchError, of } from 'rxjs';

@Component({
  selector: 'app-category-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './category-management.component.html',
  styleUrls: ['./category-management.component.css']
})
export class CategoryManagementComponent implements OnInit {
  categories: CategoryModel[] = [];
  newCategoryName: string = '';
  categoryInputError: boolean = false;

  isLoading: boolean = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  constructor(private categoryService: CategoryService, private router: Router) { }

  ngOnInit(): void {
    this.fetchCategories();
  }

  fetchCategories(): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.successMessage = null;
    
    this.categoryService.getAllCategories().subscribe({
      next: (data: CategoryModel[]) => {
        this.categories = data;
        this.isLoading = false;
      },
      error: (error: any) => {
        console.error('Error fetching categories:', error);
        this.errorMessage = 'Failed to load categories.';
        this.isLoading = false;
      }
    });
  }

  private _generateNextCategoryId(): number {
    let maxNumId = 0;
    if (this.categories && this.categories.length > 0) {
      maxNumId = Math.max(...this.categories.map(cat => cat.id));
    }
    return Math.max(maxNumId + 1, 11);
  }

  onAddCategory(): void {
    // Validate category name
    if (!this.newCategoryName.trim()) {
      this.categoryInputError = true;
      this.errorMessage = 'Category name cannot be empty.';
      this.clearMessagesAfterDelay(3000, true);
      return;
    }

    if (this.newCategoryName.trim().length < 2) {
      this.categoryInputError = true;
      this.errorMessage = 'Category name must be at least 2 characters long.';
      this.clearMessagesAfterDelay(3000, true);
      return;
    }

    // Check if category already exists
    const existingCategory = this.categories.find(cat => 
      cat.name.toLowerCase() === this.newCategoryName.trim().toLowerCase()
    );

    if (existingCategory) {
      this.categoryInputError = true;
      this.errorMessage = 'Category with this name already exists.';
      this.clearMessagesAfterDelay(3000, true);
      return;
    }

    this.categoryInputError = false;
    this.isLoading = true;
    this.errorMessage = null;
    this.successMessage = null;

    // For now, we'll just add it to the local array since CategoryService doesn't support creating new categories
    // In a real application, you would need to implement category creation in the service
    const newId = this._generateNextCategoryId();

    const categoryPayload: CategoryModel = {
      id: newId,
      name: this.newCategoryName.trim()
    };

    // Add to local array for now
    this.categories.push(categoryPayload);
    this.newCategoryName = '';
    this.successMessage = 'Category added successfully! (Note: This is local only - implement backend creation)';
    this.isLoading = false;
    this.clearMessagesAfterDelay();
  }

  onDelete(id: number): void { 
    if (confirm('Are you sure you want to delete this category? This cannot be undone.')) {
      this.isLoading = true;
      this.errorMessage = null;
      this.successMessage = null;

      // For now, we'll just remove it from the local array since CategoryService doesn't support deleting categories
      // In a real application, you would need to implement category deletion in the service
      this.categories = this.categories.filter(c => c.id !== id);
      this.successMessage = 'Category deleted successfully! (Note: This is local only - implement backend deletion)';
      this.isLoading = false;
      this.clearMessagesAfterDelay();
    }
  }

  clearMessagesAfterDelay(delay: number = 3000, isError: boolean = false): void {
    setTimeout(() => {
      if (isError) {
        this.errorMessage = null;
      } else {
        this.successMessage = null;
      }
      this.isLoading = false;
    }, delay);
  }

  goBack(): void {
    this.router.navigate(['/admin-main']);
  }
}
