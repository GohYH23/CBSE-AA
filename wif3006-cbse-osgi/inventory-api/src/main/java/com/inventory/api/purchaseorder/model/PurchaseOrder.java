package com.inventory.api.purchaseorder.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrder implements Serializable {
    private int orderId;
    private LocalDate orderDate;
    private String orderNumber;
    private String vendor;
    private List<OrderItem> orderItems;
    private String orderStatus;
    private LocalDate receivedDate; // Date when order status changed to "received"
    private LocalDate returnedDate; // Date when order status changed to "returned"
    private LocalDate shippingDate; // Date when order status changed to "shipping"
    private LocalDate cancelledDate; // Date when order status changed to "cancelled"
    
    public PurchaseOrder() {
        this.orderItems = new ArrayList<>();
    }
    
    public PurchaseOrder(int orderId, LocalDate orderDate, String orderNumber, 
                        String vendor, List<OrderItem> orderItems, String orderStatus) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.orderNumber = orderNumber;
        this.vendor = vendor;
        this.orderItems = orderItems != null ? new ArrayList<>(orderItems) : new ArrayList<>();
        this.orderStatus = orderStatus;
    }
    
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
    
    public String getVendor() {
        return vendor;
    }
    
    public void setVendor(String vendor) {
        this.vendor = vendor;
    }
    
    public List<OrderItem> getOrderItems() {
        return new ArrayList<>(orderItems);
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
        return orderItems.stream()
            .mapToDouble(OrderItem::getTotalPrice)
            .sum();
    }
    
    @Override
    public String toString() {
        return String.format("PurchaseOrder{id=%d, date=%s, number='%s', vendor='%s', status='%s', total=$%.2f}",
            orderId, orderDate, orderNumber, vendor, orderStatus, getTotalPrice());
    }
    
    public String toDisplayString() {
        return String.format("ID: %d | Date: %s | Number: %s | Vendor: %s | Status: %s | Total: $%.2f",
            orderId, orderDate.format(DateTimeFormatter.ISO_LOCAL_DATE), orderNumber, vendor, orderStatus, getTotalPrice());
    }
}
