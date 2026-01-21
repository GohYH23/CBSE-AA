package com.inventorymanagement.purchaseorder_ooiweiying.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class OrderItem {
    
    @NotBlank(message = "Item name is required")
    private String itemName;
    
    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
    
    @Min(value = 0, message = "Price must be positive")
    private double pricePerItem;
    
    public OrderItem() {}
    
    public OrderItem(String itemName, int quantity, double pricePerItem) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.pricePerItem = pricePerItem;
    }
    
    public String getItemName() {
        return itemName;
    }
    
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public double getPricePerItem() {
        return pricePerItem;
    }
    
    public void setPricePerItem(double pricePerItem) {
        this.pricePerItem = pricePerItem;
    }
    
    public double getTotalPrice() {
        return quantity * pricePerItem;
    }
    
    @Override
    public String toString() {
        return String.format("Item: %s, Qty: %d, Price: $%.2f, Total: $%.2f", 
            itemName, quantity, pricePerItem, getTotalPrice());
    }
}
