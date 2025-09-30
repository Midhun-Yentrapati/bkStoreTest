import { Component, OnInit, OnDestroy, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { BookService } from '../../../services/book.service';
import { BookWithSales } from '../../../models/book.model';
import { Router } from '@angular/router';
import { ModernPieChartComponent } from '../../shared/modern-pie-chart/modern-pie-chart.component';

@Component({
  selector: 'app-least-sold',
  standalone: true,
  imports: [CommonModule, ModernPieChartComponent],
  templateUrl: './least-sold.component.html', // Point to external HTML
  styleUrls: ['./modern-chart.component.css']  // Point to external CSS
})
export class LeastSoldComponent implements OnInit, OnDestroy {

  private salesSubscription: Subscription | undefined;

  @Input() limit: number = 10;
  @Input() chartType: 'bar' | 'pie' = 'bar';

  isLoading: boolean = false;
  errorMessage: string | null = null;
  hasChartData: boolean = false;
  chartData: { label: string; value: number }[] = [];

  constructor(private bookService: BookService, private router: Router) {
    // Chart.js components are manually registered in yearly-sales-chart component
    // No need to re-register here since we're using ModernPieChartComponent
  }

  ngOnInit(): void {
    this.fetchLeastSoldData();
  }

  fetchLeastSoldData(): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.hasChartData = false;

    // Use the new getLeastSoldBooks method
    this.salesSubscription = this.bookService.getLeastSoldBooks(this.limit).subscribe({
      next: (books: BookWithSales[]) => {
        this.isLoading = false;
        if (books && books.length > 0) {
          this.hasChartData = true;
          this.chartData = books.map(book => ({
            label: book.title,
            value: book.no_of_books_sold
          }));
        } else {
          this.hasChartData = false;
          this.chartData = [];
        }
      },
      error: (error: any) => {
        this.isLoading = false;
        this.errorMessage = 'Failed to load least sold books data.';
        console.error('Least Sold Books Chart Component: Fetch error:', error);
      }
    });
  }



  ngOnDestroy(): void {
    if (this.salesSubscription) {
      this.salesSubscription.unsubscribe();
    }
  }
  goBack(): void {
    this.router.navigate(['/admin-main']);
  }
}