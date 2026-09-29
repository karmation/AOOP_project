package com.hotel.ui;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.ServiceDAO;
import com.hotel.model.Booking;
import com.hotel.model.CustomerService;
import com.hotel.model.Service;
import com.hotel.util.ValidationUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ServicePanel extends JPanel {
    private ServiceDAO serviceDAO = new ServiceDAO();
    private BookingDAO bookingDAO = new BookingDAO();
    
    private JTable table;
    private DefaultTableModel tableModel;
    
    private JTextField txtServiceId, txtServiceName, txtPrice;
    
    // Add to booking components
    private JComboBox<String> cbActiveBookings;
    private JComboBox<String> cbServices;
    private JTextField txtQuantity;
    private List<Booking> activeBookings;
    private List<Service> allServices;

    public ServicePanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initComponents();
        loadData();
    }

    private void initComponents() {
        JPanel leftPanel = new JPanel(new BorderLayout());
        
        // Form Panel (Manage Services)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Manage Services"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtServiceId = new JTextField(15);
        txtServiceId.setEditable(false);
        txtServiceName = new JTextField(15);
        txtPrice = new JTextField(15);

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Service ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; formPanel.add(txtServiceId, gbc);

        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Service Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; formPanel.add(txtServiceName, gbc);

        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Price:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; formPanel.add(txtPrice, gbc);

        // Buttons
        JPanel btnPanel = new JPanel();
        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");

        btnAdd.addActionListener(e -> addService());
        btnUpdate.addActionListener(e -> updateService());
        btnDelete.addActionListener(e -> deleteService());

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        formPanel.add(btnPanel, gbc);
        
        leftPanel.add(formPanel, BorderLayout.NORTH);
        
        // Add Service to Booking Panel
        JPanel addToBookingPanel = new JPanel(new GridBagLayout());
        addToBookingPanel.setBorder(BorderFactory.createTitledBorder("Add Service to Booking"));
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(5, 5, 5, 5);
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        
        cbActiveBookings = new JComboBox<>();
        cbServices = new JComboBox<>();
        txtQuantity = new JTextField("1", 5);
        
        gbc2.gridx = 0; gbc2.gridy = 0; addToBookingPanel.add(new JLabel("Active Booking:"), gbc2);
        gbc2.gridx = 1; gbc2.gridy = 0; addToBookingPanel.add(cbActiveBookings, gbc2);
        
        gbc2.gridx = 0; gbc2.gridy = 1; addToBookingPanel.add(new JLabel("Service:"), gbc2);
        gbc2.gridx = 1; gbc2.gridy = 1; addToBookingPanel.add(cbServices, gbc2);
        
        gbc2.gridx = 0; gbc2.gridy = 2; addToBookingPanel.add(new JLabel("Quantity:"), gbc2);
        gbc2.gridx = 1; gbc2.gridy = 2; addToBookingPanel.add(txtQuantity, gbc2);
        
        JButton btnAddToBooking = new JButton("Add Service to Booking");
        btnAddToBooking.addActionListener(e -> addServiceToBooking());
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> populateDropdowns());
        
        JPanel btnPanel2 = new JPanel();
        btnPanel2.add(btnAddToBooking);
        btnPanel2.add(btnRefresh);
        
        gbc2.gridx = 0; gbc2.gridy = 3; gbc2.gridwidth = 2;
        addToBookingPanel.add(btnPanel2, gbc2);
        
        leftPanel.add(addToBookingPanel, BorderLayout.CENTER);

        add(leftPanel, BorderLayout.WEST);

        // Table Panel
        String[] columns = {"ID", "Service Name", "Price"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                populateForm();
            }
        });
        
        add(new JScrollPane(table), BorderLayout.CENTER);
        
        populateDropdowns();
    }
    
    private void populateDropdowns() {
        cbActiveBookings.removeAllItems();
        activeBookings = bookingDAO.getActiveBookings();
        for (Booking b : activeBookings) {
            cbActiveBookings.addItem("ID: " + b.getBookingId() + " - " + b.getCustomer().getName() + " (Room " + b.getRoom().getRoomNumber() + ")");
        }
        
        cbServices.removeAllItems();
        allServices = serviceDAO.getAllServices();
        for (Service s : allServices) {
            cbServices.addItem(s.getServiceName() + " - ₹" + s.getPrice());
        }
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Service> services = serviceDAO.getAllServices();
        for (Service s : services) {
            tableModel.addRow(new Object[]{
                s.getServiceId(), s.getServiceName(), s.getPrice()
            });
        }
    }

    private void populateForm() {
        int row = table.getSelectedRow();
        txtServiceId.setText(tableModel.getValueAt(row, 0).toString());
        txtServiceName.setText(tableModel.getValueAt(row, 1).toString());
        txtPrice.setText(tableModel.getValueAt(row, 2).toString());
    }

    private void addService() {
        if (ValidationUtil.isEmpty(txtServiceName.getText()) || !ValidationUtil.isNumeric(txtPrice.getText())) {
            JOptionPane.showMessageDialog(this, "Valid name and price are required.");
            return;
        }
        Service s = new Service(0, txtServiceName.getText(), Double.parseDouble(txtPrice.getText()));
        if (serviceDAO.addService(s)) {
            JOptionPane.showMessageDialog(this, "Service added!");
            loadData();
            populateDropdowns();
        }
    }

    private void updateService() {
        if (txtServiceId.getText().isEmpty()) return;
        Service s = new Service(Integer.parseInt(txtServiceId.getText()), txtServiceName.getText(), Double.parseDouble(txtPrice.getText()));
        if (serviceDAO.updateService(s)) {
            JOptionPane.showMessageDialog(this, "Service updated!");
            loadData();
            populateDropdowns();
        }
    }

    private void deleteService() {
        if (txtServiceId.getText().isEmpty()) return;
        int id = Integer.parseInt(txtServiceId.getText());
        if (serviceDAO.deleteService(id)) {
            JOptionPane.showMessageDialog(this, "Service deleted!");
            loadData();
            populateDropdowns();
        }
    }
    
    private void addServiceToBooking() {
        if (cbActiveBookings.getSelectedIndex() == -1 || cbServices.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Please select an active booking and a service.");
            return;
        }
        if (!ValidationUtil.isNumeric(txtQuantity.getText())) {
            JOptionPane.showMessageDialog(this, "Valid quantity required.");
            return;
        }
        
        int bookingId = activeBookings.get(cbActiveBookings.getSelectedIndex()).getBookingId();
        int serviceId = allServices.get(cbServices.getSelectedIndex()).getServiceId();
        int qty = Integer.parseInt(txtQuantity.getText());
        
        CustomerService cs = new CustomerService(0, bookingId, serviceId, qty);
        if (serviceDAO.addServiceToBooking(cs)) {
            JOptionPane.showMessageDialog(this, "Service added to booking successfully!");
            txtQuantity.setText("1");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add service to booking.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
