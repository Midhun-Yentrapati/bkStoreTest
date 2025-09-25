import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AddressService } from '../../../services/address.service';
import { Address } from '../../../models/address.model';
import { NotificationService } from '../../../services/notification.service';

@Component({
  selector: 'app-address-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './address-management.component.html',
  styleUrl: './address-management.component.css'
})
export class AddressManagementComponent implements OnInit {

  address: Address = {
    name: '',
    phone: '',
    alternatePhone: '',
    pincode: '',
    address: '',
    locality: '',
    city: '',
    state: '',
    landmark: '',
    addressType: 'HOME'
  };

  isEditing: boolean = false;
  editId: string | null = null;
  isLoading: boolean = false;
  isValidatingPincode: boolean = false;
  pincodeValid: boolean | null = null;
  
  formErrors: { [key: string]: string } = {};
  
  states: string[] = [
    'Andhra Pradesh', 'Arunachal Pradesh', 'Assam', 'Bihar', 'Chhattisgarh',
    'Goa', 'Gujarat', 'Haryana', 'Himachal Pradesh', 'Jharkhand',
    'Karnataka', 'Kerala', 'Madhya Pradesh', 'Maharashtra', 'Manipur',
    'Meghalaya', 'Mizoram', 'Nagaland', 'Odisha', 'Punjab',
    'Rajasthan', 'Sikkim', 'Tamil Nadu', 'Telangana', 'Tripura',
    'Uttar Pradesh', 'Uttarakhand', 'West Bengal'
  ];

  addressTypes = [
    { value: 'Home', label: 'Home', description: 'All day delivery' },
    { value: 'Work', label: 'Work', description: 'Delivery between 10 AM - 5 PM' },
    { value: 'Other', label: 'Other', description: 'Custom delivery time' }
  ];

  constructor(
    private addressService: AddressService,
    private router: Router,
    private route: ActivatedRoute,
    private notificationService: NotificationService
  ) { }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['edit']) {
        this.isEditing = true;
        this.editId = params['edit'];
        if (this.editId) {
          this.loadAddressForEdit(this.editId);
        }
      }
    });
  }

  loadAddressForEdit(id: string): void {
    this.isLoading = true;
    this.addressService.getAddressById(id).subscribe({
      next: (address) => {
        this.address = { ...address };
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading address:', error);
        this.notificationService.error('Error', 'Error loading address details');
        this.isLoading = false;
      }
    });
  }

  useCurrentLocation(): void {
    if (navigator.geolocation) {
      this.isLoading = true;
      navigator.geolocation.getCurrentPosition(
        (position) => {
          // In a real app, you would reverse geocode the coordinates
          this.notificationService.info('Location Detected', 'Please fill in the address details manually.');
          this.isLoading = false;
        },
        (error) => {
          this.notificationService.warning('Location Error', 'Unable to get your location. Please fill in the address manually.');
          this.isLoading = false;
        }
      );
    } else {
      this.notificationService.error('Geolocation Error', 'Geolocation is not supported by this browser.');
    }
  }

  onPincodeChange(): void {
    const pincode = this.address.pincode;
    if (pincode && pincode.length === 6) {
      this.validatePincode(pincode);
    } else {
      this.pincodeValid = null;
    }
  }

  validatePincode(pincode: string): void {
    this.isValidatingPincode = true;
    this.addressService.validatePincode(pincode).subscribe({
      next: (result) => {
        this.pincodeValid = result.isValid;
        this.isValidatingPincode = false;
        
        if (result.isValid) {
          // Get address suggestions
          this.addressService.getAddressSuggestions(pincode).subscribe({
            next: (suggestions) => {
              if (suggestions.length > 0) {
                this.address.city = suggestions[0].city;
                this.address.state = suggestions[0].state;
              }
            }
          });
        }
      },
      error: () => {
        this.isValidatingPincode = false;
        this.pincodeValid = false;
      }
    });
  }

  saveAddress(): void {
    if (!this.validateForm()) {
      return;
    }

    this.isLoading = true;
    this.clearFormErrors();

    if (this.isEditing && this.editId) {
      this.addressService.updateAddress(this.editId, this.address).subscribe({
        next: () => {
          this.notificationService.success('Success', 'Address updated successfully!');
          this.router.navigate(['/checkout']);
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error updating address:', error);
          this.notificationService.error('Update Failed', 'Failed to update address. Please try again.');
          this.isLoading = false;
        }
      });
    } else {
      this.addressService.addAddress(this.address).subscribe({
        next: () => {
          this.notificationService.success('Success', 'Address added successfully!');
          this.router.navigate(['/checkout']);
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error adding address:', error);
          this.notificationService.error('Add Failed', 'Failed to add address. Please try again.');
          this.isLoading = false;
        }
      });
    }
  }

  validateForm(): boolean {
    this.clearFormErrors();
    let isValid = true;

    if (!this.address.name.trim()) {
      this.formErrors['name'] = 'Please enter your name';
      isValid = false;
    }

    if (!this.address.phone.trim()) {
      this.formErrors['phone'] = 'Please enter your phone number';
      isValid = false;
    } else if (!/^\d{10}$/.test(this.address.phone)) {
      this.formErrors['phone'] = 'Please enter a valid 10-digit phone number';
      isValid = false;
    }

    if (this.address.alternatePhone && !/^\d{10}$/.test(this.address.alternatePhone)) {
      this.formErrors['alternatePhone'] = 'Please enter a valid 10-digit alternate phone number';
      isValid = false;
    }

    if (!this.address.pincode.trim()) {
      this.formErrors['pincode'] = 'Please enter your pincode';
      isValid = false;
    } else if (!/^\d{6}$/.test(this.address.pincode)) {
      this.formErrors['pincode'] = 'Please enter a valid 6-digit pincode';
      isValid = false;
    }

    if (!this.address.address.trim()) {
      this.formErrors['address'] = 'Please enter your address';
      isValid = false;
    }

    if (!this.address.locality.trim()) {
      this.formErrors['locality'] = 'Please enter your locality';
      isValid = false;
    }

    if (!this.address.city.trim()) {
      this.formErrors['city'] = 'Please enter your city';
      isValid = false;
    }

    if (!this.address.state.trim()) {
      this.formErrors['state'] = 'Please select your state';
      isValid = false;
    }

    return isValid;
  }

  clearFormErrors(): void {
    this.formErrors = {};
  }

  hasError(field: string): boolean {
    return !!this.formErrors[field];
  }

  getError(field: string): string {
    return this.formErrors[field] || '';
  }

  cancel(): void {
    this.router.navigate(['/checkout']);
  }
}
