import { Component, OnInit, OnDestroy, AfterViewInit, ViewChild, ElementRef, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  Chart,
  ChartConfiguration,
  ChartOptions,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  LineController,
  Title,
  Tooltip,
  Legend,
  Filler
} from 'chart.js';
import { Subscription } from 'rxjs';
import { OrderService } from '../../../services/order.service';
import { Order } from '../../../models/order.model';

@Component({
  selector: 'app-yearly-sales-chart',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './yearly-sales-chart.component.html',
  styleUrls: ['./yearly-sales-chart.component.css']
})
export class YearlySalesChartComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('yearlySalesChart', { static: false }) chartCanvas!: ElementRef<HTMLCanvasElement>;
  
  private chart: Chart | null = null;
  private ordersSubscription: Subscription | undefined;
  private viewInitialized = false;
  private chartRetryCount = 0;
  private maxRetries = 3;
  
  orders: Order[] = [];
  isLoading = true;
  selectedYearRange = 'last-5-years';
  
  // Chart data
  chartData: { year: number; sales: number }[] = [];
  
  // Statistics
  totalSalesInPeriod = 0;
  averageYearlyGrowth = 0;
  bestPerformingYear = '';
  
  constructor(
    private orderService: OrderService,
    private cdr: ChangeDetectorRef
  ) {
    // Register Chart.js components
    Chart.register(
      CategoryScale,
      LinearScale,
      PointElement,
      LineElement,
      LineController,
      Title,
      Tooltip,
      Legend,
      Filler
    );
  }
  
  ngOnInit(): void {
    // Small delay to help with Angular hydration stability
    setTimeout(() => {
      this.loadOrdersData();
    }, 100);
  }
  
  ngAfterViewInit(): void {
    this.viewInitialized = true;
    // No need to create chart here, it will be handled in loadOrdersData
  }
  
  ngOnDestroy(): void {
    if (this.ordersSubscription) {
      this.ordersSubscription.unsubscribe();
    }
    if (this.chart) {
      this.chart.destroy();
    }
  }
  
  loadOrdersData(): void {
    this.isLoading = true;
    this.ordersSubscription = this.orderService.getAllOrders().subscribe({
      next: (orders: Order[]) => {
        this.orders = orders;
        this.processYearlyData();
        this.isLoading = false;
        
        // Use setTimeout to ensure DOM is updated after isLoading changes
        setTimeout(() => {
          this.createChart();
        }, 200);
      },
      error: (error) => {
        console.error('Error loading orders:', error);
        this.isLoading = false;
      }
    });
  }
  
  processYearlyData(): void {
    const currentYear = new Date().getFullYear();
    const yearsToShow = this.getYearsToShow();
    
    // Group orders by year
    const yearlyData: { [year: number]: number } = {};
    
    this.orders.forEach(order => {
      const orderYear = new Date(order.orderDate).getFullYear();
      if (yearsToShow.includes(orderYear)) {
        if (!yearlyData[orderYear]) {
          yearlyData[orderYear] = 0;
        }
        yearlyData[orderYear] += order.finalAmount || order.totalAmount || 0;
      }
    });
    
    // Create chart data with sales only
    this.chartData = yearsToShow.map(year => {
      const sales = yearlyData[year] || 0;
      return { year, sales };
    }).sort((a, b) => a.year - b.year);
    
    // Calculate statistics
    this.calculateStatistics();
  }
  
  getYearsToShow(): number[] {
    const currentYear = new Date().getFullYear();
    
    switch (this.selectedYearRange) {
      case 'last-3-years':
        return [currentYear - 2, currentYear - 1, currentYear];
      case 'last-5-years':
        return [currentYear - 4, currentYear - 3, currentYear - 2, currentYear - 1, currentYear];
      case 'last-7-years':
        return Array.from({ length: 7 }, (_, i) => currentYear - 6 + i);
      case 'last-10-years':
        return Array.from({ length: 10 }, (_, i) => currentYear - 9 + i);
      default:
        return [currentYear - 4, currentYear - 3, currentYear - 2, currentYear - 1, currentYear];
    }
  }
  
  calculateStatistics(): void {
    if (this.chartData.length === 0) return;
    
    // Total sales in period
    this.totalSalesInPeriod = this.chartData.reduce((sum, data) => sum + data.sales, 0);
    
    // Best performing year
    const bestYear = this.chartData.reduce((best, current) => 
      current.sales > best.sales ? current : best
    );
    this.bestPerformingYear = bestYear.year.toString();
    
    // Average yearly growth
    let totalGrowth = 0;
    let growthCount = 0;
    
    for (let i = 1; i < this.chartData.length; i++) {
      const current = this.chartData[i];
      const previous = this.chartData[i - 1];
      
      if (previous.sales > 0) {
        const growth = ((current.sales - previous.sales) / previous.sales) * 100;
        totalGrowth += growth;
        growthCount++;
      }
    }
    
    this.averageYearlyGrowth = growthCount > 0 ? totalGrowth / growthCount : 0;
  }
  
  createChart(): void {
    try {
      // Safety check - ensure canvas element exists
      if (!this.chartCanvas || !this.chartCanvas.nativeElement) {
        if (this.chartRetryCount < this.maxRetries) {
          this.chartRetryCount++;
          console.warn(`Canvas element not available, retrying (${this.chartRetryCount}/${this.maxRetries})`);
          setTimeout(() => {
            this.createChart();
          }, 500 * this.chartRetryCount); // Exponential backoff
        } else {
          console.error('Failed to create chart after maximum retries');
        }
        return;
      }
      
      if (this.chart) {
        this.chart.destroy();
        this.chart = null;
      }
      
      const ctx = this.chartCanvas.nativeElement.getContext('2d');
      if (!ctx) {
        console.warn('Cannot get 2D context from canvas');
        return;
      }
    
    const config: ChartConfiguration = {
      type: 'line',
      data: {
        labels: this.chartData.map(d => d.year.toString()),
        datasets: [
          {
            label: 'Sales Revenue',
            data: this.chartData.map(d => d.sales),
            borderColor: '#60a5fa', // Light blue
            backgroundColor: 'rgba(96, 165, 250, 0.1)',
            borderWidth: 3,
            fill: true,
            tension: 0.4,
            pointBackgroundColor: '#3b82f6',
            pointBorderColor: '#ffffff',
            pointBorderWidth: 2,
            pointRadius: 6,
            pointHoverRadius: 8
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: true,
            position: 'bottom',
            labels: {
              color: '#6b7280',
              usePointStyle: true,
              padding: 20,
              font: {
                size: 12,
                family: 'system-ui, sans-serif'
              }
            }
          },
          tooltip: {
            backgroundColor: 'rgba(255, 255, 255, 0.95)',
            titleColor: '#1f2937',
            bodyColor: '#1f2937',
            borderColor: '#e5e7eb',
            borderWidth: 1,
            cornerRadius: 8,
            displayColors: true,
            callbacks: {
              label: (context) => {
                const value = context.parsed.y;
                return `${context.dataset.label}: ₹${this.formatRevenue(value)}`;
              }
            }
          }
        },
        scales: {
          x: {
            grid: {
              display: false
            },
            border: {
              display: false
            },
            ticks: {
              color: '#6b7280',
              font: {
                size: 12,
                family: 'system-ui, sans-serif'
              }
            }
          },
          y: {
            beginAtZero: true,
            grid: {
              color: 'rgba(229, 231, 235, 0.5)'
            },
            border: {
              display: false
            },
            ticks: {
              color: '#6b7280',
              font: {
                size: 12,
                family: 'system-ui, sans-serif'
              },
              callback: (value) => {
                return '₹' + this.formatRevenue(Number(value));
              }
            }
          }
        },
        interaction: {
          intersect: false,
          mode: 'index'
        },
        elements: {
          line: {
            tension: 0.4
          }
        }
      }
    };
    
    this.chart = new Chart(ctx, config);
    this.chartRetryCount = 0; // Reset retry counter on success
    console.log('Chart created successfully');
    
    } catch (error) {
      console.error('Error creating chart:', error);
    }
  }
  
  onYearRangeChange(): void {
    this.processYearlyData();
    // Use setTimeout to ensure DOM is stable
    setTimeout(() => {
      this.createChart();
    }, 100);
  }
  
  formatRevenue(amount: number): string {
    if (amount >= 10000000) {
      return (amount / 10000000).toFixed(1) + 'Cr';
    } else if (amount >= 100000) {
      return (amount / 100000).toFixed(1) + 'L';
    } else if (amount >= 1000) {
      return (amount / 1000).toFixed(1) + 'K';
    }
    return amount.toFixed(0);
  }
}
