package com.inventory.customer;

import com.inventory.api.customer.CustomerService;
import com.inventory.api.purchaseorder.PurchaseOrderService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Component(service = CustomerService.class, immediate = true)
public class CustomerImpl implements CustomerService {

    // Storage for our customers
    private final List<String> customerList = new ArrayList<>();

    // Flag to control the menu loop
    private boolean running = true;
    
    // Purchase Order Service (optional - may not be available immediately)
    private PurchaseOrderService purchaseOrderService;

    @Activate
    public void activate() {
        System.out.println("✅ Customer Component Started.");

        // ⚠️ CRITICAL: Run the menu in a separate thread so OSGi doesn't freeze!
        new Thread(this::showMenu).start();
    }

    @Deactivate
    public void deactivate() {
        System.out.println("❌ Stopping Customer Menu...");
        running = false; // This stops the loop
    }

    // --- The Interactive Menu ---
    private void showMenu() {
        Scanner scanner = new Scanner(System.in);

        // Small delay to let the log messages finish
        try { Thread.sleep(1000); } catch (InterruptedException e) {}

        while (running) {
            System.out.println("\n===========================");
            System.out.println("   INVENTORY SYSTEM MENU   ");
            System.out.println("===========================");
            System.out.println("1. Add Customer");
            System.out.println("2. View All Customers");
            System.out.println("3. Manage Purchase Order");
            System.out.println("4. Manage Goods Receive");
            System.out.println("5. Manage Purchase Return");
            System.out.println("6. Exit System");
            System.out.print("Select an option: ");

            try {
                String input = scanner.nextLine();

                switch (input) {
                    case "1":
                        System.out.print("Enter Customer Name: ");
                        String name = scanner.nextLine();
                        addCustomer(name);
                        break;
                    case "2":
                        System.out.println("\n--- Customer List ---");
                        List<String> list = getAllCustomers();
                        if (list.isEmpty()) {
                            System.out.println("(No customers found)");
                        } else {
                            for (String c : list) {
                                System.out.println(" - " + c);
                            }
                        }
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
                        System.out.println("Exiting...");
                        running = false;
                        System.exit(0); // Shuts down the whole app
                        break;
                    default:
                        System.out.println("Invalid option, try again.");
                }
            } catch (Exception e) {
                // Ignore scanner errors during shutdown
            }
        }
    }

    // --- Implementation Methods ---

    @Override
    public void addCustomer(String name) {
        customerList.add(name);
        System.out.println("✅ Added: " + name);
    }

    @Override
    public List<String> getAllCustomers() {
        return new ArrayList<>(customerList); // Return a copy
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