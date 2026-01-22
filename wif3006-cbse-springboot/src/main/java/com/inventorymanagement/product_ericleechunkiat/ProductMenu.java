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
        // UPDATED: Added "0. Back"
        System.out.println("1. Add | 2. List | 3. Edit | 4. Delete | 0. Back");
        String c = s.nextLine();

        if (c.equals("0")) {
            return; // Go Back to Main Menu
        }
        else if (c.equals("1")) {
            String id = generateProductId();
            System.out.println("Generating New ID: " + id);

            String name = prompt(s, "Name: ");
            double price = Double.parseDouble(prompt(s, "Price: "));

            System.out.println("\nAvailable Groups:");
            productService.getAllProductGroups().forEach(g -> System.out.println("- " + g.getId() + ": " + g.getGroupName()));
            System.out.print("Enter Group ID: "); String gid = s.nextLine();

            System.out.println("\nAvailable UOMs:");
            productService.getAllUOMs().forEach(u -> System.out.println("- " + u.getId() + ": " + u.getSymbol()));
            System.out.print("Enter UOM ID: "); String uid = s.nextLine();

            productService.addProduct(new Product(id, name, price, gid, uid));
            System.out.println("✅ Added.");

        } else if (c.equals("2")) {
            System.out.println("\n====================== PRODUCT LIST ======================");
            System.out.printf("%-6s | %-15s | %-10s | %-15s | %-10s%n", "ID", "Name", "Price", "Group", "UOM");
            System.out.println("----------------------------------------------------------");

            List<ProductGroup> groups = productService.getAllProductGroups();
            List<UnitMeasure> uoms = productService.getAllUOMs();

            for (Product p : productService.getAllProducts()) {
                String gName = "N/A";
                for (ProductGroup g : groups) if(g.getId().equals(p.getProductGroupId())) gName = g.getGroupName();
                String uName = "N/A";
                for (UnitMeasure u : uoms) if(u.getId().equals(p.getUomId())) uName = u.getSymbol();

                System.out.printf("%-6s | %-15s | $%-9.2f | %-15s | %-10s%n",
                        p.getId(), p.getName(), p.getPrice(), gName, uName);
            }
            System.out.println("==========================================================");

        } else if (c.equals("3")) {
            System.out.print("ID to Edit (or 0 to cancel): ");
            String id = s.nextLine();
            if (id.equals("0")) return;

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
            System.out.print("ID to Delete (or 0 to cancel): ");
            String delId = s.nextLine();
            if (!delId.equals("0")) {
                productService.deleteProduct(delId);
                System.out.println("✅ Deleted.");
            }
        }
    }

    // --- 2. Groups ---
    private void handleGroups(Scanner s) {
        System.out.println("\n--- Groups ---");
        System.out.println("1. Add | 2. List | 3. Edit | 4. Delete | 0. Back");
        String c = s.nextLine();

        if (c.equals("0")) {
            return;
        }
        else if (c.equals("1")) {
            String name = prompt(s, "Name: ");
            String desc = prompt(s, "Desc: ");
            System.out.println(productService.createProductGroup(name, desc));

        } else if (c.equals("2")) {
            System.out.println("\n=========== GROUPS ===========");
            System.out.printf("%-6s | %-15s | %-20s%n", "ID", "Name", "Description");
            System.out.println("---------------------------------------------");
            productService.getAllProductGroups().forEach(g ->
                    System.out.printf("%-6s | %-15s | %-20s%n", g.getId(), g.getGroupName(), g.getDescription()));
            System.out.println("==============================");

        } else if (c.equals("3")) {
            System.out.print("Enter Group ID to Edit (or 0 to cancel): ");
            String editId = s.nextLine();
            if (editId.equals("0")) return;

            if (productService.getProductGroupById(editId).isPresent()) {
                String newName = prompt(s, "New Name: ");
                String newDesc = prompt(s, "New Description: ");
                System.out.println(productService.updateProductGroup(editId, newName, newDesc));
            } else {
                System.out.println("❌ Group not found.");
            }

        } else if (c.equals("4")) {
            System.out.print("ID to Delete (or 0 to cancel): ");
            String delId = s.nextLine();
            if (!delId.equals("0")) {
                System.out.println(productService.deleteProductGroup(delId));
            }
        }
    }

    // --- 3. UOM ---
    private void handleUOM(Scanner s) {
        System.out.println("\n--- UOM ---");
        System.out.println("1. Add | 2. List | 3. Delete | 0. Back");
        String c = s.nextLine();

        if (c.equals("0")) {
            return;
        }
        else if (c.equals("1")) {
            String name = prompt(s, "Name: ");
            String sym = prompt(s, "Symbol: ");
            productService.addUOM(new UnitMeasure(null, name, sym));
            System.out.println("✅ UOM Added.");

        } else if (c.equals("2")) {
            System.out.println("\n=========== UOM LIST ===========");
            System.out.printf("%-6s | %-15s | %-10s%n", "ID", "Name", "Symbol");
            System.out.println("-----------------------------------");
            productService.getAllUOMs().forEach(u ->
                    System.out.printf("%-6s | %-15s | %-10s%n", u.getId(), u.getUnitName(), u.getSymbol()));
            System.out.println("================================");

        } else if (c.equals("3")) {
            System.out.print("ID to Delete (or 0 to cancel): ");
            String delId = s.nextLine();
            if (!delId.equals("0")) {
                productService.deleteUOM(delId);
                System.out.println("✅ Deleted.");
            }
        }
    }

    // --- 4. Warehouses ---
    private void handleWarehouses(Scanner s) {
        System.out.println("\n--- Warehouses ---");
        System.out.println("1. Add | 2. List | 3. Delete | 0. Back");
        String c = s.nextLine();

        if (c.equals("0")) return;

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
            System.out.print("Name to Delete (or 0 to cancel): ");
            String delId = s.nextLine();
            if (!delId.equals("0")) {
                productService.deleteWarehouse(delId);
                System.out.println("✅ Deleted.");
            }
        }
    }

    // --- 5. Stock ---
    private void handleStock(Scanner s) {
        System.out.println("\n--- Stock ---");
        System.out.println("1. Add | 2. List | 3. Export | 0. Back");
        String c = s.nextLine();

        if (c.equals("0")) return;

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
    // AUTO-INCREMENT HELPER (Only for Products)
    // ==========================================
    private String generateProductId() {
        int max = 0;
        for (Product p : productService.getAllProducts()) {
            try { int id = Integer.parseInt(p.getId()); if (id > max) max = id; } catch (Exception e) {}
        }
        return String.valueOf(max + 1);
    }
}