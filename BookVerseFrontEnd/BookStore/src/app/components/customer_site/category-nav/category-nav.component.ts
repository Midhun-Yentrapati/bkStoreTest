import { NgFor } from '@angular/common';
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { NgClass } from '@angular/common';

@Component({
  selector: 'app-category-nav',
  imports: [NgFor, NgClass],
  templateUrl: './category-nav.component.html',
  styleUrl: './category-nav.component.css'
})
export class CategoryNavComponent {

  @Input() categories: any[] = [];
  @Output() categorySelected = new EventEmitter<string>();

  selectedCategory: string = 'All';

  selectCategory(category: string): void {
    this.selectedCategory = category;
    this.categorySelected.emit(category);
  }
}
