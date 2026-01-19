package com.inventorymanagement.customer_gohyuheng;

import com.inventorymanagement.customer_gohyuheng.model.*;
import com.inventorymanagement.customer_gohyuheng.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@Component
public class CustomerMenu {

    @Autowired
    private CustomerService customerService;

    public void start(Scanner scanner) {
        boolean back = false;
        while (!back) {
            System.out.println("\n===========================");
            System.out.println("   CUSTOMER MODULE MENU    ");
            System.out.println("===========================");
            System.out.println("1. Manage Customers");
            System.out.println("2. Manage Customer Groups");
            System.out.println("3. Manage Customer Categories");
            System.out.println("4. Manage Customer Contacts");
            System.out.println("5. Back to Main Menu");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1": handleCustomerSubMenu(scanner); break;
                case "2": handleGroupSubMenu(scanner); break;
                case "3": handleCategorySubMenu(scanner); break;
                case "4": handleContactSubMenu(scanner); break;
                case "5": back = true; break;
                default: System.out.println("Invalid option. Please try again");
            }
        }
    }

    // --- 1. CUSTOMER SUB-MENU ---
    private void handleCustomerSubMenu(Scanner scanner) {
        System.out.println("\n--- MANAGE CUSTOMERS ---");
        System.out.println("1. View All Customers");
        System.out.println("2. Add New Customer");
        System.out.println("3. Delete Customer");
        System.out.print("Enter choice: ");
        String choice = scanner.nextLine();

        if (choice.equals("1")) {
            List<Customer> customers = customerService.getAllCustomers();
            if (customers.isEmpty()) {
                System.out.println("No customers found.");
            } else {
                customers.forEach(System.out::println);
            }
        } else if (choice.equals("2")) {
            System.out.print("Enter Name: ");
            String name = scanner.nextLine();
            System.out.print("Enter Email: ");
            String email = scanner.nextLine();
            System.out.print("Enter Phone: ");
            String phone = scanner.nextLine();
            System.out.print("Enter Address: ");
            String address = scanner.nextLine();

            // Link Group using Optional
            System.out.print("Enter Group Name (e.g., Corporate): ");
            String groupName = scanner.nextLine();
            Optional<CustomerGroup> groupOpt = customerService.getGroupByName(groupName);
            if (groupOpt.isEmpty()) { // Changed from == null
                System.out.println("Unable to proceed: Group '" + groupName + "' not found. Create it first.");
                return;
            }

            // Link Category using Optional
            System.out.print("Enter Category Name (e.g., Startup): ");
            String catName = scanner.nextLine();
            Optional<CustomerCategory> catOpt = customerService.getCategoryByName(catName);
            if (catOpt.isEmpty()) { // Changed from == null
                System.out.println("Unable to proceed: Category '" + catName + "' not found. Create it first.");
                return;
            }

            Customer newCustomer = new Customer();
            newCustomer.setName(name);
            newCustomer.setEmail(email);
            newCustomer.setPhoneNumber(phone);
            newCustomer.setAddress(address);
            newCustomer.setCustomerGroupId(groupOpt.get().getId());
            newCustomer.setCustomerCategoryId(catOpt.get().getId());

            customerService.createCustomer(newCustomer);
            System.out.println("Customer save successfully!");
        } else if (choice.equals("3")) {
            System.out.print("Enter Customer Name to DELETE: ");
            String name = scanner.nextLine();
            Optional<Customer> opt = customerService.getCustomerByName(name);
            if (opt.isPresent()) {
                customerService.deleteCustomer(opt.get().getId());
                System.out.println("Customer '" + name + "' and their contacts is successfully deleted.");
            } else {
                System.out.println("Customer trying deleted is not found.");
            }
        }
    }

    // --- 2. GROUP SUB-MENU ---
    private void handleGroupSubMenu(Scanner scanner) {
        System.out.println("\n--- MANAGE GROUPS ---");
        System.out.println("1. View Groups");
        System.out.println("2. Create Group");
        System.out.println("3. Delete Group");
        System.out.print("Enter choice: ");
        String choice = scanner.nextLine();

        if (choice.equals("1")) {
            customerService.getAllGroups().forEach(g ->
                    System.out.println("- Name: " + g.getGroupName() + " | Desc: " + g.getDescription()));
        } else if (choice.equals("2")) {
            System.out.print("Enter Group Name: ");
            String name = scanner.nextLine();
            System.out.print("Enter Description: ");
            String desc = scanner.nextLine();

            CustomerGroup g = new CustomerGroup();
            g.setGroupName(name);
            g.setDescription(desc);
            customerService.createGroup(g);
            System.out.println("✅ Group Created!");
        } else if (choice.equals("3")) {
            System.out.print("Enter Group Name to DELETE: ");
            String name = scanner.nextLine();
            Optional<CustomerGroup> opt = customerService.getGroupByName(name);
            if (opt.isPresent()) {
                customerService.deleteGroup(opt.get().getId());
                System.out.println("Group '" + name + "' is successfully deleted.");
            } else {
                System.out.println("Group trying deleted is not found.");
            }
        }
    }

    // --- 3. CATEGORY SUB-MENU ---
    private void handleCategorySubMenu(Scanner scanner) {
        System.out.println("\n--- MANAGE CATEGORIES ---");
        System.out.println("1. View Categories");
        System.out.println("2. Create Category");
        System.out.println("3. Delete Category");
        System.out.print("Enter choice: ");
        String choice = scanner.nextLine();

        if (choice.equals("1")) {
            customerService.getAllCategories().forEach(c ->
                    System.out.println("- Name: " + c.getCategoryName() + " | Desc: " + c.getDescription()));
        } else if (choice.equals("2")) {
            System.out.print("Enter Category Name: ");
            String name = scanner.nextLine();
            System.out.print("Enter Description: ");
            String desc = scanner.nextLine();

            CustomerCategory c = new CustomerCategory();
            c.setCategoryName(name);
            c.setDescription(desc);
            customerService.createCategory(c);
            System.out.println("✅ Category Created!");
        } else if (choice.equals("3")) {
            System.out.print("Enter Category Name to DELETE: ");
            String name = scanner.nextLine();
            Optional<CustomerCategory> opt = customerService.getCategoryByName(name);
            if (opt.isPresent()) {
                customerService.deleteCategory(opt.get().getId());
                System.out.println("Category '" + name + "' is successfully deleted.");
            } else {
                System.out.println("Category trying deleted is not found.");
            }
        }
    }

    // --- 4. CONTACT SUB-MENU ---
    private void handleContactSubMenu(Scanner scanner) {
        System.out.println("\n--- MANAGE CONTACTS ---");
        System.out.println("1. View Contacts by Customer Name");
        System.out.println("2. Add Contact by Customer Name");
        System.out.println("3. Delete Contact");
        System.out.print("Enter choice: ");
        String choice = scanner.nextLine();

        // 1. Check if the choice is valid BEFORE asking for the name
        if (!choice.equals("1") && !choice.equals("2") && !choice.equals("3")) {
            System.out.println("Invalid option. Returning to menu.");
            return;
        }

        // 2. Ask for Customer Name (Shared by both 1 and 2)
        System.out.print("Enter Customer Name: "); // Use println to prevent 'stuck' cursor
        String custName = scanner.nextLine();

        Optional<Customer> custOpt = customerService.getCustomerByName(custName);

        if (custOpt.isEmpty()) {
            System.out.println("❌ Error: Customer '" + custName + "' not found.");
            return;
        }

        String custId = custOpt.get().getId();

        // 3. Perform the specific action
        if (choice.equals("1")) {
            // --- VIEW CONTACTS ---
            List<CustomerContact> contacts = customerService.getContactsByCustomerId(custId);
            if (contacts.isEmpty()) {
                System.out.println("No contacts found for " + custName);
            } else {
                System.out.println("\n--- Contacts for " + custName + " ---");
                contacts.forEach(con ->
                        System.out.println("- " + con.getContactName() + " (" + con.getPosition() + ") Phone: " + con.getPhone()));
            }

        } else if (choice.equals("2")) {
            // --- ADD CONTACT ---
            System.out.print("Enter Contact Name: ");
            String name = scanner.nextLine();

            System.out.print("Enter Position: ");
            String pos = scanner.nextLine();

            System.out.print("Enter Phone: ");
            String phone = scanner.nextLine();

            CustomerContact c = new CustomerContact();
            c.setContactName(name);
            c.setPosition(pos);
            c.setPhone(phone);

            customerService.addContact(custId, c);
            System.out.println("✅ Contact Added to " + custName + ".");
        } else if (choice.equals("3")) {
            // --- DELETE CONTACT ---
            System.out.print("Enter Contact Name to DELETE: ");
            String name = scanner.nextLine();

            List<CustomerContact> contacts = customerService.getContactsByCustomerId(custId);
            Optional<CustomerContact> contactOpt = contacts.stream()
                    .filter(c -> c.getContactName().equalsIgnoreCase(name))
                    .findFirst();

            if (contactOpt.isPresent()) {
                customerService.deleteContact(contactOpt.get().getId());
                System.out.println("Contact '" + name + "' is successfully deleted.");
            } else {
                System.out.println("Contact trying deleted is not found.");
            }
        }
    }
}