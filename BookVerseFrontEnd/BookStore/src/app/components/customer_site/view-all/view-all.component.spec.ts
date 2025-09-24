import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ViewAllComponent } from './view-all.component';
import { BookService } from '../../../services/book.service';

import { of } from 'rxjs';
class MockBookService {
  getAllBooks = jasmine.createSpy().and.returnValue(of([]));
}

describe('ViewAllComponent', () => {
  let component: ViewAllComponent;
  let fixture: ComponentFixture<ViewAllComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ViewAllComponent],
      providers: [{ provide: BookService, useClass: MockBookService }]
    }).compileComponents();
    fixture = TestBed.createComponent(ViewAllComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
