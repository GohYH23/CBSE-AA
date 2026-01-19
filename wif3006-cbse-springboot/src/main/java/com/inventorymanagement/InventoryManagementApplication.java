package com.inventorymanagement;

import com.inventorymanagement.customer.Customer;
import com.inventorymanagement.customer.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.Scanner;

@SpringBootApplication

public class InventoryManagementApplication implements CommandLineRunner {

    @Autowired
    private CustomerRepository customerRepository;

    public static void main(String[] args) {
        SpringApplication.run(InventoryManagementApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("==========================================");
        System.out.println("   INVENTORY MANAGEMENT (TERMINAL MODE)   ");
        System.out.println("==========================================");

        while (running) {
            System.out.println("\nSelect an option:");
            System.out.println("1. View All Customers");
            System.out.println("2. Add New Customer");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    viewCustomers();
                    break;
                case "2":
                    addCustomer(scanner);
                    break;
                case "3":
                    running = false;
                    System.out.println("Exiting system...");
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    private void viewCustomers() {
        List<Customer> customers = customerRepository.findAll();
        if (customers.isEmpty()) {
            System.out.println("\n[!] No customers found in database.");
        } else {
            System.out.println("\n--- Customer List ---");
            for (Customer c : customers) {
                System.out.println(c);
            }
        }
    }

    private void addCustomer(Scanner scanner) {
        System.out.print("Enter Name: ");
        String name = scanner.nextLine();

        System.out.print("Enter Email: ");
        String email = scanner.nextLine();

        System.out.print("Enter Category (VIP/Regular): ");
        String category = scanner.nextLine();

        Customer newCustomer = new Customer(name, email, category);
        customerRepository.save(newCustomer);
        System.out.println("✅ Success! Customer saved to MongoDB.");
    }
}