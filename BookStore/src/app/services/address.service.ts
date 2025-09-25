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
   * Add a new address
   */
  addAddress(address: Omit<Address, 'id'>): Observable<Address> {
    const currentUser = this.authService.getCurrentCustomer();
    if (!currentUser) {
      return throwError(() => new Error('User not logged in'));
    }

    // Backend expects AddressDTO and will handle id, userId, timestamps
    // Remove fields that backend will set automatically
    const addressDTO = {
      name: address.name,
      phone: address.phone,
      addressLine1: address.address,  // Map 'address' to 'addressLine1'
      locality: address.locality,
      city: address.city,
      state: address.state,
      country: address.country,       // Include country field
      pincode: address.pincode,
      addressType: address.addressType,
      isDefault: address.isDefault || false,
      instructions: address.instructions,
      accessCode: address.accessCode,
      landmark: address.landmark,
      latitude: address.latitude,
      longitude: address.longitude
    };

    return this.http.post<Address>(this.apiUrl, addressDTO);
  }

  /**
   * Update an existing address
   */
  updateAddress(id: string, address: Address): Observable<Address> {
    // Backend will handle updatedAt timestamp automatically
    // Create clean DTO similar to addAddress method
    const addressDTO = {
      name: address.name,
      phone: address.phone,
      addressLine1: address.address,  // Map 'address' to 'addressLine1'
      locality: address.locality,
      city: address.city,
      state: address.state,
      country: address.country,
      pincode: address.pincode,
      addressType: address.addressType,
      isDefault: address.isDefault || false,
      instructions: address.instructions,
      accessCode: address.accessCode,
      landmark: address.landmark,
      latitude: address.latitude,
      longitude: address.longitude
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
    // Simulate pincode validation
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
    // Simulate address suggestions based on pincode
    const suggestions = [
      { city: 'Sample City', state: 'Sample State' }
    ];
    return of(suggestions);
  }

  /**
   * Geocode address to get coordinates
   */
  geocodeAddress(address: string): Observable<{latitude: number, longitude: number} | null> {
    // Simulate geocoding service
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