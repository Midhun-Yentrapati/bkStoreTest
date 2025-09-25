package com.bookstore.user_authentication_service.repository;

import com.bookstore.user_authentication_service.entity.Address;
import com.bookstore.user_authentication_service.entity.AddressType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, String> {
    
    // Basic User Address Queries
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.isActive = true ORDER BY a.isDefault DESC, a.createdAt DESC")
    List<Address> findByUserId(@Param("userId") String userId);
    
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.isActive = true")
    Page<Address> findByUserId(@Param("userId") String userId, Pageable pageable);
    
    // Method expected by tests - using Spring Data JPA method naming convention
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.isActive = true ORDER BY a.isDefault DESC, a.createdAt DESC")
    List<Address> findByUserIdOrderByIsDefaultDescCreatedAtDesc(@Param("userId") String userId);
    
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.isDefault = true AND a.isActive = true")
    Optional<Address> findDefaultAddressByUserId(@Param("userId") String userId);
    
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.addressType = :addressType AND a.isActive = true")
    List<Address> findByUserIdAndAddressType(@Param("userId") String userId, @Param("addressType") AddressType addressType);
    
    // Method for finding addresses excluding a specific address ID (used in delete operations)
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.id != :excludeId AND a.isActive = true ORDER BY a.createdAt ASC")
    List<Address> findByUserIdAndIdNotOrderByCreatedAtAsc(@Param("userId") String userId, @Param("excludeId") String excludeId);
    
    // Address Type Queries
    @Query("SELECT a FROM Address a WHERE a.addressType = :addressType AND a.isActive = true")
    Page<Address> findByAddressType(@Param("addressType") AddressType addressType, Pageable pageable);
    
    // Location-based Queries
    @Query("SELECT a FROM Address a WHERE a.city = :city AND a.isActive = true")
    Page<Address> findByCity(@Param("city") String city, Pageable pageable);
    
    @Query("SELECT a FROM Address a WHERE a.state = :state AND a.isActive = true")
    Page<Address> findByState(@Param("state") String state, Pageable pageable);
    
    @Query("SELECT a FROM Address a WHERE a.country = :country AND a.isActive = true")
    Page<Address> findByCountry(@Param("country") String country, Pageable pageable);
    
    @Query("SELECT a FROM Address a WHERE a.pincode = :pincode AND a.isActive = true")
    List<Address> findByPincode(@Param("pincode") String pincode);
    
    // Search Queries
    @Query("SELECT a FROM Address a WHERE " +
           "(LOWER(a.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.addressLine1) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.addressLine2) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.city) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.state) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "a.pincode LIKE CONCAT('%', :searchTerm, '%')) AND " +
           "a.user.id = :userId AND a.isActive = true")
    List<Address> searchUserAddresses(@Param("userId") String userId, @Param("searchTerm") String searchTerm);
    
    // Coordinate-based Queries
    @Query("SELECT a FROM Address a WHERE a.latitude IS NOT NULL AND a.longitude IS NOT NULL AND a.isActive = true")
    List<Address> findAddressesWithCoordinates();
    
    @Query("SELECT a FROM Address a WHERE " +
           "a.latitude BETWEEN :minLat AND :maxLat AND " +
           "a.longitude BETWEEN :minLng AND :maxLng AND " +
           "a.isActive = true")
    List<Address> findAddressesInBounds(
        @Param("minLat") Double minLat, @Param("maxLat") Double maxLat,
        @Param("minLng") Double minLng, @Param("maxLng") Double maxLng
    );
    
    // Statistics Queries
    @Query("SELECT COUNT(a) FROM Address a WHERE a.user.id = :userId AND a.isActive = true")
    long countByUserId(@Param("userId") String userId);
    
    @Query("SELECT COUNT(a) FROM Address a WHERE a.addressType = :addressType AND a.isActive = true")
    long countByAddressType(@Param("addressType") AddressType addressType);
    
    @Query("SELECT a.city, COUNT(a) FROM Address a WHERE a.isActive = true GROUP BY a.city ORDER BY COUNT(a) DESC")
    List<Object[]> getAddressCountByCity();
    
    @Query("SELECT a.state, COUNT(a) FROM Address a WHERE a.isActive = true GROUP BY a.state ORDER BY COUNT(a) DESC")
    List<Object[]> getAddressCountByState();
    
    // Update Queries
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId AND a.id != :excludeAddressId")
    void unsetDefaultAddressesForUser(@Param("userId") String userId, @Param("excludeAddressId") String excludeAddressId);
    
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId")
    void unsetAllDefaultAddressesForUser(@Param("userId") String userId);
    
    @Modifying
    @Query("UPDATE Address a SET a.isActive = false WHERE a.id = :addressId")
    void softDeleteAddress(@Param("addressId") String addressId);
    
    @Modifying
    @Query("UPDATE Address a SET a.isActive = false WHERE a.user.id = :userId")
    void softDeleteAllUserAddresses(@Param("userId") String userId);
    
    // Validation Queries
    @Query("SELECT COUNT(a) > 0 FROM Address a WHERE a.user.id = :userId AND a.isDefault = true AND a.isActive = true")
    boolean hasDefaultAddress(@Param("userId") String userId);
    
    @Query("SELECT COUNT(a) FROM Address a WHERE a.user.id = :userId AND a.isDefault = true AND a.isActive = true")
    long countDefaultAddresses(@Param("userId") String userId);
}
