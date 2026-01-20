package com.inventorymanagement.salesorder_wongxiuhuan.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "taxes")
public class Tax {
    @Id
    private String id;
    
    @NotBlank(message = "Tax name is required")
    private String taxName;
    
    @NotNull(message = "Tax rate is required")
    @PositiveOrZero(message = "Tax rate must be zero or positive")
    private BigDecimal taxRate;
    
    private String description;
    
    @CreatedDate
    private LocalDateTime createdDate;
    
    @LastModifiedDate
    private LocalDateTime updatedDate;

    // Constructors
    public Tax() {}

    public Tax(String id, String taxName, BigDecimal taxRate, String description) {
        this.id = id;
        this.taxName = taxName;
        this.taxRate = taxRate;
        this.description = description;
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

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    // Helper method to calculate tax amount
    public BigDecimal calculateTaxAmount(BigDecimal baseAmount) {
        if (baseAmount == null || taxRate == null) {
            return BigDecimal.ZERO;
        }
        return baseAmount.multiply(taxRate).divide(BigDecimal.valueOf(100));
    }

    @Override
    public String toString() {
        return "Tax{id='" + id + "', taxName='" + taxName + "', rate=" + taxRate + "%}";
    }
}