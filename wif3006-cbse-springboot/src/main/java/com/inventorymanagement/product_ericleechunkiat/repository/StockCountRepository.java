package com.inventorymanagement.product_ericleechunkiat.repository;

import com.inventorymanagement.product_ericleechunkiat.model.StockCount;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockCountRepository extends MongoRepository<StockCount, String> {
    // No custom logic needed for now, standard CRUD is enough
}