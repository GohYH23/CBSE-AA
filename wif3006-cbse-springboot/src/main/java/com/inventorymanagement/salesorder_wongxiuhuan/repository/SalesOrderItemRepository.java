package com.inventorymanagement.salesorder_wongxiuhuan.repository;

import com.inventorymanagement.salesorder_wongxiuhuan.model.SalesOrderItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesOrderItemRepository extends MongoRepository<SalesOrderItem, String> {
    List<SalesOrderItem> findBySalesOrderId(String salesOrderId);
    List<SalesOrderItem> findByProductId(String productId);
}