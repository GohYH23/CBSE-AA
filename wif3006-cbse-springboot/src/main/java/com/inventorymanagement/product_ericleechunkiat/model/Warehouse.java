package com.inventorymanagement.product_ericleechunkiat.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "warehouses")
public class Warehouse {
    @Id
    private String name;
    private boolean isSystemWarehouse;
    private String description;

    public Warehouse() {}

    public Warehouse(String name, boolean isSystemWarehouse, String description) {
        this.name = name;
        this.isSystemWarehouse = isSystemWarehouse;
        this.description = description;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isSystemWarehouse() { return isSystemWarehouse; }
    public void setSystemWarehouse(boolean systemWarehouse) { isSystemWarehouse = systemWarehouse; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}