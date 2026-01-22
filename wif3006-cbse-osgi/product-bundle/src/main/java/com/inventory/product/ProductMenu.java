package com.inventory.product;

import com.inventory.api.ModuleMenu;
import com.inventory.api.product.model.*;
import com.inventory.api.product.service.ProductService;
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
    public void start() { showMainMenu(new Scanner(System.in)); }

    private void showMainMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n=== PRODUCT MANAGEMENT HUB (OSGi) ===");
            System.out.println("1. Manage Products");
            System.out.println("2. Manage Product Groups");
            System.out.println("3. Manage Unit Measures");
            System.out.println("4. Manage Warehouses");
            System.out.println("5. Manage Stock Counts");
            System.out.println("0. Back");
            System.out.print("Select: ");
            String choice = scanner.nextLine();
            switch (choice) {
                case "1": handleProducts(scanner); break;
                case "2": handleGroups(scanner); break;
                case "3": handleUOM(scanner); break;
                case "4": handleWarehouses(scanner); break;
                case "5": handleStock(scanner); break;
                case "0": return;
                default: System.out.println("Invalid option.");
            }
        }
    }

    // --- HELPER: Validation ---
    private String prompt(Scanner s, String label) {
        while (true) {
            System.out.print(label);
            String input = s.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.println("Error: Field cannot be empty.");
        }
    }

    // --- 1. Manage Products ---
    private void handleProducts(Scanner scanner) {
        System.out.println("\n--- Products ---");
        System.out.println("1. Add | 2. List | 3. Edit | 4. Delete | 0. Back");
        String choice = scanner.nextLine();

        if (choice.equals("0")) {
            return;
        } else if (choice.equals("1")) {
            String id = generateProductId();
            System.out.println("New ID: " + id);
            String name = prompt(scanner, "Name: ");

            for(Product p : productService.getAllProducts()) {
                if(p.getName().equalsIgnoreCase(name)) {
                    System.out.println("❌ Error: Product '" + name + "' already exists.");
                    return;
                }
            }

            double price = 0;
            try { price = Double.parseDouble(prompt(scanner, "Price: ")); }
            catch (NumberFormatException e) { System.out.println("⚠️ Invalid price. Setting to 0."); }

            System.out.println("\nSelect Group:");
            productService.getAllProductGroups().forEach(g -> System.out.println("- " + g.getGroupId() + ": " + g.getGroupName()));
            System.out.print("Group ID: "); String gid = scanner.nextLine();

            System.out.println("\nSelect UOM:");
            productService.getAllUnitMeasures().forEach(u -> System.out.println("- " + u.getUomId() + ": " + u.getSymbol()));
            System.out.print("UOM ID: "); String uid = scanner.nextLine();

            Product p = new Product();
            p.setId(id); p.setName(name); p.setPrice(price);
            p.setProductGroupId(gid); p.setUomId(uid);
            productService.addProduct(p);
            System.out.println("✅ Product Added.");

        } else if (choice.equals("2")) {
            System.out.println("\n====================== PRODUCT LIST ======================");
            System.out.printf("%-5s | %-15s | %-10s | %-15s | %-10s%n", "ID", "Name", "Price", "Group", "UOM");
            System.out.println("----------------------------------------------------------");

            var groups = productService.getAllProductGroups();
            var uoms = productService.getAllUnitMeasures();

            for (Product p : productService.getAllProducts()) {
                String gName = "N/A";
                for (ProductGroup g : groups) if (g.getGroupId().equals(p.getProductGroupId())) gName = g.getGroupName();
                String uName = "N/A";
                for (UnitMeasure u : uoms) if (u.getUomId().equals(p.getUomId())) uName = u.getSymbol();

                System.out.printf("%-5s | %-15s | $%-9.2f | %-15s | %-10s%n",
                        p.getId(), p.getName(), p.getPrice(), gName, uName);
            }
            System.out.println("==========================================================");

        } else if (choice.equals("3")) {
            System.out.print("ID to Edit (0 to cancel): ");
            String id = scanner.nextLine();
            if(id.equals("0")) return;

            Product p = productService.getProduct(id);
            if (p != null) {
                System.out.print("New Name (" + p.getName() + "): "); String n = scanner.nextLine();
                if(!n.isEmpty()) p.setName(n);
                System.out.print("New Price (" + p.getPrice() + "): "); String pr = scanner.nextLine();
                if(!pr.isEmpty()) { try { p.setPrice(Double.parseDouble(pr)); } catch(Exception e) {} }
                productService.updateProduct(p);
                System.out.println("✅ Updated.");
            } else {
                System.out.println("❌ Not found.");
            }
        } else if (choice.equals("4")) {
            System.out.print("ID to Delete (0 to cancel): ");
            String id = scanner.nextLine();
            if(!id.equals("0")) {
                productService.deleteProduct(id);
                System.out.println("✅ Deleted.");
            }
        }
    }

    // --- 2. Manage Groups ---
    private void handleGroups(Scanner s) {
        System.out.println("\n--- Groups ---");
        System.out.println("1. Add | 2. List | 3. Edit | 4. Delete | 0. Back");
        String c = s.nextLine();

        if (c.equals("0")) {
            return;
        } else if(c.equals("1")) {
            String id = generateGroupId();
            String name = prompt(s, "Name: ");
            for(ProductGroup g : productService.getAllProductGroups()) {
                if(g.getGroupName().equalsIgnoreCase(name)) {
                    System.out.println("Error: Group name '" + name + "' already exists."); return;
                }
            }
            String desc = prompt(s, "Desc: ");
            productService.addProductGroup(new ProductGroup(id, name, desc));
            System.out.println("✅ Group Added.");

        } else if(c.equals("2")) {
            System.out.println("\n======================== GROUP LIST ========================");
            System.out.printf("%-6s | %-20s | %-30s%n", "ID", "Name", "Description");
            System.out.println("------------------------------------------------------------");
            for(ProductGroup g : productService.getAllProductGroups())
                System.out.printf("%-6s | %-20s | %-30s%n", g.getGroupId(), g.getGroupName(), g.getDescription());
            System.out.println("============================================================");

        } else if(c.equals("3")) {
            System.out.print("ID to Edit (0 to cancel): "); String id = s.nextLine();
            if(id.equals("0")) return;

            for(ProductGroup g : productService.getAllProductGroups()) {
                if(g.getGroupId().equals(id)) {
                    System.out.print("New Name ("+g.getGroupName()+"): "); String n = s.nextLine();
                    if(!n.isEmpty()) g.setGroupName(n);
                    System.out.print("New Desc ("+g.getDescription()+"): "); String d = s.nextLine();
                    if(!d.isEmpty()) g.setDescription(d);
                    productService.updateProductGroup(g);
                    System.out.println("✅ Group Updated."); return;
                }
            }
            System.out.println("❌ Group not found.");

        } else if(c.equals("4")) {
            System.out.print("ID to Delete (0 to cancel): "); String id = s.nextLine();
            if(id.equals("0")) return;

            boolean inUse = false;
            for(Product p : productService.getAllProducts()) {
                if(id.equals(p.getProductGroupId())) { inUse = true; break; }
            }
            if(inUse) System.out.println("❌ Cannot Delete: This group is used by existing products.");
            else { productService.deleteProductGroup(id); System.out.println("✅ Deleted."); }
        }
    }

    // --- 3. Manage UOM ---
    private void handleUOM(Scanner s) {
        System.out.println("\n--- UOM ---");
        System.out.println("1. Add | 2. List | 3. Edit | 4. Delete | 0. Back");
        String c = s.nextLine();

        if (c.equals("0")) {
            return;
        } else if(c.equals("1")) {
            String id = generateUomId();
            String name = prompt(s, "Name: ");
            for(UnitMeasure u : productService.getAllUnitMeasures()) {
                if(u.getUnitName().equalsIgnoreCase(name)) {
                    System.out.println("❌ Error: UOM '" + name + "' already exists."); return;
                }
            }
            String sym = prompt(s, "Symbol: ");
            productService.addUnitMeasure(new UnitMeasure(id, name, sym));
            System.out.println("✅ UOM Added.");

        } else if(c.equals("2")) {
            System.out.println("\n========================= UOM LIST =========================");
            System.out.printf("%-6s | %-20s | %-10s%n", "ID", "Name", "Symbol");
            System.out.println("------------------------------------------------------------");
            for(UnitMeasure u : productService.getAllUnitMeasures())
                System.out.printf("%-6s | %-20s | %-10s%n", u.getUomId(), u.getUnitName(), u.getSymbol());
            System.out.println("============================================================");

        } else if(c.equals("3")) {
            System.out.print("ID to Edit (0 to cancel): "); String id = s.nextLine();
            if(id.equals("0")) return;

            for(UnitMeasure u : productService.getAllUnitMeasures()) {
                if(u.getUomId().equals(id)) {
                    System.out.print("New Name ("+u.getUnitName()+"): "); String n = s.nextLine();
                    if(!n.isEmpty()) u.setUnitName(n);
                    System.out.print("New Symbol ("+u.getSymbol()+"): "); String sy = s.nextLine();
                    if(!sy.isEmpty()) u.setSymbol(sy);
                    productService.updateUnitMeasure(u);
                    System.out.println("✅ UOM Updated."); return;
                }
            }
            System.out.println("❌ UOM not found.");
        } else if(c.equals("4")) {
            System.out.print("ID to Delete (0 to cancel): "); String id = s.nextLine();
            if(id.equals("0")) return;

            boolean inUse = false;
            for(Product p : productService.getAllProducts()) {
                if(id.equals(p.getUomId())) { inUse = true; break; }
            }
            if(inUse) System.out.println("❌ Cannot Delete: This UOM is used by existing products.");
            else { productService.deleteUnitMeasure(id); System.out.println("✅ Deleted."); }
        }
    }

    // --- 4. Manage Warehouses ---
    private void handleWarehouses(Scanner s) {
        System.out.println("\n--- Warehouses ---");
        System.out.println("1. Add | 2. List | 3. Edit | 4. Delete | 0. Back");
        String c = s.nextLine();

        if (c.equals("0")) {
            return;
        } else if (c.equals("1")) {
            String name = prompt(s, "Name: ");
            for(Warehouse w : productService.getAllWarehouses()) {
                if(w.getName().equalsIgnoreCase(name)) {
                    System.out.println("Error: Warehouse '" + name + "' already exists."); return;
                }
            }
            String desc = prompt(s, "Desc: ");
            productService.addWarehouse(new Warehouse(name, false, desc));
            System.out.println("✅ Warehouse Added.");

        } else if (c.equals("2")) {
            System.out.println("\n====================== WAREHOUSE LIST ======================");
            System.out.printf("%-20s | %-30s | %-10s%n", "Name", "Description", "Is System");
            System.out.println("------------------------------------------------------------");
            for(Warehouse w : productService.getAllWarehouses())
                System.out.printf("%-20s | %-30s | %-10s%n", w.getName(), w.getDescription(), w.isSystemWarehouse() ? "Yes" : "No");
            System.out.println("============================================================");

        } else if(c.equals("3")) {
            System.out.print("Name to Edit (0 to cancel): "); String name = s.nextLine();
            if(name.equals("0")) return;

            for(Warehouse w : productService.getAllWarehouses()) {
                if(w.getName().equals(name)) {
                    System.out.print("New Desc ("+w.getDescription()+"): "); String d = s.nextLine();
                    if(!d.isEmpty()) w.setDescription(d);
                    productService.updateWarehouse(w);
                    System.out.println("✅ Warehouse Updated."); return;
                }
            }
            System.out.println("❌ Warehouse not found.");

        } else if(c.equals("4")) {
            System.out.print("Name to Delete (0 to cancel): "); String name = s.nextLine();
            if(!name.equals("0")) {
                productService.deleteWarehouse(name);
                System.out.println("✅ Deleted.");
            }
        }
    }

    // --- 5. Manage Stock (UPDATED TO MATCH SPRING BOOT) ---
    private void handleStock(Scanner s) {
        System.out.println("\n--- Stock Counts ---");
        // Updated Menu Options
        System.out.println("1. New Stock Count | 2. List History | 3. Export Report | 4. Complete Count | 0. Back");
        String c = s.nextLine();

        if (c.equals("0")) {
            return;
        } else if (c.equals("1")) {
            // --- SMART VALIDATION ---
            List<Warehouse> warehouses = productService.getAllWarehouses();
            if (warehouses.isEmpty()) {
                System.out.println("❌ Error: No warehouses exist. Create one in Menu 4 first.");
                return;
            }

            System.out.println("Available Warehouses:");
            for(Warehouse w : warehouses) System.out.println("- " + w.getName());

            String wh = "";
            while(true) {
                System.out.print("Enter Warehouse Name: ");
                wh = s.nextLine().trim();
                String finalWh = wh;
                boolean exists = false;
                for(Warehouse w : warehouses) {
                    if(w.getName().equalsIgnoreCase(finalWh)) { exists = true; break; }
                }
                if(exists) break;
                System.out.println("❌ Invalid Warehouse. Try again.");
            }
            // --- END SMART VALIDATION ---

            String autoId = "SC-" + System.currentTimeMillis() % 10000;
            String date = prompt(s, "Date (YYYY-MM-DD): ");
            productService.addStockCount(new StockCount(autoId, wh, "Pending", date));
            System.out.println("✅ Started Stock Count: " + autoId);

        } else if (c.equals("2")) {
            System.out.println("\n==================== STOCK COUNT HISTORY ===================");
            System.out.printf("%-10s | %-20s | %-12s | %-10s%n", "ID", "Warehouse", "Date", "Status");
            System.out.println("------------------------------------------------------------");
            for(StockCount sc : productService.getAllStockCounts())
                System.out.printf("%-10s | %-20s | %-12s | %-10s%n", sc.getCountId(), sc.getWarehouseName(), sc.getDate(), sc.getStatus());
            System.out.println("============================================================");

        } else if (c.equals("3")) {
            // --- MOCK EXPORT ---
            System.out.print("Enter Stock Count ID to Export: ");
            String id = s.nextLine();
            boolean found = false;
            for(StockCount sc : productService.getAllStockCounts()) {
                if(sc.getCountId().equals(id)) { found = true; break; }
            }

            if(found) {
                System.out.println("Generating PDF Report...");
                try { Thread.sleep(800); } catch(Exception e) {}
                System.out.println("✅ Report Exported: C:/Downloads/StockReport_" + id + ".pdf");
            } else {
                System.out.println("❌ Error: Stock Count ID not found.");
            }

        } else if (c.equals("4")) {
            // --- COMPLETE COUNT ---
            System.out.print("Enter Stock Count ID to Complete: ");
            String id = s.nextLine();
            // Calls the new method in ProductService interface
            System.out.println(productService.completeStockCount(id));
        }
    }

    // --- ID GENERATORS ---
    private String generateProductId() {
        int max = 0;
        for (Product p : productService.getAllProducts()) {
            try { int id = Integer.parseInt(p.getId()); if (id > max) max = id; } catch (Exception e) {}
        }
        return String.valueOf(max + 1);
    }
    private String generateGroupId() {
        int max = 0;
        for (ProductGroup g : productService.getAllProductGroups()) {
            try { int id = Integer.parseInt(g.getGroupId()); if (id > max) max = id; } catch (Exception e) {}
        }
        return String.valueOf(max + 1);
    }
    private String generateUomId() {
        int max = 0;
        for (UnitMeasure u : productService.getAllUnitMeasures()) {
            try { int id = Integer.parseInt(u.getUomId()); if (id > max) max = id; } catch (Exception e) {}
        }
        return String.valueOf(max + 1);
    }
}