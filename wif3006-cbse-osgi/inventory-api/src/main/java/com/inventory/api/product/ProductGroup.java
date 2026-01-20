package com.inventory.api.product;
import java.io.Serializable;

public class ProductGroup implements Serializable {
    private String groupId;
    private String groupName;
    private String description;

    public ProductGroup() {}
    public ProductGroup(String groupId, String groupName, String description) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.description = description;
    }
    // Getters and Setters
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    @Override public String toString() { return groupName; }
}