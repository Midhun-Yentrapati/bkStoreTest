export interface CustomerRating {
  id?: number;
  userId: string;
  userName: string;
  rating: number;
  review?: string;
  comment?: string; // Backend uses 'comment' instead of 'review'
  createdAt: string;
  isVerifiedPurchase?: boolean;
  status?: string;
  bookId?: number;
  bookTitle?: string;
}

export interface BookModel {
    id: number | string;
    isbn?: string;
    title: string;
    author: string;
    description: string;
    language?: string;
    format?: string;
    edition?: string;
    publisher?: string;
    publicationDate?: string;
    pages?: number;
    weight?: number;
    dimensions?: string;
    price: number;
    mrp?: number;
    stockDisplay: number;
    stockActual: number;
    noOfBooksSold?: number;
    totalRevenue?: number;
    averageRating?: number;
    reviewCount?: number;
    salesCategory?: 'BEST_SELLING' | 'NEWLY_LAUNCHED' | 'FEATURED' | 'SPECIAL_OFFERS';
    isActive?: boolean;
    isFeatured?: boolean;
    lastSoldAt?: string;
    createdAt?: string;
    updatedAt?: string;
    categories?: CategoryInfo[];
    images?: BookImageInfo[];
    // Legacy properties for backward compatibility
    stock_display: number;
    stock_actual: number;
    image_urls?: string[];
    customerRatings?: CustomerRating[];
}

export interface BookImageModel {
  id?: string;
  imageUrl: string;
  isPrimary: boolean;
  altText?: string;
  displayOrder: number;
}

export interface BookCreateRequest {
  isbn?: string;
  title: string;
  author: string;
  description: string;
  language?: string;
  format?: string;
  edition?: string;
  publisher?: string;
  publicationDate?: string; // Will be converted to LocalDateTime by backend
  pages?: number;
  weight?: number;
  dimensions?: string;
  price: number;
  mrp?: number;
  stockDisplay: number;
  stockActual: number;
  salesCategory: 'BEST_SELLING' | 'SPECIAL_OFFERS' | 'NEWLY_LAUNCHED'; // Backend enum values
  isActive: boolean;
  isFeatured: boolean;
  categoryIds: number[]; // Will be converted to Long[] by backend
  images: BookImageRequest[];
}

export interface BookImageRequest {
  imageUrl: string;
  isPrimary: boolean;
  altText?: string;
  displayOrder: number;
}
export interface CategoryInfo {
    id: number;
    name: string;
    slug?: string;
    description?: string;
    image?: string;
    isActive?: boolean;
    priority?: number;
}

export interface BookImageInfo {
    id: number;
    imageUrl: string;
    altText?: string;
    isPrimary?: boolean;
}

export interface BookCategoryData {
    id: string;
    category: 'newly launched' | 'highly rated' | 'special offers';
    no_of_books_sold: number;
}

export interface BookWithSales extends BookModel {
    no_of_books_sold: number;
}

// Utility functions for safe property access
export class BookUtils {
  static getFirstImageUrl(book: BookModel): string {
    return book.image_urls?.[0] || book.images?.[0]?.imageUrl || 'https://placehold.co/150x200?text=No+Image';
  }

  static getCategoryNames(book: BookModel): string[] {
    if (!book.categories) return [];
    return book.categories.map(cat => 
      typeof cat === 'string' ? cat : cat.name
    );
  }

  static getBookIdAsString(book: BookModel): string {
    return typeof book.id === 'number' ? book.id.toString() : book.id;
  }

  static hasMultipleImages(book: BookModel): boolean {
    return (book.image_urls?.length || 0) > 1 || (book.images?.length || 0) > 1;
  }
}
