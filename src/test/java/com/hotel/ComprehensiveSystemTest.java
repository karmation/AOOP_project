package com.hotel;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.CustomerDAO;
import com.hotel.dao.PaymentDAO;
import com.hotel.dao.RoomDAO;
import com.hotel.dao.ServiceDAO;
import com.hotel.dao.UserDAO;
import com.hotel.database.DBConnection;
import com.hotel.exception.AuthenticationException;
import com.hotel.model.Booking;
import com.hotel.model.Customer;
import com.hotel.model.Payment;
import com.hotel.model.Room;
import com.hotel.model.User;

import java.sql.Connection;
import java.sql.Date;
import java.util.List;

public class ComprehensiveSystemTest {

    public static void main(String[] args) {
        int passed = 0;
        int failed = 0;

        System.out.println("=================================================");
        System.out.println("RUNNING COMPREHENSIVE SYSTEM VERIFICATION TEST");
        System.out.println("=================================================");

        // TEST 1: Database Connection
        try {
            System.out.print("[TEST 1] Testing Database Connection and Schema Initialization... ");
            boolean connected = DBConnection.testConnection();
            if (!connected) throw new RuntimeException("testConnection() returned false");
            try (Connection con = DBConnection.getConnection()) {
                if (con == null || con.isClosed()) throw new RuntimeException("getConnection() returned null/closed");
            }
            System.out.println("PASSED");
            passed++;
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // TEST 2: Successful Admin Login
        try {
            System.out.print("[TEST 2] Testing Successful Admin Login (admin / admin123)... ");
            UserDAO userDAO = new UserDAO();
            User user = userDAO.authenticate("admin", "admin123");
            if (user == null || !"admin".equals(user.getUsername()) || !"ADMIN".equalsIgnoreCase(user.getRole())) {
                throw new RuntimeException("Expected authenticated admin user, got: " + user);
            }
            System.out.println("PASSED (User ID: " + user.getUserId() + ", Role: " + user.getRole() + ")");
            passed++;
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // TEST 3: Incorrect Password Rejection
        try {
            System.out.print("[TEST 3] Testing Incorrect Password Rejection... ");
            UserDAO userDAO = new UserDAO();
            try {
                userDAO.authenticate("admin", "wrongPassword999");
                System.out.println("FAILED (Should have thrown AuthenticationException)");
                failed++;
            } catch (AuthenticationException ae) {
                if (ae.getReason() == AuthenticationException.Reason.INVALID_PASSWORD) {
                    System.out.println("PASSED (Correctly rejected: " + ae.getMessage() + ")");
                    passed++;
                } else {
                    System.out.println("FAILED: Unexpected reason: " + ae.getReason());
                    failed++;
                }
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // TEST 4: Nonexistent Username Rejection
        try {
            System.out.print("[TEST 4] Testing Nonexistent Username Rejection... ");
            UserDAO userDAO = new UserDAO();
            try {
                userDAO.authenticate("ghost_user_12345", "anyPass");
                System.out.println("FAILED (Should have thrown AuthenticationException)");
                failed++;
            } catch (AuthenticationException ae) {
                if (ae.getReason() == AuthenticationException.Reason.USER_NOT_FOUND) {
                    System.out.println("PASSED (Correctly rejected: " + ae.getMessage() + ")");
                    passed++;
                } else {
                    System.out.println("FAILED: Unexpected reason: " + ae.getReason());
                    failed++;
                }
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // TEST 5: Customer Management (CRUD & Search)
        int testCustId = -1;
        try {
            System.out.print("[TEST 5] Testing Customer Management... ");
            CustomerDAO customerDAO = new CustomerDAO();
            List<Customer> initialCustomers = customerDAO.getAllCustomers();
            if (initialCustomers.isEmpty()) {
                throw new RuntimeException("Expected initial customers from seed, found none");
            }

            Customer newCust = new Customer(0, "Automated Test Guest", "9876500000", "testguest@hotel.com", "Test City, Tower A", "PASSPORT-7788");
            boolean added = customerDAO.addCustomer(newCust);
            if (!added) throw new RuntimeException("Failed to add test customer");

            List<Customer> searchResults = customerDAO.searchCustomers("Automated Test Guest");
            if (searchResults.isEmpty()) throw new RuntimeException("Could not find newly created customer");
            Customer created = searchResults.get(0);
            testCustId = created.getCustomerId();

            created.setAddress("Updated Suite 500");
            boolean updated = customerDAO.updateCustomer(created);
            if (!updated) throw new RuntimeException("Failed to update customer");

            System.out.println("PASSED (Initial customers: " + initialCustomers.size() + ", Test Cust ID: " + testCustId + ")");
            passed++;
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }

        // TEST 6: Room Management (CRUD & Status)
        int testRoomId = -1;
        try {
            System.out.print("[TEST 6] Testing Room Management... ");
            RoomDAO roomDAO = new RoomDAO();
            List<Room> allRooms = roomDAO.getAllRooms();
            if (allRooms.isEmpty()) throw new RuntimeException("Expected rooms in database");

            Room newRoom = new Room(0, "999TEST", "Deluxe", 4500.00, "AVAILABLE");
            boolean roomAdded = roomDAO.addRoom(newRoom);
            if (!roomAdded) throw new RuntimeException("Failed to add test room");

            List<Room> refreshedRooms = roomDAO.getAllRooms();
            for (Room r : refreshedRooms) {
                if ("999TEST".equals(r.getRoomNumber())) {
                    testRoomId = r.getRoomId();
                    break;
                }
            }
            if (testRoomId == -1) throw new RuntimeException("Could not find created test room");

            Room fetched = roomDAO.getRoomById(testRoomId);
            if (fetched == null || fetched.getPricePerDay() != 4500.00) {
                throw new RuntimeException("Room price or fetch failed");
            }

            System.out.println("PASSED (Total rooms: " + refreshedRooms.size() + ", Test Room ID: " + testRoomId + ")");
            passed++;
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }

        // TEST 7: Check-in and Booking
        int testBookingId = -1;
        try {
            System.out.print("[TEST 7] Testing Check-in and Booking creation... ");
            BookingDAO bookingDAO = new BookingDAO();
            RoomDAO roomDAO = new RoomDAO();

            long now = System.currentTimeMillis();
            Date checkIn = new Date(now);
            Date checkOut = new Date(now + 86400000L * 2); // 2 days

            Booking booking = new Booking(0, testCustId, testRoomId, checkIn, checkOut, 2, 0, "ACTIVE");
            boolean booked = bookingDAO.createBooking(booking);
            if (!booked) throw new RuntimeException("Failed to create booking");

            // Verify room is now OCCUPIED
            Room roomAfterBooking = roomDAO.getRoomById(testRoomId);
            if (!"OCCUPIED".equalsIgnoreCase(roomAfterBooking.getStatus())) {
                throw new RuntimeException("Room status was not updated to OCCUPIED: " + roomAfterBooking.getStatus());
            }

            List<Booking> activeBookings = bookingDAO.getActiveBookings();
            for (Booking b : activeBookings) {
                if (b.getRoomId() == testRoomId && b.getCustomerId() == testCustId) {
                    testBookingId = b.getBookingId();
                    break;
                }
            }
            if (testBookingId == -1) throw new RuntimeException("Active booking not found in active bookings list");

            System.out.println("PASSED (Booking ID: " + testBookingId + ", Room Status: OCCUPIED)");
            passed++;
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }

        // TEST 8 & 9: Check-out, Payment and Billing
        try {
            System.out.print("[TEST 8 & 9] Testing Check-out, Payment and Billing... ");
            PaymentDAO paymentDAO = new PaymentDAO();
            BookingDAO bookingDAO = new BookingDAO();
            RoomDAO roomDAO = new RoomDAO();

            double amount = 9000.00; // 2 days * 4500
            Payment payment = new Payment(0, testBookingId, amount, new java.sql.Timestamp(System.currentTimeMillis()), "CREDIT_CARD");

            boolean paymentProcessed = paymentDAO.processPayment(payment, testRoomId);
            if (!paymentProcessed) throw new RuntimeException("processPayment returned false");

            // Verify room status is back to AVAILABLE
            Room roomAfterCheckout = roomDAO.getRoomById(testRoomId);
            if (!"AVAILABLE".equalsIgnoreCase(roomAfterCheckout.getStatus())) {
                throw new RuntimeException("Room status not restored to AVAILABLE: " + roomAfterCheckout.getStatus());
            }

            // Verify booking status is COMPLETED
            Booking completedBooking = bookingDAO.getBookingById(testBookingId);
            if (!"COMPLETED".equalsIgnoreCase(completedBooking.getStatus())) {
                throw new RuntimeException("Booking status not set to COMPLETED: " + completedBooking.getStatus());
            }

            double totalRevenue = paymentDAO.getTotalRevenue();
            if (totalRevenue < amount) {
                throw new RuntimeException("Revenue query returned unexpected total: " + totalRevenue);
            }

            System.out.println("PASSED (Payment completed, Room returned to AVAILABLE, Total Revenue: " + totalRevenue + ")");
            passed += 2; // tests 8 & 9
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            e.printStackTrace();
            failed += 2;
        }

        // CLEANUP Test Records
        try {
            CustomerDAO customerDAO = new CustomerDAO();
            RoomDAO roomDAO = new RoomDAO();
            if (testRoomId != -1) {
                roomDAO.deleteRoom(testRoomId);
            }
            if (testCustId != -1) {
                // Booking history for testCustId will delete cascaded or prevent deletion
                customerDAO.deleteCustomer(testCustId);
            }
        } catch (Exception ignored) {}

        System.out.println("=================================================");
        System.out.println("TEST RESULTS: " + passed + " PASSED, " + failed + " FAILED");
        System.out.println("=================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
