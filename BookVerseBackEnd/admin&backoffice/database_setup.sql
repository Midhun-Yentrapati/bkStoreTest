-- Create database
CREATE DATABASE IF NOT EXISTS bookstore_db;
USE bookstore_db;

-- Create admin_activity_logs table
CREATE TABLE IF NOT EXISTS admin_activity_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id VARCHAR(100) NOT NULL,
    admin_name VARCHAR(255) NOT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id VARCHAR(100),
    description TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    status VARCHAR(50) NOT NULL,
    error_message TEXT,
    metadata TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    session_id VARCHAR(100),
    duration_ms BIGINT
);

-- Create daily_sales_summary table
CREATE TABLE IF NOT EXISTS daily_sales_summary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sale_date DATE NOT NULL UNIQUE,
    total_orders INT DEFAULT 0,
    total_quantity_sold INT DEFAULT 0,
    total_revenue DECIMAL(10,2) DEFAULT 0.00,
    total_platform_fee DECIMAL(10,2) DEFAULT 0.00,
    total_shipping_fee DECIMAL(10,2) DEFAULT 0.00,
    total_taxes DECIMAL(10,2) DEFAULT 0.00,
    total_discount DECIMAL(10,2) DEFAULT 0.00,
    net_revenue DECIMAL(10,2) DEFAULT 0.00,
    average_order_value DECIMAL(10,2) DEFAULT 0.00,
    cancelled_orders INT DEFAULT 0,
    delivered_orders INT DEFAULT 0,
    pending_orders INT DEFAULT 0,
    refunded_orders INT DEFAULT 0,
    top_selling_category VARCHAR(100),
    top_selling_book_id VARCHAR(100),
    top_selling_book_title VARCHAR(255),
    top_selling_book_quantity INT DEFAULT 0,
    least_selling_book_id VARCHAR(100),
    least_selling_book_title VARCHAR(255),
    least_selling_book_quantity INT DEFAULT 0,
    new_customers INT DEFAULT 0,
    returning_customers INT DEFAULT 0,
    conversion_rate DECIMAL(5,2) DEFAULT 0.00,
    peak_hour INT,
    peak_hour_orders INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert sample data for admin_activity_logs
INSERT INTO admin_activity_logs (admin_id, admin_name, action, entity_type, entity_id, description, ip_address, user_agent, status, session_id, duration_ms) VALUES
('admin001', 'John Admin', 'LOGIN', 'USER', 'user123', 'Admin logged in successfully', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', 'SUCCESS', 'session001', 1200),
('admin002', 'Jane Manager', 'CREATE_BOOK', 'BOOK', 'book001', 'Created new book: The Great Gatsby', '192.168.1.101', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36', 'SUCCESS', 'session002', 2500),
('admin001', 'John Admin', 'UPDATE_BOOK', 'BOOK', 'book002', 'Updated book details: 1984', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', 'SUCCESS', 'session001', 1800),
('admin003', 'Bob Editor', 'DELETE_BOOK', 'BOOK', 'book003', 'Deleted book: Old Book', '192.168.1.102', 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36', 'SUCCESS', 'session003', 900),
('admin002', 'Jane Manager', 'CREATE_CATEGORY', 'CATEGORY', 'cat001', 'Created new category: Science Fiction', '192.168.1.101', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36', 'SUCCESS', 'session002', 1500),
('admin001', 'John Admin', 'LOGIN', 'USER', 'user123', 'Admin logged in successfully', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', 'SUCCESS', 'session004', 1100),
('admin003', 'Bob Editor', 'UPDATE_ORDER', 'ORDER', 'order001', 'Updated order status to shipped', '192.168.1.102', 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36', 'SUCCESS', 'session003', 2000),
('admin002', 'Jane Manager', 'CREATE_BOOK', 'BOOK', 'book004', 'Created new book: To Kill a Mockingbird', '192.168.1.101', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36', 'SUCCESS', 'session002', 2200),
('admin001', 'John Admin', 'DELETE_USER', 'USER', 'user456', 'Deleted user account', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', 'SUCCESS', 'session004', 3000),
('admin003', 'Bob Editor', 'LOGIN', 'USER', 'user789', 'Admin logged in successfully', '192.168.1.102', 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36', 'SUCCESS', 'session005', 1300);

-- Insert sample data for daily_sales_summary
INSERT INTO daily_sales_summary (sale_date, total_orders, total_quantity_sold, total_revenue, total_platform_fee, total_shipping_fee, total_taxes, total_discount, net_revenue, average_order_value, cancelled_orders, delivered_orders, pending_orders, refunded_orders, top_selling_category, top_selling_book_id, top_selling_book_title, top_selling_book_quantity, least_selling_book_id, least_selling_book_title, least_selling_book_quantity, new_customers, returning_customers, conversion_rate, peak_hour, peak_hour_orders) VALUES
('2025-09-01', 25, 45, 1250.50, 62.53, 125.00, 100.04, 50.00, 1012.97, 50.02, 2, 20, 3, 0, 'Fiction', 'book001', 'The Great Gatsby', 8, 'book005', 'Old Book', 1, 5, 20, 3.2, 14, 8),
('2025-09-02', 30, 52, 1580.75, 79.04, 150.00, 126.46, 75.00, 1280.25, 52.69, 1, 25, 4, 0, 'Science Fiction', 'book002', '1984', 12, 'book006', 'Unknown Book', 1, 8, 22, 3.8, 15, 10),
('2025-09-03', 18, 35, 920.25, 46.01, 90.00, 73.62, 30.00, 750.62, 51.13, 3, 12, 3, 0, 'Mystery', 'book003', 'Sherlock Holmes', 6, 'book007', 'Rare Book', 1, 3, 15, 2.5, 16, 6),
('2025-09-04', 35, 68, 1890.00, 94.50, 175.00, 151.20, 100.00, 1520.30, 54.00, 1, 30, 4, 0, 'Romance', 'book004', 'Pride and Prejudice', 15, 'book008', 'Outdated Book', 1, 10, 25, 4.2, 13, 12),
('2025-09-05', 22, 40, 1100.80, 55.04, 110.00, 88.06, 40.00, 891.70, 50.04, 2, 18, 2, 0, 'Thriller', 'book009', 'The Da Vinci Code', 7, 'book010', 'Boring Book', 1, 6, 16, 2.8, 17, 5);

-- Show the data
SELECT 'Admin Activity Logs:' as table_name;
SELECT * FROM admin_activity_logs ORDER BY created_at DESC;

SELECT 'Daily Sales Summary:' as table_name;
SELECT * FROM daily_sales_summary ORDER BY sale_date DESC;
