package com.inventory.api.salesorder.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Tax implements Serializable {
    private String id;
    private String taxName;
    private BigDecimal taxRate;
    private String description;
    private String createdAt;
    private String editedAt;

    public Tax() {
        this.createdAt = LocalDateTime.now().toString();
    }

    public Tax(String taxName, BigDecimal taxRate, String description) {
        this.taxName = taxName;
        this.taxRate = taxRate;
        this.description = description;
        this.createdAt = LocalDateTime.now().toString();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTaxName() { return taxName; }
    public void setTaxName(String taxName) { this.taxName = taxName; }

    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getEditedAt() { return editedAt; }
    public void setEditedAt(String editedAt) { this.editedAt = editedAt; }

    @Override
    public String toString() {
        return "Tax{name='" + taxName + "', rate=" + taxRate + "%}";
    }
}