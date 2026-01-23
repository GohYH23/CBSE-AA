package com.inventorymanagement.vendor_rafidurrashid.repository;



import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.inventorymanagement.vendor_rafidurrashid.model.VendorGroup;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorGroupRepository extends MongoRepository<VendorGroup, String> {
    
    // Find by group code
    Optional<VendorGroup> findByCode(String code);
    
    // Find by name (case-insensitive)
    List<VendorGroup> findByNameContainingIgnoreCase(String name);
    
    // Find all groups ordered by name
    List<VendorGroup> findAllByOrderByNameAsc();
    
    // Check if group exists by code
    boolean existsByCode(String code);
}