export interface CartItem {
  id: string;
  userId: string;
  bookId: string;
  quantity: number;
  addedAt: string;
  updatedAt: string;
}

export interface CartItemWithDetails extends CartItem {
  book: {
    id: string;
    title: string;
    author: string;
    price: number;
    image_urls: string[];
    stock_display: number;
  };
  subtotal: number;
}
