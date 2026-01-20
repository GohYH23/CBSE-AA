package com.inventory.api.salesorder.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SalesReturn implements Serializable {
    private String id;
    private String returnNumber;
    private LocalDate returnDate;
    private String deliveryOrderId;
    private String status;
    private String description;
    private String createdAt;
    private String editedAt;

    public SalesReturn() {
        this.createdAt = LocalDateTime.now().toString();
    }

    public SalesReturn(LocalDate returnDate, String deliveryOrderId, String status, String description) {
        this.returnDate = returnDate;
        this.deliveryOrderId = deliveryOrderId;
        this.status = status;
        this.description = description;
        this.createdAt = LocalDateTime.now().toString();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getReturnNumber() { return returnNumber; }
    public void setReturnNumber(String returnNumber) { this.returnNumber = returnNumber; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public String getDeliveryOrderId() { return deliveryOrderId; }
    public void setDeliveryOrderId(String deliveryOrderId) { this.deliveryOrderId = deliveryOrderId; }

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
        return "SalesReturn{returnNumber='" + returnNumber + "', status='" + status + "'}";
    }
}