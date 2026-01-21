package com.inventory.api.salesorder.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SalesOrderItem implements Serializable {
    private String id;
    private String salesOrderId;
    private String productId;
    private BigDecimal unitPrice;
    private int quantity;
    private String productNumber;
    private String createdAt;
    private String editedAt;

    public SalesOrderItem() {
        this.createdAt = LocalDateTime.now().toString();
    }

    public SalesOrderItem(String salesOrderId, String productId, BigDecimal unitPrice, int quantity, String productNumber) {
        this.salesOrderId = salesOrderId;
        this.productId = productId;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.productNumber = productNumber;
        this.createdAt = LocalDateTime.now().toString();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSalesOrderId() { return salesOrderId; }
    public void setSalesOrderId(String salesOrderId) { this.salesOrderId = salesOrderId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getProductNumber() { return productNumber; }
    public void setProductNumber(String productNumber) { this.productNumber = productNumber; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getEditedAt() { return editedAt; }
    public void setEditedAt(String editedAt) { this.editedAt = editedAt; }

    @Override
    public String toString() {
        return "SalesOrderItem{productId='" + productId + "', quantity=" + quantity + ", unitPrice=" + unitPrice + "}";
    }
}