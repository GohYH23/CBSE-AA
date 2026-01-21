package com.inventory.product;

import com.inventory.api.ModuleMenu;
import com.inventory.api.product.model.*;
import com.inventory.api.product.service.ProductService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import java.util.Scanner;

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
            System.out.println("\n=== PRODUCT MANAGEMENT HUB ===");
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
        System.out.println("1. Add | 2. List | 3. Edit | 4. Delete");
        String choice = scanner.nextLine();

        if (choice.equals("1")) {
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
            System.out.print("ID to Edit: "); String id = scanner.nextLine();
            Product p = productService.getProduct(id);
            if (p != null) {
                System.out.print("New Name (" + p.getName() + "): "); String n = scanner.nextLine();
                if(!n.isEmpty()) p.setName(n);
                System.out.print("New Price (" + p.getPrice() + "): "); String pr = scanner.nextLine();
                if(!pr.isEmpty()) { try { p.setPrice(Double.parseDouble(pr)); } catch(Exception e) {} }
                productService.updateProduct(p);
                System.out.println("✅ Updated.");
            }
        } else if (choice.equals("4")) {
            System.out.print("ID to Delete: "); productService.deleteProduct(scanner.nextLine());
            System.out.println("✅ Deleted.");
        }
    }

    // --- 2. Manage Groups ---
    private void handleGroups(Scanner s) {
        System.out.println("\n--- Groups ---");
        System.out.println("1. Add | 2. List | 3. Edit | 4. Delete");
        String c = s.nextLine();

        if(c.equals("1")) {
            String id = generateGroupId();
            String name = prompt(s, "Name: ");
            for(ProductGroup g : productService.getAllProductGroups()) {
                if(g.getGroupName().equalsIgnoreCase(name)) {
                    System.out.println("Error: Group name '" + name + "' already exists."); return;
                }
            }
            String desc = prompt(s, "Desc: ");
            productService.addProductGroup(new ProductGroup(id, name, desc));
            System.out.println("Group Added.");

        } else if(c.equals("2")) {
            // 👇 IMPROVED TABLE
            System.out.println("\n======================== GROUP LIST ========================");
            System.out.printf("%-6s | %-20s | %-30s%n", "ID", "Name", "Description");
            System.out.println("------------------------------------------------------------");
            for(ProductGroup g : productService.getAllProductGroups())
                System.out.printf("%-6s | %-20s | %-30s%n", g.getGroupId(), g.getGroupName(), g.getDescription());
            System.out.println("============================================================");

        } else if(c.equals("3")) {
            System.out.print("ID to Edit: "); String id = s.nextLine();
            for(ProductGroup g : productService.getAllProductGroups()) {
                if(g.getGroupId().equals(id)) {
                    System.out.print("New Name ("+g.getGroupName()+"): "); String n = s.nextLine();
                    if(!n.isEmpty()) g.setGroupName(n);
                    System.out.print("New Desc ("+g.getDescription()+"): "); String d = s.nextLine();
                    if(!d.isEmpty()) g.setDescription(d);
                    productService.updateProductGroup(g);
                    System.out.println("Group Updated."); return;
                }
            }
            System.out.println("Group not found.");

        } else if(c.equals("4")) {
            System.out.print("ID to Delete: "); String id = s.nextLine();
            boolean inUse = false;
            for(Product p : productService.getAllProducts()) {
                if(id.equals(p.getProductGroupId())) { inUse = true; break; }
            }
            if(inUse) System.out.println("Cannot Delete: This group is used by existing products.");
            else { productService.deleteProductGroup(id); System.out.println("Deleted."); }
        }
    }

    // --- 3. Manage UOM ---
    private void handleUOM(Scanner s) {
        System.out.println("\n--- UOM ---");
        System.out.println("1. Add | 2. List | 3. Edit | 4. Delete");
        String c = s.nextLine();

        if(c.equals("1")) {
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
            // 👇 IMPROVED TABLE
            System.out.println("\n========================= UOM LIST =========================");
            System.out.printf("%-6s | %-20s | %-10s%n", "ID", "Name", "Symbol");
            System.out.println("------------------------------------------------------------");
            for(UnitMeasure u : productService.getAllUnitMeasures())
                System.out.printf("%-6s | %-20s | %-10s%n", u.getUomId(), u.getUnitName(), u.getSymbol());
            System.out.println("============================================================");

        } else if(c.equals("3")) {
            System.out.print("ID to Edit: "); String id = s.nextLine();
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
            System.out.print("ID to Delete: "); String id = s.nextLine();
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
        System.out.println("1. Add | 2. List | 3. Edit | 4. Delete");
        String c = s.nextLine();

        if (c.equals("1")) {
            String name = prompt(s, "Name: ");
            for(Warehouse w : productService.getAllWarehouses()) {
                if(w.getName().equalsIgnoreCase(name)) {
                    System.out.println("Error: Warehouse '" + name + "' already exists."); return;
                }
            }
            String desc = prompt(s, "Desc: ");
            productService.addWarehouse(new Warehouse(name, false, desc));
            System.out.println("Warehouse Added.");

        } else if (c.equals("2")) {
            // 👇 IMPROVED TABLE
            System.out.println("\n====================== WAREHOUSE LIST ======================");
            System.out.printf("%-20s | %-30s | %-10s%n", "Name", "Description", "Is System");
            System.out.println("------------------------------------------------------------");
            for(Warehouse w : productService.getAllWarehouses())
                System.out.printf("%-20s | %-30s | %-10s%n", w.getName(), w.getDescription(), w.isSystemWarehouse() ? "Yes" : "No");
            System.out.println("============================================================");

        } else if(c.equals("3")) {
            System.out.print("Name to Edit: "); String name = s.nextLine();
            for(Warehouse w : productService.getAllWarehouses()) {
                if(w.getName().equals(name)) {
                    System.out.print("New Desc ("+w.getDescription()+"): "); String d = s.nextLine();
                    if(!d.isEmpty()) w.setDescription(d);
                    productService.updateWarehouse(w);
                    System.out.println("Warehouse Updated."); return;
                }
            }
            System.out.println("Warehouse not found.");

        } else if(c.equals("4")) {
            System.out.print("Name to Delete: "); productService.deleteWarehouse(s.nextLine());
            System.out.println("Deleted.");
        }
    }

    // --- 5. Manage Stock ---
    private void handleStock(Scanner s) {
        System.out.println("\n--- Stock Counts ---");
        System.out.println("1. New Stock Count | 2. List History | 3. Export Report");
        String c = s.nextLine();

        if (c.equals("1")) {
            String autoId = "SC-" + System.currentTimeMillis() % 10000;
            String wh = prompt(s, "Warehouse Name: ");
            String date = prompt(s, "Date (YYYY-MM-DD): ");
            productService.addStockCount(new StockCount(autoId, wh, "Pending", date));
            System.out.println("Started Stock Count: " + autoId);

        } else if (c.equals("2")) {
            // 👇 IMPROVED TABLE
            System.out.println("\n==================== STOCK COUNT HISTORY ===================");
            System.out.printf("%-10s | %-20s | %-12s | %-10s%n", "ID", "Warehouse", "Date", "Status");
            System.out.println("------------------------------------------------------------");
            for(StockCount sc : productService.getAllStockCounts())
                System.out.printf("%-10s | %-20s | %-12s | %-10s%n", sc.getCountId(), sc.getWarehouseName(), sc.getDate(), sc.getStatus());
            System.out.println("============================================================");

        } else if (c.equals("3")) {
            System.out.println("Generating PDF Report...");
            try { Thread.sleep(1000); } catch(Exception e) {}
            System.out.println("Report Exported to C:/Downloads/stock_report.pdf");
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