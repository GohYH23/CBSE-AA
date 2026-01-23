package com.inventorymanagement;

import com.inventorymanagement.customer_gohyuheng.CustomerMenu;
import com.inventorymanagement.product_ericleechunkiat.ProductMenu;
import com.inventorymanagement.purchaseorder_ooiweiying.PurchaseOrderMenu;
import com.inventorymanagement.salesorder_wongxiuhuan.SalesOrderMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment; // ADD THIS IMPORT
import org.springframework.data.mongodb.config.EnableMongoAuditing;

import java.util.Scanner;

@SpringBootApplication
@EnableMongoAuditing
public class InventoryManagementApplication implements CommandLineRunner {

    @Autowired
    private CustomerMenu customerMenu;

    @Autowired
    private SalesOrderMenu salesOrderMenu;

    @Autowired
    private ProductMenu productMenu;

    @Autowired
    private PurchaseOrderMenu purchaseOrderMenu;
    
    @Autowired
    private Environment environment; // ADD THIS LINE

    public static void main(String[] args) {
        SpringApplication.run(InventoryManagementApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // CHECK IF RUNNING IN TEST ENVIRONMENT
        if (isTestEnvironment()) {
            System.out.println("Running in test mode - skipping interactive menu");
            return; // Exit early without showing menu
        }
        
        // ONLY SHOW MENU IN NON-TEST ENVIRONMENT
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("==========================================");
        System.out.println("   INVENTORY MANAGEMENT SYSTEM     ");
        System.out.println("==========================================");

        while (running) {
            System.out.println("\n--- MAIN MENU ---");
            System.out.println("1. Customer Module");
            System.out.println("2. Purchase Module");
            System.out.println("3. Sale Module");
            System.out.println("4. Product Module");
            System.out.println("5. Vendor Module");
            System.out.println("6. Exit");
            System.out.print("Select Module: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    customerMenu.start(scanner);
                    break;
                case "2":
                    purchaseOrderMenu.start(scanner);
                    break;
                case "3":
                    salesOrderMenu.start(scanner);
                    break;
                case "4":
                    productMenu.start();
                    break;
                case "5":
                    System.out.println("This module is not ready yet.");
                    break;
                case "6":
                    System.out.println("Exiting system...");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
    
    // ADD THIS METHOD TO DETECT TEST ENVIRONMENT
    private boolean isTestEnvironment() {
        // Method 1: Check for test profile
        String[] activeProfiles = environment.getActiveProfiles();
        for (String profile : activeProfiles) {
            if (profile.toLowerCase().contains("test")) {
                return true;
            }
        }
        
        // Method 2: Check if running from JUnit
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName().toLowerCase();
            if (className.contains("junit") || 
                className.contains("test") || 
                className.contains("runner")) {
                return true;
            }
        }
        
        // Method 3: Check system property
        String testMode = System.getProperty("test.mode");
        if (testMode != null && testMode.equalsIgnoreCase("true")) {
            return true;
        }
        
        return false;
    }
}