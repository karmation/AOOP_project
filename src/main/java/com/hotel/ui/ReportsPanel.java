package com.hotel.ui;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.CustomerDAO;
import com.hotel.dao.PaymentDAO;
import com.hotel.dao.RoomDAO;
import com.hotel.model.Booking;
import com.hotel.model.Payment;
import com.hotel.model.Room;
import com.hotel.ui.theme.AppTheme;
import com.hotel.ui.theme.UIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Modern Reporting, Business Analytics & Data Export Screen.
 * Provides Date Range Filtering (Today, Week, Month, Custom),
 * Tabbed Reporting (Overview, Booking History, Payment Ledger, Room Occupancy),
 * and one-click CSV Data Export.
 */
public class ReportsPanel extends JPanel {

    private final BookingDAO bookingDAO = new BookingDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final RoomDAO roomDAO = new RoomDAO();

    // Date Range Filter Controls
    private JRadioButton rbToday;
    private JRadioButton rbWeek;
    private JRadioButton rbMonth;
    private JRadioButton rbCustom;
    private JTextField txtFromDate;
    private JTextField txtToDate;
    private JButton btnApplyFilter;
    private JButton btnExportCsv;

    // Tabs
    private JTabbedPane tabbedPane;

    // Tab 1: Overview
    private JPanel overviewStatsGrid;
    private RevenueComparisonChart revChart;

    // Tab 2: Bookings Table
    private JTable tblBookings;
    private DefaultTableModel modelBookings;
    private JLabel lblBookingsCount;

    // Tab 3: Payments Table
    private JTable tblPayments;
    private DefaultTableModel modelPayments;
    private JLabel lblPaymentsSum;

    // Tab 4: Room Occupancy Table
    private JTable tblOccupancy;
    private DefaultTableModel modelOccupancy;

    // Cached Data
    private List<Booking> allBookings = new ArrayList<>();
    private List<Payment> allPayments = new ArrayList<>();
    private List<Room> allRooms = new ArrayList<>();

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public ReportsPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(AppTheme.BACKGROUND);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initComponents();
        loadReportData();
    }

    private void initComponents() {
        // =====================================================================
        // TOP: Filter Bar Card
        // =====================================================================
        JPanel filterCard = new JPanel(new BorderLayout(12, 8));
        filterCard.setBackground(AppTheme.SURFACE);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                new EmptyBorder(12, 16, 12, 16)));

        JPanel leftTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftTitle.setOpaque(false);
        leftTitle.add(UIComponents.createSectionHeader("Hotel Analytics & Business Reports"));

        // Filter Options
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterRow.setOpaque(false);

        rbToday = new JRadioButton("Today");
        rbWeek = new JRadioButton("This Week");
        rbMonth = new JRadioButton("This Month");
        rbMonth.setSelected(true);
        rbCustom = new JRadioButton("Custom Range:");

        ButtonGroup bg = new ButtonGroup();
        bg.add(rbToday);
        bg.add(rbWeek);
        bg.add(rbMonth);
        bg.add(rbCustom);

        Calendar cal = Calendar.getInstance();
        txtToDate = UIComponents.createTextField();
        txtToDate.setText(dateFormat.format(cal.getTime()));
        txtToDate.setPreferredSize(new Dimension(100, 32));

        cal.set(Calendar.DAY_OF_MONTH, 1);
        txtFromDate = UIComponents.createTextField();
        txtFromDate.setText(dateFormat.format(cal.getTime()));
        txtFromDate.setPreferredSize(new Dimension(100, 32));

        rbToday.addActionListener(e -> setDateRangeToday());
        rbWeek.addActionListener(e -> setDateRangeWeek());
        rbMonth.addActionListener(e -> setDateRangeMonth());

        btnApplyFilter = UIComponents.createPrimaryButton("Apply Filter");
        btnApplyFilter.setFont(AppTheme.FONT_SUBHEADING);
        btnApplyFilter.addActionListener(e -> applyDateFilter());

        btnExportCsv = UIComponents.createSecondaryButton("📥  Export CSV");
        btnExportCsv.addActionListener(e -> exportCurrentTabToCsv());

        filterRow.add(rbToday);
        filterRow.add(rbWeek);
        filterRow.add(rbMonth);
        filterRow.add(rbCustom);
        filterRow.add(new JLabel("From:"));
        filterRow.add(txtFromDate);
        filterRow.add(new JLabel("To:"));
        filterRow.add(txtToDate);
        filterRow.add(btnApplyFilter);
        filterRow.add(btnExportCsv);

        filterCard.add(leftTitle, BorderLayout.WEST);
        filterCard.add(filterRow, BorderLayout.EAST);

        add(filterCard, BorderLayout.NORTH);

        // =====================================================================
        // CENTER: Tabbed Reports
        // =====================================================================
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(AppTheme.FONT_SUBHEADING);
        tabbedPane.setBackground(AppTheme.SURFACE);

        // TAB 1: Overview
        tabbedPane.addTab("  📈  Executive Overview  ", createOverviewTab());

        // TAB 2: Bookings History
        tabbedPane.addTab("  📅  Reservations Ledger  ", createBookingsTab());

        // TAB 3: Payment History
        tabbedPane.addTab("  💳  Payment Transactions  ", createPaymentsTab());

        // TAB 4: Room Occupancy Breakdown
        tabbedPane.addTab("  🛏  Room Occupancy & Yield  ", createOccupancyTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createOverviewTab() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBackground(AppTheme.BACKGROUND);
        panel.setBorder(new EmptyBorder(16, 12, 16, 12));

        // Stats Grid
        overviewStatsGrid = new JPanel(new GridLayout(2, 4, 12, 12));
        overviewStatsGrid.setOpaque(false);
        overviewStatsGrid.setPreferredSize(new Dimension(1000, 190));
        panel.add(overviewStatsGrid, BorderLayout.NORTH);

        // Visual Comparison Chart
        revChart = new RevenueComparisonChart();
        JPanel chartCard = new JPanel(new BorderLayout());
        chartCard.setBackground(AppTheme.SURFACE);
        chartCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                new EmptyBorder(16, 16, 16, 16)));
        chartCard.add(UIComponents.createSectionHeader("Yield & Channel Distribution (Java2D Engine)"), BorderLayout.NORTH);
        chartCard.add(revChart, BorderLayout.CENTER);

        panel.add(chartCard, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBookingsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(AppTheme.SURFACE);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        String[] cols = {"ID", "Guest Name", "Phone", "Room", "Category", "Check-In", "Check-Out", "Adults", "Children", "Status"};
        modelBookings = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblBookings = new JTable(modelBookings);
        UIComponents.applyTableStyle(tblBookings);

        lblBookingsCount = new JLabel("Showing 0 bookings in range");
        lblBookingsCount.setFont(AppTheme.FONT_SUBHEADING);
        lblBookingsCount.setForeground(AppTheme.TEXT_SECONDARY);

        panel.add(lblBookingsCount, BorderLayout.NORTH);
        panel.add(UIComponents.createScrollPane(tblBookings), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPaymentsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(AppTheme.SURFACE);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        String[] cols = {"Payment ID", "Booking Ref", "Settled Amount (₹)", "Payment Method", "Timestamp"};
        modelPayments = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblPayments = new JTable(modelPayments);
        UIComponents.applyTableStyle(tblPayments);

        lblPaymentsSum = new JLabel("Total Collected in Range: ₹0.00");
        lblPaymentsSum.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPaymentsSum.setForeground(AppTheme.SUCCESS);

        panel.add(lblPaymentsSum, BorderLayout.NORTH);
        panel.add(UIComponents.createScrollPane(tblPayments), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createOccupancyTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(AppTheme.SURFACE);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        String[] cols = {"Room Number", "Category", "Nightly Rate (₹)", "Current Status", "Bookings Handled"};
        modelOccupancy = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblOccupancy = new JTable(modelOccupancy);
        UIComponents.applyTableStyle(tblOccupancy);

        JLabel lblTitle = new JLabel("Room Inventory Yield & Usage Status");
        lblTitle.setFont(AppTheme.FONT_SUBHEADING);
        lblTitle.setForeground(AppTheme.TEXT_SECONDARY);

        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(UIComponents.createScrollPane(tblOccupancy), BorderLayout.CENTER);
        return panel;
    }

    public void loadReportData() {
        btnApplyFilter.setEnabled(false);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                allBookings = bookingDAO.getAllBookings();
                allPayments = paymentDAO.getAllPayments();
                allRooms = roomDAO.getAllRooms();
                return null;
            }

            @Override
            protected void done() {
                btnApplyFilter.setEnabled(true);
                applyDateFilter();
            }
        };
        worker.execute();
    }

    private void setDateRangeToday() {
        String today = dateFormat.format(new Date());
        txtFromDate.setText(today);
        txtToDate.setText(today);
        applyDateFilter();
    }

    private void setDateRangeWeek() {
        Calendar c = Calendar.getInstance();
        txtToDate.setText(dateFormat.format(c.getTime()));
        c.add(Calendar.DAY_OF_YEAR, -7);
        txtFromDate.setText(dateFormat.format(c.getTime()));
        applyDateFilter();
    }

    private void setDateRangeMonth() {
        Calendar c = Calendar.getInstance();
        txtToDate.setText(dateFormat.format(c.getTime()));
        c.set(Calendar.DAY_OF_MONTH, 1);
        txtFromDate.setText(dateFormat.format(c.getTime()));
        applyDateFilter();
    }

    private void applyDateFilter() {
        String fromStr = txtFromDate.getText().trim();
        String toStr = txtToDate.getText().trim();

        // Filter Bookings
        modelBookings.setRowCount(0);
        int activeCount = 0;
        int completedCount = 0;
        int cancelledCount = 0;

        for (Booking b : allBookings) {
            String checkInStr = b.getCheckIn().toString();
            if (checkInStr.compareTo(fromStr) >= 0 && checkInStr.compareTo(toStr) <= 0) {
                String cName = b.getCustomer() != null ? b.getCustomer().getName() : ("Cust #" + b.getCustomerId());
                String cPhone = b.getCustomer() != null ? b.getCustomer().getPhone() : "-";
                String rNum = b.getRoom() != null ? b.getRoom().getRoomNumber() : ("Room #" + b.getRoomId());
                String rType = b.getRoom() != null ? b.getRoom().getRoomType() : "-";

                modelBookings.addRow(new Object[]{
                        "#" + b.getBookingId(),
                        cName,
                        cPhone,
                        rNum,
                        rType,
                        b.getCheckIn(),
                        b.getCheckOut(),
                        b.getAdults(),
                        b.getChildren(),
                        b.getStatus()
                });

                if ("ACTIVE".equalsIgnoreCase(b.getStatus())) activeCount++;
                else if ("COMPLETED".equalsIgnoreCase(b.getStatus())) completedCount++;
                else if ("CANCELLED".equalsIgnoreCase(b.getStatus())) cancelledCount++;
            }
        }
        lblBookingsCount.setText("Showing " + modelBookings.getRowCount() + " reservations checked in between " + fromStr + " and " + toStr);

        // Filter Payments
        modelPayments.setRowCount(0);
        double rangeRevenue = 0.0;
        for (Payment p : allPayments) {
            if (p.getPaymentDate() != null) {
                String payDateStr = p.getPaymentDate().toString().substring(0, 10);
                if (payDateStr.compareTo(fromStr) >= 0 && payDateStr.compareTo(toStr) <= 0) {
                    rangeRevenue += p.getAmount();
                    modelPayments.addRow(new Object[]{
                            "PAY-" + p.getPaymentId(),
                            "#" + p.getBookingId(),
                            currencyFormat.format(p.getAmount()),
                            p.getPaymentMethod(),
                            p.getPaymentDate().toString().substring(0, 19)
                    });
                }
            }
        }
        lblPaymentsSum.setText("Total Collections in Range (" + fromStr + " to " + toStr + "): " + currencyFormat.format(rangeRevenue));

        // Populate Room Occupancy
        modelOccupancy.setRowCount(0);
        int totalRooms = allRooms.size();
        int availRooms = 0;
        int occRooms = 0;

        for (Room r : allRooms) {
            int bookingsHandled = 0;
            for (Booking b : allBookings) {
                if (b.getRoomId() == r.getRoomId()) bookingsHandled++;
            }
            if ("AVAILABLE".equalsIgnoreCase(r.getStatus())) availRooms++;
            else if ("OCCUPIED".equalsIgnoreCase(r.getStatus())) occRooms++;

            modelOccupancy.addRow(new Object[]{
                    "Room " + r.getRoomNumber(),
                    r.getRoomType(),
                    currencyFormat.format(r.getPricePerDay()),
                    r.getStatus(),
                    bookingsHandled + " Stays"
            });
        }

        // Update Overview Stat Cards
        overviewStatsGrid.removeAll();
        overviewStatsGrid.add(UIComponents.createStatCard("Range Revenue", currencyFormat.format(rangeRevenue), fromStr + " to " + toStr, AppTheme.SUCCESS));
        overviewStatsGrid.add(UIComponents.createStatCard("Total Bookings", String.valueOf(modelBookings.getRowCount()), "Reservations in range", AppTheme.PRIMARY_BLUE));
        overviewStatsGrid.add(UIComponents.createStatCard("Active Stays", String.valueOf(activeCount), "Currently in-house", AppTheme.PRIMARY_NAVY));
        overviewStatsGrid.add(UIComponents.createStatCard("Completed Stays", String.valueOf(completedCount), "Successfully checked out", AppTheme.SUCCESS));
        overviewStatsGrid.add(UIComponents.createStatCard("Cancelled Stays", String.valueOf(cancelledCount), "Cancelled by guest", AppTheme.DANGER));
        overviewStatsGrid.add(UIComponents.createStatCard("Available Rooms", String.valueOf(availRooms), "Out of " + totalRooms + " total", AppTheme.SUCCESS));
        overviewStatsGrid.add(UIComponents.createStatCard("Occupied Rooms", String.valueOf(occRooms), "Currently occupied", AppTheme.DANGER));
        overviewStatsGrid.add(UIComponents.createStatCard("Total Customers", String.valueOf(customerDAO.getTotalCustomers()), "Registered guest records", AppTheme.PRIMARY_BLUE));

        overviewStatsGrid.revalidate();
        overviewStatsGrid.repaint();

        // Update Chart
        revChart.setData(rangeRevenue, modelBookings.getRowCount(), occRooms, totalRooms);
    }

    private void exportCurrentTabToCsv() {
        JTable currentTable;
        int selectedTab = tabbedPane.getSelectedIndex();
        String defaultFileName;

        if (selectedTab == 1) {
            currentTable = tblBookings;
            defaultFileName = "hotel_bookings_report.csv";
        } else if (selectedTab == 2) {
            currentTable = tblPayments;
            defaultFileName = "hotel_payments_ledger.csv";
        } else if (selectedTab == 3) {
            currentTable = tblOccupancy;
            defaultFileName = "hotel_occupancy_report.csv";
        } else {
            currentTable = tblBookings; // default fallback
            defaultFileName = "hotel_summary_report.csv";
        }

        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(defaultFileName));
        fc.setFileFilter(new FileNameExtensionFilter("CSV Comma-Separated Values (*.csv)", "csv"));

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getParentFile(), file.getName() + ".csv");
            }

            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                // Header
                for (int c = 0; c < currentTable.getColumnCount(); c++) {
                    pw.print("\"" + currentTable.getColumnName(c) + "\"");
                    if (c < currentTable.getColumnCount() - 1) pw.print(",");
                }
                pw.println();

                // Rows
                for (int r = 0; r < currentTable.getRowCount(); r++) {
                    for (int c = 0; c < currentTable.getColumnCount(); c++) {
                        Object val = currentTable.getValueAt(r, c);
                        String s = val != null ? val.toString().replace("\"", "\"\"") : "";
                        pw.print("\"" + s + "\"");
                        if (c < currentTable.getColumnCount() - 1) pw.print(",");
                    }
                    pw.println();
                }

                UIComponents.showSuccess(this, "Report successfully exported to:\n" + file.getAbsolutePath());
            } catch (Exception ex) {
                UIComponents.showError(this, "Failed to write CSV: " + ex.getMessage());
            }
        }
    }

    // =========================================================================
    // Java2D Visual Analytics Chart
    // =========================================================================
    private static class RevenueComparisonChart extends JPanel {
        private double revenue = 0.0;
        private int bookingsCount = 0;
        private int occupiedRooms = 0;
        private int totalRooms = 1;

        public RevenueComparisonChart() {
            setOpaque(false);
            setPreferredSize(new Dimension(800, 240));
        }

        public void setData(double revenue, int bookingsCount, int occupiedRooms, int totalRooms) {
            this.revenue = revenue;
            this.bookingsCount = bookingsCount;
            this.occupiedRooms = occupiedRooms;
            this.totalRooms = Math.max(1, totalRooms);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Draw Section 1: Occupancy Utilization Gauge Bar
            g2.setFont(AppTheme.FONT_SUBHEADING);
            g2.setColor(AppTheme.TEXT_PRIMARY);
            g2.drawString("Room Capacity Utilization:", 20, 30);

            double occPct = ((double) occupiedRooms / totalRooms) * 100.0;
            g2.setColor(AppTheme.TEXT_SECONDARY);
            g2.drawString(String.format("%d of %d Rooms In Use (%.1f%%)", occupiedRooms, totalRooms, occPct), 220, 30);

            int barY = 44;
            int barW = w - 40;
            int barH = 22;

            g2.setColor(AppTheme.BORDER);
            g2.fillRoundRect(20, barY, barW, barH, 6, 6);

            int fillW = (int) (barW * (occPct / 100.0));
            if (fillW > 0) {
                g2.setColor(AppTheme.PRIMARY_BLUE);
                g2.fillRoundRect(20, barY, Math.min(barW, Math.max(8, fillW)), barH, 6, 6);
            }

            // Draw Section 2: Financial KPI Visual Indicators
            int kpiY = 110;
            g2.setColor(AppTheme.TEXT_PRIMARY);
            g2.drawString("Performance Key Indicators:", 20, kpiY);

            int boxW = (w - 60) / 3;
            drawKpiBox(g2, 20, kpiY + 14, boxW, 70, "Revenue Realized", "₹" + String.format("%.2f", revenue), AppTheme.SUCCESS);
            drawKpiBox(g2, 30 + boxW, kpiY + 14, boxW, 70, "Bookings Volume", bookingsCount + " Stays", AppTheme.PRIMARY_BLUE);
            double adr = bookingsCount > 0 ? (revenue / bookingsCount) : 0.0;
            drawKpiBox(g2, 40 + boxW * 2, kpiY + 14, boxW, 70, "Avg Yield / Stay", "₹" + String.format("%.2f", adr), AppTheme.WARNING);

            g2.dispose();
        }

        private void drawKpiBox(Graphics2D g2, int x, int y, int w, int h, String title, String val, Color barColor) {
            g2.setColor(AppTheme.BACKGROUND);
            g2.fillRoundRect(x, y, w, h, 8, 8);
            g2.setColor(AppTheme.BORDER);
            g2.drawRoundRect(x, y, w, h, 8, 8);

            g2.setColor(barColor);
            g2.fillRoundRect(x, y, 4, h, 4, 4);

            g2.setColor(AppTheme.TEXT_SECONDARY);
            g2.setFont(AppTheme.FONT_SMALL);
            g2.drawString(title, x + 14, y + 24);

            g2.setColor(AppTheme.TEXT_PRIMARY);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
            g2.drawString(val, x + 14, y + 50);
        }
    }
}
