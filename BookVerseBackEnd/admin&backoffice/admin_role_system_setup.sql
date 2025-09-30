-- Admin Role Management System Database Setup
USE AdminService;

-- Create admin_users table
CREATE TABLE IF NOT EXISTS admin_users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    admin_role ENUM('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'MODERATOR', 'SUPPORT') NOT NULL,
    department VARCHAR(100),
    employee_id VARCHAR(50),
    full_name VARCHAR(255) NOT NULL,
    manager_id VARCHAR(36),
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    account_status ENUM('ACTIVE', 'INACTIVE', 'LOCKED', 'SUSPENDED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_admin_role (admin_role),
    INDEX idx_department (department)
);

-- Create admin_permissions table (many-to-many relationship)
CREATE TABLE IF NOT EXISTS admin_permissions (
    admin_id VARCHAR(36),
    permission ENUM(
        'USER_READ', 'USER_WRITE', 'USER_DELETE', 'USER_ACTIVATE', 'USER_DEACTIVATE', 'USER_LOCK',
        'BOOK_READ', 'BOOK_WRITE', 'BOOK_DELETE', 'BOOK_PUBLISH', 'BOOK_UNPUBLISH', 'BOOK_INVENTORY',
        'ORDER_READ', 'ORDER_WRITE', 'ORDER_DELETE', 'ORDER_PROCESS', 'ORDER_CANCEL', 'ORDER_REFUND',
        'ADMIN_READ', 'ADMIN_WRITE', 'ADMIN_DELETE', 'ADMIN_ROLE_ASSIGN', 'ADMIN_PERMISSION_GRANT',
        'SYSTEM_CONFIG', 'SYSTEM_BACKUP', 'SYSTEM_RESTORE', 'AUDIT_READ', 'AUDIT_EXPORT', 'BULK_OPERATIONS',
        'ANALYTICS_READ', 'REPORTS_GENERATE', 'REPORTS_EXPORT'
    ) NOT NULL,
    PRIMARY KEY (admin_id, permission),
    FOREIGN KEY (admin_id) REFERENCES admin_users(id) ON DELETE CASCADE
);

-- Create admin_roles table (Spring Security roles)
CREATE TABLE IF NOT EXISTS admin_roles (
    admin_id VARCHAR(36),
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (admin_id, role),
    FOREIGN KEY (admin_id) REFERENCES admin_users(id) ON DELETE CASCADE
);

-- Insert sample admin users
INSERT INTO admin_users (id, username, email, password, admin_role, department, employee_id, full_name, two_factor_enabled, account_status) VALUES
('admin-uuid-001', 'super.admin', 'super@bookstore.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyVqUxE1MqJqJqJqJqJqJqJqJqJ', 'SUPER_ADMIN', 'IT', 'EMP001', 'Super Administrator', true, 'ACTIVE'),
('admin-uuid-002', 'admin.user', 'admin@bookstore.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyVqUxE1MqJqJqJqJqJqJqJqJqJ', 'ADMIN', 'Operations', 'EMP002', 'Admin User', false, 'ACTIVE'),
('admin-uuid-003', 'manager.user', 'manager@bookstore.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyVqUxE1MqJqJqJqJqJqJqJqJqJ', 'MANAGER', 'Sales', 'EMP003', 'Manager User', false, 'ACTIVE');

-- Insert roles for admin users
INSERT INTO admin_roles (admin_id, role) VALUES
('admin-uuid-001', 'ROLE_SUPER_ADMIN'),
('admin-uuid-001', 'ROLE_ADMIN'),
('admin-uuid-001', 'ROLE_USER'),
('admin-uuid-002', 'ROLE_ADMIN'),
('admin-uuid-002', 'ROLE_USER'),
('admin-uuid-003', 'ROLE_MANAGER'),
('admin-uuid-003', 'ROLE_USER');

-- Insert permissions for SUPER_ADMIN (all permissions)
INSERT INTO admin_permissions (admin_id, permission) VALUES
('admin-uuid-001', 'USER_READ'), ('admin-uuid-001', 'USER_WRITE'), ('admin-uuid-001', 'USER_DELETE'),
('admin-uuid-001', 'USER_ACTIVATE'), ('admin-uuid-001', 'USER_DEACTIVATE'), ('admin-uuid-001', 'USER_LOCK'),
('admin-uuid-001', 'BOOK_READ'), ('admin-uuid-001', 'BOOK_WRITE'), ('admin-uuid-001', 'BOOK_DELETE'),
('admin-uuid-001', 'BOOK_PUBLISH'), ('admin-uuid-001', 'BOOK_UNPUBLISH'), ('admin-uuid-001', 'BOOK_INVENTORY'),
('admin-uuid-001', 'ORDER_READ'), ('admin-uuid-001', 'ORDER_WRITE'), ('admin-uuid-001', 'ORDER_DELETE'),
('admin-uuid-001', 'ORDER_PROCESS'), ('admin-uuid-001', 'ORDER_CANCEL'), ('admin-uuid-001', 'ORDER_REFUND'),
('admin-uuid-001', 'ADMIN_READ'), ('admin-uuid-001', 'ADMIN_WRITE'), ('admin-uuid-001', 'ADMIN_DELETE'),
('admin-uuid-001', 'ADMIN_ROLE_ASSIGN'), ('admin-uuid-001', 'ADMIN_PERMISSION_GRANT'),
('admin-uuid-001', 'SYSTEM_CONFIG'), ('admin-uuid-001', 'SYSTEM_BACKUP'), ('admin-uuid-001', 'SYSTEM_RESTORE'),
('admin-uuid-001', 'AUDIT_READ'), ('admin-uuid-001', 'AUDIT_EXPORT'), ('admin-uuid-001', 'BULK_OPERATIONS'),
('admin-uuid-001', 'ANALYTICS_READ'), ('admin-uuid-001', 'REPORTS_GENERATE'), ('admin-uuid-001', 'REPORTS_EXPORT');

-- Insert permissions for ADMIN (limited permissions)
INSERT INTO admin_permissions (admin_id, permission) VALUES
('admin-uuid-002', 'USER_READ'), ('admin-uuid-002', 'USER_WRITE'),
('admin-uuid-002', 'BOOK_READ'), ('admin-uuid-002', 'BOOK_WRITE'),
('admin-uuid-002', 'ORDER_READ'), ('admin-uuid-002', 'ORDER_WRITE'), ('admin-uuid-002', 'ORDER_PROCESS'),
('admin-uuid-002', 'ANALYTICS_READ'), ('admin-uuid-002', 'REPORTS_GENERATE');

-- Insert permissions for MANAGER (basic permissions)
INSERT INTO admin_permissions (admin_id, permission) VALUES
('admin-uuid-003', 'USER_READ'),
('admin-uuid-003', 'BOOK_READ'),
('admin-uuid-003', 'ORDER_READ'),
('admin-uuid-003', 'ANALYTICS_READ');

-- Show the created tables
SHOW TABLES;
SELECT * FROM admin_users;
SELECT * FROM admin_roles;
SELECT * FROM admin_permissions;
