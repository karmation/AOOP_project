package com.hotel.ui;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.CustomerDAO;
import com.hotel.dao.RoomDAO;
import com.hotel.exception.BookingConflictException;
import com.hotel.model.Booking;
import com.hotel.model.Customer;
import com.hotel.model.Room;
import com.hotel.ui.theme.AppTheme;
import com.hotel.ui.theme.UIComponents;
import com.hotel.util.BookingValidator;
import com.hotel.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.Date;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Fast, structured Booking & Check-In Workflow screen.
 * Includes Section A (Guest Search & Quick Add), Section B (Dates & Filtered Rooms),
 * Section C (Live Cost Calculator), and a comprehensive Booking Audit Table.
 */
public class BookingPanel extends JPanel {

    private final BookingDAO bookingDAO = new BookingDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final RoomDAO roomDAO = new RoomDAO();

    // Section A: Customer
    private JComboBox<String> cbCustomer;
    private JLabel lblCustDetails;
    private JButton btnQuickAddCustomer;

    // Section B: Room & Stay
    private JTextField txtCheckIn;
    private JTextField txtCheckOut;
    private JComboBox<String> cbRoomTypeFilter;
    private JComboBox<String> cbAvailableRooms;
    private JSpinner spinAdults;
    private JSpinner spinChildren;

    // Section C: Auto Live Cost Summary
    private JLabel lblNights;
    private JLabel lblRate;
    private JLabel lblEstTotal;

    // Table & Filters
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTextField txtSearch;
    private JComboBox<String> cbStatusFilter;
    private JButton btnCancelBooking;
    private JButton btnRefresh;

    private List<Customer> customersList = new ArrayList<>();
    private List<Room> availableRoomsList = new ArrayList<>();
    private List<Booking> cachedBookings = new ArrayList<>();

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public BookingPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(AppTheme.BACKGROUND);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initComponents();
        loadData();
    }

    private void initComponents() {
        // =====================================================================
        // TOP BAR
        // =====================================================================
        JPanel topBar = new JPanel(new BorderLayout(12, 0));
        topBar.setOpaque(false);

        JPanel leftTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftTitle.setOpaque(false);
        leftTitle.add(UIComponents.createSectionHeader("Reservations & Front-Desk Check-In"));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchPanel.setOpaque(false);

        txtSearch = UIComponents.createSearchField("Search guest or room...");
        txtSearch.setPreferredSize(new Dimension(180, 34));
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) { applyFilters(); }
        });

        cbStatusFilter = UIComponents.createComboBox(new String[]{"All Statuses", "ACTIVE", "COMPLETED", "CANCELLED"});
        cbStatusFilter.setPreferredSize(new Dimension(130, 34));
        cbStatusFilter.addActionListener(e -> applyFilters());

        btnCancelBooking = UIComponents.createDangerButton("✕ Cancel Stay");
        btnCancelBooking.addActionListener(e -> cancelBooking());

        btnRefresh = UIComponents.createSecondaryButton("⟳ Refresh");
        btnRefresh.addActionListener(e -> loadData());

        searchPanel.add(txtSearch);
        searchPanel.add(cbStatusFilter);
        searchPanel.add(btnCancelBooking);
        searchPanel.add(btnRefresh);

        topBar.add(leftTitle, BorderLayout.WEST);
        topBar.add(searchPanel, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // =====================================================================
        // WEST: Structured Workflow Booking Form (350px width)
        // =====================================================================
        JPanel formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setOpaque(false);
        formContainer.setPreferredSize(new Dimension(360, 680));

        // SECTION A: Customer Card
        JPanel cardCust = createFormSectionCard("1. Guest Identification");
        cbCustomer = UIComponents.createComboBox(new String[]{"Loading guests..."});
        cbCustomer.addActionListener(e -> updateCustomerDetailsNotice());

        btnQuickAddCustomer = UIComponents.createSecondaryButton("+ New Guest");
        btnQuickAddCustomer.setFont(AppTheme.FONT_SMALL);
        btnQuickAddCustomer.addActionListener(e -> showQuickAddCustomerDialog());

        JPanel custRow = new JPanel(new BorderLayout(6, 0));
        custRow.setOpaque(false);
        custRow.add(cbCustomer, BorderLayout.CENTER);
        custRow.add(btnQuickAddCustomer, BorderLayout.EAST);

        lblCustDetails = new JLabel("Phone: - • ID: -");
        lblCustDetails.setFont(AppTheme.FONT_SMALL);
        lblCustDetails.setForeground(AppTheme.TEXT_SECONDARY);

        cardCust.add(custRow);
        cardCust.add(Box.createVerticalStrut(4));
        cardCust.add(lblCustDetails);

        // SECTION B: Room & Dates Card
        JPanel cardRoom = createFormSectionCard("2. Room & Dates");

        // Dates Row
        long now = System.currentTimeMillis();
        txtCheckIn = UIComponents.createTextField();
        txtCheckIn.setText(dateFormat.format(new java.util.Date(now)));
        txtCheckOut = UIComponents.createTextField();
        txtCheckOut.setText(dateFormat.format(new java.util.Date(now + 86400000L))); // Tomorrow

        txtCheckIn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) { updateLiveCalculation(); }
        });
        txtCheckOut.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) { updateLiveCalculation(); }
        });

        JPanel datesRow = new JPanel(new GridLayout(1, 2, 8, 0));
        datesRow.setOpaque(false);

        JPanel pIn = new JPanel(new BorderLayout(0, 2));
        pIn.setOpaque(false);
        pIn.add(new JLabel("Check-in (YYYY-MM-DD):"), BorderLayout.NORTH);
        pIn.add(txtCheckIn, BorderLayout.CENTER);

        JPanel pOut = new JPanel(new BorderLayout(0, 2));
        pOut.setOpaque(false);
        pOut.add(new JLabel("Check-out (YYYY-MM-DD):"), BorderLayout.NORTH);
        pOut.add(txtCheckOut, BorderLayout.CENTER);

        datesRow.add(pIn);
        datesRow.add(pOut);

        // Category Filter & Room Selection
        cbRoomTypeFilter = UIComponents.createComboBox(new String[]{"All Categories", "Single", "Double", "Deluxe", "Suite"});
        cbRoomTypeFilter.addActionListener(e -> filterAvailableRoomsDropdown());

        cbAvailableRooms = UIComponents.createComboBox(new String[]{"Loading available rooms..."});
        cbAvailableRooms.addActionListener(e -> updateLiveCalculation());

        JPanel roomRow = new JPanel(new GridLayout(1, 2, 8, 0));
        roomRow.setOpaque(false);

        JPanel pType = new JPanel(new BorderLayout(0, 2));
        pType.setOpaque(false);
        pType.add(new JLabel("Filter Category:"), BorderLayout.NORTH);
        pType.add(cbRoomTypeFilter, BorderLayout.CENTER);

        JPanel pRoom = new JPanel(new BorderLayout(0, 2));
        pRoom.setOpaque(false);
        pRoom.add(new JLabel("Available Room:"), BorderLayout.NORTH);
        pRoom.add(cbAvailableRooms, BorderLayout.CENTER);

        roomRow.add(pType);
        roomRow.add(pRoom);

        // Guests Row (Adults & Children)
        spinAdults = new JSpinner(new SpinnerNumberModel(1, 1, 6, 1));
        spinChildren = new JSpinner(new SpinnerNumberModel(0, 0, 4, 1));

        JPanel guestsRow = new JPanel(new GridLayout(1, 2, 8, 0));
        guestsRow.setOpaque(false);

        JPanel pAdults = new JPanel(new BorderLayout(0, 2));
        pAdults.setOpaque(false);
        pAdults.add(new JLabel("Adults:"), BorderLayout.NORTH);
        pAdults.add(spinAdults, BorderLayout.CENTER);

        JPanel pKids = new JPanel(new BorderLayout(0, 2));
        pKids.setOpaque(false);
        pKids.add(new JLabel("Children:"), BorderLayout.NORTH);
        pKids.add(spinChildren, BorderLayout.CENTER);

        guestsRow.add(pAdults);
        guestsRow.add(pKids);

        cardRoom.add(datesRow);
        cardRoom.add(Box.createVerticalStrut(8));
        cardRoom.add(roomRow);
        cardRoom.add(Box.createVerticalStrut(8));
        cardRoom.add(guestsRow);

        // SECTION C: Estimated Cost & Confirmation
        JPanel cardSummary = createFormSectionCard("3. Cost Estimate & Check-In");

        JPanel calcGrid = new JPanel(new GridLayout(3, 2, 6, 4));
        calcGrid.setOpaque(false);

        calcGrid.add(new JLabel("Duration:"));
        lblNights = new JLabel("1 Night", SwingConstants.RIGHT);
        lblNights.setFont(AppTheme.FONT_SUBHEADING);
        calcGrid.add(lblNights);

        calcGrid.add(new JLabel("Room Rate:"));
        lblRate = new JLabel("₹0.00 / night", SwingConstants.RIGHT);
        lblRate.setFont(AppTheme.FONT_SUBHEADING);
        calcGrid.add(lblRate);

        JLabel lblTotalTitle = new JLabel("Estimated Total:");
        lblTotalTitle.setFont(AppTheme.FONT_HEADING);
        calcGrid.add(lblTotalTitle);

        lblEstTotal = new JLabel("₹0.00", SwingConstants.RIGHT);
        lblEstTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblEstTotal.setForeground(AppTheme.PRIMARY_BLUE);
        calcGrid.add(lblEstTotal);

        JButton btnSubmit = UIComponents.createPrimaryButton("✓  Confirm & Check-In Guest");
        btnSubmit.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSubmit.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        btnSubmit.addActionListener(e -> createBooking());

        cardSummary.add(calcGrid);
        cardSummary.add(Box.createVerticalStrut(12));
        cardSummary.add(btnSubmit);

        formContainer.add(cardCust);
        formContainer.add(Box.createVerticalStrut(10));
        formContainer.add(cardRoom);
        formContainer.add(Box.createVerticalStrut(10));
        formContainer.add(cardSummary);
        formContainer.add(Box.createVerticalGlue());

        JScrollPane formScroll = UIComponents.createScrollPane(formContainer);
        formScroll.getVerticalScrollBar().setUnitIncrement(14);
        formScroll.setPreferredSize(new Dimension(370, 600));
        add(formScroll, BorderLayout.WEST);

        // =====================================================================
        // CENTER: Reservations Table
        // =====================================================================
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(AppTheme.SURFACE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                new EmptyBorder(10, 10, 10, 10)));

        String[] columns = {"ID", "Guest Name", "Phone", "Room", "Type", "Check-In", "Check-Out", "Guests", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);
        UIComponents.applyTableStyle(table);

        tableCard.add(UIComponents.createScrollPane(table), BorderLayout.CENTER);
        add(tableCard, BorderLayout.CENTER);
    }

    private JPanel createFormSectionCard(String title) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppTheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                new EmptyBorder(12, 14, 12, 14)));
        card.setMaximumSize(new Dimension(Short.MAX_VALUE, 300));

        JLabel lbl = new JLabel(title);
        lbl.setFont(AppTheme.FONT_SUBHEADING);
        lbl.setForeground(AppTheme.PRIMARY_BLUE);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lbl);
        card.add(Box.createVerticalStrut(8));
        return card;
    }

    public void loadData() {
        btnRefresh.setEnabled(false);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                customersList = customerDAO.getAllCustomers();
                availableRoomsList = roomDAO.getAvailableRooms();
                cachedBookings = bookingDAO.getAllBookings();
                return null;
            }

            @Override
            protected void done() {
                btnRefresh.setEnabled(true);
                populateDropdowns();
                populateBookingsTable();
            }
        };
        worker.execute();
    }

    private void populateDropdowns() {
        // Customers Dropdown
        cbCustomer.removeAllItems();
        if (customersList.isEmpty()) {
            cbCustomer.addItem("No guests registered");
        } else {
            for (Customer c : customersList) {
                cbCustomer.addItem(c.getCustomerId() + " - " + c.getName());
            }
        }
        updateCustomerDetailsNotice();

        // Available Rooms Dropdown
        filterAvailableRoomsDropdown();
    }

    private void filterAvailableRoomsDropdown() {
        cbAvailableRooms.removeAllItems();
        String selectedCat = cbRoomTypeFilter.getSelectedItem() != null ? cbRoomTypeFilter.getSelectedItem().toString() : "All Categories";

        int count = 0;
        for (Room r : availableRoomsList) {
            if ("All Categories".equals(selectedCat) || r.getRoomType().equalsIgnoreCase(selectedCat)) {
                cbAvailableRooms.addItem(r.getRoomId() + " - Room " + r.getRoomNumber() + " (" + r.getRoomType() + " • ₹" + r.getPricePerDay() + ")");
                count++;
            }
        }
        if (count == 0) {
            cbAvailableRooms.addItem("No available rooms");
        }
        updateLiveCalculation();
    }

    private void updateCustomerDetailsNotice() {
        int idx = cbCustomer.getSelectedIndex();
        if (idx >= 0 && idx < customersList.size()) {
            Customer c = customersList.get(idx);
            lblCustDetails.setText("Phone: " + c.getPhone() + "  •  ID: " + (c.getIdProof() != null ? c.getIdProof() : "N/A"));
        } else {
            lblCustDetails.setText("Phone: -  •  ID: -");
        }
    }

    private void updateLiveCalculation() {
        try {
            Date in = Date.valueOf(txtCheckIn.getText().trim());
            Date out = Date.valueOf(txtCheckOut.getText().trim());

            long diffMs = out.getTime() - in.getTime();
            long nights = Math.max(1, diffMs / (1000 * 60 * 60 * 24));
            lblNights.setText(nights + (nights == 1 ? " Night" : " Nights"));

            Room room = getSelectedRoom();
            if (room != null) {
                double rate = room.getPricePerDay();
                lblRate.setText(currencyFormat.format(rate) + " / night");
                double total = nights * rate;
                lblEstTotal.setText(currencyFormat.format(total));
            } else {
                lblRate.setText("₹0.00 / night");
                lblEstTotal.setText("₹0.00");
            }
        } catch (Exception ignored) {
            lblNights.setText("-");
            lblEstTotal.setText("-");
        }
    }

    private Room getSelectedRoom() {
        String item = (String) cbAvailableRooms.getSelectedItem();
        if (item == null || item.contains("No available")) return null;
        try {
            int roomId = Integer.parseInt(item.split(" - ")[0]);
            for (Room r : availableRoomsList) {
                if (r.getRoomId() == roomId) return r;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void populateBookingsTable() {
        tableModel.setRowCount(0);
        for (Booking b : cachedBookings) {
            Customer c = b.getCustomer();
            Room r = b.getRoom();
            String guestName = c != null ? c.getName() : ("Customer #" + b.getCustomerId());
            String guestPhone = c != null ? c.getPhone() : "-";
            String roomNum = r != null ? r.getRoomNumber() : ("Room #" + b.getRoomId());
            String roomType = r != null ? r.getRoomType() : "-";
            String guestCount = b.getAdults() + " Adults, " + b.getChildren() + " Kids";

            tableModel.addRow(new Object[]{
                    "#" + b.getBookingId(),
                    guestName,
                    guestPhone,
                    roomNum,
                    roomType,
                    b.getCheckIn(),
                    b.getCheckOut(),
                    guestCount,
                    b.getStatus()
            });
        }
        applyFilters();
    }

    private void applyFilters() {
        String search = txtSearch.getText().trim();
        if ("Search guest or room...".equals(search)) search = "";

        String selStatus = cbStatusFilter.getSelectedItem() != null ? cbStatusFilter.getSelectedItem().toString() : "All Statuses";

        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        if (!search.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + search, 1, 2, 3)); // Name, Phone, Room Number
        }
        if (!"All Statuses".equals(selStatus)) {
            filters.add(RowFilter.regexFilter("(?i)^" + selStatus + "$", 8));
        }
        rowSorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }

    private void createBooking() {
        if (customersList.isEmpty() || cbCustomer.getSelectedIndex() < 0) {
            UIComponents.showWarning(this, "Please select or register a guest first.");
            return;
        }
        Customer customer = customersList.get(cbCustomer.getSelectedIndex());

        Room room = getSelectedRoom();
        if (room == null) {
            UIComponents.showWarning(this, "No valid available room selected.");
            return;
        }

        try {
            Date checkIn = Date.valueOf(txtCheckIn.getText().trim());
            Date checkOut = Date.valueOf(txtCheckOut.getText().trim());
            int adults = (int) spinAdults.getValue();
            int children = (int) spinChildren.getValue();

            // Validate business rules
            BookingValidator.validate(checkIn, checkOut, adults, children);

            Booking booking = new Booking(0, customer.getCustomerId(), room.getRoomId(),
                    checkIn, checkOut, adults, children, "ACTIVE");

            if (bookingDAO.createBooking(booking)) {
                UIComponents.showSuccess(this, "Booking confirmed for " + customer.getName() + " in Room " + room.getRoomNumber() + "!");
                loadData();
            } else {
                UIComponents.showError(this, "Failed to create booking. Room " + room.getRoomNumber() + " is no longer available.");
            }
        } catch (IllegalArgumentException e) {
            UIComponents.showWarning(this, "Invalid date format. Please use YYYY-MM-DD (e.g. 2026-11-20).");
        } catch (BookingConflictException e) {
            UIComponents.showWarning(this, e.getMessage());
        } catch (Exception e) {
            UIComponents.showError(this, "Booking Error: " + e.getMessage());
        }
    }

    private void cancelBooking() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            UIComponents.showWarning(this, "Please select a booking from the table to cancel.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        int bookingId = Integer.parseInt(tableModel.getValueAt(modelRow, 0).toString().replace("#", ""));
        String guest = tableModel.getValueAt(modelRow, 1).toString();
        String currentStatus = tableModel.getValueAt(modelRow, 8).toString();

        if (!"ACTIVE".equalsIgnoreCase(currentStatus)) {
            UIComponents.showWarning(this, "Only ACTIVE reservations can be cancelled. Current status is " + currentStatus + ".");
            return;
        }

        if (UIComponents.confirm(this, "Cancel booking #" + bookingId + " for " + guest + "?\nThis will free the room.", "Confirm Cancellation")) {
            if (bookingDAO.updateBookingStatus(bookingId, "CANCELLED")) {
                // Free the room
                for (Booking b : cachedBookings) {
                    if (b.getBookingId() == bookingId) {
                        try (java.sql.Connection con = com.hotel.database.DBConnection.getConnection()) {
                            roomDAO.updateRoomStatus(con, b.getRoomId(), "AVAILABLE");
                        } catch (Exception ignored) {}
                        break;
                    }
                }
                UIComponents.showSuccess(this, "Booking #" + bookingId + " has been cancelled.");
                loadData();
            } else {
                UIComponents.showError(this, "Failed to cancel booking.");
            }
        }
    }

    private void showQuickAddCustomerDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Quick Guest Registration", true);
        dialog.setSize(420, 360);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField qName = UIComponents.createTextField();
        JTextField qPhone = UIComponents.createTextField();
        JTextField qEmail = UIComponents.createTextField();
        JTextField qIdProof = UIComponents.createTextField();

        panel.add(new JLabel("Full Name *:")); panel.add(qName);
        panel.add(new JLabel("Phone Number *:")); panel.add(qPhone);
        panel.add(new JLabel("Email Address:")); panel.add(qEmail);
        panel.add(new JLabel("Govt ID *:")); panel.add(qIdProof);

        JButton btnSave = UIComponents.createPrimaryButton("Register & Select");
        btnSave.addActionListener(e -> {
            if (ValidationUtil.isEmpty(qName.getText()) || ValidationUtil.isEmpty(qPhone.getText())) {
                UIComponents.showWarning(dialog, "Name and Phone Number are required.");
                return;
            }
            Customer c = new Customer(0, qName.getText().trim(), qPhone.getText().trim(),
                    qEmail.getText().trim(), "", qIdProof.getText().trim());
            if (customerDAO.addCustomer(c)) {
                dialog.dispose();
                loadData();
            } else {
                UIComponents.showError(dialog, "Failed to add guest. Phone may already be registered.");
            }
        });

        JButton btnCancel = UIComponents.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dialog.dispose());

        panel.add(btnSave);
        panel.add(btnCancel);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }
}
