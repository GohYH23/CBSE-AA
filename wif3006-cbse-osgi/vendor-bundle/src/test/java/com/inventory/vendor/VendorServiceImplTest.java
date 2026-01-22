package com.inventory.vendor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import static org.junit.Assert.*;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class VendorServiceImplTest {
    
    private VendorServiceImpl vendorService;
    
    @Before
    public void setUp() {
        vendorService = new VendorServiceImpl();
    }
    
    @Test
    public void testAddVendor_Success() {
        Vendor vendor = createValidVendor("V001");
        Vendor addedVendor = vendorService.addVendor(vendor);
        
        assertNotNull(addedVendor);
        assertEquals("V001", addedVendor.getVendorId());
        assertEquals("Test Vendor", addedVendor.getName());
        assertTrue(vendorService.vendorExists("V001"));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testAddVendor_InvalidData() {
        Vendor invalidVendor = new Vendor();
        vendorService.addVendor(invalidVendor);
    }
    
    @Test(expected = IllegalStateException.class)
    public void testAddVendor_Duplicate() {
        Vendor vendor1 = createValidVendor("V001");
        Vendor vendor2 = createValidVendor("V001");
        
        vendorService.addVendor(vendor1);
        vendorService.addVendor(vendor2); // Should throw exception
    }
    
    @Test
    public void testGetVendor_Exists() {
        Vendor vendor = createValidVendor("V001");
        vendorService.addVendor(vendor);
        
        assertTrue(vendorService.getVendor("V001").isPresent());
        assertEquals("Test Vendor", vendorService.getVendor("V001").get().getName());
    }
    
    @Test
    public void testGetVendor_NotExists() {
        assertFalse(vendorService.getVendor("V999").isPresent());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetVendor_NullId() {
        vendorService.getVendor(null);
    }
    
    @Test
    public void testGetAllVendors() {
        assertEquals(0, vendorService.getAllVendors().size());
        
        vendorService.addVendor(createValidVendor("V001"));
        vendorService.addVendor(createValidVendor("V002"));
        
        List<Vendor> vendors = vendorService.getAllVendors();
        assertEquals(2, vendors.size());
    }
    
    @Test
    public void testUpdateVendor_Success() {
        Vendor vendor = createValidVendor("V001");
        vendorService.addVendor(vendor);
        
        vendor.setName("Updated Name");
        vendor.setEmail("updated@test.com");
        
        Vendor updatedVendor = vendorService.updateVendor(vendor);
        
        assertEquals("Updated Name", updatedVendor.getName());
        assertEquals("updated@test.com", updatedVendor.getEmail());
    }
    
    @Test(expected = IllegalStateException.class)
    public void testUpdateVendor_NotExists() {
        Vendor vendor = createValidVendor("V999");
        vendorService.updateVendor(vendor);
    }
    
    @Test
    public void testDeleteVendor_Success() {
        Vendor vendor = createValidVendor("V001");
        vendorService.addVendor(vendor);
        
        assertTrue(vendorService.deleteVendor("V001"));
        assertFalse(vendorService.vendorExists("V001"));
    }
    
    @Test
    public void testDeleteVendor_NotExists() {
        assertFalse(vendorService.deleteVendor("V999"));
    }
    
    @Test
    public void testSearchVendorsByName() {
        vendorService.addVendor(new Vendor("V001", "Tech Solutions", "John", 
            "john@tech.com", "9876543210", "Address"));
        vendorService.addVendor(new Vendor("V002", "Office Tech", "Jane", 
            "jane@office.com", "9876543211", "Address"));
        vendorService.addVendor(new Vendor("V003", "Food Supplies", "Bob", 
            "bob@food.com", "9876543212", "Address"));
        
        List<Vendor> results = vendorService.searchVendorsByName("tech");
        assertEquals(2, results.size());
    }
    
    @Test
    public void testGetVendorsByRating() {
        Vendor vendor1 = createValidVendor("V001");
        Vendor vendor2 = createValidVendor("V002");
        Vendor vendor3 = createValidVendor("V003");
        
        vendor1.setRating(4.5);
        vendor2.setRating(3.5);
        vendor3.setRating(4.0);
        
        vendorService.addVendor(vendor1);
        vendorService.addVendor(vendor2);
        vendorService.addVendor(vendor3);
        
        List<Vendor> highRated = vendorService.getVendorsByRating(4.0);
        assertEquals(2, highRated.size());
    }
    
    @Test
    public void testGetActiveVendors() {
        Vendor vendor1 = createValidVendor("V001");
        Vendor vendor2 = createValidVendor("V002");
        vendor2.setActive(false);
        
        vendorService.addVendor(vendor1);
        vendorService.addVendor(vendor2);
        
        List<Vendor> activeVendors = vendorService.getActiveVendors();
        assertEquals(1, activeVendors.size());
        assertEquals("V001", activeVendors.get(0).getVendorId());
    }
    
    @Test
    public void testUpdateVendorRating_Success() {
        Vendor vendor = createValidVendor("V001");
        vendorService.addVendor(vendor);
        
        assertTrue(vendorService.updateVendorRating("V001", 4.5));
        assertEquals(4.5, vendorService.getVendor("V001").get().getRating(), 0.01);
    }
    
    @Test
    public void testUpdateVendorRating_InvalidRating() {
        Vendor vendor = createValidVendor("V001");
        vendorService.addVendor(vendor);
        
        try {
            vendorService.updateVendorRating("V001", 6.0);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
    
    @Test
    public void testActivateVendor() {
        Vendor vendor = createValidVendor("V001");
        vendor.setActive(false);
        vendorService.addVendor(vendor);
        
        assertTrue(vendorService.activateVendor("V001"));
        assertTrue(vendorService.getVendor("V001").get().isActive());
    }
    
    @Test
    public void testDeactivateVendor() {
        Vendor vendor = createValidVendor("V001");
        vendorService.addVendor(vendor);
        
        assertTrue(vendorService.deactivateVendor("V001"));
        assertFalse(vendorService.getVendor("V001").get().isActive());
    }
    
    @Test
    public void testGetVendorCount() {
        assertEquals(0, vendorService.getVendorCount());
        
        vendorService.addVendor(createValidVendor("V001"));
        vendorService.addVendor(createValidVendor("V002"));
        
        assertEquals(2, vendorService.getVendorCount());
    }
    
    @Test
    public void testIsValidVendor() {
        Vendor validVendor = createValidVendor("V001");
        Vendor invalidVendor1 = new Vendor(); // Missing fields
        Vendor invalidVendor2 = createValidVendor("V001");
        invalidVendor2.setEmail("invalid-email"); // Invalid email
        
        assertTrue(vendorService.isValidVendor(validVendor));
        assertFalse(vendorService.isValidVendor(invalidVendor1));
        assertFalse(vendorService.isValidVendor(invalidVendor2));
    }
    
    @Test
    public void testValidateVendorId() {
        assertTrue(vendorService.validateVendorId("V001"));
        assertTrue(vendorService.validateVendorId("V999"));
        assertFalse(vendorService.validateVendorId("ABC"));
        assertFalse(vendorService.validateVendorId("V0001"));
        assertFalse(vendorService.validateVendorId(null));
        assertFalse(vendorService.validateVendorId(""));
    }
    
    @Test
    public void testValidateEmail() {
        assertTrue(vendorService.validateEmail("test@example.com"));
        assertTrue(vendorService.validateEmail("user.name@domain.co.in"));
        assertFalse(vendorService.validateEmail("invalid-email"));
        assertFalse(vendorService.validateEmail("@domain.com"));
        assertFalse(vendorService.validateEmail(null));
    }
    
    @Test
    public void testValidatePhone() {
        assertTrue(vendorService.validatePhone("9876543210"));
        assertFalse(vendorService.validatePhone("12345"));
        assertFalse(vendorService.validatePhone("abcdefghij"));
        assertFalse(vendorService.validatePhone(null));
        assertFalse(vendorService.validatePhone(""));
    }
    
    private Vendor createValidVendor(String vendorId) {
        return new Vendor(vendorId, "Test Vendor", "Test Contact", 
                         "test@test.com", "9876543210", "Test Address, Test City");
    }
}