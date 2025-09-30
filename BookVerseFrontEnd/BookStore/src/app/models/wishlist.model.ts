import { BookModel } from './book.model';

export interface WishlistItem {
  id: string;
  userId: string;
  bookId: string;
  priceWhenAdded?: number;
  notifyOnSale?: boolean;
  addedAt: string; // LocalDateTime from backend
}

export interface WishlistItemDto {
  id?: string;
  userId: string;
  bookId: string;
  priceWhenAdded?: number;
  notifyOnSale?: boolean;
}

export interface WishlistItemWithDetails extends WishlistItem {
  book: BookModel; // Full book details fetched separately
}
