package com.hotel.ui;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.PaymentDAO;
import com.hotel.dao.ServiceDAO;
import com.hotel.model.Booking;
import com.hotel.model.CustomerService;
import com.hotel.model.Payment;
import com.hotel.util.BillGenerator;

import javax.swing.*;
import java.awt.*;
import java.sql.Timestamp;
import java.util.List;

public class BillingPanel extends JPanel {
    private BookingDAO bookingDAO = new BookingDAO();
    private ServiceDAO serviceDAO = new ServiceDAO();
    private PaymentDAO paymentDAO = new PaymentDAO();
    
    private JComboBox<String> cbActiveBookings;
    private JTextArea txtBillView;
    private JComboBox<String> cbPaymentMethod;
    private JLabel lblTotalAmount;
    
    private List<Booking> activeBookings;
    private double currentTotal = 0.0;
    private Booking currentBooking = null;

    public BillingPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initComponents();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createTitledBorder("Select Booking to Checkout"));
        
        cbActiveBookings = new JComboBox<>();
        cbActiveBookings.setPreferredSize(new Dimension(300, 30));
        
        JButton btnGenerate = new JButton("Generate Bill");
        btnGenerate.addActionListener(e -> generateBill());
        
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadActiveBookings());
        
        topPanel.add(new JLabel("Active Booking: "));
        topPanel.add(cbActiveBookings);
        topPanel.add(btnGenerate);
        topPanel.add(btnRefresh);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Bill View
        txtBillView = new JTextArea();
        txtBillView.setEditable(false);
        txtBillView.setFont(new Font("Monospaced", Font.PLAIN, 14));
        add(new JScrollPane(txtBillView), BorderLayout.CENTER);
        
        // Payment Panel
        JPanel paymentPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        paymentPanel.setBorder(BorderFactory.createTitledBorder("Payment"));
        
        lblTotalAmount = new JLabel("Total: ₹0.00");
        lblTotalAmount.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        cbPaymentMethod = new JComboBox<>(new String[]{"Cash", "Card", "UPI"});
        
        JButton btnPay = new JButton("Process Payment & Checkout");
        btnPay.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnPay.setBackground(new Color(46, 204, 113));
        btnPay.setForeground(Color.WHITE);
        btnPay.addActionListener(e -> processPayment());
        
        paymentPanel.add(lblTotalAmount);
        paymentPanel.add(Box.createHorizontalStrut(20));
        paymentPanel.add(new JLabel("Method:"));
        paymentPanel.add(cbPaymentMethod);
        paymentPanel.add(Box.createHorizontalStrut(10));
        paymentPanel.add(btnPay);
        
        add(paymentPanel, BorderLayout.SOUTH);
        
        loadActiveBookings();
    }
    
    private void loadActiveBookings() {
        cbActiveBookings.removeAllItems();
        activeBookings = bookingDAO.getActiveBookings();
        for (Booking b : activeBookings) {
            cbActiveBookings.addItem("ID: " + b.getBookingId() + " - " + b.getCustomer().getName() + " (Room " + b.getRoom().getRoomNumber() + ")");
        }
        txtBillView.setText("");
        lblTotalAmount.setText("Total: ₹0.00");
        currentBooking = null;
    }
    
    private void generateBill() {
        if (cbActiveBookings.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Please select an active booking.");
            return;
        }
        
        currentBooking = activeBookings.get(cbActiveBookings.getSelectedIndex());
        List<CustomerService> services = serviceDAO.getServicesForBooking(currentBooking.getBookingId());
        
        String billText = BillGenerator.generateBill(currentBooking, services);
        txtBillView.setText(billText);
        
        currentTotal = BillGenerator.calculateTotal(currentBooking, services);
        lblTotalAmount.setText(String.format("Total: ₹%.2f", currentTotal));
    }
    
    private void processPayment() {
        if (currentBooking == null || currentTotal <= 0) {
            JOptionPane.showMessageDialog(this, "Please generate a bill first.");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Process payment of ₹" + currentTotal + " and complete checkout?", "Confirm Checkout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Payment payment = new Payment(0, currentBooking.getBookingId(), currentTotal, 
                                          new Timestamp(System.currentTimeMillis()), 
                                          cbPaymentMethod.getSelectedItem().toString());
                                          
            if (paymentDAO.processPayment(payment, currentBooking.getRoomId())) {
                JOptionPane.showMessageDialog(this, "Payment successful! Booking completed and room checked out.");
                loadActiveBookings();
            } else {
                JOptionPane.showMessageDialog(this, "Payment failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
