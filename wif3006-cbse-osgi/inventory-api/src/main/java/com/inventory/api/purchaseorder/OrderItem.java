package com.inventory.api.purchaseorder;

import java.io.Serializable;

public class OrderItem implements Serializable {
    private String itemName;
    private int quantity;
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
