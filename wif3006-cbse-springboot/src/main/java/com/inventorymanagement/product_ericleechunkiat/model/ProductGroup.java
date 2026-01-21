package com.inventorymanagement.product_ericleechunkiat.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "product_groups")
public class ProductGroup {
    @Id
    private String groupId;
    private String groupName;
    private String description;

    public ProductGroup() {}

    public ProductGroup(String groupId, String groupName, String description) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.description = description;
    }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}