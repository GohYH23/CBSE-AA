package com.inventorymanagement.customer_gohyuheng;

import com.inventorymanagement.customer_gohyuheng.model.*;
import com.inventorymanagement.customer_gohyuheng.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.time.format.DateTimeFormatter;

@Component
public class CustomerMenu {

    @Autowired
    private CustomerService customerService;

    private String promptForUpdate(Scanner scanner, String label, String currentValue) {
        System.out.print(label + " [" + currentValue + "]: ");
        String input = scanner.nextLine();
        return input.trim().isEmpty() ? currentValue : input;
    }

    public void start(Scanner scanner) {
        boolean back = false;
        while (!back) {
            System.out.println("\n===========================");
            System.out.println("   CUSTOMER MODULE MENU    ");
            System.out.println("===========================");
            System.out.println("1. Manage Customer Groups");
            System.out.println("2. Manage Customer Categories");
            System.out.println("3. Manage Customers");
            System.out.println("4. Manage Customer Contacts");
            System.out.println("5. Back to Main Menu");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1": handleGroupSubMenu(scanner); break;
                case "2": handleCategorySubMenu(scanner); break;
                case "3": handleCustomerSubMenu(scanner); break;
                case "4": handleContactSubMenu(scanner); break;
                case "5": back = true; break;
                default: System.out.println("Invalid option. Please try again (1-5)");
            }
        }
    }

    // --- 1. GROUP SUB-MENU ---
    private void handleGroupSubMenu(Scanner scanner) {
        boolean stay = true;
        while (stay) {
            System.out.println("\n--- MANAGE GROUPS ---");
            System.out.println("1. View Groups");
            System.out.println("2. Create Group");
            System.out.println("3. Edit Group");
            System.out.println("4. Delete Group");
            System.out.println("5. Back");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    List<CustomerGroup> groups = customerService.getAllGroups();
                    if (groups.isEmpty()) {
                        System.out.println("No groups found.");
                    } else {
                        System.out.println("\n--- Customer Groups ---");
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

                        System.out.printf("%-4s %-20s %-35s %-20s %-20s%n",
                                "No.", "Group Name", "Description", "Created Date", "Updated Date");
                        System.out.println("-------------------------------------------------------------------------------------------------------");

                        int i = 1;
                        for (CustomerGroup g : groups) {
                            String dateStr = (g.getCreatedDate() != null) ? g.getCreatedDate().format(formatter) : "N/A";
                            String updateStr = (g.getUpdatedDate() != null) ? g.getUpdatedDate().format(formatter) : "-";

                            System.out.printf("%-4d %-20s %-35s %-20s %-20s%n",
                                    i++,
                                    g.getGroupName(),
                                    (g.getDescription() != null ? g.getDescription() : "N/A"),
                                    dateStr, updateStr);
                        }
                    }
                    break;
                case "2":
                    System.out.print("Enter Group Name: ");
                    String name = scanner.nextLine();
                    System.out.print("Enter Description: ");
                    String desc = scanner.nextLine();

                    CustomerGroup g = new CustomerGroup();
                    g.setGroupName(name);
                    g.setDescription(desc);
                    customerService.createGroup(g);
                    System.out.println("✅ Group Created!");

                    break;
                case "3":
                    performEditGroup(scanner);
                    break;
                case "4":
                    System.out.print("Enter Group Name to Delete: ");
                    String delGroupName = scanner.nextLine();
                    Optional<CustomerGroup> groupOpt = customerService.getGroupByName(delGroupName);
                    if (groupOpt.isPresent()) {
                        String message = customerService.deleteGroup(groupOpt.get().getId());
                        System.out.println(message);
                    } else {
                        System.out.println("❌ Group trying deleted is not found.");
                    }
                    break;
                case "5":
                    stay = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again (1-5)");
            }

        }
    }

    // --- 2. CATEGORY SUB-MENU ---
    private void handleCategorySubMenu(Scanner scanner) {
        boolean stay = true;
        while (stay) {
            System.out.println("\n--- MANAGE CATEGORIES ---");
            System.out.println("1. View Categories");
            System.out.println("2. Create Category");
            System.out.println("3. Edit Category");
            System.out.println("4. Delete Category");
            System.out.println("5. Back");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    List<CustomerCategory> cats = customerService.getAllCategories();
                    if (cats.isEmpty()) {
                        System.out.println("No categories found.");
                    } else {
                        System.out.println("\n--- Customer Categories ---");
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

                        System.out.printf("%-4s %-20s %-35s %-20s %-20s%n",
                                "No.", "Category Name", "Description", "Created Date", "Updated Date");
                        System.out.println("-------------------------------------------------------------------------------------------------------");

                        int i = 1;
                        for (CustomerCategory c : cats) {
                            String dateStr = (c.getCreatedDate() != null) ? c.getCreatedDate().format(formatter) : "N/A";
                            String updateStr = (c.getUpdatedDate() != null) ? c.getUpdatedDate().format(formatter) : "-";

                            System.out.printf("%-4d %-20s %-35s %-20s %-20s%n",
                                    i++,
                                    c.getCategoryName(),
                                    (c.getDescription() != null ? c.getDescription() : "N/A"),
                                    dateStr, updateStr);
                        }
                    }
                    break;
                case "2":
                    System.out.print("Enter Category Name: ");
                    String name = scanner.nextLine();
                    System.out.print("Enter Description: ");
                    String desc = scanner.nextLine();
                    CustomerCategory c = new CustomerCategory();
                    c.setCategoryName(name);
                    c.setDescription(desc);
                    customerService.createCategory(c);
                    System.out.println("✅ Category Created!");
                    break;
                case "3":
                    performEditCategory(scanner);
                    break;
                case "4":
                    System.out.print("Enter Category Name to Delete: ");
                    String delName = scanner.nextLine();
                    Optional<CustomerCategory> opt = customerService.getCategoryByName(delName);
                    if (opt.isPresent()) {
                        String message = customerService.deleteCategory(opt.get().getId());
                        System.out.println(message);
                    } else {
                        System.out.println("❌ Category trying deleted is not found.");
                    }
                    break;
                case "5":
                    stay = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again (1-5)");
            }
        }
    }

    // --- 3. CUSTOMER SUB-MENU ---
    private void handleCustomerSubMenu(Scanner scanner) {
        boolean stay = true;
        while (stay) {
            System.out.println("\n--- MANAGE CUSTOMERS ---");
            System.out.println("1. View All Customers");
            System.out.println("2. Add New Customer");
            System.out.println("3. Edit Customer");
            System.out.println("4. Delete Customer");
            System.out.println("5. Back");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    List<Customer> customers = customerService.getAllCustomers();
                    if (customers.isEmpty()) System.out.println("No customers found.");
                    else {
                        System.out.println("\n--- Customer List ---");
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

                        System.out.printf("%-4s %-15s %-25s %-12s %-20s %-15s %-15s %-20s %-20s%n",
                                "No.", "Name", "Email", "Phone", "Address", "Group", "Category", "Created Date", "Updated Date");
                        System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------------------------");

                        int i = 1;
                        for (Customer c : customers) {
                            String groupName = "N/A";
                            if (c.getCustomerGroupId() != null) {
                                Optional<CustomerGroup> gOpt = customerService.getGroupById(c.getCustomerGroupId());
                                if (gOpt.isPresent()) groupName = gOpt.get().getGroupName();
                            }

                            String catName = "N/A";
                            if (c.getCustomerCategoryId() != null) {
                                Optional<CustomerCategory> cOpt = customerService.getCategoryById(c.getCustomerCategoryId());
                                if (cOpt.isPresent()) catName = cOpt.get().getCategoryName();
                            }

                            String dateStr = (c.getCreatedDate() != null) ? c.getCreatedDate().format(formatter) : "N/A";
                            String updateStr = (c.getUpdatedDate() != null) ? c.getUpdatedDate().format(formatter) : "-";

                            // Safe check for null address
                            String addressDisplay = (c.getAddress() != null) ? c.getAddress() : "N/A";

                            System.out.printf("%-4d %-15s %-25s %-12s %-20s %-15s %-15s %-20s %-20s%n",
                                    i++,
                                    c.getName(),
                                    c.getEmail(),
                                    c.getPhoneNumber(),
                                    addressDisplay,
                                    groupName,
                                    catName,
                                    dateStr, updateStr);
                        }
                    }
                    break;
                case "2":
                    performAddCustomer(scanner);
                    break;
                case "3":
                    performEditCustomer(scanner);
                    break;
                case "4":
                    System.out.print("Enter Name to Delete: ");
                    String delName = scanner.nextLine();
                    Optional<Customer> delOpt = customerService.getCustomerByName(delName);
                    if (delOpt.isPresent()) {
                        String message = customerService.deleteCustomer(delOpt.get().getId());
                        System.out.println(message);
                    } else {
                        System.out.println("❌ Customer trying deleted is not found.");
                    }
                    break;
                case "5":
                    stay = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again (1-5)");
            }
        }
    }

    // --- 4. CONTACT SUB-MENU ---
    private void handleContactSubMenu(Scanner scanner) {
        boolean stay = true;
        while (stay) {
            System.out.println("\n--- MANAGE CONTACTS ---");
            System.out.println("1. View Contacts List");
            System.out.println("2. View Contacts by Customer Name");
            System.out.println("3. Add Contact by Customer Name");
            System.out.println("4. Edit Contact");
            System.out.println("5. Delete Contact");
            System.out.println("6. Back");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine();

            if (choice.equals("6")) {
                stay = false;
                continue;
            }

            switch (choice) {
                case "1":
                    List<CustomerContact> allContacts = customerService.getAllContacts();
                    if (allContacts.isEmpty()) {
                        System.out.println("No contacts found in the system.");
                    } else {
                        System.out.println("\n--- All Customer Contacts ---");
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

                        System.out.printf("%-4s %-20s %-20s %-15s %-25s %-15s %-20s %-20s%n",
                                "No.", "Contact Name", "Customer", "Position", "Email", "Phone", "Created Date", "Updated Date");
                        System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------");

                        int i = 1;
                        for (CustomerContact con : allContacts) {
                            String custName = customerService.getCustomerById(con.getCustomerId())
                                    .map(Customer::getName).orElse("N/A");
                            String dateStr = (con.getCreatedDate() != null) ? con.getCreatedDate().format(formatter) : "N/A";
                            String updateStr = (con.getUpdatedDate() != null) ? con.getUpdatedDate().format(formatter) : "-";
                            String emailDisplay = (con.getEmail() != null) ? con.getEmail() : "N/A";

                            System.out.printf("%-4d %-20s %-20s %-15s %-25s %-15s %-20s %-20s%n",
                                    i++, con.getContactName(), custName,
                                    (con.getPosition() != null ? con.getPosition() : "N/A"),
                                    emailDisplay,
                                    (con.getPhone() != null ? con.getPhone() : "N/A"), dateStr, updateStr);
                        }
                    }
                    break;
                case "2":
                case "3":
                case "4":
                case "5":
                    // --- Customer Specific Actions (Prompt once here) ---
                    System.out.print("Enter Customer Name: ");
                    String custName = scanner.nextLine();
                    Optional<Customer> custOpt = customerService.getCustomerByName(custName);

                    if (custOpt.isEmpty()) {
                        System.out.println("❌ Error: Customer '" + custName + "' not found.");
                        break;
                    }
                    String custId = custOpt.get().getId();

                    if (choice.equals("2")) {
                        List<CustomerContact> contacts = customerService.getContactsByCustomerId(custId);
                        if (contacts.isEmpty()) {
                            System.out.println("No contacts found for " + custName);
                        } else {
                            System.out.println("\n--- Contacts for " + custName + " ---");
                            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

                            System.out.printf("%-4s %-20s %-15s %-25s %-15s %-20s %-20s%n",
                                    "No.", "Contact Name", "Position", "Email", "Phone", "Created Date", "Updated Date");
                            System.out.println("------------------------------------------------------------------------------------------------------------------------------------");
                            int k = 1;
                            for (CustomerContact con : contacts) {
                                String d = (con.getCreatedDate() != null) ? con.getCreatedDate().format(fmt) : "N/A";
                                String u = (con.getUpdatedDate() != null) ? con.getUpdatedDate().format(fmt) : "-";
                                String em = (con.getEmail() != null) ? con.getEmail() : "N/A";

                                System.out.printf("%-4d %-20s %-15s %-25s %-15s %-20s %-20s%n",
                                        k++, con.getContactName(),
                                        (con.getPosition() != null ? con.getPosition() : "N/A"),
                                        em,
                                        (con.getPhone() != null ? con.getPhone() : "N/A"), d, u);
                            }
                        }

                    } else if (choice.equals("3")) {
                        System.out.print("Enter Contact Name: "); String name = scanner.nextLine();
                        System.out.print("Enter Position: "); String pos = scanner.nextLine();
                        System.out.print("Enter Email: "); String email = scanner.nextLine();
                        System.out.print("Enter Phone: "); String phone = scanner.nextLine();

                        CustomerContact c = new CustomerContact();
                        c.setContactName(name);
                        c.setPosition(pos);
                        c.setEmail(email);
                        c.setPhone(phone);
                        customerService.addContact(custId, c);
                        System.out.println("✅ Contact Added to " + custName + ".");

                    } else if (choice.equals("4")) {
                        System.out.print("Enter Contact Name to Edit: ");
                        String editName = scanner.nextLine();
                        List<CustomerContact> list = customerService.getContactsByCustomerId(custId);
                        Optional<CustomerContact> target = list.stream()
                                .filter(c -> c.getContactName().equalsIgnoreCase(editName))
                                .findFirst();

                        if (target.isPresent()) {
                            CustomerContact c = target.get();
                            System.out.println("Editing Contact: " + c.getContactName() + " (Press Enter to keep current value)");

                            c.setContactName(promptForUpdate(scanner, "Name", c.getContactName()));
                            c.setPosition(promptForUpdate(scanner, "Position", c.getPosition()));
                            c.setEmail(promptForUpdate(scanner, "Email", c.getEmail()));
                            c.setPhone(promptForUpdate(scanner, "Phone", c.getPhone()));
                            customerService.updateContact(c);
                            System.out.println("✅ Contact updated!");
                        } else {
                            System.out.println("❌ Contact not found.");
                        }

                    } else if (choice.equals("5")) {
                        // --- Delete Contact (Inline) ---
                        System.out.print("Enter Contact Name to Delete: ");
                        String delName = scanner.nextLine();
                        List<CustomerContact> contList = customerService.getContactsByCustomerId(custId);
                        Optional<CustomerContact> target = contList.stream()
                                .filter(c -> c.getContactName().equalsIgnoreCase(delName))
                                .findFirst();

                        if (target.isPresent()) {
                            customerService.deleteContact(target.get().getId());
                            System.out.println("✅ Contact deleted.");
                        } else {
                            System.out.println("❌ Contact not found.");
                        }
                    }
                    break;
                default: System.out.println("Invalid option. Please try again (1-6)");
            }
        }
    }

    // Helper Methods for Edit/Add operations
    private void performAddCustomer(Scanner scanner) {
        System.out.print("Enter Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Email: ");
        String email = scanner.nextLine();
        System.out.print("Enter Phone: ");
        String phone = scanner.nextLine();
        System.out.print("Enter Address: ");
        String address = scanner.nextLine();

        System.out.print("Enter Group Name: ");
        String groupName = scanner.nextLine();
        Optional<CustomerGroup> groupOpt = customerService.getGroupByName(groupName);
        if (groupOpt.isEmpty()) {
            System.out.println("Unable to proceed: Group '" + groupName + "' not found. Create it first.");
            return;
        }

        System.out.print("Enter Category Name: ");
        String catName = scanner.nextLine();
        Optional<CustomerCategory> catOpt = customerService.getCategoryByName(catName);
        if (catOpt.isEmpty()) {
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
    }

    private void performEditCustomer(Scanner scanner) {
        System.out.print("Enter Customer Name to Edit: ");
        String editName = scanner.nextLine();
        Optional<Customer> editOpt = customerService.getCustomerByName(editName);

        if (editOpt.isEmpty()) {
            System.out.println("Customer not found.");
            return;
        }

        Customer cust = editOpt.get();
        System.out.println("Editing " + cust.getName() + " (Press Enter to keep current value)");

        cust.setName(promptForUpdate(scanner, "Name", cust.getName()));
        cust.setEmail(promptForUpdate(scanner, "Email", cust.getEmail()));
        cust.setPhoneNumber(promptForUpdate(scanner, "Phone", cust.getPhoneNumber()));
        cust.setAddress(promptForUpdate(scanner, "Address", cust.getAddress()));

        // --- Edit Group ---
        // 1. Get current Group Name for display
        String currentGroupName = "Unknown";
        Optional<CustomerGroup> currentGroup = customerService.getGroupById(cust.getCustomerGroupId());
        if(currentGroup.isPresent()) currentGroupName = currentGroup.get().getGroupName();

        // 2. Prompt for new Group Name
        String newGroupName = promptForUpdate(scanner, "Group Name", currentGroupName);

        // 3. If changed, look up new ID
        if (!newGroupName.equals(currentGroupName)) {
            Optional<CustomerGroup> newGroupOpt = customerService.getGroupByName(newGroupName);
            if (newGroupOpt.isPresent()) {
                cust.setCustomerGroupId(newGroupOpt.get().getId());
            } else {
                System.out.println("⚠️ Group '" + newGroupName + "' not found. Keeping old group.");
            }
        }

        // --- Edit Category ---
        // 1. Get current Category Name
        String currentCatName = "Unknown";
        Optional<CustomerCategory> currentCat = customerService.getCategoryById(cust.getCustomerCategoryId());
        if(currentCat.isPresent()) currentCatName = currentCat.get().getCategoryName();

        // 2. Prompt
        String newCatName = promptForUpdate(scanner, "Category Name", currentCatName);

        // 3. Update if changed
        if (!newCatName.equals(currentCatName)) {
            Optional<CustomerCategory> newCatOpt = customerService.getCategoryByName(newCatName);
            if (newCatOpt.isPresent()) {
                cust.setCustomerCategoryId(newCatOpt.get().getId());
            } else {
                System.out.println("⚠️ Category '" + newCatName + "' not found. Keeping old category.");
            }
        }

        customerService.updateCustomer(cust);
        System.out.println("✅ Customer updated successfully.");
    }

    private void performEditGroup(Scanner scanner) {
        System.out.print("Enter Group Name to Edit: ");
        String name = scanner.nextLine();
        Optional<CustomerGroup> opt = customerService.getGroupByName(name);

        if (opt.isEmpty()) {
            System.out.println("Group not found.");
            return;
        }
        CustomerGroup g = opt.get();
        System.out.println("Editing Group: " + g.getGroupName() + " (Press Enter to keep current value)");

        g.setGroupName(promptForUpdate(scanner, "Group Name", g.getGroupName()));
        g.setDescription(promptForUpdate(scanner, "Description", g.getDescription()));
        customerService.updateGroup(g);
        System.out.println("✅ Group updated!");
    }

    private void performEditCategory(Scanner scanner) {
        System.out.print("Enter Category Name to Edit: ");
        String name = scanner.nextLine();
        Optional<CustomerCategory> opt = customerService.getCategoryByName(name);

        if (opt.isEmpty()) {
            System.out.println("Category not found.");
            return;
        }
        CustomerCategory c = opt.get();
        System.out.println("Editing Category: " + c.getCategoryName() + " (Press Enter to keep current value)");

        c.setCategoryName(promptForUpdate(scanner, "Category Name", c.getCategoryName()));
        c.setDescription(promptForUpdate(scanner, "Description", c.getDescription()));
        customerService.updateCategory(c);
        System.out.println("✅ Category updated!");
    }
}