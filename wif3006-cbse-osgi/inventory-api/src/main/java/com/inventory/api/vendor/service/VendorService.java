package com.inventory.api.vendor.service;

import com.inventory.api.vendor.model.Vendor;
import java.util.List;
import java.util.Optional;

public interface VendorService {
    
    // CRUD Operations
    Vendor addVendor(Vendor vendor);
    
    Optional<Vendor> getVendor(String vendorId);
    
    List<Vendor> getAllVendors();
    
    Vendor updateVendor(Vendor vendor);
    
    boolean deleteVendor(String vendorId);
    
    // Search Operations
    List<Vendor> searchVendorsByName(String name);
    
    List<Vendor> getVendorsByRating(double minRating);
    
    List<Vendor> getActiveVendors();
    
    List<Vendor> getVendorsByCategory(String category);
    
    // Update Operations
    boolean updateVendorRating(String vendorId, double newRating);
    
    boolean activateVendor(String vendorId);
    
    boolean deactivateVendor(String vendorId);
    
    // Utility Operations
    int getVendorCount();
    
    int getActiveVendorCount();
    
    boolean isValidVendor(Vendor vendor);
    
    boolean vendorExists(String vendorId);
    
    // Validation
    boolean validateVendorId(String vendorId);
    
    boolean validateEmail(String email);
    
    boolean validatePhone(String phone);
}