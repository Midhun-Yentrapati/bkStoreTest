import { CategoryModel } from './category.model';

describe('CategoryModel', () => {
  let mockCategory: CategoryModel;

  beforeEach(() => {
    mockCategory = {
      id: 'cat1',
      name: 'Fiction'
    };
  });

  it('should create a valid CategoryModel instance', () => {
    expect(mockCategory).toBeTruthy();
    expect(mockCategory.id).toBe('cat1');
    expect(mockCategory.name).toBe('Fiction');
  });

  it('should have all required properties', () => {
    expect(mockCategory.id).toBeDefined();
    expect(mockCategory.name).toBeDefined();
  });

  it('should have correct data types', () => {
    expect(typeof mockCategory.id).toBe('string');
    expect(typeof mockCategory.name).toBe('string');
  });

  it('should work with different category names', () => {
    const categories = [
      { id: 'cat1', name: 'Fiction' },
      { id: 'cat2', name: 'Non-Fiction' },
      { id: 'cat3', name: 'Science Fiction' },
      { id: 'cat4', name: 'Mystery' },
      { id: 'cat5', name: 'Romance' }
    ];

    categories.forEach(category => {
      const testCategory: CategoryModel = category;
      expect(testCategory.id).toBe(category.id);
      expect(testCategory.name).toBe(category.name);
    });
  });

  it('should handle empty strings', () => {
    const emptyCategory: CategoryModel = {
      id: 'empty',
      name: ''
    };

    expect(emptyCategory.id).toBe('empty');
    expect(emptyCategory.name).toBe('');
  });

  it('should handle special characters in names', () => {
    const specialCategory: CategoryModel = {
      id: 'special',
      name: 'Science & Technology'
    };

    expect(specialCategory.name).toBe('Science & Technology');
  });
}); 