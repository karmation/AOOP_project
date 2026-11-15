package com.hotel.ui;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.PaymentDAO;
import com.hotel.dao.ServiceDAO;
import com.hotel.model.Booking;
import com.hotel.model.Customer;
import com.hotel.model.CustomerService;
import com.hotel.model.Payment;
import com.hotel.model.Room;
import com.hotel.ui.theme.AppTheme;
import com.hotel.ui.theme.UIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Professional Billing, Invoicing & Checkout Screen.
 * Features an itemized JTable bill breakdown, automated stay & amenity calculations,
 * transactional payment processing, room release, and printable Java invoices.
 */
public class BillingPanel extends JPanel {

    private final BookingDAO bookingDAO = new BookingDAO();
    private final ServiceDAO serviceDAO = new ServiceDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();

    // Top Selector
    private JComboBox<String> cbActiveBookings;
    private JButton btnRefresh;

    // Guest & Stay Info Panel
    private JLabel lblGuestName;
    private JLabel lblGuestPhone;
    private JLabel lblRoomInfo;
    private JLabel lblStayDates;
    private JLabel lblDuration;

    // Itemized Bill Table
    private JTable tblBill;
    private DefaultTableModel modelBill;

    // Summary & Payment Controls
    private JLabel lblRoomSubtotal;
    private JLabel lblServicesSubtotal;
    private JLabel lblGrandTotal;
    private JComboBox<String> cbPaymentMethod;
    private JButton btnPayAndCheckout;
    private JButton btnPrintBill;

    private List<Booking> activeBookings = new ArrayList<>();
    private Booking currentBooking = null;
    private double currentTotal = 0.0;
    private double currentRoomCharge = 0.0;
    private double currentServicesTotal = 0.0;
    private long currentNights = 1;
    private List<CustomerService> currentServices = new ArrayList<>();

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");

    public BillingPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(AppTheme.BACKGROUND);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initComponents();
        loadActiveBookings();
    }

    private void initComponents() {
        // =====================================================================
        // TOP: Header & Active Stay Selector Bar
        // =====================================================================
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.add(UIComponents.createSectionHeader("Guest Checkout, Invoicing & Settlement"), BorderLayout.WEST);

        btnRefresh = UIComponents.createSecondaryButton("⟳  Refresh Stays");
        btnRefresh.addActionListener(e -> loadActiveBookings());
        headerRow.add(btnRefresh, BorderLayout.EAST);

        // Active Booking Selection Card
        JPanel selectCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));
        selectCard.setBackground(AppTheme.SURFACE);
        selectCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                new EmptyBorder(6, 12, 6, 12)));

        JLabel lblSelect = new JLabel("Select Active Stay to Bill:");
        lblSelect.setFont(AppTheme.FONT_SUBHEADING);
        lblSelect.setForeground(AppTheme.TEXT_PRIMARY);

        cbActiveBookings = UIComponents.createComboBox(new String[]{"Loading active stays..."});
        cbActiveBookings.setPreferredSize(new Dimension(380, 34));
        cbActiveBookings.addActionListener(e -> onBookingSelected());

        selectCard.add(lblSelect);
        selectCard.add(cbActiveBookings);

        topContainer.add(headerRow);
        topContainer.add(Box.createVerticalStrut(10));
        topContainer.add(selectCard);

        add(topContainer, BorderLayout.NORTH);

        // =====================================================================
        // CENTER & EAST: Split View (Left = Itemized Bill, Right = Settlement)
        // =====================================================================
        JPanel splitPanel = new JPanel(new BorderLayout(16, 0));
        splitPanel.setOpaque(false);

        // ---------------------------------------------------------------------
        // LEFT (65% width): Guest Summary + Itemized Bill Table
        // ---------------------------------------------------------------------
        JPanel leftBillCard = new JPanel(new BorderLayout(0, 12));
        leftBillCard.setBackground(AppTheme.SURFACE);
        leftBillCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                new EmptyBorder(16, 18, 16, 18)));

        // Guest Stay Banner
        JPanel banner = new JPanel(new GridLayout(2, 3, 10, 6));
        banner.setOpaque(false);
        banner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                new EmptyBorder(10, 12, 10, 12)));

        lblGuestName = new JLabel("Guest: -");
        lblGuestName.setFont(AppTheme.FONT_SUBHEADING);

        lblGuestPhone = new JLabel("Phone: -");
        lblGuestPhone.setFont(AppTheme.FONT_BODY);

        lblRoomInfo = new JLabel("Room: -");
        lblRoomInfo.setFont(AppTheme.FONT_SUBHEADING);

        lblStayDates = new JLabel("Dates: -");
        lblStayDates.setFont(AppTheme.FONT_BODY);

        lblDuration = new JLabel("Nights: -");
        lblDuration.setFont(AppTheme.FONT_SUBHEADING);
        lblDuration.setForeground(AppTheme.PRIMARY_BLUE);

        banner.add(lblGuestName);
        banner.add(lblRoomInfo);
        banner.add(lblDuration);
        banner.add(lblGuestPhone);
        banner.add(lblStayDates);
        banner.add(new JLabel("Status: ACTIVE"));

        // Itemized Table
        String[] cols = {"Item Description", "Category", "Quantity / Nights", "Unit Rate (₹)", "Subtotal (₹)"};
        modelBill = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblBill = new JTable(modelBill);
        UIComponents.applyTableStyle(tblBill);

        JPanel tableWrapper = new JPanel(new BorderLayout(0, 8));
        tableWrapper.setOpaque(false);
        JLabel lblBillHeader = new JLabel("Itemized Charges Breakdown:");
        lblBillHeader.setFont(AppTheme.FONT_SUBHEADING);
        tableWrapper.add(lblBillHeader, BorderLayout.NORTH);
        tableWrapper.add(UIComponents.createScrollPane(tblBill), BorderLayout.CENTER);

        leftBillCard.add(banner, BorderLayout.NORTH);
        leftBillCard.add(tableWrapper, BorderLayout.CENTER);

        splitPanel.add(leftBillCard, BorderLayout.CENTER);

        // ---------------------------------------------------------------------
        // RIGHT (35% width, ~360px): Settlement & Checkout Panel
        // ---------------------------------------------------------------------
        JPanel rightPayCard = new JPanel();
        rightPayCard.setLayout(new BoxLayout(rightPayCard, BoxLayout.Y_AXIS));
        rightPayCard.setBackground(AppTheme.SURFACE);
        rightPayCard.setPreferredSize(new Dimension(360, 500));
        rightPayCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel lblPayTitle = new JLabel("Settlement & Payment");
        lblPayTitle.setFont(AppTheme.FONT_HEADING);
        lblPayTitle.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel lblPaySub = new JLabel("Review balance and complete checkout");
        lblPaySub.setFont(AppTheme.FONT_SMALL);
        lblPaySub.setForeground(AppTheme.TEXT_SECONDARY);

        rightPayCard.add(lblPayTitle);
        rightPayCard.add(Box.createVerticalStrut(2));
        rightPayCard.add(lblPaySub);
        rightPayCard.add(Box.createVerticalStrut(18));

        // Subtotals Breakdown Box
        JPanel subtotalBox = new JPanel(new GridLayout(2, 2, 8, 8));
        subtotalBox.setOpaque(false);
        subtotalBox.add(new JLabel("Room Accommodation:"));
        lblRoomSubtotal = new JLabel("₹0.00", SwingConstants.RIGHT);
        lblRoomSubtotal.setFont(AppTheme.FONT_SUBHEADING);
        subtotalBox.add(lblRoomSubtotal);

        subtotalBox.add(new JLabel("Dining & Services:"));
        lblServicesSubtotal = new JLabel("₹0.00", SwingConstants.RIGHT);
        lblServicesSubtotal.setFont(AppTheme.FONT_SUBHEADING);
        subtotalBox.add(lblServicesSubtotal);

        rightPayCard.add(subtotalBox);
        rightPayCard.add(Box.createVerticalStrut(14));

        // Large Total Display Box
        JPanel totalDisplay = new JPanel(new BorderLayout(4, 4));
        totalDisplay.setBackground(AppTheme.BACKGROUND);
        totalDisplay.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel lblTotalWord = new JLabel("TOTAL AMOUNT DUE");
        lblTotalWord.setFont(AppTheme.FONT_SMALL);
        lblTotalWord.setForeground(AppTheme.TEXT_SECONDARY);

        lblGrandTotal = new JLabel("₹0.00", SwingConstants.LEFT);
        lblGrandTotal.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblGrandTotal.setForeground(AppTheme.PRIMARY_NAVY);

        totalDisplay.add(lblTotalWord, BorderLayout.NORTH);
        totalDisplay.add(lblGrandTotal, BorderLayout.CENTER);

        rightPayCard.add(totalDisplay);
        rightPayCard.add(Box.createVerticalStrut(18));

        // Payment Method Selector
        JLabel lblMethod = new JLabel("Select Payment Method *");
        lblMethod.setFont(AppTheme.FONT_SUBHEADING);
        lblMethod.setAlignmentX(Component.LEFT_ALIGNMENT);

        cbPaymentMethod = UIComponents.createComboBox(new String[]{"Cash", "Card", "UPI"});
        cbPaymentMethod.setMaximumSize(new Dimension(Short.MAX_VALUE, 36));
        cbPaymentMethod.setAlignmentX(Component.LEFT_ALIGNMENT);

        rightPayCard.add(lblMethod);
        rightPayCard.add(Box.createVerticalStrut(6));
        rightPayCard.add(cbPaymentMethod);
        rightPayCard.add(Box.createVerticalStrut(20));

        // Checkout & Payment Button
        btnPayAndCheckout = UIComponents.createSuccessButton("✓  Process Payment & Checkout");
        btnPayAndCheckout.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnPayAndCheckout.setMaximumSize(new Dimension(Short.MAX_VALUE, 46));
        btnPayAndCheckout.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnPayAndCheckout.addActionListener(e -> processPaymentAndCheckout());

        // Print Invoice Button
        btnPrintBill = UIComponents.createSecondaryButton("🖨  Print Official Tax Invoice");
        btnPrintBill.setMaximumSize(new Dimension(Short.MAX_VALUE, 38));
        btnPrintBill.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnPrintBill.addActionListener(e -> printInvoice());

        JLabel lblNotice = new JLabel("<html><center>⚠ Processing payment permanently completes stay<br>and releases room back to AVAILABLE inventory.</center></html>", SwingConstants.CENTER);
        lblNotice.setFont(AppTheme.FONT_SMALL);
        lblNotice.setForeground(AppTheme.TEXT_SECONDARY);
        lblNotice.setAlignmentX(Component.CENTER_ALIGNMENT);

        rightPayCard.add(btnPayAndCheckout);
        rightPayCard.add(Box.createVerticalStrut(10));
        rightPayCard.add(btnPrintBill);
        rightPayCard.add(Box.createVerticalStrut(14));
        rightPayCard.add(lblNotice);
        rightPayCard.add(Box.createVerticalGlue());

        splitPanel.add(rightPayCard, BorderLayout.EAST);

        add(splitPanel, BorderLayout.CENTER);
    }

    public void loadActiveBookings() {
        btnRefresh.setEnabled(false);

        SwingWorker<List<Booking>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Booking> doInBackground() {
                return bookingDAO.getActiveBookings();
            }

            @Override
            protected void done() {
                btnRefresh.setEnabled(true);
                try {
                    activeBookings = get();
                    cbActiveBookings.removeAllItems();

                    if (activeBookings.isEmpty()) {
                        cbActiveBookings.addItem("No active guest stays to checkout");
                        clearBillDisplay();
                    } else {
                        for (Booking b : activeBookings) {
                            String cust = b.getCustomer() != null ? b.getCustomer().getName() : ("Customer #" + b.getCustomerId());
                            String room = b.getRoom() != null ? b.getRoom().getRoomNumber() : ("Room #" + b.getRoomId());
                            cbActiveBookings.addItem("Stay #" + b.getBookingId() + " • " + cust + " (Room " + room + ")");
                        }
                        onBookingSelected();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    public void loadData() {
        loadActiveBookings();
    }

    private void onBookingSelected() {
        int idx = cbActiveBookings.getSelectedIndex();
        if (idx < 0 || idx >= activeBookings.size()) {
            clearBillDisplay();
            return;
        }

        currentBooking = activeBookings.get(idx);
        Customer cust = currentBooking.getCustomer();
        Room room = currentBooking.getRoom();

        String custName = cust != null ? cust.getName() : ("Guest #" + currentBooking.getCustomerId());
        String custPhone = cust != null ? cust.getPhone() : "-";
        String roomNum = room != null ? ("Room " + room.getRoomNumber() + " (" + room.getRoomType() + ")") : ("Room #" + currentBooking.getRoomId());
        double roomRate = room != null ? room.getPricePerDay() : 0.0;

        lblGuestName.setText("Guest: " + custName);
        lblGuestPhone.setText("Phone: " + custPhone);
        lblRoomInfo.setText("Room: " + roomNum);
        lblStayDates.setText("Dates: " + currentBooking.getCheckIn() + " to " + currentBooking.getCheckOut());

        // Calculate Nights
        long diffMs = currentBooking.getCheckOut().getTime() - currentBooking.getCheckIn().getTime();
        currentNights = Math.max(1, diffMs / (1000 * 60 * 60 * 24));
        lblDuration.setText("Duration: " + currentNights + (currentNights == 1 ? " Night" : " Nights"));

        // Populate Table with Room Charge
        modelBill.setRowCount(0);
        currentRoomCharge = currentNights * roomRate;
        modelBill.addRow(new Object[]{
                "Room Accommodation (" + (room != null ? room.getRoomType() : "") + ")",
                "Lodging",
                currentNights + " Nights",
                currencyFormat.format(roomRate),
                currencyFormat.format(currentRoomCharge)
        });

        // Query Additional Services for this Booking
        currentServices = serviceDAO.getServicesForBooking(currentBooking.getBookingId());
        currentServicesTotal = 0.0;
        for (CustomerService cs : currentServices) {
            double lineTotal = cs.getPrice() * cs.getQuantity();
            currentServicesTotal += lineTotal;
            modelBill.addRow(new Object[]{
                    cs.getServiceName(),
                    "Service / Amenity",
                    cs.getQuantity() + " Units",
                    currencyFormat.format(cs.getPrice()),
                    currencyFormat.format(lineTotal)
            });
        }

        currentTotal = currentRoomCharge + currentServicesTotal;

        lblRoomSubtotal.setText(currencyFormat.format(currentRoomCharge));
        lblServicesSubtotal.setText(currencyFormat.format(currentServicesTotal));
        lblGrandTotal.setText(currencyFormat.format(currentTotal));

        btnPayAndCheckout.setEnabled(true);
        btnPrintBill.setEnabled(true);
    }

    private void clearBillDisplay() {
        currentBooking = null;
        currentTotal = 0.0;
        lblGuestName.setText("Guest: -");
        lblGuestPhone.setText("Phone: -");
        lblRoomInfo.setText("Room: -");
        lblStayDates.setText("Dates: -");
        lblDuration.setText("Nights: -");
        lblRoomSubtotal.setText("₹0.00");
        lblServicesSubtotal.setText("₹0.00");
        lblGrandTotal.setText("₹0.00");
        modelBill.setRowCount(0);
        btnPayAndCheckout.setEnabled(false);
        btnPrintBill.setEnabled(false);
    }

    private void processPaymentAndCheckout() {
        if (currentBooking == null || currentTotal <= 0) {
            UIComponents.showWarning(this, "Please select an active guest stay to process checkout.");
            return;
        }

        String method = cbPaymentMethod.getSelectedItem() != null ? cbPaymentMethod.getSelectedItem().toString() : "Cash";
        String guestName = currentBooking.getCustomer() != null ? currentBooking.getCustomer().getName() : "Guest";

        String confirmMsg = "Confirm Checkout & Payment Settlement:\n\n"
                + "  • Guest: " + guestName + "\n"
                + "  • Booking: #" + currentBooking.getBookingId() + "\n"
                + "  • Room: " + (currentBooking.getRoom() != null ? currentBooking.getRoom().getRoomNumber() : currentBooking.getRoomId()) + "\n"
                + "  • Total Amount: " + currencyFormat.format(currentTotal) + "\n"
                + "  • Payment Method: " + method + "\n\n"
                + "Proceed with transactional checkout?";

        if (!UIComponents.confirm(this, confirmMsg, "Confirm Settlement")) {
            return;
        }

        btnPayAndCheckout.setEnabled(false);

        Payment payment = new Payment(0, currentBooking.getBookingId(), currentTotal,
                new Timestamp(System.currentTimeMillis()), method);

        if (paymentDAO.processPayment(payment, currentBooking.getRoomId())) {
            UIComponents.showSuccess(this, "✓ Payment of " + currencyFormat.format(currentTotal)
                    + " processed successfully!\nStay #" + currentBooking.getBookingId()
                    + " completed and Room is now AVAILABLE.");
            loadActiveBookings();
        } else {
            UIComponents.showError(this, "Payment processing failed. Transaction rolled back.");
            btnPayAndCheckout.setEnabled(true);
        }
    }

    private void printInvoice() {
        if (currentBooking == null) {
            UIComponents.showWarning(this, "No booking selected to print.");
            return;
        }

        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Hotel Invoice - Booking #" + currentBooking.getBookingId());

        job.setPrintable(new Printable() {
            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                if (pageIndex > 0) return NO_SUCH_PAGE;

                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                int y = 40;
                g2d.setColor(Color.DARK_GRAY);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 20));
                g2d.drawString("GRAND HOTEL RESORT & SUITES", 40, y);

                y += 18;
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2d.drawString("Official Tax Invoice & Guest Folio • Generated on " + dateFormat.format(new Date()), 40, y);

                y += 10;
                g2d.drawLine(40, y, 520, y);

                y += 20;
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                String guest = currentBooking.getCustomer() != null ? currentBooking.getCustomer().getName() : "-";
                String room = currentBooking.getRoom() != null ? currentBooking.getRoom().getRoomNumber() : "-";
                g2d.drawString("Guest Name: " + guest, 40, y);
                g2d.drawString("Booking Ref: #" + currentBooking.getBookingId(), 340, y);

                y += 16;
                g2d.drawString("Room Allocated: Room " + room, 40, y);
                g2d.drawString("Duration: " + currentNights + " Nights", 340, y);

                y += 16;
                g2d.drawString("Stay Period: " + currentBooking.getCheckIn() + " to " + currentBooking.getCheckOut(), 40, y);

                y += 25;
                g2d.drawLine(40, y, 520, y);
                y += 15;
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
                g2d.drawString("Description", 40, y);
                g2d.drawString("Qty", 260, y);
                g2d.drawString("Rate", 340, y);
                g2d.drawString("Subtotal", 450, y);
                y += 8;
                g2d.drawLine(40, y, 520, y);

                y += 18;
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2d.drawString("Room Lodging (" + (currentBooking.getRoom() != null ? currentBooking.getRoom().getRoomType() : "") + ")", 40, y);
                g2d.drawString(currentNights + " Nts", 260, y);
                double rRate = currentBooking.getRoom() != null ? currentBooking.getRoom().getPricePerDay() : 0.0;
                g2d.drawString(currencyFormat.format(rRate), 340, y);
                g2d.drawString(currencyFormat.format(currentRoomCharge), 450, y);

                for (CustomerService cs : currentServices) {
                    y += 16;
                    g2d.drawString(cs.getServiceName(), 40, y);
                    g2d.drawString(String.valueOf(cs.getQuantity()), 260, y);
                    g2d.drawString(currencyFormat.format(cs.getPrice()), 340, y);
                    g2d.drawString(currencyFormat.format(cs.getPrice() * cs.getQuantity()), 450, y);
                }

                y += 24;
                g2d.drawLine(40, y, 520, y);
                y += 18;
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 13));
                g2d.drawString("GRAND TOTAL SETTLED:", 260, y);
                g2d.drawString(currencyFormat.format(currentTotal), 450, y);

                y += 40;
                g2d.setFont(new Font("Segoe UI", Font.ITALIC, 10));
                g2d.drawString("Thank you for choosing Grand Hotel! We look forward to welcoming you back.", 40, y);

                return PAGE_EXISTS;
            }
        });

        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException ex) {
                UIComponents.showError(this, "Printing Error: " + ex.getMessage());
            }
        }
    }
}
