-- Initialize databases for booking platform services
CREATE DATABASE IF NOT EXISTS auth_booking;
CREATE DATABASE IF NOT EXISTS payment_service;
CREATE DATABASE IF NOT EXISTS booking_db;

-- Create users with proper permissions
CREATE USER IF NOT EXISTS 'booking_user'@'%' IDENTIFIED BY 'booking123';
GRANT ALL PRIVILEGES ON auth_booking.* TO 'booking_user'@'%';
GRANT ALL PRIVILEGES ON payment_service.* TO 'booking_user'@'%';
GRANT ALL PRIVILEGES ON booking_db.* TO 'booking_user'@'%';

FLUSH PRIVILEGES;