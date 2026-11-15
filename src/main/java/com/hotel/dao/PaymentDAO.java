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
        String updateBookingSql = "UPDATE bookings SET status='COMPLETED' WHERE booking_id=? AND status='ACTIVE'";
        String updateRoomSql = "UPDATE rooms SET status='AVAILABLE' WHERE room_id=?";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps1 = con.prepareStatement(insertPaymentSql)) {
                    ps1.setInt(1, payment.getBookingId());
                    ps1.setDouble(2, payment.getAmount());
                    ps1.setString(3, payment.getPaymentMethod());
                    if (ps1.executeUpdate() == 0) { con.rollback(); return false; }
                }
                try (PreparedStatement ps2 = con.prepareStatement(updateBookingSql)) {
                    ps2.setInt(1, payment.getBookingId());
                    if (ps2.executeUpdate() == 0) { con.rollback(); return false; }
                }
                try (PreparedStatement ps3 = con.prepareStatement(updateRoomSql)) {
                    ps3.setInt(1, roomId);
                    if (ps3.executeUpdate() == 0) { con.rollback(); return false; }
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

    // -------------------------------------------------------------------------
    // Extended query methods for Dashboard / Reports
    // -------------------------------------------------------------------------

    public double getRevenueByDateRange(java.sql.Date from, java.sql.Date to) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE DATE(payment_date) BETWEEN ? AND ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, from);
            ps.setDate(2, to);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    public java.util.List<com.hotel.model.Payment> getRecentPayments(int limit) {
        java.util.List<com.hotel.model.Payment> payments = new java.util.ArrayList<>();
        String sql = "SELECT * FROM payments ORDER BY payment_date DESC LIMIT ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    payments.add(new com.hotel.model.Payment(
                            rs.getInt("payment_id"),
                            rs.getInt("booking_id"),
                            rs.getDouble("amount"),
                            rs.getTimestamp("payment_date"),
                            rs.getString("payment_method")));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return payments;
    }

    public java.util.List<com.hotel.model.Payment> getAllPayments() {
        return getRecentPayments(1000);
    }
}
