package com.inventorymanagement.customer_gohyuheng.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Document(collection = "customers")
public class Customer {

    @Id
    private String id;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    private String address;

    // These store the IDs of the Group and Category objects
    @NotBlank(message = "Customer Group is required")
    private String customerGroupId;

    @NotBlank(message = "Customer Category is required")
    private String customerCategoryId;

    @CreatedDate
    private LocalDateTime createdDate;

    // Constructors
    public Customer() {}

    // Getters and Setters
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

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    @Override
    public String toString() {
        return "Customer{id='" + id + "', name='" + name + "', email='" + email + "', created='" + createdDate + "'}";
    }
}