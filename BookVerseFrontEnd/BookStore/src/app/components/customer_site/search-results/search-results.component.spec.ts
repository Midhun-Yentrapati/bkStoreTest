import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { SearchResultsComponent } from './search-results.component';
import { BookService } from '../../../services/book.service';

describe('SearchResultsComponent', () => {
  let component: SearchResultsComponent;
  let fixture: ComponentFixture<SearchResultsComponent>;

  beforeEach(async () => {
    const mockBookService = jasmine.createSpyObj('BookService', ['searchBooks']);
    mockBookService.searchBooks.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [SearchResultsComponent, HttpClientTestingModule],
      providers: [
        { provide: BookService, useValue: mockBookService },
        {
          provide: ActivatedRoute,
          useValue: {
            queryParams: of({ query: 'test' })
          }
        }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SearchResultsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
