package com.inventorymanagement.customer_gohyuheng.service;

import com.inventorymanagement.customer_gohyuheng.model.*;
import com.inventorymanagement.customer_gohyuheng.repository.*;
import com.inventorymanagement.salesorder_wongxiuhuan.repository.SalesOrderRepository;
import com.inventorymanagement.salesorder_wongxiuhuan.model.SalesOrder;
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

    @Autowired
    private SalesOrderRepository salesOrderRepo;

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

    public Optional<Customer> getCustomerById(String id) {
        return customerRepo.findById(id);
    }

    public Customer updateCustomer(Customer customer) {
        return customerRepo.save(customer);
    }

    public String deleteCustomer(String id) {
        // Check if customer exists
        if (!customerRepo.existsById(id)) {
            return "❌ Customer not found.";
        }

        // CHECK SALES ORDERS
        List<SalesOrder> existingOrders = salesOrderRepo.findByCustomerId(id);

        if (!existingOrders.isEmpty()) {
            return "❌ Cannot delete: This customer has " + existingOrders.size() + " existing Sales Order(s).";
        }

        // If no orders, proceed to delete
        List<CustomerContact> contacts = contactRepo.findByCustomerId(id);
        contactRepo.deleteAll(contacts);

        customerRepo.deleteById(id);
        return "✅ Customer deleted successfully.";
    }

    // --- Group Logic ---
    public CustomerGroup createGroup(CustomerGroup group) {
        return groupRepo.save(group);
    }

    public Optional<CustomerGroup> getGroupById(String id) {
        return groupRepo.findById(id);
    }

    public List<CustomerGroup> getAllGroups() {
        return groupRepo.findAll();
    }

    public Optional<CustomerGroup> getGroupByName(String name) {
        return groupRepo.findByGroupName(name);
    }

    public CustomerGroup updateGroup(CustomerGroup group) {
        return groupRepo.save(group);
    }

    public String deleteGroup(String id) {
        List<Customer> linked = customerRepo.findByCustomerGroupId(id);
        if (!linked.isEmpty()) {
            return "❌ Cannot delete: " + linked.size() + " customer(s) are currently assigned to this group.";
        }
        groupRepo.deleteById(id);
        return "✅ Group deleted successfully.";
    }

    // --- Category Logic ---
    public CustomerCategory createCategory(CustomerCategory category) {
        return categoryRepo.save(category);
    }

    public Optional<CustomerCategory> getCategoryById(String id) {
        return categoryRepo.findById(id);
    }

    public List<CustomerCategory> getAllCategories() {
        return categoryRepo.findAll();
    }

    public CustomerCategory updateCategory(CustomerCategory category) {
        return categoryRepo.save(category);
    }

    public String deleteCategory(String id) {
        List<Customer> linked = customerRepo.findByCustomerCategoryId(id);
        if (!linked.isEmpty()) {
            return "❌ Cannot delete: " + linked.size() + " customer(s) are currently assigned to this category.";
        }
        categoryRepo.deleteById(id);
        return "✅ Category deleted successfully.";
    }

    public Optional<CustomerCategory> getCategoryByName(String name) {
        return categoryRepo.findByCategoryName(name);
    }

    // --- Contact Logic ---
    public List<CustomerContact> getAllContacts() {
        return contactRepo.findAll();
    }

    public CustomerContact addContact(String customerId, CustomerContact contact) {
        contact.setCustomerId(customerId); // Link it automatically
        return contactRepo.save(contact);
    }

    public List<CustomerContact> getContactsByCustomerId(String customerId) {
        return contactRepo.findByCustomerId(customerId);
    }

    public CustomerContact updateContact(CustomerContact contact) {
        return contactRepo.save(contact);
    }

    public void deleteContact(String id) {
        contactRepo.deleteById(id);
    }
}