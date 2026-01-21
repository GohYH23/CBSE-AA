package com.inventorymanagement.product_ericleechunkiat.repository;

import com.inventorymanagement.product_ericleechunkiat.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    // Custom finder methods (Spring generates the logic automatically)
    boolean existsByNameIgnoreCase(String name);
    List<Product> findByProductGroupId(String groupId);
    List<Product> findByUomId(String uomId);
}