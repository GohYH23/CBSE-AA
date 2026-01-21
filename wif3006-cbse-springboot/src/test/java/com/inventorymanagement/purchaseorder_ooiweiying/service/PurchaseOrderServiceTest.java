/*
 * Purchase Order Module testing done by Ooi Wei Ying
 * This test verifies the Spring Boot implementation for
 * Purchase Orders, including CRUD operations, status transitions,
 * date tracking, and validation rules.
 */

package com.inventorymanagement.purchaseorder_ooiweiying.service;

import com.inventorymanagement.purchaseorder_ooiweiying.model.OrderItem;
import com.inventorymanagement.purchaseorder_ooiweiying.model.PurchaseOrder;
import com.inventorymanagement.purchaseorder_ooiweiying.repository.PurchaseOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PurchaseOrderServiceTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @InjectMocks
    private PurchaseOrderService purchaseOrderService;

    // Helper method to create a test PurchaseOrder
    private PurchaseOrder createTestPurchaseOrder(int orderId, String status) {
        PurchaseOrder po = new PurchaseOrder();
        po.setId("test-id-" + orderId); // MongoDB ObjectId (String)
        po.setOrderId(orderId);
        po.setOrderDate(LocalDate.now().minusDays(1));
        po.setOrderNumber("PO-" + String.format("%03d", orderId));
        po.setVendor("Test Vendor");
        po.setOrderStatus(status);

        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem("Item1", 10, 5.50));
        items.add(new OrderItem("Item2", 5, 10.00));
        po.setOrderItems(items);

        return po;
    }

    // =================== 1. CRUD OPERATIONS TESTS ===================

    @Test
    void testCreatePurchaseOrder_ShouldSaveAndReturnPurchaseOrder() {
        PurchaseOrder po = createTestPurchaseOrder(0, "pending");
        PurchaseOrder saved = createTestPurchaseOrder(1, "pending");

        when(purchaseOrderRepository.findAll()).thenReturn(new ArrayList<>());
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(saved);

        PurchaseOrder result = purchaseOrderService.createPurchaseOrder(po);

        assertNotNull(result);
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
        assertNotEquals(0, po.getOrderId()); // ID should be generated
        assertNotNull(po.getOrderNumber()); // Order number should be generated
    }

    @Test
    void testCreatePurchaseOrder_ShouldAutoGenerateOrderId() {
        PurchaseOrder po = createTestPurchaseOrder(0, "pending");
        po.setOrderId(0);

        when(purchaseOrderRepository.findAll()).thenReturn(new ArrayList<>());
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(po);

        purchaseOrderService.createPurchaseOrder(po);

        assertNotEquals(0, po.getOrderId());
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
    }

    @Test
    void testCreatePurchaseOrder_ShouldAutoGenerateOrderNumber() {
        PurchaseOrder po = createTestPurchaseOrder(0, "pending");
        po.setOrderNumber(null);

        when(purchaseOrderRepository.findAll()).thenReturn(new ArrayList<>());
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(po);

        purchaseOrderService.createPurchaseOrder(po);

        assertNotNull(po.getOrderNumber());
        assertTrue(po.getOrderNumber().startsWith("PO-"));
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
    }

    @Test
    void testCreatePurchaseOrder_ShouldSetStatusToPendingIfEmpty() {
        PurchaseOrder po = createTestPurchaseOrder(0, null);

        when(purchaseOrderRepository.findAll()).thenReturn(new ArrayList<>());
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder saved = invocation.getArgument(0);
            saved.setId("test-id");
            return saved;
        });

        PurchaseOrder result = purchaseOrderService.createPurchaseOrder(po);

        assertEquals("pending", result.getOrderStatus());
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
    }

    @Test
    void testGetPurchaseOrderById_ShouldReturnOptionalPurchaseOrder() {
        String id = "test-id-1";
        PurchaseOrder po = createTestPurchaseOrder(1, "pending");

        when(purchaseOrderRepository.findById(id)).thenReturn(Optional.of(po));

        Optional<PurchaseOrder> result = purchaseOrderService.getPurchaseOrderById(id);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getOrderId());
        assertEquals("pending", result.get().getOrderStatus());
    }

    @Test
    void testGetPurchaseOrderById_WhenNotFound_ShouldReturnEmpty() {
        String id = "non-existent-id";

        when(purchaseOrderRepository.findById(id)).thenReturn(Optional.empty());

        Optional<PurchaseOrder> result = purchaseOrderService.getPurchaseOrderById(id);

        assertFalse(result.isPresent());
    }

    @Test
    void testGetPurchaseOrderByOrderId_ShouldReturnOptionalPurchaseOrder() {
        Integer orderId = 1;
        PurchaseOrder po = createTestPurchaseOrder(1, "pending");

        when(purchaseOrderRepository.findByOrderId(orderId)).thenReturn(Optional.of(po));

        Optional<PurchaseOrder> result = purchaseOrderService.getPurchaseOrderByOrderId(orderId);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getOrderId());
    }

    @Test
    void testGetAllPurchaseOrders_ShouldReturnList() {
        PurchaseOrder po1 = createTestPurchaseOrder(1, "pending");
        PurchaseOrder po2 = createTestPurchaseOrder(2, "shipping");

        when(purchaseOrderRepository.findAll()).thenReturn(Arrays.asList(po1, po2));

        List<PurchaseOrder> result = purchaseOrderService.getAllPurchaseOrders();

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getOrderId());
        assertEquals(2, result.get(1).getOrderId());
    }

    @Test
    void testUpdatePurchaseOrder_ShouldUpdateAndReturnPurchaseOrder() {
        String id = "test-id-1";
        PurchaseOrder existing = createTestPurchaseOrder(1, "pending");
        PurchaseOrder updated = createTestPurchaseOrder(1, "shipping");

        when(purchaseOrderRepository.findById(id)).thenReturn(Optional.of(existing));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(updated);

        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(id, updated);

        assertNotNull(result);
        assertEquals("shipping", result.getOrderStatus());
        verify(purchaseOrderRepository, times(1)).findById(id);
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
    }

    @Test
    void testUpdatePurchaseOrder_WhenNotFound_ShouldReturnNull() {
        String id = "non-existent-id";
        PurchaseOrder updated = createTestPurchaseOrder(1, "pending");

        when(purchaseOrderRepository.findById(id)).thenReturn(Optional.empty());

        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(id, updated);

        assertNull(result);
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }

    @Test
    void testDeletePurchaseOrder_ShouldCallRepositoryDelete() {
        String id = "test-id-1";

        purchaseOrderService.deletePurchaseOrder(id);

        verify(purchaseOrderRepository, times(1)).deleteById(id);
    }

    @Test
    void testDeletePurchaseOrderByOrderId_ShouldReturnTrueWhenExists() {
        Integer orderId = 1;
        PurchaseOrder po = createTestPurchaseOrder(1, "pending");

        when(purchaseOrderRepository.findByOrderId(orderId)).thenReturn(Optional.of(po));

        boolean result = purchaseOrderService.deletePurchaseOrderByOrderId(orderId);

        assertTrue(result);
        verify(purchaseOrderRepository, times(1)).deleteById(po.getId());
    }

    @Test
    void testDeletePurchaseOrderByOrderId_WhenNotFound_ShouldReturnFalse() {
        Integer orderId = 999;

        when(purchaseOrderRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        boolean result = purchaseOrderService.deletePurchaseOrderByOrderId(orderId);

        assertFalse(result);
        verify(purchaseOrderRepository, never()).deleteById(anyString());
    }

    @Test
    void testPurchaseOrderExists_ShouldReturnTrue() {
        Integer orderId = 1;
        PurchaseOrder po = createTestPurchaseOrder(1, "pending");

        when(purchaseOrderRepository.findByOrderId(orderId)).thenReturn(Optional.of(po));

        boolean result = purchaseOrderService.purchaseOrderExists(orderId);

        assertTrue(result);
    }

    @Test
    void testPurchaseOrderExists_ShouldReturnFalse() {
        Integer orderId = 999;

        when(purchaseOrderRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        boolean result = purchaseOrderService.purchaseOrderExists(orderId);

        assertFalse(result);
    }

    @Test
    void testGetNextOrderId_ShouldReturnMaxIdPlusOne() {
        PurchaseOrder po1 = createTestPurchaseOrder(5, "pending");
        PurchaseOrder po2 = createTestPurchaseOrder(10, "shipping");

        when(purchaseOrderRepository.findAll()).thenReturn(Arrays.asList(po1, po2));

        int nextId = purchaseOrderService.getNextOrderId();

        assertEquals(11, nextId);
    }

    @Test
    void testGetNextOrderId_WhenNoOrders_ShouldReturnOne() {
        when(purchaseOrderRepository.findAll()).thenReturn(new ArrayList<>());

        int nextId = purchaseOrderService.getNextOrderId();

        assertEquals(1, nextId);
    }

    @Test
    void testGetPurchaseOrdersByStatus_ShouldReturnFilteredList() {
        PurchaseOrder po1 = createTestPurchaseOrder(1, "pending");
        PurchaseOrder po2 = createTestPurchaseOrder(2, "pending");

        when(purchaseOrderRepository.findByOrderStatus("pending")).thenReturn(Arrays.asList(po1, po2));

        List<PurchaseOrder> result = purchaseOrderService.getPurchaseOrdersByStatus("pending");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(po -> "pending".equals(po.getOrderStatus())));
    }

    // =================== 2. STATUS TRANSITION TESTS ===================

    @Test
    void testUpdatePurchaseOrder_PendingToShipping_ShouldSetShippingDate() {
        String id = "test-id-1";
        PurchaseOrder existing = createTestPurchaseOrder(1, "pending");
        PurchaseOrder updated = createTestPurchaseOrder(1, "shipping");

        when(purchaseOrderRepository.findById(id)).thenReturn(Optional.of(existing));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder saved = invocation.getArgument(0);
            return saved;
        });

        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(id, updated);

        assertNotNull(result);
        assertNotNull(result.getShippingDate());
        assertEquals(LocalDate.now(), result.getShippingDate());
        assertNull(result.getCancelledDate());
    }

    @Test
    void testUpdatePurchaseOrder_ShippingToPending_ShouldClearShippingDate() {
        String id = "test-id-1";
        PurchaseOrder existing = createTestPurchaseOrder(1, "shipping");
        existing.setShippingDate(LocalDate.now().minusDays(5));
        PurchaseOrder updated = createTestPurchaseOrder(1, "pending");

        when(purchaseOrderRepository.findById(id)).thenReturn(Optional.of(existing));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder saved = invocation.getArgument(0);
            return saved;
        });

        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(id, updated);

        assertNotNull(result);
        assertNull(result.getShippingDate()); // Should be cleared
        assertNull(result.getCancelledDate());
    }

    @Test
    void testUpdatePurchaseOrder_PendingToCancelled_ShouldSetCancelledDate() {
        String id = "test-id-1";
        PurchaseOrder existing = createTestPurchaseOrder(1, "pending");
        PurchaseOrder updated = createTestPurchaseOrder(1, "cancelled");

        when(purchaseOrderRepository.findById(id)).thenReturn(Optional.of(existing));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder saved = invocation.getArgument(0);
            return saved;
        });

        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(id, updated);

        assertNotNull(result);
        assertNotNull(result.getCancelledDate());
        assertEquals(LocalDate.now(), result.getCancelledDate());
        assertNull(result.getShippingDate()); // Should be cleared
    }

    @Test
    void testUpdatePurchaseOrder_ShippingToCancelled_ShouldSetCancelledDateAndClearShippingDate() {
        String id = "test-id-1";
        PurchaseOrder existing = createTestPurchaseOrder(1, "shipping");
        existing.setShippingDate(LocalDate.now().minusDays(3));
        PurchaseOrder updated = createTestPurchaseOrder(1, "cancelled");

        when(purchaseOrderRepository.findById(id)).thenReturn(Optional.of(existing));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder saved = invocation.getArgument(0);
            return saved;
        });

        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(id, updated);

        assertNotNull(result);
        assertNotNull(result.getCancelledDate());
        assertEquals(LocalDate.now(), result.getCancelledDate());
        assertNull(result.getShippingDate()); // Should be cleared
    }

    @Test
    void testUpdatePurchaseOrder_CancelledStatus_ShouldKeepCancelledDate() {
        String id = "test-id-1";
        LocalDate existingCancelledDate = LocalDate.now().minusDays(10);
        PurchaseOrder existing = createTestPurchaseOrder(1, "cancelled");
        existing.setCancelledDate(existingCancelledDate);
        PurchaseOrder updated = createTestPurchaseOrder(1, "cancelled");

        when(purchaseOrderRepository.findById(id)).thenReturn(Optional.of(existing));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder saved = invocation.getArgument(0);
            return saved;
        });

        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(id, updated);

        assertNotNull(result);
        assertEquals(existingCancelledDate, result.getCancelledDate()); // Should keep existing date
        assertNull(result.getShippingDate());
    }

    @Test
    void testUpdatePurchaseOrder_ShippingToShipping_ShouldKeepShippingDate() {
        String id = "test-id-1";
        LocalDate existingShippingDate = LocalDate.now().minusDays(2);
        PurchaseOrder existing = createTestPurchaseOrder(1, "shipping");
        existing.setShippingDate(existingShippingDate);
        PurchaseOrder updated = createTestPurchaseOrder(1, "shipping");

        when(purchaseOrderRepository.findById(id)).thenReturn(Optional.of(existing));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder saved = invocation.getArgument(0);
            return saved;
        });

        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(id, updated);

        assertNotNull(result);
        assertEquals(existingShippingDate, result.getShippingDate()); // Should keep existing date
    }

    // =================== 3. RECEIVED/RETURNED STATUS TESTS ===================

    @Test
    void testUpdatePurchaseOrder_ToReceived_ShouldSetReceivedDate() {
        String id = "test-id-1";
        PurchaseOrder existing = createTestPurchaseOrder(1, "shipping");
        existing.setShippingDate(LocalDate.now().minusDays(1));
        PurchaseOrder updated = createTestPurchaseOrder(1, "received");

        when(purchaseOrderRepository.findById(id)).thenReturn(Optional.of(existing));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder saved = invocation.getArgument(0);
            return saved;
        });

        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(id, updated);

        assertNotNull(result);
        assertNotNull(result.getReceivedDate());
        assertEquals(LocalDate.now(), result.getReceivedDate());
        assertNull(result.getReturnedDate());
    }

    @Test
    void testUpdatePurchaseOrder_ReceivedToReceived_ShouldKeepReceivedDate() {
        String id = "test-id-1";
        LocalDate existingReceivedDate = LocalDate.now().minusDays(5);
        PurchaseOrder existing = createTestPurchaseOrder(1, "received");
        existing.setReceivedDate(existingReceivedDate);
        PurchaseOrder updated = createTestPurchaseOrder(1, "received");

        when(purchaseOrderRepository.findById(id)).thenReturn(Optional.of(existing));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder saved = invocation.getArgument(0);
            return saved;
        });

        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(id, updated);

        assertNotNull(result);
        assertEquals(existingReceivedDate, result.getReceivedDate()); // Should keep existing date
    }

    @Test
    void testUpdatePurchaseOrder_ToReturned_ShouldSetReturnedDate() {
        String id = "test-id-1";
        LocalDate existingReceivedDate = LocalDate.now().minusDays(3);
        PurchaseOrder existing = createTestPurchaseOrder(1, "received");
        existing.setReceivedDate(existingReceivedDate);
        PurchaseOrder updated = createTestPurchaseOrder(1, "returned");

        when(purchaseOrderRepository.findById(id)).thenReturn(Optional.of(existing));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder saved = invocation.getArgument(0);
            return saved;
        });

        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(id, updated);

        assertNotNull(result);
        assertNotNull(result.getReturnedDate());
        assertEquals(LocalDate.now(), result.getReturnedDate());
        assertEquals(existingReceivedDate, result.getReceivedDate()); // Should keep received date
    }

    @Test
    void testUpdatePurchaseOrder_ReturnedToReturned_ShouldKeepReturnedDate() {
        String id = "test-id-1";
        LocalDate existingReturnedDate = LocalDate.now().minusDays(2);
        LocalDate existingReceivedDate = LocalDate.now().minusDays(5);
        PurchaseOrder existing = createTestPurchaseOrder(1, "returned");
        existing.setReceivedDate(existingReceivedDate);
        existing.setReturnedDate(existingReturnedDate);
        PurchaseOrder updated = createTestPurchaseOrder(1, "returned");

        when(purchaseOrderRepository.findById(id)).thenReturn(Optional.of(existing));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder saved = invocation.getArgument(0);
            return saved;
        });

        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(id, updated);

        assertNotNull(result);
        assertEquals(existingReturnedDate, result.getReturnedDate()); // Should keep existing date
        assertEquals(existingReceivedDate, result.getReceivedDate()); // Should keep received date
    }

    // =================== 4. STATUS UNCHANGED (DATE PRESERVATION) ===================

    @Test
    void testUpdatePurchaseOrder_PendingToPending_ShouldPreserveDates() {
        String id = "test-id-1";
        PurchaseOrder existing = createTestPurchaseOrder(1, "pending");
        PurchaseOrder updated = createTestPurchaseOrder(1, "pending"); // Same status

        when(purchaseOrderRepository.findById(id)).thenReturn(Optional.of(existing));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder saved = invocation.getArgument(0);
            return saved;
        });

        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(id, updated);

        assertNotNull(result);
        assertNull(result.getShippingDate()); // Should remain null
        assertNull(result.getCancelledDate()); // Should remain null
    }

    // =================== 5. CASE-INSENSITIVE STATUS HANDLING ===================

    @Test
    void testUpdatePurchaseOrder_WithUppercaseStatus_ShouldHandleCorrectly() {
        String id = "test-id-1";
        PurchaseOrder existing = createTestPurchaseOrder(1, "pending");
        PurchaseOrder updated = createTestPurchaseOrder(1, "SHIPPING");

        when(purchaseOrderRepository.findById(id)).thenReturn(Optional.of(existing));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder saved = invocation.getArgument(0);
            return saved;
        });

        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(id, updated);

        assertNotNull(result);
        assertNotNull(result.getShippingDate());
        assertEquals(LocalDate.now(), result.getShippingDate());
    }

    @Test
    void testUpdatePurchaseOrder_WithMixedCaseStatus_ShouldHandleCorrectly() {
        String id = "test-id-1";
        PurchaseOrder existing = createTestPurchaseOrder(1, "pending");
        PurchaseOrder updated = createTestPurchaseOrder(1, "Cancelled");

        when(purchaseOrderRepository.findById(id)).thenReturn(Optional.of(existing));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder saved = invocation.getArgument(0);
            return saved;
        });

        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(id, updated);

        assertNotNull(result);
        assertNotNull(result.getCancelledDate());
        assertEquals(LocalDate.now(), result.getCancelledDate());
    }

    // =================== 6. INVALID STATUS HANDLING (DEFAULT CASE) ===================

    @Test
    void testUpdatePurchaseOrder_WithInvalidStatus_ShouldPreserveExistingDates() {
        String id = "test-id-1";
        LocalDate existingReceivedDate = LocalDate.now().minusDays(5);
        LocalDate existingShippingDate = LocalDate.now().minusDays(3);
        LocalDate existingCancelledDate = LocalDate.now().minusDays(1);
        PurchaseOrder existing = createTestPurchaseOrder(1, "pending");
        existing.setReceivedDate(existingReceivedDate);
        existing.setShippingDate(existingShippingDate);
        existing.setCancelledDate(existingCancelledDate);
        PurchaseOrder updated = createTestPurchaseOrder(1, "invalid_status");

        when(purchaseOrderRepository.findById(id)).thenReturn(Optional.of(existing));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder saved = invocation.getArgument(0);
            return saved;
        });

        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(id, updated);

        assertNotNull(result);
        // Should preserve all existing dates when status is invalid
        assertEquals(existingReceivedDate, result.getReceivedDate());
        assertEquals(existingShippingDate, result.getShippingDate());
        assertEquals(existingCancelledDate, result.getCancelledDate());
    }

    // =================== 7. ORDER ID EDGE CASES ===================

    @Test
    void testCreatePurchaseOrder_WithExistingOrderId_ShouldNotRegenerate() {
        PurchaseOrder po = createTestPurchaseOrder(999, "pending");
        po.setOrderId(999); // Explicitly set ID (not 0)

        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(po);

        PurchaseOrder result = purchaseOrderService.createPurchaseOrder(po);

        assertEquals(999, result.getOrderId()); // Should keep the set ID
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
        verify(purchaseOrderRepository, never()).findAll(); // Should not call findAll to get next ID
    }

    @Test
    void testUpdatePurchaseOrder_ShouldEnforceOrderIdMatch() {
        String id = "test-id-1";
        PurchaseOrder existing = createTestPurchaseOrder(1, "pending");
        PurchaseOrder updated = createTestPurchaseOrder(999, "shipping"); // Different ID

        when(purchaseOrderRepository.findById(id)).thenReturn(Optional.of(existing));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder saved = invocation.getArgument(0);
            return saved;
        });

        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(id, updated);

        assertNotNull(result);
        assertEquals(1, result.getOrderId()); // Should be forced to match existing
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
    }

    // =================== 8. ORDER NUMBER EDGE CASES ===================

    @Test
    void testCreatePurchaseOrder_WithEmptyStringOrderNumber_ShouldAutoGenerate() {
        PurchaseOrder po = createTestPurchaseOrder(0, "pending");
        po.setOrderNumber(""); // Empty string

        when(purchaseOrderRepository.findAll()).thenReturn(new ArrayList<>());
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(po);

        purchaseOrderService.createPurchaseOrder(po);

        assertNotNull(po.getOrderNumber());
        assertFalse(po.getOrderNumber().isEmpty());
        assertTrue(po.getOrderNumber().startsWith("PO-"));
    }

    @Test
    void testCreatePurchaseOrder_WithNullOrderNumber_ShouldAutoGenerate() {
        PurchaseOrder po = createTestPurchaseOrder(0, "pending");
        po.setOrderNumber(null);

        when(purchaseOrderRepository.findAll()).thenReturn(new ArrayList<>());
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(po);

        purchaseOrderService.createPurchaseOrder(po);

        assertNotNull(po.getOrderNumber());
        assertTrue(po.getOrderNumber().startsWith("PO-"));
    }

    // =================== 9. GOODS RECEIVE/RETURN SPECIFIC METHODS ===================

    @Test
    void testUpdateStatusToReceived_ShouldSetReceivedDate() {
        String id = "test-id-1";
        PurchaseOrder existing = createTestPurchaseOrder(1, "shipping");
        existing.setShippingDate(LocalDate.now().minusDays(1));

        when(purchaseOrderRepository.findById(id)).thenReturn(Optional.of(existing));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder saved = invocation.getArgument(0);
            return saved;
        });

        PurchaseOrder result = purchaseOrderService.updateStatusToReceived(id);

        assertNotNull(result);
        assertEquals("received", result.getOrderStatus());
        assertNotNull(result.getReceivedDate());
        assertEquals(LocalDate.now(), result.getReceivedDate());
    }

    @Test
    void testUpdateStatusToReceived_WhenNotShipping_ShouldThrowException() {
        String id = "test-id-1";
        PurchaseOrder existing = createTestPurchaseOrder(1, "pending");

        when(purchaseOrderRepository.findById(id)).thenReturn(Optional.of(existing));

        assertThrows(IllegalStateException.class, () -> {
            purchaseOrderService.updateStatusToReceived(id);
        });
    }

    @Test
    void testUpdateStatusToReturned_ShouldSetReturnedDate() {
        String id = "test-id-1";
        PurchaseOrder existing = createTestPurchaseOrder(1, "received");
        existing.setReceivedDate(LocalDate.now().minusDays(3));

        when(purchaseOrderRepository.findById(id)).thenReturn(Optional.of(existing));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder saved = invocation.getArgument(0);
            return saved;
        });

        PurchaseOrder result = purchaseOrderService.updateStatusToReturned(id);

        assertNotNull(result);
        assertEquals("returned", result.getOrderStatus());
        assertNotNull(result.getReturnedDate());
        assertEquals(LocalDate.now(), result.getReturnedDate());
    }

    @Test
    void testUpdateStatusToReturned_WhenNotReceived_ShouldThrowException() {
        String id = "test-id-1";
        PurchaseOrder existing = createTestPurchaseOrder(1, "shipping");

        when(purchaseOrderRepository.findById(id)).thenReturn(Optional.of(existing));

        assertThrows(IllegalStateException.class, () -> {
            purchaseOrderService.updateStatusToReturned(id);
        });
    }

    @Test
    void testRevertReceivedToShipping_ShouldClearReceivedDate() {
        String id = "test-id-1";
        PurchaseOrder existing = createTestPurchaseOrder(1, "received");
        existing.setReceivedDate(LocalDate.now().minusDays(5));

        when(purchaseOrderRepository.findById(id)).thenReturn(Optional.of(existing));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder saved = invocation.getArgument(0);
            return saved;
        });

        PurchaseOrder result = purchaseOrderService.revertReceivedToShipping(id);

        assertNotNull(result);
        assertEquals("shipping", result.getOrderStatus());
        assertNull(result.getReceivedDate()); // Should be cleared
    }

    @Test
    void testRevertReturnedToReceived_ShouldClearReturnedDate() {
        String id = "test-id-1";
        PurchaseOrder existing = createTestPurchaseOrder(1, "returned");
        existing.setReturnedDate(LocalDate.now().minusDays(2));

        when(purchaseOrderRepository.findById(id)).thenReturn(Optional.of(existing));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder saved = invocation.getArgument(0);
            return saved;
        });

        PurchaseOrder result = purchaseOrderService.revertReturnedToReceived(id);

        assertNotNull(result);
        assertEquals("received", result.getOrderStatus());
        assertNull(result.getReturnedDate()); // Should be cleared
    }

    // =================== 10. COMPLEX STATUS TRANSITIONS ===================

    @Test
    void testUpdatePurchaseOrder_ReceivedToCancelled_ShouldKeepReceivedDate() {
        String id = "test-id-1";
        LocalDate existingReceivedDate = LocalDate.now().minusDays(5);
        PurchaseOrder existing = createTestPurchaseOrder(1, "received");
        existing.setReceivedDate(existingReceivedDate);
        PurchaseOrder updated = createTestPurchaseOrder(1, "cancelled");

        when(purchaseOrderRepository.findById(id)).thenReturn(Optional.of(existing));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder saved = invocation.getArgument(0);
            return saved;
        });

        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(id, updated);

        assertNotNull(result);
        assertNotNull(result.getCancelledDate());
        assertEquals(LocalDate.now(), result.getCancelledDate());
        assertEquals(existingReceivedDate, result.getReceivedDate()); // Should keep received date
        assertNull(result.getShippingDate()); // Should be cleared
    }

    @Test
    void testUpdatePurchaseOrder_ReturnedToCancelled_ShouldKeepBothDates() {
        String id = "test-id-1";
        LocalDate existingReceivedDate = LocalDate.now().minusDays(10);
        LocalDate existingReturnedDate = LocalDate.now().minusDays(5);
        PurchaseOrder existing = createTestPurchaseOrder(1, "returned");
        existing.setReceivedDate(existingReceivedDate);
        existing.setReturnedDate(existingReturnedDate);
        PurchaseOrder updated = createTestPurchaseOrder(1, "cancelled");

        when(purchaseOrderRepository.findById(id)).thenReturn(Optional.of(existing));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder saved = invocation.getArgument(0);
            return saved;
        });

        PurchaseOrder result = purchaseOrderService.updatePurchaseOrder(id, updated);

        assertNotNull(result);
        assertNotNull(result.getCancelledDate());
        assertEquals(LocalDate.now(), result.getCancelledDate());
        assertEquals(existingReceivedDate, result.getReceivedDate()); // Should keep
        assertEquals(existingReturnedDate, result.getReturnedDate()); // Should keep
        assertNull(result.getShippingDate()); // Should be cleared
    }

    // =================== 11. ORDER ITEMS HANDLING ===================

    @Test
    void testCreatePurchaseOrder_WithNullOrderItems_ShouldHandleGracefully() {
        PurchaseOrder po = createTestPurchaseOrder(0, "pending");
        po.setOrderItems(null); // Null order items

        when(purchaseOrderRepository.findAll()).thenReturn(new ArrayList<>());
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder saved = invocation.getArgument(0);
            saved.setId("test-id");
            return saved;
        });

        PurchaseOrder result = purchaseOrderService.createPurchaseOrder(po);

        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
    }

    @Test
    void testGetPurchaseOrderById_WithOrderItems_ShouldReturnItems() {
        String id = "test-id-1";
        PurchaseOrder po = createTestPurchaseOrder(1, "pending");

        when(purchaseOrderRepository.findById(id)).thenReturn(Optional.of(po));

        Optional<PurchaseOrder> result = purchaseOrderService.getPurchaseOrderById(id);

        assertTrue(result.isPresent());
        assertNotNull(result.get().getOrderItems());
        assertEquals(2, result.get().getOrderItems().size());
        assertEquals("Item1", result.get().getOrderItems().get(0).getItemName());
        assertEquals(10, result.get().getOrderItems().get(0).getQuantity());
        assertEquals(5.50, result.get().getOrderItems().get(0).getPricePerItem());
    }

    // =================== 12. UPDATE BY ORDER ID ===================

    @Test
    void testUpdatePurchaseOrderByOrderId_ShouldUpdateAndReturnPurchaseOrder() {
        Integer orderId = 1;
        String id = "test-id-1";
        PurchaseOrder existing = createTestPurchaseOrder(1, "pending");
        PurchaseOrder updated = createTestPurchaseOrder(1, "shipping");

        when(purchaseOrderRepository.findByOrderId(orderId)).thenReturn(Optional.of(existing));
        when(purchaseOrderRepository.findById(id)).thenReturn(Optional.of(existing));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(updated);

        PurchaseOrder result = purchaseOrderService.updatePurchaseOrderByOrderId(orderId, updated);

        assertNotNull(result);
        assertEquals("shipping", result.getOrderStatus());
        verify(purchaseOrderRepository, times(1)).findByOrderId(orderId);
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
    }

    @Test
    void testUpdatePurchaseOrderByOrderId_WhenNotFound_ShouldReturnNull() {
        Integer orderId = 999;
        PurchaseOrder updated = createTestPurchaseOrder(999, "pending");

        when(purchaseOrderRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        PurchaseOrder result = purchaseOrderService.updatePurchaseOrderByOrderId(orderId, updated);

        assertNull(result);
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }

    // =================== 13. GENERATE ORDER NUMBER ===================

    @Test
    void testGenerateOrderNumber_ShouldFormatCorrectly() {
        String orderNumber1 = purchaseOrderService.generateOrderNumber(1);
        String orderNumber10 = purchaseOrderService.generateOrderNumber(10);
        String orderNumber100 = purchaseOrderService.generateOrderNumber(100);

        assertEquals("PO-001", orderNumber1);
        assertEquals("PO-010", orderNumber10);
        assertEquals("PO-100", orderNumber100);
    }
}
