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
        Product p = new Product("101", "Apple", 2.50, "g1", "u1");

        // Mock the duplicate check to return FALSE (no duplicate)
        when(productRepo.existsByNameIgnoreCase("Apple")).thenReturn(false);
        when(productRepo.save(any(Product.class))).thenReturn(p);

        Product result = productService.addProduct(p);

        assertNotNull(result);
        assertEquals("Apple", result.getName());
        verify(productRepo, times(1)).save(p);
    }

    @Test
    void testAddProduct_DuplicateName_ShouldThrowException() {
        Product p = new Product("102", "Banana", 2.00, "g1", "u1");

        // Mock the duplicate check to return TRUE (duplicate exists)
        when(productRepo.existsByNameIgnoreCase("Banana")).thenReturn(true);

        // Expect an Exception
        Exception exception = assertThrows(RuntimeException.class, () -> {
            productService.addProduct(p);
        });

        assertTrue(exception.getMessage().contains("already exists"));
        verify(productRepo, never()).save(any());
    }

    @Test
    void testGetProductById_ShouldReturnOptional() {
        Product p = new Product("101", "Apple", 2.50, "g1", "u1");
        when(productRepo.findById("101")).thenReturn(Optional.of(p));

        Optional<Product> result = productService.getProductById("101");

        assertTrue(result.isPresent());
        assertEquals("Apple", result.get().getName());
    }

    @Test
    void testUpdateProduct_ShouldSaveToRepo() {
        Product existing = new Product("101", "Old Name", 1.0, "g1", "u1");
        Product newDetails = new Product("101", "New Name", 2.0, "g1", "u1");

        when(productRepo.findById("101")).thenReturn(Optional.of(existing));
        when(productRepo.save(any(Product.class))).thenReturn(newDetails);

        Product updated = productService.updateProduct("101", newDetails);

        assertEquals("New Name", updated.getName());
        verify(productRepo, times(1)).save(any(Product.class));
    }

    @Test
    void testDeleteProduct_ShouldCallDeleteById() {
        productService.deleteProduct("101");
        verify(productRepo, times(1)).deleteById("101");
    }

    // ==========================================
    // 2. PRODUCT GROUP TESTS
    // ==========================================

    @Test
    void testAddGroup_ShouldSave() {
        ProductGroup pg = new ProductGroup("g1", "Fruits", "Fresh");
        when(groupRepo.existsByGroupNameIgnoreCase("Fruits")).thenReturn(false);
        when(groupRepo.save(any(ProductGroup.class))).thenReturn(pg);

        productService.addGroup(pg);
        verify(groupRepo, times(1)).save(pg);
    }

    @Test
    void testDeleteGroup_WhenInUse_ShouldThrowException() {
        String groupId = "g1";
        // Simulate that products are using this group
        when(productRepo.findByProductGroupId(groupId)).thenReturn(List.of(new Product()));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            productService.deleteGroup(groupId);
        });

        assertTrue(exception.getMessage().contains("Cannot Delete"));
        verify(groupRepo, never()).deleteById(anyString());
    }

    @Test
    void testDeleteGroup_WhenEmpty_ShouldDelete() {
        String groupId = "g2";
        // Simulate no products use this group
        when(productRepo.findByProductGroupId(groupId)).thenReturn(List.of());

        productService.deleteGroup(groupId);
        verify(groupRepo, times(1)).deleteById(groupId);
    }

    // ==========================================
    // 3. UNIT MEASURE TESTS
    // ==========================================

    @Test
    void testAddUOM_ShouldSave() {
        UnitMeasure uom = new UnitMeasure("u1", "KG", "kg");
        when(uomRepo.existsByUnitNameIgnoreCase("KG")).thenReturn(false);
        when(uomRepo.save(any(UnitMeasure.class))).thenReturn(uom);

        productService.addUOM(uom);
        verify(uomRepo, times(1)).save(uom);
    }

    @Test
    void testDeleteUOM_WhenInUse_ShouldThrowException() {
        String uomId = "u1";
        when(productRepo.findByUomId(uomId)).thenReturn(List.of(new Product()));

        assertThrows(RuntimeException.class, () -> productService.deleteUOM(uomId));
        verify(uomRepo, never()).deleteById(anyString());
    }

    // ==========================================
    // 4. WAREHOUSE TESTS
    // ==========================================

    @Test
    void testAddWarehouse_ShouldSave() {
        Warehouse w = new Warehouse("KL", false, "Main");
        when(warehouseRepo.existsByNameIgnoreCase("KL")).thenReturn(false);
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
    // 5. STOCK COUNT TESTS
    // ==========================================

    @Test
    void testCreateStockCount_ShouldSave() {
        StockCount sc = new StockCount("SC-1", "KL", "Pending", "2024");
        when(stockRepo.save(any(StockCount.class))).thenReturn(sc);

        productService.createStockCount(sc);
        verify(stockRepo, times(1)).save(sc);
    }

    @Test
    void testGetAllStockCounts_ShouldReturnList() {
        when(stockRepo.findAll()).thenReturn(List.of(new StockCount()));
        List<StockCount> list = productService.getAllStockCounts();
        assertEquals(1, list.size());
        verify(stockRepo, times(1)).findAll();
    }
}