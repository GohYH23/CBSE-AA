package com.inventorymanagement.customer_gohyuheng.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Document(collection = "customer_categories")
public class CustomerCategory {

    @Id
    private String id;

    @NotBlank(message = "Category Name is required")
    private String categoryName; // e.g., "Micro", "Startup"

    private String description;

    @CreatedDate
    private LocalDateTime createdDate;

    // Constructors
    public CustomerCategory() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}