import { BookModel, BookCategoryData, BookWithSales } from './book.model';

describe('BookModel', () => {
  let mockBook: BookModel;

  beforeEach(() => {
    mockBook = {
      id: '1',
      title: 'Test Book',
      author: 'Test Author',
      description: 'Test Description',
      categories: ['Fiction', 'Adventure'],
      price: 29.99,
      stock_display: 10,
      stock_actual: 8,
      image_urls: ['image1.jpg', 'image2.jpg']
    };
  });

  it('should create a valid BookModel instance', () => {
    expect(mockBook).toBeTruthy();
    expect(mockBook.id).toBe('1');
    expect(mockBook.title).toBe('Test Book');
    expect(mockBook.author).toBe('Test Author');
  });

  it('should have all required properties', () => {
    expect(mockBook.id).toBeDefined();
    expect(mockBook.title).toBeDefined();
    expect(mockBook.author).toBeDefined();
    expect(mockBook.description).toBeDefined();
    expect(mockBook.categories).toBeDefined();
    expect(mockBook.price).toBeDefined();
    expect(mockBook.stock_display).toBeDefined();
    expect(mockBook.stock_actual).toBeDefined();
    expect(mockBook.image_urls).toBeDefined();
  });

  it('should have correct data types', () => {
    expect(typeof mockBook.id).toBe('string');
    expect(typeof mockBook.title).toBe('string');
    expect(typeof mockBook.author).toBe('string');
    expect(typeof mockBook.description).toBe('string');
    expect(Array.isArray(mockBook.categories)).toBe(true);
    expect(typeof mockBook.price).toBe('number');
    expect(typeof mockBook.stock_display).toBe('number');
    expect(typeof mockBook.stock_actual).toBe('number');
    expect(Array.isArray(mockBook.image_urls)).toBe(true);
  });
});

describe('BookCategoryData', () => {
  let mockCategoryData: BookCategoryData;

  beforeEach(() => {
    mockCategoryData = {
      id: '1',
      category: 'newly launched',
      no_of_books_sold: 150
    };
  });

  it('should create a valid BookCategoryData instance', () => {
    expect(mockCategoryData).toBeTruthy();
    expect(mockCategoryData.id).toBe('1');
    expect(mockCategoryData.category).toBe('newly launched');
  });

  it('should have valid category values', () => {
    const validCategories = ['newly launched', 'highly rated', 'special offers'];
    expect(validCategories).toContain(mockCategoryData.category);
  });

  it('should have correct data types', () => {
    expect(typeof mockCategoryData.id).toBe('string');
    expect(typeof mockCategoryData.category).toBe('string');
    expect(typeof mockCategoryData.no_of_books_sold).toBe('number');
  });
});

describe('BookWithSales', () => {
  let mockBookWithSales: BookWithSales;

  beforeEach(() => {
    mockBookWithSales = {
      id: '1',
      title: 'Test Book',
      author: 'Test Author',
      description: 'Test Description',
      categories: ['Fiction'],
      price: 29.99,
      stock_display: 10,
      stock_actual: 8,
      image_urls: ['image1.jpg'],
      no_of_books_sold: 50
    };
  });

  it('should extend BookModel with sales data', () => {
    expect(mockBookWithSales.id).toBe('1');
    expect(mockBookWithSales.title).toBe('Test Book');
    expect(mockBookWithSales.no_of_books_sold).toBe(50);
  });

  it('should have all BookModel properties plus sales', () => {
    expect(mockBookWithSales.id).toBeDefined();
    expect(mockBookWithSales.title).toBeDefined();
    expect(mockBookWithSales.no_of_books_sold).toBeDefined();
  });
}); 