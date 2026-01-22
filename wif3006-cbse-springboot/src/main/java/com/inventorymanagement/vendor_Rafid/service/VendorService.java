package com.inventorymanagement.vendor_Rafid.service;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inventorymanagement.vendor_Rafid.model.*;
import com.inventorymanagement.vendor_Rafid.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VendorService {
    
    private final VendorRepository vendorRepository;
    private final VendorGroupRepository vendorGroupRepository;
    private final VendorCategoryRepository vendorCategoryRepository;
    private final VendorContactRepository vendorContactRepository;
    
    @Autowired
    public VendorService(
            VendorRepository vendorRepository,
            VendorGroupRepository vendorGroupRepository,
            VendorCategoryRepository vendorCategoryRepository,
            VendorContactRepository vendorContactRepository) {
        this.vendorRepository = vendorRepository;
        this.vendorGroupRepository = vendorGroupRepository;
        this.vendorCategoryRepository = vendorCategoryRepository;
        this.vendorContactRepository = vendorContactRepository;
    }
    
    // ========== VENDOR CRUD OPERATIONS ==========
    
    public Vendor createVendor(Vendor vendor) {
        // Generate vendor code if not provided
        if (vendor.getVendorCode() == null || vendor.getVendorCode().isEmpty()) {
            vendor.setVendorCode(generateVendorCode(vendor.getName()));
        }
        
        // Set timestamps
        vendor.setCreatedAt(LocalDateTime.now());
        vendor.setUpdatedAt(LocalDateTime.now());
        
        return vendorRepository.save(vendor);
    }
    
    public Optional<Vendor> getVendorById(String id) {
        return vendorRepository.findById(id);
    }
    
    public Optional<Vendor> getVendorByCode(String vendorCode) {
        return vendorRepository.findByVendorCode(vendorCode);
    }
    
    public List<Vendor> getAllVendors() {
        return vendorRepository.findAll();
    }
    
    public List<Vendor> getActiveVendors() {
        return vendorRepository.findByStatus("ACTIVE");
    }
    
    public Vendor updateVendor(String id, Vendor vendorDetails) {
        return vendorRepository.findById(id).map(vendor -> {
            // Update fields
            if (vendorDetails.getName() != null) vendor.setName(vendorDetails.getName());
            if (vendorDetails.getEmail() != null) vendor.setEmail(vendorDetails.getEmail());
            if (vendorDetails.getPhone() != null) vendor.setPhone(vendorDetails.getPhone());
            if (vendorDetails.getAddress() != null) vendor.setAddress(vendorDetails.getAddress());
            if (vendorDetails.getStatus() != null) vendor.setStatus(vendorDetails.getStatus());
            if (vendorDetails.getTaxNumber() != null) vendor.setTaxNumber(vendorDetails.getTaxNumber());
            if (vendorDetails.getPaymentTerms() != null) vendor.setPaymentTerms(vendorDetails.getPaymentTerms());
            if (vendorDetails.getCreditLimit() != null) vendor.setCreditLimit(vendorDetails.getCreditLimit());
            if (vendorDetails.getVendorGroupId() != null) vendor.setVendorGroupId(vendorDetails.getVendorGroupId());
            if (vendorDetails.getVendorCategoryId() != null) vendor.setVendorCategoryId(vendorDetails.getVendorCategoryId());
            
            vendor.setUpdatedAt(LocalDateTime.now());
            return vendorRepository.save(vendor);
        }).orElseThrow(() -> new RuntimeException("Vendor not found with id: " + id));
    }
    
    public void deleteVendor(String id) {
        // First delete all contacts associated with this vendor
        List<VendorContact> contacts = vendorContactRepository.findByVendorId(id);
        if (!contacts.isEmpty()) {
            vendorContactRepository.deleteAll(contacts);
        }
        
        // Then delete the vendor
        vendorRepository.deleteById(id);
    }
    
    public Vendor changeVendorStatus(String id, String status) {
        return vendorRepository.findById(id).map(vendor -> {
            vendor.setStatus(status);
            vendor.setUpdatedAt(LocalDateTime.now());
            return vendorRepository.save(vendor);
        }).orElseThrow(() -> new RuntimeException("Vendor not found with id: " + id));
    }
    
    // ========== VENDOR GROUP OPERATIONS ==========
    
    public VendorGroup createVendorGroup(VendorGroup vendorGroup) {
        if (vendorGroup.getCode() == null || vendorGroup.getCode().isEmpty()) {
            vendorGroup.setCode(generateGroupCode(vendorGroup.getName()));
        }
        
        vendorGroup.setCreatedAt(LocalDateTime.now());
        vendorGroup.setUpdatedAt(LocalDateTime.now());
        
        return vendorGroupRepository.save(vendorGroup);
    }
    
    public List<VendorGroup> getAllVendorGroups() {
        return vendorGroupRepository.findAllByOrderByNameAsc();
    }
    
    public Optional<VendorGroup> getVendorGroupById(String id) {
        return vendorGroupRepository.findById(id);
    }
    
    public VendorGroup updateVendorGroup(String id, VendorGroup vendorGroupDetails) {
        return vendorGroupRepository.findById(id).map(vendorGroup -> {
            if (vendorGroupDetails.getName() != null) vendorGroup.setName(vendorGroupDetails.getName());
            if (vendorGroupDetails.getDescription() != null) vendorGroup.setDescription(vendorGroupDetails.getDescription());
            
            vendorGroup.setUpdatedAt(LocalDateTime.now());
            return vendorGroupRepository.save(vendorGroup);
        }).orElseThrow(() -> new RuntimeException("Vendor group not found with id: " + id));
    }
    
    public void deleteVendorGroup(String id) {
        // Check if any vendor is using this group
        List<Vendor> vendors = vendorRepository.findByVendorGroupId(id);
        if (!vendors.isEmpty()) {
            throw new RuntimeException("Cannot delete vendor group. " + vendors.size() + " vendor(s) are using this group.");
        }
        
        vendorGroupRepository.deleteById(id);
    }
    
    // ========== VENDOR CATEGORY OPERATIONS ==========
    
    public VendorCategory createVendorCategory(VendorCategory vendorCategory) {
        if (vendorCategory.getCode() == null || vendorCategory.getCode().isEmpty()) {
            vendorCategory.setCode(generateCategoryCode(vendorCategory.getName()));
        }
        
        vendorCategory.setCreatedAt(LocalDateTime.now());
        vendorCategory.setUpdatedAt(LocalDateTime.now());
        
        return vendorCategoryRepository.save(vendorCategory);
    }
    
    public List<VendorCategory> getAllVendorCategories() {
        return vendorCategoryRepository.findAllByOrderByNameAsc();
    }
    
    public Optional<VendorCategory> getVendorCategoryById(String id) {
        return vendorCategoryRepository.findById(id);
    }
    
    public VendorCategory updateVendorCategory(String id, VendorCategory vendorCategoryDetails) {
        return vendorCategoryRepository.findById(id).map(vendorCategory -> {
            if (vendorCategoryDetails.getName() != null) vendorCategory.setName(vendorCategoryDetails.getName());
            if (vendorCategoryDetails.getDescription() != null) vendorCategory.setDescription(vendorCategoryDetails.getDescription());
            
            vendorCategory.setUpdatedAt(LocalDateTime.now());
            return vendorCategoryRepository.save(vendorCategory);
        }).orElseThrow(() -> new RuntimeException("Vendor category not found with id: " + id));
    }
    
    public void deleteVendorCategory(String id) {
        // Check if any vendor is using this category
        List<Vendor> vendors = vendorRepository.findByVendorCategoryId(id);
        if (!vendors.isEmpty()) {
            throw new RuntimeException("Cannot delete vendor category. " + vendors.size() + " vendor(s) are using this category.");
        }
        
        vendorCategoryRepository.deleteById(id);
    }
    
    // ========== VENDOR CONTACT OPERATIONS ==========
    
    public VendorContact createVendorContact(String vendorId, VendorContact vendorContact) {
        // Check if vendor exists
        if (!vendorRepository.existsById(vendorId)) {
            throw new RuntimeException("Vendor not found with id: " + vendorId);
        }
        
        vendorContact.setVendorId(vendorId);
        vendorContact.setCreatedAt(LocalDateTime.now());
        vendorContact.setUpdatedAt(LocalDateTime.now());
        
        // If this is the first contact or marked as primary, set as primary
        if (vendorContact.getIsPrimary() == null || vendorContact.getIsPrimary()) {
            setAsPrimaryContact(vendorId, vendorContact);
        }
        
        return vendorContactRepository.save(vendorContact);
    }
    
    public List<VendorContact> getVendorContacts(String vendorId) {
        return vendorContactRepository.findByVendorId(vendorId);
    }
    
    public Optional<VendorContact> getPrimaryContact(String vendorId) {
        return vendorContactRepository.findByVendorIdAndIsPrimary(vendorId, true);
    }
    
    public VendorContact updateVendorContact(String id, VendorContact vendorContactDetails) {
        return vendorContactRepository.findById(id).map(vendorContact -> {
            if (vendorContactDetails.getName() != null) vendorContact.setName(vendorContactDetails.getName());
            if (vendorContactDetails.getPosition() != null) vendorContact.setPosition(vendorContactDetails.getPosition());
            if (vendorContactDetails.getEmail() != null) vendorContact.setEmail(vendorContactDetails.getEmail());
            if (vendorContactDetails.getPhone() != null) vendorContact.setPhone(vendorContactDetails.getPhone());
            if (vendorContactDetails.getDepartment() != null) vendorContact.setDepartment(vendorContactDetails.getDepartment());
            
            if (vendorContactDetails.getIsPrimary() != null && vendorContactDetails.getIsPrimary()) {
                setAsPrimaryContact(vendorContact.getVendorId(), vendorContact);
            }
            
            vendorContact.setUpdatedAt(LocalDateTime.now());
            return vendorContactRepository.save(vendorContact);
        }).orElseThrow(() -> new RuntimeException("Vendor contact not found with id: " + id));
    }
    
    public void deleteVendorContact(String id) {
        vendorContactRepository.deleteById(id);
    }
    
    // ========== UTILITY METHODS ==========
    
    private String generateVendorCode(String vendorName) {
        String prefix = "VEND";
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);
        String nameInitials = vendorName.length() >= 3 ? 
                vendorName.substring(0, 3).toUpperCase() : 
                vendorName.toUpperCase();
        
        return prefix + "-" + nameInitials + "-" + timestamp;
    }
    
    private String generateGroupCode(String groupName) {
        return "GRP-" + groupName.replaceAll("\\s+", "-").toUpperCase();
    }
    
    private String generateCategoryCode(String categoryName) {
        return "CAT-" + categoryName.replaceAll("\\s+", "-").toUpperCase();
    }
    
    private void setAsPrimaryContact(String vendorId, VendorContact newPrimaryContact) {
        // Remove primary status from existing primary contact
        Optional<VendorContact> existingPrimary = vendorContactRepository.findByVendorIdAndIsPrimary(vendorId, true);
        if (existingPrimary.isPresent()) {
            VendorContact currentPrimary = existingPrimary.get();
            currentPrimary.setIsPrimary(false);
            vendorContactRepository.save(currentPrimary);
        }
        
        // Set new contact as primary
        newPrimaryContact.setIsPrimary(true);
    }
    
    public List<Vendor> searchVendorsByName(String name) {
        return vendorRepository.findByNameContainingIgnoreCase(name);
    }
    
    public List<Vendor> getVendorsByGroup(String groupId) {
        return vendorRepository.findByVendorGroupId(groupId);
    }
    
    public List<Vendor> getVendorsByCategory(String categoryId) {
        return vendorRepository.findByVendorCategoryId(categoryId);
    }
    
    public long countAllVendors() {
        return vendorRepository.count();
    }
    
    public long countActiveVendors() {
        return vendorRepository.countByStatus("ACTIVE");
    }
}
