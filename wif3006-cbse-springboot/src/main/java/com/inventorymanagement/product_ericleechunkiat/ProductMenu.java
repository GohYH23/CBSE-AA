package com.inventorymanagement.product_ericleechunkiat;

import com.inventorymanagement.product_ericleechunkiat.model.*;
import com.inventorymanagement.product_ericleechunkiat.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Scanner;
import java.util.List;

@Component
public class ProductMenu {

    @Autowired
    private ProductService productService;

    public void start() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== SPRING BOOT PRODUCT HUB ===");
            System.out.println("1. Manage Products");
            System.out.println("2. Manage Product Groups");
            System.out.println("3. Manage Unit Measures");
            System.out.println("4. Manage Warehouses");
            System.out.println("5. Manage Stock Counts");
            System.out.println("0. Back");
            System.out.print("Select: ");

            String choice = scanner.nextLine();
            try {
                switch (choice) {
                    case "1": handleProducts(scanner); break;
                    case "2": handleGroups(scanner); break;
                    case "3": handleUOM(scanner); break;
                    case "4": handleWarehouses(scanner); break;
                    case "5": handleStock(scanner); break;
                    case "0": return;
                    default: System.out.println("Invalid option.");
                }
            } catch (Exception e) {
                System.out.println("⚠️ " + e.getMessage());
            }
        }
    }

    private String prompt(Scanner s, String label) {
        while (true) {
            System.out.print(label);
            String input = s.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.println("Error: Field cannot be empty.");
        }
    }

    // --- 1. Products ---
    private void handleProducts(Scanner s) {
        System.out.println("\n--- Products ---");
        System.out.println("1. Add | 2. List | 3. Edit | 4. Delete");
        String c = s.nextLine();

        if (c.equals("1")) {
            // 👇 NOW USES SEQUENTIAL ID (1, 2, 3...)
            String id = generateProductId();
            System.out.println("Generating New ID: " + id);

            String name = prompt(s, "Name: ");
            double price = Double.parseDouble(prompt(s, "Price: "));

            System.out.println("\nAvailable Groups:");
            productService.getAllGroups().forEach(g -> System.out.println("- " + g.getGroupId() + ": " + g.getGroupName()));
            System.out.print("Enter Group ID: "); String gid = s.nextLine();

            System.out.println("\nAvailable UOMs:");
            productService.getAllUOMs().forEach(u -> System.out.println("- " + u.getUomId() + ": " + u.getSymbol()));
            System.out.print("Enter UOM ID: "); String uid = s.nextLine();

            productService.addProduct(new Product(id, name, price, gid, uid));
            System.out.println("✅ Added.");

        } else if (c.equals("2")) {
            System.out.println("\n====================== PRODUCT LIST ======================");
            System.out.printf("%-6s | %-15s | %-10s | %-15s | %-10s%n", "ID", "Name", "Price", "Group", "UOM");
            System.out.println("----------------------------------------------------------");

            List<ProductGroup> groups = productService.getAllGroups();
            List<UnitMeasure> uoms = productService.getAllUOMs();

            for (Product p : productService.getAllProducts()) {
                String gName = "N/A";
                for (ProductGroup g : groups) if(g.getGroupId().equals(p.getProductGroupId())) gName = g.getGroupName();

                String uName = "N/A";
                for (UnitMeasure u : uoms) if(u.getUomId().equals(p.getUomId())) uName = u.getSymbol();

                System.out.printf("%-6s | %-15s | $%-9.2f | %-15s | %-10s%n",
                        p.getId(), p.getName(), p.getPrice(), gName, uName);
            }
            System.out.println("==========================================================");

        } else if (c.equals("3")) {
            System.out.print("ID to Edit: "); String id = s.nextLine();
            Product p = productService.getProductById(id).orElse(null);
            if (p != null) {
                String n = prompt(s, "New Name ("+p.getName()+"): ");
                p.setName(n);
                String pr = prompt(s, "New Price ("+p.getPrice()+"): ");
                p.setPrice(Double.parseDouble(pr));
                productService.updateProduct(id, p);
                System.out.println("✅ Updated.");
            } else {
                System.out.println("❌ Product not found.");
            }
        } else if (c.equals("4")) {
            System.out.print("ID to Delete: "); productService.deleteProduct(s.nextLine());
            System.out.println("✅ Deleted.");
        }
    }

    // --- 2. Groups ---
    private void handleGroups(Scanner s) {
        System.out.println("\n--- Groups ---");
        System.out.println("1. Add | 2. List | 3. Delete");
        String c = s.nextLine();
        if (c.equals("1")) {
            // 👇 NOW USES SEQUENTIAL ID
            String id = generateGroupId();
            System.out.println("Generating Group ID: " + id);

            String name = prompt(s, "Name: ");
            String desc = prompt(s, "Desc: ");
            productService.addGroup(new ProductGroup(id, name, desc));
            System.out.println("✅ Group Added.");

        } else if (c.equals("2")) {
            System.out.println("\n=========== GROUPS ===========");
            System.out.printf("%-6s | %-15s | %-20s%n", "ID", "Name", "Description");
            System.out.println("---------------------------------------------");
            productService.getAllGroups().forEach(g ->
                    System.out.printf("%-6s | %-15s | %-20s%n", g.getGroupId(), g.getGroupName(), g.getDescription()));
            System.out.println("==============================");

        } else if (c.equals("3")) {
            System.out.print("ID to Delete: "); productService.deleteGroup(s.nextLine());
            System.out.println("✅ Deleted.");
        }
    }

    // --- 3. UOM ---
    private void handleUOM(Scanner s) {
        System.out.println("\n--- UOM ---");
        System.out.println("1. Add | 2. List | 3. Delete");
        String c = s.nextLine();
        if (c.equals("1")) {
            // 👇 NOW USES SEQUENTIAL ID
            String id = generateUomId();
            System.out.println("Generating UOM ID: " + id);

            String name = prompt(s, "Name: ");
            String sym = prompt(s, "Symbol: ");
            productService.addUOM(new UnitMeasure(id, name, sym));
            System.out.println("✅ UOM Added.");

        } else if (c.equals("2")) {
            System.out.println("\n=========== UOM LIST ===========");
            System.out.printf("%-6s | %-15s | %-10s%n", "ID", "Name", "Symbol");
            System.out.println("-----------------------------------");
            productService.getAllUOMs().forEach(u ->
                    System.out.printf("%-6s | %-15s | %-10s%n", u.getUomId(), u.getUnitName(), u.getSymbol()));
            System.out.println("================================");

        } else if (c.equals("3")) {
            System.out.print("ID to Delete: "); productService.deleteUOM(s.nextLine());
            System.out.println("✅ Deleted.");
        }
    }

    // --- 4. Warehouses ---
    private void handleWarehouses(Scanner s) {
        System.out.println("\n--- Warehouses ---");
        System.out.println("1. Add | 2. List | 3. Delete");
        String c = s.nextLine();
        if (c.equals("1")) {
            String name = prompt(s, "Name: ");
            String desc = prompt(s, "Desc: ");
            productService.addWarehouse(new Warehouse(name, false, desc));
            System.out.println("✅ Warehouse Added.");

        } else if (c.equals("2")) {
            System.out.println("\n=========== WAREHOUSES ===========");
            System.out.printf("%-15s | %-20s%n", "Name", "Description");
            System.out.println("-------------------------------------");
            productService.getAllWarehouses().forEach(w ->
                    System.out.printf("%-15s | %-20s%n", w.getName(), w.getDescription()));
            System.out.println("==================================");

        } else if (c.equals("3")) {
            System.out.print("Name to Delete: "); productService.deleteWarehouse(s.nextLine());
            System.out.println("✅ Deleted.");
        }
    }

    // --- 5. Stock ---
    private void handleStock(Scanner s) {
        System.out.println("\n--- Stock ---");
        System.out.println("1. Add | 2. List | 3. Export");
        String c = s.nextLine();
        if (c.equals("1")) {
            String id = "SC-" + (System.currentTimeMillis() % 10000);
            String wh = prompt(s, "Warehouse: ");
            String date = prompt(s, "Date: ");
            productService.createStockCount(new StockCount(id, wh, "Pending", date));
            System.out.println("✅ Count Started: " + id);

        } else if (c.equals("2")) {
            System.out.println("\n=========== STOCK HISTORY ===========");
            System.out.printf("%-10s | %-15s | %-12s | %-10s%n", "ID", "Warehouse", "Date", "Status");
            System.out.println("-----------------------------------------------------");
            productService.getAllStockCounts().forEach(sc ->
                    System.out.printf("%-10s | %-15s | %-12s | %-10s%n", sc.getCountId(), sc.getWarehouseName(), sc.getDate(), sc.getStatus()));
            System.out.println("=====================================");

        } else if (c.equals("3")) {
            System.out.println("✅ Report Exported (Mock).");
        }
    }

    // ==========================================
    // 👇 AUTO-INCREMENT HELPERS (Like OSGi)
    // ==========================================
    private String generateProductId() {
        int max = 0;
        for (Product p : productService.getAllProducts()) {
            try { int id = Integer.parseInt(p.getId()); if (id > max) max = id; } catch (Exception e) {}
        }
        return String.valueOf(max + 1);
    }

    private String generateGroupId() {
        int max = 0;
        for (ProductGroup g : productService.getAllGroups()) {
            try { int id = Integer.parseInt(g.getGroupId()); if (id > max) max = id; } catch (Exception e) {}
        }
        return String.valueOf(max + 1);
    }

    private String generateUomId() {
        int max = 0;
        for (UnitMeasure u : productService.getAllUOMs()) {
            try { int id = Integer.parseInt(u.getUomId()); if (id > max) max = id; } catch (Exception e) {}
        }
        return String.valueOf(max + 1);
    }
}