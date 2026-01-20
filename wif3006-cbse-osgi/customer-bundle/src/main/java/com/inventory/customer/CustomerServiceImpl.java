package com.inventory.customer;

import com.inventory.api.customer.service.CustomerService;
import com.inventory.api.customer.model.Customer;
import com.inventory.api.customer.model.CustomerGroup;
import com.inventory.api.customer.model.CustomerCategory;
import com.inventory.api.customer.model.CustomerContact;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;

import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Component(service = CustomerService.class)
public class CustomerServiceImpl implements CustomerService {

    private MongoClient mongoClient;

    // Collections
    private MongoCollection<Customer> customerCollection;
    private MongoCollection<CustomerGroup> groupCollection;
    private MongoCollection<CustomerCategory> categoryCollection;
    private MongoCollection<CustomerContact> contactCollection;

    @Activate
    public void activate() {
        System.out.println("✅ Customer Service: Starting...");
        try {
            String uri = System.getProperty("mongodb.uri");
            if (uri == null || uri.isEmpty()) {
                System.err.println("❌ Error: mongodb.uri not found in System Properties.");
                return;
            }

            // Configure Codec to handle POJOs automatically
            CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
            CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);

            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(uri))
                    .codecRegistry(codecRegistry)
                    .build();

            mongoClient = MongoClients.create(settings);
            MongoDatabase database = mongoClient.getDatabase("inventory_db_osgi");

            // Initialize Collections mapped to the API classes
            customerCollection = database.getCollection("customers", Customer.class);
            groupCollection = database.getCollection("customer_groups", CustomerGroup.class);
            categoryCollection = database.getCollection("customer_categories", CustomerCategory.class);
            contactCollection = database.getCollection("customer_contacts", CustomerContact.class);

            System.out.println("✅ Customer Service: Database Connected.");

        } catch (Exception e) {
            System.err.println("❌ Customer Service: Connection Failed.");
            e.printStackTrace();
        }
    }

    @Deactivate
    public void deactivate() {
        if (mongoClient != null) mongoClient.close();
        System.out.println("🛑 Customer Service: Stopped.");
    }

    // ================= CUSTOMERS =================

    @Override
    public void createCustomer(Customer customer) {
        customerCollection.insertOne(customer);
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerCollection.find().into(new ArrayList<>());
    }

    @Override
    public Optional<Customer> getCustomerByName(String name) {
        return Optional.ofNullable(customerCollection.find(Filters.eq("name", name)).first());
    }

    @Override
    public Optional<Customer> getCustomerById(String id) {
        return Optional.ofNullable(customerCollection.find(Filters.eq("_id", id)).first());
    }

    @Override
    public void updateCustomer(Customer customer) {
        if (customer.getId() != null) {
            customerCollection.replaceOne(Filters.eq("_id", customer.getId()), customer);
        }
    }

    @Override
    public void deleteCustomer(String id) {
        customerCollection.deleteOne(Filters.eq("_id", id));
    }

    // ================= GROUPS =================

    @Override
    public void createGroup(CustomerGroup group) {
        groupCollection.insertOne(group);
    }

    @Override
    public List<CustomerGroup> getAllGroups() {
        return groupCollection.find().into(new ArrayList<>());
    }

    @Override
    public Optional<CustomerGroup> getGroupByName(String name) {
        return Optional.ofNullable(groupCollection.find(Filters.eq("groupName", name)).first());
    }

    @Override
    public Optional<CustomerGroup> getGroupById(String id) {
        return Optional.ofNullable(groupCollection.find(Filters.eq("_id", id)).first());
    }

    @Override
    public void updateGroup(CustomerGroup group) {
        if (group.getId() != null) {
            groupCollection.replaceOne(Filters.eq("_id", group.getId()), group);
        }
    }

    @Override
    public String deleteGroup(String id) {
        // Check if any customer uses this group
        long count = customerCollection.countDocuments(Filters.eq("customerGroupId", id));
        if (count > 0) {
            return "Cannot delete: Group is assigned to " + count + " customers.";
        }
        DeleteResult result = groupCollection.deleteOne(Filters.eq("_id", id));
        return result.getDeletedCount() > 0 ? "Group deleted." : "Group not found.";
    }

    // ================= CATEGORIES =================

    @Override
    public void createCategory(CustomerCategory category) {
        categoryCollection.insertOne(category);
    }

    @Override
    public List<CustomerCategory> getAllCategories() {
        return categoryCollection.find().into(new ArrayList<>());
    }

    @Override
    public Optional<CustomerCategory> getCategoryByName(String name) {
        return Optional.ofNullable(categoryCollection.find(Filters.eq("categoryName", name)).first());
    }

    @Override
    public Optional<CustomerCategory> getCategoryById(String id) {
        return Optional.ofNullable(categoryCollection.find(Filters.eq("_id", id)).first());
    }

    @Override
    public void updateCategory(CustomerCategory category) {
        if (category.getId() != null) {
            categoryCollection.replaceOne(Filters.eq("_id", category.getId()), category);
        }
    }

    @Override
    public String deleteCategory(String id) {
        // Check if any customer uses this category
        long count = customerCollection.countDocuments(Filters.eq("customerCategoryId", id));
        if (count > 0) {
            return "Cannot delete: Category is assigned to " + count + " customers.";
        }
        DeleteResult result = categoryCollection.deleteOne(Filters.eq("_id", id));
        return result.getDeletedCount() > 0 ? "Category deleted." : "Category not found.";
    }

    // ================= CONTACTS =================

    @Override
    public List<CustomerContact> getAllContacts() {
        return contactCollection.find().into(new ArrayList<>());
    }

    @Override
    public List<CustomerContact> getContactsByCustomerId(String customerId) {
        return contactCollection.find(Filters.eq("customerId", customerId)).into(new ArrayList<>());
    }

    @Override
    public void addContact(String customerId, CustomerContact contact) {
        contact.setCustomerId(customerId);
        contactCollection.insertOne(contact);
    }

    @Override
    public void updateContact(CustomerContact contact) {
        if (contact.getId() != null) {
            contactCollection.replaceOne(Filters.eq("_id", contact.getId()), contact);
        }
    }

    @Override
    public void deleteContact(String id) {
        contactCollection.deleteOne(Filters.eq("_id", id));
    }
}

//package com.inventory.customer;
//
//import com.inventory.api.customer.CustomerService;
//import com.inventory.api.purchaseorder.PurchaseOrderService;
//import com.inventory.api.salesorder.SalesOrderService;
//import com.mongodb.client.MongoClient;
//import com.mongodb.client.MongoClients;
//import com.mongodb.client.MongoCollection;
//import com.mongodb.client.MongoDatabase;
//import org.bson.Document;
//import org.osgi.service.component.annotations.*;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Scanner;
//
//@Component(service = CustomerService.class, immediate = true)
//public class CustomerServiceImpl implements CustomerService {
//
//    // --- MongoDB Fields ---
//    private MongoClient mongoClient;
//    private MongoCollection<Document> customerCollection;
//
//    private boolean running = true;
//    private PurchaseOrderService purchaseOrderService;
//    private SalesOrderService salesOrderService;
//
//    @Activate
//    public void activate() {
//        System.out.println("✅ Customer Component Starting...");
//
//        // 1. Get the Connection String from Launcher
//        String uri = System.getProperty("mongodb.uri");
//
//        if (uri == null || uri.isEmpty()) {
//            System.err.println("❌ ERROR: 'mongodb.uri' not found! Please check Launcher & osgi.properties.");
//        } else {
//            try {
//                // 2. Connect to Cloud
//                mongoClient = MongoClients.create(uri);
//                MongoDatabase database = mongoClient.getDatabase("inventory_db_osgi");
//                customerCollection = database.getCollection("customers");
//                System.out.println("   ✅ Connected to MongoDB: inventory_db_osgi");
//            } catch (Exception e) {
//                System.err.println("   ❌ Connection Failed: " + e.getMessage());
//            }
//        }
//
//        // Run menu in background thread
//        new Thread(this::showMenu).start();
//    }
//
//    @Deactivate
//    public void deactivate() {
//        System.out.println("❌ Stopping Customer Menu...");
//        running = false;
//
//        // 3. Close Connection
//        if (mongoClient != null) {
//            mongoClient.close();
//            System.out.println("   ✅ MongoDB Connection Closed.");
//        }
//    }
//
//    // --- Implementation Methods (Updated for MongoDB) ---
//
//    @Override
//    public void addCustomer(String name) {
//        if (customerCollection != null) {
//            Document doc = new Document("name", name);
//            customerCollection.insertOne(doc);
//            System.out.println("✅ Saved to Cloud: " + name);
//        } else {
//            System.out.println("⚠️ Database not connected. Cannot save.");
//        }
//    }
//
//    @Override
//    public List<String> getAllCustomers() {
//        List<String> list = new ArrayList<>();
//        if (customerCollection != null) {
//            for (Document doc : customerCollection.find()) {
//                list.add(doc.getString("name"));
//            }
//        }
//        return list;
//    }
//
//    // --- The Interactive Menu (Unchanged from your code) ---
//    private void showMenu() {
//        Scanner scanner = new Scanner(System.in);
//        try { Thread.sleep(1000); } catch (InterruptedException e) {}
//
//        while (running) {
//            System.out.println("\n=== INVENTORY SYSTEM (MongoDB Connected) ===");
//            System.out.println("1. Add Customer");
//            System.out.println("2. View All Customers");
//            System.out.println("3. Manage Purchase Order");
//            System.out.println("4. Manage Goods Receive");
//            System.out.println("5. Manage Purchase Return");
//            System.out.println("6. Manage Sales Order");
//            System.out.println("7. Manage Delivery Order");
//            System.out.println("8. Manage Sales Return");
//            System.out.println("9. Exit System");
//            System.out.print("Select: ");
//
//            try {
//                String input = scanner.nextLine();
//                switch (input) {
//                    case "1":
//                        System.out.print("Enter Customer Name: ");
//                        addCustomer(scanner.nextLine());
//                        break;
//                    case "2":
//                        System.out.println("\n--- Customer List (From Cloud) ---");
//                        List<String> list = getAllCustomers();
//                        for (String c : list) System.out.println(" - " + c);
//                        break;
//                    case "3":
//                        // Purchase Order
//                        if (purchaseOrderService != null) {
//                            try {
//                                // Access the menu method - need to cast to implementation
//                                java.lang.reflect.Method menuMethod = purchaseOrderService.getClass()
//                                        .getMethod("showPurchaseOrderMenu", Scanner.class);
//                                menuMethod.invoke(purchaseOrderService, scanner);
//                            } catch (Exception e) {
//                                System.out.println("Error accessing Purchase Order menu: " + e.getMessage());
//                                System.out.println("Purchase Order service may not be available.");
//                            }
//                        } else {
//                            System.out.println("Purchase Order service is not available yet.");
//                        }
//                        break;
//                    case "4":
//                        // Manage Goods Receive
//                        if (purchaseOrderService != null) {
//                            try {
//                                // Access the Goods Receive menu method
//                                java.lang.reflect.Method menuMethod = purchaseOrderService.getClass()
//                                        .getMethod("showGoodsReceiveMenu", Scanner.class);
//                                menuMethod.invoke(purchaseOrderService, scanner);
//                            } catch (Exception e) {
//                                System.out.println("Error accessing Goods Receive menu: " + e.getMessage());
//                                System.out.println("Purchase Order service may not be available.");
//                            }
//                        } else {
//                            System.out.println("Purchase Order service is not available yet.");
//                        }
//                        break;
//                    case "5":
//                        // Manage Purchase Return
//                        if (purchaseOrderService != null) {
//                            try {
//                                // Access the Purchase Return menu method
//                                java.lang.reflect.Method menuMethod = purchaseOrderService.getClass()
//                                        .getMethod("showPurchaseReturnMenu", Scanner.class);
//                                menuMethod.invoke(purchaseOrderService, scanner);
//                            } catch (Exception e) {
//                                System.out.println("Error accessing Purchase Return menu: " + e.getMessage());
//                                System.out.println("Purchase Order service may not be available.");
//                            }
//                        } else {
//                            System.out.println("Purchase Order service is not available yet.");
//                        }
//                        break;
//                    case "6":
//                        // Sales Order
//                        if (salesOrderService != null) {
//                            try {
//                                java.lang.reflect.Method menuMethod = salesOrderService.getClass()
//                                        .getMethod("showSalesOrderMenu", Scanner.class);
//                                menuMethod.invoke(salesOrderService, scanner);
//                            } catch (Exception e) {
//                                System.out.println("Error accessing Sales Order menu: " + e.getMessage());
//                            }
//                        } else {
//                            System.out.println("Sales Order service is not available yet.");
//                        }
//                        break;
//                    case "7":
//                        // Delivery Order
//                        if (salesOrderService != null) {
//                            try {
//                                java.lang.reflect.Method menuMethod = salesOrderService.getClass()
//                                        .getMethod("showDeliveryOrderMenu", Scanner.class);
//                                menuMethod.invoke(salesOrderService, scanner);
//                            } catch (Exception e) {
//                                System.out.println("Error accessing Delivery Order menu: " + e.getMessage());
//                            }
//                        } else {
//                            System.out.println("Sales Order service is not available yet.");
//                        }
//                        break;
//                    case "8":
//                        // Sales Return
//                        if (salesOrderService != null) {
//                            try {
//                                java.lang.reflect.Method menuMethod = salesOrderService.getClass()
//                                        .getMethod("showSalesReturnMenu", Scanner.class);
//                                menuMethod.invoke(salesOrderService, scanner);
//                            } catch (Exception e) {
//                                System.out.println("Error accessing Sales Return menu: " + e.getMessage());
//                            }
//                        } else {
//                            System.out.println("Sales Order service is not available yet.");
//                        }
//                        break;
//                    case "9":
//                        System.out.println("Exiting...");
//                        running = false;
//                        System.exit(0);
//                        break;
//                    default:
//                        System.out.println("Invalid option, try again.");
//                }
//            } catch (Exception e) {}
//        }
//    }
//
//    // OSGi Service Reference - Purchase Order Service
//    @Reference(
//            cardinality = ReferenceCardinality.OPTIONAL,
//            policy = ReferencePolicy.DYNAMIC,
//            unbind = "unbindPurchaseOrderService"
//    )
//    protected void bindPurchaseOrderService(PurchaseOrderService service) {
//        this.purchaseOrderService = service;
//        System.out.println("   ✅ Purchase Order Service bound to Customer Menu");
//    }
//    protected void unbindPurchaseOrderService(PurchaseOrderService service) {
//        this.purchaseOrderService = null;
//        System.out.println("   ❌ Purchase Order Service unbound from Customer Menu");
//    }
//
//    // --- Added Sales Service Binding ---
//    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, unbind = "unbindSalesOrderService")
//    protected void bindSalesOrderService(SalesOrderService service) {
//        this.salesOrderService = service;
//        System.out.println("   ✅ Sales Order Service bound to Customer Menu");
//    }
//
//    protected void unbindSalesOrderService(SalesOrderService service) {
//        this.salesOrderService = null;
//        System.out.println("   ❌ Sales Order Service unbound from Customer Menu");
//    }
//}