package com.inventorymanagement.purchaseorder_ooiweiying.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "purchase_orders")
public class PurchaseOrder {
    
    @Id
    private String id; // MongoDB ObjectId
    
    private int orderId; // Sequential ID for display purposes
    
    @NotBlank(message = "Order number is required")
    private String orderNumber; // Auto-generated format: PO-XXX
    
    private LocalDate orderDate;
    
    @NotBlank(message = "Vendor is required")
    private String vendor;
    
    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<OrderItem> orderItems;
    
    @NotBlank(message = "Order status is required")
    private String orderStatus; // pending, shipping, received, cancelled, returned
    
    private LocalDate receivedDate; // Date when status changed to "received"
    private LocalDate returnedDate; // Date when status changed to "returned"
    private LocalDate shippingDate; // Date when status changed to "shipping"
    private LocalDate cancelledDate; // Date when status changed to "cancelled"
    
    public PurchaseOrder() {
        this.orderItems = new ArrayList<>();
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public int getOrderId() {
        return orderId;
    }
    
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
    
    public String getOrderNumber() {
        return orderNumber;
    }
    
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
    
    public LocalDate getOrderDate() {
        return orderDate;
    }
    
    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }
    
    public String getVendor() {
        return vendor;
    }
    
    public void setVendor(String vendor) {
        this.vendor = vendor;
    }
    
    public List<OrderItem> getOrderItems() {
        return orderItems != null ? new ArrayList<>(orderItems) : new ArrayList<>();
    }
    
    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems != null ? new ArrayList<>(orderItems) : new ArrayList<>();
    }
    
    public String getOrderStatus() {
        return orderStatus;
    }
    
    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
    
    public LocalDate getReceivedDate() {
        return receivedDate;
    }
    
    public void setReceivedDate(LocalDate receivedDate) {
        this.receivedDate = receivedDate;
    }
    
    public LocalDate getReturnedDate() {
        return returnedDate;
    }
    
    public void setReturnedDate(LocalDate returnedDate) {
        this.returnedDate = returnedDate;
    }
    
    public LocalDate getShippingDate() {
        return shippingDate;
    }
    
    public void setShippingDate(LocalDate shippingDate) {
        this.shippingDate = shippingDate;
    }
    
    public LocalDate getCancelledDate() {
        return cancelledDate;
    }
    
    public void setCancelledDate(LocalDate cancelledDate) {
        this.cancelledDate = cancelledDate;
    }
    
    public double getTotalPrice() {
        if (orderItems == null || orderItems.isEmpty()) {
            return 0.0;
        }
        return orderItems.stream()
            .mapToDouble(OrderItem::getTotalPrice)
            .sum();
    }
    
    @Override
    public String toString() {
        return String.format("PurchaseOrder{id=%s, orderId=%d, date=%s, number='%s', vendor='%s', status='%s', total=$%.2f}",
            id, orderId, orderDate, orderNumber, vendor, orderStatus, getTotalPrice());
    }
    
    public String toDisplayString() {
        return String.format("ID: %d | Date: %s | Number: %s | Vendor: %s | Status: %s | Total: $%.2f",
            orderId, 
            orderDate != null ? orderDate.format(DateTimeFormatter.ISO_LOCAL_DATE) : "N/A",
            orderNumber, 
            vendor, 
            orderStatus, 
            getTotalPrice());
    }
}
