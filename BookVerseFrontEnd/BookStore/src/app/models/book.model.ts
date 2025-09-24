import { CategoryModel } from './category.model';

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
    id: string;
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
    salesCategory: string;
    isActive: boolean;
    isFeatured: boolean;
    lastSoldAt?: string;
    createdAt?: string;
    updatedAt?: string;
    categories: CategoryModel[];
    images: BookImageModel[];
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

export interface BookCategoryData {
    id: string;
    category: string;
    no_of_books_sold: number;
}

export interface BookWithSales extends BookModel {
    no_of_books_sold: number;
}

export interface ChartData {
    labels: string[];
    data: number[];
}

export interface BookData {
    id: string;
    category: string;
    no_of_books_sold: number;
}