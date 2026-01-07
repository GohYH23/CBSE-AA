package com.inventory.customer;

import com.inventory.api.customer.CustomerService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Component(service = CustomerService.class, immediate = true)
public class CustomerImpl implements CustomerService {

    // Storage for our customers
    private final List<String> customerList = new ArrayList<>();

    // Flag to control the menu loop
    private boolean running = true;

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
            System.out.println("3. Exit System");
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
}