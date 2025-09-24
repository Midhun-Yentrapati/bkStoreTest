import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { BookService } from './book.service';
import { ChartData } from '../models/book';

@Injectable({
  providedIn: 'root'
})
export class ChartService {
  constructor(private http: HttpClient, private bookService: BookService) {}

  getHighlySoldBooks(limit: number = 10): Observable<ChartData> {
    return this.bookService.getHighlySoldBooks().pipe(
      map(books => ({
        labels: books.slice(0, limit).map(book => book.title),
        data: books.slice(0, limit).map(book => book.no_of_books_sold || 0)
      })),
      catchError(error => {
        console.error('Error fetching highly sold books:', error);
        // Fallback to empty data instead of mock data
        return of({
          labels: [],
          data: []
        });
      })
    );
  }

  getLeastSoldBooks(limit: number = 10): Observable<ChartData> {
    return this.bookService.getLeastSoldBooks().pipe(
      map(books => ({
        labels: books.slice(0, limit).map(book => book.title),
        data: books.slice(0, limit).map(book => book.no_of_books_sold || 0)
      })),
      catchError(error => {
        console.error('Error fetching least sold books:', error);
        // Fallback to empty data instead of mock data
        return of({
          labels: [],
          data: []
        });
      })
    );
  }
}
