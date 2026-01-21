package com.inventorymanagement.purchaseorder_ooiweiying.service;

import com.inventorymanagement.purchaseorder_ooiweiying.model.OrderItem;
import com.inventorymanagement.purchaseorder_ooiweiying.model.PurchaseOrder;
import com.inventorymanagement.purchaseorder_ooiweiying.repository.PurchaseOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PurchaseOrderService {
    
    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;
    
    // =================== CRUD Operations ===================
    
    public PurchaseOrder createPurchaseOrder(PurchaseOrder purchaseOrder) {
        // Auto-generate orderId if not set
        if (purchaseOrder.getOrderId() == 0) {
            purchaseOrder.setOrderId(getNextOrderId());
        }
        
        // Auto-generate order number if not set
        if (purchaseOrder.getOrderNumber() == null || purchaseOrder.getOrderNumber().isEmpty()) {
            purchaseOrder.setOrderNumber(generateOrderNumber(purchaseOrder.getOrderId()));
        }
        
        // Ensure status is set to pending if not set
        if (purchaseOrder.getOrderStatus() == null || purchaseOrder.getOrderStatus().isEmpty()) {
            purchaseOrder.setOrderStatus("pending");
        }
        
        return purchaseOrderRepository.save(purchaseOrder);
    }
    
    public List<PurchaseOrder> getAllPurchaseOrders() {
        return purchaseOrderRepository.findAll();
    }
    
    public Optional<PurchaseOrder> getPurchaseOrderById(String id) {
        return purchaseOrderRepository.findById(id);
    }
    
    public Optional<PurchaseOrder> getPurchaseOrderByOrderId(Integer orderId) {
        return purchaseOrderRepository.findByOrderId(orderId);
    }
    
    public PurchaseOrder updatePurchaseOrder(String id, PurchaseOrder updatedOrder) {
        Optional<PurchaseOrder> existingOpt = purchaseOrderRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return null; // Not found
        }
        
        PurchaseOrder existingOrder = existingOpt.get();
        
        // Ensure IDs match
        updatedOrder.setId(id);
        updatedOrder.setOrderId(existingOrder.getOrderId()); // Keep existing orderId
        
        // Handle status transitions and date management
        String newStatus = updatedOrder.getOrderStatus().toLowerCase();
        String oldStatus = existingOrder.getOrderStatus().toLowerCase();
        
        handleStatusTransitionDates(updatedOrder, existingOrder, oldStatus, newStatus);
        
        return purchaseOrderRepository.save(updatedOrder);
    }
    
    public PurchaseOrder updatePurchaseOrderByOrderId(Integer orderId, PurchaseOrder updatedOrder) {
        Optional<PurchaseOrder> existingOpt = purchaseOrderRepository.findByOrderId(orderId);
        if (existingOpt.isEmpty()) {
            return null; // Not found
        }
        
        PurchaseOrder existingOrder = existingOpt.get();
        return updatePurchaseOrder(existingOrder.getId(), updatedOrder);
    }
    
    public void deletePurchaseOrder(String id) {
        purchaseOrderRepository.deleteById(id);
    }
    
    public boolean deletePurchaseOrderByOrderId(Integer orderId) {
        Optional<PurchaseOrder> orderOpt = purchaseOrderRepository.findByOrderId(orderId);
        if (orderOpt.isPresent()) {
            purchaseOrderRepository.deleteById(orderOpt.get().getId());
            return true;
        }
        return false;
    }
    
    public boolean purchaseOrderExists(Integer orderId) {
        return purchaseOrderRepository.findByOrderId(orderId).isPresent();
    }
    
    // =================== Query Methods ===================
    
    public List<PurchaseOrder> getPurchaseOrdersByStatus(String status) {
        return purchaseOrderRepository.findByOrderStatus(status);
    }
    
    // =================== Status Transition Methods ===================
    
    public PurchaseOrder updateStatusToReceived(String id) {
        Optional<PurchaseOrder> orderOpt = purchaseOrderRepository.findById(id);
        if (orderOpt.isEmpty()) {
            return null;
        }
        
        PurchaseOrder order = orderOpt.get();
        if (!"shipping".equalsIgnoreCase(order.getOrderStatus())) {
            throw new IllegalStateException("Order must be in 'shipping' status to be received");
        }
        
        order.setOrderStatus("received");
        order.setReceivedDate(LocalDate.now());
        return purchaseOrderRepository.save(order);
    }
    
    public PurchaseOrder updateStatusToReturned(String id) {
        Optional<PurchaseOrder> orderOpt = purchaseOrderRepository.findById(id);
        if (orderOpt.isEmpty()) {
            return null;
        }
        
        PurchaseOrder order = orderOpt.get();
        if (!"received".equalsIgnoreCase(order.getOrderStatus())) {
            throw new IllegalStateException("Order must be in 'received' status to be returned");
        }
        
        order.setOrderStatus("returned");
        order.setReturnedDate(LocalDate.now());
        return purchaseOrderRepository.save(order);
    }
    
    public PurchaseOrder revertReceivedToShipping(String id) {
        Optional<PurchaseOrder> orderOpt = purchaseOrderRepository.findById(id);
        if (orderOpt.isEmpty()) {
            return null;
        }
        
        PurchaseOrder order = orderOpt.get();
        if (!"received".equalsIgnoreCase(order.getOrderStatus())) {
            throw new IllegalStateException("Order must be in 'received' status to revert to shipping");
        }
        
        order.setOrderStatus("shipping");
        order.setReceivedDate(null);
        return purchaseOrderRepository.save(order);
    }
    
    public PurchaseOrder revertReturnedToReceived(String id) {
        Optional<PurchaseOrder> orderOpt = purchaseOrderRepository.findById(id);
        if (orderOpt.isEmpty()) {
            return null;
        }
        
        PurchaseOrder order = orderOpt.get();
        if (!"returned".equalsIgnoreCase(order.getOrderStatus())) {
            throw new IllegalStateException("Order must be in 'returned' status to revert to received");
        }
        
        order.setOrderStatus("received");
        order.setReturnedDate(null);
        return purchaseOrderRepository.save(order);
    }
    
    // =================== Business Logic Methods ===================
    
    public int getNextOrderId() {
        List<PurchaseOrder> allOrders = purchaseOrderRepository.findAll();
        int maxId = 0;
        for (PurchaseOrder order : allOrders) {
            if (order.getOrderId() > maxId) {
                maxId = order.getOrderId();
            }
        }
        return maxId + 1;
    }
    
    public String generateOrderNumber(Integer orderId) {
        return String.format("PO-%03d", orderId);
    }
    
    // =================== Status Transition Date Management ===================
    
    private void handleStatusTransitionDates(PurchaseOrder updatedOrder, PurchaseOrder existingOrder, 
                                            String oldStatus, String newStatus) {
        // 1. Handle received/returned status (from Goods Receive/Return modules)
        if (newStatus.equals("received")) {
            if (oldStatus.equals("received")) {
                updatedOrder.setReceivedDate(existingOrder.getReceivedDate());
            } else {
                updatedOrder.setReceivedDate(LocalDate.now());
            }
            updatedOrder.setReturnedDate(null);
            updatedOrder.setShippingDate(existingOrder.getShippingDate());
            updatedOrder.setCancelledDate(existingOrder.getCancelledDate());
        } else if (newStatus.equals("returned")) {
            if (oldStatus.equals("returned")) {
                updatedOrder.setReturnedDate(existingOrder.getReturnedDate());
            } else {
                updatedOrder.setReturnedDate(LocalDate.now());
            }
            updatedOrder.setReceivedDate(existingOrder.getReceivedDate());
            updatedOrder.setShippingDate(existingOrder.getShippingDate());
            updatedOrder.setCancelledDate(existingOrder.getCancelledDate());
        } 
        // 2. Handle shipping status
        else if (newStatus.equals("shipping")) {
            if (!oldStatus.equals("shipping")) {
                updatedOrder.setShippingDate(LocalDate.now());
            } else {
                updatedOrder.setShippingDate(existingOrder.getShippingDate());
            }
            updatedOrder.setCancelledDate(null);
            updatedOrder.setReceivedDate(existingOrder.getReceivedDate());
            updatedOrder.setReturnedDate(existingOrder.getReturnedDate());
        }
        // 3. Handle pending status
        else if (newStatus.equals("pending")) {
            if (oldStatus.equals("shipping")) {
                updatedOrder.setShippingDate(null);
            } else {
                updatedOrder.setShippingDate(existingOrder.getShippingDate());
            }
            updatedOrder.setCancelledDate(null);
            updatedOrder.setReceivedDate(existingOrder.getReceivedDate());
            updatedOrder.setReturnedDate(existingOrder.getReturnedDate());
        }
        // 4. Handle cancelled status
        else if (newStatus.equals("cancelled")) {
            if (!oldStatus.equals("cancelled")) {
                updatedOrder.setCancelledDate(LocalDate.now());
            } else {
                updatedOrder.setCancelledDate(existingOrder.getCancelledDate());
            }
            updatedOrder.setShippingDate(null);
            updatedOrder.setReceivedDate(existingOrder.getReceivedDate());
            updatedOrder.setReturnedDate(existingOrder.getReturnedDate());
        }
        // 5. Default case (preserve existing dates)
        else {
            updatedOrder.setReceivedDate(existingOrder.getReceivedDate());
            updatedOrder.setReturnedDate(existingOrder.getReturnedDate());
            updatedOrder.setShippingDate(existingOrder.getShippingDate());
            updatedOrder.setCancelledDate(existingOrder.getCancelledDate());
        }
    }
}
