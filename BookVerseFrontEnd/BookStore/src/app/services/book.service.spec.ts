import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { BookService } from './book.service';
import { BookModel } from '../models/book.model';

describe('BookService', () => {
  let service: BookService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [BookService]
    });
    service = TestBed.inject(BookService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get all books', () => {
    const mockBooks: BookModel[] = [
      { id: '1', title: 'Book 1', author: 'Author 1', description: 'Desc 1', categories: ['Fiction'], price: 10, stock_display: 5, stock_actual: 5, image_urls: ['img1.jpg'] },
      { id: '2', title: 'Book 2', author: 'Author 2', description: 'Desc 2', categories: ['Non-Fiction'], price: 15, stock_display: 3, stock_actual: 3, image_urls: ['img2.jpg'] }
    ];

    service.getAllBooks().subscribe(books => {
      expect(books).toEqual(mockBooks);
    });

    const req = httpMock.expectOne('http://localhost:3000/books');
    expect(req.request.method).toBe('GET');
    req.flush(mockBooks);
  });

  it('should get book by id', () => {
    const mockBook: BookModel = { id: '1', title: 'Book 1', author: 'Author 1', description: 'Desc 1', categories: ['Fiction'], price: 10, stock_display: 5, stock_actual: 5, image_urls: ['img1.jpg'] };

    service.getBookById('1').subscribe(book => {
      expect(book).toEqual(mockBook);
    });

    const req = httpMock.expectOne('http://localhost:3000/books/1');
    expect(req.request.method).toBe('GET');
    req.flush(mockBook);
  });

  it('should create book', () => {
    const newBook = { title: 'New Book', author: 'New Author', description: 'New Desc', categories: ['Fiction'], price: 20, stock_display: 10, stock_actual: 10, image_urls: ['new.jpg'] };
    const createdBook = { id: '3', ...newBook };

    service.createBook(newBook).subscribe(book => {
      expect(book).toEqual(createdBook);
    });

    const req = httpMock.expectOne('http://localhost:3000/books');
    expect(req.request.method).toBe('POST');
    req.flush(createdBook);
  });

  it('should update book', () => {
    const updateData = { title: 'Updated Book' };
    const updatedBook = { id: '1', title: 'Updated Book', author: 'Author 1', description: 'Desc 1', categories: ['Fiction'], price: 10, stock_display: 5, stock_actual: 5, image_urls: ['img1.jpg'] };

    service.updateBook('1', updateData).subscribe(book => {
      expect(book).toEqual(updatedBook);
    });

    const req = httpMock.expectOne('http://localhost:3000/books/1');
    expect(req.request.method).toBe('PUT');
    req.flush(updatedBook);
  });

  it('should delete book', () => {
    service.deleteBook('1').subscribe(response => {
      expect(response).toBeTruthy();
    });

    const req = httpMock.expectOne('http://localhost:3000/books/1');
    expect(req.request.method).toBe('DELETE');
    req.flush({});
  });
}); 