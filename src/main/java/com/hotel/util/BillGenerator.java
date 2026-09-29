package com.hotel.util;

import com.hotel.model.Booking;
import com.hotel.model.CustomerService;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class BillGenerator {

    public static String generateBill(Booking booking, List<CustomerService> services) {
        StringBuilder bill = new StringBuilder();
        
        long diffInMillies = Math.abs(booking.getCheckOut().getTime() - booking.getCheckIn().getTime());
        long days = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        if (days == 0) days = 1; // Minimum 1 day charge
        
        double roomCharges = days * booking.getRoom().getPricePerDay();
        double serviceCharges = 0;
        
        bill.append("==================================================\n");
        bill.append("              GRAND HOTEL AND RESORT            \n");
        bill.append("==================================================\n\n");
        
        bill.append("Booking ID: ").append(booking.getBookingId()).append("\n");
        bill.append("Customer: ").append(booking.getCustomer().getName()).append("\n");
        bill.append("Room: ").append(booking.getRoom().getRoomNumber())
            .append(" (").append(booking.getRoom().getRoomType()).append(")\n");
        bill.append("Check-in: ").append(booking.getCheckIn()).append("\n");
        bill.append("Check-out: ").append(booking.getCheckOut()).append("\n");
        bill.append("Duration: ").append(days).append(" days\n\n");
        
        bill.append("--------------------------------------------------\n");
        bill.append("ROOM CHARGES\n");
        bill.append("--------------------------------------------------\n");
        bill.append(String.format("₹%.2f x %d days = ₹%.2f\n\n", booking.getRoom().getPricePerDay(), days, roomCharges));
        
        if (!services.isEmpty()) {
            bill.append("--------------------------------------------------\n");
            bill.append("SERVICE CHARGES\n");
            bill.append("--------------------------------------------------\n");
            bill.append(String.format("%-20s %-10s %-10s\n", "Service", "Qty", "Subtotal"));
            
            for (CustomerService cs : services) {
                double sub = cs.getPrice() * cs.getQuantity();
                serviceCharges += sub;
                bill.append(String.format("%-20s %-10d ₹%-10.2f\n", cs.getServiceName(), cs.getQuantity(), sub));
            }
            bill.append("\n");
        }
        
        double total = roomCharges + serviceCharges;
        
        bill.append("==================================================\n");
        bill.append(String.format("TOTAL AMOUNT: ₹%.2f\n", total));
        bill.append("==================================================\n");
        bill.append("             Thank you for staying with us!       \n");
        bill.append("==================================================\n");
        
        return bill.toString();
    }
    
    public static double calculateTotal(Booking booking, List<CustomerService> services) {
        long diffInMillies = Math.abs(booking.getCheckOut().getTime() - booking.getCheckIn().getTime());
        long days = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        if (days == 0) days = 1; 
        
        double roomCharges = days * booking.getRoom().getPricePerDay();
        double serviceCharges = 0;
        
        for (CustomerService cs : services) {
            serviceCharges += cs.getPrice() * cs.getQuantity();
        }
        
        return roomCharges + serviceCharges;
    }
}
