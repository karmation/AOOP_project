package com.hotel.ui;

import com.hotel.model.User;
import com.hotel.ui.theme.AppTheme;
import com.hotel.ui.theme.NavButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Main application window for the Grand Hotel Management System.
 * Features a fixed-width navigation sidebar, modern top header with real-time clock,
 * user role badge, and smooth CardLayout page switching.
 */
public class DashboardFrame extends JFrame {

    private final User currentUser;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private JLabel lblPageTitle;
    private JLabel lblClock;

    // Navigation buttons
    private final List<NavButton> navButtons = new ArrayList<>();
    private NavButton btnNavDashboard;
    private NavButton btnNavRooms;
    private NavButton btnNavCustomers;
    private NavButton btnNavBookings;
    private NavButton btnNavServices;
    private NavButton btnNavBilling;
    private NavButton btnNavReports;

    // Panels
    private DashboardPanel dashboardPanel;
    private RoomPanel roomPanel;
    private CustomerPanel customerPanel;
    private BookingPanel bookingPanel;
    private ServicePanel servicePanel;
    private BillingPanel billingPanel;
    private ReportsPanel reportsPanel;

    private final SimpleDateFormat clockFormat = new SimpleDateFormat("EEE, dd MMM yyyy   HH:mm:ss");

    public DashboardFrame() {
        this(new User(1, "admin", "", "ADMIN"));
    }

    public DashboardFrame(User user) {
        this.currentUser = user != null ? user : new User(1, "admin", "", "ADMIN");

        setTitle("Grand Hotel Management System • " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        setSize(1300, 820);
        setMinimumSize(new Dimension(1050, 680));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        setupKeyboardShortcuts();
        startClockTimer();

        // Select Dashboard by default
        selectCard("Dashboard", "Operations Dashboard", btnNavDashboard);
    }

    private void initComponents() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(AppTheme.BACKGROUND);

        // =====================================================================
        // 1. LEFT SIDEBAR (220px fixed width)
        // =====================================================================
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(AppTheme.PRIMARY_NAVY);
        sidebar.setPreferredSize(new Dimension(230, 800));
        sidebar.setBorder(new EmptyBorder(16, 10, 16, 10));

        // Hotel Brand Area
        JPanel brandBox = new JPanel();
        brandBox.setLayout(new BoxLayout(brandBox, BoxLayout.Y_AXIS));
        brandBox.setOpaque(false);
        brandBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        brandBox.setBorder(new EmptyBorder(8, 12, 20, 12));

        JLabel lblStars = new JLabel("★ ★ ★ ★ ★");
        lblStars.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStars.setForeground(AppTheme.WARNING);
        lblStars.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblBrandTitle = new JLabel("GRAND HOTEL");
        lblBrandTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblBrandTitle.setForeground(Color.WHITE);
        lblBrandTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblBrandSub = new JLabel("MANAGEMENT SUITE");
        lblBrandSub.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblBrandSub.setForeground(new Color(170, 190, 220));
        lblBrandSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        brandBox.add(lblStars);
        brandBox.add(Box.createVerticalStrut(4));
        brandBox.add(lblBrandTitle);
        brandBox.add(Box.createVerticalStrut(2));
        brandBox.add(lblBrandSub);

        sidebar.add(brandBox);

        // Separator
        JPanel sep = new JPanel();
        sep.setBackground(AppTheme.SECONDARY_NAVY);
        sep.setMaximumSize(new Dimension(210, 1));
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(14));

        // Navigation Buttons
        btnNavDashboard = createSidebarButton("⊞  Dashboard", "Dashboard", "Operations Dashboard");
        btnNavRooms     = createSidebarButton("🛏  Rooms", "Rooms", "Room Inventory & Floor Plan");
        btnNavCustomers = createSidebarButton("👤  Customers", "Customers", "Guest Directory & Registration");
        btnNavBookings  = createSidebarButton("📅  Bookings", "Bookings", "Reservations & Check-In Workflow");
        btnNavServices  = createSidebarButton("🛎  Services", "Services", "Guest Amenities & Service Orders");
        btnNavBilling   = createSidebarButton("💳  Billing & Checkout", "Billing", "Billing Summary, Payments & Invoices");
        btnNavReports   = createSidebarButton("📊  Reports & Analytics", "Reports", "Revenue, Occupancy & Business Reports");

        sidebar.add(btnNavDashboard);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(btnNavRooms);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(btnNavCustomers);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(btnNavBookings);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(btnNavServices);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(btnNavBilling);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(btnNavReports);

        sidebar.add(Box.createVerticalGlue());

        // Logout Button
        JButton btnLogout = new JButton("🚪  Sign Out");
        btnLogout.setFont(AppTheme.FONT_SUBHEADING);
        btnLogout.setForeground(new Color(255, 140, 140));
        btnLogout.setHorizontalAlignment(SwingConstants.LEFT);
        btnLogout.setBorderPainted(false);
        btnLogout.setContentAreaFilled(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.setBorder(new EmptyBorder(10, 20, 10, 20));
        btnLogout.setMaximumSize(new Dimension(220, 44));
        btnLogout.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogout.addActionListener(e -> {
            int res = JOptionPane.showConfirmDialog(this, "Are you sure you want to sign out?", "Confirm Sign Out",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (res == JOptionPane.YES_OPTION) {
                dispose();
                SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
            }
        });
        sidebar.add(btnLogout);

        // =====================================================================
        // 2. TOP HEADER BAR (60px height)
        // =====================================================================
        JPanel headerBar = new JPanel(new BorderLayout(16, 0));
        headerBar.setPreferredSize(new Dimension(1000, 60));
        headerBar.setBackground(AppTheme.SURFACE);
        headerBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER),
                new EmptyBorder(0, 24, 0, 24)));

        // Left: Current Page Title
        lblPageTitle = new JLabel("Operations Dashboard");
        lblPageTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblPageTitle.setForeground(AppTheme.TEXT_PRIMARY);

        // Right: Clock, User and Role
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightHeader.setOpaque(false);

        lblClock = new JLabel(clockFormat.format(new Date()));
        lblClock.setFont(AppTheme.FONT_BODY);
        lblClock.setForeground(AppTheme.TEXT_SECONDARY);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        userPanel.setOpaque(false);

        JLabel lblUserIcon = new JLabel("👤");
        lblUserIcon.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        JLabel lblUserName = new JLabel(currentUser.getUsername());
        lblUserName.setFont(AppTheme.FONT_SUBHEADING);
        lblUserName.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel lblRoleBadge = new JLabel(currentUser.getRole(), JLabel.CENTER);
        lblRoleBadge.setFont(AppTheme.FONT_SMALL);
        lblRoleBadge.setOpaque(true);
        lblRoleBadge.setBackground(AppTheme.PRIMARY_BLUE);
        lblRoleBadge.setForeground(Color.WHITE);
        lblRoleBadge.setBorder(new EmptyBorder(3, 8, 3, 8));

        userPanel.add(lblUserIcon);
        userPanel.add(lblUserName);
        userPanel.add(lblRoleBadge);

        rightHeader.add(lblClock);
        rightHeader.add(new JSeparator(SwingConstants.VERTICAL));
        rightHeader.add(userPanel);

        headerBar.add(lblPageTitle, BorderLayout.WEST);
        headerBar.add(rightHeader, BorderLayout.EAST);

        // =====================================================================
        // 3. MAIN CONTENT (CardLayout)
        // =====================================================================
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(AppTheme.BACKGROUND);

        dashboardPanel = new DashboardPanel(currentUser);
        roomPanel = new RoomPanel();
        customerPanel = new CustomerPanel();
        bookingPanel = new BookingPanel();
        servicePanel = new ServicePanel();
        billingPanel = new BillingPanel();
        reportsPanel = new ReportsPanel();

        cardPanel.add(dashboardPanel, "Dashboard");
        cardPanel.add(roomPanel, "Rooms");
        cardPanel.add(customerPanel, "Customers");
        cardPanel.add(bookingPanel, "Bookings");
        cardPanel.add(servicePanel, "Services");
        cardPanel.add(billingPanel, "Billing");
        cardPanel.add(reportsPanel, "Reports");

        // Assemble main frame
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.add(headerBar, BorderLayout.NORTH);
        centerContainer.add(cardPanel, BorderLayout.CENTER);

        rootPanel.add(sidebar, BorderLayout.WEST);
        rootPanel.add(centerContainer, BorderLayout.CENTER);

        setContentPane(rootPanel);
    }

    private NavButton createSidebarButton(String text, String cardName, String title) {
        NavButton btn = new NavButton(text);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.addActionListener(e -> selectCard(cardName, title, btn));
        navButtons.add(btn);
        return btn;
    }

    public void selectCard(String cardName, String title, NavButton targetBtn) {
        for (NavButton b : navButtons) {
            b.setSelected(false);
        }
        if (targetBtn != null) {
            targetBtn.setSelected(true);
        }
        cardLayout.show(cardPanel, cardName);
        lblPageTitle.setText(title);

        // Auto-refresh when entering screens
        if ("Dashboard".equals(cardName) && dashboardPanel != null) {
            dashboardPanel.loadDashboardData();
        } else if ("Rooms".equals(cardName) && roomPanel != null) {
            roomPanel.loadRoomData();
        } else if ("Customers".equals(cardName) && customerPanel != null) {
            customerPanel.loadCustomerData();
        } else if ("Bookings".equals(cardName) && bookingPanel != null) {
            bookingPanel.loadData();
        } else if ("Services".equals(cardName) && servicePanel != null) {
            servicePanel.loadData();
        } else if ("Billing".equals(cardName) && billingPanel != null) {
            billingPanel.loadData();
        } else if ("Reports".equals(cardName) && reportsPanel != null) {
            reportsPanel.loadReportData();
        }
    }

    private void setupKeyboardShortcuts() {
        JRootPane root = getRootPane();

        // Ctrl+1 through Ctrl+7 for fast navigation
        bindKey(root, "ctrl 1", () -> selectCard("Dashboard", "Operations Dashboard", btnNavDashboard));
        bindKey(root, "ctrl 2", () -> selectCard("Rooms", "Room Inventory & Floor Plan", btnNavRooms));
        bindKey(root, "ctrl 3", () -> selectCard("Customers", "Guest Directory & Registration", btnNavCustomers));
        bindKey(root, "ctrl 4", () -> selectCard("Bookings", "Reservations & Check-In Workflow", btnNavBookings));
        bindKey(root, "ctrl 5", () -> selectCard("Services", "Guest Amenities & Service Orders", btnNavServices));
        bindKey(root, "ctrl 6", () -> selectCard("Billing", "Billing Summary, Payments & Invoices", btnNavBilling));
        bindKey(root, "ctrl 7", () -> selectCard("Reports", "Revenue, Occupancy & Business Reports", btnNavReports));

        // F5 to refresh active card
        bindKey(root, "F5", () -> {
            if (btnNavDashboard.isSelected()) dashboardPanel.loadDashboardData();
            else if (btnNavRooms.isSelected()) roomPanel.loadRoomData();
            else if (btnNavCustomers.isSelected()) customerPanel.loadCustomerData();
            else if (btnNavBookings.isSelected()) bookingPanel.loadData();
            else if (btnNavServices.isSelected()) servicePanel.loadData();
            else if (btnNavBilling.isSelected()) billingPanel.loadData();
            else if (btnNavReports.isSelected()) reportsPanel.loadReportData();
        });
    }

    private void bindKey(JRootPane root, String keyStrokeStr, Runnable action) {
        KeyStroke ks = KeyStroke.getKeyStroke(keyStrokeStr);
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, keyStrokeStr);
        root.getActionMap().put(keyStrokeStr, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    private void startClockTimer() {
        Timer timer = new Timer(1000, e -> lblClock.setText(clockFormat.format(new Date())));
        timer.setRepeats(true);
        timer.start();
    }
}
