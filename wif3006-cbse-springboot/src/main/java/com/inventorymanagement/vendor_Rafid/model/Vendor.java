package com.inventorymanagement.vendor_Rafid.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;

@Document(collection = "vendors")
public class Vendor {
    @Id
    private String id;
    
    private String name;
    private String email;
    private String phone;
    private String address;
    private String status; // ACTIVE, INACTIVE, SUSPENDED
    
    @Field("vendor_code")
    private String vendorCode;
    
    @Field("tax_number")
    private String taxNumber;
    
    @Field("payment_terms")
    private String paymentTerms;
    
    @Field("credit_limit")
    private Double creditLimit;
    
    @Field("vendor_group_id")
    private String vendorGroupId;
    
    @Field("vendor_category_id")
    private String vendorCategoryId;
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Vendor() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "ACTIVE";
        this.creditLimit = 0.0;
    }
    
    public Vendor(String name, String email, String phone) {
        this();
        this.name = name;
        this.email = email;
        this.phone = phone;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getVendorCode() { return vendorCode; }
    public void setVendorCode(String vendorCode) { this.vendorCode = vendorCode; }
    
    public String getTaxNumber() { return taxNumber; }
    public void setTaxNumber(String taxNumber) { this.taxNumber = taxNumber; }
    
    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }
    
    public Double getCreditLimit() { return creditLimit; }
    public void setCreditLimit(Double creditLimit) { this.creditLimit = creditLimit; }
    
    public String getVendorGroupId() { return vendorGroupId; }
    public void setVendorGroupId(String vendorGroupId) { this.vendorGroupId = vendorGroupId; }
    
    public String getVendorCategoryId() { return vendorCategoryId; }
    public void setVendorCategoryId(String vendorCategoryId) { this.vendorCategoryId = vendorCategoryId; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public String toString() {
        return "Vendor{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
