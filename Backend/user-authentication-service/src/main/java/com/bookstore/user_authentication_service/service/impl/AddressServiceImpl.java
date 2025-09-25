package com.bookstore.user_authentication_service.service.impl;

import com.bookstore.user_authentication_service.dto.AddressDTO;
import com.bookstore.user_authentication_service.entity.Address;
import com.bookstore.user_authentication_service.entity.AddressType;
import com.bookstore.user_authentication_service.entity.User;
import com.bookstore.user_authentication_service.exception.ResourceNotFoundException;
import com.bookstore.user_authentication_service.exception.ValidationException;
import com.bookstore.user_authentication_service.repository.AddressRepository;
import com.bookstore.user_authentication_service.repository.UserRepository;
import com.bookstore.user_authentication_service.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    public AddressDTO createAddress(String userId, AddressDTO addressDTO) {
        log.info("Creating address for user: {}", userId);
        
        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        // Validate address data
        validateAddressData(addressDTO);
        
        // Check if this is the first address for the user
        boolean isFirstAddress = addressRepository.countByUserId(userId) == 0;
        
        // Create address entity
        Address address = new Address();
        mapDtoToEntity(addressDTO, address);
        address.setUser(user);
        address.setCreatedAt(LocalDateTime.now());
        address.setUpdatedAt(LocalDateTime.now());
        
        // Set as default if it's the first address or explicitly requested
        if (isFirstAddress || (addressDTO.getIsDefault() != null && addressDTO.getIsDefault())) {
            // Unset other default addresses first
            if (!isFirstAddress) {
                unsetAllDefaultAddresses(userId);
            }
            address.setIsDefault(true);
        } else {
            address.setIsDefault(false);
        }
        
        Address savedAddress = addressRepository.save(address);
        log.info("Address created successfully with ID: {}", savedAddress.getId());
        
        return mapEntityToDto(savedAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AddressDTO> getAddressById(String addressId) {
        log.debug("Fetching address by ID: {}", addressId);
        
        return addressRepository.findById(addressId)
                .map(this::mapEntityToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressDTO> getUserAddresses(String userId) {
        log.debug("Fetching addresses for user: {}", userId);
        
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        
        List<Address> addresses = addressRepository.findByUserId(userId);
        return addresses.stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AddressDTO> getUserAddresses(String userId, Pageable pageable) {
        log.debug("Fetching paginated addresses for user: {}", userId);
        
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        
        Page<Address> addresses = addressRepository.findByUserId(userId, pageable);
        return addresses.map(this::mapEntityToDto);
    }

    @Override
    public AddressDTO updateAddress(String userId, String addressId, AddressDTO addressDTO) {
        log.info("Updating address {} for user: {}", addressId, userId);
        
        // Validate address ownership
        validateAddressOwnership(userId, addressId);
        
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));
        
        // Validate address data
        validateAddressData(addressDTO);
        
        // Update address fields
        mapDtoToEntity(addressDTO, address);
        address.setUpdatedAt(LocalDateTime.now());
        
        // Handle default address logic
        if (addressDTO.getIsDefault() != null && addressDTO.getIsDefault() && !address.getIsDefault()) {
            unsetAllDefaultAddresses(userId);
            address.setIsDefault(true);
        }
        
        Address updatedAddress = addressRepository.save(address);
        log.info("Address updated successfully: {}", addressId);
        
        return mapEntityToDto(updatedAddress);
    }

    @Override
    public void deleteAddress(String userId, String addressId) {
        log.info("Deleting address {} for user: {}", addressId, userId);
        
        // Validate address ownership
        validateAddressOwnership(userId, addressId);
        
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));
        
        boolean wasDefault = address.getIsDefault();
        
        // Delete the address
        addressRepository.delete(address);
        
        // If this was the default address, set another address as default
        if (wasDefault) {
            List<Address> remainingAddresses = addressRepository.findByUserId(userId);
            if (!remainingAddresses.isEmpty()) {
                Address newDefault = remainingAddresses.get(0);
                newDefault.setIsDefault(true);
                addressRepository.save(newDefault);
                log.info("Set new default address: {}", newDefault.getId());
            }
        }
        
        log.info("Address deleted successfully: {}", addressId);
    }

    @Override
    public AddressDTO setDefaultAddress(String userId, String addressId) {
        log.info("Setting default address {} for user: {}", addressId, userId);
        
        // Validate address ownership
        validateAddressOwnership(userId, addressId);
        
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));
        
        // Unset all other default addresses for this user
        unsetAllDefaultAddresses(userId);
        
        // Set this address as default
        address.setIsDefault(true);
        address.setUpdatedAt(LocalDateTime.now());
        
        Address updatedAddress = addressRepository.save(address);
        log.info("Default address set successfully: {}", addressId);
        
        return mapEntityToDto(updatedAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AddressDTO> getDefaultAddress(String userId) {
        log.debug("Fetching default address for user: {}", userId);
        
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        
        return addressRepository.findDefaultAddressByUserId(userId)
                .map(this::mapEntityToDto);
    }

    @Override
    public void unsetDefaultAddress(String userId, String addressId) {
        log.info("Unsetting default address {} for user: {}", addressId, userId);
        
        // Validate address ownership
        validateAddressOwnership(userId, addressId);
        
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));
        
        if (address.getIsDefault()) {
            address.setIsDefault(false);
            address.setUpdatedAt(LocalDateTime.now());
            addressRepository.save(address);
            log.info("Default address unset successfully: {}", addressId);
        }
    }

    @Override
    public void unsetAllDefaultAddresses(String userId) {
        log.debug("Unsetting all default addresses for user: {}", userId);
        
        addressRepository.unsetAllDefaultAddressesForUser(userId);
        log.debug("Unset all default addresses for user: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressDTO> getAddressesByType(String userId, AddressType addressType) {
        log.debug("Fetching addresses by type {} for user: {}", addressType, userId);
        
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        
        List<Address> addresses = addressRepository.findByUserIdAndAddressType(userId, addressType);
        return addresses.stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AddressDTO> getAddressesByType(AddressType addressType, Pageable pageable) {
        log.debug("Fetching paginated addresses by type: {}", addressType);
        
        Page<Address> addresses = addressRepository.findByAddressType(addressType, pageable);
        return addresses.map(this::mapEntityToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressDTO> searchUserAddresses(String userId, String searchTerm) {
        log.debug("Searching addresses for user {} with term: {}", userId, searchTerm);
        
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        
        List<Address> addresses = addressRepository.searchUserAddresses(userId, searchTerm);
        return addresses.stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AddressDTO> getAddressesByCity(String city, Pageable pageable) {
        log.debug("Fetching addresses by city: {}", city);
        
        Page<Address> addresses = addressRepository.findByCity(city, pageable);
        return addresses.map(this::mapEntityToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AddressDTO> getAddressesByState(String state, Pageable pageable) {
        log.debug("Fetching addresses by state: {}", state);
        
        Page<Address> addresses = addressRepository.findByState(state, pageable);
        return addresses.map(this::mapEntityToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AddressDTO> getAddressesByCountry(String country, Pageable pageable) {
        log.debug("Fetching addresses by country: {}", country);
        
        Page<Address> addresses = addressRepository.findByCountry(country, pageable);
        return addresses.map(this::mapEntityToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressDTO> getAddressesByPincode(String pincode) {
        log.debug("Fetching addresses by pincode: {}", pincode);
        
        List<Address> addresses = addressRepository.findByPincode(pincode);
        return addresses.stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressDTO> getAddressesWithCoordinates() {
        log.debug("Fetching addresses with GPS coordinates");
        
        List<Address> addresses = addressRepository.findAddressesWithCoordinates();
        return addresses.stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressDTO> getAddressesInBounds(Double minLat, Double maxLat, Double minLng, Double maxLng) {
        log.debug("Fetching addresses in bounds: lat({}, {}), lng({}, {})", minLat, maxLat, minLng, maxLng);
        
        List<Address> addresses = addressRepository.findAddressesInBounds(minLat, maxLat, minLng, maxLng);
        return addresses.stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public AddressDTO updateAddressCoordinates(String addressId, Double latitude, Double longitude) {
        log.info("Updating coordinates for address: {}", addressId);
        
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));
        
        address.setLatitude(latitude);
        address.setLongitude(longitude);
        address.setUpdatedAt(LocalDateTime.now());
        
        Address updatedAddress = addressRepository.save(address);
        log.info("Coordinates updated successfully for address: {}", addressId);
        
        return mapEntityToDto(updatedAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isValidAddress(AddressDTO addressDTO) {
        try {
            validateAddressData(addressDTO);
            return true;
        } catch (ValidationException e) {
            log.debug("Address validation failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasDefaultAddress(String userId) {
        return addressRepository.hasDefaultAddress(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAddressOwnedByUser(String userId, String addressId) {
        Optional<Address> address = addressRepository.findById(addressId);
        return address.isPresent() && address.get().getUser().getId().equals(userId);
    }

    @Override
    public void validateAddressOwnership(String userId, String addressId) {
        if (!isAddressOwnedByUser(userId, addressId)) {
            throw new ValidationException("Address not found or not owned by user");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long getAddressCountByUser(String userId) {
        return addressRepository.countByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getAddressCountByType(AddressType addressType) {
        return addressRepository.countByAddressType(addressType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getAddressCountByCity() {
        return addressRepository.getAddressCountByCity();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getAddressCountByState() {
        return addressRepository.getAddressCountByState();
    }

    @Override
    public List<AddressDTO> createAddressesInBulk(String userId, List<AddressDTO> addresses) {
        log.info("Creating {} addresses in bulk for user: {}", addresses.size(), userId);
        
        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        List<Address> addressEntities = new ArrayList<>();
        boolean hasDefault = hasDefaultAddress(userId);
        
        for (AddressDTO addressDTO : addresses) {
            validateAddressData(addressDTO);
            
            Address address = new Address();
            mapDtoToEntity(addressDTO, address);
            address.setUser(user);
            address.setCreatedAt(LocalDateTime.now());
            address.setUpdatedAt(LocalDateTime.now());
            
            // Set first address as default if no default exists
            if (!hasDefault && addressEntities.isEmpty()) {
                address.setIsDefault(true);
                hasDefault = true;
            } else {
                address.setIsDefault(false);
            }
            
            addressEntities.add(address);
        }
        
        List<Address> savedAddresses = addressRepository.saveAll(addressEntities);
        log.info("Successfully created {} addresses in bulk", savedAddresses.size());
        
        return savedAddresses.stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAllUserAddresses(String userId) {
        log.info("Deleting all addresses for user: {}", userId);
        
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        
        addressRepository.softDeleteAllUserAddresses(userId);
        log.info("Successfully deleted all addresses for user: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public String getFullAddress(String addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));
        
        return address.getFullAddress();
    }

    @Override
    @Transactional(readOnly = true)
    public String getShortAddress(String addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));
        
        return address.getShortAddress();
    }

    @Override
    public void geocodeAddress(String addressId) {
        log.info("Geocoding address: {}", addressId);
        
        // Validate address exists
        addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));
        
        // TODO: Implement actual geocoding service integration
        // For now, this is a placeholder implementation
        log.warn("Geocoding service not implemented yet for address: {}", addressId);
    }

    @Override
    @Transactional(readOnly = true)
    public AddressDTO findNearestAddress(String userId, Double latitude, Double longitude) {
        log.debug("Finding nearest address for user {} at coordinates: {}, {}", userId, latitude, longitude);
        
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        
        List<Address> userAddresses = addressRepository.findByUserId(userId);
        
        Address nearestAddress = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Address address : userAddresses) {
            if (address.hasCoordinates()) {
                double distance = calculateDistance(latitude, longitude, 
                        address.getLatitude(), address.getLongitude());
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestAddress = address;
                }
            }
        }
        
        if (nearestAddress == null) {
            throw new ResourceNotFoundException("No address with coordinates found for user: " + userId);
        }
        
        return mapEntityToDto(nearestAddress);
    }

    // Helper method to calculate distance between two coordinates
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c; // Distance in km
    }

    // Helper methods for mapping between DTO and Entity
    private void mapDtoToEntity(AddressDTO dto, Address entity) {
        entity.setName(dto.getName());
        entity.setPhone(dto.getPhone());
        entity.setAlternatePhone(dto.getAlternatePhone());
        entity.setEmail(dto.getEmail());
        entity.setAddressLine1(dto.getAddressLine1());
        entity.setAddressLine2(dto.getAddressLine2());
        entity.setLandmark(dto.getLandmark());
        entity.setCity(dto.getCity());
        entity.setState(dto.getState());
        entity.setCountry(dto.getCountry());
        entity.setPincode(dto.getPincode());
        entity.setAddressType(dto.getAddressType());
        entity.setLatitude(dto.getLatitude());
        entity.setLongitude(dto.getLongitude());
    }

    private AddressDTO mapEntityToDto(Address entity) {
        AddressDTO dto = new AddressDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setPhone(entity.getPhone());
        dto.setAlternatePhone(entity.getAlternatePhone());
        dto.setEmail(entity.getEmail());
        dto.setAddressLine1(entity.getAddressLine1());
        dto.setAddressLine2(entity.getAddressLine2());
        dto.setLandmark(entity.getLandmark());
        dto.setCity(entity.getCity());
        dto.setState(entity.getState());
        dto.setCountry(entity.getCountry());
        dto.setPincode(entity.getPincode());
        dto.setAddressType(entity.getAddressType());
        dto.setIsDefault(entity.getIsDefault());
        dto.setIsActive(entity.getIsActive());
        dto.setInstructions(dto.getInstructions());
        dto.setAccessCode(dto.getAccessCode());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setUserId(entity.getUser().getId());
        dto.setFullAddress(entity.getFullAddress());
        dto.setShortAddress(entity.getShortAddress());
        dto.setHasCoordinates(entity.hasCoordinates());
        return dto;
    }

    private void validateAddressData(AddressDTO addressDTO) {
        if (addressDTO == null) {
            throw new ValidationException("Address data cannot be null");
        }
        
        if (addressDTO.getName() == null || addressDTO.getName().trim().isEmpty()) {
            throw new ValidationException("Address name is required");
        }
        
        if (addressDTO.getPhone() == null || addressDTO.getPhone().trim().isEmpty()) {
            throw new ValidationException("Phone number is required");
        }
        
        if (addressDTO.getAddressLine1() == null || addressDTO.getAddressLine1().trim().isEmpty()) {
            throw new ValidationException("Address line 1 is required");
        }
        
        if (addressDTO.getCity() == null || addressDTO.getCity().trim().isEmpty()) {
            throw new ValidationException("City is required");
        }
        
        if (addressDTO.getState() == null || addressDTO.getState().trim().isEmpty()) {
            throw new ValidationException("State is required");
        }
        
        if (addressDTO.getCountry() == null || addressDTO.getCountry().trim().isEmpty()) {
            throw new ValidationException("Country is required");
        }
        
        if (addressDTO.getPincode() == null || addressDTO.getPincode().trim().isEmpty()) {
            throw new ValidationException("Pincode is required");
        }
        
        if (addressDTO.getAddressType() == null) {
            throw new ValidationException("Address type is required");
        }
        
        // Validate phone number format (basic validation)
        if (!isValidPhoneNumber(addressDTO.getPhone())) {
            throw new ValidationException("Invalid phone number format");
        }
        
        // Validate pincode format (basic validation)
        if (!isValidPincode(addressDTO.getPincode())) {
            throw new ValidationException("Invalid pincode format");
        }
    }

    private boolean isValidPhoneNumber(String phone) {
        // Basic phone validation - adjust regex as needed
        return phone != null && phone.matches("^[+]?[0-9\\s\\-\\(\\)]{10,15}$");
    }

    private boolean isValidPincode(String pincode) {
        // Basic pincode validation - adjust regex as needed for your country
        return pincode != null && pincode.matches("^[0-9]{5,10}$");
    }
}
