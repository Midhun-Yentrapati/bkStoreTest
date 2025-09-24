import { NgFor } from '@angular/common';
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { NgClass } from '@angular/common';
import { CategoryModel } from '../../../models/category.model';

@Component({
  selector: 'app-category-nav',
  imports: [NgFor, NgClass],
  templateUrl: './category-nav.component.html',
  styleUrl: './category-nav.component.css'
})
export class CategoryNavComponent {

  @Input() categories: CategoryModel[] = [];
  @Output() categorySelected = new EventEmitter<string>();

  selectedCategory: string = 'All';

  selectCategory(categoryName: string): void {
    this.selectedCategory = categoryName;
    this.categorySelected.emit(categoryName);
  }
}
