package com.inventory.api.customer;

public interface CustomerService {
    void addCustomer(String name);
    String getCustomerDetails(int id);
}