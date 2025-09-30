import { BookModel } from './book.model';

export interface CartItem {
  id: string;
  userId: string;
  bookId: string;
  quantity: number;
  priceWhenAdded: number; // BigDecimal from backend
  createdAt: string; // LocalDateTime from backend
  updatedAt: string; // LocalDateTime from backend
}

export interface CartItemDto {
  id?: string;
  userId: string;
  bookId: string;
  quantity: number;
  priceWhenAdded: number;
}

export interface CartItemWithDetails extends CartItem {
  book: BookModel; // Full book details fetched separately
  subtotal: number;
}
