package com.inventory.customer;

import com.inventory.api.customer.service.CustomerDependencyChecker;
import com.inventory.api.customer.service.CustomerService;
import com.inventory.api.customer.model.Customer;
import com.inventory.api.customer.model.CustomerGroup;
import com.inventory.api.customer.model.CustomerCategory;
import com.inventory.api.customer.model.CustomerContact;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Component(service = CustomerService.class)
public class CustomerServiceImpl implements CustomerService {

    private MongoClient mongoClient;
    private MongoDatabase database;

    private MongoCollection<Document> customerCollection;
    private MongoCollection<Document> groupCollection;
    private MongoCollection<Document> categoryCollection;
    private MongoCollection<Document> contactCollection;

    private List<CustomerDependencyChecker> dependencyCheckers = new CopyOnWriteArrayList<>();

    @Reference(
            service = CustomerDependencyChecker.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC
    )
    public void bindChecker(CustomerDependencyChecker checker) {
        dependencyCheckers.add(checker);
    }

    public void unbindChecker(CustomerDependencyChecker checker) {
        dependencyCheckers.remove(checker);
    }

    @Activate
    public void activate() {
        System.out.println("Customer Service: Starting with MANUAL Mapping...");
        try {
            String uri = System.getProperty("mongodb.uri");
            if (uri == null || uri.isEmpty()) {
                System.err.println("Error: mongodb.uri not found in System Properties.");
                return;
            }

            // 1. Standard Connection (No CodecRegistry needed for Document)
            mongoClient = MongoClients.create(uri);
            database = mongoClient.getDatabase("inventory_db_osgi");

            // 2. Initialize Collections as 'Document'
            customerCollection = database.getCollection("customers");
            groupCollection = database.getCollection("customer_groups");
            categoryCollection = database.getCollection("customer_categories");
            contactCollection = database.getCollection("customer_contacts");

            System.out.println("Customer Service: Database Connected (Manual Mode).");

        } catch (Exception e) {
            System.err.println("Customer Service: Connection Failed.");
            e.printStackTrace();
        }
    }

    @Deactivate
    public void deactivate() {
        if (mongoClient != null) mongoClient.close();
        System.out.println("Customer Service: Stopped.");
    }

    // MAPPING HELPERS METHOD

    // --- CUSTOMER MAPPING ---
    private Customer mapToCustomer(Document doc) {
        if (doc == null) return null;
        Customer c = new Customer();
        // Safe ID conversion: works for both ObjectId and String in DB
        c.setId(doc.get("_id").toString());
        c.setName(doc.getString("name"));
        c.setEmail(doc.getString("email"));
        c.setPhoneNumber(doc.getString("phoneNumber"));
        c.setAddress(doc.getString("address"));
        c.setCustomerGroupId(doc.getString("customerGroupId"));
        c.setCustomerCategoryId(doc.getString("customerCategoryId"));
        c.setCreatedAt(doc.getString("createdAt"));
        c.setEditedAt(doc.getString("editedAt"));
        return c;
    }

    private Document mapFromCustomer(Customer c) {
        Document doc = new Document()
                .append("name", c.getName())
                .append("email", c.getEmail())
                .append("phoneNumber", c.getPhoneNumber())
                .append("address", c.getAddress())
                .append("customerGroupId", c.getCustomerGroupId())
                .append("customerCategoryId", c.getCustomerCategoryId())
                .append("createdAt", c.getCreatedAt())
                .append("editedAt", c.getEditedAt());
        return doc;
    }

    // --- GROUP MAPPING ---
    private CustomerGroup mapToGroup(Document doc) {
        if (doc == null) return null;
        CustomerGroup g = new CustomerGroup();
        g.setId(doc.get("_id").toString());
        g.setGroupName(doc.getString("groupName"));
        g.setDescription(doc.getString("description"));
        g.setCreatedAt(doc.getString("createdAt"));
        g.setEditedAt(doc.getString("editedAt"));
        return g;
    }

    private Document mapFromGroup(CustomerGroup g) {
        return new Document()
                .append("groupName", g.getGroupName())
                .append("description", g.getDescription())
                .append("createdAt", g.getCreatedAt())
                .append("editedAt", g.getEditedAt());
    }

    // --- CATEGORY MAPPING ---
    private CustomerCategory mapToCategory(Document doc) {
        if (doc == null) return null;
        CustomerCategory c = new CustomerCategory();
        c.setId(doc.get("_id").toString());
        c.setCategoryName(doc.getString("categoryName"));
        c.setDescription(doc.getString("description"));
        c.setCreatedAt(doc.getString("createdAt"));
        c.setEditedAt(doc.getString("editedAt"));
        return c;
    }

    private Document mapFromCategory(CustomerCategory c) {
        return new Document()
                .append("categoryName", c.getCategoryName())
                .append("description", c.getDescription())
                .append("createdAt", c.getCreatedAt())
                .append("editedAt", c.getEditedAt());
    }

    // --- CONTACT MAPPING ---
    private CustomerContact mapToContact(Document doc) {
        if (doc == null) return null;
        CustomerContact c = new CustomerContact();
        c.setId(doc.get("_id").toString());
        c.setContactName(doc.getString("contactName"));
        c.setPosition(doc.getString("position"));
        c.setPhone(doc.getString("phone"));
        c.setEmail(doc.getString("email"));
        c.setCustomerId(doc.getString("customerId"));
        c.setCreatedAt(doc.getString("createdAt"));
        c.setEditedAt(doc.getString("editedAt"));
        return c;
    }

    private Document mapFromContact(CustomerContact c) {
        return new Document()
                .append("contactName", c.getContactName())
                .append("position", c.getPosition())
                .append("phone", c.getPhone())
                .append("email", c.getEmail())
                .append("customerId", c.getCustomerId())
                .append("createdAt", c.getCreatedAt())
                .append("editedAt", c.getEditedAt());
    }

    // CUSTOMER IMPLEMENTATION

    @Override
    public void createCustomer(Customer customer) {
        if (customer.getCreatedAt() == null) {
            customer.setCreatedAt(LocalDateTime.now().toString());
        }
        Document doc = mapFromCustomer(customer);
        customerCollection.insertOne(doc);
    }

    @Override
    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
        for (Document doc : customerCollection.find()) {
            list.add(mapToCustomer(doc));
        }
        return list;
    }

    @Override
    public Optional<Customer> getCustomerByName(String name) {
        Document doc = customerCollection.find(Filters.eq("name", name)).first();
        return Optional.ofNullable(mapToCustomer(doc));
    }

    @Override
    public Optional<Customer> getCustomerById(String id) {
        try {
            Document doc = customerCollection.find(Filters.eq("_id", new ObjectId(id))).first();
            return Optional.ofNullable(mapToCustomer(doc));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    public void updateCustomer(Customer customer) {
        if (customer.getId() != null) {
            customer.setEditedAt(LocalDateTime.now().toString());
            Document doc = mapFromCustomer(customer);
            customerCollection.replaceOne(Filters.eq("_id", new ObjectId(customer.getId())), doc);
        }
    }

    @Override
    public String deleteCustomer(String id) {
        // Check if customer exists
        Customer customer = getCustomerById(id).orElse(null);
        if (customer == null) {
            return "Customer not found.";
        }

        // Ask all checkers to verify deletion
        for (CustomerDependencyChecker checker : dependencyCheckers) {
            if (checker.hasDependency(id)) {
                return "Cannot delete: " + checker.getDependencyMessage();
            }
        }

        // If no objections, proceed with delete
        customerCollection.deleteOne(Filters.eq("_id", new ObjectId(id)));
        contactCollection.deleteMany(Filters.eq("customerId", id));

        return "Customer deleted successfully.";
    }

    // GROUPS IMPLEMENTATION

    @Override
    public void createGroup(CustomerGroup group) {
        if (group.getCreatedAt() == null) group.setCreatedAt(LocalDateTime.now().toString());
        Document doc = mapFromGroup(group);
        groupCollection.insertOne(doc);
    }

    @Override
    public List<CustomerGroup> getAllGroups() {
        List<CustomerGroup> list = new ArrayList<>();
        for (Document doc : groupCollection.find()) {
            list.add(mapToGroup(doc));
        }
        return list;
    }

    @Override
    public Optional<CustomerGroup> getGroupByName(String name) {
        Document doc = groupCollection.find(Filters.eq("groupName", name)).first();
        return Optional.ofNullable(mapToGroup(doc));
    }

    @Override
    public Optional<CustomerGroup> getGroupById(String id) {
        try {
            Document doc = groupCollection.find(Filters.eq("_id", new ObjectId(id))).first();
            return Optional.ofNullable(mapToGroup(doc));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    public void updateGroup(CustomerGroup group) {
        if (group.getId() != null) {
            group.setEditedAt(LocalDateTime.now().toString());
            Document doc = mapFromGroup(group);
            groupCollection.replaceOne(Filters.eq("_id", new ObjectId(group.getId())), doc);
        }
    }

    @Override
    public String deleteGroup(String id) {
        long count = customerCollection.countDocuments(Filters.eq("customerGroupId", id));
        if (count > 0) {
            return "Cannot delete: Group is assigned to " + count + " customers.";
        }
        try {
            DeleteResult res = groupCollection.deleteOne(Filters.eq("_id", new ObjectId(id)));
            return res.getDeletedCount() > 0 ? "Group deleted." : "Group not found.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    //CATEGORIES IMPLEMENTATION

    @Override
    public void createCategory(CustomerCategory category) {
        if (category.getCreatedAt() == null) category.setCreatedAt(LocalDateTime.now().toString());
        Document doc = mapFromCategory(category);
        categoryCollection.insertOne(doc);
    }

    @Override
    public List<CustomerCategory> getAllCategories() {
        List<CustomerCategory> list = new ArrayList<>();
        for (Document doc : categoryCollection.find()) {
            list.add(mapToCategory(doc));
        }
        return list;
    }

    @Override
    public Optional<CustomerCategory> getCategoryByName(String name) {
        Document doc = categoryCollection.find(Filters.eq("categoryName", name)).first();
        return Optional.ofNullable(mapToCategory(doc));
    }

    @Override
    public Optional<CustomerCategory> getCategoryById(String id) {
        try {
            Document doc = categoryCollection.find(Filters.eq("_id", new ObjectId(id))).first();
            return Optional.ofNullable(mapToCategory(doc));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    public void updateCategory(CustomerCategory category) {
        if (category.getId() != null) {
            category.setEditedAt(LocalDateTime.now().toString());
            Document doc = mapFromCategory(category);
            categoryCollection.replaceOne(Filters.eq("_id", new ObjectId(category.getId())), doc);
        }
    }

    @Override
    public String deleteCategory(String id) {
        long count = customerCollection.countDocuments(Filters.eq("customerCategoryId", id));
        if (count > 0) {
            return "Cannot delete: Category is assigned to " + count + " customers.";
        }
        try {
            DeleteResult res = categoryCollection.deleteOne(Filters.eq("_id", new ObjectId(id)));
            return res.getDeletedCount() > 0 ? "Category deleted." : "Category not found.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CONTACTS IMPLEMENTATION
    @Override
    public List<CustomerContact> getAllContacts() {
        List<CustomerContact> list = new ArrayList<>();
        for (Document doc : contactCollection.find()) {
            list.add(mapToContact(doc));
        }
        return list;
    }

    @Override
    public List<CustomerContact> getContactsByCustomerId(String customerId) {
        List<CustomerContact> list = new ArrayList<>();
        for (Document doc : contactCollection.find(Filters.eq("customerId", customerId))) {
            list.add(mapToContact(doc));
        }
        return list;
    }

    @Override
    public void addContact(String customerId, CustomerContact contact) {
        contact.setCustomerId(customerId);
        if (contact.getCreatedAt() == null) contact.setCreatedAt(LocalDateTime.now().toString());
        Document doc = mapFromContact(contact);
        contactCollection.insertOne(doc);
    }

    @Override
    public void updateContact(CustomerContact contact) {
        if (contact.getId() != null) {
            contact.setEditedAt(LocalDateTime.now().toString());
            Document doc = mapFromContact(contact);
            contactCollection.replaceOne(Filters.eq("_id", new ObjectId(contact.getId())), doc);
        }
    }

    @Override
    public void deleteContact(String id) {
        try {
            contactCollection.deleteOne(Filters.eq("_id", new ObjectId(id)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}