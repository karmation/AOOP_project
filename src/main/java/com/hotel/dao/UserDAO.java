package com.hotel.dao;

import com.hotel.database.DBConnection;
import com.hotel.exception.AuthenticationException;
import com.hotel.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    /**
     * Authenticates a user with explicit distinction between invalid username,
     * invalid password, missing tables, and database connection errors.
     *
     * @param username The username to authenticate
     * @param password The plaintext password supplied by user
     * @return Authenticated User object if successful
     * @throws SQLException On database connection or query failure
     * @throws AuthenticationException On invalid username or password
     */
    public User authenticate(String username, String password) throws SQLException, AuthenticationException {
        if (username == null || username.trim().isEmpty()) {
            throw new AuthenticationException("Username cannot be empty.", AuthenticationException.Reason.USER_NOT_FOUND);
        }
        if (password == null) {
            password = "";
        }

        String sql = "SELECT user_id, username, password, role FROM users WHERE username = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.err.println("[UserDAO] Authentication failed: User '" + username.trim() + "' not found in database.");
                    throw new AuthenticationException("User account '" + username.trim() + "' does not exist.",
                            AuthenticationException.Reason.USER_NOT_FOUND);
                }

                String storedPassword = rs.getString("password");
                if (storedPassword == null || !storedPassword.equals(password)) {
                    System.err.println("[UserDAO] Authentication failed: Incorrect password for user '" + username.trim() + "'.");
                    throw new AuthenticationException("Incorrect password for user '" + username.trim() + "'.",
                            AuthenticationException.Reason.INVALID_PASSWORD);
                }

                System.out.println("[UserDAO] User '" + username.trim() + "' authenticated successfully with role: " + rs.getString("role"));
                return new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role")
                );
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Database error during authentication for user '" + username.trim() + "': "
                    + e.getMessage() + " [Error Code: " + e.getErrorCode() + ", SQLState: " + e.getSQLState() + "]");
            throw e;
        }
    }
}
