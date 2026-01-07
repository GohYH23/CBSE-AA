package com.inventorymanagement.SalesOrder.repository;

import com.inventorymanagement.SalesOrder.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    Optional<SalesOrder> findByNumber(String number);
}