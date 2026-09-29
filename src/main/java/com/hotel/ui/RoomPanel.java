package com.hotel.ui;

import com.hotel.dao.RoomDAO;
import com.hotel.model.Room;
import com.hotel.util.ValidationUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class RoomPanel extends JPanel {
    private RoomDAO roomDAO = new RoomDAO();
    private JTable table;
    private DefaultTableModel tableModel;
    
    private JTextField txtRoomId, txtRoomNumber, txtPrice;
    private JComboBox<String> cbRoomType, cbStatus;

    public RoomPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initComponents();
        loadData();
    }

    private void initComponents() {
        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Room Management"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtRoomId = new JTextField(15);
        txtRoomId.setEditable(false);
        txtRoomNumber = new JTextField(15);
        txtPrice = new JTextField(15);
        
        cbRoomType = new JComboBox<>(new String[]{"Single", "Double", "Deluxe", "Suite"});
        cbStatus = new JComboBox<>(new String[]{"AVAILABLE", "OCCUPIED", "MAINTENANCE"});

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Room ID (Auto):"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; formPanel.add(txtRoomId, gbc);

        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Room Number:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; formPanel.add(txtRoomNumber, gbc);

        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Room Type:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; formPanel.add(cbRoomType, gbc);

        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Price Per Day:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; formPanel.add(txtPrice, gbc);

        gbc.gridx = 0; gbc.gridy = 4; formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; formPanel.add(cbStatus, gbc);

        // Buttons
        JPanel btnPanel = new JPanel();
        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnClear = new JButton("Clear");

        btnAdd.addActionListener(e -> addRoom());
        btnUpdate.addActionListener(e -> updateRoom());
        btnDelete.addActionListener(e -> deleteRoom());
        btnClear.addActionListener(e -> clearForm());

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        formPanel.add(btnPanel, gbc);

        add(formPanel, BorderLayout.WEST);

        // Table Panel
        String[] columns = {"ID", "Room Number", "Type", "Price", "Status"};
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
        List<Room> rooms = roomDAO.getAllRooms();
        for (Room r : rooms) {
            tableModel.addRow(new Object[]{
                r.getRoomId(), r.getRoomNumber(), r.getRoomType(), r.getPricePerDay(), r.getStatus()
            });
        }
    }

    private void populateForm() {
        int row = table.getSelectedRow();
        txtRoomId.setText(tableModel.getValueAt(row, 0).toString());
        txtRoomNumber.setText(tableModel.getValueAt(row, 1).toString());
        cbRoomType.setSelectedItem(tableModel.getValueAt(row, 2).toString());
        txtPrice.setText(tableModel.getValueAt(row, 3).toString());
        cbStatus.setSelectedItem(tableModel.getValueAt(row, 4).toString());
    }

    private void clearForm() {
        txtRoomId.setText("");
        txtRoomNumber.setText("");
        txtPrice.setText("");
        cbRoomType.setSelectedIndex(0);
        cbStatus.setSelectedIndex(0);
        table.clearSelection();
    }

    private void addRoom() {
        if (!validateInput()) return;
        
        Room room = new Room(0, txtRoomNumber.getText(), cbRoomType.getSelectedItem().toString(),
                             Double.parseDouble(txtPrice.getText()), cbStatus.getSelectedItem().toString());
                             
        if (roomDAO.addRoom(room)) {
            JOptionPane.showMessageDialog(this, "Room added successfully!");
            clearForm();
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add room. Room number may already exist.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateRoom() {
        if (txtRoomId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a room to update.");
            return;
        }
        if (!validateInput()) return;

        Room room = new Room(Integer.parseInt(txtRoomId.getText()), txtRoomNumber.getText(),
                             cbRoomType.getSelectedItem().toString(), Double.parseDouble(txtPrice.getText()),
                             cbStatus.getSelectedItem().toString());
                             
        if (roomDAO.updateRoom(room)) {
            JOptionPane.showMessageDialog(this, "Room updated successfully!");
            clearForm();
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update room.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteRoom() {
        if (txtRoomId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a room to delete.");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this room?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            int id = Integer.parseInt(txtRoomId.getText());
            if (roomDAO.deleteRoom(id)) {
                JOptionPane.showMessageDialog(this, "Room deleted successfully!");
                clearForm();
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete room. It might be referenced in bookings.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean validateInput() {
        if (ValidationUtil.isEmpty(txtRoomNumber.getText())) {
            JOptionPane.showMessageDialog(this, "Room number cannot be empty.");
            return false;
        }
        if (!ValidationUtil.isNumeric(txtPrice.getText())) {
            JOptionPane.showMessageDialog(this, "Price must be a valid number.");
            return false;
        }
        return true;
    }
}
