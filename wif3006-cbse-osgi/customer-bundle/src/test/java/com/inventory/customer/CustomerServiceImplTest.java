/*
 * Customer Module testing done by Goh Yu Heng
 * This test verifies the OSGi implementation for
 * Customers, Groups, Categories, and Contacts.
 */
package com.inventory.customer;

import com.inventory.api.customer.model.*;
import com.mongodb.client.*;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceImplTest {

    @Mock
    private MongoCollection<Document> customerCollection;
    @Mock
    private MongoCollection<Document> groupCollection;
    @Mock
    private MongoCollection<Document> categoryCollection;
    @Mock
    private MongoCollection<Document> contactCollection;

    @Mock
    private FindIterable<Document> findIterable;
    @Mock
    private MongoCursor<Document> cursor;
    @Mock
    private DeleteResult deleteResult;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @BeforeEach
    void setUp() throws Exception {
        // Injecting mocks into private fields since @Activate is skipped in unit tests
        setField(customerService, "customerCollection", customerCollection);
        setField(customerService, "groupCollection", groupCollection);
        setField(customerService, "categoryCollection", categoryCollection);
        setField(customerService, "contactCollection", contactCollection);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // --- 1. CUSTOMER MODULE TESTS ---

    @Test
    void testCreateCustomer_ShouldInsertDocument() {
        Customer customer = new Customer();
        customer.setName("Goh Yu Heng");
        customerService.createCustomer(customer);
        verify(customerCollection, times(1)).insertOne(any(Document.class));
    }

    @Test
    void testGetCustomerById_ShouldReturnOptional() {
        ObjectId id = new ObjectId();
        when(customerCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(new Document("_id", id).append("name", "Ali"));

        Optional<Customer> result = customerService.getCustomerById(id.toString());
        assertTrue(result.isPresent());
    }

    @Test
    void testUpdateCustomer_ShouldTriggerReplaceOne() {
        Customer c = new Customer();
        c.setId(new ObjectId().toString());
        customerService.updateCustomer(c);
        verify(customerCollection, times(1)).replaceOne(any(Bson.class), any(Document.class));
    }

    @Test
    void testDeleteCustomer_ShouldCascadeToContacts() {
        String id = new ObjectId().toString();
        customerService.deleteCustomer(id);
        verify(customerCollection, times(1)).deleteOne(any(Bson.class));
        verify(contactCollection, times(1)).deleteMany(any(Bson.class));
    }

    // --- 2. GROUP MODULE TESTS ---

    @Test
    void testCreateGroup_ShouldInsertDocument() {
        customerService.createGroup(new CustomerGroup());
        verify(groupCollection, times(1)).insertOne(any(Document.class));
    }

    @Test
    void testUpdateGroup_ShouldTriggerReplaceOne() {
        CustomerGroup group = new CustomerGroup();
        group.setId(new ObjectId().toString());
        group.setGroupName("Corporate");

        customerService.updateGroup(group);

        // Verifies the manual document mapping and replace operation
        assertNotNull(group.getEditedAt());
        verify(groupCollection, times(1)).replaceOne(any(Bson.class), any(Document.class));
    }

    @Test
    void testDeleteGroup_WhenAssignedToCustomer_ShouldBlock() {
        when(customerCollection.countDocuments(any(Bson.class))).thenReturn(5L);
        String result = customerService.deleteGroup("g1");
        assertTrue(result.contains("Cannot delete"));
    }

    @Test
    void testDeleteGroup_WhenEmpty_ShouldProceed() {
        when(customerCollection.countDocuments(any(Bson.class))).thenReturn(0L);
        when(groupCollection.deleteOne(any(Bson.class))).thenReturn(deleteResult);
        when(deleteResult.getDeletedCount()).thenReturn(1L);
        assertEquals("Group deleted.", customerService.deleteGroup(new ObjectId().toString()));
    }

    // --- 3. CATEGORY MODULE TESTS ---

    @Test
    void testCreateCategory_ShouldInsertDocument() {
        customerService.createCategory(new CustomerCategory());
        verify(categoryCollection, times(1)).insertOne(any(Document.class));
    }

    @Test
    void testDeleteCategory_WhenInUse_ShouldBlock() {
        when(customerCollection.countDocuments(any(Bson.class))).thenReturn(2L);
        assertTrue(customerService.deleteCategory("c1").contains("Cannot delete"));
    }

    @Test
    void testUpdateCategory_ShouldUpdateEditedAt() {
        CustomerCategory cat = new CustomerCategory();
        cat.setId(new ObjectId().toString());
        customerService.updateCategory(cat);
        assertNotNull(cat.getEditedAt());
        verify(categoryCollection, times(1)).replaceOne(any(Bson.class), any(Document.class));
    }

    // --- 4. CONTACT MODULE TESTS ---

    @Test
    void testAddContact_ShouldLinkToCustomer() {
        String customerId = "cust-123";
        CustomerContact contact = new CustomerContact();
        customerService.addContact(customerId, contact);
        assertEquals(customerId, contact.getCustomerId());
        verify(contactCollection, times(1)).insertOne(any(Document.class));
    }

    @Test
    void testGetContactsByCustomerId_ShouldReturnFilteredList() {
        when(contactCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(false);
        assertNotNull(customerService.getContactsByCustomerId("c1"));
    }

    @Test
    void testUpdateContact_ShouldCallReplace() {
        CustomerContact c = new CustomerContact();
        c.setId(new ObjectId().toString());
        customerService.updateContact(c);
        verify(contactCollection, times(1)).replaceOne(any(Bson.class), any(Document.class));
    }

    @Test
    void testDeleteContact_ShouldCallDeleteOne() {
        customerService.deleteContact(new ObjectId().toString());
        verify(contactCollection, times(1)).deleteOne(any(Bson.class));
    }

    // --- 5. READ/VIEW ALL TESTS ---

    @Test
    void testGetAllCustomers_ShouldReturnList() {
        when(customerCollection.find()).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, false);
        when(cursor.next()).thenReturn(new Document("_id", new ObjectId()).append("name", "Ali"));

        List<Customer> list = customerService.getAllCustomers();
        assertEquals(1, list.size());
    }

    @Test
    void testGetAllGroups_ShouldReturnList() {
        when(groupCollection.find()).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(false);
        assertNotNull(customerService.getAllGroups());
    }

    @Test
    void testGetAllCategories_ShouldReturnList() {
        when(categoryCollection.find()).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(false);
        assertNotNull(customerService.getAllCategories());
    }

    @Test
    void testGetAllContacts_ShouldReturnList() {
        when(contactCollection.find()).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(false);
        assertNotNull(customerService.getAllContacts());
    }
}