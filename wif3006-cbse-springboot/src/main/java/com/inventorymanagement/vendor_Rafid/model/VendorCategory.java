package com.inventorymanagement.vendor_Rafid.model;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "vendor_categories")
public class VendorCategory {
    @Id
    private String id;
    
    private String name;
    private String description;
    private String code;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public VendorCategory() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public VendorCategory(String name, String code) {
        this();
        this.name = name;
        this.code = code;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}