package com.inventory.salesorder;

import com.inventory.api.salesorder.model.*;
import com.inventory.api.customer.service.CustomerService;
import com.inventory.api.customer.model.Customer;
import com.inventory.api.product.service.ProductService;
import com.inventory.api.product.model.Product;
import com.mongodb.client.*;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SalesOrderServiceImplTest {

    @Mock
    private MongoCollection<Document> salesOrderCollection;
    @Mock
    private MongoCollection<Document> salesOrderItemCollection;
    @Mock
    private MongoCollection<Document> deliveryOrderCollection;
    @Mock
    private MongoCollection<Document> salesReturnCollection;
    @Mock
    private MongoCollection<Document> taxCollection;

    @Mock
    private CustomerService customerService;
    @Mock
    private ProductService productService;

    @Mock
    private FindIterable<Document> findIterable;
    @Mock
    private MongoCursor<Document> cursor;
    @Mock
    private DeleteResult deleteResult;

    @InjectMocks
    private SalesOrderServiceImpl salesOrderService;

    @BeforeEach
    void setUp() throws Exception {
        setField(salesOrderService, "salesOrderCollection", salesOrderCollection);
        setField(salesOrderService, "salesOrderItemCollection", salesOrderItemCollection);
        setField(salesOrderService, "deliveryOrderCollection", deliveryOrderCollection);
        setField(salesOrderService, "salesReturnCollection", salesReturnCollection);
        setField(salesOrderService, "taxCollection", taxCollection);
        setField(salesOrderService, "customerService", customerService);
        setField(salesOrderService, "productService", productService);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // --- 1. SALES ORDER MODULE TESTS ---

    @Test
    void testCreateSalesOrder_ShouldInsertDocument() {
        SalesOrder order = new SalesOrder();
        order.setOrderDate(LocalDate.now());
        order.setCustomerId("cust123");
        
        // Mock insertOne to set the _id
        doAnswer(invocation -> {
            Document doc = invocation.getArgument(0);
            doc.put("_id", new ObjectId());
            return null;
        }).when(salesOrderCollection).insertOne(any(Document.class));
        
        salesOrderService.createSalesOrder(order);
        
        verify(salesOrderCollection, times(1)).insertOne(any(Document.class));
        assertNotNull(order.getOrderNumber());
        assertNotNull(order.getId());
    }

    @Test
    void testGetSalesOrderById_ShouldReturnOptional() {
        ObjectId id = new ObjectId();
        Document doc = new Document("_id", id)
                .append("orderNumber", "SO-20250121-001")
                .append("orderDate", LocalDate.now().toString())
                .append("customerId", "cust123")
                .append("orderStatus", "PENDING");
        
        when(salesOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(doc);

        Optional<SalesOrder> result = salesOrderService.getSalesOrderById(id.toHexString());
        
        assertTrue(result.isPresent());
        assertEquals("SO-20250121-001", result.get().getOrderNumber());
    }

    @Test
    void testGetSalesOrderByNumber_ShouldReturnOptional() {
        Document doc = new Document("_id", new ObjectId())
                .append("orderNumber", "SO-20250121-001")
                .append("orderDate", LocalDate.now().toString());
        
        when(salesOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(doc);

        Optional<SalesOrder> result = salesOrderService.getSalesOrderByNumber("SO-20250121-001");
        
        assertTrue(result.isPresent());
    }

    @Test
    void testUpdateSalesOrder_ShouldTriggerReplaceOne() {
        ObjectId orderId = new ObjectId();

        SalesOrder order = new SalesOrder();
        order.setId(orderId.toHexString());
        order.setTaxId(null);
        order.setOrderStatus("PENDING");
        order.setOrderNumber("SO-20250121-001");
        
        salesOrderService.updateSalesOrder(order);
        
        assertNotNull(order.getEditedAt());
        verify(salesOrderCollection, times(1)).replaceOne(any(Bson.class), any(Document.class));
    }

    @Test
    void testDeleteSalesOrder_WhenHasDeliveryOrders_ShouldBlock() {
        String orderId = new ObjectId().toHexString();
        
        when(deliveryOrderCollection.countDocuments(any(Bson.class))).thenReturn(2L);
        
        String result = salesOrderService.deleteSalesOrder(orderId);
        
        assertTrue(result.contains("Cannot delete"));
        assertTrue(result.contains("delivery order"));
    }

    @Test
    void testDeleteSalesOrder_WhenNoDeliveryOrders_ShouldProceed() {
        String orderId = new ObjectId().toHexString();
        
        when(deliveryOrderCollection.countDocuments(any(Bson.class))).thenReturn(0L);
        when(salesOrderCollection.deleteOne(any(Bson.class))).thenReturn(deleteResult);
        when(deleteResult.getDeletedCount()).thenReturn(1L);
        
        String result = salesOrderService.deleteSalesOrder(orderId);
        
        assertTrue(result.contains("deleted successfully"));
        verify(salesOrderItemCollection, times(1)).deleteMany(any(Bson.class));
    }

    @Test
    void testGetAllSalesOrders_ShouldReturnList() {
        when(salesOrderCollection.find()).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, false);
        when(cursor.next()).thenReturn(
            new Document("_id", new ObjectId())
                .append("orderNumber", "SO-001")
                .append("orderDate", LocalDate.now().toString())
                .append("orderStatus", "PENDING")
        );

        List<SalesOrder> list = salesOrderService.getAllSalesOrders();
        
        assertEquals(1, list.size());
    }

    // --- 2. SALES ORDER ITEMS MODULE TESTS ---

    @Test
    void testAddSalesOrderItem_ShouldInsertDocument() {
        ObjectId orderId = new ObjectId();

        SalesOrderItem item = new SalesOrderItem();
        item.setSalesOrderId(orderId.toHexString());
        item.setProductId("prod456");
        item.setUnitPrice(BigDecimal.valueOf(100));
        item.setQuantity(2);
        
        // Mock insertOne to set the _id
        doAnswer(invocation -> {
            Document doc = invocation.getArgument(0);
            doc.put("_id", new ObjectId());
            return null;
        }).when(salesOrderItemCollection).insertOne(any(Document.class));
        
        // Mock for recalculation
        Document orderDoc = new Document("_id", orderId)
                .append("orderNumber", "SO-001")
                .append("orderStatus", "PENDING");
        when(salesOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(orderDoc);
        when(salesOrderItemCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(false);
        
        salesOrderService.addSalesOrderItem(item);
        
        verify(salesOrderItemCollection, times(1)).insertOne(any(Document.class));
    }

    @Test
    void testGetItemsByOrderId_ShouldReturnList() {
        when(salesOrderItemCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, false);
        when(cursor.next()).thenReturn(
            new Document("_id", new ObjectId())
                .append("salesOrderId", "order123")
                .append("productId", "prod456")
                .append("unitPrice", "100.00")
                .append("quantity", 2)
        );

        List<SalesOrderItem> items = salesOrderService.getItemsByOrderId("order123");
        
        assertEquals(1, items.size());
        assertEquals(2, items.get(0).getQuantity());
    }

    @Test
    void testUpdateSalesOrderItem_ShouldCallReplace() {
        ObjectId itemId = new ObjectId();
        ObjectId orderId = new ObjectId();

        SalesOrderItem item = new SalesOrderItem();
        item.setId(itemId.toHexString());
        item.setSalesOrderId(orderId.toHexString());
        item.setUnitPrice(BigDecimal.valueOf(150));
        
        // Mock for recalculation
        Document orderDoc = new Document("_id", orderId)
                .append("orderNumber", "SO-001");
        when(salesOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(orderDoc);
        when(salesOrderItemCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(false);
        
        salesOrderService.updateSalesOrderItem(item);
        
        assertNotNull(item.getEditedAt());
        verify(salesOrderItemCollection, times(1)).replaceOne(any(Bson.class), any(Document.class));
    }

    @Test
    void testDeleteSalesOrderItem_ShouldCallDeleteOne() {
        ObjectId itemId = new ObjectId();
        String orderId = new ObjectId().toHexString();
        
        Document itemDoc = new Document("_id", itemId)
                .append("salesOrderId", orderId);
        
        when(salesOrderItemCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(itemDoc);
        
        salesOrderService.deleteSalesOrderItem(itemId.toHexString());
        
        verify(salesOrderItemCollection, times(1)).deleteOne(any(Bson.class));
    }

    // --- 3. DELIVERY ORDER MODULE TESTS ---

    @Test
    void testCreateDeliveryOrder_ShouldInsertDocument() {
        DeliveryOrder order = new DeliveryOrder();
        order.setDeliveryDate(LocalDate.now());
        order.setSalesOrderId(new ObjectId().toHexString());
        
        // Mock insertOne to set the _id
        doAnswer(invocation -> {
            Document doc = invocation.getArgument(0);
            doc.put("_id", new ObjectId());
            return null;
        }).when(deliveryOrderCollection).insertOne(any(Document.class));
        
        salesOrderService.createDeliveryOrder(order);
        
        verify(deliveryOrderCollection, times(1)).insertOne(any(Document.class));
        assertNotNull(order.getDeliveryNumber());
        assertNotNull(order.getId());
    }

    @Test
    void testGetDeliveryOrderById_ShouldReturnOptional() {
        ObjectId id = new ObjectId();
        Document doc = new Document("_id", id)
                .append("deliveryNumber", "DO-20250121-001")
                .append("deliveryDate", LocalDate.now().toString())
                .append("salesOrderId", "so123")
                .append("status", "PENDING");
        
        when(deliveryOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(doc);

        Optional<DeliveryOrder> result = salesOrderService.getDeliveryOrderById(id.toHexString());
        
        assertTrue(result.isPresent());
        assertEquals("DO-20250121-001", result.get().getDeliveryNumber());
    }

    @Test
    void testUpdateDeliveryOrder_ShouldTriggerReplaceOne() {
        DeliveryOrder order = new DeliveryOrder();
        order.setId(new ObjectId().toHexString());
        order.setStatus("DELIVERED");
        
        salesOrderService.updateDeliveryOrder(order);
        
        assertNotNull(order.getEditedAt());
        verify(deliveryOrderCollection, times(1)).replaceOne(any(Bson.class), any(Document.class));
    }

    @Test
    void testDeleteDeliveryOrder_WhenHasSalesReturns_ShouldBlock() {
        String deliveryId = new ObjectId().toHexString();
        
        when(salesReturnCollection.countDocuments(any(Bson.class))).thenReturn(1L);
        
        String result = salesOrderService.deleteDeliveryOrder(deliveryId);
        
        assertTrue(result.contains("Cannot delete"));
        assertTrue(result.contains("sales return"));
    }

    @Test
    void testDeleteDeliveryOrder_WhenNoSalesReturns_ShouldProceed() {
        String deliveryId = new ObjectId().toHexString();
        
        when(salesReturnCollection.countDocuments(any(Bson.class))).thenReturn(0L);
        when(deliveryOrderCollection.deleteOne(any(Bson.class))).thenReturn(deleteResult);
        when(deleteResult.getDeletedCount()).thenReturn(1L);
        
        String result = salesOrderService.deleteDeliveryOrder(deliveryId);
        
        assertTrue(result.contains("deleted successfully"));
    }

    @Test
    void testGetAllDeliveryOrders_ShouldReturnList() {
        when(deliveryOrderCollection.find()).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(false);
        
        List<DeliveryOrder> list = salesOrderService.getAllDeliveryOrders();
        
        assertNotNull(list);
    }

    // --- 4. SALES RETURN MODULE TESTS ---

    @Test
    void testCreateSalesReturn_ShouldInsertDocument() {
        SalesReturn salesReturn = new SalesReturn();
        salesReturn.setReturnDate(LocalDate.now());
        salesReturn.setDeliveryOrderId(new ObjectId().toHexString());
        
        // Mock insertOne to set the _id
        doAnswer(invocation -> {
            Document doc = invocation.getArgument(0);
            doc.put("_id", new ObjectId());
            return null;
        }).when(salesReturnCollection).insertOne(any(Document.class));
        
        salesOrderService.createSalesReturn(salesReturn);
        
        verify(salesReturnCollection, times(1)).insertOne(any(Document.class));
        assertNotNull(salesReturn.getReturnNumber());
        assertNotNull(salesReturn.getId());
    }

    @Test
    void testGetSalesReturnById_ShouldReturnOptional() {
        ObjectId id = new ObjectId();
        Document doc = new Document("_id", id)
                .append("returnNumber", "SR-20250121-001")
                .append("returnDate", LocalDate.now().toString())
                .append("deliveryOrderId", "do123")
                .append("status", "PENDING");
        
        when(salesReturnCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(doc);

        Optional<SalesReturn> result = salesOrderService.getSalesReturnById(id.toHexString());
        
        assertTrue(result.isPresent());
        assertEquals("SR-20250121-001", result.get().getReturnNumber());
    }

    @Test
    void testUpdateSalesReturn_ShouldTriggerReplaceOne() {
        SalesReturn salesReturn = new SalesReturn();
        salesReturn.setId(new ObjectId().toHexString());
        salesReturn.setStatus("COMPLETED");
        
        salesOrderService.updateSalesReturn(salesReturn);
        
        assertNotNull(salesReturn.getEditedAt());
        verify(salesReturnCollection, times(1)).replaceOne(any(Bson.class), any(Document.class));
    }

    @Test
    void testDeleteSalesReturn_ShouldCallDeleteOne() {
        String returnId = new ObjectId().toHexString();
        
        when(salesReturnCollection.deleteOne(any(Bson.class))).thenReturn(deleteResult);
        when(deleteResult.getDeletedCount()).thenReturn(1L);
        
        String result = salesOrderService.deleteSalesReturn(returnId);
        
        assertTrue(result.contains("deleted successfully"));
    }

    @Test
    void testGetAllSalesReturns_ShouldReturnList() {
        when(salesReturnCollection.find()).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(false);
        
        List<SalesReturn> list = salesOrderService.getAllSalesReturns();
        
        assertNotNull(list);
    }

    // --- 5. TAX MODULE TESTS ---

    @Test
    void testGetTaxById_ShouldReturnOptional() {
        String taxId = "tax123";
        Document doc = new Document("_id", taxId)
                .append("taxName", "GST")
                .append("taxRate", "10");
        
        when(taxCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(doc);

        Optional<Tax> result = salesOrderService.getTaxById(taxId);
        
        assertTrue(result.isPresent());
        assertEquals("GST", result.get().getTaxName());
    }

    @Test
    void testGetTaxByName_ShouldReturnOptional() {
        Document doc = new Document("_id", "tax123")
                .append("taxName", "GST")
                .append("taxRate", "10");
        
        when(taxCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(doc);

        Optional<Tax> result = salesOrderService.getTaxByName("GST");
        
        assertTrue(result.isPresent());
    }

    @Test
    void testGetTaxRateById_ShouldReturnBigDecimal() {
        String taxId = "tax123";
        Document doc = new Document("_id", taxId)
                .append("taxName", "GST")
                .append("taxRate", "10");
        
        when(taxCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(doc);

        BigDecimal rate = salesOrderService.getTaxRateById(taxId);
        
        assertEquals(BigDecimal.valueOf(10), rate);
    }

    @Test
    void testGetTaxRateById_WhenNull_ShouldReturnZero() {
        BigDecimal rate = salesOrderService.getTaxRateById(null);
        
        assertEquals(BigDecimal.ZERO, rate);
    }

    @Test
    void testGetAllTaxes_ShouldReturnList() {
        when(taxCollection.find()).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, false);
        when(cursor.next()).thenReturn(
            new Document("_id", "tax123")
                .append("taxName", "GST")
                .append("taxRate", "10")
        );

        List<Tax> list = salesOrderService.getAllTaxes();
        
        assertEquals(1, list.size());
    }

    // --- 6. HELPER METHODS TESTS ---

    @Test
    void testGetCustomerNameById_ShouldReturnName() {
        Customer customer = new Customer();
        customer.setId("cust123");
        customer.setName("John Doe");
        
        when(customerService.getCustomerById("cust123")).thenReturn(Optional.of(customer));
        
        String name = salesOrderService.getCustomerNameById("cust123");
        
        assertEquals("John Doe", name);
    }

    @Test
    void testGetCustomerNameById_WhenNotFound_ShouldReturnUnknown() {
        when(customerService.getCustomerById("cust123")).thenReturn(Optional.empty());
        
        String name = salesOrderService.getCustomerNameById("cust123");
        
        assertEquals("Unknown", name);
    }

    @Test
    void testGetCustomerIdByName_ShouldReturnId() {
        Customer customer = new Customer();
        customer.setId("cust123");
        customer.setName("John Doe");
        
        when(customerService.getCustomerByName("John Doe")).thenReturn(Optional.of(customer));
        
        String id = salesOrderService.getCustomerIdByName("John Doe");
        
        assertEquals("cust123", id);
    }

    @Test
    void testGetProductNameById_ShouldReturnName() {
        Product product = new Product();
        product.setId("prod123");
        product.setName("Laptop");
        
        when(productService.getProduct("prod123")).thenReturn(product);
        
        String name = salesOrderService.getProductNameById("prod123");
        
        assertEquals("Laptop", name);
    }

    @Test
    void testGetProductNameById_WhenNotFound_ShouldReturnUnknown() {
        when(productService.getProduct("prod123")).thenReturn(null);
        
        String name = salesOrderService.getProductNameById("prod123");
        
        assertEquals("Unknown", name);
    }

    @Test
    void testGetProductIdByName_ShouldReturnId() {
        Product product1 = new Product();
        product1.setId("prod123");
        product1.setName("Laptop");
        
        Product product2 = new Product();
        product2.setId("prod456");
        product2.setName("Mouse");
        
        List<Product> products = new ArrayList<>();
        products.add(product1);
        products.add(product2);
        
        when(productService.getAllProducts()).thenReturn(products);
        
        String id = salesOrderService.getProductIdByName("Laptop");
        
        assertEquals("prod123", id);
    }

    @Test
    void testGetProductIdByName_WhenNotFound_ShouldReturnNull() {
        when(productService.getAllProducts()).thenReturn(new ArrayList<>());
        
        String id = salesOrderService.getProductIdByName("NonExistent");
        
        assertNull(id);
    }

    @Test
    void testGetProductPriceById_ShouldReturnPrice() {
        Product product = new Product();
        product.setId("prod123");
        product.setPrice(999.99);
        
        when(productService.getProduct("prod123")).thenReturn(product);
        
        BigDecimal price = salesOrderService.getProductPriceById("prod123");
        
        assertEquals(BigDecimal.valueOf(999.99), price);
    }

    @Test
    void testGetProductPriceById_WhenNotFound_ShouldReturnZero() {
        when(productService.getProduct("prod123")).thenReturn(null);
        
        BigDecimal price = salesOrderService.getProductPriceById("prod123");
        
        assertEquals(BigDecimal.ZERO, price);
    }

    @Test
    void testGetSalesOrderNumberById_ShouldReturnNumber() {
        ObjectId id = new ObjectId();
        Document doc = new Document("_id", id)
                .append("orderNumber", "SO-20250121-001");
        
        when(salesOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(doc);

        String orderNumber = salesOrderService.getSalesOrderNumberById(id.toHexString());
        
        assertEquals("SO-20250121-001", orderNumber);
    }

    @Test
    void testGetDeliveryOrderNumberById_ShouldReturnNumber() {
        ObjectId id = new ObjectId();
        Document doc = new Document("_id", id)
                .append("deliveryNumber", "DO-20250121-001");
        
        when(deliveryOrderCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(doc);

        String deliveryNumber = salesOrderService.getDeliveryOrderNumberById(id.toHexString());
        
        assertEquals("DO-20250121-001", deliveryNumber);
    }

    // --- 7. RECALCULATION TESTS ---

    @Test
    void testRecalculateOrderTotals_ShouldUpdateAmounts() {
        ObjectId orderId = new ObjectId();
        
        // Create separate FindIterable mocks for different queries
        FindIterable<Document> orderFindIterable = mock(FindIterable.class);
        FindIterable<Document> itemsFindIterable = mock(FindIterable.class);
        FindIterable<Document> taxFindIterable = mock(FindIterable.class);
        MongoCursor<Document> itemsCursor = mock(MongoCursor.class);
        
        // Mock order retrieval (called twice - once in recalculate, once in update)
        Document orderDoc = new Document("_id", orderId)
                .append("orderNumber", "SO-001")
                .append("taxId", "tax123")
                .append("orderStatus", "PENDING");
        
        when(salesOrderCollection.find(any(Bson.class))).thenReturn(orderFindIterable);
        when(orderFindIterable.first()).thenReturn(orderDoc);
        
        // Mock items retrieval
        when(salesOrderItemCollection.find(any(Bson.class))).thenReturn(itemsFindIterable);
        when(itemsFindIterable.iterator()).thenReturn(itemsCursor);
        when(itemsCursor.hasNext()).thenReturn(true, true, false);
        when(itemsCursor.next()).thenReturn(
            new Document("_id", new ObjectId())
                .append("unitPrice", "100.00")
                .append("quantity", 2),
            new Document("_id", new ObjectId())
                .append("unitPrice", "50.00")
                .append("quantity", 1)
        );
        
        // Mock tax retrieval
        Document taxDoc = new Document("_id", "tax123")
                .append("taxRate", "10");
        when(taxCollection.find(any(Bson.class))).thenReturn(taxFindIterable);
        when(taxFindIterable.first()).thenReturn(taxDoc);
        
        salesOrderService.recalculateOrderTotals(orderId.toHexString());
        
        // Should call replaceOne to update the order
        verify(salesOrderCollection, times(1)).replaceOne(any(Bson.class), any(Document.class));
    }

}