package com.inventory.vendor;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.inventory.vendor.api.VendorService;
import java.util.Hashtable;

public class VendorActivator implements BundleActivator {
    
    private ServiceRegistration<VendorService> serviceRegistration;
    
    @Override
    public void start(BundleContext bundleContext) throws Exception {
        System.out.println("=========================================");
        System.out.println("Starting Vendor Bundle v1.0.0");
        System.out.println("=========================================");
        
        // Create service instance
        VendorService vendorService = new VendorServiceImpl();
        
        // Register the service
        Hashtable<String, String> properties = new Hashtable<>();
        properties.put("service.description", "Vendor Management Service");
        properties.put("service.version", "1.0.0");
        
        serviceRegistration = bundleContext.registerService(
            VendorService.class, 
            vendorService, 
            properties
        );
        
        System.out.println("✓ Vendor Service registered successfully");
        System.out.println("✓ Bundle ID: " + bundleContext.getBundle().getBundleId());
        System.out.println("✓ Bundle Symbolic Name: " + bundleContext.getBundle().getSymbolicName());
        System.out.println("=========================================\n");
        
        // Display startup message
        displayWelcomeMessage();
    }
    
    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        System.out.println("=========================================");
        System.out.println("Stopping Vendor Bundle");
        System.out.println("=========================================");
        
        // Unregister service
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
            System.out.println("✓ Vendor Service unregistered");
        }
        
        System.out.println("✓ Vendor Bundle stopped successfully");
        System.out.println("=========================================\n");
    }
    
    private void displayWelcomeMessage() {
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║          VENDOR MANAGEMENT SYSTEM            ║");
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.println("║ Available Commands:                          ║");
        System.out.println("║   • vendormenu    - Open vendor console      ║");
        System.out.println("║   • vendorcheck   - Check dependencies       ║");
        System.out.println("╚══════════════════════════════════════════════╝\n");
        System.out.println("Type 'vendormenu' in OSGi console to start.");
    }
}