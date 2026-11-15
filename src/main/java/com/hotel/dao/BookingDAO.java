package com.hotel.dao;

import com.hotel.database.DBConnection;
import com.hotel.model.Booking;
import com.hotel.model.Customer;
import com.hotel.model.Room;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    private RoomDAO roomDAO = new RoomDAO();

    public boolean createBooking(Booking booking) {
        String insertBookingSql = "INSERT INTO bookings (customer_id, room_id, check_in, check_out, adults, children, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String lockRoomSql = "SELECT status FROM rooms WHERE room_id=? FOR UPDATE";
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // Lock the room row so two users cannot book it simultaneously.
                try (PreparedStatement lock = con.prepareStatement(lockRoomSql)) {
                    lock.setInt(1, booking.getRoomId());
                    try (ResultSet rs = lock.executeQuery()) {
                        if (!rs.next() || !"AVAILABLE".equalsIgnoreCase(rs.getString("status"))) {
                            con.rollback();
                            return false;
                        }
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(insertBookingSql)) {
                    ps.setInt(1, booking.getCustomerId());
                    ps.setInt(2, booking.getRoomId());
                    ps.setDate(3, booking.getCheckIn());
                    ps.setDate(4, booking.getCheckOut());
                    ps.setInt(5, booking.getAdults());
                    ps.setInt(6, booking.getChildren());
                    ps.setString(7, "ACTIVE");
                    if (ps.executeUpdate() == 0) { con.rollback(); return false; }
                }
                if (!roomDAO.updateRoomStatus(con, booking.getRoomId(), "OCCUPIED")) {
                    con.rollback(); return false;
                }
                con.commit();
                return true;
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBookingStatus(int bookingId, String status) {
        String sql = "UPDATE bookings SET status=? WHERE booking_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Booking> getAllBookings() {
        return getBookingsByQuery("SELECT * FROM bookings ORDER BY booking_id DESC");
    }

    public List<Booking> getActiveBookings() {
        return getBookingsByQuery("SELECT * FROM bookings WHERE status = 'ACTIVE' ORDER BY check_in ASC");
    }
    
    public List<Booking> getBookingsByCustomerId(int customerId) {
        String sql = "SELECT * FROM bookings WHERE customer_id = ? ORDER BY booking_id DESC";
        List<Booking> bookings = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    bookings.add(mapRowToBooking(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    public Booking getBookingById(int bookingId) {
        String sql = "SELECT * FROM bookings WHERE booking_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToBooking(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Booking> getBookingsByQuery(String sql) {
        List<Booking> bookings = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                bookings.add(mapRowToBooking(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    private Booking mapRowToBooking(ResultSet rs) throws SQLException {
        Booking booking = new Booking(
            rs.getInt("booking_id"),
            rs.getInt("customer_id"),
            rs.getInt("room_id"),
            rs.getDate("check_in"),
            rs.getDate("check_out"),
            rs.getInt("adults"),
            rs.getInt("children"),
            rs.getString("status")
        );
        
        // Populate customer and room details for UI
        CustomerDAO customerDAO = new CustomerDAO();
        Customer customer = customerDAO.getCustomerById(booking.getCustomerId());
        booking.setCustomer(customer);
        
        Room room = roomDAO.getRoomById(booking.getRoomId());
        booking.setRoom(room);
        
        return booking;
    }
    
    // Aggregate queries for Dashboard/Reports
    public int getTotalBookings() {
        return getCount("SELECT COUNT(*) FROM bookings");
    }
    
    public int getActiveBookingsCount() {
        return getCount("SELECT COUNT(*) FROM bookings WHERE status = 'ACTIVE'");
    }
    
    private int getCount(String sql) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // -------------------------------------------------------------------------
    // Extended query methods for Dashboard / Reports
    // -------------------------------------------------------------------------

    public List<Booking> getTodayCheckIns() {
        return getBookingsByQuery("SELECT * FROM bookings WHERE check_in = CURDATE() AND status = 'ACTIVE' ORDER BY booking_id DESC");
    }

    public List<Booking> getTodayCheckOuts() {
        return getBookingsByQuery("SELECT * FROM bookings WHERE check_out = CURDATE() AND status = 'ACTIVE' ORDER BY booking_id DESC");
    }

    public List<Booking> getRecentBookings() {
        return getBookingsByQuery("SELECT * FROM bookings ORDER BY booking_id DESC LIMIT 10");
    }

    public List<Booking> getBookingsByStatus(String status) {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE status = ? ORDER BY booking_id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) bookings.add(mapRowToBooking(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return bookings;
    }
}
