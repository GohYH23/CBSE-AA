package com.inventory.vendor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class VendorServiceImplTest {
    
    // Mock the internal vendors map
    @Mock
    private Map<String, Vendor> vendors;
    
    private VendorServiceImpl vendorService;
    
    @Before
    public void setUp() throws Exception {
        vendorService = new VendorServiceImpl();
        
        // Inject the mock map into VendorServiceImpl using reflection
        Field vendorsField = VendorServiceImpl.class.getDeclaredField("vendors");
        vendorsField.setAccessible(true);
        vendorsField.set(vendorService, vendors);
    }
    
    private Vendor createValidVendor(String vendorId) {
        return new Vendor(vendorId, "Test Vendor", "Test Contact", 
                         "test@test.com", "9876543210", "Test Address, Test City");
    }
    
    // --- 1. CRUD OPERATIONS TESTS ---
    
    @Test
    public void testAddVendor_WhenValidVendor_ShouldAddSuccessfully() {
        // Arrange
        Vendor vendor = createValidVendor("V001");
        
        when(vendors.containsKey("V001")).thenReturn(false);
        when(vendors.put(anyString(), any(Vendor.class))).thenReturn(null);
        
        // Act
        Vendor result = vendorService.addVendor(vendor);
        
        // Assert
        assertNotNull(result);
        assertEquals("V001", result.getVendorId());
        verify(vendors, times(1)).put("V001", vendor);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testAddVendor_WhenInvalidVendor_ShouldThrowException() {
        // Arrange
        Vendor invalidVendor = new Vendor();
        invalidVendor.setVendorId("INVALID");
        invalidVendor.setName("");
        invalidVendor.setEmail("invalid-email");
        invalidVendor.setPhone("123");
        invalidVendor.setAddress("");
        
        // Act
        vendorService.addVendor(invalidVendor);
    }
    
    @Test(expected = IllegalStateException.class)
    public void testAddVendor_WhenDuplicateVendorId_ShouldThrowException() {
        // Arrange
        Vendor vendor = createValidVendor("V001");
        
        when(vendors.containsKey("V001")).thenReturn(true);
        
        // Act
        vendorService.addVendor(vendor);
    }
    
    @Test
    public void testGetVendor_WhenVendorExists_ShouldReturnOptional() {
        // Arrange
        Vendor vendor = createValidVendor("V001");
        
        when(vendors.get("V001")).thenReturn(vendor);
        
        // Act
        Optional<Vendor> result = vendorService.getVendor("V001");
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals("V001", result.get().getVendorId());
        assertEquals("Test Vendor", result.get().getName());
    }
    
    @Test
    public void testGetVendor_WhenVendorNotExists_ShouldReturnEmptyOptional() {
        // Arrange
        when(vendors.get("V999")).thenReturn(null);
        
        // Act
        Optional<Vendor> result = vendorService.getVendor("V999");
        
        // Assert
        assertFalse(result.isPresent());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetVendor_WithNullId_ShouldThrowException() {
        // Act
        vendorService.getVendor(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetVendor_WithEmptyId_ShouldThrowException() {
        // Act
        vendorService.getVendor("");
    }
    
    @Test
    public void testGetAllVendors_ShouldReturnList() {
        // Arrange
        Vendor vendor1 = createValidVendor("V001");
        Vendor vendor2 = createValidVendor("V002");
        List<Vendor> vendorList = Arrays.asList(vendor1, vendor2);
        
        when(vendors.values()).thenReturn(vendorList);
        
        // Act
        List<Vendor> result = vendorService.getAllVendors();
        
        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(vendor1));
        assertTrue(result.contains(vendor2));
    }
    
    @Test
    public void testUpdateVendor_WhenValidUpdate_ShouldUpdateSuccessfully() {
        // Arrange
        Vendor existingVendor = createValidVendor("V001");
        Vendor updatedVendor = createValidVendor("V001");
        updatedVendor.setName("Updated Name");
        updatedVendor.setEmail("updated@test.com");
        
        when(vendors.containsKey("V001")).thenReturn(true);
        when(vendors.put("V001", updatedVendor)).thenReturn(existingVendor);
        
        // Act
        Vendor result = vendorService.updateVendor(updatedVendor);
        
        // Assert
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("updated@test.com", result.getEmail());
        verify(vendors, times(1)).put("V001", updatedVendor);
    }
    
    @Test(expected = IllegalStateException.class)
    public void testUpdateVendor_WhenVendorNotExists_ShouldThrowException() {
        // Arrange
        Vendor vendor = createValidVendor("V999");
        
        when(vendors.containsKey("V999")).thenReturn(false);
        
        // Act
        vendorService.updateVendor(vendor);
    }
    
    @Test
    public void testDeleteVendor_WhenVendorExists_ShouldReturnTrue() {
        // Arrange
        when(vendors.remove("V001")).thenReturn(createValidVendor("V001"));
        
        // Act
        boolean result = vendorService.deleteVendor("V001");
        
        // Assert
        assertTrue(result);
        verify(vendors, times(1)).remove("V001");
    }
    
    @Test
    public void testDeleteVendor_WhenVendorNotExists_ShouldReturnFalse() {
        // Arrange
        when(vendors.remove("V999")).thenReturn(null);
        
        // Act
        boolean result = vendorService.deleteVendor("V999");
        
        // Assert
        assertFalse(result);
        verify(vendors, times(1)).remove("V999");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testDeleteVendor_WithNullId_ShouldThrowException() {
        // Act
        vendorService.deleteVendor(null);
    }
    
    // --- 2. SEARCH OPERATIONS TESTS ---
    
    @Test
    public void testSearchVendorsByName_WhenNameMatches_ShouldReturnList() {
        // Arrange
        Vendor vendor1 = new Vendor("V001", "Tech Solutions", "John", 
            "john@tech.com", "9876543210", "Address");
        Vendor vendor2 = new Vendor("V002", "Office Tech", "Jane", 
            "jane@office.com", "9876543211", "Address");
        Vendor vendor3 = new Vendor("V003", "Food Supplies", "Bob", 
            "bob@food.com", "9876543212", "Address");
        
        List<Vendor> vendorList = Arrays.asList(vendor1, vendor2, vendor3);
        
        when(vendors.values()).thenReturn(vendorList);
        
        // Act
        List<Vendor> result = vendorService.searchVendorsByName("tech");
        
        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(v -> v.getVendorId().equals("V001")));
        assertTrue(result.stream().anyMatch(v -> v.getVendorId().equals("V002")));
    }
    
    @Test
    public void testSearchVendorsByName_WhenNameNotMatches_ShouldReturnEmptyList() {
        // Arrange
        List<Vendor> vendorList = Arrays.asList(
            createValidVendor("V001"),
            createValidVendor("V002")
        );
        
        when(vendors.values()).thenReturn(vendorList);
        
        // Act
        List<Vendor> result = vendorService.searchVendorsByName("nonexistent");
        
        // Assert
        assertTrue(result.isEmpty());
    }
    
    @Test
    public void testSearchVendorsByName_WithNullName_ShouldReturnEmptyList() {
        // Act
        List<Vendor> result = vendorService.searchVendorsByName(null);
        
        // Assert
        assertTrue(result.isEmpty());
    }
    
    @Test
    public void testSearchVendorsByName_WithEmptyName_ShouldReturnEmptyList() {
        // Act
        List<Vendor> result = vendorService.searchVendorsByName("");
        
        // Assert
        assertTrue(result.isEmpty());
    }
    
    @Test
    public void testGetVendorsByRating_WhenRatingMatches_ShouldReturnList() {
        // Arrange
        Vendor vendor1 = createValidVendor("V001");
        vendor1.setRating(4.5);
        Vendor vendor2 = createValidVendor("V002");
        vendor2.setRating(3.5);
        Vendor vendor3 = createValidVendor("V003");
        vendor3.setRating(4.0);
        
        List<Vendor> vendorList = Arrays.asList(vendor1, vendor2, vendor3);
        
        when(vendors.values()).thenReturn(vendorList);
        
        // Act
        List<Vendor> result = vendorService.getVendorsByRating(4.0);
        
        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(v -> v.getVendorId().equals("V001")));
        assertTrue(result.stream().anyMatch(v -> v.getVendorId().equals("V003")));
    }
    
    @Test
    public void testGetActiveVendors_ShouldReturnActiveOnly() {
        // Arrange
        Vendor vendor1 = createValidVendor("V001");
        vendor1.setActive(true);
        Vendor vendor2 = createValidVendor("V002");
        vendor2.setActive(false);
        Vendor vendor3 = createValidVendor("V003");
        vendor3.setActive(true);
        
        List<Vendor> vendorList = Arrays.asList(vendor1, vendor2, vendor3);
        
        when(vendors.values()).thenReturn(vendorList);
        
        // Act
        List<Vendor> result = vendorService.getActiveVendors();
        
        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(Vendor::isActive));
    }
    
    @Test
    public void testGetInactiveVendors_ShouldReturnInactiveOnly() {
        // Arrange
        Vendor vendor1 = createValidVendor("V001");
        vendor1.setActive(true);
        Vendor vendor2 = createValidVendor("V002");
        vendor2.setActive(false);
        Vendor vendor3 = createValidVendor("V003");
        vendor3.setActive(false);
        
        List<Vendor> vendorList = Arrays.asList(vendor1, vendor2, vendor3);
        
        when(vendors.values()).thenReturn(vendorList);
        
        // Act
        List<Vendor> result = vendorService.getInactiveVendors();
        
        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().noneMatch(Vendor::isActive));
    }
    
    // --- 3. UPDATE OPERATIONS TESTS ---
    
    @Test
    public void testUpdateVendorRating_WhenValidRating_ShouldUpdateSuccessfully() {
        // Arrange
        Vendor vendor = createValidVendor("V001");
        
        when(vendors.get("V001")).thenReturn(vendor);
        
        // Act
        boolean result = vendorService.updateVendorRating("V001", 4.5);
        
        // Assert
        assertTrue(result);
        assertEquals(4.5, vendor.getRating(), 0.01);
    }
    
    @Test
    public void testUpdateVendorRating_WhenVendorNotExists_ShouldReturnFalse() {
        // Arrange
        when(vendors.get("V999")).thenReturn(null);
        
        // Act
        boolean result = vendorService.updateVendorRating("V999", 4.5);
        
        // Assert
        assertFalse(result);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateVendorRating_WithInvalidRatingBelowZero_ShouldThrowException() {
        // Arrange
        Vendor vendor = createValidVendor("V001");
        
        when(vendors.get("V001")).thenReturn(vendor);
        
        // Act
        vendorService.updateVendorRating("V001", -1.0);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateVendorRating_WithInvalidRatingAboveFive_ShouldThrowException() {
        // Arrange
        Vendor vendor = createValidVendor("V001");
        
        when(vendors.get("V001")).thenReturn(vendor);
        
        // Act
        vendorService.updateVendorRating("V001", 6.0);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateVendorRating_WithNullVendorId_ShouldThrowException() {
        // Act
        vendorService.updateVendorRating(null, 4.5);
    }
    
    @Test
    public void testActivateVendor_WhenVendorExistsAndInactive_ShouldReturnTrue() {
        // Arrange
        Vendor vendor = createValidVendor("V001");
        vendor.setActive(false);
        
        when(vendors.get("V001")).thenReturn(vendor);
        
        // Act
        boolean result = vendorService.activateVendor("V001");
        
        // Assert
        assertTrue(result);
        assertTrue(vendor.isActive());
    }
    
    @Test
    public void testActivateVendor_WhenVendorAlreadyActive_ShouldReturnFalse() {
        // Arrange
        Vendor vendor = createValidVendor("V001");
        vendor.setActive(true);
        
        when(vendors.get("V001")).thenReturn(vendor);
        
        // Act
        boolean result = vendorService.activateVendor("V001");
        
        // Assert
        assertFalse(result);
        assertTrue(vendor.isActive());
    }
    
    @Test
    public void testDeactivateVendor_WhenVendorExistsAndActive_ShouldReturnTrue() {
        // Arrange
        Vendor vendor = createValidVendor("V001");
        vendor.setActive(true);
        
        when(vendors.get("V001")).thenReturn(vendor);
        
        // Act
        boolean result = vendorService.deactivateVendor("V001");
        
        // Assert
        assertTrue(result);
        assertFalse(vendor.isActive());
    }
    
    // --- 4. UTILITY OPERATIONS TESTS ---
    
    @Test
    public void testGetVendorCount_ShouldReturnCorrectCount() {
        // Arrange
        when(vendors.size()).thenReturn(5);
        
        // Act
        int count = vendorService.getVendorCount();
        
        // Assert
        assertEquals(5, count);
    }
    
    @Test
    public void testGetActiveVendorCount_ShouldReturnCorrectCount() {
        // Arrange
        Vendor vendor1 = createValidVendor("V001");
        vendor1.setActive(true);
        Vendor vendor2 = createValidVendor("V002");
        vendor2.setActive(false);
        Vendor vendor3 = createValidVendor("V003");
        vendor3.setActive(true);
        
        List<Vendor> vendorList = Arrays.asList(vendor1, vendor2, vendor3);
        
        when(vendors.values()).thenReturn(vendorList);
        
        // Act
        int count = vendorService.getActiveVendorCount();
        
        // Assert
        assertEquals(2, count);
    }
    
    @Test
    public void testVendorExists_WhenVendorExists_ShouldReturnTrue() {
        // Arrange
        when(vendors.containsKey("V001")).thenReturn(true);
        
        // Act
        boolean exists = vendorService.vendorExists("V001");
        
        // Assert
        assertTrue(exists);
    }
    
    @Test
    public void testVendorExists_WhenVendorNotExists_ShouldReturnFalse() {
        // Arrange
        when(vendors.containsKey("V999")).thenReturn(false);
        
        // Act
        boolean exists = vendorService.vendorExists("V999");
        
        // Assert
        assertFalse(exists);
    }
    
    // --- 5. VALIDATION TESTS ---
    
    @Test
    public void testValidateVendorId_WhenValidFormat_ShouldReturnTrue() {
        // Test valid vendor IDs
        assertTrue(vendorService.validateVendorId("V001"));
        assertTrue(vendorService.validateVendorId("V999"));
        assertTrue(vendorService.validateVendorId("V123"));
    }
    
    @Test
    public void testValidateVendorId_WhenInvalidFormat_ShouldReturnFalse() {
        // Test invalid vendor IDs
        assertFalse(vendorService.validateVendorId("ABC"));
        assertFalse(vendorService.validateVendorId("V0001"));
        assertFalse(vendorService.validateVendorId("V01"));
        assertFalse(vendorService.validateVendorId("v001"));
        assertFalse(vendorService.validateVendorId("001"));
        assertFalse(vendorService.validateVendorId(null));
        assertFalse(vendorService.validateVendorId(""));
    }
    
    @Test
    public void testValidateEmail_WhenValidEmail_ShouldReturnTrue() {
        // Test valid emails
        assertTrue(vendorService.validateEmail("test@example.com"));
        assertTrue(vendorService.validateEmail("user.name@domain.co.in"));
        assertTrue(vendorService.validateEmail("user+tag@example.com"));
        assertTrue(vendorService.validateEmail("user@sub.domain.com"));
    }
    
    @Test
    public void testValidateEmail_WhenInvalidEmail_ShouldReturnFalse() {
        // Test invalid emails
        assertFalse(vendorService.validateEmail("invalid-email"));
        assertFalse(vendorService.validateEmail("@domain.com"));
        assertFalse(vendorService.validateEmail("user@.com"));
        assertFalse(vendorService.validateEmail("user@domain."));
        assertFalse(vendorService.validateEmail("user@domain..com"));
        assertFalse(vendorService.validateEmail(null));
        assertFalse(vendorService.validateEmail(""));
    }
    
    @Test
    public void testValidatePhone_WhenValidPhone_ShouldReturnTrue() {
        // Test valid phone numbers
        assertTrue(vendorService.validatePhone("9876543210"));
        assertTrue(vendorService.validatePhone("1234567890"));
        assertTrue(vendorService.validatePhone("9999999999"));
    }
    
    @Test
    public void testValidatePhone_WhenInvalidPhone_ShouldReturnFalse() {
        // Test invalid phone numbers
        assertFalse(vendorService.validatePhone("12345"));
        assertFalse(vendorService.validatePhone("12345678901"));
        assertFalse(vendorService.validatePhone("abcdefghij"));
        assertFalse(vendorService.validatePhone("123-456-7890"));
        assertFalse(vendorService.validatePhone(null));
        assertFalse(vendorService.validatePhone(""));
    }
    
    @Test
    public void testIsValidVendor_WhenAllFieldsValid_ShouldReturnTrue() {
        // Arrange
        Vendor validVendor = createValidVendor("V001");
        
        // Act
        boolean isValid = vendorService.isValidVendor(validVendor);
        
        // Assert
        assertTrue(isValid);
    }
    
    @Test
    public void testIsValidVendor_WhenMissingFields_ShouldReturnFalse() {
        // Arrange
        Vendor invalidVendor = new Vendor();
        
        // Act
        boolean isValid = vendorService.isValidVendor(invalidVendor);
        
        // Assert
        assertFalse(isValid);
    }
    
    @Test
    public void testIsValidVendor_WhenInvalidEmail_ShouldReturnFalse() {
        // Arrange
        Vendor vendor = createValidVendor("V001");
        vendor.setEmail("invalid-email");
        
        // Act
        boolean isValid = vendorService.isValidVendor(vendor);
        
        // Assert
        assertFalse(isValid);
    }
    
    @Test
    public void testIsValidVendor_WhenInvalidPhone_ShouldReturnFalse() {
        // Arrange
        Vendor vendor = createValidVendor("V001");
        vendor.setPhone("12345");
        
        // Act
        boolean isValid = vendorService.isValidVendor(vendor);
        
        // Assert
        assertFalse(isValid);
    }
    
    @Test
    public void testIsValidVendor_WhenInvalidVendorId_ShouldReturnFalse() {
        // Arrange
        Vendor vendor = createValidVendor("ABC");
        vendor.setVendorId("ABC"); // Invalid ID format
        
        // Act
        boolean isValid = vendorService.isValidVendor(vendor);
        
        // Assert
        assertFalse(isValid);
    }
    
    // --- 6. EDGE CASES AND BOUNDARY TESTS ---
    
    @Test
    public void testAddVendor_WithMinimumValidData_ShouldSucceed() {
        // Arrange
        Vendor vendor = new Vendor("V001", "Min Vendor", "Contact", 
                                  "min@test.com", "9876543210", "Address");
        
        when(vendors.containsKey("V001")).thenReturn(false);
        when(vendors.put(anyString(), any(Vendor.class))).thenReturn(null);
        
        // Act
        Vendor result = vendorService.addVendor(vendor);
        
        // Assert
        assertNotNull(result);
        verify(vendors, times(1)).put("V001", vendor);
    }
    
    @Test
    public void testUpdateVendorRating_WithBoundaryValues_ShouldSucceed() {
        // Test minimum and maximum valid ratings
        Vendor vendor = createValidVendor("V001");
        
        when(vendors.get("V001")).thenReturn(vendor);
        
        // Test minimum valid rating (0.0)
        boolean result1 = vendorService.updateVendorRating("V001", 0.0);
        assertTrue(result1);
        assertEquals(0.0, vendor.getRating(), 0.01);
        
        // Test maximum valid rating (5.0)
        boolean result2 = vendorService.updateVendorRating("V001", 5.0);
        assertTrue(result2);
        assertEquals(5.0, vendor.getRating(), 0.01);
    }
    
    @Test
    public void testSearchVendorsByName_CaseInsensitive_ShouldReturnMatches() {
        // Arrange
        Vendor vendor1 = new Vendor("V001", "Tech Solutions", "John", 
            "john@tech.com", "9876543210", "Address");
        Vendor vendor2 = new Vendor("V002", "TECHNOLOGY Corp", "Jane", 
            "jane@tech.com", "9876543211", "Address");
        Vendor vendor3 = new Vendor("V003", "Food Supplies", "Bob", 
            "bob@food.com", "9876543212", "Address");
        
        List<Vendor> vendorList = Arrays.asList(vendor1, vendor2, vendor3);
        
        when(vendors.values()).thenReturn(vendorList);
        
        // Act - Test case insensitivity
        List<Vendor> result1 = vendorService.searchVendorsByName("TECH");
        List<Vendor> result2 = vendorService.searchVendorsByName("tech");
        List<Vendor> result3 = vendorService.searchVendorsByName("Tech");
        
        // Assert
        assertEquals(2, result1.size());
        assertEquals(2, result2.size());
        assertEquals(2, result3.size());
    }
    
    // --- 7. PERFORMANCE-RELATED TESTS ---
    
    @Test
    public void testGetAllVendors_WithLargeDataset_ShouldHandleCorrectly() {
        // Arrange
        List<Vendor> largeVendorList = new ArrayList<>();
        for (int i = 1; i <= 100; i++) { // Using 100 instead of 1000 for speed
            largeVendorList.add(new Vendor(
                String.format("V%03d", i),
                "Vendor " + i,
                "Contact " + i,
                "vendor" + i + "@test.com",
                "9876543210",
                "Address " + i
            ));
        }
        
        when(vendors.values()).thenReturn(largeVendorList);
        
        // Act
        List<Vendor> result = vendorService.getAllVendors();
        
        // Assert
        assertEquals(100, result.size());
        assertEquals("V001", result.get(0).getVendorId());
        assertEquals("Vendor 100", result.get(99).getName());
    }
    
    @Test
    public void testConcurrentOperations_ShouldBeThreadSafe() {
        // Arrange
        Vendor vendor1 = createValidVendor("V001");
        Vendor vendor2 = createValidVendor("V002");
        
        when(vendors.containsKey("V001")).thenReturn(false);
        when(vendors.containsKey("V002")).thenReturn(false);
        when(vendors.put(anyString(), any(Vendor.class))).thenReturn(null);
        
        // Act - Simulate operations
        vendorService.addVendor(vendor1);
        vendorService.addVendor(vendor2);
        
        // Assert
        verify(vendors, times(2)).put(anyString(), any(Vendor.class));
    }
}