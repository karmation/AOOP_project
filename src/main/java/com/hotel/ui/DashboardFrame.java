package com.hotel.ui;

import com.hotel.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class DashboardFrame extends JFrame {
    private User currentUser;
    private JPanel cardPanel;
    private CardLayout cardLayout;

    public DashboardFrame(User user) {
        this.currentUser = user;
        
        setTitle("Hotel Management System - Dashboard (" + user.getRole() + ")");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel titleLabel = new JLabel("Grand Hotel Management System");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        JLabel userLabel = new JLabel("Welcome, " + currentUser.getUsername());
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(userLabel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);

        // Sidebar Navigation
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(new Color(52, 73, 94));
        sidebarPanel.setPreferredSize(new Dimension(200, getHeight()));
        
        // Main Content Area
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        
        // Add Panels
        cardPanel.add(new RoomPanel(), "Rooms");
        cardPanel.add(new CustomerPanel(), "Customers");
        cardPanel.add(new BookingPanel(), "Bookings");
        cardPanel.add(new ServicePanel(), "Services");
        cardPanel.add(new BillingPanel(), "Billing");
        cardPanel.add(new ReportsPanel(), "Reports");

        // Sidebar Buttons
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        sidebarPanel.add(createNavButton("Rooms", "Rooms"));
        sidebarPanel.add(createNavButton("Customers", "Customers"));
        sidebarPanel.add(createNavButton("Bookings", "Bookings"));
        sidebarPanel.add(createNavButton("Services", "Services"));
        sidebarPanel.add(createNavButton("Billing", "Billing"));
        sidebarPanel.add(createNavButton("Reports", "Reports"));
        
        sidebarPanel.add(Box.createVerticalGlue());
        
        JButton btnLogout = new JButton("Logout");
        btnLogout.setMaximumSize(new Dimension(180, 40));
        btnLogout.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogout.addActionListener(e -> {
            this.dispose();
            new LoginFrame().setVisible(true);
        });
        sidebarPanel.add(btnLogout);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        add(sidebarPanel, BorderLayout.WEST);
        add(cardPanel, BorderLayout.CENTER);
    }

    private JButton createNavButton(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(180, 40));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        
        btn.addActionListener((ActionEvent e) -> {
            cardLayout.show(cardPanel, cardName);
        });
        
        return btn;
    }
}
