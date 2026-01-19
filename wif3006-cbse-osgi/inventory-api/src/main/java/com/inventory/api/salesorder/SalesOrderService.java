package com.inventory.api.salesorder;

import java.util.List;

public interface SalesOrderService {
    // View all sales orders
    List<SalesOrder> getAllSalesOrders();
    
    // Get a specific sales order by ID
    SalesOrder getSalesOrderById(int orderId);
    
    // Create a new sales order
    SalesOrder addSalesOrder(SalesOrder salesOrder);
    
    // Update an existing sales order (e.g., change status, edit items)
    SalesOrder updateSalesOrder(int orderId, SalesOrder salesOrder);
    
    // Delete a sales order
    boolean deleteSalesOrder(int orderId);
    
    // Check if sales order exists
    boolean salesOrderExists(int orderId);
    
    // Generate next available ID (helper for simple in-memory storage)
    int getNextOrderId();
    
}