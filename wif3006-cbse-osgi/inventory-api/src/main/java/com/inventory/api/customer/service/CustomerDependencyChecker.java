package com.inventory.api.customer.service;

public interface CustomerDependencyChecker {
    // Returns true if the module has data related to this customer
    boolean hasDependency(String customerId);

    // Returns the error message (e.g., "Customer has Sales Orders")
    String getDependencyMessage();
}