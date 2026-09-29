package com.hotel.ui;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.CustomerDAO;
import com.hotel.dao.PaymentDAO;
import com.hotel.dao.RoomDAO;

import javax.swing.*;
import java.awt.*;

public class ReportsPanel extends JPanel {
    
    private BookingDAO bookingDAO = new BookingDAO();
    private CustomerDAO customerDAO = new CustomerDAO();
    private PaymentDAO paymentDAO = new PaymentDAO();
    private RoomDAO roomDAO = new RoomDAO();

    public ReportsPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
    }

    private void initComponents() {
        JPanel gridPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        
        // 1. Revenue Report
        gridPanel.add(createReportCard("Total Revenue", "₹" + String.format("%.2f", paymentDAO.getTotalRevenue()), new Color(46, 204, 113)));
        
        // 2. Active Bookings
        gridPanel.add(createReportCard("Active Bookings", String.valueOf(bookingDAO.getActiveBookingsCount()), new Color(52, 152, 219)));
        
        // 3. Available Rooms
        gridPanel.add(createReportCard("Available Rooms", String.valueOf(roomDAO.getAvailableRooms().size()), new Color(155, 89, 182)));
        
        // 4. Total Customers
        gridPanel.add(createReportCard("Total Customers", String.valueOf(customerDAO.getAllCustomers().size()), new Color(241, 196, 15)));
        
        add(gridPanel, BorderLayout.CENTER);
        
        JButton btnRefresh = new JButton("Refresh Data");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnRefresh.addActionListener(e -> {
            removeAll();
            initComponents();
            revalidate();
            repaint();
        });
        
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(btnRefresh);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createReportCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        
        JLabel lblValue = new JLabel(value, SwingConstants.CENTER);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblValue.setForeground(Color.WHITE);
        
        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        
        return card;
    }
}
