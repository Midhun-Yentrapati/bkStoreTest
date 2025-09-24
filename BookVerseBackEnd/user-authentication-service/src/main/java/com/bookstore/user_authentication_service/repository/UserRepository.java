package com.bookstore.user_authentication_service.repository;

import com.bookstore.user_authentication_service.entity.AccountStatus;
import com.bookstore.user_authentication_service.entity.User;
import com.bookstore.user_authentication_service.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    // Basic Authentication Queries
    @Query("SELECT u FROM User u WHERE (u.username = :usernameOrEmail OR u.email = :usernameOrEmail) AND u.deletedAt IS NULL")
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);
    
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.deletedAt IS NULL")
    Optional<User> findByUsername(@Param("username") String username);
    
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<User> findByEmail(@Param("email") String email);
    
    @Query("SELECT u FROM User u WHERE u.employeeId = :employeeId AND u.deletedAt IS NULL")
    Optional<User> findByEmployeeId(@Param("employeeId") String employeeId);
    
    // Existence Checks
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username AND u.deletedAt IS NULL")
    boolean existsByUsername(@Param("username") String username);
    
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    boolean existsByEmail(@Param("email") String email);
    
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.employeeId = :employeeId AND u.deletedAt IS NULL")
    boolean existsByEmployeeId(@Param("employeeId") String employeeId);
    
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE (u.username = :username OR u.email = :email) AND u.deletedAt IS NULL")
    boolean existsByUsernameOrEmail(@Param("username") String username, @Param("email") String email);
    
    // Role-based Queries
    @Query("SELECT u FROM User u WHERE u.userRole = 'CUSTOMER' AND u.deletedAt IS NULL")
    Page<User> findAllCustomers(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.userRole != 'CUSTOMER' AND u.deletedAt IS NULL")
    Page<User> findAllAdmins(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.userRole = :userRole AND u.deletedAt IS NULL")
    Page<User> findByUserRole(@Param("userRole") UserRole userRole, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.userRole IN :roles AND u.deletedAt IS NULL")
    Page<User> findByUserRoleIn(@Param("roles") List<UserRole> roles, Pageable pageable);
    
    // Status-based Queries
    @Query("SELECT u FROM User u WHERE u.accountStatus = :status AND u.deletedAt IS NULL")
    Page<User> findByAccountStatus(@Param("status") AccountStatus status, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.accountStatus = 'ACTIVE' AND u.deletedAt IS NULL")
    Page<User> findActiveUsers(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.accountStatus = 'INACTIVE' AND u.deletedAt IS NULL")
    Page<User> findInactiveUsers(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.accountLockedUntil IS NOT NULL AND u.accountLockedUntil > :now AND u.deletedAt IS NULL")
    Page<User> findLockedUsers(@Param("now") LocalDateTime now, Pageable pageable);
    
    // Verification Status Queries
    @Query("SELECT u FROM User u WHERE u.isEmailVerified = :verified AND u.deletedAt IS NULL")
    Page<User> findByEmailVerified(@Param("verified") Boolean verified, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.isMobileVerified = :verified AND u.deletedAt IS NULL")
    Page<User> findByMobileVerified(@Param("verified") Boolean verified, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.isEmailVerified = false AND u.deletedAt IS NULL")
    Page<User> findUnverifiedEmailUsers(Pageable pageable);
    
    // Department and Manager Queries
    @Query("SELECT u FROM User u WHERE u.department = :department AND u.deletedAt IS NULL")
    Page<User> findByDepartment(@Param("department") String department, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.managerId = :managerId AND u.deletedAt IS NULL")
    List<User> findByManagerId(@Param("managerId") String managerId);
    
    @Query("SELECT DISTINCT u.department FROM User u WHERE u.department IS NOT NULL AND u.deletedAt IS NULL")
    List<String> findAllDepartments();
    
    // Search Queries
    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.employeeId) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.department) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "u.deletedAt IS NULL")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "u.userRole = 'CUSTOMER' AND u.deletedAt IS NULL")
    Page<User> searchCustomers(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.employeeId) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.department) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "u.userRole != 'CUSTOMER' AND u.deletedAt IS NULL")
    Page<User> searchAdmins(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Statistics Queries
    @Query("SELECT COUNT(u) FROM User u WHERE u.deletedAt IS NULL")
    long countAllUsers();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.userRole = :userRole AND u.deletedAt IS NULL")
    long countByUserRole(@Param("userRole") UserRole userRole);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.accountStatus = :status AND u.deletedAt IS NULL")
    long countByAccountStatus(@Param("status") AccountStatus status);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.userRole = 'CUSTOMER' AND u.deletedAt IS NULL")
    long countCustomers();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.userRole != 'CUSTOMER' AND u.deletedAt IS NULL")
    long countAdmins();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate AND u.createdAt <= :endDate AND u.deletedAt IS NULL")
    long countUsersRegisteredBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Update Queries
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = :attempts WHERE u.id = :userId")
    void updateFailedLoginAttempts(@Param("userId") String userId, @Param("attempts") Integer attempts);
    
    @Modifying
    @Query("UPDATE User u SET u.accountLockedUntil = :lockedUntil WHERE u.id = :userId")
    void updateAccountLockedUntil(@Param("userId") String userId, @Param("lockedUntil") LocalDateTime lockedUntil);
    
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime, u.lastLoginIp = :ipAddress WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") String userId, @Param("loginTime") LocalDateTime loginTime, @Param("ipAddress") String ipAddress);
    
    @Modifying
    @Query("UPDATE User u SET u.isEmailVerified = :verified WHERE u.id = :userId")
    void updateEmailVerified(@Param("userId") String userId, @Param("verified") Boolean verified);
    
    @Modifying
    @Query("UPDATE User u SET u.isMobileVerified = :verified WHERE u.id = :userId")
    void updateMobileVerified(@Param("userId") String userId, @Param("verified") Boolean verified);
    
    @Modifying
    @Query("UPDATE User u SET u.accountStatus = :status WHERE u.id = :userId")
    void updateAccountStatus(@Param("userId") String userId, @Param("status") AccountStatus status);
    
    @Modifying
    @Query("UPDATE User u SET u.deletedAt = :deletedAt WHERE u.id = :userId")
    void softDeleteUser(@Param("userId") String userId, @Param("deletedAt") LocalDateTime deletedAt);
    
    // Recent Activity Queries
    @Query("SELECT u FROM User u WHERE u.lastLoginAt >= :since AND u.deletedAt IS NULL ORDER BY u.lastLoginAt DESC")
    Page<User> findRecentlyActiveUsers(@Param("since") LocalDateTime since, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.createdAt >= :since AND u.deletedAt IS NULL ORDER BY u.createdAt DESC")
    Page<User> findRecentlyRegisteredUsers(@Param("since") LocalDateTime since, Pageable pageable);
    
    // Complex Filter Query
    @Query("SELECT u FROM User u WHERE " +
           "(:userRole IS NULL OR u.userRole = :userRole) AND " +
           "(:accountStatus IS NULL OR u.accountStatus = :accountStatus) AND " +
           "(:department IS NULL OR u.department = :department) AND " +
           "(:isEmailVerified IS NULL OR u.isEmailVerified = :isEmailVerified) AND " +
           "(:isMobileVerified IS NULL OR u.isMobileVerified = :isMobileVerified) AND " +
           "u.deletedAt IS NULL")
    Page<User> findUsersWithFilters(
        @Param("userRole") UserRole userRole,
        @Param("accountStatus") AccountStatus accountStatus,
        @Param("department") String department,
        @Param("isEmailVerified") Boolean isEmailVerified,
        @Param("isMobileVerified") Boolean isMobileVerified,
        Pageable pageable
    );
}
