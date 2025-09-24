export interface Address {
  id?: string;
  name: string;
  phone: string;
  alternatePhone?: string;
  email?: string;
  pincode: string;
  address: string;
  locality: string;
  city: string;
  state: string;
  country?: string;
  landmark?: string;
  addressType: 'Home' | 'Work' | 'Other';
  isDefault?: boolean;
  isActive?: boolean;
  coordinates?: {
    latitude: number;
    longitude: number;
  };
  createdAt?: Date;
  updatedAt?: Date;
  userId?: string;
} 