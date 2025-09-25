import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HorizontalBookSectionComponent } from './horizontal-book-section.component';

describe('HorizontalBookSectionComponent', () => {
  let component: HorizontalBookSectionComponent;
  let fixture: ComponentFixture<HorizontalBookSectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HorizontalBookSectionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HorizontalBookSectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
