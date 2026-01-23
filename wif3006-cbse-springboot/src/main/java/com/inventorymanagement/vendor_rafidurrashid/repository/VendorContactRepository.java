package com.inventorymanagement.vendor_rafidurrashid.repository;



import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.inventorymanagement.vendor_rafidurrashid.model.VendorContact;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorContactRepository extends MongoRepository<VendorContact, String> {
    
    // Find all contacts for a vendor
    List<VendorContact> findByVendorId(String vendorId);
    
    // Find primary contact for a vendor
    Optional<VendorContact> findByVendorIdAndIsPrimary(String vendorId, Boolean isPrimary);
    
    // Find by email
    Optional<VendorContact> findByEmail(String email);
    
    // Find by name (case-insensitive)
    List<VendorContact> findByNameContainingIgnoreCase(String name);
    
    // Find contacts by department
    List<VendorContact> findByDepartment(String department);
    
    // Count contacts for a vendor
    long countByVendorId(String vendorId);
    
    // Delete all contacts for a vendor
    void deleteByVendorId(String vendorId);
}