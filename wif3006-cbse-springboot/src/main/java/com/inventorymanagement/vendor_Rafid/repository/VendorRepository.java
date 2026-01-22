package com.inventorymanagement.vendor_Rafid.repository;



import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.inventorymanagement.vendor_Rafid.model.Vendor;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorRepository extends MongoRepository<Vendor, String> {
    
    // Find by vendor code
    Optional<Vendor> findByVendorCode(String vendorCode);
    
    // Find by name (case-insensitive search)
    List<Vendor> findByNameContainingIgnoreCase(String name);
    
    // Find by status
    List<Vendor> findByStatus(String status);
    
    // Find by vendor group
    List<Vendor> findByVendorGroupId(String vendorGroupId);
    
    // Find by vendor category
    List<Vendor> findByVendorCategoryId(String vendorCategoryId);
    
    // Find by email
    Optional<Vendor> findByEmail(String email);
    
    // Find by tax number
    Optional<Vendor> findByTaxNumber(String taxNumber);
    
    // Find vendors with credit limit above certain value
    @Query("{ 'creditLimit': { $gt: ?0 } }")
    List<Vendor> findVendorsWithCreditLimitAbove(Double minCreditLimit);
    
    // Count vendors by status
    long countByStatus(String status);
    
    // Find active vendors
    List<Vendor> findByStatusOrderByNameAsc(String status);
}