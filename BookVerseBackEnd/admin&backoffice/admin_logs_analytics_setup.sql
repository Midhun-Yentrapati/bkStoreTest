-- Admin Logs and Analytics Database Setup
USE bookstore_db;

-- Create admin_logs table for admin activity tracking
CREATE TABLE IF NOT EXISTS admin_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id VARCHAR(100) NOT NULL,
    admin_name VARCHAR(255) NOT NULL,
    action_type VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id VARCHAR(100),
    description TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    status VARCHAR(50) NOT NULL,
    error_message TEXT,
    metadata JSON,
    session_id VARCHAR(100),
    duration_ms BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_admin_id (admin_id),
    INDEX idx_action_type (action_type),
    INDEX idx_entity_type (entity_type),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);

-- Create admin_analytics table for analytics data
CREATE TABLE IF NOT EXISTS admin_analytics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    analytics_type VARCHAR(100) NOT NULL,
    metric_name VARCHAR(255) NOT NULL,
    metric_value DECIMAL(15,4) NOT NULL,
    metric_unit VARCHAR(50),
    date_range_start DATE,
    date_range_end DATE,
    filters JSON,
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_analytics_type (analytics_type),
    INDEX idx_metric_name (metric_name),
    INDEX idx_calculated_at (calculated_at)
);

-- Create admin_dashboard_metrics table for dashboard data
CREATE TABLE IF NOT EXISTS admin_dashboard_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    metric_date DATE NOT NULL,
    total_users INT DEFAULT 0,
    total_orders INT DEFAULT 0,
    total_revenue DECIMAL(15,2) DEFAULT 0.00,
    total_books INT DEFAULT 0,
    active_carts INT DEFAULT 0,
    pending_orders INT DEFAULT 0,
    completed_orders INT DEFAULT 0,
    cancelled_orders INT DEFAULT 0,
    new_registrations INT DEFAULT 0,
    page_views INT DEFAULT 0,
    unique_visitors INT DEFAULT 0,
    conversion_rate DECIMAL(5,2) DEFAULT 0.00,
    average_order_value DECIMAL(10,2) DEFAULT 0.00,
    top_selling_category VARCHAR(100),
    top_selling_book VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_metric_date (metric_date),
    INDEX idx_metric_date (metric_date)
);

-- Insert sample admin logs data
INSERT INTO admin_logs (admin_id, admin_name, action_type, entity_type, entity_id, description, ip_address, user_agent, status, session_id, duration_ms) VALUES
('admin001', 'John Admin', 'LOGIN', 'USER', 'admin001', 'Admin logged in successfully', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', 'SUCCESS', 'session001', 1200),
('admin002', 'Jane Manager', 'CREATE_BOOK', 'BOOK', 'book001', 'Created new book: The Great Gatsby', '192.168.1.101', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36', 'SUCCESS', 'session002', 2500),
('admin001', 'John Admin', 'UPDATE_ORDER', 'ORDER', 'order001', 'Updated order status to shipped', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', 'SUCCESS', 'session001', 1800),
('admin003', 'Bob Editor', 'DELETE_USER', 'USER', 'user456', 'Deleted user account', '192.168.1.102', 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36', 'SUCCESS', 'session003', 3000),
('admin002', 'Jane Manager', 'CREATE_CATEGORY', 'CATEGORY', 'cat001', 'Created new category: Science Fiction', '192.168.1.101', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36', 'SUCCESS', 'session002', 1500),
('admin001', 'John Admin', 'VIEW_ANALYTICS', 'ANALYTICS', 'dashboard', 'Viewed admin dashboard', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', 'SUCCESS', 'session001', 800),
('admin003', 'Bob Editor', 'EXPORT_DATA', 'DATA', 'users', 'Exported user data to CSV', '192.168.1.102', 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36', 'SUCCESS', 'session003', 4500),
('admin002', 'Jane Manager', 'UPDATE_BOOK', 'BOOK', 'book002', 'Updated book details: 1984', '192.168.1.101', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36', 'SUCCESS', 'session002', 2200),
('admin001', 'John Admin', 'DELETE_BOOK', 'BOOK', 'book003', 'Deleted book: Old Book', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', 'SUCCESS', 'session001', 900),
('admin003', 'Bob Editor', 'VIEW_LOGS', 'LOGS', 'admin_logs', 'Viewed admin activity logs', '192.168.1.102', 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36', 'SUCCESS', 'session003', 1200);

-- Insert sample analytics data
INSERT INTO admin_analytics (analytics_type, metric_name, metric_value, metric_unit, date_range_start, date_range_end, filters) VALUES
('USER_ANALYTICS', 'total_users', 1250, 'count', '2025-09-01', '2025-09-07', '{"status": "active"}'),
('ORDER_ANALYTICS', 'total_orders', 89, 'count', '2025-09-01', '2025-09-07', '{"status": "completed"}'),
('REVENUE_ANALYTICS', 'total_revenue', 12500.50, 'currency', '2025-09-01', '2025-09-07', '{"currency": "USD"}'),
('BOOK_ANALYTICS', 'total_books', 450, 'count', '2025-09-01', '2025-09-07', '{"status": "active"}'),
('CART_ANALYTICS', 'active_carts', 25, 'count', '2025-09-01', '2025-09-07', '{"status": "active"}'),
('CONVERSION_ANALYTICS', 'conversion_rate', 3.2, 'percentage', '2025-09-01', '2025-09-07', '{}'),
('PERFORMANCE_ANALYTICS', 'average_page_load_time', 1.5, 'seconds', '2025-09-01', '2025-09-07', '{}'),
('USER_ANALYTICS', 'new_registrations', 45, 'count', '2025-09-01', '2025-09-07', '{}'),
('ORDER_ANALYTICS', 'average_order_value', 140.45, 'currency', '2025-09-01', '2025-09-07', '{"currency": "USD"}'),
('BOOK_ANALYTICS', 'top_selling_category', 1, 'rank', '2025-09-01', '2025-09-07', '{"category": "Fiction"}');

-- Insert sample dashboard metrics
INSERT INTO admin_dashboard_metrics (metric_date, total_users, total_orders, total_revenue, total_books, active_carts, pending_orders, completed_orders, cancelled_orders, new_registrations, page_views, unique_visitors, conversion_rate, average_order_value, top_selling_category, top_selling_book) VALUES
('2025-09-01', 1200, 15, 2500.00, 445, 20, 5, 10, 0, 8, 1250, 450, 2.5, 166.67, 'Fiction', 'The Great Gatsby'),
('2025-09-02', 1210, 18, 3200.50, 448, 22, 7, 11, 0, 10, 1380, 480, 3.1, 177.81, 'Science Fiction', '1984'),
('2025-09-03', 1215, 12, 1800.25, 450, 18, 4, 8, 0, 5, 1100, 420, 2.8, 150.02, 'Mystery', 'Sherlock Holmes'),
('2025-09-04', 1225, 22, 4200.75, 452, 25, 8, 14, 0, 12, 1650, 520, 3.5, 190.94, 'Romance', 'Pride and Prejudice'),
('2025-09-05', 1230, 16, 2800.00, 453, 20, 6, 10, 0, 7, 1420, 460, 3.0, 175.00, 'Thriller', 'The Da Vinci Code'),
('2025-09-06', 1235, 20, 3500.25, 455, 23, 7, 13, 0, 9, 1580, 490, 3.2, 175.01, 'Fiction', 'To Kill a Mockingbird'),
('2025-09-07', 1240, 25, 4200.50, 458, 28, 9, 16, 0, 11, 1720, 510, 3.8, 168.02, 'Science Fiction', 'Dune');

-- Show the data
SELECT 'Admin Logs:' as table_name;
SELECT * FROM admin_logs ORDER BY created_at DESC LIMIT 5;

SELECT 'Admin Analytics:' as table_name;
SELECT * FROM admin_analytics ORDER BY calculated_at DESC LIMIT 5;

SELECT 'Dashboard Metrics:' as table_name;
SELECT * FROM admin_dashboard_metrics ORDER BY metric_date DESC LIMIT 5;
