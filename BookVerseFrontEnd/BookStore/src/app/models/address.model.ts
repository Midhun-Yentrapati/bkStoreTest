export interface Address {
  id?: string;
  name: string;
  phone: string;
  alternatePhone?: string;
  email?: string;
  pincode: string;
  addressLine1: string;  // Changed from 'address' to match backend
  addressLine2?: string; // Added to match backend
  locality?: string;     // Made optional as backend doesn't require it
  city: string;
  state: string;
  country: string;       // Made required to match backend
  landmark?: string;
  addressType: 'HOME' | 'WORK' | 'OTHER';
  isDefault?: boolean;
  isActive?: boolean;
  instructions?: string;
  accessCode?: string;
  latitude?: number;
  longitude?: number;
  coordinates?: {        // Keep for backward compatibility
    latitude: number;
    longitude: number;
  };
  createdAt?: Date;
  updatedAt?: Date;
  userId?: string;
  // Computed fields from backend
  fullAddress?: string;
  shortAddress?: string;
  hasCoordinates?: boolean;
}