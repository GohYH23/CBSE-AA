package com.inventorymanagement.customer_gohyuheng.service;

import com.inventorymanagement.customer_gohyuheng.model.*;
import com.inventorymanagement.customer_gohyuheng.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepo;

    @Autowired
    private CustomerGroupRepository groupRepo;

    @Autowired
    private CustomerCategoryRepository categoryRepo;

    @Autowired
    private CustomerContactRepository contactRepo;

    // --- Customer Logic ---
    public Customer createCustomer(Customer customer) {
        return customerRepo.save(customer);
    }

    public List<Customer> getAllCustomers() {
        return customerRepo.findAll();
    }

    public Optional<Customer> getCustomerByName(String name) {
        return customerRepo.findByName(name);
    }

    public void deleteCustomer(String id) {
        customerRepo.deleteById(id);
        List<CustomerContact> contacts = contactRepo.findByCustomerId(id);
        contactRepo.deleteAll(contacts);
    }

    // --- Group Logic ---
    public CustomerGroup createGroup(CustomerGroup group) {
        return groupRepo.save(group);
    }

    public List<CustomerGroup> getAllGroups() {
        return groupRepo.findAll();
    }

    public Optional<CustomerGroup> getGroupByName(String name) {
        return groupRepo.findByGroupName(name);
    }

    public void deleteGroup(String id) {
        groupRepo.deleteById(id);
    }

    // --- Category Logic ---
    public CustomerCategory createCategory(CustomerCategory category) {
        return categoryRepo.save(category);
    }

    public List<CustomerCategory> getAllCategories() {
        return categoryRepo.findAll();
    }

    public void deleteCategory(String id) {
        categoryRepo.deleteById(id);
    }

    public Optional<CustomerCategory> getCategoryByName(String name) {
        return categoryRepo.findByCategoryName(name);
    }

    // --- Contact Logic ---
    public CustomerContact addContact(String customerId, CustomerContact contact) {
        contact.setCustomerId(customerId); // Link it automatically
        return contactRepo.save(contact);
    }

    public List<CustomerContact> getContactsByCustomerId(String customerId) {
        return contactRepo.findByCustomerId(customerId);
    }

    public void deleteContact(String id) {
        contactRepo.deleteById(id);
    }
}