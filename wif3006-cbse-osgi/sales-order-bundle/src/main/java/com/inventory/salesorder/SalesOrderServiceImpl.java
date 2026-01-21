package com.inventory.salesorder;

import com.inventory.api.salesorder.service.SalesOrderService;
import com.inventory.api.salesorder.model.SalesOrder;
import com.inventory.api.salesorder.model.SalesOrderItem;
import com.inventory.api.salesorder.model.DeliveryOrder;
import com.inventory.api.salesorder.model.SalesReturn;
import com.inventory.api.salesorder.model.Tax;
import com.inventory.api.customer.service.CustomerService;
import com.inventory.api.customer.model.Customer;
import com.inventory.api.product.service.ProductService;
import com.inventory.api.product.model.Product;

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
import java.util.Date;
@Component(service = SalesOrderService.class)
public class SalesOrderServiceImpl implements SalesOrderService {

    private MongoClient mongoClient;
    private MongoDatabase database;

    private MongoCollection<Document> salesOrderCollection;
    private MongoCollection<Document> salesOrderItemCollection;
    private MongoCollection<Document> deliveryOrderCollection;
    private MongoCollection<Document> salesReturnCollection;
    private MongoCollection<Document> taxCollection;

    // Service References (for cross-module queries)
    @Reference
    private CustomerService customerService;

    @Reference
    private ProductService productService;

    @Activate
    public void activate() {
        System.out.println("Sales Order Service: Starting with MANUAL Mapping...");
        try {
            String uri = System.getProperty("mongodb.uri");
            if (uri == null || uri.isEmpty()) {
                System.err.println("Error: mongodb.uri not found in System Properties.");
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
            taxCollection = database.getCollection("taxes");

            System.out.println("Sales Order Service: Database Connected (Manual Mode).");

        } catch (Exception e) {
            System.err.println("Sales Order Service: Connection Failed.");
            e.printStackTrace();
        }
    }

    @Deactivate
    public void deactivate() {
        if (mongoClient != null) mongoClient.close();
        System.out.println("Sales Order Service: Stopped.");
    }

    // =================== MAPPING HELPERS ===================

    // --- SALES ORDER MAPPING ---
    private SalesOrder mapToSalesOrder(Document doc) {
        if (doc == null) return null;
        SalesOrder order = new SalesOrder();
        order.setId(doc.get("_id").toString());
        order.setOrderNumber(doc.getString("orderNumber"));
        
        // Parse LocalDate from string
        Object orderDateObj = doc.get("orderDate");
        if (orderDateObj != null) {
            if (orderDateObj instanceof Date) {
                // Convert java.util.Date to LocalDate
                Date date = (Date) orderDateObj;
                order.setOrderDate(date.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate());
            } else if (orderDateObj instanceof String) {
                // Parse String to LocalDate
                order.setOrderDate(LocalDate.parse((String) orderDateObj));
            }
        }
        
        order.setCustomerId(doc.getString("customerId"));
        order.setTaxId(doc.getString("taxId"));
        order.setOrderStatus(doc.getString("orderStatus"));
        order.setDescription(doc.getString("description"));
        
        // Parse BigDecimal amounts
        Object beforeTaxObj = doc.get("beforeTaxAmount");
        if (beforeTaxObj != null) {
            if (beforeTaxObj instanceof Double) {
                order.setBeforeTaxAmount(BigDecimal.valueOf((Double) beforeTaxObj));
            } else if (beforeTaxObj instanceof String) {
                order.setBeforeTaxAmount(new BigDecimal((String) beforeTaxObj));
            }
        }
        
        Object taxAmountObj = doc.get("taxAmount");
        if (taxAmountObj != null) {
            if (taxAmountObj instanceof Double) {
                order.setTaxAmount(BigDecimal.valueOf((Double) taxAmountObj));
            } else if (taxAmountObj instanceof String) {
                order.setTaxAmount(new BigDecimal((String) taxAmountObj));
            }
        }
        
        Object afterTaxObj = doc.get("afterTaxAmount");
        if (afterTaxObj != null) {
            if (afterTaxObj instanceof Double) {
                order.setAfterTaxAmount(BigDecimal.valueOf((Double) afterTaxObj));
            } else if (afterTaxObj instanceof String) {
                order.setAfterTaxAmount(new BigDecimal((String) afterTaxObj));
            }
        }
        
        order.setCreatedAt(doc.getString("createdAt"));
        order.setEditedAt(doc.getString("editedAt"));
        return order;
    }

    private Document mapFromSalesOrder(SalesOrder order) {
        return new Document()
                .append("orderNumber", order.getOrderNumber())
                .append("orderDate", order.getOrderDate() != null ? order.getOrderDate().toString() : null)
                .append("customerId", order.getCustomerId())
                .append("taxId", order.getTaxId())
                .append("orderStatus", order.getOrderStatus())
                .append("description", order.getDescription())
                .append("beforeTaxAmount", order.getBeforeTaxAmount() != null ? order.getBeforeTaxAmount().toString() : "0")
                .append("taxAmount", order.getTaxAmount() != null ? order.getTaxAmount().toString() : "0")
                .append("afterTaxAmount", order.getAfterTaxAmount() != null ? order.getAfterTaxAmount().toString() : "0")
                .append("createdAt", order.getCreatedAt())
                .append("editedAt", order.getEditedAt());
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

    // --- TAX MAPPING ---
    private Tax mapToTax(Document doc) {
        if (doc == null) return null;
        Tax tax = new Tax();

        Object idObj = doc.get("_id");
        if (idObj != null) {
            tax.setId(idObj.toString());
        }

        tax.setTaxName(doc.getString("taxName"));
    
        // Parse BigDecimal for taxRate
        Object taxRateObj = doc.get("taxRate");
        if (taxRateObj != null) {
            if (taxRateObj instanceof Integer) {
                tax.setTaxRate(BigDecimal.valueOf((Integer) taxRateObj));
            } else if (taxRateObj instanceof Double) {
                tax.setTaxRate(BigDecimal.valueOf((Double) taxRateObj));
            } else if (taxRateObj instanceof String) {
                tax.setTaxRate(new BigDecimal((String) taxRateObj));
            }
        }
    
        tax.setDescription(doc.getString("description"));
        tax.setCreatedAt(doc.getString("createdAt"));
        tax.setEditedAt(doc.getString("editedAt"));
        return tax;
    }

    private Document mapFromTax(Tax tax) {
        return new Document()
                .append("taxName", tax.getTaxName())
                .append("taxRate", tax.getTaxRate() != null ? tax.getTaxRate().toString() : null)
                .append("description", tax.getDescription())
                .append("createdAt", tax.getCreatedAt())
                .append("editedAt", tax.getEditedAt());
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
        try {
            for (Document doc : salesOrderCollection.find()) {
                SalesOrder order = mapToSalesOrder(doc);
                list.add(order);
            }
        } catch (Exception e) {
            System.err.println("DEBUG SERVICE: Exception in getAllSalesOrders!");
            e.printStackTrace();
            throw e;
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
        
        // Recalculate order totals after adding item
        recalculateOrderTotals(item.getSalesOrderId());
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
            
            // Recalculate order totals after updating item
            recalculateOrderTotals(item.getSalesOrderId());
        }
    }

    @Override
    public void deleteSalesOrderItem(String id) {
        try {
            // Get the item first to find the order ID
            Document doc = salesOrderItemCollection.find(Filters.eq("_id", new ObjectId(id))).first();
            if (doc != null) {
                String orderId = doc.getString("salesOrderId");
                salesOrderItemCollection.deleteOne(Filters.eq("_id", new ObjectId(id)));
                
                // Recalculate order totals after deleting item
                recalculateOrderTotals(orderId);
            }
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

    @Override
    public String getProductNameById(String productId) {
        if (productId == null || productService == null) return "Unknown";
        Product product = productService.getProduct(productId);
        return product != null ? product.getName() : "Unknown";
    }

    @Override
    public String getProductIdByName(String productName) {
        if (productName == null || productService == null) return null;
        
        // Search through all products to find matching name
        List<Product> products = productService.getAllProducts();
        for (Product product : products) {
            if (product.getName().equalsIgnoreCase(productName)) {
                return product.getId();
            }
        }
        return null;
    }

    public Optional<Tax> getTaxById(String id) {
        try {
            Document doc = taxCollection.find(Filters.eq("_id", id)).first();
            return Optional.ofNullable(mapToTax(doc));
        } catch (IllegalArgumentException e) {
            System.err.println("Error finding tax by id: " + id);
            return Optional.empty();
        }
    }

    @Override
    public List<Tax> getAllTaxes() {
        List<Tax> list = new ArrayList<>();
        for (Document doc : taxCollection.find()) {
            list.add(mapToTax(doc));
        }
        return list;
    }

    @Override
    public BigDecimal getTaxRateById(String taxId) {
        if (taxId == null) return BigDecimal.ZERO;
    
        Optional<Tax> tax = getTaxById(taxId);
        return tax.map(Tax::getTaxRate).orElse(BigDecimal.ZERO);
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return prefix + "-" + LocalDateTime.now().format(formatter);
    }
    
    /**
     * Recalculate order totals (before tax, tax amount, after tax)
     * Called whenever items are added, updated, or deleted
     */
    @Override
    public void recalculateOrderTotals(String orderId) {
        try {
            Optional<SalesOrder> orderOpt = getSalesOrderById(orderId);
            if (orderOpt.isEmpty()) return;
            
            SalesOrder order = orderOpt.get();
            List<SalesOrderItem> items = getItemsByOrderId(orderId);
            
            // Calculate subtotal (before tax)
            BigDecimal beforeTax = BigDecimal.ZERO;
            for (SalesOrderItem item : items) {
                BigDecimal itemTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                beforeTax = beforeTax.add(itemTotal);
            }
            
            // Calculate tax amount
            BigDecimal taxAmount = BigDecimal.ZERO;
            if (order.getTaxId() != null) {
                BigDecimal taxRate = getTaxRateById(order.getTaxId());
                taxAmount = beforeTax.multiply(taxRate).divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
            }
            
            // Calculate after tax amount
            BigDecimal afterTax = beforeTax.add(taxAmount);
            
            // Update order with calculated amounts
            order.setBeforeTaxAmount(beforeTax);
            order.setTaxAmount(taxAmount);
            order.setAfterTaxAmount(afterTax);
            
            updateSalesOrder(order);
            
        } catch (Exception e) {
            System.err.println("Error recalculating order totals: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get product price by ID
     */
    @Override
    public BigDecimal getProductPriceById(String productId) {
        if (productId == null || productService == null) return BigDecimal.ZERO;
        Product product = productService.getProduct(productId);
        return product != null ? BigDecimal.valueOf(product.getPrice()) : BigDecimal.ZERO;
    }

    @Override
    public Optional<Tax> getTaxByName(String taxName) {
        Document doc = taxCollection.find(Filters.eq("taxName", taxName)).first();
        return Optional.ofNullable(mapToTax(doc));
    }
}