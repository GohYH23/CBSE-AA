package com.inventorymanagement.purchaseorder_ooiweiying.repository;

import com.inventorymanagement.purchaseorder_ooiweiying.model.PurchaseOrder;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends MongoRepository<PurchaseOrder, String> {
    
    List<PurchaseOrder> findByOrderStatus(String status);
    
    Optional<PurchaseOrder> findByOrderNumber(String orderNumber);
    
    Optional<PurchaseOrder> findByOrderId(Integer orderId);
    
    List<PurchaseOrder> findByVendor(String vendor);
}
