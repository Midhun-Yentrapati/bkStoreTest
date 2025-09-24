import { Component, Input } from '@angular/core';
import { BookModel } from '../../../models/book.model';
import { BookCardComponent } from "../book-card/book-card.component";
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-vertical-book-grid',
  imports: [BookCardComponent, CommonModule],
  templateUrl: './vertical-book-grid.component.html',
  styleUrl: './vertical-book-grid.component.css'
})
export class VerticalBookGridComponent {

  @Input() books: BookModel[] = [];

}
