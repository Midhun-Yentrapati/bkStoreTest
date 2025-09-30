-- Add daily_sales_summary table to AdminService database
USE AdminService;

CREATE TABLE IF NOT EXISTS daily_sales_summary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sales_date DATE NOT NULL UNIQUE,
    total_revenue DECIMAL(10,2),
    total_orders INT,
    total_items_sold INT,
    average_order_value DECIMAL(10,2),
    top_selling_item VARCHAR(255),
    top_selling_item_quantity INT,
    least_selling_item VARCHAR(255),
    least_selling_item_quantity INT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_sales_date (sales_date)
);

-- Insert sample data
INSERT INTO daily_sales_summary (
    sales_date, total_revenue, total_orders, total_items_sold, 
    average_order_value, top_selling_item, top_selling_item_quantity,
    least_selling_item, least_selling_item_quantity
) VALUES 
('2025-09-01', 1500.50, 25, 45, 60.02, 'The Great Gatsby', 15, 'Advanced Mathematics', 2),
('2025-09-02', 2200.75, 35, 60, 62.88, 'To Kill a Mockingbird', 20, 'Quantum Physics', 1),
('2025-09-03', 1800.25, 30, 55, 60.01, '1984', 18, 'Advanced Calculus', 3),
('2025-09-04', 2500.00, 40, 70, 62.50, 'Pride and Prejudice', 25, 'Organic Chemistry', 2),
('2025-09-05', 1900.80, 32, 58, 59.40, 'The Catcher in the Rye', 22, 'Linear Algebra', 1);

SELECT * FROM daily_sales_summary;
SELECT COUNT(*) as total_records FROM daily_sales_summary;
SELECT * FROM daily_sales_summary ORDER BY sales_date DESC LIMIT 10;