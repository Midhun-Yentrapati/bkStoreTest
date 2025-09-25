import { Component, OnInit, OnDestroy, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, FormControl } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, takeUntil } from 'rxjs/operators';
import { CategoryService } from '../../../services/category.service';
import { CategoryModel } from '../../../models/category.model';

@Component({
  selector: 'app-filter-sidebar',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './filter-sidebar.component.html',
  styleUrls: ['./filter-sidebar.component.css']
})
export class FilterSidebarComponent implements OnInit, OnDestroy {
  @Output() filtersChanged = new EventEmitter<any>();
  filterForm: FormGroup;
  categories: CategoryModel[] = [];
  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private categoryService: CategoryService
  ) {
    this.filterForm = this.fb.group({
      categories: this.fb.group({}), // Categories will be added dynamically
      price: [5000] // Default max price - match the HTML max value
    });
  }

  ngOnInit(): void {
    // Fetch categories and dynamically add form controls for them
    this.categoryService.getAllCategories().subscribe(cats => {
      this.categories = cats;
      const categoriesFormGroup = this.filterForm.get('categories') as FormGroup;
      this.categories.forEach(cat => {
        // Use a safe key for the form control name
        categoriesFormGroup.addControl(this.getSafeFormControlName(cat.id), this.fb.control(false));
      });
    });

    // Listen for form changes and emit them after a short delay
    this.filterForm.valueChanges.pipe(
      debounceTime(500), // Wait 500ms after the user stops interacting
      takeUntil(this.destroy$)
    ).subscribe(formValue => {
      // Reformat the categories object into an array of selected category IDs
      const selectedCategoryIds = Object.keys(formValue.categories)
        .filter(key => formValue.categories[key])
        .map(safeKey => this.getOriginalId(safeKey)); // Convert safe key back to original ID
      
      this.filtersChanged.emit({
        categories: selectedCategoryIds,
        maxPrice: formValue.price
      });
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // Helper methods to handle numeric category IDs from backend
  getSafeFormControlName(id: number): string {
    return `cat_${id}`;
  }

  getOriginalId(safeKey: string): number {
    const idStr = safeKey.replace('cat_', '');
    return Number(idStr);
  }

  resetFilters() {
    this.filterForm.reset({ 
      categories: {},
      price: 5000 // Match the default value
    });
  }
}
