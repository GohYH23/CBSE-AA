package com.inventorymanagement.salesorder_wongxiuhuan.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "sales_order_items")
public class SalesOrderItem {
    @Id
    private String id;
    
    @NotBlank(message = "Sales Order ID is required")
    private String salesOrderId;
    
    @NotBlank(message = "Product is required")
    private String productId;
    
    @NotNull(message = "Unit price is required")
    @Positive(message = "Unit price must be positive")
    private BigDecimal unitPrice;
    
    @Positive(message = "Quantity must be positive")
    private int quantity;
    
    private String productNumber;
    
    @CreatedDate
    private LocalDateTime createdDate;
    
    @LastModifiedDate
    private LocalDateTime updatedDate;

    // Constructors
    public SalesOrderItem() {}

    public SalesOrderItem(String id, String salesOrderId, String productId, 
                          BigDecimal unitPrice, int quantity, String productNumber) {
        this.id = id;
        this.salesOrderId = salesOrderId;
        this.productId = productId;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.productNumber = productNumber;
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

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    // Calculated method for total
    public BigDecimal getTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public String toString() {
        return "SalesOrderItem{id='" + id + "', product='" + productId + "', qty=" + quantity + "}";
    }
}