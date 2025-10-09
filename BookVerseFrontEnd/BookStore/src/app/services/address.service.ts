import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { Address } from '../models/address.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class AddressService {
  // Update API URL to point to Spring Boot API Gateway
  private apiBaseUrl = 'http://localhost:8090/api'; // API Gateway URL
  private apiUrl = `${this.apiBaseUrl}/users/addresses`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) { }

  /**
   * Get all addresses for current user
   */
  getAddresses(): Observable<Address[]> {
    // Backend gets userId from JWT token, no need to pass it as parameter
    return this.http.get<Address[]>(this.apiUrl);
  }

  /**
   * Get a specific address by ID
   */
  getAddressById(id: string): Observable<Address> {
    return this.http.get<Address>(`${this.apiUrl}/${id}`);
  }

  /**
   * Get a specific address by ID for admin users
   * Uses the admin endpoint that can access any user's address
   */
  getAddressByIdForAdmin(id: string): Observable<Address> {
    return this.http.get<Address>(`${this.apiBaseUrl}/addresses/${id}`);
  }

  /**
   * Add a new address
   */
  addAddress(address: Omit<Address, 'id'>): Observable<Address> {
    const currentUser = this.authService.getCurrentCustomer();
    if (!currentUser) {
      return throwError(() => new Error('User not logged in'));
    }

    // Create AddressDTO matching backend expectations
    const addressDTO = {
      name: address.name,
      phone: address.phone,
      alternatePhone: address.alternatePhone || null,
      email: address.email || null,
      pincode: address.pincode,
      addressLine1: address.addressLine1,
      addressLine2: address.addressLine2 || null,
      city: address.city,
      state: address.state,
      country: address.country || 'India', // Default to India if not provided
      landmark: address.landmark || null,
      addressType: address.addressType,
      isDefault: address.isDefault || false,
      isActive: true, // Always set as active for new addresses
      instructions: address.instructions || null,
      accessCode: address.accessCode || null,
      latitude: address.latitude || null,
      longitude: address.longitude || null
    };

    return this.http.post<Address>(this.apiUrl, addressDTO);
  }

  /**
   * Update an existing address
   */
  updateAddress(id: string, address: Address): Observable<Address> {
    // Create AddressDTO matching backend expectations
    const addressDTO = {
      name: address.name,
      phone: address.phone,
      alternatePhone: address.alternatePhone || null,
      email: address.email || null,
      pincode: address.pincode,
      addressLine1: address.addressLine1,
      addressLine2: address.addressLine2 || null,
      city: address.city,
      state: address.state,
      country: address.country || 'India',
      landmark: address.landmark || null,
      addressType: address.addressType,
      isDefault: address.isDefault || false,
      isActive: address.isActive !== false, // Keep active unless explicitly set to false
      instructions: address.instructions || null,
      accessCode: address.accessCode || null,
      latitude: address.latitude || null,
      longitude: address.longitude || null
    };
    
    return this.http.put<Address>(`${this.apiUrl}/${id}`, addressDTO);
  }

  /**
   * Delete an address
   */
  deleteAddress(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Permanently delete an address
   */
  permanentDeleteAddress(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Set an address as default
   */
  setDefaultAddress(id: string): Observable<Address> {
    return this.http.put<Address>(`${this.apiUrl}/${id}/default`, {});
  }

  /**
   * Get the default address
   */
  getDefaultAddress(): Observable<Address | null> {
    return this.http.get<Address>(`${this.apiUrl}/default`);
  }

  /**
   * Validate pincode
   */
  validatePincode(pincode: string): Observable<{isValid: boolean, location?: string}> {
    // Simulate pincode validation - In production, use a real API
    const isValid = /^\d{6}$/.test(pincode);
    return of({
      isValid,
      location: isValid ? 'Serviceable Area' : undefined
    });
  }

  /**
   * Get address suggestions based on pincode
   */
  getAddressSuggestions(pincode: string): Observable<{city: string, state: string}[]> {
    // Simulate address suggestions based on pincode - In production, use a real API
    const suggestions = [
      { city: 'Sample City', state: 'Sample State' }
    ];
    return of(suggestions);
  }

  /**
   * Geocode address to get coordinates
   */
  geocodeAddress(address: string): Observable<{latitude: number, longitude: number} | null> {
    // Simulate geocoding service - In production, use Google Maps or similar
    return of({
      latitude: 12.9716,
      longitude: 77.5946
    });
  }

  /**
   * Get addresses by type
   */
  getAddressesByType(type: Address['addressType']): Observable<Address[]> {
    return new Observable(observer => {
      this.getAddresses().subscribe(addresses => {
        const filteredAddresses = addresses.filter(addr => addr.addressType === type);
        observer.next(filteredAddresses);
        observer.complete();
      });
    });
  }

  /**
   * Search addresses
   */
  searchAddresses(query: string): Observable<Address[]> {
    const userId = this.authService.getCurrentCustomer()?.id;
    const searchParams = `?q=${encodeURIComponent(query)}${userId ? `&userId=${userId}` : ''}`;
    return this.http.get<Address[]>(`${this.apiUrl}/search${searchParams}`);
  }
} 