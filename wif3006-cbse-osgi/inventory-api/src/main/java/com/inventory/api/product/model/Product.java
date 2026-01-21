package com.inventory.api.product.model;

import java.io.Serializable;

public class Product implements Serializable {
    // This ID is required to save the object to a file without errors
    private static final long serialVersionUID = 1L;

    //this Constructor to fix the test error
    public Product(String id, String name, double price, String productGroupId, String uomId) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.productGroupId = productGroupId;
        this.uomId = uomId;
    }

    // 1. The Attributes (What a product has)
    private String id;
    private String name;
    private double price;
    private int quantity;

    // --- NEW FIELDS FOR LINKING (UC-09) ---
    private String productGroupId;
    private String uomId;

    // 2. Default Constructor (Crucial for reading from files)
    public Product() {}

    // 3. Helper Constructor
    public Product(String id, String name, double price, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    // 4. Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    // --- NEW GETTERS AND SETTERS ---
    public String getProductGroupId() { return productGroupId; }
    public void setProductGroupId(String productGroupId) { this.productGroupId = productGroupId; }

    public String getUomId() { return uomId; }
    public void setUomId(String uomId) { this.uomId = uomId; }

    // 5. toString (Updated to show links)
    @Override
    public String toString() {
        return id + " | " + name + " | $" + price + " (Grp: " + productGroupId + ", UOM: " + uomId + ")";
    }
}