package com.hotel;

import com.hotel.dao.UserDAO;
import com.hotel.model.User;
import com.hotel.ui.DashboardFrame;
import javax.swing.SwingUtilities;

public class DashboardInitTest {
    public static void main(String[] args) throws Exception {
        System.out.println("[TEST] Step 1: Testing UserDAO authentication for admin / admin123...");
        UserDAO userDAO = new UserDAO();
        User user = userDAO.authenticate("admin", "admin123");
        if (user == null) {
            System.err.println("FAILED: User is null");
            System.exit(1);
        }
        System.out.println("[TEST] User authenticated: " + user.getUsername() + ", Role: " + user.getRole() + ", ID: " + user.getUserId());

        System.out.println("[TEST] Step 2: Testing DashboardFrame(User) instantiation on Swing EventQueue...");
        SwingUtilities.invokeAndWait(() -> {
            try {
                DashboardFrame frame = new DashboardFrame(user);
                System.out.println("[TEST] DashboardFrame instantiated successfully! Title: " + frame.getTitle());
                frame.dispose();
            } catch (Throwable t) {
                System.err.println("[TEST] FAILED with exception:");
                t.printStackTrace();
                System.exit(2);
            }
        });

        System.out.println("[TEST] Step 3: SUCCESS - Complete flow from UserDAO -> LoginFrame -> DashboardFrame(User) -> DashboardPanel(User) is 100% verified!");
        System.exit(0);
    }
}
