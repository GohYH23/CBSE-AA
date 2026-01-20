package com.inventorymanagement.salesorder_wongxiuhuan.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "delivery_orders")
public class DeliveryOrder {
    @Id
    private String id;
    
    @NotNull(message = "Delivery date is required")
    private LocalDate deliveryDate;
    
    private String deliveryNumber;
    
    @NotBlank(message = "Sales Order is required")
    private String salesOrderId;
    
    @NotBlank(message = "Status is required")
    private String status;
    
    private String description;
    
    @CreatedDate
    private LocalDateTime createdDate;
    
    @LastModifiedDate
    private LocalDateTime updatedDate;

    // Constructors
    public DeliveryOrder() {}

    public DeliveryOrder(String id, LocalDate deliveryDate, String deliveryNumber, 
                        String salesOrderId, String status, String description) {
        this.id = id;
        this.deliveryDate = deliveryDate;
        this.deliveryNumber = deliveryNumber;
        this.salesOrderId = salesOrderId;
        this.status = status;
        this.description = description;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDate getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDate deliveryDate) { this.deliveryDate = deliveryDate; }

    public String getDeliveryNumber() { return deliveryNumber; }
    public void setDeliveryNumber(String deliveryNumber) { this.deliveryNumber = deliveryNumber; }

    public String getSalesOrderId() { return salesOrderId; }
    public void setSalesOrderId(String salesOrderId) { this.salesOrderId = salesOrderId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    @Override
    public String toString() {
        return "DeliveryOrder{id='" + id + "', deliveryNumber='" + deliveryNumber + "', created='" + createdDate + "'}";
    }
}