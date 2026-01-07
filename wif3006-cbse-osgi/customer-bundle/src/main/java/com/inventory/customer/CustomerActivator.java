package com.inventory.customer;

import com.inventory.api.customer.CustomerService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class CustomerActivator implements BundleActivator {

    public void start(BundleContext context) {
        System.out.println("✅ Customer Bundle Starting...");

        // 1. Create the implementation
        CustomerService service = new CustomerImpl();

        // 2. Register it in OSGi (so others can see it later)
        context.registerService(CustomerService.class.getName(), service, null);

        // 3. --- TEST ZONE ---
        // We call the function directly here to test it
        System.out.println("------------------------------------------------");
        System.out.println("🧪 TESTING: addCustomer()");

        service.addCustomer("Alice Wonderland"); // <--- The function you want to test
        service.addCustomer("Bob Builder");

        System.out.println("🧪 TESTING COMPLETE");
        System.out.println("------------------------------------------------");
    }

    public void stop(BundleContext context) {
        System.out.println("❌ Customer Bundle Stopping...");
    }
}