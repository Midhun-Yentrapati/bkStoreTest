
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
  categoryDescription: string = '';
  categoryImage: string = '';
  displayOrder: number = 0;
  isActive: boolean = true;
  categoryInputError: boolean = false;

  isLoading: boolean = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  editingCategory: CategoryModel | null = null;

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

    const categoryPayload: CategoryModel = {
      id: 0, // Backend will assign ID
      name: this.newCategoryName.trim(),
      slug: this.generateSlug(this.newCategoryName.trim()),
      description: this.categoryDescription.trim() || undefined,
      image: this.categoryImage.trim() || undefined,
      displayOrder: this.displayOrder || this.categories.length + 1,
      isActive: this.isActive
    };

    this.categoryService.createCategory(categoryPayload).subscribe({
      next: (newCategory) => {
        this.categories.push(newCategory);
        this.resetForm();
        this.successMessage = 'Category added successfully!';
        this.isLoading = false;
        this.clearMessagesAfterDelay();
      },
      error: (error) => {
        console.error('Error creating category:', error);
        this.errorMessage = 'Failed to create category. Please try again.';
        this.isLoading = false;
        this.clearMessagesAfterDelay(3000, true);
      }
    });
  }

  private generateSlug(name: string): string {
    return name.toLowerCase()
      .replace(/[^a-z0-9 -]/g, '') // Remove invalid chars
      .replace(/\s+/g, '-') // Replace spaces with -
      .replace(/-+/g, '-') // Replace multiple - with single -
      .replace(/^-+|-+$/g, ''); // Trim - from start and end
  }

  onDelete(id: number): void { 
    if (confirm('Are you sure you want to delete this category? This cannot be undone.')) {
      this.isLoading = true;
      this.errorMessage = null;
      this.successMessage = null;

      this.categoryService.deleteCategory(id).subscribe({
        next: () => {
          this.categories = this.categories.filter(c => c.id !== id);
          this.successMessage = 'Category deleted successfully!';
          this.isLoading = false;
          this.clearMessagesAfterDelay();
        },
        error: (error) => {
          console.error('Error deleting category:', error);
          this.errorMessage = 'Failed to delete category. Please try again.';
          this.isLoading = false;
          this.clearMessagesAfterDelay(3000, true);
        }
      });
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

  onEdit(category: CategoryModel): void {
    this.editingCategory = category;
    this.newCategoryName = category.name;
    this.categoryDescription = category.description || '';
    this.categoryImage = category.image || '';
    this.displayOrder = category.displayOrder || 0;
    this.isActive = category.isActive !== false;
  }

  onToggleStatus(category: CategoryModel): void {
    const updatedCategory = { ...category, isActive: !category.isActive };
    
    this.categoryService.updateCategory(category.id, updatedCategory).subscribe({
      next: (updated) => {
        const index = this.categories.findIndex(c => c.id === category.id);
        if (index !== -1) {
          this.categories[index] = updated;
        }
        this.successMessage = `Category ${updated.isActive ? 'activated' : 'deactivated'} successfully!`;
        this.clearMessagesAfterDelay();
      },
      error: (error) => {
        console.error('Error updating category status:', error);
        this.errorMessage = 'Failed to update category status. Please try again.';
        this.clearMessagesAfterDelay(3000, true);
      }
    });
  }

  resetForm(): void {
    this.newCategoryName = '';
    this.categoryDescription = '';
    this.categoryImage = '';
    this.displayOrder = 0;
    this.isActive = true;
    this.editingCategory = null;
    this.categoryInputError = false;
  }

  goBack(): void {
    this.router.navigate(['/admin-main']);
  }
}
