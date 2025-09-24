import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HighlySoldComponent } from './highly-sold.component';
import { BookService } from '../../../services/book.service';

import { of } from 'rxjs';
class MockBookService {
  getHighlySoldBooks = jasmine.createSpy().and.returnValue(of([]));
}

describe('HighlySoldComponent', () => {
  let component: HighlySoldComponent;
  let fixture: ComponentFixture<HighlySoldComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HighlySoldComponent],
      providers: [{ provide: BookService, useClass: MockBookService }]
    }).compileComponents();
    fixture = TestBed.createComponent(HighlySoldComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
