package com.inventory.api.salesorder.service;

import com.inventory.api.salesorder.model.SalesOrder;
import com.inventory.api.salesorder.model.SalesOrderItem;
import com.inventory.api.salesorder.model.DeliveryOrder;
import com.inventory.api.salesorder.model.SalesReturn;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SalesOrderService {

    // ================= SALES ORDERS =================
    
    /**
     * Create a new sales order
     * @param order the sales order to create
     * @return the created sales order with generated ID
     */
    SalesOrder createSalesOrder(SalesOrder order);
    
    /**
     * Get all sales orders
     * @return list of all sales orders
     */
    List<SalesOrder> getAllSalesOrders();
    
    /**
     * Get sales order by ID
     * @param id the sales order ID
     * @return Optional containing the sales order if found
     */
    Optional<SalesOrder> getSalesOrderById(String id);
    
    /**
     * Get sales order by order number
     * @param orderNumber the order number
     * @return Optional containing the sales order if found
     */
    Optional<SalesOrder> getSalesOrderByNumber(String orderNumber);
    
    /**
     * Update an existing sales order
     * @param order the sales order to update
     */
    void updateSalesOrder(SalesOrder order);
    
    /**
     * Delete a sales order by ID
     * @param id the sales order ID
     * @return message indicating success or failure
     */
    String deleteSalesOrder(String id);

    // ================= SALES ORDER ITEMS =================

    /**
     * Add an item to a sales order
     * @param item the sales order item to add
     */
    void addSalesOrderItem(SalesOrderItem item);
    
    /**
     * Get all items for a specific sales order
     * @param orderId the sales order ID
     * @return list of items belonging to the order
     */
    List<SalesOrderItem> getItemsByOrderId(String orderId);
    
    /**
     * Update a sales order item
     * @param item the item to update
     */
    void updateSalesOrderItem(SalesOrderItem item);
    
    /**
     * Delete a sales order item by ID
     * @param id the item ID
     */
    void deleteSalesOrderItem(String id);

    // ================= DELIVERY ORDERS =================

    /**
     * Create a new delivery order
     * @param order the delivery order to create
     * @return the created delivery order with generated ID
     */
    DeliveryOrder createDeliveryOrder(DeliveryOrder order);
    
    /**
     * Get all delivery orders
     * @return list of all delivery orders
     */
    List<DeliveryOrder> getAllDeliveryOrders();
    
    /**
     * Get delivery order by ID
     * @param id the delivery order ID
     * @return Optional containing the delivery order if found
     */
    Optional<DeliveryOrder> getDeliveryOrderById(String id);
    
    /**
     * Get delivery order by delivery number
     * @param deliveryNumber the delivery number
     * @return Optional containing the delivery order if found
     */
    Optional<DeliveryOrder> getDeliveryOrderByNumber(String deliveryNumber);
    
    /**
     * Update an existing delivery order
     * @param order the delivery order to update
     */
    void updateDeliveryOrder(DeliveryOrder order);
    
    /**
     * Delete a delivery order by ID
     * @param id the delivery order ID
     * @return message indicating success or failure
     */
    String deleteDeliveryOrder(String id);

    // ================= SALES RETURNS =================

    /**
     * Create a new sales return
     * @param salesReturn the sales return to create
     * @return the created sales return with generated ID
     */
    SalesReturn createSalesReturn(SalesReturn salesReturn);
    
    /**
     * Get all sales returns
     * @return list of all sales returns
     */
    List<SalesReturn> getAllSalesReturns();
    
    /**
     * Get sales return by ID
     * @param id the sales return ID
     * @return Optional containing the sales return if found
     */
    Optional<SalesReturn> getSalesReturnById(String id);
    
    /**
     * Get sales return by return number
     * @param returnNumber the return number
     * @return Optional containing the sales return if found
     */
    Optional<SalesReturn> getSalesReturnByNumber(String returnNumber);
    
    /**
     * Update an existing sales return
     * @param salesReturn the sales return to update
     */
    void updateSalesReturn(SalesReturn salesReturn);
    
    /**
     * Delete a sales return by ID
     * @param id the sales return ID
     * @return message indicating success or failure
     */
    String deleteSalesReturn(String id);

    // ================= HELPER METHODS (for cross-module lookups) =================

    /**
     * Get customer name by ID (calls Customer Service)
     * @param customerId the customer ID
     * @return customer name or "Unknown" if not found
     */
    String getCustomerNameById(String customerId);

    /**
     * Get customer ID by name (calls Customer Service)
     * @param customerName the customer name
     * @return customer ID or null if not found
     */
    String getCustomerIdByName(String customerName);

    /**
     * Get product name by ID (calls Product Service)
     * @param productId the product ID
     * @return product name or "Unknown" if not found
     */
    String getProductNameById(String productId);

    /**
     * Get product ID by name (calls Product Service)
     * @param productName the product name
     * @return product ID or null if not found
     */
    String getProductIdByName(String productName);

    /**
     * Get tax rate by tax ID (calls Tax/Product Service)
     * @param taxId the tax ID
     * @return tax rate as BigDecimal
     */
    BigDecimal getTaxRateById(String taxId);

    /**
     * Get sales order number by ID
     * @param id the sales order ID
     * @return order number or "Unknown" if not found
     */
    String getSalesOrderNumberById(String id);

    /**
     * Get delivery order number by ID
     * @param id the delivery order ID
     * @return delivery number or "Unknown" if not found
     */
    String getDeliveryOrderNumberById(String id);
}