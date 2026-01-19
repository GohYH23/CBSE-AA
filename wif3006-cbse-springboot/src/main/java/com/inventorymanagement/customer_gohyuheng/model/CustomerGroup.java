package com.inventorymanagement.customer_gohyuheng.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Document(collection = "customer_groups")
public class CustomerGroup {

    @Id
    private String id;

    @NotBlank(message = "Group Name is required")
    private String groupName; // e.g., "Education", "Government"

    private String description;

    @CreatedDate
    private LocalDateTime createdDate;

    // Constructors
    public CustomerGroup() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}