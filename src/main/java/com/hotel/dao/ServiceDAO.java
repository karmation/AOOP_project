package com.hotel.dao;

import com.hotel.database.DBConnection;
import com.hotel.model.CustomerService;
import com.hotel.model.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDAO {

    public boolean addService(Service service) {
        String sql = "INSERT INTO services (service_name, price) VALUES (?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, service.getServiceName());
            ps.setDouble(2, service.getPrice());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateService(Service service) {
        String sql = "UPDATE services SET service_name=?, price=? WHERE service_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, service.getServiceName());
            ps.setDouble(2, service.getPrice());
            ps.setInt(3, service.getServiceId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteService(int serviceId) {
        String sql = "DELETE FROM services WHERE service_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, serviceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Service> getAllServices() {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT * FROM services";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                services.add(new Service(
                    rs.getInt("service_id"),
                    rs.getString("service_name"),
                    rs.getDouble("price")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return services;
    }

    // ----------------------------------------------------
    // Methods for adding services to a booking
    // ----------------------------------------------------

    public boolean addServiceToBooking(CustomerService cs) {
        String sql = "INSERT INTO customer_services (booking_id, service_id, quantity) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cs.getBookingId());
            ps.setInt(2, cs.getServiceId());
            ps.setInt(3, cs.getQuantity());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<CustomerService> getServicesForBooking(int bookingId) {
        List<CustomerService> services = new ArrayList<>();
        String sql = "SELECT cs.id, cs.booking_id, cs.service_id, cs.quantity, s.service_name, s.price " +
                     "FROM customer_services cs " +
                     "JOIN services s ON cs.service_id = s.service_id " +
                     "WHERE cs.booking_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CustomerService cs = new CustomerService(
                        rs.getInt("id"),
                        rs.getInt("booking_id"),
                        rs.getInt("service_id"),
                        rs.getInt("quantity")
                    );
                    cs.setServiceName(rs.getString("service_name"));
                    cs.setPrice(rs.getDouble("price"));
                    services.add(cs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return services;
    }
}
