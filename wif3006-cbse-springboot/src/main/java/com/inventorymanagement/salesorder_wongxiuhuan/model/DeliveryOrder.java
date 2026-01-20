package com.inventorymanagement.salesorder_wongxiuhuan.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "delivery_orders")
public class DeliveryOrder {
    @Id
    private String id;
    private LocalDate deliveryDate;
    private String deliveryNumber;
    private String salesOrderId;
    private String status;
    private String description;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    // Constructors
    public DeliveryOrder() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    public DeliveryOrder(String id, LocalDate deliveryDate, String deliveryNumber, 
                        String salesOrderId, String status, String description) {
        this.id = id;
        this.deliveryDate = deliveryDate;
        this.deliveryNumber = deliveryNumber;
        this.salesOrderId = salesOrderId;
        this.status = status;
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

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
        this.updatedDate = LocalDateTime.now();
    }

    public String getDeliveryNumber() {
        return deliveryNumber;
    }

    public void setDeliveryNumber(String deliveryNumber) {
        this.deliveryNumber = deliveryNumber;
    }

    public String getSalesOrderId() {
        return salesOrderId;
    }

    public void setSalesOrderId(String salesOrderId) {
        this.salesOrderId = salesOrderId;
        this.updatedDate = LocalDateTime.now();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
        return "DeliveryOrder{" +
                "id='" + id + '\'' +
                ", deliveryDate=" + deliveryDate +
                ", deliveryNumber='" + deliveryNumber + '\'' +
                ", salesOrderId='" + salesOrderId + '\'' +
                ", status='" + status + '\'' +
                ", description='" + description + '\'' +
                ", createdDate=" + createdDate +
                ", updatedDate=" + updatedDate +
                '}';
    }
}