package com.inventorymanagement.Vendor_Rafid; 

import com.inventorymanagement.vendor_Rafid.model.*;
import com.inventorymanagement.vendor_Rafid.service.VendorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class VendorServiceTest {

    @Autowired
    private VendorService vendorService;

    private Vendor testVendor;
    private VendorGroup testVendorGroup;
    private VendorCategory testVendorCategory;
    private VendorContact testVendorContact;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        cleanupTestData();
        
        // Create test vendor group
        testVendorGroup = new VendorGroup("Test Supplier Group", "TEST-GRP");
        testVendorGroup = vendorService.createVendorGroup(testVendorGroup);
        
        // Create test vendor category
        testVendorCategory = new VendorCategory("Test Supplier Category", "TEST-CAT");
        testVendorCategory = vendorService.createVendorCategory(testVendorCategory);
        
        // Create test vendor
        testVendor = new Vendor();
        testVendor.setName("Test Vendor Inc.");
        testVendor.setEmail("test@vendor.com");
        testVendor.setPhone("123-456-7890");
        testVendor.setAddress("123 Test St, Test City");
        testVendor.setTaxNumber("TAX-123456");
        testVendor.setPaymentTerms("Net 30");
        testVendor.setCreditLimit(50000.0);
        testVendor.setVendorGroupId(testVendorGroup.getId());
        testVendor.setVendorCategoryId(testVendorCategory.getId());
        
        testVendor = vendorService.createVendor(testVendor);
        
        // Create test vendor contact
        testVendorContact = new VendorContact();
        testVendorContact.setName("John Test");
        testVendorContact.setPosition("Sales Manager");
        testVendorContact.setEmail("john@vendor.com");
        testVendorContact.setPhone("987-654-3210");
        testVendorContact.setDepartment("Sales");
        testVendorContact.setIsPrimary(true);
        
        testVendorContact = vendorService.createVendorContact(testVendor.getId(), testVendorContact);
    }

    @Test
    void testCreateVendor() {
        assertNotNull(testVendor.getId());
        assertNotNull(testVendor.getVendorCode());
        assertEquals("Test Vendor Inc.", testVendor.getName());
        assertEquals("test@vendor.com", testVendor.getEmail());
        assertEquals("ACTIVE", testVendor.getStatus());
        assertEquals(50000.0, testVendor.getCreditLimit());
        assertNotNull(testVendor.getCreatedAt());
        assertNotNull(testVendor.getUpdatedAt());
    }

    @Test
    void testGetVendorById() {
        Optional<Vendor> foundVendor = vendorService.getVendorById(testVendor.getId());
        assertTrue(foundVendor.isPresent());
        assertEquals(testVendor.getName(), foundVendor.get().getName());
        assertEquals(testVendor.getEmail(), foundVendor.get().getEmail());
    }

    @Test
    void testGetVendorByCode() {
        Optional<Vendor> foundVendor = vendorService.getVendorByCode(testVendor.getVendorCode());
        assertTrue(foundVendor.isPresent());
        assertEquals(testVendor.getId(), foundVendor.get().getId());
        assertEquals(testVendor.getVendorCode(), foundVendor.get().getVendorCode());
    }

    @Test
    void testGetAllVendors() {
        List<Vendor> vendors = vendorService.getAllVendors();
        assertFalse(vendors.isEmpty());
        
        // Verify our test vendor is in the list
        boolean found = vendors.stream()
            .anyMatch(v -> v.getId().equals(testVendor.getId()));
        assertTrue(found);
    }

    @Test
    void testGetActiveVendors() {
        List<Vendor> activeVendors = vendorService.getActiveVendors();
        assertFalse(activeVendors.isEmpty());
        
        // All returned vendors should be active
        boolean allActive = activeVendors.stream()
            .allMatch(v -> "ACTIVE".equals(v.getStatus()));
        assertTrue(allActive);
        
        // Our test vendor should be in the list
        boolean found = activeVendors.stream()
            .anyMatch(v -> v.getId().equals(testVendor.getId()));
        assertTrue(found);
    }

    @Test
    void testUpdateVendor() {
        String vendorId = testVendor.getId();
        
        Vendor updates = new Vendor();
        updates.setName("Updated Vendor Name");
        updates.setEmail("updated@vendor.com");
        updates.setPhone("555-555-5555");
        updates.setCreditLimit(75000.0);
        updates.setStatus("INACTIVE");
        
        Vendor updatedVendor = vendorService.updateVendor(vendorId, updates);
        
        assertEquals(vendorId, updatedVendor.getId());
        assertEquals("Updated Vendor Name", updatedVendor.getName());
        assertEquals("updated@vendor.com", updatedVendor.getEmail());
        assertEquals("555-555-5555", updatedVendor.getPhone());
        assertEquals(75000.0, updatedVendor.getCreditLimit());
        assertEquals("INACTIVE", updatedVendor.getStatus());
        assertNotNull(updatedVendor.getUpdatedAt());
    }

    @Test
    void testChangeVendorStatus() {
        Vendor changedVendor = vendorService.changeVendorStatus(testVendor.getId(), "SUSPENDED");
        
        assertEquals("SUSPENDED", changedVendor.getStatus());
        assertEquals(testVendor.getId(), changedVendor.getId());
    }

    @Test
    void testSearchVendorsByName() {
        List<Vendor> searchResults = vendorService.searchVendorsByName("Test");
        assertFalse(searchResults.isEmpty());
        
        // Should find our test vendor
        boolean found = searchResults.stream()
            .anyMatch(v -> v.getId().equals(testVendor.getId()));
        assertTrue(found);
        
        // Test case-insensitive search
        List<Vendor> lowerCaseResults = vendorService.searchVendorsByName("test");
        assertFalse(lowerCaseResults.isEmpty());
    }

    @Test
    void testCreateVendorGroup() {
        assertNotNull(testVendorGroup.getId());
        assertNotNull(testVendorGroup.getCode());
        assertEquals("Test Supplier Group", testVendorGroup.getName());
        assertNotNull(testVendorGroup.getCreatedAt());
        assertNotNull(testVendorGroup.getUpdatedAt());
    }

    @Test
    void testGetAllVendorGroups() {
        List<VendorGroup> groups = vendorService.getAllVendorGroups();
        assertFalse(groups.isEmpty());
        
        boolean found = groups.stream()
            .anyMatch(g -> g.getId().equals(testVendorGroup.getId()));
        assertTrue(found);
    }

    @Test
    void testGetVendorsByGroup() {
        List<Vendor> vendorsInGroup = vendorService.getVendorsByGroup(testVendorGroup.getId());
        assertFalse(vendorsInGroup.isEmpty());
        
        // Our test vendor should be in this group
        boolean found = vendorsInGroup.stream()
            .anyMatch(v -> v.getId().equals(testVendor.getId()));
        assertTrue(found);
    }

    @Test
    void testUpdateVendorGroup() {
        String groupId = testVendorGroup.getId();
        
        VendorGroup updates = new VendorGroup();
        updates.setName("Updated Group Name");
        updates.setDescription("Updated group description");
        
        VendorGroup updatedGroup = vendorService.updateVendorGroup(groupId, updates);
        
        assertEquals(groupId, updatedGroup.getId());
        assertEquals("Updated Group Name", updatedGroup.getName());
        assertEquals("Updated group description", updatedGroup.getDescription());
        assertNotNull(updatedGroup.getUpdatedAt());
    }

    @Test
    void testCreateVendorCategory() {
        assertNotNull(testVendorCategory.getId());
        assertNotNull(testVendorCategory.getCode());
        assertEquals("Test Supplier Category", testVendorCategory.getName());
        assertNotNull(testVendorCategory.getCreatedAt());
        assertNotNull(testVendorCategory.getUpdatedAt());
    }

    @Test
    void testGetAllVendorCategories() {
        List<VendorCategory> categories = vendorService.getAllVendorCategories();
        assertFalse(categories.isEmpty());
        
        boolean found = categories.stream()
            .anyMatch(c -> c.getId().equals(testVendorCategory.getId()));
        assertTrue(found);
    }

    @Test
    void testGetVendorsByCategory() {
        List<Vendor> vendorsInCategory = vendorService.getVendorsByCategory(testVendorCategory.getId());
        assertFalse(vendorsInCategory.isEmpty());
        
        // Our test vendor should be in this category
        boolean found = vendorsInCategory.stream()
            .anyMatch(v -> v.getId().equals(testVendor.getId()));
        assertTrue(found);
    }

    @Test
    void testUpdateVendorCategory() {
        String categoryId = testVendorCategory.getId();
        
        VendorCategory updates = new VendorCategory();
        updates.setName("Updated Category Name");
        updates.setDescription("Updated category description");
        
        VendorCategory updatedCategory = vendorService.updateVendorCategory(categoryId, updates);
        
        assertEquals(categoryId, updatedCategory.getId());
        assertEquals("Updated Category Name", updatedCategory.getName());
        assertEquals("Updated category description", updatedCategory.getDescription());
        assertNotNull(updatedCategory.getUpdatedAt());
    }

    @Test
    void testCreateVendorContact() {
        assertNotNull(testVendorContact.getId());
        assertEquals(testVendor.getId(), testVendorContact.getVendorId());
        assertEquals("John Test", testVendorContact.getName());
        assertEquals("Sales Manager", testVendorContact.getPosition());
        assertEquals("john@vendor.com", testVendorContact.getEmail());
        assertTrue(testVendorContact.getIsPrimary());
        assertNotNull(testVendorContact.getCreatedAt());
        assertNotNull(testVendorContact.getUpdatedAt());
    }

    @Test
    void testGetVendorContacts() {
        List<VendorContact> contacts = vendorService.getVendorContacts(testVendor.getId());
        assertFalse(contacts.isEmpty());
        
        // Should contain our test contact
        boolean found = contacts.stream()
            .anyMatch(c -> c.getId().equals(testVendorContact.getId()));
        assertTrue(found);
        
        // Should have exactly 1 contact
        assertEquals(1, contacts.size());
    }

    @Test
    void testGetPrimaryContact() {
        Optional<VendorContact> primaryContact = vendorService.getPrimaryContact(testVendor.getId());
        assertTrue(primaryContact.isPresent());
        
        VendorContact contact = primaryContact.get();
        assertEquals(testVendorContact.getId(), contact.getId());
        assertTrue(contact.getIsPrimary());
    }

    @Test
    void testUpdateVendorContact() {
        String contactId = testVendorContact.getId();
        
        VendorContact updates = new VendorContact();
        updates.setName("Updated Contact Name");
        updates.setPosition("Operations Manager");
        updates.setEmail("updated@vendor.com");
        
        VendorContact updatedContact = vendorService.updateVendorContact(contactId, updates);
        
        assertEquals(contactId, updatedContact.getId());
        assertEquals("Updated Contact Name", updatedContact.getName());
        assertEquals("Operations Manager", updatedContact.getPosition());
        assertEquals("updated@vendor.com", updatedContact.getEmail());
        assertNotNull(updatedContact.getUpdatedAt());
    }

    @Test
    void testAddSecondContactAndSetPrimary() {
        // Create a second contact
        VendorContact secondContact = new VendorContact();
        secondContact.setName("Jane Smith");
        secondContact.setPosition("Customer Support");
        secondContact.setEmail("jane@vendor.com");
        secondContact.setPhone("111-222-3333");
        secondContact.setIsPrimary(true); // This should become primary
        
        VendorContact createdContact = vendorService.createVendorContact(testVendor.getId(), secondContact);
        
        // Verify new contact was created
        assertNotNull(createdContact.getId());
        assertTrue(createdContact.getIsPrimary());
        
        // Get primary contact - should now be the new one
        Optional<VendorContact> primaryContact = vendorService.getPrimaryContact(testVendor.getId());
        assertTrue(primaryContact.isPresent());
        assertEquals(createdContact.getId(), primaryContact.get().getId());
        
        // Old contact should no longer be primary
        Optional<VendorContact> oldContact = vendorService.getVendorContacts(testVendor.getId()).stream()
            .filter(c -> c.getId().equals(testVendorContact.getId()))
            .findFirst();
        assertTrue(oldContact.isPresent());
        assertFalse(oldContact.get().getIsPrimary());
    }

    @Test
    void testCountAllVendors() {
        long count = vendorService.countAllVendors();
        assertTrue(count > 0);
    }

    @Test
    void testCountActiveVendors() {
        long activeCount = vendorService.countActiveVendors();
        assertTrue(activeCount >= 1); // At least our test vendor
    }

    @Test
    void testDeleteVendorContact() {
        // Delete the contact
        vendorService.deleteVendorContact(testVendorContact.getId());
        
        // Verify contact is deleted
        List<VendorContact> contacts = vendorService.getVendorContacts(testVendor.getId());
        boolean found = contacts.stream()
            .anyMatch(c -> c.getId().equals(testVendorContact.getId()));
        assertFalse(found);
    }

    @Test
    void testDeleteVendor() {
        // First delete all contacts
        List<VendorContact> contacts = vendorService.getVendorContacts(testVendor.getId());
        for (VendorContact contact : contacts) {
            vendorService.deleteVendorContact(contact.getId());
        }
        
        // Now delete the vendor
        vendorService.deleteVendor(testVendor.getId());
        
        // Verify vendor is deleted
        Optional<Vendor> deletedVendor = vendorService.getVendorById(testVendor.getId());
        assertFalse(deletedVendor.isPresent());
        
        // Verify vendor group and category still exist
        Optional<VendorGroup> group = vendorService.getVendorGroupById(testVendorGroup.getId());
        assertTrue(group.isPresent());
        
        Optional<VendorCategory> category = vendorService.getVendorCategoryById(testVendorCategory.getId());
        assertTrue(category.isPresent());
    }

    @Test
    void testDeleteVendorGroupWithVendors() {
        // Try to delete group with vendors - should fail
        Exception exception = assertThrows(RuntimeException.class, () -> {
            vendorService.deleteVendorGroup(testVendorGroup.getId());
        });
        
        assertTrue(exception.getMessage().contains("Cannot delete vendor group"));
        assertTrue(exception.getMessage().contains("vendor(s) are using this group"));
    }

    @Test
    void testDeleteVendorCategoryWithVendors() {
        // Try to delete category with vendors - should fail
        Exception exception = assertThrows(RuntimeException.class, () -> {
            vendorService.deleteVendorCategory(testVendorCategory.getId());
        });
        
        assertTrue(exception.getMessage().contains("Cannot delete vendor category"));
        assertTrue(exception.getMessage().contains("vendor(s) are using this category"));
    }

    @Test
    void testDeleteVendorGroupWithoutVendors() {
        // Create a new group without vendors
        VendorGroup newGroup = new VendorGroup();
        newGroup.setName("Empty Group");
        newGroup.setDescription("Group with no vendors");
        VendorGroup createdGroup = vendorService.createVendorGroup(newGroup);
        
        // Delete it - should succeed
        assertDoesNotThrow(() -> {
            vendorService.deleteVendorGroup(createdGroup.getId());
        });
        
        // Verify it's deleted
        Optional<VendorGroup> deletedGroup = vendorService.getVendorGroupById(createdGroup.getId());
        assertFalse(deletedGroup.isPresent());
    }

    @Test
    void testDeleteVendorCategoryWithoutVendors() {
        // Create a new category without vendors
        VendorCategory newCategory = new VendorCategory();
        newCategory.setName("Empty Category");
        newCategory.setDescription("Category with no vendors");
        VendorCategory createdCategory = vendorService.createVendorCategory(newCategory);
        
        // Delete it - should succeed
        assertDoesNotThrow(() -> {
            vendorService.deleteVendorCategory(createdCategory.getId());
        });
        
        // Verify it's deleted
        Optional<VendorCategory> deletedCategory = vendorService.getVendorCategoryById(createdCategory.getId());
        assertFalse(deletedCategory.isPresent());
    }

    private void cleanupTestData() {
        // Clean up test vendors
        List<Vendor> vendors = vendorService.getAllVendors();
        for (Vendor vendor : vendors) {
            if (vendor.getName().contains("Test") || vendor.getEmail().contains("test")) {
                List<VendorContact> contacts = vendorService.getVendorContacts(vendor.getId());
                for (VendorContact contact : contacts) {
                    vendorService.deleteVendorContact(contact.getId());
                }
                vendorService.deleteVendor(vendor.getId());
            }
        }
        
        // Clean up test groups
        List<VendorGroup> groups = vendorService.getAllVendorGroups();
        for (VendorGroup group : groups) {
            if (group.getName().contains("Test") || group.getName().contains("Empty")) {
                try {
                    vendorService.deleteVendorGroup(group.getId());
                } catch (Exception e) {
                    // Ignore if can't delete due to foreign key constraints
                }
            }
        }
        
        // Clean up test categories
        List<VendorCategory> categories = vendorService.getAllVendorCategories();
        for (VendorCategory category : categories) {
            if (category.getName().contains("Test") || category.getName().contains("Empty")) {
                try {
                    vendorService.deleteVendorCategory(category.getId());
                } catch (Exception e) {
                    // Ignore if can't delete due to foreign key constraints
                }
            }
        }
    }
}