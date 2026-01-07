package com.inventorymanagement.SalesOrder.dto;

import java.time.LocalDateTime;

public class SalesOrderListDto {
    private Long id;
    private String number;
    private LocalDateTime orderDate;
    private String orderStatus;
    private String orderStatusName;
    private String description;
    private String customerId;
    private String taxId;
    private Double beforeTaxAmount;
    private Double taxAmount;
    private Double afterTaxAmount;

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public String getOrderStatusName() { return orderStatusName; }
    public void setOrderStatusName(String orderStatusName) { this.orderStatusName = orderStatusName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }

    public Double getBeforeTaxAmount() { return beforeTaxAmount; }
    public void setBeforeTaxAmount(Double beforeTaxAmount) { this.beforeTaxAmount = beforeTaxAmount; }

    public Double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(Double taxAmount) { this.taxAmount = taxAmount; }

    public Double getAfterTaxAmount() { return afterTaxAmount; }
    public void setAfterTaxAmount(Double afterTaxAmount) { this.afterTaxAmount = afterTaxAmount; }
}
