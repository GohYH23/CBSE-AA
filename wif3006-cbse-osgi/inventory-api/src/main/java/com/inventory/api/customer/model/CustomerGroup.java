package com.inventory.api.customer.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CustomerGroup implements Serializable {
    private String id;
    private String groupName;
    private String description;
    private String createdAt;
    private String editedAt;

    public CustomerGroup() {
        this.createdAt = LocalDateTime.now().toString();
    }

    public CustomerGroup(String groupName, String description) {
        this.groupName = groupName;
        this.description = description;
        this.createdAt = LocalDateTime.now().toString();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdDate) { this.createdAt = createdDate; }

    public String getEditedAt() { return editedAt; }
    public void setEditedAt(String editedDate) { this.editedAt = editedDate; }
}