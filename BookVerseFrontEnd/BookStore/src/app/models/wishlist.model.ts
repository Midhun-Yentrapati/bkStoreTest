export interface WishlistItem {
  id: string;
  userId: string;
  bookId: string;
  addedAt: string;
}

export interface WishlistItemWithDetails extends WishlistItem {
  book: {
    id: string;
    title: string;
    author: string;
    price: number;
    image_urls: string[];
    description: string;
    stock_display: number;
  };
}
