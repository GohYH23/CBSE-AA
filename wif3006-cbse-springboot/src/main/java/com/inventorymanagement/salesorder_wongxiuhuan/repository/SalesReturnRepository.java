package com.inventorymanagement.salesorder_wongxiuhuan.repository;

import com.inventorymanagement.salesorder_wongxiuhuan.model.SalesReturn;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalesReturnRepository extends MongoRepository<SalesReturn, String> {
    Optional<SalesReturn> findByReturnNumber(String returnNumber);
    List<SalesReturn> findByDeliveryOrderId(String deliveryOrderId);
    List<SalesReturn> findByStatus(String status);
}