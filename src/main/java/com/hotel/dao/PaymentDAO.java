package com.hotel.dao;

import com.hotel.database.DBConnection;
import com.hotel.model.Payment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PaymentDAO {

    public boolean processPayment(Payment payment, int roomId) {
        String insertPaymentSql = "INSERT INTO payments (booking_id, amount, payment_method) VALUES (?, ?, ?)";
        String updateBookingSql = "UPDATE bookings SET status='COMPLETED' WHERE booking_id=?";
        String updateRoomSql = "UPDATE rooms SET status='AVAILABLE' WHERE room_id=?";

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false); // Start transaction

            // 1. Insert Payment
            try (PreparedStatement ps1 = con.prepareStatement(insertPaymentSql)) {
                ps1.setInt(1, payment.getBookingId());
                ps1.setDouble(2, payment.getAmount());
                ps1.setString(3, payment.getPaymentMethod());
                
                if (ps1.executeUpdate() == 0) {
                    con.rollback();
                    return false;
                }
            }

            // 2. Update Booking Status
            try (PreparedStatement ps2 = con.prepareStatement(updateBookingSql)) {
                ps2.setInt(1, payment.getBookingId());
                
                if (ps2.executeUpdate() == 0) {
                    con.rollback();
                    return false;
                }
            }

            // 3. Update Room Status
            try (PreparedStatement ps3 = con.prepareStatement(updateRoomSql)) {
                ps3.setInt(1, roomId);
                
                if (ps3.executeUpdate() == 0) {
                    con.rollback();
                    return false;
                }
            }

            con.commit(); // Everything succeeded
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (con != null) con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (con != null) con.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public double getTotalRevenue() {
        String sql = "SELECT SUM(amount) FROM payments";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}
