package com.inventorymanagement.customer;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "customers") // Changed from @Entity
public class Customer {

    @Id // Import from org.springframework.data.annotation.Id
    private String id; // Changed from Long to String

    private String name;
    private String email;
    private String category;

    public Customer() {}

    public Customer(String name, String email, String category) {
        this.name = name;
        this.email = email;
        this.category = category;
    }

    // Update Getter for ID
    public String getId() { return id; }

    // Other getters and setters remain the same...
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getCategory() { return category; }

    @Override
    public String toString() {
        return "Customer{id='" + id + "', name='" + name + "', email='" + email + "'}";
    }
}