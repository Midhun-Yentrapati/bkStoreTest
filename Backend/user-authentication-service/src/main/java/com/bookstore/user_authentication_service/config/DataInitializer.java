package com.bookstore.user_authentication_service.config;

import com.bookstore.user_authentication_service.entity.User;
import com.bookstore.user_authentication_service.entity.UserRole;
import com.bookstore.user_authentication_service.entity.UserType;
import com.bookstore.user_authentication_service.entity.AccountStatus;
import com.bookstore.user_authentication_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.auto-create:false}")
    private boolean autoCreateAdmin;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.email:admin@bookstore.com}")
    private String adminEmail;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Value("${app.admin.full-name:System Administrator}")
    private String adminFullName;

    @Value("${app.admin.employee-id:EMP-ADMIN-001}")
    private String adminEmployeeId;

    @Value("${app.admin.department:IT Administration}")
    private String adminDepartment;

    @Override
    public void run(String... args) throws Exception {
        if (autoCreateAdmin) {
            createSuperAdminIfNotExists();
        }
    }

    private void createSuperAdminIfNotExists() {
        try {
            log.info("Checking if super admin user exists...");
            
            // Check if super admin already exists
            boolean superAdminExists = userRepository.existsByUsernameOrEmail(adminUsername, adminEmail);
            
            if (!superAdminExists) {
                log.info("Creating super admin user: {}", adminUsername);
                
                User superAdmin = User.builder()
                        .id(UUID.randomUUID().toString())
                        .username(adminUsername)
                        .email(adminEmail)
                        .password(passwordEncoder.encode(adminPassword))
                        .fullName(adminFullName)
                        .userRole(UserRole.SUPER_ADMIN)
                        .userType(UserType.ADMIN)
                        .accountStatus(AccountStatus.ACTIVE)
                        .employeeId(adminEmployeeId)
                        .department(adminDepartment)
                        .isEmailVerified(true)
                        .isMobileVerified(false)
                        .isTwoFactorEnabled(false)
                        .failedLoginAttempts(0)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                userRepository.save(superAdmin);
                
                log.info("✅ Super admin user created successfully!");
                log.info("   Username: {}", adminUsername);
                log.info("   Email: {}", adminEmail);
                log.info("   Role: SUPER_ADMIN");
                log.info("   Employee ID: {}", adminEmployeeId);
                log.info("   Department: {}", adminDepartment);
                
            } else {
                log.info("Super admin user already exists, skipping creation.");
            }
            
        } catch (Exception e) {
            log.error("❌ Failed to create super admin user: {}", e.getMessage(), e);
        }
    }
}
