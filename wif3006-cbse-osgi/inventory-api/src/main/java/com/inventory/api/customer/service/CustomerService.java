package com.inventory.api.customer.service;

import com.inventory.api.customer.model.Customer;
import com.inventory.api.customer.model.CustomerCategory;
import com.inventory.api.customer.model.CustomerContact;
import com.inventory.api.customer.model.CustomerGroup;

import java.util.List;
import java.util.Optional;

public interface CustomerService {

    // ================= CUSTOMERS =================
    void createCustomer(Customer customer);

    List<Customer> getAllCustomers();

    // Using Optional to prevent null pointer exceptions in the Menu
    Optional<Customer> getCustomerByName(String name);

    Optional<Customer> getCustomerById(String id);

    void updateCustomer(Customer customer);

    String deleteCustomer(String id);

    // ================= GROUPS =================
    void createGroup(CustomerGroup group);

    List<CustomerGroup> getAllGroups();

    Optional<CustomerGroup> getGroupByName(String name);

    Optional<CustomerGroup> getGroupById(String id);

    void updateGroup(CustomerGroup group);

    // Returns a String message (e.g., "Cannot delete, in use")
    String deleteGroup(String id);

    // ================= CATEGORIES =================
    void createCategory(CustomerCategory category);

    List<CustomerCategory> getAllCategories();

    Optional<CustomerCategory> getCategoryByName(String name);

    Optional<CustomerCategory> getCategoryById(String id);

    void updateCategory(CustomerCategory category);

    // Returns a String message
    String deleteCategory(String id);

    // ================= CONTACTS =================
    List<CustomerContact> getAllContacts();

    List<CustomerContact> getContactsByCustomerId(String customerId);

    // Pass customerId explicitly to link the contact
    void addContact(String customerId, CustomerContact contact);

    void updateContact(CustomerContact contact);

    void deleteContact(String id);
}
