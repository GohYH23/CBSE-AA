package com.inventorymanagement.product_ericleechunkiat.repository;

import com.inventorymanagement.product_ericleechunkiat.model.Warehouse;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WarehouseRepository extends MongoRepository<Warehouse, String> {
    boolean existsByNameIgnoreCase(String name);
}