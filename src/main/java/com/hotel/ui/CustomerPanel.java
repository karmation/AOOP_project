package com.hotel.ui;

import com.hotel.dao.CustomerDAO;
import com.hotel.model.Customer;
import com.hotel.util.ValidationUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CustomerPanel extends JPanel {
    private CustomerDAO customerDAO = new CustomerDAO();
    private JTable table;
    private DefaultTableModel tableModel;
    
    private JTextField txtId, txtName, txtPhone, txtEmail, txtAddress, txtIdProof;

    public CustomerPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initComponents();
        loadData();
    }

    private void initComponents() {
        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Customer Management"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtId = new JTextField(15);
        txtId.setEditable(false);
        txtName = new JTextField(15);
        txtPhone = new JTextField(15);
        txtEmail = new JTextField(15);
        txtAddress = new JTextField(15);
        txtIdProof = new JTextField(15);

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Customer ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; formPanel.add(txtId, gbc);

        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; formPanel.add(txtName, gbc);

        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; formPanel.add(txtPhone, gbc);

        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; formPanel.add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy = 4; formPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; formPanel.add(txtAddress, gbc);

        gbc.gridx = 0; gbc.gridy = 5; formPanel.add(new JLabel("ID Proof (AADHAAR/PAN):"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; formPanel.add(txtIdProof, gbc);

        // Buttons
        JPanel btnPanel = new JPanel();
        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnClear = new JButton("Clear");

        btnAdd.addActionListener(e -> addCustomer());
        btnUpdate.addActionListener(e -> updateCustomer());
        btnDelete.addActionListener(e -> deleteCustomer());
        btnClear.addActionListener(e -> clearForm());

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        formPanel.add(btnPanel, gbc);

        add(formPanel, BorderLayout.WEST);

        // Table Panel
        String[] columns = {"ID", "Name", "Phone", "Email", "ID Proof"};
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
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Customer> customers = customerDAO.getAllCustomers();
        for (Customer c : customers) {
            tableModel.addRow(new Object[]{
                c.getCustomerId(), c.getName(), c.getPhone(), c.getEmail(), c.getIdProof()
            });
        }
    }

    private void populateForm() {
        int row = table.getSelectedRow();
        int id = (int) tableModel.getValueAt(row, 0);
        Customer c = customerDAO.getCustomerById(id);
        if (c != null) {
            txtId.setText(String.valueOf(c.getCustomerId()));
            txtName.setText(c.getName());
            txtPhone.setText(c.getPhone());
            txtEmail.setText(c.getEmail());
            txtAddress.setText(c.getAddress());
            txtIdProof.setText(c.getIdProof());
        }
    }

    private void clearForm() {
        txtId.setText("");
        txtName.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        txtAddress.setText("");
        txtIdProof.setText("");
        table.clearSelection();
    }

    private void addCustomer() {
        if (!validateInput()) return;
        
        Customer c = new Customer(0, txtName.getText(), txtPhone.getText(), 
                                  txtEmail.getText(), txtAddress.getText(), txtIdProof.getText());
                             
        if (customerDAO.addCustomer(c)) {
            JOptionPane.showMessageDialog(this, "Customer added successfully!");
            clearForm();
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add customer.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCustomer() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a customer to update.");
            return;
        }
        if (!validateInput()) return;

        Customer c = new Customer(Integer.parseInt(txtId.getText()), txtName.getText(), txtPhone.getText(), 
                                  txtEmail.getText(), txtAddress.getText(), txtIdProof.getText());
                             
        if (customerDAO.updateCustomer(c)) {
            JOptionPane.showMessageDialog(this, "Customer updated successfully!");
            clearForm();
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update customer.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCustomer() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a customer to delete.");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this customer?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            int id = Integer.parseInt(txtId.getText());
            if (customerDAO.deleteCustomer(id)) {
                JOptionPane.showMessageDialog(this, "Customer deleted successfully!");
                clearForm();
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete customer. Might have active bookings.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean validateInput() {
        if (ValidationUtil.isEmpty(txtName.getText())) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty.");
            return false;
        }
        if (!ValidationUtil.isValidPhone(txtPhone.getText())) {
            JOptionPane.showMessageDialog(this, "Phone must be exactly 10 digits.");
            return false;
        }
        if (!ValidationUtil.isEmpty(txtEmail.getText()) && !ValidationUtil.isValidEmail(txtEmail.getText())) {
            JOptionPane.showMessageDialog(this, "Invalid email format.");
            return false;
        }
        if (ValidationUtil.isEmpty(txtIdProof.getText())) {
            JOptionPane.showMessageDialog(this, "ID proof cannot be empty.");
            return false;
        }
        return true;
    }
}
