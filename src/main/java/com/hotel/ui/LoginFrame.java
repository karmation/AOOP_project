package com.hotel.ui;

import com.hotel.dao.UserDAO;
import com.hotel.database.DBConnection;
import com.hotel.exception.AuthenticationException;
import com.hotel.model.User;
import com.hotel.ui.theme.AppTheme;
import com.hotel.ui.theme.UIComponents;
import com.hotel.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

/**
 * Modern, split-panel login window for the Grand Hotel Management System.
 */
public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnExit;

    public LoginFrame() {
        setTitle("Grand Hotel Management System - Sign In");
        setSize(900, 580);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        initComponents();

        // Check database connection and verify schema on startup
        SwingUtilities.invokeLater(DBConnection::testConnection);
    }

    private void initComponents() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(AppTheme.BACKGROUND);

        // =====================================================================
        // 1. LEFT BRANDING PANEL (40% width, ~360px)
        // =====================================================================
        JPanel brandPanel = new JPanel(new BorderLayout());
        brandPanel.setPreferredSize(new Dimension(360, 580));
        brandPanel.setBackground(AppTheme.PRIMARY_NAVY);
        brandPanel.setBorder(new EmptyBorder(50, 40, 50, 40));

        // Brand Content
        JPanel brandContent = new JPanel();
        brandContent.setLayout(new BoxLayout(brandContent, BoxLayout.Y_AXIS));
        brandContent.setOpaque(false);

        JLabel lblBadge = new JLabel("★ ★ ★ ★ ★");
        lblBadge.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblBadge.setForeground(AppTheme.WARNING);
        lblBadge.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblHotel = new JLabel("GRAND HOTEL");
        lblHotel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblHotel.setForeground(Color.WHITE);
        lblHotel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Enterprise Management Suite");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(new Color(180, 195, 220));
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel divider = new JPanel();
        divider.setBackground(AppTheme.PRIMARY_BLUE);
        divider.setMaximumSize(new Dimension(80, 4));
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblFeature1 = new JLabel("✔  Instant Guest Check-in & Reservations");
        lblFeature1.setFont(AppTheme.FONT_BODY);
        lblFeature1.setForeground(new Color(220, 230, 245));
        lblFeature1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblFeature2 = new JLabel("✔  Live Room Availability & Visual Floor Cards");
        lblFeature2.setFont(AppTheme.FONT_BODY);
        lblFeature2.setForeground(new Color(220, 230, 245));
        lblFeature2.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblFeature3 = new JLabel("✔  Automated Invoicing & Service Billing");
        lblFeature3.setFont(AppTheme.FONT_BODY);
        lblFeature3.setForeground(new Color(220, 230, 245));
        lblFeature3.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblFeature4 = new JLabel("✔  Transactional Checkout & Revenue Analytics");
        lblFeature4.setFont(AppTheme.FONT_BODY);
        lblFeature4.setForeground(new Color(220, 230, 245));
        lblFeature4.setAlignmentX(Component.LEFT_ALIGNMENT);

        brandContent.add(lblBadge);
        brandContent.add(Box.createVerticalStrut(10));
        brandContent.add(lblHotel);
        brandContent.add(Box.createVerticalStrut(4));
        brandContent.add(lblSubtitle);
        brandContent.add(Box.createVerticalStrut(20));
        brandContent.add(divider);
        brandContent.add(Box.createVerticalStrut(35));
        brandContent.add(lblFeature1);
        brandContent.add(Box.createVerticalStrut(14));
        brandContent.add(lblFeature2);
        brandContent.add(Box.createVerticalStrut(14));
        brandContent.add(lblFeature3);
        brandContent.add(Box.createVerticalStrut(14));
        brandContent.add(lblFeature4);

        JLabel lblFooter = new JLabel("Secure Java Swing Desktop Edition • MySQL 26.7");
        lblFooter.setFont(AppTheme.FONT_SMALL);
        lblFooter.setForeground(new Color(130, 150, 180));

        brandPanel.add(brandContent, BorderLayout.CENTER);
        brandPanel.add(lblFooter, BorderLayout.SOUTH);

        // =====================================================================
        // 2. RIGHT FORM PANEL (60% width, ~540px)
        // =====================================================================
        JPanel formWrapper = new JPanel(new GridBagLayout());
        formWrapper.setBackground(AppTheme.BACKGROUND);

        // Centered Card Panel
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppTheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                new EmptyBorder(35, 45, 35, 45)));
        card.setPreferredSize(new Dimension(420, 460));

        JLabel lblHeading = new JLabel("Welcome Back");
        lblHeading.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblHeading.setForeground(AppTheme.TEXT_PRIMARY);
        lblHeading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel("Sign in with your staff or administrator account");
        lblSub.setFont(AppTheme.FONT_BODY);
        lblSub.setForeground(AppTheme.TEXT_SECONDARY);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Username Field
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(AppTheme.FONT_SUBHEADING);
        lblUser.setForeground(AppTheme.TEXT_PRIMARY);
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtUsername = UIComponents.createTextField();
        txtUsername.setMaximumSize(new Dimension(Short.MAX_VALUE, 38));
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtUsername.setText("admin");

        // Password Field
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(AppTheme.FONT_SUBHEADING);
        lblPass.setForeground(AppTheme.TEXT_PRIMARY);
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPassword = new JPasswordField("admin123");
        txtPassword.setFont(AppTheme.FONT_BODY);
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                new EmptyBorder(8, 8, 8, 8)));
        txtPassword.setMaximumSize(new Dimension(Short.MAX_VALUE, 38));
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Buttons
        btnLogin = UIComponents.createPrimaryButton("Sign In to Portal");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setMaximumSize(new Dimension(Short.MAX_VALUE, 42));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.addActionListener(this::handleLogin);

        // Enter key listeners
        txtUsername.addActionListener(e -> txtPassword.requestFocusInWindow());
        txtPassword.addActionListener(this::handleLogin);

        JLabel lblHint = new JLabel("Default Credentials: admin  /  admin123", SwingConstants.CENTER);
        lblHint.setFont(AppTheme.FONT_SMALL);
        lblHint.setForeground(AppTheme.TEXT_SECONDARY);
        lblHint.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnExit = new JButton("Exit Application");
        btnExit.setFont(AppTheme.FONT_SMALL);
        btnExit.setForeground(AppTheme.DANGER);
        btnExit.setBorderPainted(false);
        btnExit.setContentAreaFilled(false);
        btnExit.setFocusPainted(false);
        btnExit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnExit.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnExit.addActionListener(e -> System.exit(0));

        card.add(lblHeading);
        card.add(Box.createVerticalStrut(4));
        card.add(lblSub);
        card.add(Box.createVerticalStrut(24));
        card.add(lblUser);
        card.add(Box.createVerticalStrut(6));
        card.add(txtUsername);
        card.add(Box.createVerticalStrut(14));
        card.add(lblPass);
        card.add(Box.createVerticalStrut(6));
        card.add(txtPassword);
        card.add(Box.createVerticalStrut(24));
        card.add(btnLogin);
        card.add(Box.createVerticalStrut(16));
        card.add(lblHint);
        card.add(Box.createVerticalStrut(8));
        card.add(btnExit);

        formWrapper.add(card);

        rootPanel.add(brandPanel, BorderLayout.WEST);
        rootPanel.add(formWrapper, BorderLayout.CENTER);

        setContentPane(rootPanel);
    }

    private void handleLogin(ActionEvent e) {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (ValidationUtil.isEmpty(username) || ValidationUtil.isEmpty(password)) {
            UIComponents.showWarning(this, "Please enter both username and password.");
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Verifying Credentials...");

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                UserDAO userDAO = new UserDAO();
                return userDAO.authenticate(username, password);
            }

            @Override
            protected void done() {
                btnLogin.setEnabled(true);
                btnLogin.setText("Sign In to Portal");
                try {
                    User user = get();
                    if (user != null) {
                        dispose();
                        SwingUtilities.invokeLater(() -> {
                            DashboardFrame dashboard = new DashboardFrame(user);
                            dashboard.setVisible(true);
                        });
                    }
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    if (cause instanceof AuthenticationException) {
                        AuthenticationException authEx = (AuthenticationException) cause;
                        String hint = (authEx.getReason() == AuthenticationException.Reason.USER_NOT_FOUND)
                                ? "\n\nDefault credentials: admin / admin123"
                                : "\nPlease check your password and try again.";

                        JOptionPane.showMessageDialog(LoginFrame.this,
                                authEx.getMessage() + hint,
                                "Login Failed",
                                JOptionPane.WARNING_MESSAGE);
                        txtPassword.setText("");
                        txtPassword.requestFocus();
                    } else if (cause instanceof SQLException) {
                        SQLException sqlEx = (SQLException) cause;
                        int errorCode = sqlEx.getErrorCode();
                        String errorMsg = sqlEx.getMessage() != null ? sqlEx.getMessage() : "";

                        if (errorCode == 1146 || errorMsg.contains("doesn't exist")) {
                            JOptionPane.showMessageDialog(LoginFrame.this,
                                    "Database Schema Error:\n" + errorMsg
                                    + "\n\nThe 'users' table is missing. Please initialize the database schema.",
                                    "Missing Database Table",
                                    JOptionPane.ERROR_MESSAGE);
                        } else if (errorCode == 1045 || errorMsg.contains("Access denied")) {
                            JOptionPane.showMessageDialog(LoginFrame.this,
                                    "Database Authentication Error:\nAccess denied for user in db.properties.\n\n"
                                    + "Please verify your MySQL username and password.",
                                    "Database Connection Error",
                                    JOptionPane.ERROR_MESSAGE);
                        } else if (errorCode == 0 || errorMsg.contains("Communications link failure") || errorMsg.contains("Connection refused")) {
                            JOptionPane.showMessageDialog(LoginFrame.this,
                                    "Database Connection Failure:\nCannot connect to MySQL server on localhost:3306.\n\n"
                                    + "Please verify that the MySQL service is running.",
                                    "Database Connection Error",
                                    JOptionPane.ERROR_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(LoginFrame.this,
                                    "SQL Query Error (Code " + errorCode + "):\n" + errorMsg,
                                    "Database Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(LoginFrame.this,
                                "An unexpected error occurred during login:\n"
                                + (cause != null ? cause.getMessage() : ex.getMessage()),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LoginFrame.this,
                            "An unexpected error occurred:\n" + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
