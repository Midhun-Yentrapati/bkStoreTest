import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';

interface ChartData {
  label: string;
  value: number;
  color: string;
}

@Component({
  selector: 'app-modern-pie-chart',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="modern-chart-container">
      <div class="chart-wrapper" (mouseleave)="hideTooltip()">
        <svg [attr.width]="size" [attr.height]="size" class="pie-chart">
          <!-- Background circle -->
          <circle 
            [attr.cx]="center" 
            [attr.cy]="center" 
            [attr.r]="radius" 
            fill="none" 
            stroke="#f1f5f9" 
            stroke-width="2">
          </circle>
          
          <!-- Pie segments -->
          <g *ngFor="let segment of segments">
            <path 
              [attr.d]="segment.pathData" 
              [attr.fill]="segment.color"
              [attr.stroke]="'#ffffff'"
              [attr.stroke-width]="2"
              class="chart-segment"
              [attr.data-label]="segment.label"
              [attr.data-value]="segment.value"
              [attr.data-percentage]="segment.percentage"
              (mouseenter)="showTooltip($event, segment)"
              (mousemove)="updateTooltipPosition($event)">
            </path>
          </g>
          
          <!-- Center circle for donut effect -->
          <circle 
            [attr.cx]="center" 
            [attr.cy]="center" 
            [attr.r]="innerRadius" 
            [attr.fill]="centerColor">
          </circle>
          
          <!-- Center text -->
          <text 
            [attr.x]="center" 
            [attr.y]="center - 10" 
            text-anchor="middle" 
            class="center-title">
            {{ centerTitle }}
          </text>
          <text 
            [attr.x]="center" 
            [attr.y]="center + 10" 
            text-anchor="middle" 
            class="center-value">
            {{ totalValue }}
          </text>
        </svg>
        
        <!-- Hover Tooltip -->
        <div 
          class="chart-tooltip"
          [class.visible]="tooltipVisible"
          [style.left.px]="tooltipX"
          [style.top.px]="tooltipY">
          <div class="tooltip-content">
            <div class="tooltip-label">{{ tooltipData?.label }}</div>
            <div class="tooltip-value">{{ tooltipData?.value }} books ({{ tooltipData?.percentage }}%)</div>
          </div>
        </div>
      </div>
      
      <!-- Legend (optional) -->
      <div class="chart-legend" *ngIf="showLegend">
        <div 
          class="legend-item" 
          *ngFor="let item of chartData; trackBy: trackByLabel">
          <div 
            class="legend-color" 
            [style.background-color]="item.color">
          </div>
          <div class="legend-content">
            <span class="legend-label">{{ item.label }}</span>
            <span class="legend-value">{{ getPercentage(item.value) }}%</span>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .modern-chart-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 1.5rem;
      width: 100%;
      height: 100%;
    }

    .chart-wrapper {
      position: relative;
      display: flex;
      justify-content: center;
      align-items: center;
    }

    .chart-tooltip {
      position: absolute;
      background: rgba(0, 0, 0, 0.9);
      color: white;
      padding: 8px 12px;
      border-radius: 6px;
      font-size: 12px;
      pointer-events: none;
      z-index: 1000;
      opacity: 0;
      transition: opacity 0.2s ease;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      backdrop-filter: blur(4px);
    }

    .chart-tooltip.visible {
      opacity: 1;
    }

    .tooltip-content {
      display: flex;
      flex-direction: column;
      gap: 2px;
    }

    .tooltip-label {
      font-weight: 600;
      font-size: 13px;
    }

    .tooltip-value {
      font-size: 11px;
      opacity: 0.9;
    }

    .pie-chart {
      max-width: 100%;
      height: auto;
    }

    .chart-segment {
      transition: all 0.3s ease;
      cursor: pointer;
    }

    .chart-segment:hover {
      filter: brightness(1.1);
      transform: scale(1.02);
      transform-origin: center;
    }

    .center-title {
      font-size: 14px;
      font-weight: 600;
      fill: #64748b;
    }

    .center-value {
      font-size: 16px;
      font-weight: 700;
      fill: #1e293b;
    }

    .chart-legend {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 0.75rem;
      width: 100%;
      max-width: 400px;
    }

    .legend-item {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.5rem;
      border-radius: 8px;
      transition: background-color 0.2s ease;
    }

    .legend-item:hover {
      background-color: #f8fafc;
    }

    .legend-color {
      width: 16px;
      height: 16px;
      border-radius: 50%;
      flex-shrink: 0;
    }

    .legend-content {
      display: flex;
      flex-direction: column;
      gap: 0.125rem;
      flex: 1;
    }

    .legend-label {
      font-size: 0.875rem;
      font-weight: 500;
      color: #374151;
      line-height: 1.2;
    }

    .legend-value {
      font-size: 0.75rem;
      font-weight: 600;
      color: #6b7280;
    }

    @media (max-width: 768px) {
      .chart-legend {
        grid-template-columns: 1fr;
      }
      
      .legend-item {
        padding: 0.375rem;
      }
    }
  `]
})
export class ModernPieChartComponent implements OnChanges {
  @Input() data: { label: string; value: number }[] = [];
  @Input() size: number = 280;
  @Input() showLegend: boolean = false;
  @Input() centerTitle: string = 'Total';
  @Input() centerColor: string = '#ffffff';
  @Input() colors: string[] = [
    '#ff8a80',  // Light coral
    '#b39ddb',  // Light purple  
    '#81c784',  // Light green
    '#ffb74d',  // Light orange
    '#64b5f6',  // Light blue
    '#f48fb1',  // Light pink
    '#aed581',  // Light lime
    '#ffab91'   // Light deep orange
  ];

  chartData: ChartData[] = [];
  segments: any[] = [];
  center: number = 0;
  radius: number = 0;
  innerRadius: number = 0;
  totalValue: number = 0;

  // Tooltip properties
  tooltipVisible: boolean = false;
  tooltipX: number = 0;
  tooltipY: number = 0;
  tooltipData: any = null;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['data'] || changes['size'] || changes['colors']) {
      this.updateChart();
    }
  }

  private updateChart(): void {
    this.center = this.size / 2;
    this.radius = (this.size / 2) - 20;
    this.innerRadius = this.radius * 0.6;
    
    this.totalValue = this.data.reduce((sum, item) => sum + item.value, 0);
    
    // Create chart data with colors
    this.chartData = this.data.map((item, index) => ({
      label: item.label,
      value: item.value,
      color: this.colors[index % this.colors.length]
    }));

    // Generate path data for segments
    this.segments = this.generateSegments();
  }

  private generateSegments(): any[] {
    if (this.totalValue === 0) return [];

    let currentAngle = -Math.PI / 2; // Start from top
    const segments = [];

    for (const item of this.chartData) {
      const percentage = (item.value / this.totalValue) * 100;
      const segmentAngle = (item.value / this.totalValue) * 2 * Math.PI;
      
      const startAngle = currentAngle;
      const endAngle = currentAngle + segmentAngle;

      const x1 = this.center + this.radius * Math.cos(startAngle);
      const y1 = this.center + this.radius * Math.sin(startAngle);
      const x2 = this.center + this.radius * Math.cos(endAngle);
      const y2 = this.center + this.radius * Math.sin(endAngle);

      const ix1 = this.center + this.innerRadius * Math.cos(startAngle);
      const iy1 = this.center + this.innerRadius * Math.sin(startAngle);
      const ix2 = this.center + this.innerRadius * Math.cos(endAngle);
      const iy2 = this.center + this.innerRadius * Math.sin(endAngle);

      const largeArcFlag = segmentAngle > Math.PI ? 1 : 0;

      const pathData = [
        `M ${ix1} ${iy1}`,
        `L ${x1} ${y1}`,
        `A ${this.radius} ${this.radius} 0 ${largeArcFlag} 1 ${x2} ${y2}`,
        `L ${ix2} ${iy2}`,
        `A ${this.innerRadius} ${this.innerRadius} 0 ${largeArcFlag} 0 ${ix1} ${iy1}`,
        'Z'
      ].join(' ');

      segments.push({
        pathData,
        color: item.color,
        label: item.label,
        value: item.value,
        percentage: percentage.toFixed(1)
      });

      currentAngle = endAngle;
    }

    return segments;
  }

  getPercentage(value: number): string {
    if (this.totalValue === 0) return '0';
    return ((value / this.totalValue) * 100).toFixed(1);
  }

  trackByLabel(index: number, item: ChartData): string {
    return item.label;
  }

  showTooltip(event: MouseEvent, segment: any): void {
    this.tooltipVisible = true;
    this.tooltipData = segment;
    this.updateTooltipPosition(event);
  }

  updateTooltipPosition(event: MouseEvent): void {
    const rect = (event.currentTarget as Element).getBoundingClientRect();
    this.tooltipX = event.clientX - rect.left + 10;
    this.tooltipY = event.clientY - rect.top - 10;
  }

  hideTooltip(): void {
    this.tooltipVisible = false;
    this.tooltipData = null;
  }
}
