package com.inventory.vendor;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import com.inventory.vendor.api.VendorService;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class VendorDependencyChecker {
    
    public static void checkDependencies() {
        System.out.println("\n" +
            "╔══════════════════════════════════════════════════════╗\n" +
            "║       VENDOR BUNDLE DEPENDENCY CHECK                ║\n" +
            "╠══════════════════════════════════════════════════════╣");
        
        try {
            BundleContext context = FrameworkUtil.getBundle(VendorDependencyChecker.class)
                                                .getBundleContext();
            
            if (context == null) {
                System.out.println("║ ❌ ERROR: Bundle context is null                        ║");
                System.out.println("║    Make sure bundle is started in OSGi container       ║");
                System.out.println("╚══════════════════════════════════════════════════════╝");
                return;
            }
            
            // Check 1: OSGi Framework
            System.out.println("║ [1] OSGi Framework:                 ✅ AVAILABLE        ║");
            
            // Check 2: Bundle Status
            System.out.printf("║ [2] Bundle Status:                  %-19s║\n",
                context.getBundle().getState() == org.osgi.framework.Bundle.ACTIVE ? 
                "✅ ACTIVE" : "⚠️  INACTIVE");
            
            // Check 3: VendorService Registration
            ServiceReference<VendorService> serviceRef = 
                context.getServiceReference(VendorService.class);
            
            if (serviceRef != null) {
                VendorService service = context.getService(serviceRef);
                System.out.println("║ [3] VendorService:                  ✅ REGISTERED       ║");
                System.out.printf("║     Service Properties:             %-19s║\n",
                    serviceRef.getProperty("service.description"));
                
                // Check service methods
                checkServiceMethods(service);
            } else {
                System.out.println("║ [3] VendorService:                  ❌ NOT FOUND        ║");
            }
            
            // Check 4: Required Packages
            System.out.println("║ [4] Package Dependencies:                                ║");
            checkRequiredPackages();
            
            // Check 5: Bundle Info
            System.out.println("║ [5] Bundle Information:                                  ║");
            System.out.printf("║     Symbolic Name:            %-25s║\n",
                context.getBundle().getSymbolicName());
            System.out.printf("║     Version:                  %-25s║\n",
                context.getBundle().getVersion());
            System.out.printf("║     ID:                       %-25s║\n",
                context.getBundle().getBundleId());
            
            // Check 6: Service Count
            ServiceReference<?>[] allRefs = context.getAllServiceReferences(null, null);
            System.out.printf("║ [6] Total Services in Container: %-22d║\n",
                allRefs != null ? allRefs.length : 0);
            
            System.out.println("╠══════════════════════════════════════════════════════╣");
            System.out.println("║ ✅ Dependency check completed successfully!         ║");
            System.out.println("╚══════════════════════════════════════════════════════╝\n");
            
        } catch (Exception e) {
            System.out.println("║ ❌ ERROR during dependency check:                   ║");
            System.out.printf("║    %-45s║\n", e.getMessage());
            System.out.println("╚══════════════════════════════════════════════════════╝");
        }
    }
    
    private static void checkServiceMethods(VendorService service) {
        try {
            Method[] methods = VendorService.class.getDeclaredMethods();
            int methodCount = methods.length;
            System.out.printf("║     Methods Available:           %-25d║\n", methodCount);
            
            // Check if key methods exist
            List<String> keyMethods = Arrays.asList(
                "addVendor", "getVendor", "getAllVendors", 
                "updateVendor", "deleteVendor"
            );
            
            long availableKeyMethods = Arrays.stream(methods)
                .map(Method::getName)
                .filter(keyMethods::contains)
                .count();
            
            System.out.printf("║     Key Methods (5/5):           %-25s║\n",
                availableKeyMethods == 5 ? "✅ ALL AVAILABLE" : 
                "⚠️  " + availableKeyMethods + "/5 available");
                
        } catch (Exception e) {
            System.out.printf("║     Methods Check:               %-25s║\n",
                "❌ FAILED");
        }
    }
    
    private static void checkRequiredPackages() {
        String[] requiredPackages = {
            "org.osgi.framework",
            "org.osgi.service.component",
            "org.osgi.service.component.annotations",
            "org.osgi.annotation.versioning",
            "java.util",
            "java.lang"
        };
        
        for (String pkg : requiredPackages) {
            try {
                if (pkg.startsWith("java.")) {
                    Class.forName(pkg + ".Object");
                    System.out.printf("║     %-30s ✅ AVAILABLE        ║\n", pkg);
                } else {
                    // For OSGi packages, try to load a class
                    Class.forName(pkg + ".Bundle");
                    System.out.printf("║     %-30s ✅ AVAILABLE        ║\n", pkg);
                }
            } catch (ClassNotFoundException e) {
                System.out.printf("║     %-30s ❌ MISSING           ║\n", pkg);
            } catch (Exception e) {
                System.out.printf("║     %-30s ⚠️  CHECK ERROR       ║\n", pkg);
            }
        }
    }
    
    // Command line entry point for testing
    public static void main(String[] args) {
        System.out.println("Running Vendor Dependency Check...");
        checkDependencies();
    }
}