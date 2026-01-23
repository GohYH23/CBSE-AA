package com.inventory.api.vendor.model;

public class VendorContact {
    private String contactId;
    private String vendorId;
    private String name;
    private String position;
    private String email;
    private String phone;
    private boolean primary;
    
    // Constructors, Getters, Setters
    public VendorContact() {}
    
    public VendorContact(String contactId, String vendorId, String name, 
                        String position, String email, String phone) {
        this.contactId = contactId;
        this.vendorId = vendorId;
        this.name = name;
        this.position = position;
        this.email = email;
        this.phone = phone;
        this.primary = false;
    }
    
    // Getters and Setters
    public String getContactId() { return contactId; }
    public void setContactId(String contactId) { this.contactId = contactId; }
    
    public String getVendorId() { return vendorId; }
    public void setVendorId(String vendorId) { this.vendorId = vendorId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public boolean isPrimary() { return primary; }
    public void setPrimary(boolean primary) { this.primary = primary; }
    
    @Override
    public String toString() {
        return String.format("VendorContact [ID=%s, Name=%s, Position=%s, Email=%s, Phone=%s, Primary=%s]",
            contactId, name, position, email, phone, primary);
    }
}