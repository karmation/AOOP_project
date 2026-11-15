package com.hotel.ui.theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Navigation sidebar button with selected / hover / normal states.
 */
public class NavButton extends JButton {

    private static final int BTN_WIDTH  = 220;
    private static final int BTN_HEIGHT = 48;

    private boolean selected = false;
    private boolean hovered  = false;

    public NavButton(String text) {
        super(text);
        setFont(AppTheme.FONT_BODY);
        setHorizontalAlignment(SwingConstants.LEFT);
        setBorderPainted(false);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 12));

        setPreferredSize(new Dimension(BTN_WIDTH, BTN_HEIGHT));
        setMinimumSize(new Dimension(BTN_WIDTH, BTN_HEIGHT));
        setMaximumSize(new Dimension(BTN_WIDTH, BTN_HEIGHT));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;
        repaint();
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        if (selected) {
            g2.setColor(AppTheme.PRIMARY_BLUE);
            g2.fillRoundRect(8, 4, w - 16, h - 8, 8, 8);
            setForeground(Color.WHITE);
        } else if (hovered) {
            g2.setColor(AppTheme.HOVER);
            g2.fillRoundRect(8, 4, w - 16, h - 8, 8, 8);
            setForeground(AppTheme.PRIMARY_NAVY);
        } else {
            setForeground(AppTheme.TEXT_SECONDARY);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
