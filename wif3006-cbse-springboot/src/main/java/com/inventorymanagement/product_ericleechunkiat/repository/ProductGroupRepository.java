package com.inventorymanagement.product_ericleechunkiat.repository;

import com.inventorymanagement.product_ericleechunkiat.model.ProductGroup;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductGroupRepository extends MongoRepository<ProductGroup, String> {
    boolean existsByGroupNameIgnoreCase(String groupName);
}