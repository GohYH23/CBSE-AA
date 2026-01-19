package com.inventorymanagement.customer_gohyuheng.repository;

import com.inventorymanagement.customer_gohyuheng.model.CustomerContact;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CustomerContactRepository extends MongoRepository<CustomerContact, String> {
    // Custom finder to get all contacts for a specific customer
    List<CustomerContact> findByCustomerId(String customerId);
}