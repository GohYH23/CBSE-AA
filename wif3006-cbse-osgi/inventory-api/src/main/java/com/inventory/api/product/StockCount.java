package com.inventory.api.product;
import java.io.Serializable;

public class StockCount implements Serializable {
    private String countId;
    private String warehouseName;
    private String status;
    private String date;

    public StockCount() {}
    public StockCount(String countId, String warehouseName, String status, String date) {
        this.countId = countId;
        this.warehouseName = warehouseName;
        this.status = status;
        this.date = date;
    }
    // Getters/Setters
    public String getCountId() { return countId; }
    public void setCountId(String countId) { this.countId = countId; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}