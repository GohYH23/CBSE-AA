package com.inventory.launcher;

import org.apache.felix.framework.Felix;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import java.io.File;
import java.io.InputStream;
import java.util.*;

public class Launcher {

    public static void main(String[] args) {
        try {
            System.out.println("Starting Pure OSGi Inventory System...");

            /* ------------------------------------------------------------------
             * Load configuration (MongoDB URI)
             * ------------------------------------------------------------------ */
            try (InputStream input =
                         Launcher.class.getClassLoader().getResourceAsStream("osgi.properties")) {

                if (input != null) {
                    Properties prop = new Properties();
                    prop.load(input);

                    String uri = prop.getProperty("mongodb.uri");
                    if (uri != null) {
                        System.setProperty("mongodb.uri", uri);
                        System.out.println("Configuration Loaded: MongoDB URI set.");
                    }
                } else {
                    System.out.println("Warning: osgi.properties not found!");
                }
            }

            /* ------------------------------------------------------------------
             * Configure & start Felix framework
             * ------------------------------------------------------------------ */
            Map<String, String> config = new HashMap<>();
            config.put(Constants.FRAMEWORK_STORAGE_CLEAN,
                    Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);

            Felix felix = new Felix(config);
            felix.start();

            BundleContext context = felix.getBundleContext();

            /* ------------------------------------------------------------------
             * Path setup (SIMPLE & CORRECT)
             * ------------------------------------------------------------------ */
            String baseDir = new File(".").getAbsolutePath().replace("\\", "/");
            if (!baseDir.endsWith("/")) {
                baseDir += "/";
            }

            String bundlesDir = baseDir + "target/bundles/";

            /* ------------------------------------------------------------------
             * Define bundles
             * ------------------------------------------------------------------ */
            List<String> infrastructureBundles = List.of(
                    bundlesDir + "org.osgi.util.function-1.2.0.jar",
                    bundlesDir + "org.osgi.util.promise-1.2.0.jar",
                    bundlesDir + "org.osgi.service.component-1.5.0.jar",
                    bundlesDir + "org.apache.felix.scr-2.2.6.jar",
                    bundlesDir + "bson-4.10.2.jar",
                    bundlesDir + "mongodb-driver-core-4.10.2.jar",
                    bundlesDir + "mongodb-driver-sync-4.10.2.jar"
            );

            List<String> projectBundles = List.of(
                    bundlesDir + "inventory-api-1.0.0.jar",
                    bundlesDir + "main-menu-bundle-1.0.0.jar",
                    bundlesDir + "customer-bundle-1.0.0.jar",
                    bundlesDir + "purchase-order-bundle-1.0.0.jar",
                    bundlesDir + "sales-order-bundle-1.0.0.jar",
                    bundlesDir + "product-bundle-1.0.0.jar"
            );

            /* ------------------------------------------------------------------
             * Install & start infrastructure
             * ------------------------------------------------------------------ */
            System.out.println("--- Loading Infrastructure ---");
            for (String path : infrastructureBundles) {
                installAndStart(context, path);
            }

            /* ------------------------------------------------------------------
             * Install & start project modules
             * ------------------------------------------------------------------ */
            System.out.println("--- Loading Modules ---");
            for (String path : projectBundles) {
                installAndStart(context, path);
            }

            /* ------------------------------------------------------------------
             * Keep framework running
             * ------------------------------------------------------------------ */
            felix.waitForStop(0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* --------------------------------------------------------------------------
     * Helper: Install & start a bundle
     * -------------------------------------------------------------------------- */
    private static void installAndStart(BundleContext context, String bundlePath) {
        File jar = new File(bundlePath);

        if (!jar.exists()) {
            System.err.println("Bundle file not found: " + bundlePath);
            return;
        }

        try {
            Bundle bundle = context.installBundle(jar.toURI().toString());
            bundle.start();
            System.out.println("Installed & Started: " + bundle.getSymbolicName());
        } catch (Exception e) {
            System.err.println("Failed to load bundle: " + bundlePath);
            e.printStackTrace();
        }
    }
}
