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
  addressType: 'HOME' | 'WORK' | 'OTHER';
  isDefault?: boolean;
  isActive?: boolean;
  instructions?: string;  // Added: Delivery instructions
  accessCode?: string;    // Added: Access code for delivery
  latitude?: number;      // Added: GPS latitude coordinate
  longitude?: number;     // Added: GPS longitude coordinate
  coordinates?: {
    latitude: number;
    longitude: number;
  };
  createdAt?: Date;
  updatedAt?: Date;
  userId?: string;
}