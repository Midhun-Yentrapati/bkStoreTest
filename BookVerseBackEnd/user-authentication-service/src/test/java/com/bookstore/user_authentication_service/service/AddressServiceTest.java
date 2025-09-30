package com.bookstore.user_authentication_service.service;

import com.bookstore.user_authentication_service.dto.AddressDTO;
import com.bookstore.user_authentication_service.entity.Address;
import com.bookstore.user_authentication_service.entity.AddressType;
import com.bookstore.user_authentication_service.entity.User;
import com.bookstore.user_authentication_service.exception.ValidationException;
import com.bookstore.user_authentication_service.exception.ResourceNotFoundException;
import com.bookstore.user_authentication_service.repository.AddressRepository;
import com.bookstore.user_authentication_service.repository.UserRepository;
import com.bookstore.user_authentication_service.service.impl.AddressServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AddressService Tests")
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AddressServiceImpl addressService;

    private User testUser;
    private Address testAddress;
    private AddressDTO addressDTO;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id("user-id")
                .username("testuser")
                .email("test@example.com")
                .build();

        testAddress = Address.builder()
                .id("address-id")
                .name("John Doe")
                .phone("1234567890")
                .addressLine1("123 Main St")
                .landmark("Downtown")
                .city("New York")
                .state("NY")
                .country("USA")
                .pincode("10001")
                .addressType(AddressType.HOME)
                .isDefault(false)
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .build();

        addressDTO = AddressDTO.builder()
                .name("John Doe")
                .phone("1234567890")
                .addressLine1("123 Main St")
                .landmark("Downtown")
                .city("New York")
                .state("NY")
                .country("USA")
                .pincode("10001")
                .addressType(AddressType.HOME)
                .isDefault(false)
                .build();
    }

    @Nested
    @DisplayName("Address Creation Tests")
    class AddressCreationTests {

        @Test
        @DisplayName("Should create address successfully")
        void shouldCreateAddressSuccessfully() {
            // Given
            when(userRepository.findById("user-id")).thenReturn(Optional.of(testUser));
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

            // When
            AddressDTO result = addressService.createAddress("user-id", addressDTO);

            // Then
            assertNotNull(result);
            assertEquals("John Doe", result.getName());
            assertEquals("1234567890", result.getPhone());
            verify(addressRepository).save(any(Address.class));
        }

        @Test
        @DisplayName("Should set first address as default automatically")
        void shouldSetFirstAddressAsDefaultAutomatically() {
            // Given
            when(userRepository.findById("user-id")).thenReturn(Optional.of(testUser));
            when(addressRepository.countByUserId("user-id")).thenReturn(0L);
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

            // When
            addressService.createAddress("user-id", addressDTO);

            // Then
            verify(addressRepository).save(argThat(address -> address.getIsDefault()));
        }

        @Test
        @DisplayName("Should not set as default when user has existing addresses")
        void shouldNotSetAsDefaultWhenUserHasExistingAddresses() {
            // Given
            when(userRepository.findById("user-id")).thenReturn(Optional.of(testUser));
            when(addressRepository.countByUserId("user-id")).thenReturn(2L);
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

            // When
            addressService.createAddress("user-id", addressDTO);

            // Then
            verify(addressRepository).save(argThat(address -> !address.getIsDefault()));
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            when(userRepository.findById("non-existent-user")).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class, () -> 
                addressService.createAddress("non-existent-user", addressDTO));
        }
    }

    @Nested
    @DisplayName("Default Address Management Tests")
    class DefaultAddressManagementTests {

        @Test
        @DisplayName("Should set address as default")
        void shouldSetAddressAsDefault() {
            // Given
            Address currentDefault = Address.builder()
                    .id("current-default-id")
                    .isDefault(true)
                    .user(testUser)
                    .build();

            when(addressRepository.findById("address-id")).thenReturn(Optional.of(testAddress));
            when(addressRepository.findDefaultAddressByUserId("user-id"))
                    .thenReturn(Optional.of(currentDefault));
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

            // When
            AddressDTO result = addressService.setDefaultAddress("user-id", "address-id");

            // Then
            assertNotNull(result);
            verify(addressRepository, times(2)).save(any(Address.class));
            verify(addressRepository).save(argThat(address -> 
                address.getId().equals("current-default-id") && !address.getIsDefault()));
            verify(addressRepository).save(argThat(address -> 
                address.getId().equals("address-id") && address.getIsDefault()));
        }

        @Test
        @DisplayName("Should handle setting default when no current default exists")
        void shouldHandleSettingDefaultWhenNoCurrentDefaultExists() {
            // Given
            when(addressRepository.findById("address-id")).thenReturn(Optional.of(testAddress));
            when(addressRepository.findDefaultAddressByUserId("user-id"))
                    .thenReturn(Optional.empty());
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

            // When
            AddressDTO result = addressService.setDefaultAddress("user-id", "address-id");

            // Then
            assertNotNull(result);
            verify(addressRepository, times(1)).save(any(Address.class));
            verify(addressRepository).save(argThat(address -> 
                address.getId().equals("address-id") && address.getIsDefault()));
        }

        @Test
        @DisplayName("Should get default address")
        void shouldGetDefaultAddress() {
            // Given
            testAddress.setIsDefault(true);
            when(addressRepository.findDefaultAddressByUserId("user-id"))
                    .thenReturn(Optional.of(testAddress));

            // When
            Optional<AddressDTO> resultOptional = addressService.getDefaultAddress("user-id");

            // Then
            assertTrue(resultOptional.isPresent());
            AddressDTO result = resultOptional.get();
            assertNotNull(result);
            assertTrue(result.getIsDefault());
            assertEquals("address-id", result.getId());
        }

        @Test
        @DisplayName("Should return empty Optional when no default address exists")
        void shouldReturnEmptyOptionalWhenNoDefaultAddressExists() {
            // Given
            when(addressRepository.findDefaultAddressByUserId("user-id"))
                    .thenReturn(Optional.empty());

            // When
            Optional<AddressDTO> result = addressService.getDefaultAddress("user-id");

            // Then
            assertFalse(result.isPresent());
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should throw exception when trying to set non-existent address as default")
        void shouldThrowExceptionWhenTryingToSetNonExistentAddressAsDefault() {
            // Given
            when(addressRepository.findById("non-existent-address")).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class, () -> 
                addressService.setDefaultAddress("user-id", "non-existent-address"));
        }
    }

    @Nested
    @DisplayName("Address Validation Tests")
    class AddressValidationTests {

        @ParameterizedTest
        @ValueSource(strings = {"12345", "123456", "ABCDEF", "12-34", "123 45"})
        @DisplayName("Should accept valid postal codes")
        void shouldAcceptValidPostalCodes(String validPincode) {
            // Given
            addressDTO.setPincode(validPincode);
            when(userRepository.findById("user-id")).thenReturn(Optional.of(testUser));
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

            // When & Then
            assertDoesNotThrow(() -> addressService.createAddress("user-id", addressDTO));
        }

        @ParameterizedTest
        @ValueSource(strings = {"1234", "1234567", "ABCDEFG", "12@34", ""})
        @DisplayName("Should reject invalid postal codes")
        void shouldRejectInvalidPostalCodes(String invalidPincode) {
            // Given
            addressDTO.setPincode(invalidPincode);

            // When & Then
            assertThrows(ValidationException.class, () -> 
                addressService.createAddress("user-id", addressDTO));
        }

        @Test
        @DisplayName("Should reject address with missing required fields")
        void shouldRejectAddressWithMissingRequiredFields() {
            // Given
            addressDTO.setName(null);
            addressDTO.setAddressLine1(null);
            addressDTO.setCity(null);

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () -> 
                addressService.createAddress("user-id", addressDTO));
            assertTrue(exception.getMessage().contains("required"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"123456789012345", "12345678901", "abcdefghij", "+1234567890"})
        @DisplayName("Should accept valid phone numbers")
        void shouldAcceptValidPhoneNumbers(String validPhone) {
            // Given
            addressDTO.setPhone(validPhone);
            when(userRepository.findById("user-id")).thenReturn(Optional.of(testUser));
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

            // When & Then
            assertDoesNotThrow(() -> addressService.createAddress("user-id", addressDTO));
        }

        @ParameterizedTest
        @ValueSource(strings = {"123", "12345678901234567890", "abc123", "123-abc-456"})
        @DisplayName("Should reject invalid phone numbers")
        void shouldRejectInvalidPhoneNumbers(String invalidPhone) {
            // Given
            addressDTO.setPhone(invalidPhone);

            // When & Then
            assertThrows(ValidationException.class, () -> 
                addressService.createAddress("user-id", addressDTO));
        }

        @Test
        @DisplayName("Should handle special characters in address fields")
        void shouldHandleSpecialCharactersInAddressFields() {
            // Given
            addressDTO.setAddressLine1("123 Main St, Apt #4B");
            addressDTO.setAddressLine2("O'Connor Heights");
            addressDTO.setLandmark("Near St. Mary's Church & School");
            when(userRepository.findById("user-id")).thenReturn(Optional.of(testUser));
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

            // When & Then
            assertDoesNotThrow(() -> addressService.createAddress("user-id", addressDTO));
        }
    }

    @Nested
    @DisplayName("Address Retrieval Tests")
    class AddressRetrievalTests {

        @Test
        @DisplayName("Should get all addresses for user")
        void shouldGetAllAddressesForUser() {
            // Given
            List<Address> addresses = Arrays.asList(testAddress);
            when(addressRepository.findByUserId("user-id"))
                    .thenReturn(addresses);

            // When
            List<AddressDTO> result = addressService.getUserAddresses("user-id");

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("John Doe", result.get(0).getName());
        }

        @Test
        @DisplayName("Should get address by ID")
        void shouldGetAddressById() {
            // Given
            when(addressRepository.findById("address-id")).thenReturn(Optional.of(testAddress));

            // When
            Optional<AddressDTO> optionalResult = addressService.getAddressById("address-id");

            // Then
            assertTrue(optionalResult.isPresent());
            AddressDTO result = optionalResult.get();
            assertNotNull(result);
            assertEquals("address-id", result.getId());
            assertEquals("John Doe", result.getName());
        }

        @Test
        @DisplayName("Should throw exception when address not found by ID")
        void shouldThrowExceptionWhenAddressNotFoundById() {
            // Given
            when(addressRepository.findById("non-existent-id")).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class, () -> 
                addressService.getAddressById("non-existent-id"));
        }

        @Test
        @DisplayName("Should return empty list when user has no addresses")
        void shouldReturnEmptyListWhenUserHasNoAddresses() {
            // Given
            when(addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc("user-id"))
                    .thenReturn(Arrays.asList());

            // When
            List<AddressDTO> result = addressService.getUserAddresses("user-id");

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Address Update Tests")
    class AddressUpdateTests {

        @Test
        @DisplayName("Should update address successfully")
        void shouldUpdateAddressSuccessfully() {
            // Given
            AddressDTO updateRequest = AddressDTO.builder()
                    .name("Jane Doe")
                    .phone("9876543210")
                    .addressLine1("456 Oak Ave")
                    .landmark("Uptown")
                    .city("Boston")
                    .state("MA")
                    .country("USA")
                    .pincode("02101")
                    .addressType(AddressType.WORK)
                    .build();

            when(addressRepository.findById("address-id")).thenReturn(Optional.of(testAddress));
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

            // When
            AddressDTO result = addressService.updateAddress(testUser.getId(), "address-id", updateRequest);

            // Then
            assertNotNull(result);
            verify(addressRepository).save(argThat(address -> 
                "Jane Doe".equals(address.getName()) &&
                "9876543210".equals(address.getPhone()) &&
                "456 Oak Ave".equals(address.getAddressLine1()) &&
                AddressType.WORK.equals(address.getAddressType())));
        }

        @Test
        @DisplayName("Should preserve default status during update")
        void shouldPreserveDefaultStatusDuringUpdate() {
            // Given
            testAddress.setIsDefault(true);
            AddressDTO updateRequest = AddressDTO.builder()
                    .name("Updated Name")
                    .isDefault(false) // Try to change default status
                    .build();

            when(addressRepository.findById("address-id")).thenReturn(Optional.of(testAddress));
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

            // When
            addressService.updateAddress("user-id", "address-id", updateRequest);

            // Then
            verify(addressRepository).save(argThat(address -> address.getIsDefault()));
        }

        @Test
        @DisplayName("Should update GPS coordinates")
        void shouldUpdateGpsCoordinates() {
            // Given
            AddressDTO updateRequest = AddressDTO.builder()
                    .latitude(40.7128)  // Use Double literal instead of BigDecimal
                    .longitude(-74.0060) // Use Double literal instead of BigDecimal
                    .build();

            when(addressRepository.findById("address-id")).thenReturn(Optional.of(testAddress));
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

            // When
            addressService.updateAddress("user-id", "address-id", updateRequest);

            // Then
            verify(addressRepository).save(argThat(address -> 
                address.getLatitude() != null && address.getLongitude() != null));
        }
    }

    @Nested
    @DisplayName("Address Deletion Tests")
    class AddressDeletionTests {

        @Test
        @DisplayName("Should delete non-default address successfully")
        void shouldDeleteNonDefaultAddressSuccessfully() {
            // Given
            when(addressRepository.findById("address-id")).thenReturn(Optional.of(testAddress));

            // When
            addressService.deleteAddress("user-id", "address-id");

            // Then
            verify(addressRepository).delete(testAddress);
        }

        @Test
        @DisplayName("Should handle deletion of default address")
        void shouldHandleDeletionOfDefaultAddress() {
            // Given
            testAddress.setIsDefault(true);
            Address anotherAddress = Address.builder()
                    .id("another-address-id")
                    .user(testUser)
                    .isDefault(false)
                    .createdAt(LocalDateTime.now().minusDays(1))
                    .build();

            when(addressRepository.findById("address-id")).thenReturn(Optional.of(testAddress));
            when(addressRepository.findByUserIdAndIdNotOrderByCreatedAtAsc("user-id", "address-id"))
                    .thenReturn(Arrays.asList(anotherAddress));
            when(addressRepository.save(any(Address.class))).thenReturn(anotherAddress);

            // When
            addressService.deleteAddress("user-id", "address-id");

            // Then
            verify(addressRepository).delete(testAddress);
            verify(addressRepository).save(argThat(address -> 
                address.getId().equals("another-address-id") && address.getIsDefault()));
        }

        @Test
        @DisplayName("Should handle deletion of last address")
        void shouldHandleDeletionOfLastAddress() {
            // Given
            testAddress.setIsDefault(true);
            when(addressRepository.findById("address-id")).thenReturn(Optional.of(testAddress));
            when(addressRepository.findByUserIdAndIdNotOrderByCreatedAtAsc("user-id", "address-id"))
                    .thenReturn(Arrays.asList());

            // When
            addressService.deleteAddress("user-id", "address-id");

            // Then
            verify(addressRepository).delete(testAddress);
            verify(addressRepository, never()).save(any(Address.class));
        }

        @Test
        @DisplayName("Should throw exception when trying to delete non-existent address")
        void shouldThrowExceptionWhenTryingToDeleteNonExistentAddress() {
            // Given
            when(addressRepository.findById("non-existent-id")).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class, () -> 
                addressService.deleteAddress("user-id", "non-existent-id"));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Scenarios")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null user ID gracefully")
        void shouldHandleNullUserIdGracefully() {
            // When & Then
            assertThrows(ValidationException.class, () -> 
                addressService.createAddress(null, addressDTO));
            assertThrows(ValidationException.class, () -> 
                addressService.getUserAddresses(null));
            assertThrows(ValidationException.class, () -> 
                addressService.getDefaultAddress(null));
        }

        @Test
        @DisplayName("Should handle null address DTO gracefully")
        void shouldHandleNullAddressDtoGracefully() {
            // When & Then
            assertThrows(ValidationException.class, () -> 
                addressService.createAddress("user-id", null));
            assertThrows(ValidationException.class, () -> 
                addressService.updateAddress("user-id", "address-id", null));
        }

        @Test
        @DisplayName("Should handle very long address fields")
        void shouldHandleVeryLongAddressFields() {
            // Given
            String veryLongString = "a".repeat(500);
            addressDTO.setAddressLine1(veryLongString);
            addressDTO.setInstructions(veryLongString);

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () -> 
                addressService.createAddress("user-id", addressDTO));
            assertTrue(exception.getMessage().contains("too long") || 
                      exception.getMessage().contains("exceeds maximum length"));
        }

        @Test
        @DisplayName("Should handle invalid GPS coordinates")
        void shouldHandleInvalidGpsCoordinates() {
            // Given
            addressDTO.setLatitude(999.999); // Invalid latitude (out of range -90 to 90)
            addressDTO.setLongitude(999.999); // Invalid longitude (out of range -180 to 180)

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () -> 
                addressService.createAddress("user-id", addressDTO));
            assertTrue(exception.getMessage().contains("Invalid coordinates") || 
                      exception.getMessage().contains("latitude") || 
                      exception.getMessage().contains("longitude"));
        }

        @Test
        @DisplayName("Should handle concurrent default address changes")
        void shouldHandleConcurrentDefaultAddressChanges() {
            // Given
            Address address1 = Address.builder().id("addr1").user(testUser).isDefault(true).build();
            Address address2 = Address.builder().id("addr2").user(testUser).isDefault(false).build();

            when(addressRepository.findById("addr2")).thenReturn(Optional.of(address2));
            when(addressRepository.findDefaultAddressByUserId("user-id"))
                    .thenReturn(Optional.of(address1));
            when(addressRepository.save(any(Address.class))).thenReturn(address2);

            // When
            addressService.setDefaultAddress("user-id", "addr2");

            // Then
            verify(addressRepository, times(2)).save(any(Address.class));
        }
    }
}
