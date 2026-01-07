package com.inventory.api.customer;

import java.util.List;

public interface CustomerService {
    void addCustomer(String name);
    List<String> getAllCustomers();
}