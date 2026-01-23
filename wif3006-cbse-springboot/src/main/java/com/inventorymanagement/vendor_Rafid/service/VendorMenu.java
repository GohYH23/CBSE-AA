package com.inventorymanagement.vendor_Rafid.service;

import com.inventorymanagement.vendor_Rafid.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@Component
public class VendorMenu {

    @Autowired
    private VendorService vendorService;

    private Scanner scanner = new Scanner(System.in);

    public void start() {
        while (true) {
            System.out.println("\n=== VENDOR MANAGEMENT SYSTEM ===");
            System.out.println("1. Manage Vendors");
            System.out.println("2. Manage Vendor Groups");
            System.out.println("3. Manage Vendor Categories");
            System.out.println("4. Manage Vendor Contacts");
            System.out.println("5. View Reports");
            System.out.println("0. Back to Main Menu");
            System.out.print("Select: ");

            String choice = scanner.nextLine();
            try {
                switch (choice) {
                    case "1": manageVendors(); break;
                    case "2": manageVendorGroups(); break;
                    case "3": manageVendorCategories(); break;
                    case "4": manageVendorContacts(); break;
                    case "5": viewReports(); break;
                    case "0": return;
                    default: System.out.println("❌ Invalid option.");
                }
            } catch (Exception e) {
                System.out.println("⚠️ Error: " + e.getMessage());
            }
        }
    }

    // ==================== 1. MANAGE VENDORS ====================
    private void manageVendors() {
        while (true) {
            System.out.println("\n=== MANAGE VENDORS ===");
            System.out.println("1. Add New Vendor");
            System.out.println("2. View All Vendors");
            System.out.println("3. Search Vendor");
            System.out.println("4. View Vendor Details");
            System.out.println("5. Update Vendor");
            System.out.println("6. Change Vendor Status");
            System.out.println("7. Delete Vendor");
            System.out.println("8. View Active Vendors");
            System.out.println("0. Back");
            System.out.print("Select: ");

            String choice = scanner.nextLine();
            
            switch (choice) {
                case "1": addVendor(); break;
                case "2": viewAllVendors(); break;
                case "3": searchVendor(); break;
                case "4": viewVendorDetails(); break;
                case "5": updateVendor(); break;
                case "6": changeVendorStatus(); break;
                case "7": deleteVendor(); break;
                case "8": viewActiveVendors(); break;
                case "0": return;
                default: System.out.println("❌ Invalid option.");
            }
        }
    }

    private void addVendor() {
        System.out.println("\n=== ADD NEW VENDOR ===");
        
        System.out.print("Vendor Name: ");
        String name = scanner.nextLine().trim();
        
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        
        System.out.print("Phone: ");
        String phone = scanner.nextLine().trim();
        
        System.out.print("Address: ");
        String address = scanner.nextLine().trim();
        
        Vendor vendor = new Vendor();
        vendor.setName(name);
        vendor.setEmail(email);
        vendor.setPhone(phone);
        vendor.setAddress(address);
        
        System.out.print("Tax Number: ");
        String taxNumber = scanner.nextLine().trim();
        if (!taxNumber.isEmpty()) {
            vendor.setTaxNumber(taxNumber);
        }
        
        System.out.print("Payment Terms (e.g., Net 30): ");
        String paymentTerms = scanner.nextLine().trim();
        if (!paymentTerms.isEmpty()) {
            vendor.setPaymentTerms(paymentTerms);
        }
        
        try {
            System.out.print("Credit Limit: ");
            String creditLimitStr = scanner.nextLine().trim();
            if (!creditLimitStr.isEmpty()) {
                vendor.setCreditLimit(Double.parseDouble(creditLimitStr));
            }
        } catch (NumberFormatException e) {
            System.out.println("⚠️ Invalid credit limit. Setting to 0.");
            vendor.setCreditLimit(0.0);
        }
        
        // Select Vendor Group
        List<VendorGroup> groups = vendorService.getAllVendorGroups();
        if (!groups.isEmpty()) {
            System.out.println("\nAvailable Vendor Groups:");
            System.out.printf("%-4s %-15s %-30s%n", "No.", "Code", "Group Name");
            System.out.println("----------------------------------------------");
            for (int i = 0; i < groups.size(); i++) {
                System.out.printf("%-4d %-15s %-30s%n", 
                    i + 1, 
                    groups.get(i).getCode(),
                    truncate(groups.get(i).getName(), 30));
            }
            System.out.print("Select Group (enter number, or 0 for none): ");
            try {
                int groupChoice = Integer.parseInt(scanner.nextLine().trim());
                if (groupChoice > 0 && groupChoice <= groups.size()) {
                    vendor.setVendorGroupId(groups.get(groupChoice - 1).getId());
                }
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Invalid selection.");
            }
        }
        
        // Select Vendor Category
        List<VendorCategory> categories = vendorService.getAllVendorCategories();
        if (!categories.isEmpty()) {
            System.out.println("\nAvailable Vendor Categories:");
            System.out.printf("%-4s %-15s %-30s%n", "No.", "Code", "Category Name");
            System.out.println("-------------------------------------------------");
            for (int i = 0; i < categories.size(); i++) {
                System.out.printf("%-4d %-15s %-30s%n", 
                    i + 1, 
                    categories.get(i).getCode(),
                    truncate(categories.get(i).getName(), 30));
            }
            System.out.print("Select Category (enter number, or 0 for none): ");
            try {
                int categoryChoice = Integer.parseInt(scanner.nextLine().trim());
                if (categoryChoice > 0 && categoryChoice <= categories.size()) {
                    vendor.setVendorCategoryId(categories.get(categoryChoice - 1).getId());
                }
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Invalid selection.");
            }
        }
        
        try {
            Vendor createdVendor = vendorService.createVendor(vendor);
            System.out.println("\n✅ Vendor added successfully!");
            System.out.println("Vendor Code: " + createdVendor.getVendorCode());
            System.out.println("Vendor ID: " + createdVendor.getId());
        } catch (Exception e) {
            System.out.println("❌ Error adding vendor: " + e.getMessage());
        }
    }

    private void viewAllVendors() {
        List<Vendor> vendors = vendorService.getAllVendors();
        
        if (vendors.isEmpty()) {
            System.out.println("\n📭 No vendors found.");
            return;
        }

        System.out.println("\n=========================== VENDOR LIST ===========================");
        System.out.printf("%-4s %-15s %-25s %-20s %-12s %-10s%n", 
            "No.", "Vendor Code", "Name", "Email", "Phone", "Status");
        System.out.println("-------------------------------------------------------------------");

        int counter = 1;
        int activeCount = 0;
        
        for (Vendor vendor : vendors) {
            String status = vendor.getStatus();
            if (status.equals("ACTIVE")) activeCount++;
            
            String statusIcon = status.equals("ACTIVE") ? "✅" : 
                              status.equals("INACTIVE") ? "⏸️" : "🚫";
            
            System.out.printf("%-4d %-15s %-25s %-20s %-12s %-10s %s%n",
                counter++,
                vendor.getVendorCode(),
                truncate(vendor.getName(), 25),
                truncate(vendor.getEmail(), 20),
                truncate(vendor.getPhone(), 12),
                status,
                statusIcon);
        }
        
        System.out.println("===================================================================");
        System.out.println("Total Vendors: " + vendors.size() + " | Active: " + activeCount);
    }

    private void viewActiveVendors() {
        List<Vendor> vendors = vendorService.getActiveVendors();
        
        if (vendors.isEmpty()) {
            System.out.println("\n📭 No active vendors found.");
            return;
        }

        System.out.println("\n=========================== ACTIVE VENDORS ===========================");
        System.out.printf("%-4s %-15s %-25s %-20s %-12s %-15s%n", 
            "No.", "Vendor Code", "Name", "Email", "Phone", "Credit Limit");
        System.out.println("----------------------------------------------------------------------");

        int counter = 1;
        double totalCreditLimit = 0;
        
        for (Vendor vendor : vendors) {
            double creditLimit = vendor.getCreditLimit() != null ? vendor.getCreditLimit() : 0;
            totalCreditLimit += creditLimit;
            
            System.out.printf("%-4d %-15s %-25s %-20s %-12s $%-14.2f%n",
                counter++,
                vendor.getVendorCode(),
                truncate(vendor.getName(), 25),
                truncate(vendor.getEmail(), 20),
                truncate(vendor.getPhone(), 12),
                creditLimit);
        }
        
        System.out.println("======================================================================");
        System.out.printf("Total Active Vendors: %d | Total Credit Limit: $%.2f%n", 
            vendors.size(), totalCreditLimit);
    }

    private void searchVendor() {
        System.out.print("\nEnter vendor name to search: ");
        String searchName = scanner.nextLine().trim();
        
        List<Vendor> vendors = vendorService.searchVendorsByName(searchName);
        
        if (vendors.isEmpty()) {
            System.out.println("\n📭 No vendors found matching: " + searchName);
            return;
        }

        System.out.println("\n=========================== SEARCH RESULTS ===========================");
        System.out.printf("%-4s %-15s %-25s %-20s %-12s %-10s%n", 
            "No.", "Vendor Code", "Name", "Email", "Phone", "Status");
        System.out.println("-------------------------------------------------------------------");

        int counter = 1;
        
        for (Vendor vendor : vendors) {
            System.out.printf("%-4d %-15s %-25s %-20s %-12s %-10s%n",
                counter++,
                vendor.getVendorCode(),
                truncate(vendor.getName(), 25),
                truncate(vendor.getEmail(), 20),
                truncate(vendor.getPhone(), 12),
                vendor.getStatus());
        }
        
        System.out.println("===================================================================");
        System.out.println("Found " + vendors.size() + " vendor(s)");
    }

    private void viewVendorDetails() {
        System.out.print("\nEnter Vendor Code: ");
        String vendorCode = scanner.nextLine().trim();
        
        Optional<Vendor> vendorOpt = vendorService.getVendorByCode(vendorCode);
        if (vendorOpt.isEmpty()) {
            System.out.println("❌ Vendor not found!");
            return;
        }
        
        Vendor vendor = vendorOpt.get();
        
        System.out.println("\n=========================== VENDOR DETAILS ===========================");
        System.out.println("Vendor Code    : " + vendor.getVendorCode());
        System.out.println("Name           : " + vendor.getName());
        System.out.println("Email          : " + vendor.getEmail());
        System.out.println("Phone          : " + vendor.getPhone());
        System.out.println("Address        : " + vendor.getAddress());
        System.out.println("Status         : " + vendor.getStatus());
        System.out.println("Tax Number     : " + (vendor.getTaxNumber() != null ? vendor.getTaxNumber() : "N/A"));
        System.out.println("Payment Terms  : " + (vendor.getPaymentTerms() != null ? vendor.getPaymentTerms() : "N/A"));
        System.out.println("Credit Limit   : $" + (vendor.getCreditLimit() != null ? vendor.getCreditLimit() : "0.00"));
        System.out.println("Created        : " + vendor.getCreatedAt());
        System.out.println("Last Updated   : " + vendor.getUpdatedAt());
        
        // Show vendor group
        if (vendor.getVendorGroupId() != null) {
            Optional<VendorGroup> groupOpt = vendorService.getVendorGroupById(vendor.getVendorGroupId());
            if (groupOpt.isPresent()) {
                System.out.println("Vendor Group   : " + groupOpt.get().getName() + " (" + groupOpt.get().getCode() + ")");
            }
        }
        
        // Show vendor category
        if (vendor.getVendorCategoryId() != null) {
            Optional<VendorCategory> categoryOpt = vendorService.getVendorCategoryById(vendor.getVendorCategoryId());
            if (categoryOpt.isPresent()) {
                System.out.println("Category       : " + categoryOpt.get().getName() + " (" + categoryOpt.get().getCode() + ")");
            }
        }
        
        // Show contacts
        List<VendorContact> contacts = vendorService.getVendorContacts(vendor.getId());
        if (!contacts.isEmpty()) {
            System.out.println("\n------------------------ CONTACTS ------------------------");
            System.out.printf("%-25s %-20s %-15s %-10s%n", "Name", "Position", "Phone", "Primary");
            System.out.println("----------------------------------------------------------");
            for (VendorContact contact : contacts) {
                String primary = contact.getIsPrimary() ? "✓" : "";
                System.out.printf("%-25s %-20s %-15s %-10s%n",
                    truncate(contact.getName(), 25),
                    truncate(contact.getPosition(), 20),
                    truncate(contact.getPhone(), 15),
                    primary);
            }
        }
        
        System.out.println("====================================================================");
    }

    private void updateVendor() {
        System.out.print("\nEnter Vendor Code to update: ");
        String vendorCode = scanner.nextLine().trim();
        
        Optional<Vendor> vendorOpt = vendorService.getVendorByCode(vendorCode);
        if (vendorOpt.isEmpty()) {
            System.out.println("❌ Vendor not found!");
            return;
        }
        
        Vendor vendor = vendorOpt.get();
        
        System.out.println("\n=== UPDATE VENDOR: " + vendor.getName() + " ===");
        System.out.println("Current Information:");
        System.out.println("Name: " + vendor.getName());
        System.out.println("Email: " + vendor.getEmail());
        System.out.println("Phone: " + vendor.getPhone());
        System.out.println("Address: " + vendor.getAddress());
        System.out.println("Credit Limit: $" + vendor.getCreditLimit());
        
        System.out.println("\nEnter new values (press Enter to keep current):");
        
        System.out.print("Name [" + vendor.getName() + "]: ");
        String name = scanner.nextLine().trim();
        
        System.out.print("Email [" + vendor.getEmail() + "]: ");
        String email = scanner.nextLine().trim();
        
        System.out.print("Phone [" + vendor.getPhone() + "]: ");
        String phone = scanner.nextLine().trim();
        
        System.out.print("Address [" + vendor.getAddress() + "]: ");
        String address = scanner.nextLine().trim();
        
        System.out.print("Tax Number [" + (vendor.getTaxNumber() != null ? vendor.getTaxNumber() : "") + "]: ");
        String taxNumber = scanner.nextLine().trim();
        
        System.out.print("Payment Terms [" + (vendor.getPaymentTerms() != null ? vendor.getPaymentTerms() : "") + "]: ");
        String paymentTerms = scanner.nextLine().trim();
        
        System.out.print("Credit Limit [" + vendor.getCreditLimit() + "]: ");
        String creditLimitStr = scanner.nextLine().trim();
        
        Vendor updatedVendor = new Vendor();
        if (!name.isEmpty()) updatedVendor.setName(name);
        if (!email.isEmpty()) updatedVendor.setEmail(email);
        if (!phone.isEmpty()) updatedVendor.setPhone(phone);
        if (!address.isEmpty()) updatedVendor.setAddress(address);
        if (!taxNumber.isEmpty()) updatedVendor.setTaxNumber(taxNumber);
        if (!paymentTerms.isEmpty()) updatedVendor.setPaymentTerms(paymentTerms);
        if (!creditLimitStr.isEmpty()) {
            try {
                updatedVendor.setCreditLimit(Double.parseDouble(creditLimitStr));
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Invalid credit limit. Keeping current value.");
            }
        }
        
        try {
            vendorService.updateVendor(vendor.getId(), updatedVendor);
            System.out.println("\n✅ Vendor updated successfully!");
        } catch (Exception e) {
            System.out.println("❌ Error updating vendor: " + e.getMessage());
        }
    }

    private void changeVendorStatus() {
        System.out.print("\nEnter Vendor Code: ");
        String vendorCode = scanner.nextLine().trim();
        
        Optional<Vendor> vendorOpt = vendorService.getVendorByCode(vendorCode);
        if (vendorOpt.isEmpty()) {
            System.out.println("❌ Vendor not found!");
            return;
        }
        
        Vendor vendor = vendorOpt.get();
        System.out.println("\nCurrent Status: " + vendor.getStatus());
        System.out.println("\nAvailable Statuses:");
        System.out.println("1. ACTIVE");
        System.out.println("2. INACTIVE");
        System.out.println("3. SUSPENDED");
        System.out.print("\nSelect new status (1-3): ");
        
        String choice = scanner.nextLine();
        String newStatus;
        
        switch (choice) {
            case "1": newStatus = "ACTIVE"; break;
            case "2": newStatus = "INACTIVE"; break;
            case "3": newStatus = "SUSPENDED"; break;
            default:
                System.out.println("❌ Invalid choice!");
                return;
        }
        
        try {
            vendorService.changeVendorStatus(vendor.getId(), newStatus);
            System.out.println("\n✅ Vendor status changed to: " + newStatus);
        } catch (Exception e) {
            System.out.println("❌ Error changing status: " + e.getMessage());
        }
    }

    private void deleteVendor() {
        System.out.print("\nEnter Vendor Code to delete: ");
        String vendorCode = scanner.nextLine().trim();
        
        Optional<Vendor> vendorOpt = vendorService.getVendorByCode(vendorCode);
        if (vendorOpt.isEmpty()) {
            System.out.println("❌ Vendor not found!");
            return;
        }
        
        Vendor vendor = vendorOpt.get();
        
        System.out.println("\n⚠️ WARNING: Deleting Vendor");
        System.out.println("Name: " + vendor.getName());
        System.out.println("Code: " + vendor.getVendorCode());
        System.out.println("Email: " + vendor.getEmail());
        System.out.println("This will also delete all associated contacts!");
        System.out.print("\nAre you sure? (yes/no): ");
        
        String confirm = scanner.nextLine().trim();
        if (confirm.equalsIgnoreCase("yes")) {
            try {
                vendorService.deleteVendor(vendor.getId());
                System.out.println("✅ Vendor deleted successfully!");
            } catch (Exception e) {
                System.out.println("❌ Error deleting vendor: " + e.getMessage());
            }
        } else {
            System.out.println("❌ Deletion cancelled.");
        }
    }

    // ==================== 2. MANAGE VENDOR GROUPS ====================
    private void manageVendorGroups() {
        while (true) {
            System.out.println("\n=== MANAGE VENDOR GROUPS ===");
            System.out.println("1. Add New Vendor Group");
            System.out.println("2. View All Vendor Groups");
            System.out.println("3. Update Vendor Group");
            System.out.println("4. Delete Vendor Group");
            System.out.println("0. Back");
            System.out.print("Select: ");

            String choice = scanner.nextLine();
            
            switch (choice) {
                case "1": addVendorGroup(); break;
                case "2": viewAllVendorGroups(); break;
                case "3": updateVendorGroup(); break;
                case "4": deleteVendorGroup(); break;
                case "0": return;
                default: System.out.println("❌ Invalid option.");
            }
        }
    }

    private void addVendorGroup() {
        System.out.println("\n=== ADD NEW VENDOR GROUP ===");
        
        System.out.print("Group Name: ");
        String name = scanner.nextLine().trim();
        
        System.out.print("Description: ");
        String description = scanner.nextLine().trim();
        
        VendorGroup vendorGroup = new VendorGroup();
        vendorGroup.setName(name);
        vendorGroup.setDescription(description);
        
        try {
            VendorGroup createdGroup = vendorService.createVendorGroup(vendorGroup);
            System.out.println("\n✅ Vendor group added successfully!");
            System.out.println("Group Code: " + createdGroup.getCode());
            System.out.println("Group ID: " + createdGroup.getId());
        } catch (Exception e) {
            System.out.println("❌ Error adding vendor group: " + e.getMessage());
        }
    }

    private void viewAllVendorGroups() {
        List<VendorGroup> groups = vendorService.getAllVendorGroups();
        
        if (groups.isEmpty()) {
            System.out.println("\n📭 No vendor groups found.");
            return;
        }

        System.out.println("\n=========================== VENDOR GROUPS ===========================");
        System.out.printf("%-4s %-15s %-25s %-40s%n", 
            "No.", "Code", "Group Name", "Description");
        System.out.println("--------------------------------------------------------------------");

        int counter = 1;
        
        for (VendorGroup group : groups) {
            System.out.printf("%-4d %-15s %-25s %-40s%n",
                counter++,
                group.getCode(),
                truncate(group.getName(), 25),
                truncate(group.getDescription(), 40));
        }
        
        System.out.println("====================================================================");
        
        // Show vendor count per group
        System.out.println("\n📊 Vendors per Group:");
        for (VendorGroup group : groups) {
            List<Vendor> vendors = vendorService.getVendorsByGroup(group.getId());
            System.out.printf("  • %s: %d vendor(s)%n", group.getName(), vendors.size());
        }
    }

    private void updateVendorGroup() {
        System.out.print("\nEnter Vendor Group Code: ");
        String groupCode = scanner.nextLine().trim();
        
        // Find group by code
        Optional<VendorGroup> groupOpt = vendorService.getAllVendorGroups().stream()
            .filter(g -> g.getCode().equals(groupCode))
            .findFirst();
        
        if (groupOpt.isEmpty()) {
            System.out.println("❌ Vendor group not found!");
            return;
        }
        
        VendorGroup group = groupOpt.get();
        
        System.out.println("\n=== UPDATE VENDOR GROUP: " + group.getName() + " ===");
        System.out.println("Current Name: " + group.getName());
        System.out.println("Current Description: " + group.getDescription());
        
        System.out.println("\nEnter new values (press Enter to keep current):");
        
        System.out.print("Name [" + group.getName() + "]: ");
        String name = scanner.nextLine().trim();
        
        System.out.print("Description [" + group.getDescription() + "]: ");
        String description = scanner.nextLine().trim();
        
        VendorGroup updatedGroup = new VendorGroup();
        if (!name.isEmpty()) updatedGroup.setName(name);
        if (!description.isEmpty()) updatedGroup.setDescription(description);
        
        try {
           vendorService.updateVendorGroup(group.getId(), updatedGroup);
            System.out.println("\n✅ Vendor group updated successfully!");
        } catch (Exception e) {
            System.out.println("❌ Error updating vendor group: " + e.getMessage());
        }
    }

    private void deleteVendorGroup() {
        System.out.print("\nEnter Vendor Group Code to delete: ");
        String groupCode = scanner.nextLine().trim();
        
        // Find group by code
        Optional<VendorGroup> groupOpt = vendorService.getAllVendorGroups().stream()
            .filter(g -> g.getCode().equals(groupCode))
            .findFirst();
        
        if (groupOpt.isEmpty()) {
            System.out.println("❌ Vendor group not found!");
            return;
        }
        
        VendorGroup group = groupOpt.get();
        
        // Check if any vendor is using this group
        List<Vendor> vendors = vendorService.getVendorsByGroup(group.getId());
        
        System.out.println("\n⚠️ WARNING: Deleting Vendor Group");
        System.out.println("Name: " + group.getName());
        System.out.println("Code: " + group.getCode());
        System.out.println("Vendors using this group: " + vendors.size());
        
        if (!vendors.isEmpty()) {
            System.out.println("\n❌ Cannot delete group with associated vendors!");
            System.out.println("Please reassign or delete the following vendors first:");
            for (Vendor vendor : vendors) {
                System.out.println("  • " + vendor.getName() + " (" + vendor.getVendorCode() + ")");
            }
            return;
        }
        
        System.out.print("\nAre you sure? (yes/no): ");
        String confirm = scanner.nextLine().trim();
        
        if (confirm.equalsIgnoreCase("yes")) {
            try {
                vendorService.deleteVendorGroup(group.getId());
                System.out.println("✅ Vendor group deleted successfully!");
            } catch (Exception e) {
                System.out.println("❌ Error deleting vendor group: " + e.getMessage());
            }
        } else {
            System.out.println("❌ Deletion cancelled.");
        }
    }

    // ==================== 3. MANAGE VENDOR CATEGORIES ====================
    private void manageVendorCategories() {
        while (true) {
            System.out.println("\n=== MANAGE VENDOR CATEGORIES ===");
            System.out.println("1. Add New Vendor Category");
            System.out.println("2. View All Vendor Categories");
            System.out.println("3. Update Vendor Category");
            System.out.println("4. Delete Vendor Category");
            System.out.println("0. Back");
            System.out.print("Select: ");

            String choice = scanner.nextLine();
            
            switch (choice) {
                case "1": addVendorCategory(); break;
                case "2": viewAllVendorCategories(); break;
                case "3": updateVendorCategory(); break;
                case "4": deleteVendorCategory(); break;
                case "0": return;
                default: System.out.println("❌ Invalid option.");
            }
        }
    }

    private void addVendorCategory() {
        System.out.println("\n=== ADD NEW VENDOR CATEGORY ===");
        
        System.out.print("Category Name: ");
        String name = scanner.nextLine().trim();
        
        System.out.print("Description: ");
        String description = scanner.nextLine().trim();
        
        VendorCategory vendorCategory = new VendorCategory();
        vendorCategory.setName(name);
        vendorCategory.setDescription(description);
        
        try {
            VendorCategory createdCategory = vendorService.createVendorCategory(vendorCategory);
            System.out.println("\n✅ Vendor category added successfully!");
            System.out.println("Category Code: " + createdCategory.getCode());
            System.out.println("Category ID: " + createdCategory.getId());
        } catch (Exception e) {
            System.out.println("❌ Error adding vendor category: " + e.getMessage());
        }
    }

    private void viewAllVendorCategories() {
        List<VendorCategory> categories = vendorService.getAllVendorCategories();
        
        if (categories.isEmpty()) {
            System.out.println("\n📭 No vendor categories found.");
            return;
        }

        System.out.println("\n=========================== VENDOR CATEGORIES ===========================");
        System.out.printf("%-4s %-15s %-25s %-40s%n", 
            "No.", "Code", "Category Name", "Description");
        System.out.println("-----------------------------------------------------------------------");

        int counter = 1;
        
        for (VendorCategory category : categories) {
            System.out.printf("%-4d %-15s %-25s %-40s%n",
                counter++,
                category.getCode(),
                truncate(category.getName(), 25),
                truncate(category.getDescription(), 40));
        }
        
        System.out.println("=======================================================================");
        
        // Show vendor count per category
        System.out.println("\n📊 Vendors per Category:");
        for (VendorCategory category : categories) {
            List<Vendor> vendors = vendorService.getVendorsByCategory(category.getId());
            System.out.printf("  • %s: %d vendor(s)%n", category.getName(), vendors.size());
        }
    }

    private void updateVendorCategory() {
        System.out.print("\nEnter Vendor Category Code: ");
        String categoryCode = scanner.nextLine().trim();
        
        // Find category by code
        Optional<VendorCategory> categoryOpt = vendorService.getAllVendorCategories().stream()
            .filter(c -> c.getCode().equals(categoryCode))
            .findFirst();
        
        if (categoryOpt.isEmpty()) {
            System.out.println("❌ Vendor category not found!");
            return;
        }
        
        VendorCategory category = categoryOpt.get();
        
        System.out.println("\n=== UPDATE VENDOR CATEGORY: " + category.getName() + " ===");
        System.out.println("Current Name: " + category.getName());
        System.out.println("Current Description: " + category.getDescription());
        
        System.out.println("\nEnter new values (press Enter to keep current):");
        
        System.out.print("Name [" + category.getName() + "]: ");
        String name = scanner.nextLine().trim();
        
        System.out.print("Description [" + category.getDescription() + "]: ");
        String description = scanner.nextLine().trim();
        
        VendorCategory updatedCategory = new VendorCategory();
        if (!name.isEmpty()) updatedCategory.setName(name);
        if (!description.isEmpty()) updatedCategory.setDescription(description);
        
        try {
            vendorService.updateVendorCategory(category.getId(), updatedCategory);
            System.out.println("\n✅ Vendor category updated successfully!");
        } catch (Exception e) {
            System.out.println("❌ Error updating vendor category: " + e.getMessage());
        }
    }

    private void deleteVendorCategory() {
        System.out.print("\nEnter Vendor Category Code to delete: ");
        String categoryCode = scanner.nextLine().trim();
        
        // Find category by code
        Optional<VendorCategory> categoryOpt = vendorService.getAllVendorCategories().stream()
            .filter(c -> c.getCode().equals(categoryCode))
            .findFirst();
        
        if (categoryOpt.isEmpty()) {
            System.out.println("❌ Vendor category not found!");
            return;
        }
        
        VendorCategory category = categoryOpt.get();
        
        // Check if any vendor is using this category
        List<Vendor> vendors = vendorService.getVendorsByCategory(category.getId());
        
        System.out.println("\n⚠️ WARNING: Deleting Vendor Category");
        System.out.println("Name: " + category.getName());
        System.out.println("Code: " + category.getCode());
        System.out.println("Vendors using this category: " + vendors.size());
        
        if (!vendors.isEmpty()) {
            System.out.println("\n❌ Cannot delete category with associated vendors!");
            System.out.println("Please reassign or delete the following vendors first:");
            for (Vendor vendor : vendors) {
                System.out.println("  • " + vendor.getName() + " (" + vendor.getVendorCode() + ")");
            }
            return;
        }
        
        System.out.print("\nAre you sure? (yes/no): ");
        String confirm = scanner.nextLine().trim();
        
        if (confirm.equalsIgnoreCase("yes")) {
            try {
                vendorService.deleteVendorCategory(category.getId());
                System.out.println("✅ Vendor category deleted successfully!");
            } catch (Exception e) {
                System.out.println("❌ Error deleting vendor category: " + e.getMessage());
            }
        } else {
            System.out.println("❌ Deletion cancelled.");
        }
    }

    // ==================== 4. MANAGE VENDOR CONTACTS ====================
    private void manageVendorContacts() {
        while (true) {
            System.out.println("\n=== MANAGE VENDOR CONTACTS ===");
            System.out.println("1. Add Contact");
            System.out.println("2. View Vendor Contacts");
            System.out.println("3. Update Contact");
            System.out.println("4. Delete Contact");
            System.out.println("0. Back");
            System.out.print("Select: ");

            String choice = scanner.nextLine();
            
            switch (choice) {
                case "1": addContact(); break;
                case "2": viewContacts(); break;
                case "3": updateContact(); break;
                case "4": deleteContact(); break;
                case "0": return;
                default: System.out.println("❌ Invalid option.");
            }
        }
    }

    private void addContact() {
        System.out.print("\nEnter Vendor Code: ");
        String vendorCode = scanner.nextLine().trim();
        
        Optional<Vendor> vendorOpt = vendorService.getVendorByCode(vendorCode);
        if (vendorOpt.isEmpty()) {
            System.out.println("❌ Vendor not found!");
            return;
        }
        
        Vendor vendor = vendorOpt.get();
        
        System.out.println("\n=== ADD CONTACT FOR: " + vendor.getName() + " ===");
        
        System.out.print("Contact Name: ");
        String name = scanner.nextLine().trim();
        
        System.out.print("Position: ");
        String position = scanner.nextLine().trim();
        
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        
        System.out.print("Phone: ");
        String phone = scanner.nextLine().trim();
        
        System.out.print("Department: ");
        String department = scanner.nextLine().trim();
        
        System.out.print("Is Primary Contact? (yes/no): ");
        boolean isPrimary = scanner.nextLine().trim().equalsIgnoreCase("yes");
        
        VendorContact contact = new VendorContact();
        contact.setName(name);
        contact.setPosition(position);
        contact.setEmail(email);
        contact.setPhone(phone);
        contact.setDepartment(department);
        contact.setIsPrimary(isPrimary);
        
        try {
            VendorContact createdContact = vendorService.createVendorContact(vendor.getId(), contact);
            System.out.println("\n✅ Contact added successfully!");
            System.out.println("Contact ID: " + createdContact.getId());
        } catch (Exception e) {
            System.out.println("❌ Error adding contact: " + e.getMessage());
        }
    }

    private void viewContacts() {
        System.out.print("\nEnter Vendor Code: ");
        String vendorCode = scanner.nextLine().trim();
        
        Optional<Vendor> vendorOpt = vendorService.getVendorByCode(vendorCode);
        if (vendorOpt.isEmpty()) {
            System.out.println("❌ Vendor not found!");
            return;
        }
        
        Vendor vendor = vendorOpt.get();
        List<VendorContact> contacts = vendorService.getVendorContacts(vendor.getId());
        
        if (contacts.isEmpty()) {
            System.out.println("\n📭 No contacts found for this vendor.");
            return;
        }

        System.out.println("\n=========================== CONTACTS FOR: " + vendor.getName() + " ===========================");
        System.out.printf("%-4s %-25s %-20s %-25s %-15s %-10s%n", 
            "No.", "Name", "Position", "Email", "Phone", "Primary");
        System.out.println("------------------------------------------------------------------------------------------");

        int counter = 1;
        
        for (VendorContact contact : contacts) {
            String primary = contact.getIsPrimary() ? "✓" : "";
            System.out.printf("%-4d %-25s %-20s %-25s %-15s %-10s%n",
                counter++,
                truncate(contact.getName(), 25),
                truncate(contact.getPosition(), 20),
                truncate(contact.getEmail(), 25),
                truncate(contact.getPhone(), 15),
                primary);
        }
        
        System.out.println("==========================================================================================");
        
        // Show primary contact
        Optional<VendorContact> primaryContact = vendorService.getPrimaryContact(vendor.getId());
        if (primaryContact.isPresent()) {
            System.out.println("\n📞 Primary Contact:");
            System.out.println("  Name: " + primaryContact.get().getName());
            System.out.println("  Email: " + primaryContact.get().getEmail());
            System.out.println("  Phone: " + primaryContact.get().getPhone());
        }
    }

    private void updateContact() {
        System.out.print("\nEnter Vendor Code: ");
        String vendorCode = scanner.nextLine().trim();
        
        Optional<Vendor> vendorOpt = vendorService.getVendorByCode(vendorCode);
        if (vendorOpt.isEmpty()) {
            System.out.println("❌ Vendor not found!");
            return;
        }
        
        Vendor vendor = vendorOpt.get();
        List<VendorContact> contacts = vendorService.getVendorContacts(vendor.getId());
        
        if (contacts.isEmpty()) {
            System.out.println("❌ No contacts found for this vendor.");
            return;
        }
        
        System.out.println("\nSelect contact to update:");
        for (int i = 0; i < contacts.size(); i++) {
            String primary = contacts.get(i).getIsPrimary() ? " (Primary)" : "";
            System.out.printf("%d. %s - %s%s%n", 
                i + 1, 
                contacts.get(i).getName(),
                contacts.get(i).getPosition(),
                primary);
        }
        
        System.out.print("Enter contact number: ");
        try {
            int contactNum = Integer.parseInt(scanner.nextLine().trim());
            if (contactNum < 1 || contactNum > contacts.size()) {
                System.out.println("❌ Invalid contact number.");
                return;
            }
            
            VendorContact contact = contacts.get(contactNum - 1);
            
            System.out.println("\n=== UPDATE CONTACT: " + contact.getName() + " ===");
            System.out.println("Current Position: " + contact.getPosition());
            System.out.println("Current Email: " + contact.getEmail());
            System.out.println("Current Phone: " + contact.getPhone());
            
            System.out.println("\nEnter new values (press Enter to keep current):");
            
            System.out.print("Name [" + contact.getName() + "]: ");
            String name = scanner.nextLine().trim();
            
            System.out.print("Position [" + contact.getPosition() + "]: ");
            String position = scanner.nextLine().trim();
            
            System.out.print("Email [" + contact.getEmail() + "]: ");
            String email = scanner.nextLine().trim();
            
            System.out.print("Phone [" + contact.getPhone() + "]: ");
            String phone = scanner.nextLine().trim();
            
            System.out.print("Department [" + contact.getDepartment() + "]: ");
            String department = scanner.nextLine().trim();
            
            System.out.print("Make Primary? (yes/no) [" + (contact.getIsPrimary() ? "yes" : "no") + "]: ");
            String primaryStr = scanner.nextLine().trim();
            
            VendorContact updatedContact = new VendorContact();
            if (!name.isEmpty()) updatedContact.setName(name);
            if (!position.isEmpty()) updatedContact.setPosition(position);
            if (!email.isEmpty()) updatedContact.setEmail(email);
            if (!phone.isEmpty()) updatedContact.setPhone(phone);
            if (!department.isEmpty()) updatedContact.setDepartment(department);
            if (!primaryStr.isEmpty()) {
                updatedContact.setIsPrimary(primaryStr.equalsIgnoreCase("yes"));
            }
            
            vendorService.updateVendorContact(contact.getId(), updatedContact);
            System.out.println("\n✅ Contact updated successfully!");
            
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid number.");
        } catch (Exception e) {
            System.out.println("❌ Error updating contact: " + e.getMessage());
        }
    }

    private void deleteContact() {
        System.out.print("\nEnter Vendor Code: ");
        String vendorCode = scanner.nextLine().trim();
        
        Optional<Vendor> vendorOpt = vendorService.getVendorByCode(vendorCode);
        if (vendorOpt.isEmpty()) {
            System.out.println("❌ Vendor not found!");
            return;
        }
        
        Vendor vendor = vendorOpt.get();
        List<VendorContact> contacts = vendorService.getVendorContacts(vendor.getId());
        
        if (contacts.isEmpty()) {
            System.out.println("❌ No contacts found for this vendor.");
            return;
        }
        
        System.out.println("\nSelect contact to delete:");
        for (int i = 0; i < contacts.size(); i++) {
            String primary = contacts.get(i).getIsPrimary() ? " (Primary)" : "";
            System.out.printf("%d. %s - %s%s%n", 
                i + 1, 
                contacts.get(i).getName(),
                contacts.get(i).getPosition(),
                primary);
        }
        
        System.out.print("Enter contact number: ");
        try {
            int contactNum = Integer.parseInt(scanner.nextLine().trim());
            if (contactNum < 1 || contactNum > contacts.size()) {
                System.out.println("❌ Invalid contact number.");
                return;
            }
            
            VendorContact contact = contacts.get(contactNum - 1);
            
            System.out.println("\n⚠️ WARNING: Deleting Contact");
            System.out.println("Name: " + contact.getName());
            System.out.println("Position: " + contact.getPosition());
            System.out.println("Email: " + contact.getEmail());
            
            if (contact.getIsPrimary()) {
                System.out.println("⚠️ This is the primary contact!");
            }
            
            System.out.print("\nAre you sure? (yes/no): ");
            String confirm = scanner.nextLine().trim();
            
            if (confirm.equalsIgnoreCase("yes")) {
                vendorService.deleteVendorContact(contact.getId());
                System.out.println("✅ Contact deleted successfully!");
            } else {
                System.out.println("❌ Deletion cancelled.");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid number.");
        }
    }

    // ==================== 5. VIEW REPORTS ====================
    private void viewReports() {
        while (true) {
            System.out.println("\n=== VENDOR REPORTS ===");
            System.out.println("1. Vendor Statistics");
            System.out.println("2. Vendors by Group");
            System.out.println("3. Vendors by Category");
            System.out.println("4. Credit Limit Report");
            System.out.println("0. Back");
            System.out.print("Select: ");

            String choice = scanner.nextLine();
            
            switch (choice) {
                case "1": showVendorStatistics(); break;
                case "2": showVendorsByGroup(); break;
                case "3": showVendorsByCategory(); break;
                case "4": showCreditLimitReport(); break;
                case "0": return;
                default: System.out.println("❌ Invalid option.");
            }
        }
    }

    private void showVendorStatistics() {
        long totalVendors = vendorService.countAllVendors();
        long activeVendors = vendorService.countActiveVendors();
        long inactiveVendors = totalVendors - activeVendors;
        
        List<Vendor> allVendors = vendorService.getAllVendors();
        double totalCreditLimit = allVendors.stream()
            .mapToDouble(v -> v.getCreditLimit() != null ? v.getCreditLimit() : 0)
            .sum();
        
        System.out.println("\n=========================== VENDOR STATISTICS ===========================");
        System.out.println("📊 Summary:");
        System.out.println("  Total Vendors: " + totalVendors);
        System.out.println("  Active Vendors: " + activeVendors + " (" + (totalVendors > 0 ? (activeVendors * 100 / totalVendors) : 0) + "%)");
        System.out.println("  Inactive/Suspended Vendors: " + inactiveVendors + " (" + (totalVendors > 0 ? (inactiveVendors * 100 / totalVendors) : 0) + "%)");
        System.out.println("  Total Credit Limit: $" + String.format("%.2f", totalCreditLimit));
        
        System.out.println("\n📈 Status Distribution:");
        long activeCount = allVendors.stream().filter(v -> v.getStatus().equals("ACTIVE")).count();
        long inactiveCount = allVendors.stream().filter(v -> v.getStatus().equals("INACTIVE")).count();
        long suspendedCount = allVendors.stream().filter(v -> v.getStatus().equals("SUSPENDED")).count();
        
        System.out.println("  ✅ ACTIVE: " + activeCount);
        System.out.println("  ⏸️ INACTIVE: " + inactiveCount);
        System.out.println("  🚫 SUSPENDED: " + suspendedCount);
        
        System.out.println("\n🏢 Vendor Groups:");
        List<VendorGroup> groups = vendorService.getAllVendorGroups();
        for (VendorGroup group : groups) {
            List<Vendor> vendors = vendorService.getVendorsByGroup(group.getId());
            System.out.printf("  • %s: %d vendor(s)%n", group.getName(), vendors.size());
        }
        
        System.out.println("\n📂 Vendor Categories:");
        List<VendorCategory> categories = vendorService.getAllVendorCategories();
        for (VendorCategory category : categories) {
            List<Vendor> vendors = vendorService.getVendorsByCategory(category.getId());
            System.out.printf("  • %s: %d vendor(s)%n", category.getName(), vendors.size());
        }
        
        System.out.println("========================================================================");
    }

    private void showVendorsByGroup() {
        List<VendorGroup> groups = vendorService.getAllVendorGroups();
        
        if (groups.isEmpty()) {
            System.out.println("\n📭 No vendor groups found.");
            return;
        }
        
        System.out.println("\n=========================== VENDORS BY GROUP ===========================");
        
        for (VendorGroup group : groups) {
            List<Vendor> vendors = vendorService.getVendorsByGroup(group.getId());
            
            System.out.println("\n📋 Group: " + group.getName() + " (" + group.getCode() + ")");
            System.out.println("Description: " + group.getDescription());
            System.out.println("Number of Vendors: " + vendors.size());
            
            if (!vendors.isEmpty()) {
                System.out.println("\n  Vendors:");
                for (Vendor vendor : vendors) {
                    System.out.printf("    • %s (%s) - %s%n", 
                        vendor.getName(), 
                        vendor.getVendorCode(),
                        vendor.getStatus());
                }
            }
            
            System.out.println("--------------------------------------------------------------------");
        }
        
        System.out.println("========================================================================");
    }

    private void showVendorsByCategory() {
        List<VendorCategory> categories = vendorService.getAllVendorCategories();
        
        if (categories.isEmpty()) {
            System.out.println("\n📭 No vendor categories found.");
            return;
        }
        
        System.out.println("\n=========================== VENDORS BY CATEGORY ===========================");
        
        for (VendorCategory category : categories) {
            List<Vendor> vendors = vendorService.getVendorsByCategory(category.getId());
            
            System.out.println("\n📂 Category: " + category.getName() + " (" + category.getCode() + ")");
            System.out.println("Description: " + category.getDescription());
            System.out.println("Number of Vendors: " + vendors.size());
            
            if (!vendors.isEmpty()) {
                System.out.println("\n  Vendors:");
                for (Vendor vendor : vendors) {
                    System.out.printf("    • %s (%s) - %s%n", 
                        vendor.getName(), 
                        vendor.getVendorCode(),
                        vendor.getStatus());
                }
            }
            
            System.out.println("----------------------------------------------------------------------");
        }
        
        System.out.println("==========================================================================");
    }

    private void showCreditLimitReport() {
        List<Vendor> vendors = vendorService.getAllVendors();
        
        if (vendors.isEmpty()) {
            System.out.println("\n📭 No vendors found.");
            return;
        }
        
        System.out.println("\n=========================== CREDIT LIMIT REPORT ===========================");
        System.out.println("⚠️ Vendors with High Credit Limits (Above $10,000):");
        
        long highCreditCount = 0;
        double totalHighCredit = 0;
        
        for (Vendor vendor : vendors) {
            double creditLimit = vendor.getCreditLimit() != null ? vendor.getCreditLimit() : 0;
            if (creditLimit > 10000) {
                highCreditCount++;
                totalHighCredit += creditLimit;
                System.out.printf("    • %s (%s): $%,.2f%n", 
                    vendor.getName(), 
                    vendor.getVendorCode(),
                    creditLimit);
            }
        }
        
        if (highCreditCount == 0) {
            System.out.println("    None");
        }
        
        System.out.println("\n📊 Credit Limit Summary:");
        double totalCreditLimit = vendors.stream()
            .mapToDouble(v -> v.getCreditLimit() != null ? v.getCreditLimit() : 0)
            .sum();
        
        double avgCreditLimit = vendors.size() > 0 ? totalCreditLimit / vendors.size() : 0;
        
        System.out.println("  Total Credit Limit: $ " + String.format("%,.2f", totalCreditLimit));
        System.out.println("  Average Credit Limit: $ " + String.format("%,.2f", avgCreditLimit));
        System.out.println("  Vendors with High Credit (>$10K): " + highCreditCount);
        System.out.println("  Total High Credit: $ " + String.format("%,.2f", totalHighCredit));
        
        System.out.println("\n💰 Top 5 Vendors by Credit Limit:");
        vendors.stream()
            .sorted((v1, v2) -> {
                double c1 = v1.getCreditLimit() != null ? v1.getCreditLimit() : 0;
                double c2 = v2.getCreditLimit() != null ? v2.getCreditLimit() : 0;
                return Double.compare(c2, c1);
            })
            .limit(5)
            .forEach(v -> {
                double credit = v.getCreditLimit() != null ? v.getCreditLimit() : 0;
                System.out.printf("    • %s (%s): $%,.2f%n", 
                    v.getName(), 
                    v.getVendorCode(),
                    credit);
            });
        
        System.out.println("==========================================================================");
    }

    // ==================== HELPER METHODS ====================
    private String truncate(String text, int length) {
        if (text == null) return "";
        if (text.length() <= length) return text;
        return text.substring(0, length - 3) + "...";
    }
}