package com.inventorymanagement.salesorder_wongxiuhuan.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "sales_orders")
public class SalesOrder {
    @Id
    private String id;
    
    @NotNull(message = "Order date is required")
    private LocalDate orderDate;
    
    private String orderNumber;
    
    @NotBlank(message = "Customer is required")
    private String customerId;
    
    private String taxId;
    
    @NotBlank(message = "Order status is required")
    private String orderStatus;
    
    private String description;
    
    @CreatedDate
    private LocalDateTime createdDate;
    
    @LastModifiedDate
    private LocalDateTime updatedDate;

    // Constructors
    public SalesOrder() {}

    public SalesOrder(String id, LocalDate orderDate, String orderNumber, String customerId, 
                      String taxId, String orderStatus, String description) {
        this.id = id;
        this.orderDate = orderDate;
        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.taxId = taxId;
        this.orderStatus = orderStatus;
        this.description = description;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    @Override
    public String toString() {
        return "SalesOrder{id='" + id + "', orderNumber='" + orderNumber + "', created='" + createdDate + "'}";
    }
}