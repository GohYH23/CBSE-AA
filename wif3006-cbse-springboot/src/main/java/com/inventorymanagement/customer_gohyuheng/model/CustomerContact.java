package com.inventorymanagement.customer_gohyuheng.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Document(collection = "customer_contacts")
public class CustomerContact {

    @Id
    private String id;

    @NotBlank(message = "Contact Name is required")
    private String contactName;

    private String position; // e.g., "Manager", "Assistant"
    private String phone;
    private String email;

    @NotBlank(message = "Customer ID is required")
    private String customerId; // Links this contact to a specific Customer

    @CreatedDate
    private LocalDateTime createdDate;

    // Constructors
    public CustomerContact() {}

    // Getters and Setters
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
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}