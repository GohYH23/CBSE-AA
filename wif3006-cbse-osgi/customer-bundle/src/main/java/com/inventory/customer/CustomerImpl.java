package com.inventory.customer;

import com.inventory.api.customer.CustomerService;
import com.inventory.api.purchaseorder.PurchaseOrderService;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.osgi.service.component.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Component(service = CustomerService.class, immediate = true)
public class CustomerImpl implements CustomerService {

    // --- MongoDB Fields ---
    private MongoClient mongoClient;
    private MongoCollection<Document> customerCollection;

    private boolean running = true;
    private PurchaseOrderService purchaseOrderService;

    @Activate
    public void activate() {
        System.out.println("✅ Customer Component Starting...");

        // 1. Get the Connection String from Launcher
        String uri = System.getProperty("mongodb.uri");

        if (uri == null || uri.isEmpty()) {
            System.err.println("❌ ERROR: 'mongodb.uri' not found! Please check Launcher & osgi.properties.");
        } else {
            try {
                // 2. Connect to Cloud
                mongoClient = MongoClients.create(uri);
                MongoDatabase database = mongoClient.getDatabase("inventory_db_osgi");
                customerCollection = database.getCollection("customers");
                System.out.println("   ✅ Connected to MongoDB: inventory_db_osgi");
            } catch (Exception e) {
                System.err.println("   ❌ Connection Failed: " + e.getMessage());
            }
        }

        // Run menu in background thread
        new Thread(this::showMenu).start();
    }

    @Deactivate
    public void deactivate() {
        System.out.println("❌ Stopping Customer Menu...");
        running = false;

        // 3. Close Connection
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("   ✅ MongoDB Connection Closed.");
        }
    }

    // --- Implementation Methods (Updated for MongoDB) ---

    @Override
    public void addCustomer(String name) {
        if (customerCollection != null) {
            Document doc = new Document("name", name);
            customerCollection.insertOne(doc);
            System.out.println("✅ Saved to Cloud: " + name);
        } else {
            System.out.println("⚠️ Database not connected. Cannot save.");
        }
    }

    @Override
    public List<String> getAllCustomers() {
        List<String> list = new ArrayList<>();
        if (customerCollection != null) {
            for (Document doc : customerCollection.find()) {
                list.add(doc.getString("name"));
            }
        }
        return list;
    }

    // --- The Interactive Menu (Unchanged from your code) ---
    private void showMenu() {
        Scanner scanner = new Scanner(System.in);
        try { Thread.sleep(1000); } catch (InterruptedException e) {}

        while (running) {
            System.out.println("\n=== INVENTORY SYSTEM (MongoDB Connected) ===");
            System.out.println("1. Add Customer");
            System.out.println("2. View All Customers");
            System.out.println("3. Manage Purchase Order");
            System.out.println("6. Exit System");
            System.out.print("Select: ");

            try {
                String input = scanner.nextLine();
                switch (input) {
                    case "1":
                        System.out.print("Enter Customer Name: ");
                        addCustomer(scanner.nextLine());
                        break;
                    case "2":
                        System.out.println("\n--- Customer List (From Cloud) ---");
                        List<String> list = getAllCustomers();
                        for (String c : list) System.out.println(" - " + c);
                        break;
                    case "3":
                        // Purchase Order
                        if (purchaseOrderService != null) {
                            try {
                                // Access the menu method - need to cast to implementation
                                java.lang.reflect.Method menuMethod = purchaseOrderService.getClass()
                                        .getMethod("showPurchaseOrderMenu", Scanner.class);
                                menuMethod.invoke(purchaseOrderService, scanner);
                            } catch (Exception e) {
                                System.out.println("Error accessing Purchase Order menu: " + e.getMessage());
                                System.out.println("Purchase Order service may not be available.");
                            }
                        } else {
                            System.out.println("Purchase Order service is not available yet.");
                        }
                        break;
                    case "4":
                        // Manage Goods Receive
                        if (purchaseOrderService != null) {
                            try {
                                // Access the Goods Receive menu method
                                java.lang.reflect.Method menuMethod = purchaseOrderService.getClass()
                                        .getMethod("showGoodsReceiveMenu", Scanner.class);
                                menuMethod.invoke(purchaseOrderService, scanner);
                            } catch (Exception e) {
                                System.out.println("Error accessing Goods Receive menu: " + e.getMessage());
                                System.out.println("Purchase Order service may not be available.");
                            }
                        } else {
                            System.out.println("Purchase Order service is not available yet.");
                        }
                        break;
                    case "5":
                        // Manage Purchase Return
                        if (purchaseOrderService != null) {
                            try {
                                // Access the Purchase Return menu method
                                java.lang.reflect.Method menuMethod = purchaseOrderService.getClass()
                                        .getMethod("showPurchaseReturnMenu", Scanner.class);
                                menuMethod.invoke(purchaseOrderService, scanner);
                            } catch (Exception e) {
                                System.out.println("Error accessing Purchase Return menu: " + e.getMessage());
                                System.out.println("Purchase Order service may not be available.");
                            }
                        } else {
                            System.out.println("Purchase Order service is not available yet.");
                        }
                        break;
                    case "6":
                        running = false;
                        System.exit(0);
                        break;
                    default: System.out.println("Invalid option.");
                }
            } catch (Exception e) {}
        }
    }

    // OSGi Service Reference - Purchase Order Service
    @Reference(
        cardinality = ReferenceCardinality.OPTIONAL,
        policy = ReferencePolicy.DYNAMIC,
        unbind = "unbindPurchaseOrderService"
    )
    protected void bindPurchaseOrderService(PurchaseOrderService service) {
        this.purchaseOrderService = service;
        System.out.println("   ✅ Purchase Order Service bound to Customer Menu");
    }
    protected void unbindPurchaseOrderService(PurchaseOrderService service) {
        this.purchaseOrderService = null;
        System.out.println("   ❌ Purchase Order Service unbound from Customer Menu");
    }
}