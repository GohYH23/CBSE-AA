package com.inventorymanagement.salesorder_wongxiuhuan.repository;

import com.inventorymanagement.salesorder_wongxiuhuan.model.DeliveryOrder;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryOrderRepository extends MongoRepository<DeliveryOrder, String> {
    Optional<DeliveryOrder> findByDeliveryNumber(String deliveryNumber);
    List<DeliveryOrder> findBySalesOrderId(String salesOrderId);
    List<DeliveryOrder> findByStatus(String status);
}