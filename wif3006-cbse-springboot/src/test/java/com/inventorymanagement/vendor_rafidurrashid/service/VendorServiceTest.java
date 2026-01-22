package com.inventorymanagement.vendor_rafidurrashid.service;

import com.inventorymanagement.vendor_rafidurrashid.service.VendorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.inventorymanagement.vendor_rafidurrashid.model.Vendor;
import com.inventorymanagement.vendor_rafidurrashid.model.VendorContact;
import com.inventorymanagement.vendor_rafidurrashid.repository.VendorCategoryRepository;
import com.inventorymanagement.vendor_rafidurrashid.repository.VendorContactRepository;
import com.inventorymanagement.vendor_rafidurrashid.repository.VendorGroupRepository;
import com.inventorymanagement.vendor_rafidurrashid.repository.VendorRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VendorServiceTest {
    
    @Mock
    private VendorRepository vendorRepository;
    
    @Mock
    private VendorGroupRepository vendorGroupRepository;
    
    @Mock
    private VendorCategoryRepository vendorCategoryRepository;
    
    @Mock
    private VendorContactRepository vendorContactRepository;
    
    @InjectMocks
    private VendorService vendorService;
    
    private Vendor testVendor;
    
    @BeforeEach
    public void setup() {
        testVendor = new Vendor();
        testVendor.setId("vendor-001");
        testVendor.setName("ABC Supplies");
        testVendor.setEmail("contact@abc.com");
        testVendor.setPhone("0123456789");
        testVendor.setStatus("ACTIVE");
    }
    
    @Test
    public void testGetAllVendors() {
        // Arrange
        Vendor anotherVendor = new Vendor();
        anotherVendor.setId("vendor-002");
        anotherVendor.setName("XYZ Corp");
        
        List<Vendor> vendors = Arrays.asList(testVendor, anotherVendor);
        when(vendorRepository.findAll()).thenReturn(vendors);
        
        // Act
        List<Vendor> result = vendorService.getAllVendors();
        
        // Assert
        assertEquals(2, result.size());
        verify(vendorRepository, times(1)).findAll();
    }
    
    @Test
    public void testGetVendorById_Found() {
        // Arrange
        when(vendorRepository.findById("vendor-001")).thenReturn(Optional.of(testVendor));
        
        // Act
        Optional<Vendor> result = vendorService.getVendorById("vendor-001");
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals("ABC Supplies", result.get().getName());
    }
    
    @Test
    public void testGetVendorById_NotFound() {
        // Arrange
        when(vendorRepository.findById("nonexistent")).thenReturn(Optional.empty());
        
        // Act
        Optional<Vendor> result = vendorService.getVendorById("nonexistent");
        
        // Assert
        assertFalse(result.isPresent());
    }
    
    @Test
    public void testCreateVendor() {
        // Arrange
        Vendor newVendor = new Vendor();
        newVendor.setName("New Vendor");
        newVendor.setEmail("new@vendor.com");
        newVendor.setPhone("9876543210");
        newVendor.setStatus("ACTIVE");
        
        Vendor savedVendor = new Vendor();
        savedVendor.setId("vendor-002");
        savedVendor.setName("New Vendor");
        savedVendor.setEmail("new@vendor.com");
        savedVendor.setPhone("9876543210");
        savedVendor.setStatus("ACTIVE");
        
        when(vendorRepository.save(any(Vendor.class))).thenReturn(savedVendor);
        
        // Act
        Vendor result = vendorService.createVendor(newVendor);
        
        // Assert
        assertNotNull(result);
        assertEquals("vendor-002", result.getId());
        assertEquals("New Vendor", result.getName());
        verify(vendorRepository, times(1)).save(any(Vendor.class));
    }
    
    @Test
    public void testUpdateVendor() {
        // Arrange
        Vendor updateData = new Vendor();
        updateData.setName("Updated Name");
        updateData.setEmail("updated@email.com");
        
        when(vendorRepository.findById("vendor-001")).thenReturn(Optional.of(testVendor));
        
        Vendor updatedVendor = new Vendor();
        updatedVendor.setId("vendor-001");
        updatedVendor.setName("Updated Name");
        updatedVendor.setEmail("updated@email.com");
        updatedVendor.setPhone("0123456789");
        updatedVendor.setStatus("ACTIVE");
        
        when(vendorRepository.save(any(Vendor.class))).thenReturn(updatedVendor);
        
        // Act
        Vendor result = vendorService.updateVendor("vendor-001", updateData);
        
        // Assert
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("updated@email.com", result.getEmail());
        verify(vendorRepository, times(1)).save(any(Vendor.class));
    }
    
    @Test
    public void testDeleteVendor() {
        // Arrange
        VendorContact contact1 = new VendorContact();
        contact1.setId("contact-001");
        contact1.setVendorId("vendor-001");
        
        List<VendorContact> contacts = Arrays.asList(contact1);
        when(vendorContactRepository.findByVendorId("vendor-001")).thenReturn(contacts);
        
        // Act
        vendorService.deleteVendor("vendor-001");
        
        // Assert
        verify(vendorContactRepository, times(1)).deleteAll(contacts);
        verify(vendorRepository, times(1)).deleteById("vendor-001");
    }
    
    @Test
    public void testDeleteVendor_NoContacts() {
        // Arrange
        when(vendorContactRepository.findByVendorId("vendor-001")).thenReturn(Collections.emptyList());
        
        // Act
        vendorService.deleteVendor("vendor-001");
        
        // Assert
        verify(vendorContactRepository, never()).deleteAll(any());
        verify(vendorRepository, times(1)).deleteById("vendor-001");
    }
}