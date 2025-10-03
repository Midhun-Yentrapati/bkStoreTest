export interface Book{
    id: string;
    title: string;
    author: string;
    description: string;
    categories: string[];
    sales_category: string;
    price: number;
    stock_display: number;
    stock_actual: number;
    image_urls: string[];
}
export interface BookIdResponse {
  id: string;
}
export interface BookData{
  id : string;
  category : string;
  no_of_books_sold : number;
}

export interface ChartData{
  labels : string[];
  data : number[];
}
export interface Category {
  id: string;
  name: string;
}

export interface User {
  id: string;
  username: string;
  email: string;
  passwordHash: string; // Although not displayed, it's part of the data
}

export interface AdminUser {
  id: string;
  username: string;
  email: string;
  fullName: string;
  userType: string;
  userRole: string;
  employeeId?: string;
  department?: string;
  isActive: boolean;
  createdAt?: string;
  lastUpdated?: string;
}