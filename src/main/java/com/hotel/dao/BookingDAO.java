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
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false); // Start transaction

            try (PreparedStatement ps = con.prepareStatement(insertBookingSql)) {
                ps.setInt(1, booking.getCustomerId());
                ps.setInt(2, booking.getRoomId());
                ps.setDate(3, booking.getCheckIn());
                ps.setDate(4, booking.getCheckOut());
                ps.setInt(5, booking.getAdults());
                ps.setInt(6, booking.getChildren());
                ps.setString(7, "ACTIVE");
                
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    // Update room status
                    boolean roomUpdated = roomDAO.updateRoomStatus(con, booking.getRoomId(), "OCCUPIED");
                    if (roomUpdated) {
                        con.commit(); // Success
                        return true;
                    }
                }
                con.rollback(); // Failed
            }
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (con != null) con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                if (con != null) con.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
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
}
