package com.hotel.model;

public class CustomerService {
    private int id;
    private int bookingId;
    private int serviceId;
    private int quantity;
    
    // For UI convenience
    private String serviceName;
    private double price;

    public CustomerService() {}

    public CustomerService(int id, int bookingId, int serviceId, int quantity) {
        this.id = id;
        this.bookingId = bookingId;
        this.serviceId = serviceId;
        this.quantity = quantity;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
