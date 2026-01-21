/*
 * Customer Module testing done by Goh Yu Heng
 * This test cover all functionalities
 * for Customers, Groups, Categories, and Contacts.
 */

package com.inventorymanagement.customer_gohyuheng.service;

import com.inventorymanagement.customer_gohyuheng.model.*;
import com.inventorymanagement.customer_gohyuheng.repository.*;
import com.inventorymanagement.salesorder_wongxiuhuan.repository.SalesOrderRepository;
import com.inventorymanagement.salesorder_wongxiuhuan.model.SalesOrder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepo;

    @Mock
    private CustomerGroupRepository groupRepo;

    @Mock
    private CustomerCategoryRepository categoryRepo;

    @Mock
    private CustomerContactRepository contactRepo;

    @Mock
    private SalesOrderRepository salesOrderRepo;

    @InjectMocks
    private CustomerService customerService;

    // --- 1. CUSTOMER FUNCTIONALITIES TESTS ---

    @Test
    void testCreateCustomer_ShouldSaveAndReturnCustomer() {
        Customer customer = new Customer();
        customer.setName("Ali");
        when(customerRepo.save(any(Customer.class))).thenReturn(customer);

        Customer result = customerService.createCustomer(new Customer());

        assertNotNull(result);
        assertEquals("Ali", result.getName());
        verify(customerRepo, times(1)).save(any());
    }

    @Test
    void testGetCustomerById_ShouldReturnOptionalCustomer() {
        String id = "cust-001";
        Customer customer = new Customer();
        customer.setId(id);
        when(customerRepo.findById(id)).thenReturn(Optional.of(customer));

        Optional<Customer> result = customerService.getCustomerById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    void testUpdateCustomer_ShouldTriggerSave() {
        Customer customer = new Customer();
        customer.setName("Ali Updated");

        customerService.updateCustomer(customer);

        verify(customerRepo, times(1)).save(customer);
    }

    @Test
    void testDeleteCustomer_ShouldCallRepoAndDeleteAssociatedContacts() {
        String customerId = "cust-123";

        when(customerRepo.existsById(customerId)).thenReturn(true);

        when(salesOrderRepo.findByCustomerId(customerId)).thenReturn(Collections.emptyList());

        List<CustomerContact> associatedContacts = List.of(new CustomerContact());
        when(contactRepo.findByCustomerId(customerId)).thenReturn(associatedContacts);

        String result = customerService.deleteCustomer(customerId);
        assertTrue(result.contains("✅ Customer deleted"));
        verify(customerRepo, times(1)).deleteById(customerId);
        verify(contactRepo, times(1)).deleteAll(associatedContacts);
    }

    @Test
    void testDeleteCustomer_WhenOrdersExist_ShouldReturnErrorMessage() {
        String customerId = "cust-999";

        when(customerRepo.existsById(customerId)).thenReturn(true);

        when(salesOrderRepo.findByCustomerId(customerId)).thenReturn(List.of(new SalesOrder()));

        String result = customerService.deleteCustomer(customerId);

        assertTrue(result.contains("❌ Cannot delete"));

        verify(customerRepo, never()).deleteById(customerId);

        verify(contactRepo, never()).deleteAll(anyList());
    }

    @Test
    void testDeleteCustomer_WhenCustomerNotFound_ShouldReturnErrorMessage() {
        String customerId = "cust-unknown";

        when(customerRepo.existsById(customerId)).thenReturn(false);

        String result = customerService.deleteCustomer(customerId);

        assertTrue(result.contains("❌ Customer not found"));
        verify(customerRepo, never()).deleteById(anyString());
    }

    // --- 2. GROUP FUNCTIONALITIES TESTS ---

    @Test
    void testCreateGroup_ShouldSaveAndReturnGroup() {
        CustomerGroup group = new CustomerGroup();
        group.setGroupName("Corporate");
        when(groupRepo.save(any(CustomerGroup.class))).thenReturn(group);

        CustomerGroup result = customerService.createGroup(new CustomerGroup());

        assertNotNull(result);
        assertEquals("Corporate", result.getGroupName());
        verify(groupRepo, times(1)).save(any());
    }

    @Test
    void testUpdateGroup_ShouldTriggerSave() {
        CustomerGroup group = new CustomerGroup();
        customerService.updateGroup(group);
        verify(groupRepo, times(1)).save(group);
    }

    @Test
    void testDeleteGroup_WhenAssignedToCustomer_ShouldReturnErrorMessage() {
        String groupId = "group-001";
        // Simulate that 1 customer is still assigned to this group
        when(customerRepo.findByCustomerGroupId(groupId)).thenReturn(List.of(new Customer()));

        String result = customerService.deleteGroup(groupId);

        assertTrue(result.contains("❌ Cannot delete"));
        verify(groupRepo, never()).deleteById(anyString());
    }

    @Test
    void testDeleteGroup_WhenNotAssigned_ShouldDeleteSuccessfully() {
        String groupId = "group-002";
        when(customerRepo.findByCustomerGroupId(groupId)).thenReturn(List.of());

        String result = customerService.deleteGroup(groupId);

        assertTrue(result.contains("✅ Group deleted successfully"));
        verify(groupRepo, times(1)).deleteById(groupId);
    }

    // --- 3. CATEGORY FUNCTIONALITIES TESTS ---

    @Test
    void testCreateCategory_ShouldSaveAndReturnCategory() {
        CustomerCategory cat = new CustomerCategory();
        cat.setCategoryName("Startup");
        when(categoryRepo.save(any(CustomerCategory.class))).thenReturn(cat);

        CustomerCategory result = customerService.createCategory(new CustomerCategory());

        assertEquals("Startup", result.getCategoryName());
        verify(categoryRepo, times(1)).save(any());
    }

    @Test
    void testUpdateCategory_ShouldTriggerSave() {
        CustomerCategory category = new CustomerCategory();
        customerService.updateCategory(category);
        verify(categoryRepo, times(1)).save(category);
    }

    @Test
    void testDeleteCategory_WhenInUse_ShouldPreventDeletion() {
        String catId = "cat-999";
        when(customerRepo.findByCustomerCategoryId(catId)).thenReturn(List.of(new Customer()));

        String result = customerService.deleteCategory(catId);

        assertTrue(result.contains("❌ Cannot delete"));
        verify(categoryRepo, never()).deleteById(catId);
    }

    // --- 4. CONTACT FUNCTIONALITIES TESTS ---

    @Test
    void testAddContact_ShouldSetCustomerIdAndSave() {
        String customerId = "cust-101";
        CustomerContact contact = new CustomerContact();
        when(contactRepo.save(any(CustomerContact.class))).thenReturn(contact);

        customerService.addContact(customerId, contact);

        // Verifies the service automatically links the ID
        assertEquals(customerId, contact.getCustomerId());
        verify(contactRepo, times(1)).save(contact);
    }

    @Test
    void testGetContactsByCustomerId_ShouldReturnList() {
        String customerId = "cust-123";
        when(contactRepo.findByCustomerId(customerId)).thenReturn(List.of(new CustomerContact()));

        List<CustomerContact> results = customerService.getContactsByCustomerId(customerId);

        assertFalse(results.isEmpty());
        verify(contactRepo, times(1)).findByCustomerId(customerId);
    }

    @Test
    void testUpdateContact_ShouldSaveToRepo() {
        CustomerContact contact = new CustomerContact();
        contact.setContactName("Updated Name");

        customerService.updateContact(contact);

        verify(contactRepo, times(1)).save(contact);
    }

    @Test
    void testDeleteContact_ShouldCallRepoDelete() {
        String contactId = "cont-555";
        customerService.deleteContact(contactId);
        verify(contactRepo, times(1)).deleteById(contactId);
    }

    // --- 5. READ/VIEW ALL TESTS ---

    @Test
    void testGetAllCustomers_ShouldReturnListFromRepo() {
        when(customerRepo.findAll()).thenReturn(List.of(new Customer(), new Customer()));
        List<Customer> list = customerService.getAllCustomers();
        assertEquals(2, list.size());
        verify(customerRepo, times(1)).findAll();
    }

    @Test
    void testGetAllGroups_ShouldReturnListFromRepo() {
        when(groupRepo.findAll()).thenReturn(List.of(new CustomerGroup()));
        List<CustomerGroup> list = customerService.getAllGroups();
        assertNotNull(list);
        verify(groupRepo, times(1)).findAll();
    }

    @Test
    void testGetAllCategories_ShouldReturnListFromRepo() {
        // Arrange
        when(categoryRepo.findAll()).thenReturn(List.of(new CustomerCategory()));

        // Act
        List<CustomerCategory> list = customerService.getAllCategories();

        // Assert
        assertNotNull(list);
        assertEquals(1, list.size());
        verify(categoryRepo, times(1)).findAll();
    }

    @Test
    void testGetAllContacts_ShouldReturnListFromRepo() {
        // Arrange
        when(contactRepo.findAll()).thenReturn(List.of(new CustomerContact()));

        // Act
        List<CustomerContact> list = customerService.getAllContacts();

        // Assert
        assertNotNull(list);
        verify(contactRepo, times(1)).findAll();
    }
}