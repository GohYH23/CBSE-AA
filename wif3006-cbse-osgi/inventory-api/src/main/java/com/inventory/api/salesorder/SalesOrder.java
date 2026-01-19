package com.inventory.api.salesorder;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SalesOrder implements Serializable {
    private int orderId;
    private LocalDate orderDate;
    private String orderNumber; 
    private String customerName; 
    private List<SalesOrderItem> orderItems;
    private String orderStatus;
    private LocalDate deliveryDate; 
    private LocalDate returnedDate; 
    
    public SalesOrder() {
        this.orderItems = new ArrayList<>();
    }
    
    public SalesOrder(int orderId, LocalDate orderDate, String orderNumber, 
                      String customerName, List<SalesOrderItem> orderItems, String orderStatus) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.orderNumber = orderNumber;
        this.customerName = customerName;
        this.orderItems = orderItems != null ? new ArrayList<>(orderItems) : new ArrayList<>();
        this.orderStatus = orderStatus;
    }
    
    // --- Getters and Setters ---

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomer() {
    return this.customerName;
}

    public void setCustomer(String customerName) {
        this.customerName = customerName;
    }

    public String getStatus() {
        return this.orderStatus;
    }

    public void setStatus(String status) {
        this.orderStatus = status;
    }

    public void setShippedDate(LocalDate date) {
        this.deliveryDate = date;
    }

    public LocalDate getShippedDate() {
        return this.deliveryDate;
    }

    public List<SalesOrderItem> getSalesOrderItems() {
        return this.orderItems;
    }

    public void setItems(List<SalesOrderItem> items) {
        this.orderItems = items;
    }

    public List<SalesOrderItem> getOrderItems() {
        return new ArrayList<>(orderItems);
    }

    public void setOrderItems(List<SalesOrderItem> orderItems) {
        this.orderItems = orderItems != null ? new ArrayList<>(orderItems) : new ArrayList<>();
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public LocalDate getReturnedDate() {
        return returnedDate;
    }

    public void setReturnedDate(LocalDate returnedDate) {
        this.returnedDate = returnedDate;
    }

    public double getTotalPrice() {
        return orderItems.stream()
            .mapToDouble(SalesOrderItem::getTotalPrice)
            .sum();
    }
    
    @Override
    public String toString() {
        return String.format("SalesOrder{id=%d, date=%s, number='%s', customer='%s', status='%s', total=$%.2f}",
            orderId, orderDate, orderNumber, customerName, orderStatus, getTotalPrice());
    }
    
    public String toDisplayString() {
        return String.format("ID: %d | Date: %s | Number: %s | Customer: %s | Status: %s | Total: $%.2f",
            orderId, orderDate.format(DateTimeFormatter.ISO_LOCAL_DATE), orderNumber, customerName, orderStatus, getTotalPrice());
    }
}