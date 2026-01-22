package com.inventorymanagement.product_ericleechunkiat.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "product_groups")
public class ProductGroup {

    @Id
    private String id;
    private String groupName;
    private String description;

    // --- CONSTRUCTORS ---
    public ProductGroup() {}

    public ProductGroup(String id, String groupName, String description) {
        this.id = id;
        this.groupName = groupName;
        this.description = description;
    }

    // --- GETTERS AND SETTERS (The Missing Part!) ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}