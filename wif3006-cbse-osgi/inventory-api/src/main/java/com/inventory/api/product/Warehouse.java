package com.inventory.api.product;
import java.io.Serializable;

public class Warehouse implements Serializable {
    private String name;
    private boolean isSystemWarehouse;
    private String description;

    public Warehouse() {}
    public Warehouse(String name, boolean isSystemWarehouse, String description) {
        this.name = name;
        this.isSystemWarehouse = isSystemWarehouse;
        this.description = description;
    }
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isSystemWarehouse() { return isSystemWarehouse; }
    public void setSystemWarehouse(boolean systemWarehouse) { isSystemWarehouse = systemWarehouse; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    @Override public String toString() { return name; }
}