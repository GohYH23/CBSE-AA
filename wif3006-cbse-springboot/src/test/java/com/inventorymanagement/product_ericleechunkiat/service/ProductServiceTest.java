/*
 * Product Module testing done by ERICLEECHUNKIAT
 * This test covers all functionalities for
 * Products, Groups, UOM, Warehouses, and Stock Counts.
 */

package com.inventorymanagement.product_ericleechunkiat.service;

import com.inventorymanagement.product_ericleechunkiat.model.*;
import com.inventorymanagement.product_ericleechunkiat.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    // --- MOCK REPOSITORIES ---
    @Mock private ProductRepository productRepo;
    @Mock private ProductGroupRepository groupRepo;
    @Mock private UnitMeasureRepository uomRepo;
    @Mock private WarehouseRepository warehouseRepo;
    @Mock private StockCountRepository stockRepo;

    // --- INJECT SERVICE ---
    @InjectMocks
    private ProductService productService;

    // ==========================================
    // 1. PRODUCT TESTS
    // ==========================================

    @Test
    void testAddProduct_ShouldSaveAndReturnProduct() {
        Product p = new Product();
        p.setName("Apple");

        when(productRepo.existsByNameIgnoreCase("Apple")).thenReturn(false);
        when(productRepo.save(any(Product.class))).thenReturn(p);

        Product result = productService.addProduct(p);

        assertNotNull(result);
        assertEquals("Apple", result.getName());
        verify(productRepo, times(1)).save(p);
    }

    @Test
    void testAddProduct_DuplicateName_ShouldThrowException() {
        Product p = new Product();
        p.setName("Banana");

        when(productRepo.existsByNameIgnoreCase("Banana")).thenReturn(true);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            productService.addProduct(p);
        });

        assertTrue(exception.getMessage().contains("already exists"));
        verify(productRepo, never()).save(any());
    }

    @Test
    void testGetProductById_ShouldReturnOptional() {
        Product p = new Product();
        p.setName("Apple");
        when(productRepo.findById("101")).thenReturn(Optional.of(p));

        Optional<Product> result = productService.getProductById("101");

        assertTrue(result.isPresent());
        assertEquals("Apple", result.get().getName());
    }

    // ==========================================
    // 2. PRODUCT GROUP TESTS (Fixed to match your Service)
    // ==========================================

    @Test
    void testCreateProductGroup_ShouldReturnSuccessMessage() {
        // Mocking the duplicate check
        when(groupRepo.existsByGroupNameIgnoreCase("Fruits")).thenReturn(false);
        // Mocking the auto-increment logic (empty list = ID 1)
        when(groupRepo.findAll()).thenReturn(Collections.emptyList());

        // Use the EXACT method from your Service: createProductGroup(String, String)
        String result = productService.createProductGroup("Fruits", "Fresh");

        assertTrue(result.contains("✅ Group Added"));
        verify(groupRepo, times(1)).save(any(ProductGroup.class));
    }

    @Test
    void testDeleteProductGroup_WhenInUse_ShouldReturnWarning() {
        String groupId = "1";
        // Simulate that products ARE using this group
        when(productRepo.findByProductGroupId(groupId)).thenReturn(List.of(new Product()));

        // Call method directly
        String result = productService.deleteProductGroup(groupId);

        // Expect the specific string return from your Service
        assertEquals("⚠️ Cannot Delete: Group is in use.", result);
        verify(groupRepo, never()).deleteById(anyString());
    }

    @Test
    void testDeleteProductGroup_WhenEmpty_ShouldDelete() {
        String groupId = "2";
        // Simulate NO products using this group
        when(productRepo.findByProductGroupId(groupId)).thenReturn(Collections.emptyList());
        when(groupRepo.existsById(groupId)).thenReturn(true);

        String result = productService.deleteProductGroup(groupId);

        assertEquals("✅ Group Deleted.", result);
        verify(groupRepo, times(1)).deleteById(groupId);
    }

    // ==========================================
    // 3. UNIT MEASURE TESTS
    // ==========================================

    @Test
    void testAddUOM_ShouldSave() {
        UnitMeasure uom = new UnitMeasure();
        uom.setUnitName("KG");
        uom.setId("1");

        when(uomRepo.existsByUnitNameIgnoreCase("KG")).thenReturn(false);
        when(uomRepo.findAll()).thenReturn(Collections.emptyList()); // For auto-increment
        when(uomRepo.save(any(UnitMeasure.class))).thenReturn(uom);

        UnitMeasure result = productService.addUOM(uom);

        assertNotNull(result);
        verify(uomRepo, times(1)).save(uom);
    }

    @Test
    void testDeleteUOM_WhenInUse_ShouldThrowException() {
        String uomId = "u1";
        // Simulate usage
        when(productRepo.findByUomId(uomId)).thenReturn(List.of(new Product()));

        assertThrows(RuntimeException.class, () -> productService.deleteUOM(uomId));
        verify(uomRepo, never()).deleteById(anyString());
    }

    // ==========================================
    // 4. WAREHOUSE TESTS
    // ==========================================

    @Test
    void testAddWarehouse_ShouldSave() {
        Warehouse w = new Warehouse();
        w.setName("KL");

        when(warehouseRepo.save(any(Warehouse.class))).thenReturn(w);

        productService.addWarehouse(w);
        verify(warehouseRepo, times(1)).save(w);
    }

    @Test
    void testDeleteWarehouse_ShouldCallDelete() {
        productService.deleteWarehouse("KL");
        verify(warehouseRepo, times(1)).deleteById("KL");
    }

    // ==========================================
    // 5. STOCK COUNT TESTS (Including Complete Count)
    // ==========================================

    @Test
    void testCreateStockCount_ShouldSave() {
        StockCount sc = new StockCount();
        when(stockRepo.save(any(StockCount.class))).thenReturn(sc);

        productService.createStockCount(sc);
        verify(stockRepo, times(1)).save(sc);
    }

    @Test
    void testCompleteStockCount_ShouldUpdateStatus() {
        // 1. Setup existing pending count
        StockCount sc = new StockCount();
        sc.setCountId("SC-100");
        sc.setStatus("Pending");

        // 2. Mock finding the ID
        when(stockRepo.findById("SC-100")).thenReturn(Optional.of(sc));
        // Mock saving the updated version
        when(stockRepo.save(any(StockCount.class))).thenReturn(sc);

        // 3. Act
        String result = productService.completeStockCount("SC-100");

        // 4. Assert
        assertTrue(result.contains("marked as Completed"));
        assertEquals("Completed", sc.getStatus()); // Verify status changed
        verify(stockRepo, times(1)).save(sc); // Verify save was called
    }
}