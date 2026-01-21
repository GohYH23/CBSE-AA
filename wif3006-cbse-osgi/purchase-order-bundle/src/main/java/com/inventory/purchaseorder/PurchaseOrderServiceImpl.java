// Name: Ooi Wei Ying
// Student ID: 22056924

package com.inventory.purchaseorder;

import com.inventory.api.purchaseorder.model.PurchaseOrder;
import com.inventory.api.purchaseorder.service.PurchaseOrderService;
import com.inventory.api.purchaseorder.model.OrderItem;
import org.bson.Document;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

@Component(service = PurchaseOrderService.class, immediate = true)
public class PurchaseOrderServiceImpl implements PurchaseOrderService {
    
    private PurchaseOrderMenu currentMenu;
    
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> purchaseOrderCollection;
    
    @Activate
    public void activate() {
        System.out.println("✅ Purchase Order Component Started.");
        try {
            String uri = System.getProperty("mongodb.uri");
            if (uri == null || uri.isEmpty()) {
                System.err.println("❌ Error: mongodb.uri not found in System Properties.");
                return;
            }
            
            mongoClient = MongoClients.create(uri);
            database = mongoClient.getDatabase("inventory_db_osgi");
            purchaseOrderCollection = database.getCollection("purchase_orders");
            
            System.out.println("   ✅ Connected to MongoDB: inventory_db_osgi");
            System.out.println("   Loaded " + purchaseOrderCollection.countDocuments() + " purchase order(s) from database.");
        } catch (Exception e) {
            System.err.println("   ❌ MongoDB Connection Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Deactivate
    public void deactivate() {
        System.out.println("❌ Stopping Purchase Order Component...");
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
    
    // =================== DOCUMENT MAPPING METHODS ===================
    
    private PurchaseOrder mapToPurchaseOrder(Document doc) {
        if (doc == null) return null;
        
        PurchaseOrder po = new PurchaseOrder();
        po.setOrderId(doc.getInteger("orderId", 0));
        
        String orderDateStr = doc.getString("orderDate");
        if (orderDateStr != null && !orderDateStr.isEmpty()) {
            po.setOrderDate(LocalDate.parse(orderDateStr));
        }
        
        po.setOrderNumber(doc.getString("orderNumber"));
        po.setVendor(doc.getString("vendor"));
        po.setOrderStatus(doc.getString("orderStatus"));
        
        String receivedDateStr = doc.getString("receivedDate");
        if (receivedDateStr != null && !receivedDateStr.isEmpty()) {
            po.setReceivedDate(LocalDate.parse(receivedDateStr));
        }
        
        String returnedDateStr = doc.getString("returnedDate");
        if (returnedDateStr != null && !returnedDateStr.isEmpty()) {
            po.setReturnedDate(LocalDate.parse(returnedDateStr));
        }
        
        String shippingDateStr = doc.getString("shippingDate");
        if (shippingDateStr != null && !shippingDateStr.isEmpty()) {
            po.setShippingDate(LocalDate.parse(shippingDateStr));
        }
        
        String cancelledDateStr = doc.getString("cancelledDate");
        if (cancelledDateStr != null && !cancelledDateStr.isEmpty()) {
            po.setCancelledDate(LocalDate.parse(cancelledDateStr));
        }
        
        // Map OrderItems (nested documents)
        @SuppressWarnings("unchecked")
        List<Document> itemsDocs = (List<Document>) doc.get("orderItems");
        List<OrderItem> orderItems = new ArrayList<>();
        if (itemsDocs != null) {
            for (Document itemDoc : itemsDocs) {
                OrderItem item = new OrderItem(
                    itemDoc.getString("itemName"),
                    itemDoc.getInteger("quantity", 0),
                    itemDoc.getDouble("pricePerItem")
                );
                orderItems.add(item);
            }
        }
        po.setOrderItems(orderItems);
        
        return po;
    }
    
    private Document mapFromPurchaseOrder(PurchaseOrder po) {
        Document doc = new Document()
            .append("orderId", po.getOrderId())
            .append("orderDate", po.getOrderDate() != null ? po.getOrderDate().toString() : null)
            .append("orderNumber", po.getOrderNumber())
            .append("vendor", po.getVendor())
            .append("orderStatus", po.getOrderStatus())
            .append("receivedDate", po.getReceivedDate() != null ? po.getReceivedDate().toString() : null)
            .append("returnedDate", po.getReturnedDate() != null ? po.getReturnedDate().toString() : null)
            .append("shippingDate", po.getShippingDate() != null ? po.getShippingDate().toString() : null)
            .append("cancelledDate", po.getCancelledDate() != null ? po.getCancelledDate().toString() : null);
        
        // Map OrderItems as nested documents
        List<Document> itemsDocs = new ArrayList<>();
        if (po.getOrderItems() != null) {
            for (OrderItem item : po.getOrderItems()) {
                Document itemDoc = new Document()
                    .append("itemName", item.getItemName())
                    .append("quantity", item.getQuantity())
                    .append("pricePerItem", item.getPricePerItem());
                itemsDocs.add(itemDoc);
            }
        }
        doc.append("orderItems", itemsDocs);
        
        return doc;
    }
    
    // =================== SERVICE IMPLEMENTATION ===================
    
    @Override
    public List<PurchaseOrder> getAllPurchaseOrders() {
        List<PurchaseOrder> orders = new ArrayList<>();
        if (purchaseOrderCollection == null) return orders;
        
        for (Document doc : purchaseOrderCollection.find()) {
            orders.add(mapToPurchaseOrder(doc));
        }
        return orders;
    }
    
    @Override
    public PurchaseOrder getPurchaseOrderById(int orderId) {
        if (purchaseOrderCollection == null) return null;
        
        Document doc = purchaseOrderCollection.find(Filters.eq("orderId", orderId)).first();
        return mapToPurchaseOrder(doc);
    }
    
    @Override
    public PurchaseOrder addPurchaseOrder(PurchaseOrder purchaseOrder) {
        if (purchaseOrderCollection == null) return null;
        
        // Generate ID if not set
        if (purchaseOrder.getOrderId() == 0) {
            purchaseOrder.setOrderId(getNextOrderId());
        }
        
        // Auto-generate order number if not set
        if (purchaseOrder.getOrderNumber() == null || purchaseOrder.getOrderNumber().isEmpty()) {
            purchaseOrder.setOrderNumber(String.format("PO-%03d", purchaseOrder.getOrderId()));
        }
        
        Document doc = mapFromPurchaseOrder(purchaseOrder);
        purchaseOrderCollection.insertOne(doc);
        return purchaseOrder;
    }
    
    @Override
    public PurchaseOrder updatePurchaseOrder(int orderId, PurchaseOrder updatedOrder) {
        if (purchaseOrderCollection == null) return null;
        
        PurchaseOrder existingOrder = getPurchaseOrderById(orderId);
        if (existingOrder == null) {
            return null; // Not found
        }
        
        updatedOrder.setOrderId(orderId); // Ensure ID matches
        
        String newStatus = updatedOrder.getOrderStatus().toLowerCase();
        String oldStatus = existingOrder.getOrderStatus().toLowerCase();
        
        // Handle status transitions and date management
        
        // 1. Handle received/returned status (from Goods Receive/Return modules)
        if (newStatus.equals("received")) {
            if (oldStatus.equals("received")) {
                updatedOrder.setReceivedDate(existingOrder.getReceivedDate());
            } else {
                updatedOrder.setReceivedDate(LocalDate.now());
            }
            updatedOrder.setReturnedDate(null);
            // Keep shipping date if exists
            updatedOrder.setShippingDate(existingOrder.getShippingDate());
            updatedOrder.setCancelledDate(existingOrder.getCancelledDate());
        } else if (newStatus.equals("returned")) {
            if (oldStatus.equals("returned")) {
                updatedOrder.setReturnedDate(existingOrder.getReturnedDate());
            } else {
                updatedOrder.setReturnedDate(LocalDate.now());
            }
            updatedOrder.setReceivedDate(existingOrder.getReceivedDate());
            // Keep shipping date if exists
            updatedOrder.setShippingDate(existingOrder.getShippingDate());
            updatedOrder.setCancelledDate(existingOrder.getCancelledDate());
        } 
        // 2. Handle shipping status
        else if (newStatus.equals("shipping")) {
            if (!oldStatus.equals("shipping")) {
                // Status changed to shipping - set shipping date to today
                updatedOrder.setShippingDate(LocalDate.now());
            } else {
                // Already shipping - keep existing shipping date
                updatedOrder.setShippingDate(existingOrder.getShippingDate());
            }
            // Clear cancelled date (can't be cancelled and shipping at same time)
            updatedOrder.setCancelledDate(null);
            // Keep received/returned dates if they exist
            updatedOrder.setReceivedDate(existingOrder.getReceivedDate());
            updatedOrder.setReturnedDate(existingOrder.getReturnedDate());
        }
        // 3. Handle pending status
        else if (newStatus.equals("pending")) {
            if (oldStatus.equals("shipping")) {
                // Changed from shipping to pending - clear shipping date
                updatedOrder.setShippingDate(null);
            } else {
                // Keep existing shipping date if not from shipping
                updatedOrder.setShippingDate(existingOrder.getShippingDate());
            }
            // Clear cancelled date (can't be cancelled and pending at same time)
            updatedOrder.setCancelledDate(null);
            // Keep received/returned dates if they exist
            updatedOrder.setReceivedDate(existingOrder.getReceivedDate());
            updatedOrder.setReturnedDate(existingOrder.getReturnedDate());
        }
        // 4. Handle cancelled status
        else if (newStatus.equals("cancelled")) {
            if (!oldStatus.equals("cancelled")) {
                // Status changed to cancelled - set cancelled date to today
                updatedOrder.setCancelledDate(LocalDate.now());
            } else {
                // Already cancelled - keep existing cancelled date
                updatedOrder.setCancelledDate(existingOrder.getCancelledDate());
            }
            // Clear shipping date (can't be cancelled and shipping at same time)
            updatedOrder.setShippingDate(null);
            // Keep received/returned dates if they exist
            updatedOrder.setReceivedDate(existingOrder.getReceivedDate());
            updatedOrder.setReturnedDate(existingOrder.getReturnedDate());
        }
        // 5. Default case (should not happen, but keep existing dates)
        else {
            updatedOrder.setReceivedDate(existingOrder.getReceivedDate());
            updatedOrder.setReturnedDate(existingOrder.getReturnedDate());
            updatedOrder.setShippingDate(existingOrder.getShippingDate());
            updatedOrder.setCancelledDate(existingOrder.getCancelledDate());
        }
        
        Document doc = mapFromPurchaseOrder(updatedOrder);
        purchaseOrderCollection.replaceOne(Filters.eq("orderId", orderId), doc);
        return updatedOrder;
    }
    
    // Get purchase orders by status
    public List<PurchaseOrder> getPurchaseOrdersByStatus(String status) {
        if (purchaseOrderCollection == null) return new ArrayList<>();
        
        List<PurchaseOrder> orders = new ArrayList<>();
        for (Document doc : purchaseOrderCollection.find(Filters.eq("orderStatus", status))) {
            orders.add(mapToPurchaseOrder(doc));
        }
        return orders;
    }
    
    @Override
    public boolean deletePurchaseOrder(int orderId) {
        if (purchaseOrderCollection == null) return false;
        
        try {
            var result = purchaseOrderCollection.deleteOne(Filters.eq("orderId", orderId));
            return result.getDeletedCount() > 0;
        } catch (Exception e) {
            System.err.println("Error deleting purchase order: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean purchaseOrderExists(int orderId) {
        if (purchaseOrderCollection == null) return false;
        
        return purchaseOrderCollection.countDocuments(Filters.eq("orderId", orderId)) > 0;
    }
    
    @Override
    public int getNextOrderId() {
        if (purchaseOrderCollection == null) return 1;
        
        // Find maximum orderId
        int maxId = 0;
        for (Document doc : purchaseOrderCollection.find()) {
            Integer id = doc.getInteger("orderId");
            if (id != null && id > maxId) {
                maxId = id;
            }
        }
        return maxId + 1;
    }
    
    // Method to show menu (called from main menu)
    public void showPurchaseOrderMenu(Scanner scanner) {
        if (currentMenu != null) {
            currentMenu.setRunning(true);
        } else {
            currentMenu = new PurchaseOrderMenu(this, scanner);
        }
        currentMenu.showPurchaseOrderMenu();
    }
    
    // Method to show Goods Receive menu (called from main menu)
    public void showGoodsReceiveMenu(Scanner scanner) {
        GoodsReceiveMenu goodsReceiveMenu = new GoodsReceiveMenu(this, scanner);
        goodsReceiveMenu.showGoodsReceiveMenu();
    }
    
    // Method to show Purchase Return menu (called from main menu)
    public void showPurchaseReturnMenu(Scanner scanner) {
        PurchaseReturnMenu purchaseReturnMenu = new PurchaseReturnMenu(this, scanner);
        purchaseReturnMenu.showPurchaseReturnMenu();
    }
}
