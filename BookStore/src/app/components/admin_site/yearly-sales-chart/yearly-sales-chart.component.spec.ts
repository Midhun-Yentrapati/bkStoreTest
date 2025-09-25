import { ComponentFixture, TestBed } from '@angular/core/testing';

import { YearlySalesChartComponent } from './yearly-sales-chart.component';

describe('YearlySalesChartComponent', () => {
  let component: YearlySalesChartComponent;
  let fixture: ComponentFixture<YearlySalesChartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [YearlySalesChartComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(YearlySalesChartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
