package com.inventorymanagement.salesorder_wongxiuhuan.service;

import com.inventorymanagement.salesorder_wongxiuhuan.model.*;
import com.inventorymanagement.salesorder_wongxiuhuan.repository.*;
import com.inventorymanagement.customer_gohyuheng.repository.CustomerRepository;
import com.inventorymanagement.customer_gohyuheng.model.Customer;
import com.inventorymanagement.product_ericleechunkiat.repository.ProductRepository;
import com.inventorymanagement.product_ericleechunkiat.model.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SalesOrderServiceTest {

    @Mock
    private SalesOrderRepository salesOrderRepo;

    @Mock
    private SalesOrderItemRepository salesOrderItemRepo;

    @Mock
    private DeliveryOrderRepository deliveryOrderRepo;

    @Mock
    private SalesReturnRepository salesReturnRepo;

    @Mock
    private TaxRepository taxRepo;

    @Mock
    private CustomerRepository customerRepo;

    @Mock
    private ProductRepository productRepo;

    @InjectMocks
    private SalesOrderService salesOrderService;

    // --- 1. SALES ORDER FUNCTIONALITIES TESTS ---

    @Test
    void testCreateSalesOrder_ShouldGenerateOrderNumberAndSave() {
        SalesOrder order = new SalesOrder();
        order.setCustomerId("cust-001");
        when(salesOrderRepo.save(any(SalesOrder.class))).thenReturn(order);

        SalesOrder result = salesOrderService.createSalesOrder(order);

        assertNotNull(result);
        assertNotNull(result.getOrderNumber());
        assertTrue(result.getOrderNumber().startsWith("SO-"));
        assertNotNull(result.getCreatedDate());
        assertNotNull(result.getUpdatedDate());
        verify(salesOrderRepo, times(1)).save(any(SalesOrder.class));
    }

    @Test
    void testGetAllSalesOrders_ShouldReturnListFromRepo() {
        when(salesOrderRepo.findAll()).thenReturn(List.of(new SalesOrder(), new SalesOrder()));

        List<SalesOrder> list = salesOrderService.getAllSalesOrders();

        assertEquals(2, list.size());
        verify(salesOrderRepo, times(1)).findAll();
    }

    @Test
    void testGetSalesOrderById_ShouldReturnOptionalSalesOrder() {
        String id = "so-001";
        SalesOrder order = new SalesOrder();
        order.setId(id);
        when(salesOrderRepo.findById(id)).thenReturn(Optional.of(order));

        Optional<SalesOrder> result = salesOrderService.getSalesOrderById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        verify(salesOrderRepo, times(1)).findById(id);
    }

    @Test
    void testGetSalesOrderByNumber_ShouldReturnOptionalSalesOrder() {
        String orderNumber = "SO-20240101120000";
        SalesOrder order = new SalesOrder();
        order.setOrderNumber(orderNumber);
        when(salesOrderRepo.findByOrderNumber(orderNumber)).thenReturn(Optional.of(order));

        Optional<SalesOrder> result = salesOrderService.getSalesOrderByNumber(orderNumber);

        assertTrue(result.isPresent());
        assertEquals(orderNumber, result.get().getOrderNumber());
        verify(salesOrderRepo, times(1)).findByOrderNumber(orderNumber);
    }

    @Test
    void testGetSalesOrdersByCustomerId_ShouldReturnList() {
        String customerId = "cust-123";
        when(salesOrderRepo.findByCustomerId(customerId)).thenReturn(List.of(new SalesOrder()));

        List<SalesOrder> results = salesOrderService.getSalesOrdersByCustomerId(customerId);

        assertFalse(results.isEmpty());
        verify(salesOrderRepo, times(1)).findByCustomerId(customerId);
    }

    @Test
    void testGetSalesOrdersByStatus_ShouldReturnList() {
        String status = "PENDING";
        when(salesOrderRepo.findByOrderStatus(status)).thenReturn(List.of(new SalesOrder()));

        List<SalesOrder> results = salesOrderService.getSalesOrdersByStatus(status);

        assertFalse(results.isEmpty());
        verify(salesOrderRepo, times(1)).findByOrderStatus(status);
    }

    @Test
    void testUpdateSalesOrder_ShouldUpdateTimestampAndSave() {
        SalesOrder order = new SalesOrder();
        order.setId("so-001");
        when(salesOrderRepo.save(any(SalesOrder.class))).thenReturn(order);

        SalesOrder result = salesOrderService.updateSalesOrder(order);

        assertNotNull(result.getUpdatedDate());
        verify(salesOrderRepo, times(1)).save(order);
    }

    @Test
    void testDeleteSalesOrder_WhenLinkedToDeliveryOrders_ShouldReturnErrorMessage() {
        String orderId = "so-001";
        when(deliveryOrderRepo.findBySalesOrderId(orderId)).thenReturn(List.of(new DeliveryOrder()));

        String result = salesOrderService.deleteSalesOrder(orderId);

        assertTrue(result.contains("❌ Cannot delete"));
        verify(salesOrderRepo, never()).deleteById(anyString());
    }

    @Test
    void testDeleteSalesOrder_WhenNotLinked_ShouldDeleteSuccessfully() {
        String orderId = "so-002";
        when(deliveryOrderRepo.findBySalesOrderId(orderId)).thenReturn(List.of());
        when(salesOrderItemRepo.findBySalesOrderId(orderId)).thenReturn(List.of());

        String result = salesOrderService.deleteSalesOrder(orderId);

        assertTrue(result.contains("✅ Sales Order deleted successfully"));
        verify(salesOrderItemRepo, times(1)).deleteAll(any());
        verify(salesOrderRepo, times(1)).deleteById(orderId);
    }

    // --- 2. SALES ORDER ITEM FUNCTIONALITIES TESTS ---

    @Test
    void testAddSalesOrderItem_ShouldSetTimestampsAndRecalculateTotals() {
        SalesOrderItem item = new SalesOrderItem();
        item.setSalesOrderId("so-001");
        item.setUnitPrice(BigDecimal.valueOf(100));
        item.setQuantity(2);
        
        SalesOrder order = new SalesOrder();
        order.setId("so-001");
        
        when(salesOrderItemRepo.save(any(SalesOrderItem.class))).thenReturn(item);
        when(salesOrderRepo.findById("so-001")).thenReturn(Optional.of(order));
        when(salesOrderItemRepo.findBySalesOrderId("so-001")).thenReturn(List.of(item));

        SalesOrderItem result = salesOrderService.addSalesOrderItem(item);

        assertNotNull(result.getCreatedDate());
        assertNotNull(result.getUpdatedDate());
        verify(salesOrderItemRepo, times(1)).save(any(SalesOrderItem.class));
        verify(salesOrderRepo, atLeastOnce()).findById("so-001");
    }

    @Test
    void testGetItemsByOrderId_ShouldReturnList() {
        String orderId = "so-123";
        when(salesOrderItemRepo.findBySalesOrderId(orderId)).thenReturn(List.of(new SalesOrderItem()));

        List<SalesOrderItem> results = salesOrderService.getItemsByOrderId(orderId);

        assertFalse(results.isEmpty());
        verify(salesOrderItemRepo, times(1)).findBySalesOrderId(orderId);
    }

    @Test
    void testGetAllSalesOrderItems_ShouldReturnListFromRepo() {
        when(salesOrderItemRepo.findAll()).thenReturn(List.of(new SalesOrderItem()));

        List<SalesOrderItem> list = salesOrderService.getAllSalesOrderItems();

        assertNotNull(list);
        verify(salesOrderItemRepo, times(1)).findAll();
    }

    @Test
    void testGetSalesOrderItemById_ShouldReturnOptionalItem() {
        String id = "item-001";
        SalesOrderItem item = new SalesOrderItem();
        item.setId(id);
        when(salesOrderItemRepo.findById(id)).thenReturn(Optional.of(item));

        Optional<SalesOrderItem> result = salesOrderService.getSalesOrderItemById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        verify(salesOrderItemRepo, times(1)).findById(id);
    }

    @Test
    void testUpdateSalesOrderItem_ShouldUpdateTimestampAndRecalculateTotals() {
        SalesOrderItem item = new SalesOrderItem();
        item.setId("item-001");
        item.setSalesOrderId("so-001");
        item.setUnitPrice(BigDecimal.valueOf(150));
        item.setQuantity(3);
        
        SalesOrder order = new SalesOrder();
        order.setId("so-001");
        
        when(salesOrderItemRepo.save(any(SalesOrderItem.class))).thenReturn(item);
        when(salesOrderRepo.findById("so-001")).thenReturn(Optional.of(order));
        when(salesOrderItemRepo.findBySalesOrderId("so-001")).thenReturn(List.of(item));

        SalesOrderItem result = salesOrderService.updateSalesOrderItem(item);

        assertNotNull(result.getUpdatedDate());
        verify(salesOrderItemRepo, times(1)).save(item);
        verify(salesOrderRepo, atLeastOnce()).findById("so-001");
    }

    @Test
    void testDeleteSalesOrderItem_ShouldDeleteAndRecalculateTotals() {
        String itemId = "item-555";
        SalesOrderItem item = new SalesOrderItem();
        item.setId(itemId);
        item.setSalesOrderId("so-001");
        
        SalesOrder order = new SalesOrder();
        order.setId("so-001");
        
        when(salesOrderItemRepo.findById(itemId)).thenReturn(Optional.of(item));
        when(salesOrderRepo.findById("so-001")).thenReturn(Optional.of(order));
        when(salesOrderItemRepo.findBySalesOrderId("so-001")).thenReturn(List.of());

        salesOrderService.deleteSalesOrderItem(itemId);

        verify(salesOrderItemRepo, times(1)).deleteById(itemId);
        verify(salesOrderRepo, atLeastOnce()).findById("so-001");
    }

    // --- 3. DELIVERY ORDER FUNCTIONALITIES TESTS ---

    @Test
    void testCreateDeliveryOrder_ShouldGenerateDeliveryNumberAndSave() {
        DeliveryOrder order = new DeliveryOrder();
        order.setSalesOrderId("so-001");
        when(deliveryOrderRepo.save(any(DeliveryOrder.class))).thenReturn(order);

        DeliveryOrder result = salesOrderService.createDeliveryOrder(order);

        assertNotNull(result);
        assertNotNull(result.getDeliveryNumber());
        assertTrue(result.getDeliveryNumber().startsWith("DO-"));
        assertNotNull(result.getCreatedDate());
        assertNotNull(result.getUpdatedDate());
        verify(deliveryOrderRepo, times(1)).save(any(DeliveryOrder.class));
    }

    @Test
    void testGetAllDeliveryOrders_ShouldReturnListFromRepo() {
        when(deliveryOrderRepo.findAll()).thenReturn(List.of(new DeliveryOrder()));

        List<DeliveryOrder> list = salesOrderService.getAllDeliveryOrders();

        assertNotNull(list);
        verify(deliveryOrderRepo, times(1)).findAll();
    }

    @Test
    void testGetDeliveryOrderById_ShouldReturnOptionalDeliveryOrder() {
        String id = "do-001";
        DeliveryOrder order = new DeliveryOrder();
        order.setId(id);
        when(deliveryOrderRepo.findById(id)).thenReturn(Optional.of(order));

        Optional<DeliveryOrder> result = salesOrderService.getDeliveryOrderById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        verify(deliveryOrderRepo, times(1)).findById(id);
    }

    @Test
    void testGetDeliveryOrderByNumber_ShouldReturnOptionalDeliveryOrder() {
        String deliveryNumber = "DO-20240101120000";
        DeliveryOrder order = new DeliveryOrder();
        order.setDeliveryNumber(deliveryNumber);
        when(deliveryOrderRepo.findByDeliveryNumber(deliveryNumber)).thenReturn(Optional.of(order));

        Optional<DeliveryOrder> result = salesOrderService.getDeliveryOrderByNumber(deliveryNumber);

        assertTrue(result.isPresent());
        assertEquals(deliveryNumber, result.get().getDeliveryNumber());
        verify(deliveryOrderRepo, times(1)).findByDeliveryNumber(deliveryNumber);
    }

    @Test
    void testGetDeliveryOrdersBySalesOrderId_ShouldReturnList() {
        String salesOrderId = "so-123";
        when(deliveryOrderRepo.findBySalesOrderId(salesOrderId)).thenReturn(List.of(new DeliveryOrder()));

        List<DeliveryOrder> results = salesOrderService.getDeliveryOrdersBySalesOrderId(salesOrderId);

        assertFalse(results.isEmpty());
        verify(deliveryOrderRepo, times(1)).findBySalesOrderId(salesOrderId);
    }

    @Test
    void testUpdateDeliveryOrder_ShouldUpdateTimestampAndSave() {
        DeliveryOrder order = new DeliveryOrder();
        order.setId("do-001");
        when(deliveryOrderRepo.save(any(DeliveryOrder.class))).thenReturn(order);

        DeliveryOrder result = salesOrderService.updateDeliveryOrder(order);

        assertNotNull(result.getUpdatedDate());
        verify(deliveryOrderRepo, times(1)).save(order);
    }

    @Test
    void testDeleteDeliveryOrder_WhenLinkedToSalesReturns_ShouldReturnErrorMessage() {
        String orderId = "do-001";
        when(salesReturnRepo.findByDeliveryOrderId(orderId)).thenReturn(List.of(new SalesReturn()));

        String result = salesOrderService.deleteDeliveryOrder(orderId);

        assertTrue(result.contains("❌ Cannot delete"));
        verify(deliveryOrderRepo, never()).deleteById(anyString());
    }

    @Test
    void testDeleteDeliveryOrder_WhenNotLinked_ShouldDeleteSuccessfully() {
        String orderId = "do-002";
        when(salesReturnRepo.findByDeliveryOrderId(orderId)).thenReturn(List.of());

        String result = salesOrderService.deleteDeliveryOrder(orderId);

        assertTrue(result.contains("✅ Delivery Order deleted successfully"));
        verify(deliveryOrderRepo, times(1)).deleteById(orderId);
    }

    // --- 4. SALES RETURN FUNCTIONALITIES TESTS ---

    @Test
    void testCreateSalesReturn_ShouldGenerateReturnNumberAndSave() {
        SalesReturn salesReturn = new SalesReturn();
        salesReturn.setDeliveryOrderId("do-001");
        when(salesReturnRepo.save(any(SalesReturn.class))).thenReturn(salesReturn);

        SalesReturn result = salesOrderService.createSalesReturn(salesReturn);

        assertNotNull(result);
        assertNotNull(result.getReturnNumber());
        assertTrue(result.getReturnNumber().startsWith("SR-"));
        assertNotNull(result.getCreatedDate());
        assertNotNull(result.getUpdatedDate());
        verify(salesReturnRepo, times(1)).save(any(SalesReturn.class));
    }

    @Test
    void testGetAllSalesReturns_ShouldReturnListFromRepo() {
        when(salesReturnRepo.findAll()).thenReturn(List.of(new SalesReturn()));

        List<SalesReturn> list = salesOrderService.getAllSalesReturns();

        assertNotNull(list);
        verify(salesReturnRepo, times(1)).findAll();
    }

    @Test
    void testGetSalesReturnById_ShouldReturnOptionalSalesReturn() {
        String id = "sr-001";
        SalesReturn salesReturn = new SalesReturn();
        salesReturn.setId(id);
        when(salesReturnRepo.findById(id)).thenReturn(Optional.of(salesReturn));

        Optional<SalesReturn> result = salesOrderService.getSalesReturnById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        verify(salesReturnRepo, times(1)).findById(id);
    }

    @Test
    void testGetSalesReturnByNumber_ShouldReturnOptionalSalesReturn() {
        String returnNumber = "SR-20240101120000";
        SalesReturn salesReturn = new SalesReturn();
        salesReturn.setReturnNumber(returnNumber);
        when(salesReturnRepo.findByReturnNumber(returnNumber)).thenReturn(Optional.of(salesReturn));

        Optional<SalesReturn> result = salesOrderService.getSalesReturnByNumber(returnNumber);

        assertTrue(result.isPresent());
        assertEquals(returnNumber, result.get().getReturnNumber());
        verify(salesReturnRepo, times(1)).findByReturnNumber(returnNumber);
    }

    @Test
    void testGetSalesReturnsByDeliveryOrderId_ShouldReturnList() {
        String deliveryOrderId = "do-123";
        when(salesReturnRepo.findByDeliveryOrderId(deliveryOrderId)).thenReturn(List.of(new SalesReturn()));

        List<SalesReturn> results = salesOrderService.getSalesReturnsByDeliveryOrderId(deliveryOrderId);

        assertFalse(results.isEmpty());
        verify(salesReturnRepo, times(1)).findByDeliveryOrderId(deliveryOrderId);
    }

    @Test
    void testUpdateSalesReturn_ShouldUpdateTimestampAndSave() {
        SalesReturn salesReturn = new SalesReturn();
        salesReturn.setId("sr-001");
        when(salesReturnRepo.save(any(SalesReturn.class))).thenReturn(salesReturn);

        SalesReturn result = salesOrderService.updateSalesReturn(salesReturn);

        assertNotNull(result.getUpdatedDate());
        verify(salesReturnRepo, times(1)).save(salesReturn);
    }

    @Test
    void testDeleteSalesReturn_ShouldDeleteSuccessfully() {
        String returnId = "sr-555";

        String result = salesOrderService.deleteSalesReturn(returnId);

        assertTrue(result.contains("✅ Sales Return deleted successfully"));
        verify(salesReturnRepo, times(1)).deleteById(returnId);
    }

    // --- 5. TAX FUNCTIONALITIES TESTS ---

    @Test
    void testCreateTax_ShouldSetTimestampsAndSave() {
        Tax tax = new Tax();
        tax.setTaxName("GST");
        tax.setTaxRate(BigDecimal.valueOf(6));
        when(taxRepo.save(any(Tax.class))).thenReturn(tax);

        Tax result = salesOrderService.createTax(tax);

        assertNotNull(result);
        assertNotNull(result.getCreatedDate());
        assertNotNull(result.getUpdatedDate());
        verify(taxRepo, times(1)).save(any(Tax.class));
    }

    @Test
    void testGetAllTaxes_ShouldReturnListFromRepo() {
        when(taxRepo.findAll()).thenReturn(List.of(new Tax()));

        List<Tax> list = salesOrderService.getAllTaxes();

        assertNotNull(list);
        verify(taxRepo, times(1)).findAll();
    }

    @Test
    void testGetTaxById_ShouldReturnOptionalTax() {
        String id = "tax-001";
        Tax tax = new Tax();
        tax.setId(id);
        when(taxRepo.findById(id)).thenReturn(Optional.of(tax));

        Optional<Tax> result = salesOrderService.getTaxById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        verify(taxRepo, times(1)).findById(id);
    }

    @Test
    void testGetTaxByName_ShouldReturnOptionalTax() {
        String taxName = "GST";
        Tax tax = new Tax();
        tax.setTaxName(taxName);
        when(taxRepo.findByTaxName(taxName)).thenReturn(Optional.of(tax));

        Optional<Tax> result = salesOrderService.getTaxByName(taxName);

        assertTrue(result.isPresent());
        assertEquals(taxName, result.get().getTaxName());
        verify(taxRepo, times(1)).findByTaxName(taxName);
    }

    @Test
    void testUpdateTax_ShouldUpdateTimestampAndSave() {
        Tax tax = new Tax();
        tax.setId("tax-001");
        when(taxRepo.save(any(Tax.class))).thenReturn(tax);

        Tax result = salesOrderService.updateTax(tax);

        assertNotNull(result.getUpdatedDate());
        verify(taxRepo, times(1)).save(tax);
    }

    @Test
    void testDeleteTax_WhenInUse_ShouldReturnErrorMessage() {
        String taxId = "tax-001";
        SalesOrder order = new SalesOrder();
        order.setTaxId(taxId);
        when(salesOrderRepo.findAll()).thenReturn(List.of(order));

        String result = salesOrderService.deleteTax(taxId);

        assertTrue(result.contains("❌ Cannot delete"));
        verify(taxRepo, never()).deleteById(anyString());
    }

    @Test
    void testDeleteTax_WhenNotInUse_ShouldDeleteSuccessfully() {
        String taxId = "tax-002";
        when(salesOrderRepo.findAll()).thenReturn(List.of());

        String result = salesOrderService.deleteTax(taxId);

        assertTrue(result.contains("✅ Tax deleted successfully"));
        verify(taxRepo, times(1)).deleteById(taxId);
    }

    // --- 6. HELPER METHODS TESTS ---

    @Test
    void testGetCustomerNameById_ShouldReturnCustomerName() {
        String customerId = "cust-123";
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setName("John Doe");
        when(customerRepo.findById(customerId)).thenReturn(Optional.of(customer));

        String result = salesOrderService.getCustomerNameById(customerId);

        assertEquals("John Doe", result);
        verify(customerRepo, times(1)).findById(customerId);
    }

    @Test
    void testGetCustomerNameById_WhenNotFound_ShouldReturnUnknown() {
        String customerId = "cust-999";
        when(customerRepo.findById(customerId)).thenReturn(Optional.empty());

        String result = salesOrderService.getCustomerNameById(customerId);

        assertEquals("Unknown", result);
        verify(customerRepo, times(1)).findById(customerId);
    }

    @Test
    void testGetCustomerIdByName_ShouldReturnCustomerId() {
        String customerName = "John Doe";
        Customer customer = new Customer();
        customer.setId("cust-123");
        customer.setName(customerName);
        when(customerRepo.findByName(customerName)).thenReturn(Optional.of(customer));

        String result = salesOrderService.getCustomerIdByName(customerName);

        assertEquals("cust-123", result);
        verify(customerRepo, times(1)).findByName(customerName);
    }

    @Test
    void testGetProductNameById_ShouldReturnProductName() {
        String productId = "prod-123";
        Product product = new Product();
        product.setId(productId);
        product.setName("Widget A");
        when(productRepo.findById(productId)).thenReturn(Optional.of(product));

        String result = salesOrderService.getProductNameById(productId);

        assertEquals("Widget A", result);
        verify(productRepo, times(1)).findById(productId);
    }

    @Test
    void testGetProductPriceById_ShouldReturnPrice() {
        String productId = "prod-123";
        Product product = new Product();
        product.setId(productId);
        product.setPrice(99.99);
        when(productRepo.findById(productId)).thenReturn(Optional.of(product));

        BigDecimal result = salesOrderService.getProductPriceById(productId);

        assertEquals(BigDecimal.valueOf(99.99), result);
        verify(productRepo, times(1)).findById(productId);
    }

    @Test
    void testGetTaxRateById_ShouldReturnTaxRate() {
        String taxId = "tax-001";
        Tax tax = new Tax();
        tax.setId(taxId);
        tax.setTaxRate(BigDecimal.valueOf(6));
        when(taxRepo.findById(taxId)).thenReturn(Optional.of(tax));

        BigDecimal result = salesOrderService.getTaxRateById(taxId);

        assertEquals(BigDecimal.valueOf(6), result);
        verify(taxRepo, times(1)).findById(taxId);
    }

    @Test
    void testRecalculateOrderTotals_ShouldCalculateCorrectAmounts() {
        String orderId = "so-001";
        
        // Setup order with tax
        SalesOrder order = new SalesOrder();
        order.setId(orderId);
        order.setTaxId("tax-001");
        
        // Setup tax
        Tax tax = new Tax();
        tax.setId("tax-001");
        tax.setTaxRate(BigDecimal.valueOf(6));
        
        // Setup items: 2 items @ 100 each = 200 subtotal
        SalesOrderItem item1 = new SalesOrderItem();
        item1.setUnitPrice(BigDecimal.valueOf(100));
        item1.setQuantity(2);
        
        when(salesOrderRepo.findById(orderId)).thenReturn(Optional.of(order));
        when(taxRepo.findById("tax-001")).thenReturn(Optional.of(tax));
        when(salesOrderItemRepo.findBySalesOrderId(orderId)).thenReturn(List.of(item1));
        when(salesOrderRepo.save(any(SalesOrder.class))).thenReturn(order);

        salesOrderService.recalculateOrderTotals(orderId);

        // Verify calculations: beforeTax = 200, tax = 12 (6% of 200), afterTax = 212
        verify(salesOrderRepo, times(1)).save(argThat(o -> 
            o.getBeforeTaxAmount().compareTo(BigDecimal.valueOf(200)) == 0 &&
            o.getTaxAmount().compareTo(BigDecimal.valueOf(12)) == 0 &&
            o.getAfterTaxAmount().compareTo(BigDecimal.valueOf(212)) == 0
        ));
    }

    @Test
    void testRecalculateOrderTotals_WithNoTax_ShouldCalculateWithoutTax() {
        String orderId = "so-002";
        
        // Setup order without tax
        SalesOrder order = new SalesOrder();
        order.setId(orderId);
        order.setTaxId(null);
        
        // Setup item: 3 @ 50 = 150
        SalesOrderItem item = new SalesOrderItem();
        item.setUnitPrice(BigDecimal.valueOf(50));
        item.setQuantity(3);
        
        when(salesOrderRepo.findById(orderId)).thenReturn(Optional.of(order));
        when(salesOrderItemRepo.findBySalesOrderId(orderId)).thenReturn(List.of(item));
        when(salesOrderRepo.save(any(SalesOrder.class))).thenReturn(order);

        salesOrderService.recalculateOrderTotals(orderId);

        // Verify: beforeTax = 150, tax = 0, afterTax = 150
        verify(salesOrderRepo, times(1)).save(argThat(o -> 
            o.getBeforeTaxAmount().compareTo(BigDecimal.valueOf(150)) == 0 &&
            o.getTaxAmount().compareTo(BigDecimal.ZERO) == 0 &&
            o.getAfterTaxAmount().compareTo(BigDecimal.valueOf(150)) == 0
        ));
    }
}