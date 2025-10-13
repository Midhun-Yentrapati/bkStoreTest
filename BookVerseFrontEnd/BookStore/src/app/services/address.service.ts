import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { Address } from '../models/address.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class AddressService {
  // API Gateway URL
  private apiBaseUrl = 'http://localhost:8090/api';
  // Base URL for most user address operations
  private apiUrl = `${this.apiBaseUrl}/users/addresses`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) { }

  /**
   * Get all addresses for current user
   */
  getAddresses(): Observable<Address[]> {
    // Backend gets userId from JWT token
    return this.http.get<Address[]>(this.apiUrl);
  }

  /**
   * Get a specific address by ID for the currently logged-in user
   
  getAddressById(id: string): Observable<Address> {
    return this.http.get<Address>(`${this.apiUrl}/${id}`);
  }
    */
  getAddressById(id: string): Observable<Address> {
    // CORRECTED URL: The path is /api/users/address/{id} for a single address,
    // which is handled by the getAddressByIdForAdmin method's URL construction.
    // We will reuse that logic here for consistency.
    const singleAddressUrl = `${this.apiBaseUrl}/users/address/${id}`;
    return this.http.get<Address>(singleAddressUrl);
  }

  // =================================================================
  // == THE FIX IS IN THIS METHOD
  // =================================================================
  /**
   * Get a specific address by ID for admin users.
   * This points to the correct admin-accessible endpoint.
   */
  getAddressByIdForAdmin(id: string): Observable<Address> {
    // CORRECTED URL: The path is /api/users/address/{id}, not /api/addresses/{id}
    const adminAddressUrl = `${this.apiBaseUrl}/users/address/${id}`;
    return this.http.get<Address>(adminAddressUrl);
  }
  // =================================================================
  // == END OF FIX
  // =================================================================

  /**
   * Add a new address
   */
  addAddress(address: Omit<Address, 'id'>): Observable<Address> {
    const currentUser = this.authService.getCurrentCustomer();
    if (!currentUser) {
      return throwError(() => new Error('User not logged in'));
    }

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
      isActive: true,
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
      isActive: address.isActive !== false,
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

  // --- Helper/Simulation Methods (can be removed if not used) ---

  validatePincode(pincode: string): Observable<{isValid: boolean, location?: string}> {
    const isValid = /^\d{6}$/.test(pincode);
    return of({
      isValid,
      location: isValid ? 'Serviceable Area' : undefined
    });
  }

  getAddressSuggestions(pincode: string): Observable<{city: string, state: string}[]> {
    const suggestions = [
      { city: 'Sample City', state: 'Sample State' }
    ];
    return of(suggestions);
  }

  geocodeAddress(address: string): Observable<{latitude: number, longitude: number} | null> {
    return of({
      latitude: 12.9716,
      longitude: 77.5946
    });
  }

  getAddressesByType(type: Address['addressType']): Observable<Address[]> {
    return new Observable(observer => {
      this.getAddresses().subscribe(addresses => {
        const filteredAddresses = addresses.filter(addr => addr.addressType === type);
        observer.next(filteredAddresses);
        observer.complete();
      });
    });
  }

  searchAddresses(query: string): Observable<Address[]> {
    const userId = this.authService.getCurrentCustomer()?.id;
    const searchParams = `?q=${encodeURIComponent(query)}${userId ? `&userId=${userId}` : ''}`;
    return this.http.get<Address[]>(`${this.apiUrl}/search${searchParams}`);
  }
}