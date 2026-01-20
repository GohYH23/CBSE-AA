package com.inventory.product;

import com.inventory.api.ModuleMenu;
import com.inventory.api.product.*;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import java.util.Scanner;
import java.util.List;

@Component(service = ModuleMenu.class, immediate = true)
public class ProductMenu implements ModuleMenu {

    @Reference
    private ProductService productService;

    @Override
    public String getModuleName() { return "Product Management Module"; }

    @Override
    public void start() {
        showMainMenu(new Scanner(System.in));
    }

    private void showMainMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n=== PRODUCT MANAGEMENT HUB ===");
            System.out.println("1. Manage Products");
            System.out.println("2. Manage Product Groups");
            System.out.println("3. Manage Unit Measures");
            System.out.println("4. Manage Warehouses");
            System.out.println("5. Manage Stock Counts");
            System.out.println("6. Back to Main System");
            System.out.print("Select Category: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1": handleProducts(scanner); break;
                case "2": handleGroups(scanner); break;
                case "3": handleUOM(scanner); break;
                case "4": handleWarehouses(scanner); break;
                case "5": handleStock(scanner); break;
                case "6": return;
                default: System.out.println("Invalid option.");
            }
        }
    }

    // --- 1. Products (UC-09) with Smart Table & Auto-ID ---
    private void handleProducts(Scanner scanner) {
        System.out.println("\n--- Products ---");
        System.out.println("1. Add Product");
        System.out.println("2. List Products");
        System.out.println("3. Edit Product");
        System.out.println("4. Delete Product");
        System.out.print("Choice: ");
        String choice = scanner.nextLine();

        if (choice.equals("1")) {
            // Auto-Generate ID
            String id = generateProductId();
            System.out.println("Generating New ID: " + id);

            System.out.print("Name: "); String name = scanner.nextLine();
            System.out.print("Price: "); double price = Double.parseDouble(scanner.nextLine());

            // List Groups for selection
            System.out.println("\nSelect Group:");
            for(ProductGroup g : productService.getAllProductGroups())
                System.out.printf("- %s (%s)%n", g.getGroupId(), g.getGroupName());
            System.out.print("Enter Group ID: "); String gid = scanner.nextLine();

            // List UOMs for selection
            System.out.println("\nSelect UOM:");
            for(UnitMeasure u : productService.getAllUnitMeasures())
                System.out.printf("- %s (%s)%n", u.getUomId(), u.getSymbol());
            System.out.print("Enter UOM ID: "); String uid = scanner.nextLine();

            Product p = new Product();
            p.setId(id); p.setName(name); p.setPrice(price);
            p.setProductGroupId(gid); p.setUomId(uid);

            productService.addProduct(p);
            System.out.println("Product Added.");

        } else if (choice.equals("2")) {
            // === SMART PRODUCT TABLE ===
            System.out.println("\n====================== PRODUCT LIST ======================");
            System.out.printf("%-5s | %-15s | %-10s | %-15s | %-10s%n", "ID", "Name", "Price", "Group", "UOM");
            System.out.println("----------------------------------------------------------");

            var groups = productService.getAllProductGroups();
            var uoms = productService.getAllUnitMeasures();
            var products = productService.getAllProducts();

            for (Product p : products) {
                // Find Group Name
                String gName = "N/A";
                if (p.getProductGroupId() != null) {
                    for (ProductGroup g : groups) {
                        if (g.getGroupId().equals(p.getProductGroupId())) { gName = g.getGroupName(); break; }
                    }
                }
                // Find UOM Symbol
                String uName = "N/A";
                if (p.getUomId() != null) {
                    for (UnitMeasure u : uoms) {
                        if (u.getUomId().equals(p.getUomId())) { uName = u.getSymbol(); break; }
                    }
                }
                // Print Row
                System.out.printf("%-5s | %-15s | $%-9.2f | %-15s | %-10s%n",
                        p.getId(), p.getName(), p.getPrice(), gName, uName);
            }
            System.out.println("==========================================================");

        } else if (choice.equals("3")) {
            System.out.print("Enter ID to Edit: "); String id = scanner.nextLine();
            Product p = productService.getProduct(id);
            if (p != null) {
                System.out.print("New Name (" + p.getName() + "): "); String n = scanner.nextLine();
                if(!n.isEmpty()) p.setName(n);
                System.out.print("New Price (" + p.getPrice() + "): "); String pr = scanner.nextLine();
                if(!pr.isEmpty()) p.setPrice(Double.parseDouble(pr));
                productService.updateProduct(p);
                System.out.println("Updated.");
            }
        } else if (choice.equals("4")) {
            System.out.print("Enter ID to Delete: "); productService.deleteProduct(scanner.nextLine());
            System.out.println("Deleted.");
        }
    }

    // --- 2. Product Groups (UC-10) with Auto-ID ---
    private void handleGroups(Scanner s) {
        System.out.println("\n--- Product Groups ---");
        System.out.println("1. Add | 2. List | 3. Delete");
        String c = s.nextLine();

        if(c.equals("1")) {
            String id = generateGroupId();
            System.out.println("Generating Group ID: " + id);

            System.out.print("Name: "); String name = s.nextLine();
            System.out.print("Desc: "); String desc = s.nextLine();
            productService.addProductGroup(new ProductGroup(id, name, desc));
            System.out.println("Group Added.");
        } else if(c.equals("2")) {
            System.out.println("\n=========== GROUPS ===========");
            System.out.printf("%-5s | %-15s | %-20s%n", "ID", "Name", "Description");
            System.out.println("---------------------------------------------");
            for(ProductGroup g : productService.getAllProductGroups())
                System.out.printf("%-5s | %-15s | %-20s%n", g.getGroupId(), g.getGroupName(), g.getDescription());
            System.out.println("==============================");
        } else if(c.equals("3")) {
            System.out.print("ID to Delete: "); productService.deleteProductGroup(s.nextLine());
            System.out.println("Deleted.");
        }
    }

    // --- 3. UOM (UC-11) with Auto-ID ---
    private void handleUOM(Scanner s) {
        System.out.println("\n--- Unit of Measures ---");
        System.out.println("1. Add | 2. List | 3. Delete");
        String c = s.nextLine();

        if(c.equals("1")) {
            String id = generateUomId();
            System.out.println("Generating UOM ID: " + id);

            System.out.print("Name: "); String name = s.nextLine();
            System.out.print("Symbol: "); String sym = s.nextLine();
            productService.addUnitMeasure(new UnitMeasure(id, name, sym));
            System.out.println("UOM Added.");
        } else if(c.equals("2")) {
            System.out.println("\n=========== UOM LIST ===========");
            System.out.printf("%-5s | %-15s | %-10s%n", "ID", "Name", "Symbol");
            System.out.println("-----------------------------------");
            for(UnitMeasure u : productService.getAllUnitMeasures())
                System.out.printf("%-5s | %-15s | %-10s%n", u.getUomId(), u.getUnitName(), u.getSymbol());
            System.out.println("================================");
        } else if(c.equals("3")) {
            System.out.print("ID to Delete: "); productService.deleteUnitMeasure(s.nextLine());
            System.out.println("Deleted.");
        }
    }

    // --- 4. Warehouses (UC-13) ---
    private void handleWarehouses(Scanner s) {
        System.out.println("\n--- Warehouses ---");
        System.out.println("1. Add | 2. List | 3. Delete");
        String c = s.nextLine();

        if (c.equals("1")) {
            System.out.print("Name: "); String name = s.nextLine();
            System.out.print("Description: "); String desc = s.nextLine();
            productService.addWarehouse(new Warehouse(name, false, desc));
            System.out.println("Warehouse Added.");
        } else if (c.equals("2")) {
            System.out.println("\n=========== WAREHOUSES ===========");
            System.out.printf("%-15s | %-10s | %-20s%n", "Name", "Type", "Description");
            System.out.println("---------------------------------------------------");
            for (Warehouse w : productService.getAllWarehouses()) {
                String type = w.isSystemWarehouse() ? "System" : "Local";
                System.out.printf("%-15s | %-10s | %-20s%n", w.getName(), type, w.getDescription());
            }
            System.out.println("==================================");
        } else if(c.equals("3")) {
            System.out.print("Name to Delete: "); productService.deleteWarehouse(s.nextLine());
            System.out.println("Deleted.");
        }
    }

    // --- 5. Stock Count (UC-12) ---
    private void handleStock(Scanner s) {
        System.out.println("\n--- Stock Counts ---");
        System.out.println("1. New Stock Count | 2. List History");
        String c = s.nextLine();

        if (c.equals("1")) {
            String autoId = "SC-" + System.currentTimeMillis() % 10000;
            System.out.print("Warehouse Name: "); String wh = s.nextLine();
            System.out.print("Date (YYYY-MM-DD): "); String date = s.nextLine();
            productService.addStockCount(new StockCount(autoId, wh, "Pending", date));
            System.out.println("Started Stock Count: " + autoId);
        } else {
            System.out.println("\n=========== STOCK HISTORY ===========");
            System.out.printf("%-10s | %-15s | %-10s | %-12s%n", "ID", "Warehouse", "Status", "Date");
            System.out.println("-----------------------------------------------------");
            for(StockCount sc : productService.getAllStockCounts())
                System.out.printf("%-10s | %-15s | %-10s | %-12s%n",
                        sc.getCountId(), sc.getWarehouseName(), sc.getStatus(), sc.getDate());
            System.out.println("=====================================");
        }
    }

    // --- AUTO ID GENERATOR HELPERS ---
    private String generateProductId() {
        int max = 0;
        for (Product p : productService.getAllProducts()) {
            try { int id = Integer.parseInt(p.getId()); if (id > max) max = id; }
            catch (Exception e) {}
        }
        return String.valueOf(max + 1);
    }

    private String generateGroupId() {
        int max = 0;
        for (ProductGroup g : productService.getAllProductGroups()) {
            try { int id = Integer.parseInt(g.getGroupId()); if (id > max) max = id; }
            catch (Exception e) {}
        }
        return String.valueOf(max + 1);
    }

    private String generateUomId() {
        int max = 0;
        for (UnitMeasure u : productService.getAllUnitMeasures()) {
            try { int id = Integer.parseInt(u.getUomId()); if (id > max) max = id; }
            catch (Exception e) {}
        }
        return String.valueOf(max + 1);
    }
}