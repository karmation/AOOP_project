package com.hotel;

import com.formdev.flatlaf.FlatLightLaf;
import com.hotel.ui.LoginFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Apply modern Look and Feel (FlatLaf)
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }

        // Launch Application
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
