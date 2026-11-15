package com.hotel.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents an authenticated staff or administrator user in the system.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private int userId;
    private String username;
    private String password;
    private String role; // "ADMIN", "STAFF"

    public User() {
    }

    public User(int userId, String username, String password, String role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = (role != null) ? role.toUpperCase() : "STAFF";
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Alias for username to support standard name-based callers.
     */
    public String getName() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = (role != null) ? role.toUpperCase() : "STAFF";
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userId == user.userId && Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username);
    }

    @Override
    public String toString() {
        return "User[id=" + userId + ", username=" + username + ", role=" + role + "]";
    }
}
