package com.inventorymanagement.product_ericleechunkiat.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "products")
public class Product {
    @Id
    private String id;
    private String name;
    private double price;
    private String productGroupId;
    private String uomId;

    public Product() {}

    public Product(String id, String name, double price, String productGroupId, String uomId) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.productGroupId = productGroupId;
        this.uomId = uomId;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getProductGroupId() { return productGroupId; }
    public void setProductGroupId(String productGroupId) { this.productGroupId = productGroupId; }
    public String getUomId() { return uomId; }
    public void setUomId(String uomId) { this.uomId = uomId; }
}