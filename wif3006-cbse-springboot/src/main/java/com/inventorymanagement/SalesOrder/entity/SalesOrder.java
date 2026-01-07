package com.inventorymanagement.SalesOrder.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales_orders")
public class SalesOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // "SO000001" style
    @Column(nullable = false, unique = true)
    private String number;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SalesOrderStatus orderStatus;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String taxId;

    private String createdById;
    private String updatedById;

    @Column(nullable = false)
    private Double beforeTaxAmount;

    @Column(nullable = false)
    private Double taxAmount;

    @Column(nullable = false)
    private Double afterTaxAmount;

    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalesOrderItem> items = new ArrayList<>();

    public SalesOrder() {
        this.orderDate = LocalDateTime.now();
        this.orderStatus = SalesOrderStatus.CREATED;
        this.beforeTaxAmount = 0.0;
        this.taxAmount = 0.0;
        this.afterTaxAmount = 0.0;
    }

    // ===== getters / setters =====
    public Long getId() { return id; }

    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public SalesOrderStatus getOrderStatus() { return orderStatus; }
    public void setOrderStatus(SalesOrderStatus orderStatus) { this.orderStatus = orderStatus; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }

    public String getCreatedById() { return createdById; }
    public void setCreatedById(String createdById) { this.createdById = createdById; }

    public String getUpdatedById() { return updatedById; }
    public void setUpdatedById(String updatedById) { this.updatedById = updatedById; }

    public Double getBeforeTaxAmount() { return beforeTaxAmount; }
    public void setBeforeTaxAmount(Double beforeTaxAmount) { this.beforeTaxAmount = beforeTaxAmount; }

    public Double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(Double taxAmount) { this.taxAmount = taxAmount; }

    public Double getAfterTaxAmount() { return afterTaxAmount; }
    public void setAfterTaxAmount(Double afterTaxAmount) { this.afterTaxAmount = afterTaxAmount; }

    public List<SalesOrderItem> getItems() { return items; }

    // helper
    public void addItem(SalesOrderItem item) {
        items.add(item);
        item.setSalesOrder(this);
    }
}