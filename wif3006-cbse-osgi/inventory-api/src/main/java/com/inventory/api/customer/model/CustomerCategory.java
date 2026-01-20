package com.inventory.api.customer.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CustomerCategory implements Serializable {
    private String id;
    private String categoryName;
    private String description;
    private String createdDate;

    public CustomerCategory() {
    }

    public CustomerCategory(String categoryName, String description) {
        this.categoryName = categoryName;
        this.description = description;
        this.createdDate = LocalDateTime.now().toString();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }
}