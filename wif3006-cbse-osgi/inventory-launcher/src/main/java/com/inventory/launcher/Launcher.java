package com.inventory.launcher;

import org.apache.felix.framework.Felix;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

            // --- PATH SETUP ---
            String rootPath = new File(".").getAbsolutePath().replace(".", "");

            // 3. Define Bundle Lists
            List<String> infrastructureBundles = new ArrayList<>();
            List<String> projectBundles = new ArrayList<>();

            // A. Infrastructure (The "Manager" components)
            // These are copied here by the maven-dependency-plugin
            String libDir = "inventory-launcher/target/bundles/";

            // ORDER MATTERS: Function -> Promise -> API -> SCR
            infrastructureBundles.add(libDir + "org.osgi.util.function-1.2.0.jar");
            infrastructureBundles.add(libDir + "org.osgi.util.promise-1.2.0.jar");
            infrastructureBundles.add(libDir + "org.osgi.service.component-1.5.0.jar");
            infrastructureBundles.add(libDir + "org.apache.felix.scr-2.2.6.jar");

            // B. Project Bundles (Your Code)
            projectBundles.add("inventory-api/target/inventory-api-1.0.0.jar");
            projectBundles.add("customer-bundle/target/customer-bundle-1.0.0.jar");

            // 4. Install & Start Infrastructure
            System.out.println("--- Loading Infrastructure ---");
            for (String path : infrastructureBundles) {
                installAndStart(context, rootPath, path);
            }

            // 5. Install & Start Modules
            System.out.println("--- Loading Modules ---");
            for (String path : projectBundles) {
                installAndStart(context, rootPath, path);
            }

            // Keep running
            felix.waitForStop(0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void installAndStart(BundleContext context, String rootPath, String relativePath) {
        File f = new File(relativePath);
        String fullPath = "file:" + f.getAbsolutePath();

        try {
            Bundle b = context.installBundle(fullPath);
            b.start();
            System.out.println("   ✅ Installed & Started: " + b.getSymbolicName());
        } catch (Exception e) {
            System.err.println("   ❌ Failed to load: " + relativePath);
            System.err.println("      Error: " + e.getMessage());
        }
    }
}