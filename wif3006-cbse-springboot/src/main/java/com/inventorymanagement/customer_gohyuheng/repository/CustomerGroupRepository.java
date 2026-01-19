package com.inventorymanagement.customer_gohyuheng.repository;

import com.inventorymanagement.customer_gohyuheng.model.CustomerGroup;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerGroupRepository extends MongoRepository<CustomerGroup, String> {
    Optional<CustomerGroup> findByGroupName(String groupName);
}