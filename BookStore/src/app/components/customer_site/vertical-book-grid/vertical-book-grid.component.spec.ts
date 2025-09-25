import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VerticalBookGridComponent } from './vertical-book-grid.component';

describe('VerticalBookGridComponent', () => {
  let component: VerticalBookGridComponent;
  let fixture: ComponentFixture<VerticalBookGridComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VerticalBookGridComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VerticalBookGridComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
