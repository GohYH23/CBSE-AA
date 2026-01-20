package com.inventorymanagement.salesorder_wongxiuhuan.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "taxes")
public class Tax {
    @Id
    private String id;
    private String taxName;
    private BigDecimal taxRate;
    private String description;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    // Constructors
    public Tax() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    public Tax(String id, String taxName, BigDecimal taxRate, String description) {
        this.id = id;
        this.taxName = taxName;
        this.taxRate = taxRate;
        this.description = description;
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTaxName() {
        return taxName;
    }

    public void setTaxName(String taxName) {
        this.taxName = taxName;
        this.updatedDate = LocalDateTime.now();
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
        this.updatedDate = LocalDateTime.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedDate = LocalDateTime.now();
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    // Helper method to calculate tax amount
    public BigDecimal calculateTaxAmount(BigDecimal baseAmount) {
        if (baseAmount == null || taxRate == null) {
            return BigDecimal.ZERO;
        }
        return baseAmount.multiply(taxRate).divide(BigDecimal.valueOf(100));
    }

    @Override
    public String toString() {
        return "Tax{" +
                "id='" + id + '\'' +
                ", taxName='" + taxName + '\'' +
                ", taxRate=" + taxRate +
                ", description='" + description + '\'' +
                ", createdDate=" + createdDate +
                ", updatedDate=" + updatedDate +
                '}';
    }
}