package com.hotel.ui;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.ServiceDAO;
import com.hotel.model.Booking;
import com.hotel.model.CustomerService;
import com.hotel.model.Service;
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
 * Modern Hotel Amenities & Guest Services Screen.
 * Contains:
 * 1. Hotel Services Catalogue (Add, Edit, Delete, Search)
 * 2. Add Amenities / Dining to Active Guest Bookings with Live Order Table
 */
public class ServicePanel extends JPanel {

    private final ServiceDAO serviceDAO = new ServiceDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

    // Catalogue Table
    private JTable tblCatalogue;
    private DefaultTableModel modelCatalogue;
    private TableRowSorter<DefaultTableModel> catalogueSorter;
    private JTextField txtSearchCatalogue;

    // Service Form
    private JTextField txtServiceId;
    private JTextField txtServiceName;
    private JTextField txtPrice;
    private JButton btnAddService;
    private JButton btnUpdateService;
    private JButton btnDeleteService;
    private JButton btnClearForm;

    // Order to Booking Section
    private JComboBox<String> cbActiveBookings;
    private JComboBox<String> cbServices;
    private JSpinner spinQuantity;
    private JLabel lblUnitPrice;
    private JLabel lblSubtotal;
    private JButton btnAddToBooking;

    // Ordered Services Table
    private JTable tblOrders;
    private DefaultTableModel modelOrders;

    private List<Service> allServices = new ArrayList<>();
    private List<Booking> activeBookings = new ArrayList<>();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public ServicePanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(AppTheme.BACKGROUND);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initComponents();
        loadData();
    }

    private void initComponents() {
        // =====================================================================
        // TOP: Header
        // =====================================================================
        JPanel topBar = new JPanel(new BorderLayout(12, 0));
        topBar.setOpaque(false);

        JPanel leftTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftTitle.setOpaque(false);
        leftTitle.add(UIComponents.createSectionHeader("Amenities, Dining & Guest Service Orders"));

        JButton btnRefresh = UIComponents.createSecondaryButton("⟳  Refresh");
        btnRefresh.addActionListener(e -> loadData());

        topBar.add(leftTitle, BorderLayout.WEST);
        topBar.add(btnRefresh, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // =====================================================================
        // SPLIT CONTENT: Left = Catalogue (45%), Right = Booking Orders (55%)
        // =====================================================================
        JPanel mainContent = new JPanel(new GridLayout(1, 2, 16, 0));
        mainContent.setOpaque(false);

        // ---------------------------------------------------------------------
        // LEFT COLUMN: Hotel Services Catalogue
        // ---------------------------------------------------------------------
        JPanel leftCard = new JPanel(new BorderLayout(0, 10));
        leftCard.setBackground(AppTheme.SURFACE);
        leftCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                new EmptyBorder(16, 16, 16, 16)));

        JPanel headCat = new JPanel(new BorderLayout());
        headCat.setOpaque(false);
        JLabel lblCatTitle = new JLabel("1. Hotel Amenities Catalogue");
        lblCatTitle.setFont(AppTheme.FONT_HEADING);
        lblCatTitle.setForeground(AppTheme.TEXT_PRIMARY);

        txtSearchCatalogue = UIComponents.createSearchField("Search services...");
        txtSearchCatalogue.setPreferredSize(new Dimension(160, 32));
        txtSearchCatalogue.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) { applyCatalogueSearch(); }
        });

        headCat.add(lblCatTitle, BorderLayout.WEST);
        headCat.add(txtSearchCatalogue, BorderLayout.EAST);

        leftCard.add(headCat, BorderLayout.NORTH);

        // Catalogue Table
        String[] catCols = {"ID", "Service / Amenity", "Price (₹)"};
        modelCatalogue = new DefaultTableModel(catCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblCatalogue = new JTable(modelCatalogue);
        catalogueSorter = new TableRowSorter<>(modelCatalogue);
        tblCatalogue.setRowSorter(catalogueSorter);
        UIComponents.applyTableStyle(tblCatalogue);

        tblCatalogue.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblCatalogue.getSelectedRow() != -1) {
                int modelRow = tblCatalogue.convertRowIndexToModel(tblCatalogue.getSelectedRow());
                txtServiceId.setText(modelCatalogue.getValueAt(modelRow, 0).toString());
                txtServiceName.setText(modelCatalogue.getValueAt(modelRow, 1).toString());

                // Find price numeric
                int sid = Integer.parseInt(txtServiceId.getText());
                for (Service s : allServices) {
                    if (s.getServiceId() == sid) {
                        txtPrice.setText(String.valueOf(s.getPrice()));
                        break;
                    }
                }
            }
        });

        leftCard.add(UIComponents.createScrollPane(tblCatalogue), BorderLayout.CENTER);

        // Catalogue Form (Bottom)
        JPanel catForm = new JPanel(new GridLayout(2, 4, 8, 8));
        catForm.setOpaque(false);
        catForm.setBorder(new EmptyBorder(10, 0, 0, 0));

        txtServiceId = UIComponents.createTextField();
        txtServiceId.setEditable(false);
        txtServiceId.setBackground(new Color(245, 247, 250));
        txtServiceName = UIComponents.createTextField();
        txtPrice = UIComponents.createTextField();

        catForm.add(new JLabel("Service Name:"));
        catForm.add(txtServiceName);
        catForm.add(new JLabel("Price (₹):"));
        catForm.add(txtPrice);

        btnAddService = UIComponents.createPrimaryButton("+ Add");
        btnUpdateService = UIComponents.createSecondaryButton("✎ Update");
        btnDeleteService = UIComponents.createDangerButton("🗑 Delete");
        btnClearForm = UIComponents.createSecondaryButton("✕ Clear");

        btnAddService.addActionListener(e -> addService());
        btnUpdateService.addActionListener(e -> updateService());
        btnDeleteService.addActionListener(e -> deleteService());
        btnClearForm.addActionListener(e -> clearForm());

        catForm.add(btnAddService);
        catForm.add(btnUpdateService);
        catForm.add(btnDeleteService);
        catForm.add(btnClearForm);

        leftCard.add(catForm, BorderLayout.SOUTH);
        mainContent.add(leftCard);

        // ---------------------------------------------------------------------
        // RIGHT COLUMN: Add Service to Guest Stay & Current Orders
        // ---------------------------------------------------------------------
        JPanel rightCard = new JPanel(new BorderLayout(0, 10));
        rightCard.setBackground(AppTheme.SURFACE);
        rightCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                new EmptyBorder(16, 16, 16, 16)));

        JLabel lblOrderTitle = new JLabel("2. Charge Amenity to Active Guest Booking");
        lblOrderTitle.setFont(AppTheme.FONT_HEADING);
        lblOrderTitle.setForeground(AppTheme.TEXT_PRIMARY);
        rightCard.add(lblOrderTitle, BorderLayout.NORTH);

        // Order Form Controls
        JPanel orderControls = new JPanel(new GridLayout(3, 2, 10, 8));
        orderControls.setOpaque(false);

        cbActiveBookings = UIComponents.createComboBox(new String[]{"Loading active stays..."});
        cbActiveBookings.addActionListener(e -> loadOrdersForSelectedBooking());

        cbServices = UIComponents.createComboBox(new String[]{"Loading services..."});
        cbServices.addActionListener(e -> updateServiceOrderCalculation());

        spinQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        spinQuantity.addChangeListener(e -> updateServiceOrderCalculation());

        lblUnitPrice = new JLabel("Unit: ₹0.00");
        lblUnitPrice.setFont(AppTheme.FONT_SUBHEADING);

        lblSubtotal = new JLabel("Subtotal: ₹0.00");
        lblSubtotal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSubtotal.setForeground(AppTheme.PRIMARY_BLUE);

        btnAddToBooking = UIComponents.createPrimaryButton("✓  Add Service to Stay");
        btnAddToBooking.addActionListener(e -> addServiceToBooking());

        orderControls.add(new JLabel("Active Guest Stay:"));
        orderControls.add(cbActiveBookings);
        orderControls.add(new JLabel("Select Amenity / Service:"));
        orderControls.add(cbServices);

        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        qtyPanel.setOpaque(false);
        qtyPanel.add(new JLabel("Qty:"));
        qtyPanel.add(spinQuantity);
        qtyPanel.add(lblUnitPrice);

        orderControls.add(qtyPanel);

        JPanel btnSubRow = new JPanel(new BorderLayout(6, 0));
        btnSubRow.setOpaque(false);
        btnSubRow.add(lblSubtotal, BorderLayout.WEST);
        btnSubRow.add(btnAddToBooking, BorderLayout.EAST);
        orderControls.add(btnSubRow);

        // Orders Table for Selected Booking
        JPanel ordersContainer = new JPanel(new BorderLayout(0, 8));
        ordersContainer.setOpaque(false);
        ordersContainer.add(orderControls, BorderLayout.NORTH);

        JLabel lblListTitle = new JLabel("Services Ordered for Selected Stay:");
        lblListTitle.setFont(AppTheme.FONT_SUBHEADING);
        lblListTitle.setForeground(AppTheme.TEXT_SECONDARY);

        String[] orderCols = {"Order #", "Amenity", "Quantity", "Rate", "Subtotal"};
        modelOrders = new DefaultTableModel(orderCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblOrders = new JTable(modelOrders);
        UIComponents.applyTableStyle(tblOrders);

        JPanel ordersTableBox = new JPanel(new BorderLayout(0, 4));
        ordersTableBox.setOpaque(false);
        ordersTableBox.add(lblListTitle, BorderLayout.NORTH);
        ordersTableBox.add(UIComponents.createScrollPane(tblOrders), BorderLayout.CENTER);

        ordersContainer.add(ordersTableBox, BorderLayout.CENTER);

        rightCard.add(ordersContainer, BorderLayout.CENTER);
        mainContent.add(rightCard);

        add(mainContent, BorderLayout.CENTER);
    }

    public void loadData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                allServices = serviceDAO.getAllServices();
                activeBookings = bookingDAO.getActiveBookings();
                return null;
            }

            @Override
            protected void done() {
                // Populate Catalogue
                modelCatalogue.setRowCount(0);
                cbServices.removeAllItems();

                for (Service s : allServices) {
                    modelCatalogue.addRow(new Object[]{
                            s.getServiceId(),
                            s.getServiceName(),
                            currencyFormat.format(s.getPrice())
                    });
                    cbServices.addItem(s.getServiceId() + " - " + s.getServiceName() + " (" + currencyFormat.format(s.getPrice()) + ")");
                }

                // Populate Active Bookings
                cbActiveBookings.removeAllItems();
                if (activeBookings.isEmpty()) {
                    cbActiveBookings.addItem("No active guest stays");
                } else {
                    for (Booking b : activeBookings) {
                        String guest = b.getCustomer() != null ? b.getCustomer().getName() : ("Guest #" + b.getCustomerId());
                        String room = b.getRoom() != null ? b.getRoom().getRoomNumber() : ("Room #" + b.getRoomId());
                        cbActiveBookings.addItem(b.getBookingId() + " - " + guest + " (" + room + ")");
                    }
                }

                updateServiceOrderCalculation();
                loadOrdersForSelectedBooking();
            }
        };
        worker.execute();
    }

    private void applyCatalogueSearch() {
        String query = txtSearchCatalogue.getText().trim();
        if ("Search services...".equals(query)) query = "";

        if (query.isEmpty()) {
            catalogueSorter.setRowFilter(null);
        } else {
            catalogueSorter.setRowFilter(RowFilter.regexFilter("(?i)" + query, 1));
        }
    }

    private void updateServiceOrderCalculation() {
        Service s = getSelectedService();
        int qty = (int) spinQuantity.getValue();
        if (s != null) {
            lblUnitPrice.setText("Rate: " + currencyFormat.format(s.getPrice()));
            lblSubtotal.setText("Subtotal: " + currencyFormat.format(s.getPrice() * qty));
        } else {
            lblUnitPrice.setText("Rate: ₹0.00");
            lblSubtotal.setText("Subtotal: ₹0.00");
        }
    }

    private Service getSelectedService() {
        String item = (String) cbServices.getSelectedItem();
        if (item == null) return null;
        try {
            int sid = Integer.parseInt(item.split(" - ")[0]);
            for (Service s : allServices) {
                if (s.getServiceId() == sid) return s;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private Booking getSelectedBooking() {
        String item = (String) cbActiveBookings.getSelectedItem();
        if (item == null || item.contains("No active")) return null;
        try {
            int bid = Integer.parseInt(item.split(" - ")[0]);
            for (Booking b : activeBookings) {
                if (b.getBookingId() == bid) return b;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void loadOrdersForSelectedBooking() {
        Booking b = getSelectedBooking();
        modelOrders.setRowCount(0);
        if (b == null) return;

        List<CustomerService> list = serviceDAO.getServicesForBooking(b.getBookingId());
        for (CustomerService cs : list) {
            double lineTotal = cs.getPrice() * cs.getQuantity();
            modelOrders.addRow(new Object[]{
                    "#" + cs.getId(),
                    cs.getServiceName(),
                    cs.getQuantity(),
                    currencyFormat.format(cs.getPrice()),
                    currencyFormat.format(lineTotal)
            });
        }
    }

    private void addServiceToBooking() {
        Booking booking = getSelectedBooking();
        if (booking == null) {
            UIComponents.showWarning(this, "Please select an active guest stay.");
            return;
        }

        Service service = getSelectedService();
        if (service == null) {
            UIComponents.showWarning(this, "Please select a valid service to add.");
            return;
        }

        int qty = (int) spinQuantity.getValue();
        CustomerService cs = new CustomerService(0, booking.getBookingId(), service.getServiceId(), qty);

        if (serviceDAO.addServiceToBooking(cs)) {
            UIComponents.showSuccess(this, qty + "x '" + service.getServiceName() + "' charged to Booking #" + booking.getBookingId() + "!");
            loadOrdersForSelectedBooking();
        } else {
            UIComponents.showError(this, "Failed to add service to booking.");
        }
    }

    private void clearForm() {
        txtServiceId.setText("");
        txtServiceName.setText("");
        txtPrice.setText("");
        tblCatalogue.clearSelection();
        txtServiceName.requestFocusInWindow();
    }

    private void addService() {
        if (!validateCatalogueInput()) return;

        Service s = new Service(0, txtServiceName.getText().trim(), Double.parseDouble(txtPrice.getText().trim()));
        if (serviceDAO.addService(s)) {
            UIComponents.showSuccess(this, "Service '" + s.getServiceName() + "' added!");
            clearForm();
            loadData();
        } else {
            UIComponents.showError(this, "Failed to add service. Name may already exist.");
        }
    }

    private void updateService() {
        if (txtServiceId.getText().isEmpty()) {
            UIComponents.showWarning(this, "Please select a service from the catalogue to update.");
            return;
        }
        if (!validateCatalogueInput()) return;

        Service s = new Service(Integer.parseInt(txtServiceId.getText()),
                txtServiceName.getText().trim(),
                Double.parseDouble(txtPrice.getText().trim()));

        if (serviceDAO.updateService(s)) {
            UIComponents.showSuccess(this, "Service '" + s.getServiceName() + "' updated!");
            clearForm();
            loadData();
        } else {
            UIComponents.showError(this, "Failed to update service.");
        }
    }

    private void deleteService() {
        if (txtServiceId.getText().isEmpty()) {
            UIComponents.showWarning(this, "Please select a service to delete.");
            return;
        }

        int id = Integer.parseInt(txtServiceId.getText());
        String name = txtServiceName.getText();

        if (UIComponents.confirm(this, "Delete service '" + name + "' from catalogue?", "Confirm Delete")) {
            if (serviceDAO.deleteService(id)) {
                UIComponents.showSuccess(this, "Service deleted!");
                clearForm();
                loadData();
            } else {
                UIComponents.showError(this, "Cannot delete service because it is linked to past guest stays.");
            }
        }
    }

    private boolean validateCatalogueInput() {
        if (ValidationUtil.isEmpty(txtServiceName.getText())) {
            UIComponents.showWarning(this, "Service name cannot be empty.");
            txtServiceName.requestFocusInWindow();
            return false;
        }
        if (!ValidationUtil.isNumeric(txtPrice.getText())) {
            UIComponents.showWarning(this, "Price must be a valid number.");
            txtPrice.requestFocusInWindow();
            return false;
        }
        return true;
    }
}
