package com.inventory.api.product;

import java.io.Serializable;

public class Product implements Serializable {
    // This ID is required to save the object to a file without errors
    private static final long serialVersionUID = 1L;

    // 1. The Attributes (What a product has)
    private String id;
    private String name;
    private double price;
    private int quantity; // Useful for inventory tracking

    // 2. Default Constructor (Crucial for reading from files)
    public Product() {}

    // 3. Helper Constructor (Makes creating new products easier)
    public Product(String id, String name, double price, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    // 4. Getters and Setters (So other code can read/write these values)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    // 5. toString (Helps when printing the object for debugging)
    @Override
    public String toString() {
        return "Product [id=" + id + ", name=" + name + ", price=" + price + ", qty=" + quantity + "]";
    }
}