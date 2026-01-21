package com.inventory.api.salesorder.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DeliveryOrder implements Serializable {
    private String id;
    private String deliveryNumber;
    private LocalDate deliveryDate;
    private String salesOrderId;
    private String status; // PENDING, IN_TRANSIT, DELIVERED, CANCELLED
    private String description;
    private String createdAt;
    private String editedAt;

    public DeliveryOrder() {
        this.createdAt = LocalDateTime.now().toString();
        this.status = "PENDING";
    }

    public DeliveryOrder(LocalDate deliveryDate, String salesOrderId, String status, String description) {
        this.deliveryDate = deliveryDate;
        this.salesOrderId = salesOrderId;
        this.status = status != null ? status : "PENDING"; // Default to PENDING if null
        this.description = description;
        this.createdAt = LocalDateTime.now().toString();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDeliveryNumber() { return deliveryNumber; }
    public void setDeliveryNumber(String deliveryNumber) { this.deliveryNumber = deliveryNumber; }

    public LocalDate getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDate deliveryDate) { this.deliveryDate = deliveryDate; }

    public String getSalesOrderId() { return salesOrderId; }
    public void setSalesOrderId(String salesOrderId) { this.salesOrderId = salesOrderId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getEditedAt() { return editedAt; }
    public void setEditedAt(String editedAt) { this.editedAt = editedAt; }

    @Override
    public String toString() {
        return "DeliveryOrder{deliveryNumber='" + deliveryNumber + "', status='" + status + "'}";
    }
}