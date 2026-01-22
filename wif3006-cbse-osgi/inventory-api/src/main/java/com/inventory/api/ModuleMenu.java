package com.inventory.api;

// Add this import at the top
import com.inventory.api.vendor.service.VendorService;
import org.osgi.service.component.annotations.*;

@Component(
    immediate = true,
    property = {
        "osgi.command.scope=inventory",
        "osgi.command.function=menu"
    }
)
@Service
public class ModuleMenu {
    
    // Add VendorService reference
    private VendorService vendorService;
    
    // Add this setter method
    @Reference
    public void setVendorService(VendorService vendorService) {
        this.vendorService = vendorService;
    }
    
    public void menu() {
        System.out.println("\n=== INVENTORY MANAGEMENT SYSTEM ===");
        System.out.println("Available Modules:");
        System.out.println("1. Customer Management");
        System.out.println("2. Product Management");
        System.out.println("3. Sales Order Management");
        System.out.println("4. Purchase Order Management");
        System.out.println("5. Vendor Management");  // NEW: Add vendor option
        System.out.println("0. Exit");
        System.out.print("Select module: ");
    }
    
    // Add vendor module check method
    public boolean isVendorModuleAvailable() {
        return vendorService != null;
    }
    
    // Add vendor statistics method
    public void vendorStats() {
        if (vendorService != null) {
            System.out.println("\n=== VENDOR STATISTICS ===");
            System.out.println("Total Vendors: " + vendorService.getVendorCount());
            System.out.println("Active Vendors: " + vendorService.getActiveVendorCount());
        } else {
            System.out.println("Vendor module not available.");
        }
    }
}