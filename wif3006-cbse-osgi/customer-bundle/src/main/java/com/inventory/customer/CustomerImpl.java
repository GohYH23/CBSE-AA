package com.inventory.customer;
import com.inventory.api.customer.CustomerService;

public class CustomerImpl implements CustomerService {
    public void addCustomer(String name) {
        System.out.println("[OSGi-Customer] Adding customer: " + name);
    }
    public String getCustomerDetails(int id) {
        return "Customer Details for ID " + id;
    }
}