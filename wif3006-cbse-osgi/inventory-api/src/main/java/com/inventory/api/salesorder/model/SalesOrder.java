package com.inventory.api.salesorder.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SalesOrder implements Serializable {
    private String id;
    private String orderNumber;
    private LocalDate orderDate;
    private String customerId;
    private String taxId;
    private String orderStatus;
    private String description;
    private String createdAt;
    private String editedAt;

    public SalesOrder() {
        this.createdAt = LocalDateTime.now().toString();
    }

    public SalesOrder(LocalDate orderDate, String customerId, String taxId, String orderStatus, String description) {
        this.orderDate = orderDate;
        this.customerId = customerId;
        this.taxId = taxId;
        this.orderStatus = orderStatus;
        this.description = description;
        this.createdAt = LocalDateTime.now().toString();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getEditedAt() { return editedAt; }
    public void setEditedAt(String editedAt) { this.editedAt = editedAt; }

    @Override
    public String toString() {
        return "SalesOrder{orderNumber='" + orderNumber + "', status='" + orderStatus + "'}";
    }
}