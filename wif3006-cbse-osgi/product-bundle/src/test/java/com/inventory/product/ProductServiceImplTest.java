package com.inventory.product;

import com.inventory.api.product.model.*;
import com.mongodb.client.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    // --- 1. Mock MongoDB Collections ---
    @Mock private MongoCollection<Document> productCollection;
    @Mock private MongoCollection<Document> productGroupCollection;
    @Mock private MongoCollection<Document> unitMeasureCollection;
    @Mock private MongoCollection<Document> warehouseCollection;
    @Mock private MongoCollection<Document> stockCountCollection;

    // --- 2. Mock MongoDB Helpers ---
    @Mock private FindIterable<Document> findIterable;
    @Mock private MongoCursor<Document> cursor;
    @Mock private DeleteResult deleteResult;
    @Mock private UpdateResult updateResult;

    // --- 3. Inject into Service ---
    @InjectMocks
    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() throws Exception {
        // Reflection is required because fields are private in OSGi Impl
        setField(productService, "productCollection", productCollection);
        setField(productService, "productGroupCollection", productGroupCollection);
        setField(productService, "unitMeasureCollection", unitMeasureCollection);
        setField(productService, "warehouseCollection", warehouseCollection);
        setField(productService, "stockCountCollection", stockCountCollection);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // ================= PRODUCT TESTS =================

    @Test
    void testAddProduct_ShouldInsertDocument() {
        Product product = new Product("101", "Apple", 2.50, "g1", "u1");
        productService.addProduct(product);
        verify(productCollection, times(1)).insertOne(any(Document.class));
    }

    @Test
    void testGetAllProducts_ShouldReturnList() {
        when(productCollection.find()).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, false);
        when(cursor.next()).thenReturn(new Document("id", "101").append("name", "Apple").append("price", 2.50));

        List<Product> list = productService.getAllProducts();
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("Apple", list.get(0).getName());
    }

    @Test
    void testGetProduct_ShouldReturnObject() {
        when(productCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(new Document("id", "101").append("name", "Banana"));

        Product p = productService.getProduct("101");
        assertNotNull(p);
        assertEquals("Banana", p.getName());
    }

    @Test
    void testUpdateProduct_ShouldUpdateOne() {
        Product product = new Product("101", "Updated Apple", 3.00, "g1", "u1");
        productService.updateProduct(product);
        verify(productCollection, times(1)).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    void testDeleteProduct_ShouldDeleteOne() {
        productService.deleteProduct("101");
        verify(productCollection, times(1)).deleteOne(any(Bson.class));
    }

    // ================= GROUP TESTS =================

    @Test
    void testAddProductGroup_ShouldInsert() {
        ProductGroup pg = new ProductGroup("g1", "Fruits", "Fresh");
        productService.addProductGroup(pg);
        verify(productGroupCollection, times(1)).insertOne(any(Document.class));
    }

    @Test
    void testDeleteProductGroup_ShouldDelete() {
        productService.deleteProductGroup("g1");
        verify(productGroupCollection, times(1)).deleteOne(any(Bson.class));
    }

    // ================= UOM TESTS =================

    @Test
    void testAddUnitMeasure_ShouldInsert() {
        UnitMeasure uom = new UnitMeasure("u1", "Kilogram", "kg");
        productService.addUnitMeasure(uom);
        verify(unitMeasureCollection, times(1)).insertOne(any(Document.class));
    }

    @Test
    void testGetAllUnitMeasures_ShouldReturnList() {
        when(unitMeasureCollection.find()).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(false);

        List<UnitMeasure> list = productService.getAllUnitMeasures();
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    // ================= WAREHOUSE TESTS =================

    @Test
    void testAddWarehouse_ShouldInsert() {
        Warehouse wh = new Warehouse("KL Sentral", false, "Main Hub");
        productService.addWarehouse(wh);
        verify(warehouseCollection, times(1)).insertOne(any(Document.class));
    }

    @Test
    void testDeleteWarehouse_ShouldDelete() {
        productService.deleteWarehouse("KL Sentral");
        verify(warehouseCollection, times(1)).deleteOne(any(Bson.class));
    }

    // ================= STOCK COUNT TESTS =================

    @Test
    void testAddStockCount_ShouldInsert() {
        StockCount sc = new StockCount("SC-001", "KL Sentral", "Pending", "2024-01-01");
        productService.addStockCount(sc);
        verify(stockCountCollection, times(1)).insertOne(any(Document.class));
    }

    @Test
    void testGetAllStockCounts_ShouldReturnList() {
        when(stockCountCollection.find()).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, false);
        when(cursor.next()).thenReturn(new Document("id", "SC-001").append("status", "Pending"));

        List<StockCount> list = productService.getAllStockCounts();
        assertEquals(1, list.size());
        assertEquals("SC-001", list.get(0).getCountId());
    }

    @Test
    void testCompleteStockCount_Success() {
        // Mock the DB returning "1 row updated"
        when(stockCountCollection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(updateResult);
        when(updateResult.getMatchedCount()).thenReturn(1L);

        String result = productService.completeStockCount("SC-001");

        // Assert success message
        assertTrue(result.contains("marked as Completed"));
        verify(stockCountCollection, times(1)).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    void testCompleteStockCount_NotFound() {
        // Mock the DB returning "0 rows updated" (ID not found)
        when(stockCountCollection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(updateResult);
        when(updateResult.getMatchedCount()).thenReturn(0L);

        String result = productService.completeStockCount("SC-999");

        // Assert error message
        assertTrue(result.contains("Error: Stock Count ID not found"));
    }
}