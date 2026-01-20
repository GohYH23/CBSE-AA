package com.inventory.api.customer.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CustomerContact implements Serializable {
    private String id;
    private String contactName;
    private String position;
    private String phone;
    private String email;
    private String customerId;
    private String createdDate;

    public CustomerContact() {
    }

    public CustomerContact(String contactName, String position, String phone, String email, String customerId) {
        this.contactName = contactName;
        this.position = position;
        this.phone = phone;
        this.email = email;
        this.customerId = customerId;
        this.createdDate = LocalDateTime.now().toString();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }
}