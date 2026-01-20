package com.inventorymanagement.salesorder_wongxiuhuan.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "sales_orders")
public class SalesOrder {
    @Id
    private String id;
    private LocalDate orderDate;
    private String orderNumber;
    private String customerId;
    private String taxId;
    private String orderStatus;
    private String description;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    // Constructors
    public SalesOrder() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    public SalesOrder(String id, LocalDate orderDate, String orderNumber, String customerId, 
                      String taxId, String orderStatus, String description) {
        this.id = id;
        this.orderDate = orderDate;
        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.taxId = taxId;
        this.orderStatus = orderStatus;
        this.description = description;
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
        this.updatedDate = LocalDateTime.now();
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
        this.updatedDate = LocalDateTime.now();
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
        this.updatedDate = LocalDateTime.now();
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
        this.updatedDate = LocalDateTime.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedDate = LocalDateTime.now();
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    @Override
    public String toString() {
        return "SalesOrder{" +
                "id='" + id + '\'' +
                ", orderDate=" + orderDate +
                ", orderNumber='" + orderNumber + '\'' +
                ", customerId='" + customerId + '\'' +
                ", taxId='" + taxId + '\'' +
                ", orderStatus='" + orderStatus + '\'' +
                ", description='" + description + '\'' +
                ", createdDate=" + createdDate +
                ", updatedDate=" + updatedDate +
                '}';
    }
}