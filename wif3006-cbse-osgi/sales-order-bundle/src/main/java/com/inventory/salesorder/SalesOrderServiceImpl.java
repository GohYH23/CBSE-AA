package com.inventory.salesorder;

import com.inventory.api.salesorder.service.SalesOrderService;
import com.inventory.api.salesorder.model.SalesOrder;
import com.inventory.api.salesorder.model.SalesOrderItem;
import com.inventory.api.salesorder.model.DeliveryOrder;
import com.inventory.api.salesorder.model.SalesReturn;
import com.inventory.api.customer.service.CustomerService;
import com.inventory.api.customer.model.Customer;
// TODO: Uncomment when Product module is ready
// import com.inventory.api.product.service.ProductService;
// import com.inventory.api.product.model.Product;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component(service = SalesOrderService.class)
public class SalesOrderServiceImpl implements SalesOrderService {

    private MongoClient mongoClient;
    private MongoDatabase database;

    private MongoCollection<Document> salesOrderCollection;
    private MongoCollection<Document> salesOrderItemCollection;
    private MongoCollection<Document> deliveryOrderCollection;
    private MongoCollection<Document> salesReturnCollection;

    // Service References (for cross-module queries)
    @Reference
    private CustomerService customerService;

    // TODO: Uncomment when Product module is ready
    // @Reference
    // private ProductService productService;

    @Activate
    public void activate() {
        System.out.println("✅ Sales Order Service: Starting with MANUAL Mapping...");
        try {
            String uri = System.getProperty("mongodb.uri");
            if (uri == null || uri.isEmpty()) {
                System.err.println("❌ Error: mongodb.uri not found in System Properties.");
                return;
            }

            // 1. Standard Connection (No CodecRegistry needed for Document)
            mongoClient = MongoClients.create(uri);
            database = mongoClient.getDatabase("inventory_db_osgi");

            // 2. Initialize Collections as 'Document'
            salesOrderCollection = database.getCollection("sales_orders");
            salesOrderItemCollection = database.getCollection("sales_order_items");
            deliveryOrderCollection = database.getCollection("delivery_orders");
            salesReturnCollection = database.getCollection("sales_returns");

            System.out.println("✅ Sales Order Service: Database Connected (Manual Mode).");

        } catch (Exception e) {
            System.err.println("❌ Sales Order Service: Connection Failed.");
            e.printStackTrace();
        }
    }

    @Deactivate
    public void deactivate() {
        if (mongoClient != null) mongoClient.close();
        System.out.println("🛑 Sales Order Service: Stopped.");
    }

    // =================== MAPPING HELPERS ===================

    // --- SALES ORDER MAPPING ---
    private SalesOrder mapToSalesOrder(Document doc) {
        if (doc == null) return null;
        SalesOrder order = new SalesOrder();
        order.setId(doc.get("_id").toString());
        order.setOrderNumber(doc.getString("orderNumber"));
        
        // Parse LocalDate from string
        String orderDateStr = doc.getString("orderDate");
        if (orderDateStr != null) {
            order.setOrderDate(LocalDate.parse(orderDateStr));
        }
        
        order.setCustomerId(doc.getString("customerId"));
        order.setTaxId(doc.getString("taxId"));
        order.setOrderStatus(doc.getString("orderStatus"));
        order.setDescription(doc.getString("description"));
        order.setCreatedAt(doc.getString("createdAt"));
        order.setEditedAt(doc.getString("editedAt"));
        return order;
    }

    private Document mapFromSalesOrder(SalesOrder order) {
        Document doc = new Document()
                .append("orderNumber", order.getOrderNumber())
                .append("orderDate", order.getOrderDate() != null ? order.getOrderDate().toString() : null)
                .append("customerId", order.getCustomerId())
                .append("taxId", order.getTaxId())
                .append("orderStatus", order.getOrderStatus())
                .append("description", order.getDescription())
                .append("createdAt", order.getCreatedAt())
                .append("editedAt", order.getEditedAt());
        return doc;
    }

    // --- SALES ORDER ITEM MAPPING ---
    private SalesOrderItem mapToSalesOrderItem(Document doc) {
        if (doc == null) return null;
        SalesOrderItem item = new SalesOrderItem();
        item.setId(doc.get("_id").toString());
        item.setSalesOrderId(doc.getString("salesOrderId"));
        item.setProductId(doc.getString("productId"));
        
        // Parse BigDecimal
        Object unitPriceObj = doc.get("unitPrice");
        if (unitPriceObj != null) {
            if (unitPriceObj instanceof Double) {
                item.setUnitPrice(BigDecimal.valueOf((Double) unitPriceObj));
            } else if (unitPriceObj instanceof String) {
                item.setUnitPrice(new BigDecimal((String) unitPriceObj));
            }
        }
        
        item.setQuantity(doc.getInteger("quantity", 0));
        item.setProductNumber(doc.getString("productNumber"));
        item.setCreatedAt(doc.getString("createdAt"));
        item.setEditedAt(doc.getString("editedAt"));
        return item;
    }

    private Document mapFromSalesOrderItem(SalesOrderItem item) {
        return new Document()
                .append("salesOrderId", item.getSalesOrderId())
                .append("productId", item.getProductId())
                .append("unitPrice", item.getUnitPrice() != null ? item.getUnitPrice().toString() : null)
                .append("quantity", item.getQuantity())
                .append("productNumber", item.getProductNumber())
                .append("createdAt", item.getCreatedAt())
                .append("editedAt", item.getEditedAt());
    }

    // --- DELIVERY ORDER MAPPING ---
    private DeliveryOrder mapToDeliveryOrder(Document doc) {
        if (doc == null) return null;
        DeliveryOrder order = new DeliveryOrder();
        order.setId(doc.get("_id").toString());
        order.setDeliveryNumber(doc.getString("deliveryNumber"));
        
        String deliveryDateStr = doc.getString("deliveryDate");
        if (deliveryDateStr != null) {
            order.setDeliveryDate(LocalDate.parse(deliveryDateStr));
        }
        
        order.setSalesOrderId(doc.getString("salesOrderId"));
        order.setStatus(doc.getString("status"));
        order.setDescription(doc.getString("description"));
        order.setCreatedAt(doc.getString("createdAt"));
        order.setEditedAt(doc.getString("editedAt"));
        return order;
    }

    private Document mapFromDeliveryOrder(DeliveryOrder order) {
        return new Document()
                .append("deliveryNumber", order.getDeliveryNumber())
                .append("deliveryDate", order.getDeliveryDate() != null ? order.getDeliveryDate().toString() : null)
                .append("salesOrderId", order.getSalesOrderId())
                .append("status", order.getStatus())
                .append("description", order.getDescription())
                .append("createdAt", order.getCreatedAt())
                .append("editedAt", order.getEditedAt());
    }

    // --- SALES RETURN MAPPING ---
    private SalesReturn mapToSalesReturn(Document doc) {
        if (doc == null) return null;
        SalesReturn salesReturn = new SalesReturn();
        salesReturn.setId(doc.get("_id").toString());
        salesReturn.setReturnNumber(doc.getString("returnNumber"));
        
        String returnDateStr = doc.getString("returnDate");
        if (returnDateStr != null) {
            salesReturn.setReturnDate(LocalDate.parse(returnDateStr));
        }
        
        salesReturn.setDeliveryOrderId(doc.getString("deliveryOrderId"));
        salesReturn.setStatus(doc.getString("status"));
        salesReturn.setDescription(doc.getString("description"));
        salesReturn.setCreatedAt(doc.getString("createdAt"));
        salesReturn.setEditedAt(doc.getString("editedAt"));
        return salesReturn;
    }

    private Document mapFromSalesReturn(SalesReturn salesReturn) {
        return new Document()
                .append("returnNumber", salesReturn.getReturnNumber())
                .append("returnDate", salesReturn.getReturnDate() != null ? salesReturn.getReturnDate().toString() : null)
                .append("deliveryOrderId", salesReturn.getDeliveryOrderId())
                .append("status", salesReturn.getStatus())
                .append("description", salesReturn.getDescription())
                .append("createdAt", salesReturn.getCreatedAt())
                .append("editedAt", salesReturn.getEditedAt());
    }

    // =================== SALES ORDERS IMPLEMENTATION ===================

    @Override
    public SalesOrder createSalesOrder(SalesOrder order) {
        if (order.getCreatedAt() == null) {
            order.setCreatedAt(LocalDateTime.now().toString());
        }
        if (order.getOrderNumber() == null) {
            order.setOrderNumber(generateOrderNumber("SO"));
        }
        Document doc = mapFromSalesOrder(order);
        salesOrderCollection.insertOne(doc);
        
        // Set the generated ID back to the order
        order.setId(doc.get("_id").toString());
        return order;
    }

    @Override
    public List<SalesOrder> getAllSalesOrders() {
        List<SalesOrder> list = new ArrayList<>();
        for (Document doc : salesOrderCollection.find()) {
            list.add(mapToSalesOrder(doc));
        }
        return list;
    }

    @Override
    public Optional<SalesOrder> getSalesOrderById(String id) {
        try {
            Document doc = salesOrderCollection.find(Filters.eq("_id", new ObjectId(id))).first();
            return Optional.ofNullable(mapToSalesOrder(doc));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<SalesOrder> getSalesOrderByNumber(String orderNumber) {
        Document doc = salesOrderCollection.find(Filters.eq("orderNumber", orderNumber)).first();
        return Optional.ofNullable(mapToSalesOrder(doc));
    }

    @Override
    public void updateSalesOrder(SalesOrder order) {
        if (order.getId() != null) {
            order.setEditedAt(LocalDateTime.now().toString());
            Document doc = mapFromSalesOrder(order);
            salesOrderCollection.replaceOne(Filters.eq("_id", new ObjectId(order.getId())), doc);
        }
    }

    @Override
    public String deleteSalesOrder(String id) {
        // Check if there are delivery orders linked to this sales order
        long deliveryCount = deliveryOrderCollection.countDocuments(Filters.eq("salesOrderId", id));
        if (deliveryCount > 0) {
            return "Cannot delete: " + deliveryCount + " delivery order(s) are linked to this sales order.";
        }

        try {
            // Delete associated items first
            salesOrderItemCollection.deleteMany(Filters.eq("salesOrderId", id));
            
            // Delete the order
            DeleteResult result = salesOrderCollection.deleteOne(Filters.eq("_id", new ObjectId(id)));
            return result.getDeletedCount() > 0 ? "Sales Order deleted successfully." : "Sales Order not found.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // =================== SALES ORDER ITEMS IMPLEMENTATION ===================

    @Override
    public void addSalesOrderItem(SalesOrderItem item) {
        if (item.getCreatedAt() == null) {
            item.setCreatedAt(LocalDateTime.now().toString());
        }
        Document doc = mapFromSalesOrderItem(item);
        salesOrderItemCollection.insertOne(doc);
    }

    @Override
    public List<SalesOrderItem> getItemsByOrderId(String orderId) {
        List<SalesOrderItem> list = new ArrayList<>();
        for (Document doc : salesOrderItemCollection.find(Filters.eq("salesOrderId", orderId))) {
            list.add(mapToSalesOrderItem(doc));
        }
        return list;
    }

    @Override
    public void updateSalesOrderItem(SalesOrderItem item) {
        if (item.getId() != null) {
            item.setEditedAt(LocalDateTime.now().toString());
            Document doc = mapFromSalesOrderItem(item);
            salesOrderItemCollection.replaceOne(Filters.eq("_id", new ObjectId(item.getId())), doc);
        }
    }

    @Override
    public void deleteSalesOrderItem(String id) {
        try {
            salesOrderItemCollection.deleteOne(Filters.eq("_id", new ObjectId(id)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =================== DELIVERY ORDERS IMPLEMENTATION ===================

    @Override
    public DeliveryOrder createDeliveryOrder(DeliveryOrder order) {
        if (order.getCreatedAt() == null) {
            order.setCreatedAt(LocalDateTime.now().toString());
        }
        if (order.getDeliveryNumber() == null) {
            order.setDeliveryNumber(generateOrderNumber("DO"));
        }
        Document doc = mapFromDeliveryOrder(order);
        deliveryOrderCollection.insertOne(doc);
        
        order.setId(doc.get("_id").toString());
        return order;
    }

    @Override
    public List<DeliveryOrder> getAllDeliveryOrders() {
        List<DeliveryOrder> list = new ArrayList<>();
        for (Document doc : deliveryOrderCollection.find()) {
            list.add(mapToDeliveryOrder(doc));
        }
        return list;
    }

    @Override
    public Optional<DeliveryOrder> getDeliveryOrderById(String id) {
        try {
            Document doc = deliveryOrderCollection.find(Filters.eq("_id", new ObjectId(id))).first();
            return Optional.ofNullable(mapToDeliveryOrder(doc));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<DeliveryOrder> getDeliveryOrderByNumber(String deliveryNumber) {
        Document doc = deliveryOrderCollection.find(Filters.eq("deliveryNumber", deliveryNumber)).first();
        return Optional.ofNullable(mapToDeliveryOrder(doc));
    }

    @Override
    public void updateDeliveryOrder(DeliveryOrder order) {
        if (order.getId() != null) {
            order.setEditedAt(LocalDateTime.now().toString());
            Document doc = mapFromDeliveryOrder(order);
            deliveryOrderCollection.replaceOne(Filters.eq("_id", new ObjectId(order.getId())), doc);
        }
    }

    @Override
    public String deleteDeliveryOrder(String id) {
        // Check if there are sales returns linked to this delivery order
        long returnCount = salesReturnCollection.countDocuments(Filters.eq("deliveryOrderId", id));
        if (returnCount > 0) {
            return "Cannot delete: " + returnCount + " sales return(s) are linked to this delivery order.";
        }

        try {
            DeleteResult result = deliveryOrderCollection.deleteOne(Filters.eq("_id", new ObjectId(id)));
            return result.getDeletedCount() > 0 ? "Delivery Order deleted successfully." : "Delivery Order not found.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // =================== SALES RETURNS IMPLEMENTATION ===================

    @Override
    public SalesReturn createSalesReturn(SalesReturn salesReturn) {
        if (salesReturn.getCreatedAt() == null) {
            salesReturn.setCreatedAt(LocalDateTime.now().toString());
        }
        if (salesReturn.getReturnNumber() == null) {
            salesReturn.setReturnNumber(generateOrderNumber("SR"));
        }
        Document doc = mapFromSalesReturn(salesReturn);
        salesReturnCollection.insertOne(doc);
        
        salesReturn.setId(doc.get("_id").toString());
        return salesReturn;
    }

    @Override
    public List<SalesReturn> getAllSalesReturns() {
        List<SalesReturn> list = new ArrayList<>();
        for (Document doc : salesReturnCollection.find()) {
            list.add(mapToSalesReturn(doc));
        }
        return list;
    }

    @Override
    public Optional<SalesReturn> getSalesReturnById(String id) {
        try {
            Document doc = salesReturnCollection.find(Filters.eq("_id", new ObjectId(id))).first();
            return Optional.ofNullable(mapToSalesReturn(doc));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<SalesReturn> getSalesReturnByNumber(String returnNumber) {
        Document doc = salesReturnCollection.find(Filters.eq("returnNumber", returnNumber)).first();
        return Optional.ofNullable(mapToSalesReturn(doc));
    }

    @Override
    public void updateSalesReturn(SalesReturn salesReturn) {
        if (salesReturn.getId() != null) {
            salesReturn.setEditedAt(LocalDateTime.now().toString());
            Document doc = mapFromSalesReturn(salesReturn);
            salesReturnCollection.replaceOne(Filters.eq("_id", new ObjectId(salesReturn.getId())), doc);
        }
    }

    @Override
    public String deleteSalesReturn(String id) {
        try {
            DeleteResult result = salesReturnCollection.deleteOne(Filters.eq("_id", new ObjectId(id)));
            return result.getDeletedCount() > 0 ? "Sales Return deleted successfully." : "Sales Return not found.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // =================== HELPER METHODS ===================

    @Override
    public String getCustomerNameById(String customerId) {
        if (customerId == null || customerService == null) return "Unknown";
        Optional<Customer> customer = customerService.getCustomerById(customerId);
        return customer.map(Customer::getName).orElse("Unknown");
    }

    @Override
    public String getCustomerIdByName(String customerName) {
        if (customerName == null || customerService == null) return null;
        Optional<Customer> customer = customerService.getCustomerByName(customerName);
        return customer.map(Customer::getId).orElse(null);
    }

    // TODO: Uncomment when Product module is ready
    @Override
    public String getProductNameById(String productId) {
        // Temporary implementation until Product service is available
        if (productId == null) return "Unknown";
        return "Product-" + productId.substring(0, Math.min(8, productId.length()));
        
        // TODO: Replace with actual implementation:
        // if (productId == null || productService == null) return "Unknown";
        // Optional<Product> product = productService.getProductById(productId);
        // return product.map(Product::getProductName).orElse("Unknown");
    }

    // TODO: Uncomment when Product module is ready
    @Override
    public String getProductIdByName(String productName) {
        // Temporary implementation until Product service is available
        System.out.println("⚠️ Warning: Product service not available. Product lookup will fail.");
        return null;
        
        // TODO: Replace with actual implementation:
        // if (productName == null || productService == null) return null;
        // Optional<Product> product = productService.getProductByName(productName);
        // return product.map(Product::getId).orElse(null);
    }

    @Override
    public BigDecimal getTaxRateById(String taxId) {
        // TODO: Implement when Tax service is available
        // For now, return a default value
        return BigDecimal.valueOf(10); // Default 10%
    }

    @Override
    public String getSalesOrderNumberById(String id) {
        Optional<SalesOrder> order = getSalesOrderById(id);
        return order.map(SalesOrder::getOrderNumber).orElse("Unknown");
    }

    @Override
    public String getDeliveryOrderNumberById(String id) {
        Optional<DeliveryOrder> order = getDeliveryOrderById(id);
        return order.map(DeliveryOrder::getDeliveryNumber).orElse("Unknown");
    }

    // =================== UTILITY METHODS ===================

    private String generateOrderNumber(String prefix) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return prefix + "-" + timestamp + "-" + random;
    }
}