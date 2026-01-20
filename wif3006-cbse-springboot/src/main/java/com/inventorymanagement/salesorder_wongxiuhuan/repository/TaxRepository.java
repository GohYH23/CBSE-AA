package com.inventorymanagement.salesorder_wongxiuhuan.repository;

import com.inventorymanagement.salesorder_wongxiuhuan.model.Tax;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaxRepository extends MongoRepository<Tax, String> {
    Optional<Tax> findByTaxName(String taxName);
}