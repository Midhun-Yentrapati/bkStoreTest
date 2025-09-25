package com.bookstore.user_authentication_service.service;

import com.bookstore.user_authentication_service.dto.AddressDTO;
import com.bookstore.user_authentication_service.entity.AddressType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface AddressService {
    
    // Address CRUD Operations
    AddressDTO createAddress(String userId, AddressDTO addressDTO);
    Optional<AddressDTO> getAddressById(String addressId);
    List<AddressDTO> getUserAddresses(String userId);
    Page<AddressDTO> getUserAddresses(String userId, Pageable pageable);
    AddressDTO updateAddress(String userId, String addressId, AddressDTO addressDTO);
    void deleteAddress(String userId, String addressId);
    
    // Default Address Management
    AddressDTO setDefaultAddress(String userId, String addressId);
    Optional<AddressDTO> getDefaultAddress(String userId);
    void unsetDefaultAddress(String userId, String addressId);
    void unsetAllDefaultAddresses(String userId);
    
    // Address Type Operations
    List<AddressDTO> getAddressesByType(String userId, AddressType addressType);
    Page<AddressDTO> getAddressesByType(AddressType addressType, Pageable pageable);
    
    // Search and Filter Operations
    List<AddressDTO> searchUserAddresses(String userId, String searchTerm);
    Page<AddressDTO> getAddressesByCity(String city, Pageable pageable);
    Page<AddressDTO> getAddressesByState(String state, Pageable pageable);
    Page<AddressDTO> getAddressesByCountry(String country, Pageable pageable);
    List<AddressDTO> getAddressesByPincode(String pincode);
    
    // Location-based Operations
    List<AddressDTO> getAddressesWithCoordinates();
    List<AddressDTO> getAddressesInBounds(Double minLat, Double maxLat, Double minLng, Double maxLng);
    AddressDTO updateAddressCoordinates(String addressId, Double latitude, Double longitude);
    
    // Validation Operations
    boolean isValidAddress(AddressDTO addressDTO);
    boolean hasDefaultAddress(String userId);
    boolean isAddressOwnedByUser(String userId, String addressId);
    void validateAddressOwnership(String userId, String addressId);
    
    // Statistics and Analytics
    long getAddressCountByUser(String userId);
    long getAddressCountByType(AddressType addressType);
    List<Object[]> getAddressCountByCity();
    List<Object[]> getAddressCountByState();
    
    // Bulk Operations
    List<AddressDTO> createAddressesInBulk(String userId, List<AddressDTO> addresses);
    void deleteAllUserAddresses(String userId);
    
    // Address Formatting
    String getFullAddress(String addressId);
    String getShortAddress(String addressId);
    
    // Geocoding Support (for future implementation)
    void geocodeAddress(String addressId);
    AddressDTO findNearestAddress(String userId, Double latitude, Double longitude);
}
