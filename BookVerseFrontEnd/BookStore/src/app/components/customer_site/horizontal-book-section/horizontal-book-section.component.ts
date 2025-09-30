import { Component, Input} from '@angular/core';
import { BookModel } from '../../../models/book.model';
import { NgFor } from '@angular/common';
import { BookCardComponent } from '../book-card/book-card.component';



@Component({
  selector: 'app-horizontal-book-section',
  imports: [NgFor, BookCardComponent],
  templateUrl: './horizontal-book-section.component.html',
  styleUrl: './horizontal-book-section.component.css',
})
export class HorizontalBookSectionComponent {

  @Input() books: BookModel[] = [];
  @Input() sectionTitle: string = '';

}
