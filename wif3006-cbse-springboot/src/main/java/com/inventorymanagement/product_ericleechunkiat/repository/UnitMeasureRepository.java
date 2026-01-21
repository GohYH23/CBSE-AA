package com.inventorymanagement.product_ericleechunkiat.repository;

import com.inventorymanagement.product_ericleechunkiat.model.UnitMeasure;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnitMeasureRepository extends MongoRepository<UnitMeasure, String> {
    boolean existsByUnitNameIgnoreCase(String unitName);
}