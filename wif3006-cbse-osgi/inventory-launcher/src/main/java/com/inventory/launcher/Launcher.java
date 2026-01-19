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
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public class Launcher {
    public static void main(String[] args) {
        try {
            System.out.println("🚀 Starting Pure OSGi Inventory System...");

            try (InputStream input = Launcher.class.getClassLoader().getResourceAsStream("osgi.properties")) {
                if (input == null) {
                    System.out.println("⚠️ Warning: osgi.properties not found!");
                } else {
                    Properties prop = new Properties();
                    prop.load(input);

                    // 1. Get the URI from the file
                    String uri = prop.getProperty("mongodb.uri");

                    // 2. Save it to the System (Global Variable)
                    // Now ANY bundle can read this using System.getProperty()
                    if (uri != null) {
                        System.setProperty("mongodb.uri", uri);
                        System.out.println("📝 Configuration Loaded: MongoDB URI set.");
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            // 1. Configure Framework
            Map<String, String> config = new HashMap<>();
            config.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);

            // 2. Start Felix
            Felix felix = new Felix(config);
            felix.start();
            BundleContext context = felix.getBundleContext();

            // --- PATH SETUP ---
            // Determine the project root directory
            // If running from inventory-launcher/target, go up two levels
            // If running from wif3006-cbse-osgi, use current directory
            File currentDir = new File(".").getAbsoluteFile();
            String rootPath;
            
            // Check if we're in the target directory
            if (currentDir.getName().equals("target") && currentDir.getParentFile().getName().equals("inventory-launcher")) {
                // We're in inventory-launcher/target, go up to project root
                rootPath = currentDir.getParentFile().getParentFile().getAbsolutePath();
            } else {
                // We're in project root
                rootPath = currentDir.getAbsolutePath();
            }
            
            // Ensure path uses forward slashes (or use File.separator)
            rootPath = rootPath.replace("\\", "/");
            if (!rootPath.endsWith("/")) {
                rootPath += "/";
            }

            // 3. Define Bundle Lists
            List<String> infrastructureBundles = new ArrayList<>();
            List<String> projectBundles = new ArrayList<>();

            // A. Infrastructure (The "Manager" components)
            // These are copied here by the maven-dependency-plugin
            String libDir = rootPath + "inventory-launcher/target/bundles/";

            // ORDER MATTERS: Function -> Promise -> API -> SCR
            infrastructureBundles.add(libDir + "org.osgi.util.function-1.2.0.jar");
            infrastructureBundles.add(libDir + "org.osgi.util.promise-1.2.0.jar");
            infrastructureBundles.add(libDir + "org.osgi.service.component-1.5.0.jar");
            infrastructureBundles.add(libDir + "org.apache.felix.scr-2.2.6.jar");

            // B. Project Bundles (Your Code)
            projectBundles.add(rootPath + "inventory-api/target/inventory-api-1.0.0.jar");
            projectBundles.add(rootPath + "customer-bundle/target/customer-bundle-1.0.0.jar");
            projectBundles.add(rootPath + "purchase-order-bundle/target/purchase-order-bundle-1.0.0.jar");

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

    private static void installAndStart(BundleContext context, String rootPath, String bundlePath) {
        // bundlePath is already an absolute path from rootPath
        File f = new File(bundlePath);
        
        if (!f.exists()) {
            System.err.println("   ❌ Bundle file not found: " + bundlePath);
            return;
        }
        
        String fullPath = "file:" + f.getAbsolutePath();

        try {
            Bundle b = context.installBundle(fullPath);
            b.start();
            System.out.println("   ✅ Installed & Started: " + b.getSymbolicName());
        } catch (Exception e) {
            System.err.println("   ❌ Failed to load: " + bundlePath);
            System.err.println("      Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}