package com.hotel.ui.theme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

public class UIComponents {

    // -------------------------------------------------------------------------
    // Buttons
    // -------------------------------------------------------------------------

    public static JButton createPrimaryButton(String text) {
        JButton btn = buildButton(text, AppTheme.PRIMARY_BLUE, Color.WHITE);
        return btn;
    }

    public static JButton createDangerButton(String text) {
        return buildButton(text, AppTheme.DANGER, Color.WHITE);
    }

    public static JButton createSuccessButton(String text) {
        return buildButton(text, AppTheme.SUCCESS, Color.WHITE);
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = buildButton(text, AppTheme.SURFACE, AppTheme.PRIMARY_NAVY);
        btn.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1));
        return btn;
    }

    private static JButton buildButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(AppTheme.FONT_SUBHEADING);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        return btn;
    }

    // -------------------------------------------------------------------------
    // Label
    // -------------------------------------------------------------------------

    public static JLabel createLabel(String text, Font font, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(font);
        lbl.setForeground(color);
        return lbl;
    }

    // -------------------------------------------------------------------------
    // Stat Card
    // -------------------------------------------------------------------------

    public static JPanel createStatCard(String title, String value, String subtitle, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(8, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                // Top colored accent bar
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, getWidth(), 4, 4, 4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                new EmptyBorder(14, 16, 14, 16)));

        JPanel textPanel = new JPanel(new GridLayout(3, 1, 0, 4));
        textPanel.setOpaque(false);

        JLabel lblTitle = createLabel(title, AppTheme.FONT_SUBHEADING, AppTheme.TEXT_SECONDARY);
        JLabel lblValue = createLabel(value, new Font("Segoe UI", Font.BOLD, 22), AppTheme.TEXT_PRIMARY);
        JLabel lblSub = createLabel(subtitle, AppTheme.FONT_SMALL, AppTheme.TEXT_SECONDARY);

        textPanel.add(lblTitle);
        textPanel.add(lblValue);
        textPanel.add(lblSub);

        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    // -------------------------------------------------------------------------
    // Dialog Helpers
    // -------------------------------------------------------------------------

    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showWarning(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    public static boolean confirm(Component parent, String message, String title) {
        return JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    // -------------------------------------------------------------------------
    // Text field
    // -------------------------------------------------------------------------

    public static JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(AppTheme.FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                new EmptyBorder(8, 8, 8, 8)));
        tf.setPreferredSize(new Dimension(tf.getPreferredSize().width, 32));
        return tf;
    }

    // -------------------------------------------------------------------------
    // Combo box
    // -------------------------------------------------------------------------

    public static <T> JComboBox<T> createComboBox(T[] items) {
        JComboBox<T> combo = new JComboBox<>(items);
        combo.setFont(AppTheme.FONT_BODY);
        combo.setBackground(AppTheme.SURFACE);
        combo.setForeground(AppTheme.TEXT_PRIMARY);
        combo.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1));
        combo.setPreferredSize(new Dimension(combo.getPreferredSize().width, 32));
        return combo;
    }

    // -------------------------------------------------------------------------
    // Scroll pane
    // -------------------------------------------------------------------------

    public static JScrollPane createScrollPane(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor = AppTheme.BORDER;
                this.trackColor = AppTheme.BACKGROUND;
            }
            @Override protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
            @Override protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
            private JButton createZeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                b.setMinimumSize(new Dimension(0, 0));
                b.setMaximumSize(new Dimension(0, 0));
                return b;
            }
        });
        sp.getHorizontalScrollBar().setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor = AppTheme.BORDER;
                this.trackColor = AppTheme.BACKGROUND;
            }
            @Override protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
            @Override protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
            private JButton createZeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                b.setMinimumSize(new Dimension(0, 0));
                b.setMaximumSize(new Dimension(0, 0));
                return b;
            }
        });
        return sp;
    }

    // -------------------------------------------------------------------------
    // Section header
    // -------------------------------------------------------------------------

    public static JPanel createSectionHeader(String title) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setOpaque(false);

        // Left accent bar
        JPanel bar = new JPanel();
        bar.setBackground(AppTheme.PRIMARY_BLUE);
        bar.setPreferredSize(new Dimension(4, 20));
        panel.add(bar);

        // Gap
        panel.add(Box.createHorizontalStrut(10));

        // Title label
        JLabel lbl = createLabel(title, AppTheme.FONT_HEADING, AppTheme.TEXT_PRIMARY);
        panel.add(lbl);

        return panel;
    }

    // -------------------------------------------------------------------------
    // Card
    // -------------------------------------------------------------------------

    public static JPanel createCard(Color bg, int cornerRadius) {
        JPanel card = new JPanel();
        card.setBackground(bg);
        card.setBorder(new EmptyBorder(12, 12, 12, 12));
        card.setOpaque(true);
        return card;
    }

    // -------------------------------------------------------------------------
    // Search field with placeholder
    // -------------------------------------------------------------------------

    public static JTextField createSearchField(String placeholder) {
        JTextField tf = new JTextField(placeholder) {
            {
                setForeground(AppTheme.TEXT_SECONDARY);
                addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        if (getText().equals(placeholder)) {
                            setText("");
                            setForeground(AppTheme.TEXT_PRIMARY);
                        }
                    }
                    @Override
                    public void focusLost(FocusEvent e) {
                        if (getText().isEmpty()) {
                            setText(placeholder);
                            setForeground(AppTheme.TEXT_SECONDARY);
                        }
                    }
                });
            }
        };
        tf.setFont(AppTheme.FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                new EmptyBorder(6, 10, 6, 10)));
        tf.setPreferredSize(new Dimension(200, 32));
        return tf;
    }

    // -------------------------------------------------------------------------
    // Table style
    // -------------------------------------------------------------------------

    public static void applyTableStyle(JTable table) {
        // Header
        JTableHeader header = table.getTableHeader();
        header.setBackground(AppTheme.PRIMARY_NAVY);
        header.setForeground(Color.WHITE);
        header.setFont(AppTheme.FONT_SUBHEADING);
        header.setReorderingAllowed(false);
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);

        // Table body
        table.setFont(AppTheme.FONT_BODY);
        table.setForeground(AppTheme.TEXT_PRIMARY);
        table.setRowHeight(36);
        table.setGridColor(AppTheme.BORDER);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(AppTheme.HOVER);
        table.setSelectionForeground(AppTheme.TEXT_PRIMARY);
        table.setFocusable(false);

        // Alternating row renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                if (isSelected) {
                    c.setBackground(AppTheme.HOVER);
                    c.setForeground(AppTheme.TEXT_PRIMARY);
                } else {
                    c.setBackground(row % 2 == 0 ? AppTheme.SURFACE : AppTheme.BACKGROUND);
                    c.setForeground(AppTheme.TEXT_PRIMARY);
                }
                ((JLabel) c).setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        });
    }

    // -------------------------------------------------------------------------
    // Status badge
    // -------------------------------------------------------------------------

    public static JLabel createStatusBadge(String status) {
        JLabel lbl = new JLabel(status, JLabel.CENTER);
        lbl.setFont(AppTheme.FONT_SMALL);
        lbl.setOpaque(true);
        lbl.setBorder(new EmptyBorder(2, 10, 2, 10));

        Color bg;
        Color fg = Color.WHITE;
        String upper = status == null ? "" : status.toUpperCase();
        switch (upper) {
            case "AVAILABLE":
            case "ACTIVE":
                bg = AppTheme.SUCCESS;
                break;
            case "OCCUPIED":
            case "COMPLETED":
                bg = AppTheme.DANGER;
                break;
            case "MAINTENANCE":
            case "CANCELLED":
                bg = AppTheme.WARNING;
                fg = AppTheme.TEXT_PRIMARY;
                break;
            default:
                bg = AppTheme.TEXT_SECONDARY;
                break;
        }
        lbl.setBackground(bg);
        lbl.setForeground(fg);
        return lbl;
    }
}
