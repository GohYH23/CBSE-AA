package com.inventory.launcher;

import org.apache.felix.framework.Felix;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class Launcher {
    public static void main(String[] args) {
        try {
            System.out.println("🚀 Starting Pure OSGi Inventory System...");

            // 1. Configure Framework
            Map<String, String> config = new HashMap<>();
            config.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);

            // 2. Start Felix
            Felix felix = new Felix(config);
            felix.start();
            BundleContext context = felix.getBundleContext();

            // 3. Define path to your modules
            // Assuming we run this from the root folder
            String rootPath = new File(".").getAbsolutePath();
            List<String> modules = new ArrayList<>();
            modules.add("/inventory-api/target/inventory-api-1.0.0.jar");
            modules.add("/customer-bundle/target/customer-bundle-1.0.0.jar");
            // Add other bundles here...

            // 4. Install & Start Bundles
            for (String modulePath : modules) {
                // Remove trailing dot if present in path manipulation
                String fullPath = "file:" + rootPath.replace("/.", "") + modulePath;
                try {
                    Bundle b = context.installBundle(fullPath);
                    b.start();
                    System.out.println("   Installed: " + b.getSymbolicName());
                } catch (Exception e) {
                    System.err.println("   Failed: " + modulePath + " -> " + e.getMessage());
                }
            }

            // Keep running
            felix.waitForStop(0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}