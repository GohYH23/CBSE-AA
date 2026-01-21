/*
 * Purchase Order Module testing done by Ooi Wei Ying
 * This test verifies the OSGi implementation for
 * Purchase Orders, including CRUD operations, status transitions,
 * date tracking, and validation rules.
 */
package com.inventory.purchaseorder;

import com.inventory.api.purchaseorder.model.OrderItem;
import com.inventory.api.purchaseorder.model.PurchaseOrder;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PurchaseOrderServiceImplTest {

    @Mock
    private MongoCollection<Document> purchaseOrderCollection;

    @Mock
    private MongoDatabase database;

    @Mock
    private FindIterable<Document> findIterable;

    @Mock
    private MongoCursor<Document> cursor;

    @Mock
    private DeleteResult deleteResult;

    @Mock
    private UpdateResult updateResult;

    @InjectMocks
    private PurchaseOrderServiceImpl purchaseOrderService;

    @BeforeEach
    void setUp() throws Exception {
        // Injecting mocks into private fields since @Activate is skipped in unit tests
        setField(purchaseOrderService, "purchaseOrderCollection", purchaseOrderCollection);
        setField(purchaseOrderService, "database", database);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // Helper method to create a test PurchaseOrder
    private PurchaseOrder createTestPurchaseOrder(int orderId, String status) {
        PurchaseOrder po = new PurchaseOrder();
        po.setOrderId(orderId);
        po.setOrderDate(LocalDate.now().minusDays(1));
        po.setOrderNumber("PO-001");
        po.setVendor("Test Vendor");
        po.setOrderStatus(status);
        
        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem("Item1", 10, 5.50));
        items.add(new OrderItem("Item2", 5, 10.00));
        po.setOrderItems(items);
        
        return po;
    }

    // Helper method to create a Document from PurchaseOrder
    private Document createTestDocument(int orderId, String status, LocalDate shippingDate, LocalDate cancelledDate) {
        Document doc = new Document("orderId", orderId)
            .append("orderDate", LocalDate.now().minusDays(1).toString())
            .append("orderNumber", "PO-001")
            .append("vendor", "Test Vendor")
            .append("orderStatus", status)
            .append("orderItems", Arrays.asList(
                new Document("itemName", "Item1")
                    .append("quantity", 10)
                    .append("pricePerItem", 5.50),
                new Document("itemName", "Item2")
                    .append("quantity", 5)
                    .append("pricePerItem", 10.00)
            ));
        
        if (shippingDate != null) {
            doc.append("shippingDate", shippingDate.toString());
        }
        if (cancelledDate != null) {
            doc.append("cancelledDate", cancelledDate.toString());
        }
        
        return doc;
    }

    // =================== 1. CRUD OPERATIONS TESTS ===================

    @Test
    void testAddPurchaseOrder_ShouldInsertDocument() {
        // Mock getNextOrderId by setting up find() to return empty cursor
        when(purchaseOrderCollection.find()).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(false);
        
        PurchaseOrder po = createTestPurchaseOrder(0, "pending");
        
        purchaseOrderService.addPurchaseOrder(po);
        
        verify(purchaseOrderCollection, times(1)).insertOne(any(Document.class));
        assertNotEquals(0, po.getOrderId()); // ID should be generated
        assertNotNull(po.getOrderNumber()); // Order number should be generated
    }

    @Test
    void testAddPurchaseOrder_ShouldAutoGenerateOrderId() {
        // Mock getNextOrderId by setting up find() to return empty cursor
        when(purchaseOrderCollection.find()).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(false);
        
        PurchaseOrder po = createTestPurchaseOrder(0, "pending");
        purchaseOrderService.addPurchaseOrder(po);
        
        assertNotEquals(0, po.getOrderId());
    }

    @Test
    void testAddPurchaseOrder_ShouldAutoGenerateOrderNumber() {
        when(purchaseOrderCollection.find()).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(false);
        
        PurchaseOrder po = createTestPurchaseOrder(0, "pending");
        po.setOrderNumber(null);
        purchaseOrderService.addPurchaseOrder(po);
        
        assertNotNull(po.getOrderNumber());
        assertTrue(po.getOrderNumber().startsWith("PO-"));
    }

    @Test
    void testGetPurchaseOrderById_ShouldReturnPurchaseOrder() {
        int orderId = 1;
        Document doc = createTestDocument(orderId, "pending", null, null);
        
        when(purchaseOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(doc);
        
        PurchaseOrder result = purchaseOrderService.getPurchaseOrderById(orderId);
        
        assertNotNull(result);
        assertEquals(orderId, result.getOrderId());
        assertEquals("pending", result.getOrderStatus());
    }

    @Test
    void testGetPurchaseOrderById_WhenNotFound_ShouldReturnNull() {
        when(purchaseOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(null);
        
        PurchaseOrder result = purchaseOrderService.getPurchaseOrderById(999);
        
        assertNull(result);
    }

    @Test
    void testGetAllPurchaseOrders_ShouldReturnList() {
        Document doc1 = createTestDocument(1, "pending", null, null);
        Document doc2 = createTestDocument(2, "shipping", LocalDate.now(), null);
        
        when(purchaseOrderCollection.find()).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, true, false);
        when(cursor.next()).thenReturn(doc1, doc2);
        
        List<PurchaseOrder> result = purchaseOrderService.getAllPurchaseOrders();
        
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getOrderId());
        assertEquals(2, result.get(1).getOrderId());
    }

    @Test
    void testUpdatePurchaseOrder_ShouldTriggerReplaceOne() {
        int orderId = 1;
        PurchaseOrder existing = createTestPurchaseOrder(orderId, "pending");
        PurchaseOrder updated = createTestPurchaseOrder(orderId, "shipping");
        
        Document existingDoc = createTestDocument(orderId, "pending", null, null);
        when(purchaseOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(existingDoc);
        
        purchaseOrderService.updatePurchaseOrder(orderId, updated);
        
        verify(purchaseOrderCollection, times(1)).replaceOne(any(Bson.class), any(Document.class));
    }

    @Test
    void testUpdatePurchaseOrder_WhenNotFound_ShouldReturnNull() {
        when(purchaseOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(null);
        
        PurchaseOrder updated = createTestPurchaseOrder(999, "pending");
        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(999, updated);
        
        assertNull(result);
        verify(purchaseOrderCollection, never()).replaceOne(any(Bson.class), any(Document.class));
    }

    @Test
    void testDeletePurchaseOrder_ShouldCallDeleteOne() {
        when(purchaseOrderCollection.deleteOne(any(Bson.class))).thenReturn(deleteResult);
        when(deleteResult.getDeletedCount()).thenReturn(1L);
        
        boolean result = purchaseOrderService.deletePurchaseOrder(1);
        
        assertTrue(result);
        verify(purchaseOrderCollection, times(1)).deleteOne(any(Bson.class));
    }

    @Test
    void testDeletePurchaseOrder_WhenNotFound_ShouldReturnFalse() {
        when(purchaseOrderCollection.deleteOne(any(Bson.class))).thenReturn(deleteResult);
        when(deleteResult.getDeletedCount()).thenReturn(0L);
        
        boolean result = purchaseOrderService.deletePurchaseOrder(999);
        
        assertFalse(result);
    }

    @Test
    void testPurchaseOrderExists_ShouldReturnTrue() {
        when(purchaseOrderCollection.countDocuments(any(Bson.class))).thenReturn(1L);
        
        boolean result = purchaseOrderService.purchaseOrderExists(1);
        
        assertTrue(result);
    }

    @Test
    void testPurchaseOrderExists_ShouldReturnFalse() {
        when(purchaseOrderCollection.countDocuments(any(Bson.class))).thenReturn(0L);
        
        boolean result = purchaseOrderService.purchaseOrderExists(999);
        
        assertFalse(result);
    }

    @Test
    void testGetNextOrderId_ShouldReturnMaxIdPlusOne() {
        Document doc1 = createTestDocument(5, "pending", null, null);
        Document doc2 = createTestDocument(10, "shipping", LocalDate.now(), null);
        
        when(purchaseOrderCollection.find()).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, true, false);
        when(cursor.next()).thenReturn(doc1, doc2);
        
        int nextId = purchaseOrderService.getNextOrderId();
        
        assertEquals(11, nextId);
    }

    @Test
    void testGetNextOrderId_WhenNoOrders_ShouldReturnOne() {
        when(purchaseOrderCollection.find()).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(false);
        
        int nextId = purchaseOrderService.getNextOrderId();
        
        assertEquals(1, nextId);
    }

    @Test
    void testGetPurchaseOrdersByStatus_ShouldReturnFilteredList() {
        Document doc1 = createTestDocument(1, "pending", null, null);
        Document doc2 = createTestDocument(2, "pending", null, null);
        
        when(purchaseOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, true, false);
        when(cursor.next()).thenReturn(doc1, doc2);
        
        List<PurchaseOrder> result = purchaseOrderService.getPurchaseOrdersByStatus("pending");
        
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(po -> "pending".equals(po.getOrderStatus())));
    }

    // =================== 2. STATUS TRANSITION TESTS ===================

    @Test
    void testUpdatePurchaseOrder_PendingToShipping_ShouldSetShippingDate() {
        int orderId = 1;
        
        Document existingDoc = createTestDocument(orderId, "pending", null, null);
        when(purchaseOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(existingDoc);
        
        PurchaseOrder updated = createTestPurchaseOrder(orderId, "shipping");
        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(orderId, updated);
        
        assertNotNull(result);
        assertNotNull(result.getShippingDate());
        assertEquals(LocalDate.now(), result.getShippingDate());
        assertNull(result.getCancelledDate());
    }

    @Test
    void testUpdatePurchaseOrder_ShippingToPending_ShouldClearShippingDate() {
        int orderId = 1;
        LocalDate existingShippingDate = LocalDate.now().minusDays(5);
        
        Document existingDoc = createTestDocument(orderId, "shipping", existingShippingDate, null);
        when(purchaseOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(existingDoc);
        
        PurchaseOrder updated = createTestPurchaseOrder(orderId, "pending");
        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(orderId, updated);
        
        assertNotNull(result);
        assertNull(result.getShippingDate()); // Should be cleared
        assertNull(result.getCancelledDate());
    }

    @Test
    void testUpdatePurchaseOrder_PendingToCancelled_ShouldSetCancelledDate() {
        int orderId = 1;
        
        Document existingDoc = createTestDocument(orderId, "pending", null, null);
        when(purchaseOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(existingDoc);
        
        PurchaseOrder updated = createTestPurchaseOrder(orderId, "cancelled");
        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(orderId, updated);
        
        assertNotNull(result);
        assertNotNull(result.getCancelledDate());
        assertEquals(LocalDate.now(), result.getCancelledDate());
        assertNull(result.getShippingDate()); // Should be cleared
    }

    @Test
    void testUpdatePurchaseOrder_ShippingToCancelled_ShouldSetCancelledDateAndClearShippingDate() {
        int orderId = 1;
        LocalDate existingShippingDate = LocalDate.now().minusDays(3);
        
        Document existingDoc = createTestDocument(orderId, "shipping", existingShippingDate, null);
        when(purchaseOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(existingDoc);
        
        PurchaseOrder updated = createTestPurchaseOrder(orderId, "cancelled");
        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(orderId, updated);
        
        assertNotNull(result);
        assertNotNull(result.getCancelledDate());
        assertEquals(LocalDate.now(), result.getCancelledDate());
        assertNull(result.getShippingDate()); // Should be cleared
    }

    @Test
    void testUpdatePurchaseOrder_CancelledStatus_ShouldKeepCancelledDate() {
        int orderId = 1;
        LocalDate existingCancelledDate = LocalDate.now().minusDays(10);
        
        Document existingDoc = createTestDocument(orderId, "cancelled", null, existingCancelledDate);
        when(purchaseOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(existingDoc);
        
        PurchaseOrder updated = createTestPurchaseOrder(orderId, "cancelled");
        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(orderId, updated);
        
        assertNotNull(result);
        assertEquals(existingCancelledDate, result.getCancelledDate()); // Should keep existing date
        assertNull(result.getShippingDate());
    }

    @Test
    void testUpdatePurchaseOrder_ShippingToShipping_ShouldKeepShippingDate() {
        int orderId = 1;
        LocalDate existingShippingDate = LocalDate.now().minusDays(2);
        
        Document existingDoc = createTestDocument(orderId, "shipping", existingShippingDate, null);
        when(purchaseOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(existingDoc);
        
        PurchaseOrder updated = createTestPurchaseOrder(orderId, "shipping");
        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(orderId, updated);
        
        assertNotNull(result);
        assertEquals(existingShippingDate, result.getShippingDate()); // Should keep existing date
    }

    // =================== 3. RECEIVED/RETURNED STATUS TESTS ===================

    @Test
    void testUpdatePurchaseOrder_ToReceived_ShouldSetReceivedDate() {
        int orderId = 1;
        
        Document existingDoc = createTestDocument(orderId, "shipping", LocalDate.now().minusDays(1), null);
        when(purchaseOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(existingDoc);
        
        PurchaseOrder updated = createTestPurchaseOrder(orderId, "received");
        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(orderId, updated);
        
        assertNotNull(result);
        assertNotNull(result.getReceivedDate());
        assertEquals(LocalDate.now(), result.getReceivedDate());
        assertNull(result.getReturnedDate());
    }

    @Test
    void testUpdatePurchaseOrder_ReceivedToReceived_ShouldKeepReceivedDate() {
        int orderId = 1;
        LocalDate existingReceivedDate = LocalDate.now().minusDays(5);
        
        Document existingDoc = createTestDocument(orderId, "received", LocalDate.now().minusDays(6), null);
        existingDoc.append("receivedDate", existingReceivedDate.toString());
        when(purchaseOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(existingDoc);
        
        PurchaseOrder updated = createTestPurchaseOrder(orderId, "received");
        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(orderId, updated);
        
        assertNotNull(result);
        assertEquals(existingReceivedDate, result.getReceivedDate()); // Should keep existing date
    }

    @Test
    void testUpdatePurchaseOrder_ToReturned_ShouldSetReturnedDate() {
        int orderId = 1;
        LocalDate existingReceivedDate = LocalDate.now().minusDays(3);
        
        Document existingDoc = createTestDocument(orderId, "received", LocalDate.now().minusDays(5), null);
        existingDoc.append("receivedDate", existingReceivedDate.toString());
        when(purchaseOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(existingDoc);
        
        PurchaseOrder updated = createTestPurchaseOrder(orderId, "returned");
        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(orderId, updated);
        
        assertNotNull(result);
        assertNotNull(result.getReturnedDate());
        assertEquals(LocalDate.now(), result.getReturnedDate());
        assertEquals(existingReceivedDate, result.getReceivedDate()); // Should keep received date
    }

    @Test
    void testUpdatePurchaseOrder_ReturnedToReturned_ShouldKeepReturnedDate() {
        int orderId = 1;
        LocalDate existingReturnedDate = LocalDate.now().minusDays(2);
        LocalDate existingReceivedDate = LocalDate.now().minusDays(5);
        
        Document existingDoc = createTestDocument(orderId, "returned", LocalDate.now().minusDays(6), null);
        existingDoc.append("receivedDate", existingReceivedDate.toString());
        existingDoc.append("returnedDate", existingReturnedDate.toString());
        when(purchaseOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(existingDoc);
        
        PurchaseOrder updated = createTestPurchaseOrder(orderId, "returned");
        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(orderId, updated);
        
        assertNotNull(result);
        assertEquals(existingReturnedDate, result.getReturnedDate()); // Should keep existing date
        assertEquals(existingReceivedDate, result.getReceivedDate()); // Should keep received date
    }

    // =================== 4. DOCUMENT MAPPING TESTS ===================

    @Test
    void testMapToPurchaseOrder_ShouldMapAllFields() {
        Document doc = createTestDocument(1, "pending", null, null);
        doc.append("receivedDate", LocalDate.now().minusDays(1).toString());
        doc.append("returnedDate", LocalDate.now().minusDays(2).toString());
        
        when(purchaseOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(doc);
        
        PurchaseOrder result = purchaseOrderService.getPurchaseOrderById(1);
        
        assertNotNull(result);
        assertEquals(1, result.getOrderId());
        assertEquals("PO-001", result.getOrderNumber());
        assertEquals("Test Vendor", result.getVendor());
        assertEquals("pending", result.getOrderStatus());
        assertNotNull(result.getOrderItems());
        assertEquals(2, result.getOrderItems().size());
    }

    @Test
    void testMapToPurchaseOrder_WithOrderItems_ShouldMapItemsCorrectly() {
        Document doc = createTestDocument(1, "pending", null, null);
        
        when(purchaseOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(doc);
        
        PurchaseOrder result = purchaseOrderService.getPurchaseOrderById(1);
        
        assertNotNull(result);
        assertEquals(2, result.getOrderItems().size());
        assertEquals("Item1", result.getOrderItems().get(0).getItemName());
        assertEquals(10, result.getOrderItems().get(0).getQuantity());
        assertEquals(5.50, result.getOrderItems().get(0).getPricePerItem());
    }

    @Test
    void testMapToPurchaseOrder_WithNullDates_ShouldHandleGracefully() {
        Document doc = createTestDocument(1, "pending", null, null);
        doc.append("shippingDate", null);
        doc.append("cancelledDate", null);
        
        when(purchaseOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(doc);
        
        PurchaseOrder result = purchaseOrderService.getPurchaseOrderById(1);
        
        assertNotNull(result);
        assertNull(result.getShippingDate());
        assertNull(result.getCancelledDate());
    }

    @Test
    void testMapToPurchaseOrder_WithEmptyOrderItems_ShouldReturnEmptyList() {
        Document doc = new Document("orderId", 1)
            .append("orderDate", LocalDate.now().toString())
            .append("orderNumber", "PO-001")
            .append("vendor", "Test Vendor")
            .append("orderStatus", "pending")
            .append("orderItems", new ArrayList<>());
        
        when(purchaseOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(doc);
        
        PurchaseOrder result = purchaseOrderService.getPurchaseOrderById(1);
        
        assertNotNull(result);
        assertNotNull(result.getOrderItems());
        assertTrue(result.getOrderItems().isEmpty());
    }

    // =================== 5. CASE-INSENSITIVE STATUS HANDLING ===================

    @Test
    void testUpdatePurchaseOrder_WithUppercaseStatus_ShouldHandleCorrectly() {
        int orderId = 1;
        Document existingDoc = createTestDocument(orderId, "pending", null, null);
        when(purchaseOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(existingDoc);
        
        PurchaseOrder updated = createTestPurchaseOrder(orderId, "SHIPPING");
        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(orderId, updated);
        
        assertNotNull(result);
        assertNotNull(result.getShippingDate());
        assertEquals(LocalDate.now(), result.getShippingDate());
    }

    @Test
    void testUpdatePurchaseOrder_WithMixedCaseStatus_ShouldHandleCorrectly() {
        int orderId = 1;
        Document existingDoc = createTestDocument(orderId, "pending", null, null);
        when(purchaseOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(existingDoc);
        
        PurchaseOrder updated = createTestPurchaseOrder(orderId, "Cancelled");
        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(orderId, updated);
        
        assertNotNull(result);
        assertNotNull(result.getCancelledDate());
        assertEquals(LocalDate.now(), result.getCancelledDate());
    }

    // =================== 6. INVALID STATUS HANDLING (DEFAULT CASE) ===================

    @Test
    void testUpdatePurchaseOrder_WithInvalidStatus_ShouldPreserveExistingDates() {
        int orderId = 1;
        LocalDate existingReceivedDate = LocalDate.now().minusDays(5);
        LocalDate existingShippingDate = LocalDate.now().minusDays(3);
        LocalDate existingCancelledDate = LocalDate.now().minusDays(1);
        
        Document existingDoc = createTestDocument(orderId, "pending", existingShippingDate, existingCancelledDate);
        existingDoc.append("receivedDate", existingReceivedDate.toString());
        when(purchaseOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(existingDoc);
        
        PurchaseOrder updated = createTestPurchaseOrder(orderId, "invalid_status");
        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(orderId, updated);
        
        assertNotNull(result);
        // Should preserve all existing dates when status is invalid
        assertEquals(existingReceivedDate, result.getReceivedDate());
        assertEquals(existingShippingDate, result.getShippingDate());
        assertEquals(existingCancelledDate, result.getCancelledDate());
    }

    // =================== 7. ORDER ID EDGE CASES ===================

    @Test
    void testAddPurchaseOrder_WithExistingOrderId_ShouldNotRegenerate() {
        // When orderId is already set (not 0), getNextOrderId() is not called
        PurchaseOrder po = createTestPurchaseOrder(999, "pending");
        po.setOrderId(999); // Explicitly set ID (not 0)
        purchaseOrderService.addPurchaseOrder(po);
        
        assertEquals(999, po.getOrderId()); // Should keep the set ID
        verify(purchaseOrderCollection, times(1)).insertOne(any(Document.class));
    }

    @Test
    void testUpdatePurchaseOrder_ShouldEnforceOrderIdMatch() {
        int orderId = 1;
        Document existingDoc = createTestDocument(orderId, "pending", null, null);
        when(purchaseOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(existingDoc);
        
        PurchaseOrder updated = createTestPurchaseOrder(999, "shipping"); // Different ID
        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(orderId, updated);
        
        assertNotNull(result);
        assertEquals(orderId, result.getOrderId()); // Should be forced to match
        verify(purchaseOrderCollection, times(1)).replaceOne(any(Bson.class), any(Document.class));
    }

    // =================== 8. ORDER NUMBER EDGE CASES ===================

    @Test
    void testAddPurchaseOrder_WithEmptyStringOrderNumber_ShouldAutoGenerate() {
        when(purchaseOrderCollection.find()).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(false);
        
        PurchaseOrder po = createTestPurchaseOrder(0, "pending");
        po.setOrderNumber(""); // Empty string
        purchaseOrderService.addPurchaseOrder(po);
        
        assertNotNull(po.getOrderNumber());
        assertFalse(po.getOrderNumber().isEmpty());
        assertTrue(po.getOrderNumber().startsWith("PO-"));
    }

    @Test
    void testAddPurchaseOrder_WithNullOrderNumber_ShouldAutoGenerate() {
        when(purchaseOrderCollection.find()).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(false);
        
        PurchaseOrder po = createTestPurchaseOrder(0, "pending");
        po.setOrderNumber(null);
        purchaseOrderService.addPurchaseOrder(po);
        
        assertNotNull(po.getOrderNumber());
        assertTrue(po.getOrderNumber().startsWith("PO-"));
    }

    // =================== 9. STATUS UNCHANGED (DATE PRESERVATION) ===================

    @Test
    void testUpdatePurchaseOrder_PendingToPending_ShouldPreserveDates() {
        int orderId = 1;
        Document existingDoc = createTestDocument(orderId, "pending", null, null);
        when(purchaseOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(existingDoc);
        
        PurchaseOrder updated = createTestPurchaseOrder(orderId, "pending"); // Same status
        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(orderId, updated);
        
        assertNotNull(result);
        assertNull(result.getShippingDate()); // Should remain null
        assertNull(result.getCancelledDate()); // Should remain null
    }

    @Test
    void testUpdatePurchaseOrder_CancelledToCancelled_ShouldPreserveCancelledDate() {
        int orderId = 1;
        LocalDate existingCancelledDate = LocalDate.now().minusDays(10);
        
        Document existingDoc = createTestDocument(orderId, "cancelled", null, existingCancelledDate);
        when(purchaseOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(existingDoc);
        
        PurchaseOrder updated = createTestPurchaseOrder(orderId, "cancelled");
        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(orderId, updated);
        
        assertNotNull(result);
        assertEquals(existingCancelledDate, result.getCancelledDate()); // Should preserve
        assertNull(result.getShippingDate()); // Should remain cleared
    }

    // =================== 10. COMPLEX STATUS TRANSITIONS ===================

    @Test
    void testUpdatePurchaseOrder_ReceivedToCancelled_ShouldKeepReceivedDate() {
        int orderId = 1;
        LocalDate existingReceivedDate = LocalDate.now().minusDays(5);
        
        Document existingDoc = createTestDocument(orderId, "received", LocalDate.now().minusDays(6), null);
        existingDoc.append("receivedDate", existingReceivedDate.toString());
        when(purchaseOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(existingDoc);
        
        PurchaseOrder updated = createTestPurchaseOrder(orderId, "cancelled");
        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(orderId, updated);
        
        assertNotNull(result);
        assertNotNull(result.getCancelledDate());
        assertEquals(LocalDate.now(), result.getCancelledDate());
        assertEquals(existingReceivedDate, result.getReceivedDate()); // Should keep received date
        assertNull(result.getShippingDate()); // Should be cleared
    }

    @Test
    void testUpdatePurchaseOrder_ReturnedToCancelled_ShouldKeepBothDates() {
        int orderId = 1;
        LocalDate existingReceivedDate = LocalDate.now().minusDays(10);
        LocalDate existingReturnedDate = LocalDate.now().minusDays(5);
        
        Document existingDoc = createTestDocument(orderId, "returned", LocalDate.now().minusDays(12), null);
        existingDoc.append("receivedDate", existingReceivedDate.toString());
        existingDoc.append("returnedDate", existingReturnedDate.toString());
        when(purchaseOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(existingDoc);
        
        PurchaseOrder updated = createTestPurchaseOrder(orderId, "cancelled");
        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(orderId, updated);
        
        assertNotNull(result);
        assertNotNull(result.getCancelledDate());
        assertEquals(LocalDate.now(), result.getCancelledDate());
        assertEquals(existingReceivedDate, result.getReceivedDate()); // Should keep
        assertEquals(existingReturnedDate, result.getReturnedDate()); // Should keep
        assertNull(result.getShippingDate()); // Should be cleared
    }

    // =================== 11. NULL ORDER ITEMS HANDLING ===================

    @Test
    void testMapToPurchaseOrder_WithNullOrderItems_ShouldReturnEmptyList() {
        Document doc = new Document("orderId", 1)
            .append("orderDate", LocalDate.now().toString())
            .append("orderNumber", "PO-001")
            .append("vendor", "Test Vendor")
            .append("orderStatus", "pending")
            .append("orderItems", null); // Explicitly null
        
        when(purchaseOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(doc);
        
        PurchaseOrder result = purchaseOrderService.getPurchaseOrderById(1);
        
        assertNotNull(result);
        assertNotNull(result.getOrderItems()); // Should not be null, should be empty list
        assertTrue(result.getOrderItems().isEmpty());
    }

    @Test
    void testMapFromPurchaseOrder_WithNullOrderItems_ShouldHandleGracefully() {
        // When orderId is already set (not 0), getNextOrderId() is not called
        PurchaseOrder po = new PurchaseOrder();
        po.setOrderId(1); // Already set, so getNextOrderId() won't be called
        po.setOrderDate(LocalDate.now());
        po.setVendor("Test Vendor");
        po.setOrderStatus("pending");
        po.setOrderItems(null); // Null order items
        
        purchaseOrderService.addPurchaseOrder(po);
        
        verify(purchaseOrderCollection, times(1)).insertOne(any(Document.class));
        assertNotNull(po.getOrderItems()); // Should be initialized to empty list by model
    }

    // =================== 12. DOCUMENT WITH MISSING FIELDS ===================

    @Test
    void testMapToPurchaseOrder_WithMissingOptionalFields_ShouldHandleGracefully() {
        Document doc = new Document("orderId", 1)
            .append("orderStatus", "pending");
        // Missing orderDate, orderNumber, vendor, orderItems, dates
        
        when(purchaseOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(doc);
        
        PurchaseOrder result = purchaseOrderService.getPurchaseOrderById(1);
        
        assertNotNull(result);
        assertEquals(1, result.getOrderId());
        assertEquals("pending", result.getOrderStatus());
        assertNull(result.getOrderDate());
        assertNull(result.getOrderNumber());
        assertNull(result.getVendor());
        assertNotNull(result.getOrderItems());
    }

    @Test
    void testMapToPurchaseOrder_WithMissingDateFields_ShouldReturnNullDates() {
        Document doc = createTestDocument(1, "pending", null, null);
        // Don't append any date fields - they should be missing
        doc.remove("shippingDate");
        doc.remove("cancelledDate");
        doc.remove("receivedDate");
        doc.remove("returnedDate");
        
        when(purchaseOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(doc);
        
        PurchaseOrder result = purchaseOrderService.getPurchaseOrderById(1);
        
        assertNotNull(result);
        assertNull(result.getShippingDate());
        assertNull(result.getCancelledDate());
        assertNull(result.getReceivedDate());
        assertNull(result.getReturnedDate());
    }

    // =================== 13. EMPTY STRING DATE HANDLING ===================

    @Test
    void testMapToPurchaseOrder_WithEmptyStringDates_ShouldReturnNull() {
        Document doc = createTestDocument(1, "pending", null, null);
        doc.append("shippingDate", ""); // Empty string
        doc.append("cancelledDate", ""); // Empty string
        doc.append("receivedDate", ""); // Empty string
        doc.append("returnedDate", ""); // Empty string
        
        when(purchaseOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(doc);
        
        PurchaseOrder result = purchaseOrderService.getPurchaseOrderById(1);
        
        assertNotNull(result);
        // Empty strings should be treated as null
        assertNull(result.getShippingDate());
        assertNull(result.getCancelledDate());
        assertNull(result.getReceivedDate());
        assertNull(result.getReturnedDate());
    }

    // =================== 14. COLLECTION NULL EDGE CASES ===================

    @Test
    void testGetAllPurchaseOrders_WhenCollectionIsNull_ShouldReturnEmptyList() throws Exception {
        setField(purchaseOrderService, "purchaseOrderCollection", null);
        
        List<PurchaseOrder> result = purchaseOrderService.getAllPurchaseOrders();
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetPurchaseOrderById_WhenCollectionIsNull_ShouldReturnNull() throws Exception {
        setField(purchaseOrderService, "purchaseOrderCollection", null);
        
        PurchaseOrder result = purchaseOrderService.getPurchaseOrderById(1);
        
        assertNull(result);
    }

    @Test
    void testAddPurchaseOrder_WhenCollectionIsNull_ShouldReturnNull() throws Exception {
        setField(purchaseOrderService, "purchaseOrderCollection", null);
        
        PurchaseOrder po = createTestPurchaseOrder(0, "pending");
        PurchaseOrder result = purchaseOrderService.addPurchaseOrder(po);
        
        assertNull(result);
    }

    @Test
    void testUpdatePurchaseOrder_WhenCollectionIsNull_ShouldReturnNull() throws Exception {
        setField(purchaseOrderService, "purchaseOrderCollection", null);
        
        PurchaseOrder po = createTestPurchaseOrder(1, "pending");
        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(1, po);
        
        assertNull(result);
    }

    @Test
    void testDeletePurchaseOrder_WhenCollectionIsNull_ShouldReturnFalse() throws Exception {
        setField(purchaseOrderService, "purchaseOrderCollection", null);
        
        boolean result = purchaseOrderService.deletePurchaseOrder(1);
        
        assertFalse(result);
    }

    @Test
    void testPurchaseOrderExists_WhenCollectionIsNull_ShouldReturnFalse() throws Exception {
        setField(purchaseOrderService, "purchaseOrderCollection", null);
        
        boolean result = purchaseOrderService.purchaseOrderExists(1);
        
        assertFalse(result);
    }

    @Test
    void testGetNextOrderId_WhenCollectionIsNull_ShouldReturnOne() throws Exception {
        setField(purchaseOrderService, "purchaseOrderCollection", null);
        
        int result = purchaseOrderService.getNextOrderId();
        
        assertEquals(1, result);
    }

    @Test
    void testGetPurchaseOrdersByStatus_WhenCollectionIsNull_ShouldReturnEmptyList() throws Exception {
        setField(purchaseOrderService, "purchaseOrderCollection", null);
        
        List<PurchaseOrder> result = purchaseOrderService.getPurchaseOrdersByStatus("pending");
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
