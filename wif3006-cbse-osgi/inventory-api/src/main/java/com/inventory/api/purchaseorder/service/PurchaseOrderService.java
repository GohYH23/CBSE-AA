package com.inventory.api.purchaseorder.service;

import com.inventory.api.purchaseorder.model.PurchaseOrder;
import java.util.List;

public interface PurchaseOrderService {
    // View all purchase orders
    List<PurchaseOrder> getAllPurchaseOrders();
    
    // Get a specific purchase order by ID
    PurchaseOrder getPurchaseOrderById(int orderId);
    
    // Add a new purchase order
    PurchaseOrder addPurchaseOrder(PurchaseOrder purchaseOrder);
    
    // Update an existing purchase order
    PurchaseOrder updatePurchaseOrder(int orderId, PurchaseOrder purchaseOrder);
    
    // Delete a purchase order
    boolean deletePurchaseOrder(int orderId);
    
    // Check if purchase order exists
    boolean purchaseOrderExists(int orderId);
    
    // Get next available order ID
    int getNextOrderId();
}
