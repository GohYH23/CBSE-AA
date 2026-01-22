package com.inventorymanagement.vendor_Rafid.repository;




import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.inventorymanagement.vendor_Rafid.model.VendorCategory;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorCategoryRepository extends MongoRepository<VendorCategory, String> {
    
    // Find by category code
    Optional<VendorCategory> findByCode(String code);
    
    // Find by name (case-insensitive)
    List<VendorCategory> findByNameContainingIgnoreCase(String name);
    
    // Find all categories ordered by name
    List<VendorCategory> findAllByOrderByNameAsc();
    
    // Check if category exists by code
    boolean existsByCode(String code);
}