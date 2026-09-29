package com.hotel.ui;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.CustomerDAO;
import com.hotel.dao.RoomDAO;
import com.hotel.model.Booking;
import com.hotel.model.Customer;
import com.hotel.model.Room;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

public class BookingPanel extends JPanel {
    private BookingDAO bookingDAO = new BookingDAO();
    private CustomerDAO customerDAO = new CustomerDAO();
    private RoomDAO roomDAO = new RoomDAO();
    
    private JTable table;
    private DefaultTableModel tableModel;
    
    private JComboBox<String> cbCustomer;
    private JComboBox<String> cbRoom;
    private JTextField txtCheckIn, txtCheckOut, txtAdults, txtChildren;
    
    private List<Customer> customersList;
    private List<Room> roomsList;

    public BookingPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initComponents();
        loadData();
    }

    private void initComponents() {
        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Booking Management"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cbCustomer = new JComboBox<>();
        cbRoom = new JComboBox<>();
        txtCheckIn = new JTextField("YYYY-MM-DD", 10);
        txtCheckOut = new JTextField("YYYY-MM-DD", 10);
        txtAdults = new JTextField("1", 5);
        txtChildren = new JTextField("0", 5);

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Customer:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; formPanel.add(cbCustomer, gbc);

        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Available Room:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; formPanel.add(cbRoom, gbc);

        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Check-in (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; formPanel.add(txtCheckIn, gbc);

        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Check-out (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; formPanel.add(txtCheckOut, gbc);

        gbc.gridx = 0; gbc.gridy = 4; formPanel.add(new JLabel("Adults:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; formPanel.add(txtAdults, gbc);

        gbc.gridx = 0; gbc.gridy = 5; formPanel.add(new JLabel("Children:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; formPanel.add(txtChildren, gbc);

        // Buttons
        JPanel btnPanel = new JPanel();
        JButton btnAdd = new JButton("Create Booking");
        JButton btnCancel = new JButton("Cancel Booking");
        JButton btnRefresh = new JButton("Refresh Dropdowns");

        btnAdd.addActionListener(e -> createBooking());
        btnCancel.addActionListener(e -> cancelBooking());
        btnRefresh.addActionListener(e -> populateDropdowns());

        btnPanel.add(btnAdd);
        btnPanel.add(btnCancel);
        btnPanel.add(btnRefresh);

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        formPanel.add(btnPanel, gbc);

        add(formPanel, BorderLayout.WEST);

        // Table Panel
        String[] columns = {"ID", "Customer", "Room", "Check-in", "Check-out", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        
        add(new JScrollPane(table), BorderLayout.CENTER);
        
        populateDropdowns();
    }
    
    private void populateDropdowns() {
        cbCustomer.removeAllItems();
        customersList = customerDAO.getAllCustomers();
        for (Customer c : customersList) {
            cbCustomer.addItem(c.getCustomerId() + " - " + c.getName());
        }
        
        cbRoom.removeAllItems();
        roomsList = roomDAO.getAvailableRooms();
        for (Room r : roomsList) {
            cbRoom.addItem(r.getRoomId() + " - " + r.getRoomNumber() + " (" + r.getRoomType() + ") - ₹" + r.getPricePerDay());
        }
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Booking> bookings = bookingDAO.getAllBookings();
        for (Booking b : bookings) {
            String customerName = b.getCustomer() != null ? b.getCustomer().getName() : "Unknown";
            String roomNumber = b.getRoom() != null ? b.getRoom().getRoomNumber() : "Unknown";
            tableModel.addRow(new Object[]{
                b.getBookingId(), customerName, roomNumber, b.getCheckIn(), b.getCheckOut(), b.getStatus()
            });
        }
    }

    private void createBooking() {
        if (cbCustomer.getSelectedIndex() == -1 || cbRoom.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Please select a customer and a room.");
            return;
        }

        try {
            int customerId = customersList.get(cbCustomer.getSelectedIndex()).getCustomerId();
            int roomId = roomsList.get(cbRoom.getSelectedIndex()).getRoomId();
            
            Date checkIn = Date.valueOf(txtCheckIn.getText());
            Date checkOut = Date.valueOf(txtCheckOut.getText());
            
            if (checkOut.before(checkIn) || checkOut.equals(checkIn)) {
                JOptionPane.showMessageDialog(this, "Check-out date must be after check-in date.");
                return;
            }
            
            int adults = Integer.parseInt(txtAdults.getText());
            int children = Integer.parseInt(txtChildren.getText());

            Booking booking = new Booking(0, customerId, roomId, checkIn, checkOut, adults, children, "ACTIVE");
            
            if (bookingDAO.createBooking(booking)) {
                JOptionPane.showMessageDialog(this, "Booking created successfully!");
                populateDropdowns();
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create booking.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD or ensure numbers are valid.");
        }
    }
    
    private void cancelBooking() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to cancel.");
            return;
        }
        
        int bookingId = (int) tableModel.getValueAt(row, 0);
        String status = (String) tableModel.getValueAt(row, 5);
        
        if (!"ACTIVE".equals(status)) {
            JOptionPane.showMessageDialog(this, "Only ACTIVE bookings can be cancelled.");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel this booking?", "Confirm Cancel", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (bookingDAO.updateBookingStatus(bookingId, "CANCELLED")) {
                // Free up the room
                Booking b = bookingDAO.getBookingById(bookingId);
                try {
                    roomDAO.updateRoomStatus(com.hotel.database.DBConnection.getConnection(), b.getRoomId(), "AVAILABLE");
                    JOptionPane.showMessageDialog(this, "Booking cancelled successfully.");
                    loadData();
                    populateDropdowns();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
