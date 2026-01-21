package com.inventorymanagement;

import com.inventorymanagement.customer_gohyuheng.CustomerMenu;
import com.inventorymanagement.salesorder_wongxiuhuan.SalesOrderMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

import java.util.Scanner;

@SpringBootApplication
@EnableMongoAuditing
public class InventoryManagementApplication implements CommandLineRunner {

    // Inject new menu class
    @Autowired
    private CustomerMenu customerMenu;

    @Autowired
    private SalesOrderMenu salesOrderMenu;

    public static void main(String[] args) {
        SpringApplication.run(InventoryManagementApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
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
                    System.out.println("This module is not ready yet.");
                    break;
                case "3":
                    salesOrderMenu.start(scanner);
                    break;
                case "4":
                    System.out.println("This module is not ready yet.");
                    break;
                case "5":
                    System.out.println("This module is not ready yet.");
                    break;
                case "6":
                    System.out.println("Exiting system...");
                    System.exit(0); // Fully stops the program as now got added we dependency (latyer try postman)
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
}