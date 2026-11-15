package com.hotel.ui;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.CustomerDAO;
import com.hotel.model.Booking;
import com.hotel.model.Customer;
import com.hotel.ui.theme.AppTheme;
import com.hotel.ui.theme.UIComponents;
import com.hotel.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern Guest & Customer Management Screen.
 * Features quick guest registration, phone & email validation, instant search,
 * and a direct Guest Booking History dialog.
 */
public class CustomerPanel extends JPanel {

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

    // Table & Sorting
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTextField txtSearch;

    // Form inputs
    private JTextField txtId;
    private JTextField txtName;
    private JTextField txtPhone;
    private JTextField txtEmail;
    private JTextField txtAddress;
    private JTextField txtIdProof;

    // Action buttons
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;
    private JButton btnViewHistory;

    private List<Customer> cachedCustomers = new ArrayList<>();

    public CustomerPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(AppTheme.BACKGROUND);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initComponents();
        loadCustomerData();
    }

    private void initComponents() {
        // =====================================================================
        // TOP: Header Bar & Search
        // =====================================================================
        JPanel topBar = new JPanel(new BorderLayout(12, 0));
        topBar.setOpaque(false);

        JPanel leftTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftTitle.setOpaque(false);
        leftTitle.add(UIComponents.createSectionHeader("Guest Directory & Registration"));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchPanel.setOpaque(false);

        txtSearch = UIComponents.createSearchField("Search by name or phone...");
        txtSearch.setPreferredSize(new Dimension(240, 34));
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) { applySearch(); }
        });

        btnViewHistory = UIComponents.createSecondaryButton("📜  View Stay History");
        btnViewHistory.addActionListener(e -> viewSelectedCustomerHistory());

        searchPanel.add(txtSearch);
        searchPanel.add(btnViewHistory);

        topBar.add(leftTitle, BorderLayout.WEST);
        topBar.add(searchPanel, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // =====================================================================
        // WEST: Customer Form Card
        // =====================================================================
        JPanel formCard = new JPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBackground(AppTheme.SURFACE);
        formCard.setPreferredSize(new Dimension(320, 600));
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel lblFormTitle = new JLabel("Guest Registration");
        lblFormTitle.setFont(AppTheme.FONT_HEADING);
        lblFormTitle.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel lblFormSub = new JLabel("Add or update guest identification details");
        lblFormSub.setFont(AppTheme.FONT_SMALL);
        lblFormSub.setForeground(AppTheme.TEXT_SECONDARY);

        txtId = UIComponents.createTextField();
        txtId.setEditable(false);
        txtId.setBackground(new Color(245, 247, 250));

        txtName = UIComponents.createTextField();
        txtPhone = UIComponents.createTextField();
        txtEmail = UIComponents.createTextField();
        txtAddress = UIComponents.createTextField();
        txtIdProof = UIComponents.createTextField();

        formCard.add(lblFormTitle);
        formCard.add(Box.createVerticalStrut(2));
        formCard.add(lblFormSub);
        formCard.add(Box.createVerticalStrut(16));

        addFormField(formCard, "Guest ID (Auto)", txtId);
        addFormField(formCard, "Full Name *", txtName);
        addFormField(formCard, "Phone Number *", txtPhone);
        addFormField(formCard, "Email Address", txtEmail);
        addFormField(formCard, "Postal Address", txtAddress);
        addFormField(formCard, "Govt ID (Passport / Aadhaar) *", txtIdProof);

        formCard.add(Box.createVerticalStrut(8));

        // Action Buttons (2x2)
        JPanel btnGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        btnGrid.setOpaque(false);
        btnGrid.setMaximumSize(new Dimension(Short.MAX_VALUE, 80));

        btnAdd = UIComponents.createPrimaryButton("+ Register");
        btnUpdate = UIComponents.createSecondaryButton("✎ Update");
        btnDelete = UIComponents.createDangerButton("🗑 Delete");
        btnClear = UIComponents.createSecondaryButton("✕ Clear");

        btnAdd.addActionListener(e -> addCustomer());
        btnUpdate.addActionListener(e -> updateCustomer());
        btnDelete.addActionListener(e -> deleteCustomer());
        btnClear.addActionListener(e -> clearForm());

        btnGrid.add(btnAdd);
        btnGrid.add(btnUpdate);
        btnGrid.add(btnDelete);
        btnGrid.add(btnClear);

        formCard.add(btnGrid);
        formCard.add(Box.createVerticalGlue());

        add(formCard, BorderLayout.WEST);

        // =====================================================================
        // CENTER: Customer Table
        // =====================================================================
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(AppTheme.SURFACE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                new EmptyBorder(10, 10, 10, 10)));

        String[] columns = {"ID", "Full Name", "Phone", "Email", "Address", "Government ID"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);
        UIComponents.applyTableStyle(table);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
                populateForm(modelRow);
            }
        });

        tableCard.add(UIComponents.createScrollPane(table), BorderLayout.CENTER);
        add(tableCard, BorderLayout.CENTER);
    }

    private void addFormField(JPanel parent, String labelText, JComponent field) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(AppTheme.FONT_SUBHEADING);
        lbl.setForeground(AppTheme.TEXT_PRIMARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(lbl);
        parent.add(Box.createVerticalStrut(3));

        field.setMaximumSize(new Dimension(Short.MAX_VALUE, 34));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(field);
        parent.add(Box.createVerticalStrut(8));
    }

    public void loadCustomerData() {
        SwingWorker<List<Customer>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Customer> doInBackground() {
                return customerDAO.getAllCustomers();
            }

            @Override
            protected void done() {
                try {
                    cachedCustomers = get();
                    tableModel.setRowCount(0);
                    for (Customer c : cachedCustomers) {
                        tableModel.addRow(new Object[]{
                                c.getCustomerId(),
                                c.getName(),
                                c.getPhone(),
                                c.getEmail() != null ? c.getEmail() : "-",
                                c.getAddress() != null ? c.getAddress() : "-",
                                c.getIdProof() != null ? c.getIdProof() : "-"
                        });
                    }
                    applySearch();
                } catch (Exception e) {
                    e.printStackTrace();
                    UIComponents.showError(CustomerPanel.this, "Error fetching customers: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    public void loadData() {
        loadCustomerData();
    }

    private void applySearch() {
        String query = txtSearch.getText().trim();
        if ("Search by name or phone...".equals(query)) query = "";

        if (query.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            // Filter on Name (col 1) or Phone (col 2)
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + query, 1, 2));
        }
    }

    private void populateForm(int modelRow) {
        txtId.setText(tableModel.getValueAt(modelRow, 0).toString());
        txtName.setText(tableModel.getValueAt(modelRow, 1).toString());
        txtPhone.setText(tableModel.getValueAt(modelRow, 2).toString());
        txtEmail.setText(tableModel.getValueAt(modelRow, 3).toString().replace("-", ""));
        txtAddress.setText(tableModel.getValueAt(modelRow, 4).toString().replace("-", ""));
        txtIdProof.setText(tableModel.getValueAt(modelRow, 5).toString().replace("-", ""));
    }

    private void clearForm() {
        txtId.setText("");
        txtName.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        txtAddress.setText("");
        txtIdProof.setText("");
        table.clearSelection();
        txtName.requestFocusInWindow();
    }

    private void addCustomer() {
        if (!validateInput()) return;

        // Check duplicate phone
        String phone = txtPhone.getText().trim();
        for (Customer c : cachedCustomers) {
            if (phone.equals(c.getPhone())) {
                UIComponents.showWarning(this, "A guest with phone number " + phone + " is already registered as '" + c.getName() + "'.");
                return;
            }
        }

        Customer customer = new Customer(0,
                txtName.getText().trim(),
                phone,
                txtEmail.getText().trim(),
                txtAddress.getText().trim(),
                txtIdProof.getText().trim());

        if (customerDAO.addCustomer(customer)) {
            UIComponents.showSuccess(this, "Guest '" + customer.getName() + "' registered successfully!");
            clearForm();
            loadCustomerData();
        } else {
            UIComponents.showError(this, "Failed to register guest. Please verify connection and fields.");
        }
    }

    private void updateCustomer() {
        if (txtId.getText().isEmpty()) {
            UIComponents.showWarning(this, "Please select a guest from the table to update.");
            return;
        }
        if (!validateInput()) return;

        Customer customer = new Customer(
                Integer.parseInt(txtId.getText()),
                txtName.getText().trim(),
                txtPhone.getText().trim(),
                txtEmail.getText().trim(),
                txtAddress.getText().trim(),
                txtIdProof.getText().trim());

        if (customerDAO.updateCustomer(customer)) {
            UIComponents.showSuccess(this, "Guest details updated successfully!");
            clearForm();
            loadCustomerData();
        } else {
            UIComponents.showError(this, "Failed to update guest details.");
        }
    }

    private void deleteCustomer() {
        if (txtId.getText().isEmpty()) {
            UIComponents.showWarning(this, "Please select a guest to delete.");
            return;
        }

        int id = Integer.parseInt(txtId.getText());
        String name = txtName.getText();

        if (UIComponents.confirm(this, "Are you sure you want to delete guest profile: " + name + "?", "Confirm Deletion")) {
            if (customerDAO.deleteCustomer(id)) {
                UIComponents.showSuccess(this, "Guest profile deleted successfully!");
                clearForm();
                loadCustomerData();
            } else {
                UIComponents.showError(this, "Cannot delete '" + name + "' because they have active or historical bookings.\nHotel business rules preserve booking audit records.");
            }
        }
    }

    private void viewSelectedCustomerHistory() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            UIComponents.showWarning(this, "Please select a guest from the table to view stay history.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        int custId = Integer.parseInt(tableModel.getValueAt(modelRow, 0).toString());
        String custName = tableModel.getValueAt(modelRow, 1).toString();

        List<Booking> bookings = bookingDAO.getBookingsByCustomerId(custId);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Stay History • " + custName, true);
        dialog.setSize(680, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(AppTheme.BACKGROUND);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Booking & Stay History for " + custName + " (ID: #" + custId + ")");
        title.setFont(AppTheme.FONT_HEADING);
        panel.add(title, BorderLayout.NORTH);

        String[] cols = {"Booking ID", "Room Number", "Check-In", "Check-Out", "Adults", "Children", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        for (Booking b : bookings) {
            String roomNum = b.getRoom() != null ? b.getRoom().getRoomNumber() : ("Room #" + b.getRoomId());
            model.addRow(new Object[]{
                    "#" + b.getBookingId(),
                    roomNum,
                    b.getCheckIn(),
                    b.getCheckOut(),
                    b.getAdults(),
                    b.getChildren(),
                    b.getStatus()
            });
        }

        JTable histTable = new JTable(model);
        UIComponents.applyTableStyle(histTable);
        panel.add(UIComponents.createScrollPane(histTable), BorderLayout.CENTER);

        JButton btnClose = UIComponents.createSecondaryButton("Close");
        btnClose.addActionListener(e -> dialog.dispose());
        JPanel btm = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btm.setOpaque(false);
        btm.add(btnClose);
        panel.add(btm, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private boolean validateInput() {
        if (ValidationUtil.isEmpty(txtName.getText())) {
            UIComponents.showWarning(this, "Full Name is required.");
            txtName.requestFocusInWindow();
            return false;
        }
        String phone = txtPhone.getText().trim();
        if (ValidationUtil.isEmpty(phone) || !ValidationUtil.isValidPhone(phone)) {
            UIComponents.showWarning(this, "Please enter a valid 10-digit phone number.");
            txtPhone.requestFocusInWindow();
            return false;
        }
        String email = txtEmail.getText().trim();
        if (!email.isEmpty() && !ValidationUtil.isValidEmail(email)) {
            UIComponents.showWarning(this, "Invalid email address format (e.g. name@domain.com).");
            txtEmail.requestFocusInWindow();
            return false;
        }
        if (ValidationUtil.isEmpty(txtIdProof.getText())) {
            UIComponents.showWarning(this, "Government ID proof is required for check-in compliance.");
            txtIdProof.requestFocusInWindow();
            return false;
        }
        return true;
    }
}
