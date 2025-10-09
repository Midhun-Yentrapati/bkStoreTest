import { Component, OnInit, OnDestroy, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { BookService } from '../../../services/book.service';
import { BookWithSales } from '../../../models/book.model';
import { Router } from '@angular/router';
import { ModernPieChartComponent } from '../../shared/modern-pie-chart/modern-pie-chart.component';
import { HttpClient } from '@angular/common/http';

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

  constructor(private bookService: BookService, private router: Router, private http: HttpClient) {
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

    // Use analytics service for least selling books (same pattern as highly-sold component)
    import('../../../services/analytics.service').then(({ AnalyticsService }) => {
      const analyticsService = new AnalyticsService(this.http);
      this.salesSubscription = analyticsService.getLeastSellingBooksData().subscribe({
        next: (books: any[]) => {
          this.isLoading = false;
          console.log('Least Selling Books from Analytics Service:', books);
          
          if (books && books.length > 0) {
            this.hasChartData = true;
            // Filter out books with 0 sales and map the data
            this.chartData = books
              .filter(book => {
                const sales = book.quantitySold || book.no_of_books_sold || book.sales || 0;
                return sales > 0;
              })
              .map(book => ({
                label: book.title || book.name,
                value: book.quantitySold || book.no_of_books_sold || book.sales || 0
              }));
            
            console.log('Chart data for least sold books:', this.chartData);
            
            // If after filtering we have no data, show no data message
            if (this.chartData.length === 0) {
              this.hasChartData = false;
              console.log('All books had 0 sales, showing no data message');
            }
          } else {
            this.hasChartData = false;
            this.chartData = [];
            console.log('No books returned from analytics service');
          }
        },
        error: (error: any) => {
          this.isLoading = false;
          this.errorMessage = 'Failed to load least sold books data.';
          console.error('Least Sold Books Chart Component: Fetch error:', error);
        }
      });
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