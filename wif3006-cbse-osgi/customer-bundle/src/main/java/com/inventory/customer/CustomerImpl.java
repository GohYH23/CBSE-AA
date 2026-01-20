package com.inventory.customer;

import com.inventory.api.customer.CustomerService;
import com.inventory.api.purchaseorder.PurchaseOrderService;
import com.inventory.api.salesorder.SalesOrderService;
import com.inventory.api.product.ProductService;
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
    private SalesOrderService salesOrderService;
    private ProductService productService;

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
            System.out.println("4. Manage Goods Receive");
            System.out.println("5. Manage Purchase Return");
            System.out.println("6. Manage Sales Order");     
            System.out.println("7. Manage Delivery Order");  
            System.out.println("8. Manage Sales Return");
            System.out.println("9. Manage Products");
            System.out.println("10. Exit System");
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
                        // Sales Order
                        if (salesOrderService != null) {
                            try {
                                java.lang.reflect.Method menuMethod = salesOrderService.getClass()
                                    .getMethod("showSalesOrderMenu", Scanner.class);
                                menuMethod.invoke(salesOrderService, scanner);
                            } catch (Exception e) {
                                System.out.println("Error accessing Sales Order menu: " + e.getMessage());
                            }
                        } else {
                            System.out.println("Sales Order service is not available yet.");
                        }
                        break;
                    case "7":
                        // Delivery Order
                        if (salesOrderService != null) {
                            try {
                                java.lang.reflect.Method menuMethod = salesOrderService.getClass()
                                    .getMethod("showDeliveryOrderMenu", Scanner.class);
                                menuMethod.invoke(salesOrderService, scanner);
                            } catch (Exception e) {
                                System.out.println("Error accessing Delivery Order menu: " + e.getMessage());
                            }
                        } else {
                            System.out.println("Sales Order service is not available yet.");
                        }
                        break;
                    case "8":
                        // Sales Return
                        if (salesOrderService != null) {
                            try {
                                java.lang.reflect.Method menuMethod = salesOrderService.getClass()
                                    .getMethod("showSalesReturnMenu", Scanner.class);
                                menuMethod.invoke(salesOrderService, scanner);
                            } catch (Exception e) {
                                System.out.println("Error accessing Sales Return menu: " + e.getMessage());
                            }
                        } else {
                            System.out.println("Sales Order service is not available yet.");
                        }
                        break;
                    case "9":
                        if (productService != null) {
                            productService.showMenu(scanner);
                        } else {
                            System.out.println("⚠️ Product Service is not available yet.");
                        }
                        break;
                    case "10":
                        System.out.println("Exiting...");
                        running = false;
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Invalid option, try again.");
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

    // --- Added Sales Service Binding ---
    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, unbind = "unbindSalesOrderService")
    protected void bindSalesOrderService(SalesOrderService service) {
        this.salesOrderService = service;
        System.out.println("   ✅ Sales Order Service bound to Customer Menu");
    }
    
    protected void unbindSalesOrderService(SalesOrderService service) {
        this.salesOrderService = null;
        System.out.println("   ❌ Sales Order Service unbound from Customer Menu");
    }

    // --- Added Product Service Binding ---
    @Reference(
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unbindProductService"
    )
    protected void bindProductService(ProductService service) {
        this.productService = service;
        System.out.println("   ✅ Product Service bound to Customer Menu");
    }

    protected void unbindProductService(ProductService service) {
        this.productService = null;
        System.out.println("   ❌ Product Service unbound from Customer Menu");
    }
}