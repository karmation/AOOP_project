package com.hotel.ui;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.CustomerDAO;
import com.hotel.dao.PaymentDAO;
import com.hotel.dao.RoomDAO;
import com.hotel.model.Booking;
import com.hotel.model.Payment;
import com.hotel.model.User;
import com.hotel.ui.theme.AppTheme;
import com.hotel.ui.theme.UIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Comprehensive Hotel Administration Dashboard displaying real-time metrics,
 * recent bookings, today's arrivals/departures, and occupancy visualization.
 */
public class DashboardPanel extends JPanel {

    private final User currentUser;
    private final RoomDAO roomDAO = new RoomDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();

    // Metric Cards Panels
    private JPanel statsGrid;
    private JTable tblRecentBookings;
    private DefaultTableModel modelRecentBookings;
    private JTable tblActivity;
    private DefaultTableModel modelActivity;
    private OccupancyBarChart chartPanel;
    private JLabel lblStatusNotice;
    private JButton btnRefresh;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public DashboardPanel() {
        this(new User(1, "admin", "", "ADMIN"));
    }

    public DashboardPanel(User user) {
        this.currentUser = (user != null) ? user : new User(1, "admin", "", "ADMIN");
        setLayout(new BorderLayout(16, 16));
        setBackground(AppTheme.BACKGROUND);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initComponents();
        loadDashboardData();
    }

    private void initComponents() {
        // =====================================================================
        // TOP: Action & Title Bar
        // =====================================================================
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Operations Dashboard");
        lblTitle.setFont(AppTheme.FONT_TITLE);
        lblTitle.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Real-time property metrics, guest activity, and revenue analytics");
        lblSub.setFont(AppTheme.FONT_BODY);
        lblSub.setForeground(AppTheme.TEXT_SECONDARY);

        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(lblSub);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        lblStatusNotice = new JLabel("Live Data Connected");
        lblStatusNotice.setFont(AppTheme.FONT_SMALL);
        lblStatusNotice.setForeground(AppTheme.SUCCESS);

        btnRefresh = UIComponents.createSecondaryButton("⟳  Refresh Dashboard");
        btnRefresh.addActionListener(e -> loadDashboardData());

        actionPanel.add(lblStatusNotice);
        actionPanel.add(btnRefresh);

        topBar.add(titlePanel, BorderLayout.WEST);
        topBar.add(actionPanel, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // =====================================================================
        // CENTER: Scrollable Dashboard Content
        // =====================================================================
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // 1. Stats Cards Grid (2 rows x 4 columns)
        statsGrid = new JPanel(new GridLayout(2, 4, 12, 12));
        statsGrid.setOpaque(false);
        statsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));
        statsGrid.setPreferredSize(new Dimension(1000, 190));

        // Placeholders until data loads
        for (int i = 0; i < 8; i++) {
            statsGrid.add(UIComponents.createStatCard("Loading...", "-", "Connecting...", AppTheme.PRIMARY_BLUE));
        }

        contentPanel.add(statsGrid);
        contentPanel.add(Box.createVerticalStrut(20));

        // 2. Middle Row: Recent Bookings (60%) & Occupancy Chart (40%)
        JPanel midPanel = new JPanel(new BorderLayout(16, 0));
        midPanel.setOpaque(false);
        midPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        midPanel.setPreferredSize(new Dimension(1000, 300));

        // Recent Bookings Card
        JPanel cardBookings = new JPanel(new BorderLayout(0, 10));
        cardBookings.setBackground(AppTheme.SURFACE);
        cardBookings.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                new EmptyBorder(14, 16, 14, 16)));

        JPanel headBookings = UIComponents.createSectionHeader("Recent Guest Reservations");
        cardBookings.add(headBookings, BorderLayout.NORTH);

        String[] colsBookings = {"ID", "Customer", "Room", "Check-In", "Check-Out", "Status"};
        modelRecentBookings = new DefaultTableModel(colsBookings, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblRecentBookings = new JTable(modelRecentBookings);
        UIComponents.applyTableStyle(tblRecentBookings);
        cardBookings.add(UIComponents.createScrollPane(tblRecentBookings), BorderLayout.CENTER);

        // Occupancy & Visual Chart Card
        JPanel cardChart = new JPanel(new BorderLayout(0, 10));
        cardChart.setPreferredSize(new Dimension(380, 300));
        cardChart.setBackground(AppTheme.SURFACE);
        cardChart.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                new EmptyBorder(14, 16, 14, 16)));

        JPanel headChart = UIComponents.createSectionHeader("Room Inventory & Occupancy");
        cardChart.add(headChart, BorderLayout.NORTH);

        chartPanel = new OccupancyBarChart();
        cardChart.add(chartPanel, BorderLayout.CENTER);

        midPanel.add(cardBookings, BorderLayout.CENTER);
        midPanel.add(cardChart, BorderLayout.EAST);

        contentPanel.add(midPanel);
        contentPanel.add(Box.createVerticalStrut(20));

        // 3. Bottom Row: Today's Activity Card
        JPanel cardActivity = new JPanel(new BorderLayout(0, 10));
        cardActivity.setBackground(AppTheme.SURFACE);
        cardActivity.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        cardActivity.setPreferredSize(new Dimension(1000, 260));
        cardActivity.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                new EmptyBorder(14, 16, 14, 16)));

        JPanel headActivity = UIComponents.createSectionHeader("Today's Front-Desk Activity (Arrivals, Departures & Payments)");
        cardActivity.add(headActivity, BorderLayout.NORTH);

        String[] colsActivity = {"Activity Type", "Reference", "Guest / Details", "Room / Amount", "Time / Date", "Status"};
        modelActivity = new DefaultTableModel(colsActivity, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblActivity = new JTable(modelActivity);
        UIComponents.applyTableStyle(tblActivity);
        cardActivity.add(UIComponents.createScrollPane(tblActivity), BorderLayout.CENTER);

        contentPanel.add(cardActivity);

        JScrollPane scrollPane = UIComponents.createScrollPane(contentPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void loadDashboardData() {
        btnRefresh.setEnabled(false);
        lblStatusNotice.setText("Refreshing from MySQL...");
        lblStatusNotice.setForeground(AppTheme.WARNING);

        SwingWorker<DashboardData, Void> worker = new SwingWorker<>() {
            @Override
            protected DashboardData doInBackground() {
                DashboardData data = new DashboardData();
                data.totalRooms = roomDAO.getTotalRooms();
                data.availableRooms = roomDAO.countByStatus("AVAILABLE");
                data.occupiedRooms = roomDAO.countByStatus("OCCUPIED");
                data.totalCustomers = customerDAO.getTotalCustomers();
                data.activeBookings = bookingDAO.getActiveBookingsCount();
                data.todayCheckIns = bookingDAO.getTodayCheckIns();
                data.todayCheckOuts = bookingDAO.getTodayCheckOuts();
                data.totalRevenue = paymentDAO.getTotalRevenue();
                data.recentBookings = bookingDAO.getRecentBookings();
                data.recentPayments = paymentDAO.getRecentPayments(6);
                return data;
            }

            @Override
            protected void done() {
                btnRefresh.setEnabled(true);
                lblStatusNotice.setText("Live Data Connected");
                lblStatusNotice.setForeground(AppTheme.SUCCESS);

                try {
                    DashboardData data = get();
                    updateUI(data);
                } catch (Exception ex) {
                    lblStatusNotice.setText("Error loading live data");
                    lblStatusNotice.setForeground(AppTheme.DANGER);
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void updateUI(DashboardData data) {
        // Update Stats Cards
        statsGrid.removeAll();

        statsGrid.add(UIComponents.createStatCard(
                "Total Rooms",
                String.valueOf(data.totalRooms),
                "Total inventory registered",
                AppTheme.PRIMARY_BLUE));

        statsGrid.add(UIComponents.createStatCard(
                "Available Rooms",
                String.valueOf(data.availableRooms),
                "Ready for immediate check-in",
                AppTheme.SUCCESS));

        statsGrid.add(UIComponents.createStatCard(
                "Occupied Rooms",
                String.valueOf(data.occupiedRooms),
                "Currently hosted guests",
                AppTheme.DANGER));

        statsGrid.add(UIComponents.createStatCard(
                "Total Customers",
                String.valueOf(data.totalCustomers),
                "Registered guest profiles",
                AppTheme.PRIMARY_NAVY));

        statsGrid.add(UIComponents.createStatCard(
                "Active Bookings",
                String.valueOf(data.activeBookings),
                "Currently active stays",
                AppTheme.PRIMARY_BLUE));

        statsGrid.add(UIComponents.createStatCard(
                "Today's Check-ins",
                String.valueOf(data.todayCheckIns.size()),
                "Scheduled arrivals today",
                AppTheme.WARNING));

        statsGrid.add(UIComponents.createStatCard(
                "Today's Check-outs",
                String.valueOf(data.todayCheckOuts.size()),
                "Scheduled departures today",
                new Color(0x9B, 0x59, 0xB6)));

        String revenueStr = currencyFormat.format(data.totalRevenue);
        statsGrid.add(UIComponents.createStatCard(
                "Total Revenue",
                revenueStr,
                "Lifetime payments collected",
                AppTheme.SUCCESS));

        statsGrid.revalidate();
        statsGrid.repaint();

        // Update Recent Bookings Table
        modelRecentBookings.setRowCount(0);
        for (Booking b : data.recentBookings) {
            String custName = b.getCustomer() != null ? b.getCustomer().getName() : ("Customer #" + b.getCustomerId());
            String roomNum = b.getRoom() != null ? b.getRoom().getRoomNumber() : ("Room #" + b.getRoomId());
            modelRecentBookings.addRow(new Object[]{
                    "#" + b.getBookingId(),
                    custName,
                    roomNum,
                    b.getCheckIn(),
                    b.getCheckOut(),
                    b.getStatus()
            });
        }

        // Update Today's Activity Table
        modelActivity.setRowCount(0);
        for (Booking b : data.todayCheckIns) {
            String custName = b.getCustomer() != null ? b.getCustomer().getName() : ("Guest #" + b.getCustomerId());
            String roomNum = b.getRoom() != null ? b.getRoom().getRoomNumber() : ("Room #" + b.getRoomId());
            modelActivity.addRow(new Object[]{"Check-In", "#" + b.getBookingId(), custName, roomNum, "Today", "ARRIVING"});
        }
        for (Booking b : data.todayCheckOuts) {
            String custName = b.getCustomer() != null ? b.getCustomer().getName() : ("Guest #" + b.getCustomerId());
            String roomNum = b.getRoom() != null ? b.getRoom().getRoomNumber() : ("Room #" + b.getRoomId());
            modelActivity.addRow(new Object[]{"Check-Out", "#" + b.getBookingId(), custName, roomNum, "Today", "DEPARTING"});
        }
        for (Payment p : data.recentPayments) {
            modelActivity.addRow(new Object[]{
                    "Payment",
                    "Pay #" + p.getPaymentId(),
                    "Booking #" + p.getBookingId(),
                    currencyFormat.format(p.getAmount()),
                    p.getPaymentDate() != null ? p.getPaymentDate().toString().substring(0, 16) : "-",
                    p.getPaymentMethod()
            });
        }
        if (modelActivity.getRowCount() == 0) {
            modelActivity.addRow(new Object[]{"Info", "-", "No check-ins, departures, or payments recorded for today", "-", "-", "IDLE"});
        }

        // Update Chart
        chartPanel.setData(data.totalRooms, data.availableRooms, data.occupiedRooms);
    }

    // =========================================================================
    // Java2D Occupancy Chart Component
    // =========================================================================
    private static class OccupancyBarChart extends JPanel {
        private int total = 0;
        private int available = 0;
        private int occupied = 0;

        public OccupancyBarChart() {
            setOpaque(false);
            setPreferredSize(new Dimension(340, 200));
        }

        public void setData(int total, int available, int occupied) {
            this.total = total;
            this.available = available;
            this.occupied = occupied;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth() - 32;
            int h = getHeight();

            if (total == 0) {
                g2.setColor(AppTheme.TEXT_SECONDARY);
                g2.setFont(AppTheme.FONT_BODY);
                g2.drawString("No room inventory data", 20, h / 2);
                g2.dispose();
                return;
            }

            double occRate = (double) occupied / total * 100.0;
            double availRate = (double) available / total * 100.0;

            // Draw Summary Header
            g2.setColor(AppTheme.TEXT_PRIMARY);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
            g2.drawString(String.format("%.1f%%", occRate), 16, 32);

            g2.setFont(AppTheme.FONT_SMALL);
            g2.setColor(AppTheme.TEXT_SECONDARY);
            g2.drawString("Current Occupancy Rate", 16, 50);

            // Draw Stacked Horizontal Progress Bar
            int barY = 70;
            int barH = 26;
            int barW = w;

            int occW = (int) (barW * (occRate / 100.0));
            int availW = (int) (barW * (availRate / 100.0));
            int otherW = barW - occW - availW;

            // Background bar
            g2.setColor(AppTheme.BORDER);
            g2.fillRoundRect(16, barY, barW, barH, 8, 8);

            // Occupied segment (Red)
            if (occW > 0) {
                g2.setColor(AppTheme.DANGER);
                g2.fillRoundRect(16, barY, Math.min(barW, Math.max(10, occW)), barH, 8, 8);
            }

            // Available segment (Green)
            if (availW > 0) {
                g2.setColor(AppTheme.SUCCESS);
                g2.fillRect(16 + occW, barY, availW, barH);
            }

            // Other segment (Maintenance/Reserved)
            if (otherW > 0) {
                g2.setColor(AppTheme.WARNING);
                g2.fillRect(16 + occW + availW, barY, otherW, barH);
            }

            // Legend Items
            int legendY = 130;
            drawLegendItem(g2, 16, legendY, AppTheme.DANGER, "Occupied: " + occupied + " (" + String.format("%.0f%%", occRate) + ")");
            drawLegendItem(g2, 16, legendY + 24, AppTheme.SUCCESS, "Available: " + available + " (" + String.format("%.0f%%", availRate) + ")");
            int others = total - occupied - available;
            drawLegendItem(g2, 16, legendY + 48, AppTheme.WARNING, "Other / Maint: " + Math.max(0, others));

            g2.dispose();
        }

        private void drawLegendItem(Graphics2D g2, int x, int y, Color color, String text) {
            g2.setColor(color);
            g2.fillRoundRect(x, y - 10, 12, 12, 4, 4);
            g2.setColor(AppTheme.TEXT_PRIMARY);
            g2.setFont(AppTheme.FONT_BODY);
            g2.drawString(text, x + 20, y);
        }
    }

    private static class DashboardData {
        int totalRooms;
        int availableRooms;
        int occupiedRooms;
        int totalCustomers;
        int activeBookings;
        double totalRevenue;
        List<Booking> todayCheckIns;
        List<Booking> todayCheckOuts;
        List<Booking> recentBookings;
        List<Payment> recentPayments;
    }
}

