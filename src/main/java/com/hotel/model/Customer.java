package com.hotel.model;

public class Customer extends Person {
    private int customerId;
    private String address;
    private String idProof;

    public Customer() { super(); }

    public Customer(int customerId, String name, String phone, String email, String address, String idProof) {
        super(name, phone, email);
        this.customerId = customerId;
        this.address = address;
        this.idProof = idProof;
    }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getIdProof() { return idProof; }
    public void setIdProof(String idProof) { this.idProof = idProof; }

    @Override
    public String toString() {
        return getName() + " (" + getPhone() + ")";
    }
}
