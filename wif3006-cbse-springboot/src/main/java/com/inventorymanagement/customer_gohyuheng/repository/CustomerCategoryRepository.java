package com.inventorymanagement.customer_gohyuheng.repository;

import com.inventorymanagement.customer_gohyuheng.model.CustomerCategory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerCategoryRepository extends MongoRepository<CustomerCategory, String> {
    Optional<CustomerCategory> findByCategoryName(String categoryName);
}