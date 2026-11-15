package com.hotel.dao;

import com.hotel.database.DBConnection;
import com.hotel.model.Room;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO implements GenericDAO<Room> {

    public boolean addRoom(Room room) {
        String sql = "INSERT INTO rooms (room_number, room_type, price_per_day, status) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, room.getRoomNumber());
            ps.setString(2, room.getRoomType());
            ps.setDouble(3, room.getPricePerDay());
            ps.setString(4, room.getStatus());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateRoom(Room room) {
        String sql = "UPDATE rooms SET room_number=?, room_type=?, price_per_day=?, status=? WHERE room_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, room.getRoomNumber());
            ps.setString(2, room.getRoomType());
            ps.setDouble(3, room.getPricePerDay());
            ps.setString(4, room.getStatus());
            ps.setInt(5, room.getRoomId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteRoom(int roomId) {
        String sql = "DELETE FROM rooms WHERE room_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Room> getAllRooms() {
        return getRoomsByQuery("SELECT * FROM rooms");
    }

    public List<Room> getAvailableRooms() {
        return getRoomsByQuery("SELECT * FROM rooms WHERE status = 'AVAILABLE'");
    }

    public List<Room> getOccupiedRooms() {
        return getRoomsByQuery("SELECT * FROM rooms WHERE status = 'OCCUPIED'");
    }

    public Room getRoomById(int roomId) {
        String sql = "SELECT * FROM rooms WHERE room_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToRoom(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateRoomStatus(Connection con, int roomId, String status) throws SQLException {
        String sql = "UPDATE rooms SET status=? WHERE room_id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, roomId);
            return ps.executeUpdate() > 0;
        }
    }

    private List<Room> getRoomsByQuery(String sql) {
        List<Room> rooms = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rooms.add(mapRowToRoom(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    private Room mapRowToRoom(ResultSet rs) throws SQLException {
        return new Room(
            rs.getInt("room_id"),
            rs.getString("room_number"),
            rs.getString("room_type"),
            rs.getDouble("price_per_day"),
            rs.getString("status")
        );
    }

    // GenericDAO interface implementation; existing named methods remain available to the UI.
    @Override public boolean add(Room item) { return addRoom(item); }
    @Override public boolean update(Room item) { return updateRoom(item); }
    @Override public boolean delete(int id) { return deleteRoom(id); }
    @Override public List<Room> getAll() { return getAllRooms(); }

    // -------------------------------------------------------------------------
    // Extended aggregate methods for Dashboard / Reports
    // -------------------------------------------------------------------------

    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM rooms WHERE UPPER(status) = UPPER(?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int getTotalRooms() {
        String sql = "SELECT COUNT(*) FROM rooms";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

}
