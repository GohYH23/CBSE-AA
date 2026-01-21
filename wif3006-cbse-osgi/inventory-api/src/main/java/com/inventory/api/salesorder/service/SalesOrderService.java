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
    SalesOrder createSalesOrder(SalesOrder order);

    List<SalesOrder> getAllSalesOrders();

    Optional<SalesOrder> getSalesOrderById(String id);

    Optional<SalesOrder> getSalesOrderByNumber(String orderNumber);

    void updateSalesOrder(SalesOrder order);

    // Returns a String message (e.g., "Cannot delete, has delivery orders")
    String deleteSalesOrder(String id);

    // ================= SALES ORDER ITEMS =================
    void addSalesOrderItem(SalesOrderItem item);

    List<SalesOrderItem> getItemsByOrderId(String orderId);

    void updateSalesOrderItem(SalesOrderItem item);

    void deleteSalesOrderItem(String id);

    // ================= DELIVERY ORDERS =================
    DeliveryOrder createDeliveryOrder(DeliveryOrder order);

    List<DeliveryOrder> getAllDeliveryOrders();

    Optional<DeliveryOrder> getDeliveryOrderById(String id);

    Optional<DeliveryOrder> getDeliveryOrderByNumber(String deliveryNumber);

    void updateDeliveryOrder(DeliveryOrder order);

    // Returns a String message
    String deleteDeliveryOrder(String id);

    // ================= SALES RETURNS =================
    SalesReturn createSalesReturn(SalesReturn salesReturn);

    List<SalesReturn> getAllSalesReturns();

    Optional<SalesReturn> getSalesReturnById(String id);

    Optional<SalesReturn> getSalesReturnByNumber(String returnNumber);

    void updateSalesReturn(SalesReturn salesReturn);

    // Returns a String message
    String deleteSalesReturn(String id);

    // ================= HELPER METHODS =================
    // These methods help with cross-module lookups (Customer, Product, Tax)
    String getCustomerNameById(String customerId);

    String getCustomerIdByName(String customerName);

    String getProductNameById(String productId);

    String getProductIdByName(String productName);

    BigDecimal getTaxRateById(String taxId);

    String getSalesOrderNumberById(String id);

    String getDeliveryOrderNumberById(String id);
}