package com.inventory.api.customer.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Customer implements Serializable {
    private String id;
    private String name;
    private String email;
    private String phoneNumber;
    private String address;
    private String customerGroupId;
    private String customerCategoryId;
    private String createdAt;
    private String editedAt;

    public Customer() {
        this.createdAt = LocalDateTime.now().toString();
    }

    public Customer(String name, String email, String phoneNumber, String address, String customerGroupId, String customerCategoryId) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.customerGroupId = customerGroupId;
        this.customerCategoryId = customerCategoryId;
        this.createdAt = LocalDateTime.now().toString();
    }

    // --- Getters and Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCustomerGroupId() { return customerGroupId; }
    public void setCustomerGroupId(String customerGroupId) { this.customerGroupId = customerGroupId; }

    public String getCustomerCategoryId() { return customerCategoryId; }
    public void setCustomerCategoryId(String customerCategoryId) { this.customerCategoryId = customerCategoryId; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdDate) { this.createdAt = createdDate; }

    public String getEditedAt() { return editedAt; }
    public void setEditedAt(String editedDate) { this.editedAt = editedDate; }

    @Override
    public String toString() {
        return "Customer{name='" + name + "', email='" + email + "'}";
    }
}