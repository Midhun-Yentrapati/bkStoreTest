import { TestBed } from '@angular/core/testing';
import { ChartService } from './chart.service';

describe('ChartService', () => {
  let service: ChartService;

  const mockChartData = {
    labels: ['Jan', 'Feb', 'Mar', 'Apr'],
    datasets: [
      {
        label: 'Sales',
        data: [65, 59, 80, 81],
        backgroundColor: 'rgba(75, 192, 192, 0.2)',
        borderColor: 'rgba(75, 192, 192, 1)',
        borderWidth: 1
      }
    ]
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ChartService]
    });
    service = TestBed.inject(ChartService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getHighlySoldBooks', () => {
    it('should get highly sold books data successfully', () => {
      service.getHighlySoldBooks().subscribe(data => {
        expect(data).toBeTruthy();
        expect(data.labels).toBeDefined();
        expect(data.data).toBeDefined();
      });
    });

    it('should get highly sold books data with custom limit', () => {
      service.getHighlySoldBooks(5).subscribe(data => {
        expect(data).toBeTruthy();
        expect(data.labels.length).toBe(5);
        expect(data.data.length).toBe(5);
      });
    });
  });

  describe('getLeastSoldBooks', () => {
    it('should get least sold books data successfully', () => {
      service.getLeastSoldBooks().subscribe(data => {
        expect(data).toBeTruthy();
        expect(data.labels).toBeDefined();
        expect(data.data).toBeDefined();
      });
    });

    it('should get least sold books data with custom limit', () => {
      service.getLeastSoldBooks(5).subscribe(data => {
        expect(data).toBeTruthy();
        expect(data.labels.length).toBe(5);
        expect(data.data.length).toBe(5);
      });
    });
  });
}); 