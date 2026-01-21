package com.inventorymanagement.salesorder_wongxiuhuan.service;

import com.inventorymanagement.salesorder_wongxiuhuan.model.*;
import com.inventorymanagement.salesorder_wongxiuhuan.repository.*;
import com.inventorymanagement.customer_gohyuheng.repository.CustomerRepository;
import com.inventorymanagement.product_ericleechunkiat.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class SalesOrderService {

    @Autowired
    private SalesOrderRepository salesOrderRepo;

    @Autowired
    private SalesOrderItemRepository salesOrderItemRepo;

    @Autowired
    private DeliveryOrderRepository deliveryOrderRepo;

    @Autowired
    private SalesReturnRepository salesReturnRepo;

    @Autowired
    private TaxRepository taxRepo;

    @Autowired
    private CustomerRepository customerRepo;

    @Autowired
    private ProductRepository productRepo;

    // ==================== SALES ORDER LOGIC ====================

    public SalesOrder createSalesOrder(SalesOrder order) {
        order.setOrderNumber(generateOrderNumber());
        order.setCreatedDate(LocalDateTime.now());
        order.setUpdatedDate(LocalDateTime.now());
        return salesOrderRepo.save(order);
    }

    public List<SalesOrder> getAllSalesOrders() {
        return salesOrderRepo.findAll();
    }

    public Optional<SalesOrder> getSalesOrderById(String id) {
        return salesOrderRepo.findById(id);
    }

    public Optional<SalesOrder> getSalesOrderByNumber(String orderNumber) {
        return salesOrderRepo.findByOrderNumber(orderNumber);
    }

    public List<SalesOrder> getSalesOrdersByCustomerId(String customerId) {
        return salesOrderRepo.findByCustomerId(customerId);
    }

    public List<SalesOrder> getSalesOrdersByStatus(String status) {
        return salesOrderRepo.findByOrderStatus(status);
    }

    public SalesOrder updateSalesOrder(SalesOrder order) {
        order.setUpdatedDate(LocalDateTime.now());
        return salesOrderRepo.save(order);
    }

    public String deleteSalesOrder(String id) {
        // Check if there are delivery orders linked
        List<DeliveryOrder> linkedDeliveries = deliveryOrderRepo.findBySalesOrderId(id);
        if (!linkedDeliveries.isEmpty()) {
            return "❌ Cannot delete: " + linkedDeliveries.size() + " delivery order(s) are linked to this sales order.";
        }

        // Delete all items first
        List<SalesOrderItem> items = salesOrderItemRepo.findBySalesOrderId(id);
        salesOrderItemRepo.deleteAll(items);

        // Delete the order
        salesOrderRepo.deleteById(id);
        return "✅ Sales Order deleted successfully.";
    }

    private String generateOrderNumber() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return "SO-" + LocalDateTime.now().format(formatter);
    }

    // ==================== SALES ORDER ITEM LOGIC ====================

    public SalesOrderItem addSalesOrderItem(SalesOrderItem item) {
        item.setCreatedDate(LocalDateTime.now());
        item.setUpdatedDate(LocalDateTime.now());
        SalesOrderItem saved = salesOrderItemRepo.save(item);

        recalculateOrderTotals(item.getSalesOrderId());

        return saved;
    }

    public List<SalesOrderItem> getItemsByOrderId(String salesOrderId) {
        return salesOrderItemRepo.findBySalesOrderId(salesOrderId);
    }

    public List<SalesOrderItem> getAllSalesOrderItems() {
        return salesOrderItemRepo.findAll();
    }

    public Optional<SalesOrderItem> getSalesOrderItemById(String id) {
        return salesOrderItemRepo.findById(id);
    }

    public SalesOrderItem updateSalesOrderItem(SalesOrderItem item) {
        item.setUpdatedDate(LocalDateTime.now());
        SalesOrderItem updated = salesOrderItemRepo.save(item);

        recalculateOrderTotals(item.getSalesOrderId());

        return updated;
    }

    public void deleteSalesOrderItem(String id) {
        Optional<SalesOrderItem> itemOpt = salesOrderItemRepo.findById(id);
        if (itemOpt.isPresent()) {
            String orderId = itemOpt.get().getSalesOrderId();
            salesOrderItemRepo.deleteById(id);
        
            recalculateOrderTotals(orderId);
        }
    }

    // ==================== DELIVERY ORDER LOGIC ====================

    public DeliveryOrder createDeliveryOrder(DeliveryOrder order) {
        order.setDeliveryNumber(generateDeliveryNumber());
        order.setCreatedDate(LocalDateTime.now());
        order.setUpdatedDate(LocalDateTime.now());
        return deliveryOrderRepo.save(order);
    }

    public List<DeliveryOrder> getAllDeliveryOrders() {
        return deliveryOrderRepo.findAll();
    }

    public Optional<DeliveryOrder> getDeliveryOrderById(String id) {
        return deliveryOrderRepo.findById(id);
    }

    public Optional<DeliveryOrder> getDeliveryOrderByNumber(String deliveryNumber) {
        return deliveryOrderRepo.findByDeliveryNumber(deliveryNumber);
    }

    public List<DeliveryOrder> getDeliveryOrdersBySalesOrderId(String salesOrderId) {
        return deliveryOrderRepo.findBySalesOrderId(salesOrderId);
    }

    public DeliveryOrder updateDeliveryOrder(DeliveryOrder order) {
        order.setUpdatedDate(LocalDateTime.now());
        return deliveryOrderRepo.save(order);
    }

    public String deleteDeliveryOrder(String id) {
        // Check if there are sales returns linked
        List<SalesReturn> linkedReturns = salesReturnRepo.findByDeliveryOrderId(id);
        if (!linkedReturns.isEmpty()) {
            return "❌ Cannot delete: " + linkedReturns.size() + " sales return(s) are linked to this delivery order.";
        }

        deliveryOrderRepo.deleteById(id);
        return "✅ Delivery Order deleted successfully.";
    }

    private String generateDeliveryNumber() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return "DO-" + LocalDateTime.now().format(formatter);
    }

    // ==================== SALES RETURN LOGIC ====================

    public SalesReturn createSalesReturn(SalesReturn salesReturn) {
        salesReturn.setReturnNumber(generateReturnNumber());
        salesReturn.setCreatedDate(LocalDateTime.now());
        salesReturn.setUpdatedDate(LocalDateTime.now());
        return salesReturnRepo.save(salesReturn);
    }

    public List<SalesReturn> getAllSalesReturns() {
        return salesReturnRepo.findAll();
    }

    public Optional<SalesReturn> getSalesReturnById(String id) {
        return salesReturnRepo.findById(id);
    }

    public Optional<SalesReturn> getSalesReturnByNumber(String returnNumber) {
        return salesReturnRepo.findByReturnNumber(returnNumber);
    }

    public List<SalesReturn> getSalesReturnsByDeliveryOrderId(String deliveryOrderId) {
        return salesReturnRepo.findByDeliveryOrderId(deliveryOrderId);
    }

    public SalesReturn updateSalesReturn(SalesReturn salesReturn) {
        salesReturn.setUpdatedDate(LocalDateTime.now());
        return salesReturnRepo.save(salesReturn);
    }

    public String deleteSalesReturn(String id) {
        salesReturnRepo.deleteById(id);
        return "✅ Sales Return deleted successfully.";
    }

    private String generateReturnNumber() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return "SR-" + LocalDateTime.now().format(formatter);
    }

    // ==================== TAX LOGIC ====================

    public Tax createTax(Tax tax) {
        tax.setCreatedDate(LocalDateTime.now());
        tax.setUpdatedDate(LocalDateTime.now());
        return taxRepo.save(tax);
    }

    public List<Tax> getAllTaxes() {
        return taxRepo.findAll();
    }

    public Optional<Tax> getTaxById(String id) {
        return taxRepo.findById(id);
    }

    public Optional<Tax> getTaxByName(String taxName) {
        return taxRepo.findByTaxName(taxName);
    }

    public Tax updateTax(Tax tax) {
        tax.setUpdatedDate(LocalDateTime.now());
        return taxRepo.save(tax);
    }

    public String deleteTax(String id) {
        // Check if any sales orders are using this tax
        List<SalesOrder> linkedOrders = salesOrderRepo.findAll().stream()
                .filter(order -> id.equals(order.getTaxId()))
                .toList();

        if (!linkedOrders.isEmpty()) {
            return "❌ Cannot delete: " + linkedOrders.size() + " sales order(s) are using this tax.";
        }

        taxRepo.deleteById(id);
        return "✅ Tax deleted successfully.";
    }

    // ==================== HELPER METHODS FOR CROSS-MODULE DATA ====================

    /**
     * Get customer name by ID
     * Integrated with Customer module
     */
    public String getCustomerNameById(String customerId) {
        return customerRepo.findById(customerId)
                .map(customer -> customer.getName())
                .orElse("Unknown");
    }

    /**
     * Get customer ID by name
     * Integrated with Customer module
     */
    public String getCustomerIdByName(String customerName) {
        return customerRepo.findByName(customerName)
                .map(customer -> customer.getId())
                .orElse(null);
    }

    /**
    * Get product name by ID
    */
    public String getProductNameById(String productId) {
        return productRepo.findById(productId)
                .map(product -> product.getName())
                .orElse("Unknown");
    }

    /**
     * Get product ID by name
    */
    public String getProductIdByName(String productName) {
        return productRepo.findByName(productName)
                .map(product -> product.getId())
                .orElse(null);
    }

    /**
     * Get product price by ID
    */
    public BigDecimal getProductPriceById(String productId) {
        return productRepo.findById(productId)
                .map(product -> BigDecimal.valueOf(product.getPrice()))
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Get tax rate by tax ID
     */
    public BigDecimal getTaxRateById(String taxId) {
        return taxRepo.findById(taxId)
                .map(Tax::getTaxRate)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Get sales order number by ID
     */
    public String getSalesOrderNumberById(String salesOrderId) {
        return salesOrderRepo.findById(salesOrderId)
                .map(SalesOrder::getOrderNumber)
                .orElse("Unknown");
    }

    /**
     * Get delivery order number by ID
     */
    public String getDeliveryOrderNumberById(String deliveryOrderId) {
        return deliveryOrderRepo.findById(deliveryOrderId)
                .map(DeliveryOrder::getDeliveryNumber)
                .orElse("Unknown");
    }

    /**
    * Recalculate order totals (before tax, tax amount, after tax)
    * Called whenever items are added, updated, or deleted
    */
    public void recalculateOrderTotals(String orderId) {
        try {
            Optional<SalesOrder> orderOpt = salesOrderRepo.findById(orderId);
            if (orderOpt.isEmpty()) return;
        
            SalesOrder order = orderOpt.get();
            List<SalesOrderItem> items = salesOrderItemRepo.findBySalesOrderId(orderId);
        
            // Calculate subtotal (before tax)
            BigDecimal beforeTax = BigDecimal.ZERO;
            for (SalesOrderItem item : items) {
                BigDecimal itemTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                beforeTax = beforeTax.add(itemTotal);
            }
        
            // Calculate tax amount
            BigDecimal taxAmount = BigDecimal.ZERO;
            if (order.getTaxId() != null && !order.getTaxId().trim().isEmpty()) {
                BigDecimal taxRate = getTaxRateById(order.getTaxId());
                taxAmount = beforeTax.multiply(taxRate).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
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
}