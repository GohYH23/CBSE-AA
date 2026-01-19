package com.inventorymanagement.customer;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
// Change JpaRepository to MongoRepository
// Change ID type <Customer, Long> to <Customer, String>
public interface CustomerRepository extends MongoRepository<Customer, String> {

    // You can still define custom finders here
    // Customer findByName(String name);
}