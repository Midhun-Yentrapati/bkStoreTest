import { Component, OnInit, computed, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormArray } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { AddressService } from '../../services/address.service';
import { UserModel } from '../../models/user.model';
import { Address } from '../../models/address.model';
import { Subject, takeUntil, map, catchError, of } from 'rxjs';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit, OnDestroy {
  isEditMode = false;
  profileForm: FormGroup;
  // Get current user information
  currentUser = computed(() => this.authService.getCurrentCustomer());
  isSubmitting = false;
  successMessage = '';
  errorMessage = '';
  showAddressForm = false;
  isEditingAddress = false;
  editingAddressId: string | null = null;
  selectedMapCoordinates: { latitude: number; longitude: number } | null = null;

  // Addresses from database
  addresses: Address[] = [];
  isLoadingAddresses = false;

  newAddressForm: FormGroup;
  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private addressService: AddressService
  ) {
    this.profileForm = this.createProfileForm();
    this.newAddressForm = this.fb.group({
      name: ['', Validators.required],
      phone: ['', [Validators.required, Validators.pattern(/^\+?[1-9]\d{1,14}$/)]],
      alternatePhone: [''],
      pincode: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]],
      address: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(200)]],
      locality: ['', Validators.required],
      city: ['', Validators.required],
      state: ['', Validators.required],
      country: ['India', Validators.required],
      landmark: [''],
      addressType: ['HOME', Validators.required],
      isDefault: [false]
    });
  }

  ngOnInit() {
    this.populateForm();
    this.loadAddresses();
    
    // Fetch complete user profile data when component loads
    this.fetchCompleteUserProfile();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private createProfileForm(): FormGroup {
    return this.fb.group({
      fullName: ['', [Validators.required, Validators.minLength(2)]],
      username: ['', [Validators.required, Validators.minLength(4)]],
      email: ['', [Validators.required, Validators.email]],
      mobileNumber: ['', [Validators.pattern('^[0-9]{10}$')]],
      dateOfBirth: [''],
      bio: ['', [Validators.maxLength(500)]],
      profilePicture: ['']
    });
  }

  private populateForm() {
    const user = this.currentUser();
    console.log('🔍 Profile Component - Current user data:', user);
    
    if (user) {
      console.log('🔍 Profile Component - DOB and Bio values:', {
        dateOfBirth: user.dateOfBirth,
        bio: user.bio
      });
      
      const formValues = {
        fullName: user.fullName || '',
        username: user.username || '',
        email: user.email || '',
        mobileNumber: user.mobileNumber || '',
        profilePicture: user.profilePicture || '',
        dateOfBirth: user.dateOfBirth || '',
        bio: user.bio || ''
      };
      
      console.log('🔍 Profile Component - Form values being set:', formValues);
      
      this.profileForm.patchValue(formValues);
      
      console.log('🔍 Profile Component - Form values after patch:', this.profileForm.value);
    }
  }

  private loadAddresses() {
    this.isLoadingAddresses = true;
    this.addressService.getAddresses()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (addresses) => {
          this.addresses = addresses.filter(addr => addr.isActive !== false);
          this.isLoadingAddresses = false;
        },
        error: (error) => {
          console.error('Error loading addresses:', error);
          this.errorMessage = 'Failed to load addresses. Please try again.';
          this.isLoadingAddresses = false;
        }
      });
  }

  private loadStates() {
    // This method is not implemented in the provided code
  }

  private fetchCompleteUserProfile() {
    this.authService.fetchCompleteUserProfile().subscribe({
      next: (completeUser) => {
        console.log('✅ Profile component loaded with complete user data:', completeUser);
        // Form will be populated automatically due to signal reactivity
        setTimeout(() => {
          this.populateForm();
        }, 100);
      },
      error: (error) => {
        console.warn('⚠️ Failed to fetch complete profile, using existing data:', error);
        // Fallback to existing data
        this.populateForm();
      }
    });
  }

  toggleEditMode() {
    this.isEditMode = !this.isEditMode;
    if (!this.isEditMode) {
      // If canceling edit, reset form
      this.populateForm();
      this.clearMessages();
    }
  }

  onSubmit() {
    if (this.profileForm.valid && !this.isSubmitting) {
      this.isSubmitting = true;
      this.clearMessages();

      // Use AuthService to update profile
      this.authService.updateProfile(this.profileForm.value).subscribe({
        next: (updatedUser) => {
          this.successMessage = 'Profile updated successfully!';
          this.isSubmitting = false;
          this.isEditMode = false;
          
          // Wait for the AuthService signal to update, then refresh the form
          setTimeout(() => {
            this.populateForm();
          }, 0);
          
          console.log('Profile updated:', updatedUser);
        },
        error: (err) => {
          this.errorMessage = err.message || 'Failed to update profile. Please try again.';
          this.isSubmitting = false;
        }
      });
    } else {
      this.profileForm.markAllAsTouched();
    }
  }

  // Address Management
  toggleAddressForm() {
    this.showAddressForm = !this.showAddressForm;
    if (!this.showAddressForm) {
      this.resetAddressForm();
    }
  }

  editAddress(address: Address) {
    this.isEditingAddress = true;
    this.editingAddressId = address.id || null;
    this.showAddressForm = true;
    
    // Populate form with address data
    this.newAddressForm.patchValue({
      name: address.name,
      phone: address.phone,
      alternatePhone: address.alternatePhone || '',
      pincode: address.pincode,
      address: address.address,
      locality: address.locality,
      city: address.city,
      state: address.state,
      country: address.country || 'India',
      landmark: address.landmark || '',
      addressType: address.addressType,
      isDefault: address.isDefault || false
    });

    if (address.coordinates) {
      this.selectedMapCoordinates = {
        latitude: address.coordinates.latitude,
        longitude: address.coordinates.longitude
      };
    }
  }

  private resetAddressForm() {
    this.newAddressForm.reset({
      name: '',
      phone: '',
      alternatePhone: '',
      pincode: '',
      address: '',
      locality: '',
      city: '',
      state: '',
      country: 'India',
      landmark: '',
      addressType: 'HOME',
      isDefault: false
    });
    this.isEditingAddress = false;
    this.editingAddressId = null;
    this.selectedMapCoordinates = null;
  }

  addAddress() {
    if (this.newAddressForm.valid) {
      const addressData = this.newAddressForm.value;
      
      if (this.selectedMapCoordinates) {
        addressData.coordinates = this.selectedMapCoordinates;
      }

      if (this.isEditingAddress && this.editingAddressId) {
        // Update existing address
        const updatedAddress: Address = {
          ...this.addresses.find(addr => addr.id === this.editingAddressId)!,
          ...addressData
        };

        this.addressService.updateAddress(this.editingAddressId, updatedAddress)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: () => {
              this.successMessage = 'Address updated successfully!';
              this.loadAddresses();
              this.toggleAddressForm();
            },
            error: (error) => {
              this.errorMessage = 'Failed to update address. Please try again.';
              console.error('Error updating address:', error);
            }
          });
      } else {
        // Add new address
        this.addressService.addAddress(addressData)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: () => {
              this.successMessage = 'Address added successfully!';
              this.loadAddresses();
              this.toggleAddressForm();
            },
            error: (error) => {
              this.errorMessage = 'Failed to add address. Please try again.';
              console.error('Error adding address:', error);
            }
          });
      }
    } else {
      this.newAddressForm.markAllAsTouched();
    }
  }

  setDefaultAddress(addressId: string) {
    this.addressService.setDefaultAddress(addressId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.successMessage = 'Default address updated!';
          this.loadAddresses();
        },
        error: (error) => {
          this.errorMessage = 'Failed to set default address. Please try again.';
          console.error('Error setting default address:', error);
        }
      });
  }

  deleteAddress(addressId: string) {
    if (confirm('Are you sure you want to delete this address?')) {
      this.addressService.deleteAddress(addressId)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.successMessage = 'Address deleted successfully!';
            this.loadAddresses();
          },
          error: (error) => {
            this.errorMessage = 'Failed to delete address. Please try again.';
            console.error('Error deleting address:', error);
          }
        });
    }
  }

  // Map Integration (Mock implementation)
  onMapClick(event: any) {
    // This would be integrated with Google Maps or similar
    console.log('Map clicked:', event);
    this.selectedMapCoordinates = {
      latitude: 12.9716 + (Math.random() - 0.5) * 0.1, // Mock coordinates
      longitude: 77.5946 + (Math.random() - 0.5) * 0.1
    };
  }

  // Profile Picture Upload
  onProfilePictureChange(event: any) {
    const file = event.target.files[0];
    if (file) {
      // In real app, this would upload to cloud storage
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.profileForm.patchValue({
          profilePicture: e.target.result
        });
      };
      reader.readAsDataURL(file);
    }
  }

  // Utility methods
  private clearMessages() {
    this.successMessage = '';
    this.errorMessage = '';
  }

  getFieldError(fieldName: string): string {
    const field = this.profileForm.get(fieldName);
    if (field?.errors && field?.touched) {
      if (field.errors['required']) return `${this.getFieldDisplayName(fieldName)} is required.`;
      if (field.errors['email']) return 'Please enter a valid email address.';
      if (field.errors['minlength']) return `${this.getFieldDisplayName(fieldName)} must be at least ${field.errors['minlength'].requiredLength} characters.`;
      if (field.errors['maxlength']) return `${this.getFieldDisplayName(fieldName)} must not exceed ${field.errors['maxlength'].requiredLength} characters.`;
      if (field.errors['pattern']) return 'Please enter a valid format.';
    }
    return '';
  }

  private getFieldDisplayName(fieldName: string): string {
    const names: { [key: string]: string } = {
      fullName: 'Full name',
      username: 'Username',
      email: 'Email',
      mobileNumber: 'Mobile number',
      dateOfBirth: 'Date of birth',
      bio: 'Bio'
    };
    return names[fieldName] || fieldName;
  }

  hasFieldError(fieldName: string): boolean {
    const field = this.profileForm.get(fieldName);
    return !!(field?.errors && field?.touched);
  }

  getAddressFieldError(fieldName: string): string {
    const field = this.newAddressForm.get(fieldName);
    if (field?.errors && field?.touched) {
      if (field.errors['required']) return `${this.getAddressFieldDisplayName(fieldName)} is required.`;
      if (field.errors['pattern']) return 'Please enter a valid format.';
      if (field.errors['minlength']) return `${this.getAddressFieldDisplayName(fieldName)} must be at least ${field.errors['minlength'].requiredLength} characters.`;
      if (field.errors['maxlength']) return `${this.getAddressFieldDisplayName(fieldName)} must not exceed ${field.errors['maxlength'].requiredLength} characters.`;
    }
    return '';
  }

  hasAddressFieldError(fieldName: string): boolean {
    const field = this.newAddressForm.get(fieldName);
    return !!(field?.errors && field?.touched);
  }

  private getAddressFieldDisplayName(fieldName: string): string {
    const names: { [key: string]: string } = {
      name: 'Name',
      phone: 'Phone number',
      alternatePhone: 'Alternate phone number',
      pincode: 'Pincode',
      address: 'Address',
      locality: 'Locality',
      city: 'City',
      state: 'State',
      country: 'Country',
      landmark: 'Landmark',
      addressType: 'Address type'
    };
    return names[fieldName] || fieldName;
  }

  // Get states for dropdown
  get states(): string[] {
    return [
      'Andhra Pradesh', 'Arunachal Pradesh', 'Assam', 'Bihar', 'Chhattisgarh',
      'Goa', 'Gujarat', 'Haryana', 'Himachal Pradesh', 'Jharkhand',
      'Karnataka', 'Kerala', 'Madhya Pradesh', 'Maharashtra', 'Manipur',
      'Meghalaya', 'Mizoram', 'Nagaland', 'Odisha', 'Punjab',
      'Rajasthan', 'Sikkim', 'Tamil Nadu', 'Telangana', 'Tripura',
      'Uttar Pradesh', 'Uttarakhand', 'West Bengal'
    ];
  }

  get addressTypes() {
    return [
      { value: 'HOME', label: 'Home', description: 'All day delivery' },
      { value: 'WORK', label: 'Work', description: 'Delivery between 10 AM - 5 PM' },
      { value: 'OTHER', label: 'Other', description: 'Custom delivery time' }
    ];
  }
}