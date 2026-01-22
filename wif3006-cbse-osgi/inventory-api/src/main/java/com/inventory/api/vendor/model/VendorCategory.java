package com.inventory.api.vendor.model;

public enum VendorCategory {
    RAW_MATERIAL_SUPPLIER("Raw Material Supplier"),
    MANUFACTURER("Manufacturer"),
    DISTRIBUTOR("Distributor"),
    WHOLESALER("Wholesaler"),
    RETAILER("Retailer"),
    SERVICE_PROVIDER("Service Provider"),
    LOGISTICS("Logistics"),
    IT_SERVICES("IT Services");
    
    private final String displayName;
    
    VendorCategory(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}