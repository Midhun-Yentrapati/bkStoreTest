-- Book Catalog Database Schema
-- Run this script in MySQL to create the database and user

-- Create database if it doesn't exist
CREATE DATABASE IF NOT EXISTS catlog_schema CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Use the database
USE catlog_schema;

-- Grant privileges to root user (if needed)
GRANT ALL PRIVILEGES ON catlog_schema.* TO 'root'@'localhost';
FLUSH PRIVILEGES;

-- Note: Tables will be auto-created by Hibernate with spring.jpa.hibernate.ddl-auto=update
