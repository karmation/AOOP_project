package com.hotel.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

/**
 * Ensures required database tables, columns, and initial seed records exist.
 * Preserves all existing customer, booking, room, and payment data without dropping anything.
 */
public class DatabaseInitializer {

    public static void initialize(Connection con) {
        if (con == null) {
            return;
        }

        try {
            ensureTables(con);
            alignTableColumns(con);
            seedDefaultAdmin(con);
            seedDefaultServices(con);
            System.out.println("[DatabaseInitializer] Database schema and initial records verified successfully.");
        } catch (SQLException e) {
            System.err.println("[DatabaseInitializer] Warning during database initialization (Error Code "
                    + e.getErrorCode() + "): " + e.getMessage());
        }
    }

    private static void ensureTables(Connection con) throws SQLException {
        try (Statement stmt = con.createStatement()) {
            // 1. users table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users ("
                    + "user_id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "username VARCHAR(50) NOT NULL UNIQUE, "
                    + "password VARCHAR(255) NOT NULL, "
                    + "role VARCHAR(20) DEFAULT 'STAFF'"
                    + ")");

            // 2. rooms table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS rooms ("
                    + "room_id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "room_number VARCHAR(10) NOT NULL UNIQUE, "
                    + "room_type VARCHAR(20) NOT NULL, "
                    + "price_per_day DECIMAL(10,2) NOT NULL, "
                    + "status VARCHAR(20) DEFAULT 'AVAILABLE'"
                    + ")");

            // 3. customers table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS customers ("
                    + "customer_id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "name VARCHAR(100) NOT NULL, "
                    + "phone VARCHAR(20) NOT NULL, "
                    + "email VARCHAR(100), "
                    + "address TEXT, "
                    + "id_proof VARCHAR(50) NOT NULL DEFAULT 'ID-VERIFIED'"
                    + ")");

            // 4. bookings table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS bookings ("
                    + "booking_id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "customer_id INT NOT NULL, "
                    + "room_id INT NOT NULL, "
                    + "check_in DATE NOT NULL, "
                    + "check_out DATE NOT NULL, "
                    + "adults INT DEFAULT 1, "
                    + "children INT DEFAULT 0, "
                    + "status VARCHAR(20) DEFAULT 'ACTIVE', "
                    + "FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE, "
                    + "FOREIGN KEY (room_id) REFERENCES rooms(room_id)"
                    + ")");

            // 5. services table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS services ("
                    + "service_id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "service_name VARCHAR(100) NOT NULL UNIQUE, "
                    + "price DECIMAL(10,2) NOT NULL"
                    + ")");

            // 6. customer_services table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS customer_services ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "booking_id INT NOT NULL, "
                    + "service_id INT NOT NULL, "
                    + "quantity INT DEFAULT 1, "
                    + "FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE, "
                    + "FOREIGN KEY (service_id) REFERENCES services(service_id)"
                    + ")");

            // 7. payments table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS payments ("
                    + "payment_id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "booking_id INT NOT NULL, "
                    + "amount DECIMAL(10,2) NOT NULL, "
                    + "payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "payment_method VARCHAR(20) NOT NULL, "
                    + "FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE"
                    + ")");
        }
    }

    private static void alignTableColumns(Connection con) {
        try {
            DatabaseMetaData meta = con.getMetaData();
            String catalog = con.getCatalog();

            // Rooms: ensure room_number is VARCHAR(10) and price_per_day exists
            try (Statement stmt = con.createStatement()) {
                stmt.executeUpdate("ALTER TABLE rooms MODIFY COLUMN room_number VARCHAR(10) NOT NULL");
            } catch (SQLException ignored) {}

            Set<String> roomCols = getColumnNames(meta, catalog, "rooms");
            if (!roomCols.contains("price_per_day") && roomCols.contains("price")) {
                try (Statement stmt = con.createStatement()) {
                    stmt.executeUpdate("ALTER TABLE rooms CHANGE COLUMN price price_per_day DECIMAL(10,2) NOT NULL");
                    System.out.println("[DatabaseInitializer] Migrated column 'price' to 'price_per_day' in rooms table.");
                }
            }

            // Customers: ensure address and id_proof exist
            Set<String> custCols = getColumnNames(meta, catalog, "customers");
            if (!custCols.contains("address")) {
                try (Statement stmt = con.createStatement()) {
                    stmt.executeUpdate("ALTER TABLE customers ADD COLUMN address TEXT");
                    System.out.println("[DatabaseInitializer] Added column 'address' to customers table.");
                }
            }
            if (!custCols.contains("id_proof")) {
                try (Statement stmt = con.createStatement()) {
                    stmt.executeUpdate("ALTER TABLE customers ADD COLUMN id_proof VARCHAR(50) NOT NULL DEFAULT 'ID-VERIFIED'");
                    System.out.println("[DatabaseInitializer] Added column 'id_proof' to customers table.");
                }
            }

            // Bookings: ensure adults, children, status exist
            Set<String> bookCols = getColumnNames(meta, catalog, "bookings");
            if (!bookCols.contains("adults")) {
                try (Statement stmt = con.createStatement()) {
                    stmt.executeUpdate("ALTER TABLE bookings ADD COLUMN adults INT DEFAULT 1");
                    System.out.println("[DatabaseInitializer] Added column 'adults' to bookings table.");
                }
            }
            if (!bookCols.contains("children")) {
                try (Statement stmt = con.createStatement()) {
                    stmt.executeUpdate("ALTER TABLE bookings ADD COLUMN children INT DEFAULT 0");
                    System.out.println("[DatabaseInitializer] Added column 'children' to bookings table.");
                }
            }
            if (!bookCols.contains("status")) {
                try (Statement stmt = con.createStatement()) {
                    stmt.executeUpdate("ALTER TABLE bookings ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE'");
                    System.out.println("[DatabaseInitializer] Added column 'status' to bookings table.");
                }
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseInitializer] Column alignment notice: " + e.getMessage());
        }
    }

    private static Set<String> getColumnNames(DatabaseMetaData meta, String catalog, String tableName) throws SQLException {
        Set<String> cols = new HashSet<>();
        try (ResultSet rs = meta.getColumns(catalog, null, tableName, null)) {
            while (rs.next()) {
                cols.add(rs.getString("COLUMN_NAME").toLowerCase());
            }
        }
        return cols;
    }

    private static void seedDefaultAdmin(Connection con) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM users WHERE username = 'admin'";
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                String insertSql = "INSERT INTO users (username, password, role) VALUES ('admin', 'admin123', 'ADMIN')";
                stmt.executeUpdate(insertSql);
                System.out.println("[DatabaseInitializer] Default admin user ('admin' / 'admin123') inserted.");
            }
        }
    }

    private static void seedDefaultServices(Connection con) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM services";
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                String insertSql = "INSERT INTO services (service_name, price) VALUES "
                        + "('Breakfast', 300.00), "
                        + "('Lunch', 500.00), "
                        + "('Dinner', 500.00), "
                        + "('Laundry', 200.00), "
                        + "('Room Cleaning', 150.00), "
                        + "('Airport Pickup', 800.00)";
                stmt.executeUpdate(insertSql);
                System.out.println("[DatabaseInitializer] Default hotel services seeded.");
            }
        }
    }
}
