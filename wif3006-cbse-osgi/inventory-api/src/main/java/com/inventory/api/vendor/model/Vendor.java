package com.inventory.api.vendor.model;

import java.io.Serializable;

public class Vendor implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String vendorId;
    private String name;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private double rating;
    private boolean active;
    private String category;
    private String paymentTerms;
    
    // Constructors
    public Vendor() {
        this.active = true;
        this.rating = 0.0;
    }
    
    public Vendor(String vendorId, String name, String contactPerson, 
                  String email, String phone, String address) {
        this();
        this.vendorId = vendorId;
        this.name = name;
        this.contactPerson = contactPerson;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }
    
    // Getters and Setters
    public String getVendorId() { return vendorId; }
    public void setVendorId(String vendorId) { this.vendorId = vendorId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }
    
    @Override
    public String toString() {
        return String.format(
            "Vendor [ID=%s, Name=%s, Contact=%s, Email=%s, Phone=%s, Rating=%.1f, Active=%s]",
            vendorId, name, contactPerson, email, phone, rating, active
        );
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vendor vendor = (Vendor) obj;
        return vendorId != null && vendorId.equals(vendor.vendorId);
    }
    
    @Override
    public int hashCode() {
        return vendorId != null ? vendorId.hashCode() : 0;
    }
}