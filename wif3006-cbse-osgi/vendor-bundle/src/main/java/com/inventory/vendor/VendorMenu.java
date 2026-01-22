package com.inventory.vendor;

import com.inventory.vendor.api.VendorService;
import org.osgi.service.component.annotations.*;
import org.osgi.service.component.ComponentContext;
import java.util.Scanner;
import java.util.List;

@Component(
    immediate = true,
    property = {
        "osgi.command.scope=vendor",
        "osgi.command.function=vendormenu",
        "osgi.command.function=vendorcheck"
    },
    service = VendorMenu.class
)
@Service
public class VendorMenu {
    
    private VendorService vendorService;
    private final Scanner scanner = new Scanner(System.in);
    private boolean isRunning = false;
    
    @Reference
    public void setVendorService(VendorService vendorService) {
        this.vendorService = vendorService;
    }
    
    @Activate
    public void activate(ComponentContext context) {
        System.out.println("VendorMenu component activated");
    }
    
    @Deactivate
    public void deactivate() {
        System.out.println("VendorMenu component deactivated");
        if (isRunning) {
            scanner.close();
        }
    }
    
    public void vendormenu() {
        isRunning = true;
        displayHeader();
        
        boolean exit = false;
        
        while (!exit && isRunning) {
            displayMainMenu();
            System.out.print("\nEnter your choice (0-9): ");
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    addVendor();
                    break;
                case "2":
                    viewAllVendors();
                    break;
                case "3":
                    searchVendorById();
                    break;
                case "4":
                    searchVendorsByName();
                    break;
                case "5":
                    updateVendor();
                    break;
                case "6":
                    deleteVendor();
                    break;
                case "7":
                    manageVendorStatus();
                    break;
                case "8":
                    viewVendorsByRating();
                    break;
                case "9":
                    viewStatistics();
                    break;
                case "0":
                    exit = true;
                    System.out.println("\nExiting Vendor Menu...");
                    break;
                default:
                    System.out.println("\n❌ Invalid choice. Please enter a number between 0-9.");
            }
            
            if (!exit) {
                System.out.print("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }
        
        isRunning = false;
    }
    
    public void vendorcheck() {
        VendorDependencyChecker.checkDependencies();
    }
    
    private void displayHeader() {
        System.out.println("\n" + 
            "╔══════════════════════════════════════════════════════╗\n" +
            "║            VENDOR MANAGEMENT CONSOLE                 ║\n" +
            "║            Version 1.0.0                            ║\n" +
            "╚══════════════════════════════════════════════════════╝\n");
    }
    
    private void displayMainMenu() {
        System.out.println("\n" +
            "┌────────────────────────────────────────────────────┐\n" +
            "│                     MAIN MENU                      │\n" +
            "├────────────────────────────────────────────────────┤\n" +
            "│  1. 📝 Add New Vendor                              │\n" +
            "│  2. 👁️  View All Vendors                           │\n" +
            "│  3. 🔍 Search Vendor by ID                         │\n" +
            "│  4. 🔎 Search Vendors by Name                      │\n" +
            "│  5. ✏️  Update Vendor                              │\n" +
            "│  6. 🗑️  Delete Vendor                              │\n" +
            "│  7. ⚙️  Manage Vendor Status (Activate/Deactivate)  │\n" +
            "│  8. ⭐ View Vendors by Rating                      │\n" +
            "│  9. 📊 View Statistics                             │\n" +
            "│  0. 🚪 Exit                                        │\n" +
            "└────────────────────────────────────────────────────┘");
    }
    
    private void addVendor() {
        System.out.println("\n" +
            "┌────────────────────────────────────────────────────┐\n" +
            "│                  ADD NEW VENDOR                    │\n" +
            "└────────────────────────────────────────────────────┘");
        
        Vendor vendor = new Vendor();
        
        System.out.print("Enter Vendor ID (Format: V001): ");
        vendor.setVendorId(scanner.nextLine().trim().toUpperCase());
        
        System.out.print("Enter Vendor Name: ");
        vendor.setName(scanner.nextLine().trim());
        
        System.out.print("Enter Contact Person: ");
        vendor.setContactPerson(scanner.nextLine().trim());
        
        System.out.print("Enter Email: ");
        vendor.setEmail(scanner.nextLine().trim());
        
        System.out.print("Enter Phone (10 digits): ");
        vendor.setPhone(scanner.nextLine().trim());
        
        System.out.print("Enter Address: ");
        vendor.setAddress(scanner.nextLine().trim());
        
        try {
            Vendor addedVendor = vendorService.addVendor(vendor);
            System.out.println("\n✅ Vendor added successfully!");
            System.out.println("📋 Details: " + addedVendor);
        } catch (Exception e) {
            System.out.println("\n❌ Error: " + e.getMessage());
        }
    }
    
    private void viewAllVendors() {
        List<Vendor> vendors = vendorService.getAllVendors();
        
        System.out.println("\n" +
            "┌────────────────────────────────────────────────────┐\n" +
            "│                ALL VENDORS (" + vendors.size() + ")                  │\n" +
            "└────────────────────────────────────────────────────┘");
        
        if (vendors.isEmpty()) {
            System.out.println("📭 No vendors found.");
        } else {
            System.out.println("┌─────┬────────────┬────────────────────┬────────────────────┬──────────────┬──────────────┐");
            System.out.println("│ ID  │    Name    │    Contact Person  │       Email        │    Phone     │    Rating    │");
            System.out.println("├─────┼────────────┼────────────────────┼────────────────────┼──────────────┼──────────────┤");
            
            for (Vendor vendor : vendors) {
                String status = vendor.isActive() ? "✅" : "❌";
                System.out.printf("│ %-3s │ %-10s │ %-18s │ %-18s │ %-12s │ %-6.1f %-5s │\n",
                    vendor.getVendorId(),
                    truncate(vendor.getName(), 10),
                    truncate(vendor.getContactPerson(), 18),
                    truncate(vendor.getEmail(), 18),
                    vendor.getPhone(),
                    vendor.getRating(),
                    status);
            }
            System.out.println("└─────┴────────────┴────────────────────┴────────────────────┴──────────────┴──────────────┘");
        }
    }
    
    private void searchVendorById() {
        System.out.print("\nEnter Vendor ID to search: ");
        String vendorId = scanner.nextLine().trim().toUpperCase();
        
        vendorService.getVendor(vendorId).ifPresentOrElse(
            vendor -> {
                System.out.println("\n✅ Vendor Found:");
                System.out.println("┌────────────────────────────────────────────────────┐");
                System.out.println("│                    VENDOR DETAILS                  │");
                System.out.println("├────────────────────────────────────────────────────┤");
                System.out.printf("│ ID:            %-35s │\n", vendor.getVendorId());
                System.out.printf("│ Name:          %-35s │\n", vendor.getName());
                System.out.printf("│ Contact:       %-35s │\n", vendor.getContactPerson());
                System.out.printf("│ Email:         %-35s │\n", vendor.getEmail());
                System.out.printf("│ Phone:         %-35s │\n", vendor.getPhone());
                System.out.printf("│ Address:       %-35s │\n", vendor.getAddress());
                System.out.printf("│ Rating:        %-35.1f │\n", vendor.getRating());
                System.out.printf("│ Status:        %-35s │\n", 
                    vendor.isActive() ? "Active ✅" : "Inactive ❌");
                System.out.println("└────────────────────────────────────────────────────┘");
            },
            () -> System.out.println("\n❌ Vendor not found with ID: " + vendorId)
        );
    }
    
    private void searchVendorsByName() {
        System.out.print("\nEnter vendor name to search: ");
        String name = scanner.nextLine().trim();
        
        List<Vendor> results = vendorService.searchVendorsByName(name);
        
        System.out.println("\n" +
            "┌────────────────────────────────────────────────────┐\n" +
            "│           SEARCH RESULTS (" + results.size() + " found)               │\n" +
            "└────────────────────────────────────────────────────┘");
        
        if (results.isEmpty()) {
            System.out.println("📭 No vendors found matching: " + name);
        } else {
            results.forEach(vendor -> 
                System.out.printf("• %s - %s (Rating: %.1f, Status: %s)\n",
                    vendor.getVendorId(), vendor.getName(), 
                    vendor.getRating(), vendor.isActive() ? "Active" : "Inactive")
            );
        }
    }
    
    private void updateVendor() {
        System.out.print("\nEnter Vendor ID to update: ");
        String vendorId = scanner.nextLine().trim().toUpperCase();
        
        vendorService.getVendor(vendorId).ifPresentOrElse(
            existingVendor -> {
                System.out.println("\nCurrent Details:");
                System.out.println(existingVendor);
                System.out.println("\nEnter new details (press Enter to keep current value):");
                
                System.out.print("Name [" + existingVendor.getName() + "]: ");
                String name = scanner.nextLine().trim();
                if (!name.isEmpty()) existingVendor.setName(name);
                
                System.out.print("Contact Person [" + existingVendor.getContactPerson() + "]: ");
                String contact = scanner.nextLine().trim();
                if (!contact.isEmpty()) existingVendor.setContactPerson(contact);
                
                System.out.print("Email [" + existingVendor.getEmail() + "]: ");
                String email = scanner.nextLine().trim();
                if (!email.isEmpty()) existingVendor.setEmail(email);
                
                System.out.print("Phone [" + existingVendor.getPhone() + "]: ");
                String phone = scanner.nextLine().trim();
                if (!phone.isEmpty()) existingVendor.setPhone(phone);
                
                System.out.print("Address [" + existingVendor.getAddress() + "]: ");
                String address = scanner.nextLine().trim();
                if (!address.isEmpty()) existingVendor.setAddress(address);
                
                System.out.print("Rating [" + existingVendor.getRating() + "]: ");
                String ratingStr = scanner.nextLine().trim();
                if (!ratingStr.isEmpty()) {
                    try {
                        double rating = Double.parseDouble(ratingStr);
                        if (rating >= 0 && rating <= 5) {
                            existingVendor.setRating(rating);
                        } else {
                            System.out.println("Rating must be between 0-5. Keeping current value.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid rating format. Keeping current value.");
                    }
                }
                
                try {
                    vendorService.updateVendor(existingVendor);
                    System.out.println("\n✅ Vendor updated successfully!");
                } catch (Exception e) {
                    System.out.println("\n❌ Error: " + e.getMessage());
                }
            },
            () -> System.out.println("\n❌ Vendor not found with ID: " + vendorId)
        );
    }
    
    private void deleteVendor() {
        System.out.print("\nEnter Vendor ID to delete: ");
        String vendorId = scanner.nextLine().trim().toUpperCase();
        
        System.out.print("Are you sure you want to delete vendor " + vendorId + "? (yes/no): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();
        
        if (confirmation.equals("yes") || confirmation.equals("y")) {
            if (vendorService.deleteVendor(vendorId)) {
                System.out.println("\n✅ Vendor deleted successfully!");
            } else {
                System.out.println("\n❌ Vendor not found with ID: " + vendorId);
            }
        } else {
            System.out.println("\n⚠️  Deletion cancelled.");
        }
    }
    
    private void manageVendorStatus() {
        System.out.print("\nEnter Vendor ID: ");
        String vendorId = scanner.nextLine().trim().toUpperCase();
        
        vendorService.getVendor(vendorId).ifPresentOrElse(
            vendor -> {
                System.out.println("\nCurrent Status: " + 
                    (vendor.isActive() ? "Active ✅" : "Inactive ❌"));
                System.out.println("\n1. Activate Vendor");
                System.out.println("2. Deactivate Vendor");
                System.out.println("3. Cancel");
                System.out.print("\nChoose action: ");
                
                String choice = scanner.nextLine().trim();
                
                switch (choice) {
                    case "1":
                        if (vendorService.activateVendor(vendorId)) {
                            System.out.println("\n✅ Vendor activated successfully!");
                        } else {
                            System.out.println("\n⚠️  Vendor is already active or not found.");
                        }
                        break;
                    case "2":
                        if (vendorService.deactivateVendor(vendorId)) {
                            System.out.println("\n✅ Vendor deactivated successfully!");
                        } else {
                            System.out.println("\n⚠️  Vendor is already inactive or not found.");
                        }
                        break;
                    case "3":
                        System.out.println("\n⚠️  Operation cancelled.");
                        break;
                    default:
                        System.out.println("\n❌ Invalid choice.");
                }
            },
            () -> System.out.println("\n❌ Vendor not found with ID: " + vendorId)
        );
    }
    
    private void viewVendorsByRating() {
        System.out.print("\nEnter minimum rating (0.0 - 5.0): ");
        try {
            double minRating = Double.parseDouble(scanner.nextLine().trim());
            
            if (minRating < 0 || minRating > 5) {
                System.out.println("❌ Rating must be between 0.0 and 5.0");
                return;
            }
            
            List<Vendor> vendors = vendorService.getVendorsByRating(minRating);
            
            System.out.println("\n" +
                "┌────────────────────────────────────────────────────┐\n" +
                "│      VENDORS WITH RATING >= " + minRating + " (" + vendors.size() + " found)       │\n" +
                "└────────────────────────────────────────────────────┘");
            
            if (vendors.isEmpty()) {
                System.out.println("📭 No vendors found with rating >= " + minRating);
            } else {
                vendors.forEach(vendor -> 
                    System.out.printf("• %s - %s (Rating: %.1f)\n",
                        vendor.getVendorId(), vendor.getName(), vendor.getRating())
                );
            }
        } catch (NumberFormatException e) {
            System.out.println("\n❌ Invalid rating format.");
        }
    }
    
    private void viewStatistics() {
        int totalVendors = vendorService.getVendorCount();
        int activeVendors = vendorService.getActiveVendorCount();
        int inactiveVendors = totalVendors - activeVendors;
        
        System.out.println("\n" +
            "┌────────────────────────────────────────────────────┐\n" +
            "│                VENDOR STATISTICS                   │\n" +
            "├────────────────────────────────────────────────────┤\n" +
            "│                                                    │\n" +
            "│  📊 Total Vendors:      " + String.format("%-30d", totalVendors) + "│\n" +
            "│  ✅ Active Vendors:     " + String.format("%-30d", activeVendors) + "│\n" +
            "│  ❌ Inactive Vendors:   " + String.format("%-30d", inactiveVendors) + "│\n" +
            "│                                                    │\n");
        
        if (totalVendors > 0) {
            int activePercentage = (int) ((activeVendors * 100.0) / totalVendors);
            System.out.println("│  📈 Active Rate:        " + 
                String.format("%-30s", activePercentage + "%") + "│\n" +
                "│                                                    │");
        }
        
        System.out.println("└────────────────────────────────────────────────────┘");
    }
    
    private String truncate(String str, int length) {
        if (str.length() <= length) {
            return str;
        }
        return str.substring(0, length - 3) + "...";
    }
}