package com.hotel.ui;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.RoomDAO;
import com.hotel.model.Room;
import com.hotel.ui.theme.AppTheme;
import com.hotel.ui.theme.UIComponents;
import com.hotel.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Modern Room Management screen featuring two viewing modes:
 * 1. Filterable, sortable Table View
 * 2. Visual Color-Coded Room Cards View (Floor Map)
 */
public class RoomPanel extends JPanel {

    private final RoomDAO roomDAO = new RoomDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

    // Table view
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;

    // Visual cards view
    private JPanel cardsGrid;
    private JScrollPane scrollCards;

    // View toggle
    private CardLayout viewCardLayout;
    private JPanel viewCardContainer;
    private JToggleButton btnToggleTable;
    private JToggleButton btnToggleGrid;

    // Filters & Search
    private JTextField txtSearch;
    private JComboBox<String> cbFilterType;
    private JComboBox<String> cbFilterStatus;

    // Form inputs
    private JTextField txtRoomId;
    private JTextField txtRoomNumber;
    private JTextField txtPrice;
    private JComboBox<String> cbRoomType;
    private JComboBox<String> cbStatus;
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;

    private List<Room> cachedRooms = new ArrayList<>();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public RoomPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(AppTheme.BACKGROUND);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initComponents();
        loadRoomData();
    }

    private void initComponents() {
        // =====================================================================
        // TOP: Header Bar with View Switcher & Filters
        // =====================================================================
        JPanel topBar = new JPanel(new BorderLayout(12, 0));
        topBar.setOpaque(false);

        JPanel leftTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftTitle.setOpaque(false);
        leftTitle.add(UIComponents.createSectionHeader("Room Inventory & Floor Plan"));

        // Controls: Search, Type Filter, Status Filter, View Toggle
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);

        txtSearch = UIComponents.createSearchField("Search room #...");
        txtSearch.setPreferredSize(new Dimension(160, 34));
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) { applyFilters(); }
        });

        cbFilterType = UIComponents.createComboBox(new String[]{"All Types", "Single", "Double", "Deluxe", "Suite"});
        cbFilterType.setPreferredSize(new Dimension(120, 34));
        cbFilterType.addActionListener(e -> applyFilters());

        cbFilterStatus = UIComponents.createComboBox(new String[]{"All Statuses", "AVAILABLE", "OCCUPIED", "MAINTENANCE"});
        cbFilterStatus.setPreferredSize(new Dimension(130, 34));
        cbFilterStatus.addActionListener(e -> applyFilters());

        // Toggle buttons for Table vs Grid
        btnToggleTable = new JToggleButton("Table");
        btnToggleTable.setSelected(true);
        btnToggleTable.setFont(AppTheme.FONT_SUBHEADING);

        btnToggleGrid = new JToggleButton("Visual Grid");
        btnToggleGrid.setFont(AppTheme.FONT_SUBHEADING);

        ButtonGroup viewGroup = new ButtonGroup();
        viewGroup.add(btnToggleTable);
        viewGroup.add(btnToggleGrid);

        btnToggleTable.addActionListener(e -> viewCardLayout.show(viewCardContainer, "TABLE"));
        btnToggleGrid.addActionListener(e -> viewCardLayout.show(viewCardContainer, "GRID"));

        controls.add(txtSearch);
        controls.add(cbFilterType);
        controls.add(cbFilterStatus);
        controls.add(btnToggleTable);
        controls.add(btnToggleGrid);

        topBar.add(leftTitle, BorderLayout.WEST);
        topBar.add(controls, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // =====================================================================
        // CENTER: View Container (Table View & Visual Cards Grid)
        // =====================================================================
        viewCardLayout = new CardLayout();
        viewCardContainer = new JPanel(viewCardLayout);
        viewCardContainer.setOpaque(false);

        // 1. Table View Card
        String[] columns = {"ID", "Room Number", "Room Type", "Price Per Night (₹)", "Status"};
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
                populateFormFromModel(modelRow);
            }
        });

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(AppTheme.SURFACE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                new EmptyBorder(10, 10, 10, 10)));
        tableCard.add(UIComponents.createScrollPane(table), BorderLayout.CENTER);

        // 2. Visual Grid Card
        cardsGrid = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 14));
        cardsGrid.setBackground(AppTheme.BACKGROUND);
        scrollCards = UIComponents.createScrollPane(cardsGrid);
        scrollCards.getVerticalScrollBar().setUnitIncrement(16);

        viewCardContainer.add(tableCard, "TABLE");
        viewCardContainer.add(scrollCards, "GRID");

        add(viewCardContainer, BorderLayout.CENTER);

        // =====================================================================
        // WEST: Modern Room Entry Form
        // =====================================================================
        JPanel formCard = new JPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBackground(AppTheme.SURFACE);
        formCard.setPreferredSize(new Dimension(310, 600));
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel lblFormTitle = new JLabel("Room Details");
        lblFormTitle.setFont(AppTheme.FONT_HEADING);
        lblFormTitle.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel lblFormSub = new JLabel("Create or modify room inventory");
        lblFormSub.setFont(AppTheme.FONT_SMALL);
        lblFormSub.setForeground(AppTheme.TEXT_SECONDARY);

        txtRoomId = UIComponents.createTextField();
        txtRoomId.setEditable(false);
        txtRoomId.setBackground(new Color(245, 247, 250));

        txtRoomNumber = UIComponents.createTextField();
        txtPrice = UIComponents.createTextField();

        cbRoomType = UIComponents.createComboBox(new String[]{"Single", "Double", "Deluxe", "Suite"});
        cbStatus = UIComponents.createComboBox(new String[]{"AVAILABLE", "OCCUPIED", "MAINTENANCE"});

        formCard.add(lblFormTitle);
        formCard.add(Box.createVerticalStrut(2));
        formCard.add(lblFormSub);
        formCard.add(Box.createVerticalStrut(18));

        addFormField(formCard, "System Room ID (Auto)", txtRoomId);
        addFormField(formCard, "Room Number *", txtRoomNumber);
        addFormField(formCard, "Room Category *", cbRoomType);
        addFormField(formCard, "Rate / Night (₹) *", txtPrice);
        addFormField(formCard, "Current Status *", cbStatus);

        formCard.add(Box.createVerticalStrut(10));

        // Action Buttons Grid (2x2)
        JPanel btnGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        btnGrid.setOpaque(false);
        btnGrid.setMaximumSize(new Dimension(Short.MAX_VALUE, 80));

        btnAdd = UIComponents.createPrimaryButton("+ Add Room");
        btnUpdate = UIComponents.createSecondaryButton("✎ Update");
        btnDelete = UIComponents.createDangerButton("🗑 Delete");
        btnClear = UIComponents.createSecondaryButton("✕ Clear");

        btnAdd.addActionListener(e -> addRoom());
        btnUpdate.addActionListener(e -> updateRoom());
        btnDelete.addActionListener(e -> deleteRoom());
        btnClear.addActionListener(e -> clearForm());

        btnGrid.add(btnAdd);
        btnGrid.add(btnUpdate);
        btnGrid.add(btnDelete);
        btnGrid.add(btnClear);

        formCard.add(btnGrid);
        formCard.add(Box.createVerticalGlue());

        add(formCard, BorderLayout.WEST);
    }

    private void addFormField(JPanel parent, String labelText, JComponent field) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(AppTheme.FONT_SUBHEADING);
        lbl.setForeground(AppTheme.TEXT_PRIMARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(lbl);
        parent.add(Box.createVerticalStrut(4));

        field.setMaximumSize(new Dimension(Short.MAX_VALUE, 36));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(field);
        parent.add(Box.createVerticalStrut(12));
    }

    public void loadRoomData() {
        SwingWorker<List<Room>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Room> doInBackground() {
                return roomDAO.getAllRooms();
            }

            @Override
            protected void done() {
                try {
                    cachedRooms = get();
                    populateViews();
                } catch (Exception e) {
                    e.printStackTrace();
                    UIComponents.showError(RoomPanel.this, "Error fetching rooms from database: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    // Alias for existing callers
    public void loadData() {
        loadRoomData();
    }

    private void populateViews() {
        // Populate Table
        tableModel.setRowCount(0);
        for (Room r : cachedRooms) {
            tableModel.addRow(new Object[]{
                    r.getRoomId(),
                    r.getRoomNumber(),
                    r.getRoomType(),
                    currencyFormat.format(r.getPricePerDay()),
                    r.getStatus()
            });
        }

        // Populate Visual Cards
        cardsGrid.removeAll();
        for (Room r : cachedRooms) {
            cardsGrid.add(createRoomCard(r));
        }
        cardsGrid.revalidate();
        cardsGrid.repaint();

        applyFilters();
    }

    private JPanel createRoomCard(Room r) {
        JPanel card = new JPanel(new BorderLayout(8, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                // Status border accent
                Color statusColor = getStatusColor(r.getStatus());
                g2.setColor(statusColor);
                g2.fillRoundRect(0, 0, getWidth(), 6, 6, 6);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(170, 140));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                new EmptyBorder(14, 12, 12, 12)));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lblNumber = new JLabel("Room " + r.getRoomNumber(), SwingConstants.CENTER);
        lblNumber.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblNumber.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel lblType = new JLabel(r.getRoomType(), SwingConstants.CENTER);
        lblType.setFont(AppTheme.FONT_SUBHEADING);
        lblType.setForeground(AppTheme.TEXT_SECONDARY);

        JLabel lblPrice = new JLabel(currencyFormat.format(r.getPricePerDay()) + " / night", SwingConstants.CENTER);
        lblPrice.setFont(AppTheme.FONT_SMALL);
        lblPrice.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel lblBadge = UIComponents.createStatusBadge(r.getStatus());

        JPanel centerPanel = new JPanel(new GridLayout(3, 1, 0, 2));
        centerPanel.setOpaque(false);
        centerPanel.add(lblNumber);
        centerPanel.add(lblType);
        centerPanel.add(lblPrice);

        card.add(centerPanel, BorderLayout.CENTER);
        card.add(lblBadge, BorderLayout.SOUTH);

        // Click on room card to populate form
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                txtRoomId.setText(String.valueOf(r.getRoomId()));
                txtRoomNumber.setText(r.getRoomNumber());
                cbRoomType.setSelectedItem(r.getRoomType());
                txtPrice.setText(String.valueOf(r.getPricePerDay()));
                cbStatus.setSelectedItem(r.getStatus());
            }
        });

        return card;
    }

    private Color getStatusColor(String status) {
        if (status == null) return AppTheme.TEXT_SECONDARY;
        switch (status.toUpperCase()) {
            case "AVAILABLE": return AppTheme.SUCCESS;
            case "OCCUPIED": return AppTheme.DANGER;
            case "MAINTENANCE": return AppTheme.WARNING;
            default: return AppTheme.PRIMARY_BLUE;
        }
    }

    private void applyFilters() {
        String search = txtSearch.getText().trim();
        if ("Search room #...".equals(search)) search = "";

        String selType = cbFilterType.getSelectedItem() != null ? cbFilterType.getSelectedItem().toString() : "All Types";
        String selStatus = cbFilterStatus.getSelectedItem() != null ? cbFilterStatus.getSelectedItem().toString() : "All Statuses";

        // Filter Table
        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        if (!search.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + search, 1)); // Column 1: Room Number
        }
        if (!"All Types".equals(selType)) {
            filters.add(RowFilter.regexFilter("(?i)^" + selType + "$", 2));
        }
        if (!"All Statuses".equals(selStatus)) {
            filters.add(RowFilter.regexFilter("(?i)^" + selStatus + "$", 4));
        }
        rowSorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));

        // Filter Visual Cards
        cardsGrid.removeAll();
        for (Room r : cachedRooms) {
            boolean matchSearch = search.isEmpty() || r.getRoomNumber().toLowerCase().contains(search.toLowerCase());
            boolean matchType = "All Types".equals(selType) || r.getRoomType().equalsIgnoreCase(selType);
            boolean matchStatus = "All Statuses".equals(selStatus) || r.getStatus().equalsIgnoreCase(selStatus);

            if (matchSearch && matchType && matchStatus) {
                cardsGrid.add(createRoomCard(r));
            }
        }
        cardsGrid.revalidate();
        cardsGrid.repaint();
    }

    private void populateFormFromModel(int modelRow) {
        txtRoomId.setText(tableModel.getValueAt(modelRow, 0).toString());
        txtRoomNumber.setText(tableModel.getValueAt(modelRow, 1).toString());
        cbRoomType.setSelectedItem(tableModel.getValueAt(modelRow, 2).toString());

        // Extract price numerical
        int roomId = Integer.parseInt(txtRoomId.getText());
        for (Room r : cachedRooms) {
            if (r.getRoomId() == roomId) {
                txtPrice.setText(String.valueOf(r.getPricePerDay()));
                break;
            }
        }
        cbStatus.setSelectedItem(tableModel.getValueAt(modelRow, 4).toString());
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

        Room room = new Room(0, txtRoomNumber.getText().trim(),
                cbRoomType.getSelectedItem().toString(),
                Double.parseDouble(txtPrice.getText().trim()),
                cbStatus.getSelectedItem().toString());

        if (roomDAO.addRoom(room)) {
            UIComponents.showSuccess(this, "Room " + room.getRoomNumber() + " added successfully!");
            clearForm();
            loadRoomData();
        } else {
            UIComponents.showError(this, "Failed to add room. Room number may already exist.");
        }
    }

    private void updateRoom() {
        if (txtRoomId.getText().isEmpty()) {
            UIComponents.showWarning(this, "Please select a room to update.");
            return;
        }
        if (!validateInput()) return;

        Room room = new Room(Integer.parseInt(txtRoomId.getText()),
                txtRoomNumber.getText().trim(),
                cbRoomType.getSelectedItem().toString(),
                Double.parseDouble(txtPrice.getText().trim()),
                cbStatus.getSelectedItem().toString());

        if (roomDAO.updateRoom(room)) {
            UIComponents.showSuccess(this, "Room " + room.getRoomNumber() + " updated successfully!");
            clearForm();
            loadRoomData();
        } else {
            UIComponents.showError(this, "Failed to update room.");
        }
    }

    private void deleteRoom() {
        if (txtRoomId.getText().isEmpty()) {
            UIComponents.showWarning(this, "Please select a room to delete.");
            return;
        }

        int id = Integer.parseInt(txtRoomId.getText());
        String roomNum = txtRoomNumber.getText();

        if (UIComponents.confirm(this, "Are you sure you want to delete Room " + roomNum + "?", "Confirm Delete")) {
            if (roomDAO.deleteRoom(id)) {
                UIComponents.showSuccess(this, "Room " + roomNum + " deleted successfully!");
                clearForm();
                loadRoomData();
            } else {
                UIComponents.showError(this, "Cannot delete Room " + roomNum + " because it has historical or active bookings attached.");
            }
        }
    }

    private boolean validateInput() {
        if (ValidationUtil.isEmpty(txtRoomNumber.getText())) {
            UIComponents.showWarning(this, "Room number cannot be empty.");
            txtRoomNumber.requestFocusInWindow();
            return false;
        }
        if (!ValidationUtil.isNumeric(txtPrice.getText())) {
            UIComponents.showWarning(this, "Price per night must be a valid positive number.");
            txtPrice.requestFocusInWindow();
            return false;
        }
        return true;
    }
}
