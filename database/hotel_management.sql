CREATE DATABASE IF NOT EXISTS hotel_management;
USE hotel_management;

-- 1. users table
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'STAFF'
);

-- Insert default admin
INSERT IGNORE INTO users (username, password, role) VALUES ('admin', 'admin123', 'ADMIN');

-- 2. rooms table
CREATE TABLE IF NOT EXISTS rooms (
    room_id INT AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(10) NOT NULL UNIQUE,
    room_type VARCHAR(20) NOT NULL,
    price_per_day DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'AVAILABLE'
);

-- Insert sample rooms
INSERT IGNORE INTO rooms (room_number, room_type, price_per_day, status) VALUES 
('101', 'Single', 1000.00, 'AVAILABLE'),
('102', 'Double', 1500.00, 'AVAILABLE'),
('201', 'Deluxe', 2500.00, 'AVAILABLE'),
('301', 'Suite', 5000.00, 'AVAILABLE');

-- 3. customers table
CREATE TABLE IF NOT EXISTS customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    address TEXT,
    id_proof VARCHAR(50) NOT NULL
);

-- Insert sample customer
INSERT IGNORE INTO customers (name, phone, email, address, id_proof) VALUES
('John Doe', '9876543210', 'john@example.com', '123 Main St, City', 'AADHAAR-1234');

-- 4. bookings table
CREATE TABLE IF NOT EXISTS bookings (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    room_id INT NOT NULL,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    adults INT DEFAULT 1,
    children INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES rooms(room_id)
);

-- 5. services table
CREATE TABLE IF NOT EXISTS services (
    service_id INT AUTO_INCREMENT PRIMARY KEY,
    service_name VARCHAR(100) NOT NULL UNIQUE,
    price DECIMAL(10,2) NOT NULL
);

-- Insert sample services
INSERT IGNORE INTO services (service_name, price) VALUES 
('Breakfast', 300.00),
('Lunch', 500.00),
('Dinner', 500.00),
('Laundry', 200.00),
('Room Cleaning', 150.00),
('Airport Pickup', 800.00);

-- 6. customer_services table
CREATE TABLE IF NOT EXISTS customer_services (
    id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    service_id INT NOT NULL,
    quantity INT DEFAULT 1,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES services(service_id)
);

-- 7. payments table
CREATE TABLE IF NOT EXISTS payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    payment_method VARCHAR(20) NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE
);
