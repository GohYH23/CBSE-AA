package com.inventorymanagement.salesorder_wongxiuhuan.repository;

import com.inventorymanagement.salesorder_wongxiuhuan.model.SalesOrder;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalesOrderRepository extends MongoRepository<SalesOrder, String> {
    Optional<SalesOrder> findByOrderNumber(String orderNumber);
    List<SalesOrder> findByCustomerId(String customerId);
    List<SalesOrder> findByOrderStatus(String orderStatus);
}