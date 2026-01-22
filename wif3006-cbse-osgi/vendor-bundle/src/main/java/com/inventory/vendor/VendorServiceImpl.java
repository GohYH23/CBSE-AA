package com.inventory.vendor;

import com.inventory.vendor.api.VendorService;
import org.osgi.service.component.annotations.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component(
    service = VendorService.class,
    immediate = true,
    property = {
        "service.description=Vendor Management Service",
        "service.vendor=Inventory System"
    }
)
public class VendorServiceImpl implements VendorService {
    
    private final Map<String, Vendor> vendors = new ConcurrentHashMap<>();
    
    @Override
    public Vendor addVendor(Vendor vendor) {
        if (!isValidVendor(vendor)) {
            throw new IllegalArgumentException("Invalid vendor data");
        }
        
        if (vendorExists(vendor.getVendorId())) {
            throw new IllegalStateException("Vendor already exists with ID: " + vendor.getVendorId());
        }
        
        vendors.put(vendor.getVendorId(), vendor);
        System.out.println("Vendor added successfully: " + vendor.getVendorId());
        return vendor;
    }
    
    @Override
    public Optional<Vendor> getVendor(String vendorId) {
        if (vendorId == null || vendorId.trim().isEmpty()) {
            throw new IllegalArgumentException("Vendor ID cannot be null or empty");
        }
        return Optional.ofNullable(vendors.get(vendorId));
    }
    
    @Override
    public List<Vendor> getAllVendors() {
        return new ArrayList<>(vendors.values());
    }
    
    @Override
    public Vendor updateVendor(Vendor vendor) {
        if (!isValidVendor(vendor)) {
            throw new IllegalArgumentException("Invalid vendor data");
        }
        
        if (!vendorExists(vendor.getVendorId())) {
            throw new IllegalStateException("Vendor not found: " + vendor.getVendorId());
        }
        
        vendors.put(vendor.getVendorId(), vendor);
        System.out.println("Vendor updated successfully: " + vendor.getVendorId());
        return vendor;
    }
    
    @Override
    public boolean deleteVendor(String vendorId) {
        if (vendorId == null || vendorId.trim().isEmpty()) {
            throw new IllegalArgumentException("Vendor ID cannot be null or empty");
        }
        
        boolean removed = vendors.remove(vendorId) != null;
        if (removed) {
            System.out.println("Vendor deleted successfully: " + vendorId);
        }
        return removed;
    }
    
    @Override
    public List<Vendor> searchVendorsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String searchTerm = name.toLowerCase();
        return vendors.values().stream()
            .filter(vendor -> vendor.getName().toLowerCase().contains(searchTerm))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Vendor> getVendorsByRating(double minRating) {
        return vendors.values().stream()
            .filter(vendor -> vendor.getRating() >= minRating)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Vendor> getActiveVendors() {
        return vendors.values().stream()
            .filter(Vendor::isActive)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Vendor> getInactiveVendors() {
        return vendors.values().stream()
            .filter(vendor -> !vendor.isActive())
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean updateVendorRating(String vendorId, double newRating) {
        if (vendorId == null || vendorId.trim().isEmpty()) {
            throw new IllegalArgumentException("Vendor ID cannot be null or empty");
        }
        
        if (newRating < 0 || newRating > 5) {
            throw new IllegalArgumentException("Rating must be between 0 and 5");
        }
        
        Vendor vendor = vendors.get(vendorId);
        if (vendor != null) {
            vendor.setRating(newRating);
            System.out.println("Vendor rating updated: " + vendorId + " = " + newRating);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean activateVendor(String vendorId) {
        Vendor vendor = vendors.get(vendorId);
        if (vendor != null && !vendor.isActive()) {
            vendor.setActive(true);
            System.out.println("Vendor activated: " + vendorId);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean deactivateVendor(String vendorId) {
        Vendor vendor = vendors.get(vendorId);
        if (vendor != null && vendor.isActive()) {
            vendor.setActive(false);
            System.out.println("Vendor deactivated: " + vendorId);
            return true;
        }
        return false;
    }
    
    @Override
    public int getVendorCount() {
        return vendors.size();
    }
    
    @Override
    public int getActiveVendorCount() {
        return (int) vendors.values().stream()
            .filter(Vendor::isActive)
            .count();
    }
    
    @Override
    public boolean isValidVendor(Vendor vendor) {
        return vendor != null &&
               validateVendorId(vendor.getVendorId()) &&
               vendor.getName() != null && !vendor.getName().trim().isEmpty() &&
               validateEmail(vendor.getEmail()) &&
               validatePhone(vendor.getPhone()) &&
               vendor.getAddress() != null && !vendor.getAddress().trim().isEmpty();
    }
    
    @Override
    public boolean vendorExists(String vendorId) {
        return vendorId != null && vendors.containsKey(vendorId);
    }
    
    @Override
    public boolean validateVendorId(String vendorId) {
        return vendorId != null && 
               vendorId.matches("V\\d{3}") && // Format: V001, V002, etc.
               vendorId.length() == 4;
    }
    
    @Override
    public boolean validateEmail(String email) {
        return email != null && 
               email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
    
    @Override
    public boolean validatePhone(String phone) {
        return phone != null && 
               phone.matches("\\d{10}");
    }
    
    // Initialize with sample data
    private void initializeSampleData() {
        try {
            addVendor(new Vendor("V001", "Tech Solutions Inc.", "John Smith", 
                "john@techsolutions.com", "9876543210", "123 Tech Park, Bangalore"));
            
            addVendor(new Vendor("V002", "Office Supplies Ltd.", "Jane Doe", 
                "jane@officesupplies.com", "9876543211", "456 Business Avenue, Mumbai"));
            
            addVendor(new Vendor("V003", "Raw Materials Corp.", "Robert Johnson", 
                "robert@rawmaterials.com", "9876543212", "789 Industrial Area, Delhi"));
            
            // Set ratings
            updateVendorRating("V001", 4.5);
            updateVendorRating("V002", 4.0);
            updateVendorRating("V003", 4.8);
            
            // Deactivate one vendor for demo
            deactivateVendor("V003");
            
            System.out.println("Sample vendor data initialized successfully!");
        } catch (Exception e) {
            System.err.println("Error initializing sample data: " + e.getMessage());
        }
    }
    
    // Component lifecycle methods
    @SuppressWarnings("unused")
    private void activate() {
        System.out.println("VendorServiceImpl activated");
        initializeSampleData();
    }
    
    @SuppressWarnings("unused")
    private void deactivate() {
        System.out.println("VendorServiceImpl deactivated");
        vendors.clear();
    }
}