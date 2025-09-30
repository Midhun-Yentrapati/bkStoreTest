-- Dummy Data for daily_sales_summary table
USE AdminService;

-- Insert sample daily sales data for the past 30 days
INSERT INTO daily_sales_summary (
    sales_date, 
    total_revenue, 
    total_orders, 
    total_items_sold, 
    average_order_value, 
    top_selling_item, 
    top_selling_item_quantity, 
    least_selling_item, 
    least_selling_item_quantity, 
    created_at, 
    updated_at
) VALUES 
-- Recent data (last 7 days)
('2025-09-10', 2850.75, 45, 78, 63.35, 'The Great Gatsby', 12, 'Advanced Mathematics', 2, NOW(), NOW()),
('2025-09-09', 3200.50, 52, 89, 61.55, 'To Kill a Mockingbird', 15, 'Quantum Physics', 1, NOW(), NOW()),
('2025-09-08', 2750.25, 38, 65, 72.38, '1984', 10, 'Linear Algebra', 3, NOW(), NOW()),
('2025-09-07', 4100.00, 58, 95, 70.69, 'Pride and Prejudice', 18, 'Organic Chemistry', 2, NOW(), NOW()),
('2025-09-06', 3600.80, 48, 82, 75.02, 'The Catcher in the Rye', 14, 'Advanced Calculus', 1, NOW(), NOW()),
('2025-09-05', 2950.40, 42, 71, 70.25, 'Harry Potter and the Sorcerer''s Stone', 16, 'Statistics', 2, NOW(), NOW()),
('2025-09-04', 3300.60, 55, 88, 60.01, 'The Lord of the Rings', 13, 'Differential Equations', 1, NOW(), NOW()),

-- Previous week
('2025-09-03', 2800.30, 44, 76, 63.64, 'The Hobbit', 11, 'Discrete Mathematics', 2, NOW(), NOW()),
('2025-09-02', 3500.90, 50, 85, 70.02, 'Dune', 17, 'Abstract Algebra', 1, NOW(), NOW()),
('2025-09-01', 3100.45, 47, 79, 65.97, 'Foundation', 14, 'Number Theory', 3, NOW(), NOW()),
('2025-08-31', 2700.20, 41, 68, 65.86, 'The Martian', 12, 'Topology', 2, NOW(), NOW()),
('2025-08-30', 3800.70, 56, 92, 67.87, 'Project Hail Mary', 19, 'Real Analysis', 1, NOW(), NOW()),
('2025-08-29', 2900.15, 43, 74, 67.45, 'The Seven Husbands of Evelyn Hugo', 13, 'Complex Analysis', 2, NOW(), NOW()),
('2025-08-28', 3200.85, 49, 81, 65.32, 'Where the Crawdads Sing', 15, 'Graph Theory', 1, NOW(), NOW()),

-- Two weeks ago
('2025-08-27', 2600.50, 39, 66, 66.68, 'The Silent Patient', 10, 'Combinatorics', 3, NOW(), NOW()),
('2025-08-26', 3400.25, 51, 87, 66.67, 'Educated', 16, 'Set Theory', 2, NOW(), NOW()),
('2025-08-25', 3000.80, 46, 77, 65.23, 'Becoming', 14, 'Logic', 1, NOW(), NOW()),
('2025-08-24', 2750.60, 42, 72, 65.49, 'Sapiens', 11, 'Category Theory', 2, NOW(), NOW()),
('2025-08-23', 3600.40, 53, 89, 67.93, 'Atomic Habits', 18, 'Game Theory', 1, NOW(), NOW()),
('2025-08-22', 3100.75, 48, 82, 64.60, 'The Psychology of Money', 15, 'Probability Theory', 3, NOW(), NOW()),
('2025-08-21', 2850.30, 44, 75, 64.78, 'Thinking, Fast and Slow', 13, 'Statistics', 2, NOW(), NOW()),

-- Three weeks ago
('2025-08-20', 3300.90, 50, 84, 66.02, 'The Lean Startup', 17, 'Machine Learning', 1, NOW(), NOW()),
('2025-08-19', 2700.45, 41, 69, 65.86, 'Good to Great', 12, 'Data Structures', 2, NOW(), NOW()),
('2025-08-18', 3500.20, 52, 88, 67.31, 'The Innovator''s Dilemma', 16, 'Algorithms', 1, NOW(), NOW()),
('2025-08-17', 2900.65, 45, 76, 64.46, 'Crossing the Chasm', 14, 'Software Engineering', 3, NOW(), NOW()),
('2025-08-16', 3200.80, 49, 83, 65.32, 'The Hard Thing About Hard Things', 15, 'Database Systems', 2, NOW(), NOW()),
('2025-08-15', 2800.35, 43, 73, 65.12, 'Zero to One', 13, 'Computer Networks', 1, NOW(), NOW()),
('2025-08-14', 3700.50, 54, 91, 68.53, 'The Lean Product Playbook', 19, 'Operating Systems', 2, NOW(), NOW()),

-- Four weeks ago
('2025-08-13', 3000.25, 47, 79, 63.83, 'Hooked', 15, 'Computer Architecture', 1, NOW(), NOW()),
('2025-08-12', 2600.70, 40, 67, 65.02, 'The Mom Test', 11, 'Compiler Design', 3, NOW(), NOW()),
('2025-08-11', 3400.85, 51, 86, 66.68, 'Sprint', 17, 'Distributed Systems', 2, NOW(), NOW()),
('2025-08-10', 3100.40, 48, 81, 64.59, 'The Design of Everyday Things', 14, 'Information Theory', 1, NOW(), NOW()),
('2025-08-09', 2750.15, 42, 71, 65.48, 'Don''t Make Me Think', 12, 'Cryptography', 2, NOW(), NOW()),
('2025-08-08', 3600.60, 53, 90, 67.93, 'The Elements of User Experience', 18, 'Computer Graphics', 1, NOW(), NOW()),
('2025-08-07', 2900.90, 45, 77, 64.46, 'About Face', 13, 'Human-Computer Interaction', 3, NOW(), NOW());

-- Verify the data
SELECT COUNT(*) as total_records FROM daily_sales_summary;
SELECT * FROM daily_sales_summary ORDER BY sales_date DESC LIMIT 10;

-- Show summary statistics
SELECT 
    COUNT(*) as total_days,
    SUM(total_revenue) as total_revenue,
    SUM(total_orders) as total_orders,
    SUM(total_items_sold) as total_items_sold,
    AVG(average_order_value) as avg_order_value
FROM daily_sales_summary;
