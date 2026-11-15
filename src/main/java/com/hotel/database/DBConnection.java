package com.hotel.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import javax.swing.JOptionPane;

public class DBConnection {
    private static final Properties properties = new Properties();
    private static boolean driverLoaded = false;
    private static String loadError = null;

    static {
        loadConfiguration();
    }

    public static synchronized void loadConfiguration() {
        properties.clear();
        driverLoaded = false;
        loadError = null;

        // 1. Try loading from working directory or project paths first (allows editing db.properties without jar rebuild)
        File[] candidateFiles = new File[] {
            new File("db.properties"),
            new File("src/main/resources/db.properties"),
            new File("config/db.properties")
        };

        boolean loaded = false;
        for (File file : candidateFiles) {
            if (file.exists() && file.isFile()) {
                try (InputStream fis = new FileInputStream(file)) {
                    properties.load(fis);
                    loaded = true;
                    System.out.println("[DBConnection] Loaded database configuration from file: " + file.getAbsolutePath());
                    break;
                } catch (Exception e) {
                    System.err.println("[DBConnection] Warning: Could not read configuration from " + file.getPath() + ": " + e.getMessage());
                }
            }
        }

        // 2. Fall back to classpath resource
        if (!loaded) {
            try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (input != null) {
                    properties.load(input);
                    loaded = true;
                    System.out.println("[DBConnection] Loaded database configuration from classpath: db.properties");
                }
            } catch (Exception e) {
                System.err.println("[DBConnection] Warning: Could not read classpath db.properties: " + e.getMessage());
            }
        }

        if (!loaded) {
            loadError = "Unable to find db.properties file on filesystem or classpath.";
            System.err.println("[DBConnection] " + loadError);
            return;
        }

        // 3. Load JDBC Driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            driverLoaded = true;
        } catch (ClassNotFoundException ex) {
            loadError = "MySQL JDBC Driver (com.mysql.cj.jdbc.Driver) not found on classpath.\n" + ex.getMessage();
            System.err.println("[DBConnection] " + loadError);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (!driverLoaded || properties.isEmpty()) {
            loadConfiguration();
        }
        if (!driverLoaded) {
            throw new SQLException("Database driver not loaded: " + loadError);
        }

        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String password = properties.getProperty("db.password");

        if (url == null || url.trim().isEmpty()) {
            throw new SQLException("Database URL ('db.url') is not configured in db.properties.");
        }
        if (user == null || user.trim().isEmpty()) {
            user = "root";
        }
        if (password == null) {
            password = "";
        }

        try {
            return DriverManager.getConnection(url.trim(), user.trim(), password.trim());
        } catch (SQLException e) {
            // Log technical details safely to terminal WITHOUT displaying the password
            System.err.println("[DBConnection] Connection failed to URL [" + url.trim() + "] as user [" + user.trim()
                    + "] - Error Code: " + e.getErrorCode() + ", SQLState: " + e.getSQLState() + " - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Checks if a database connection can be established and verifies schema integrity.
     * Shows a user-friendly dialog on failure.
     * @return true if connection is successful, false otherwise
     */
    public static boolean testConnection() {
        try (Connection con = getConnection()) {
            if (con != null && !con.isClosed()) {
                // Verify schema and seed records safely
                DatabaseInitializer.initialize(con);
                return true;
            }
            return false;
        } catch (SQLException e) {
            int errorCode = e.getErrorCode();
            String errorMsg = e.getMessage() != null ? e.getMessage() : "";
            String advice;

            if (errorCode == 1045 || errorMsg.contains("Access denied")) {
                advice = "Authentication Error: Access denied for MySQL user.\n"
                        + "Please check the 'db.password' and 'db.user' settings in db.properties.";
            } else if (errorCode == 0 || errorMsg.contains("Communications link failure") || errorMsg.contains("Connection refused")) {
                advice = "Network / Service Error: Cannot connect to MySQL server on localhost:3306.\n"
                        + "Please ensure the MySQL service is started in Windows Services.";
            } else if (errorCode == 1049 || errorMsg.contains("Unknown database")) {
                advice = "Database Error: The database 'hotel_management' does not exist.\n"
                        + "Please execute database/hotel_management.sql to create it.";
            } else {
                advice = "Please check your MySQL server status and db.properties settings.";
            }

            JOptionPane.showMessageDialog(null,
                "Cannot connect to the database.\n\n"
                + advice + "\n\n"
                + "Technical Error (Code " + errorCode + "): " + errorMsg,
                "Database Connection Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}
