package com.inventory.product;

import com.inventory.api.ModuleMenu;
import com.inventory.api.product.*;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Scanner;

@Component(service = ModuleMenu.class, immediate = true)
public class ProductMenu implements ModuleMenu {

    @Reference
    private ProductService productService;

    @Override
    public String getModuleName() {
        return "Product Management Module";
    }

    @Override
    public void start() {
        Scanner scanner = new Scanner(System.in);
        showMainMenu(scanner);
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

    // --- 1. Products (UC-09) ---
    private void handleProducts(Scanner scanner) {
        System.out.println("\n--- Products ---");
        System.out.println("1. Add Product");
        System.out.println("2. List Products");
        System.out.print("Choice: ");
        if (scanner.nextLine().equals("1")) {
            System.out.print("ID: "); String id = scanner.nextLine();
            System.out.print("Name: "); String name = scanner.nextLine();
            System.out.print("Price: "); double price = Double.parseDouble(scanner.nextLine());
            Product p = new Product(); p.setId(id); p.setName(name); p.setPrice(price);
            productService.addProduct(p);
            System.out.println("✅ Product Added.");
        } else {
            for (Product p : productService.getAllProducts()) {
                System.out.println(p.getId() + " | " + p.getName() + " | $" + p.getPrice());
            }
        }
    }

    // --- 2. Product Groups (UC-10) ---
    private void handleGroups(Scanner scanner) {
        System.out.println("\n--- Product Groups ---");
        System.out.println("1. Add Group");
        System.out.println("2. List Groups");
        System.out.print("Choice: ");
        if (scanner.nextLine().equals("1")) {
            System.out.print("ID: "); String id = scanner.nextLine();
            System.out.print("Name: "); String name = scanner.nextLine();
            System.out.print("Desc: "); String desc = scanner.nextLine();
            productService.addProductGroup(new ProductGroup(id, name, desc));
            System.out.println("✅ Group Added.");
        } else {
            for (ProductGroup g : productService.getAllProductGroups()) {
                System.out.println(g.getGroupId() + " | " + g.getGroupName());
            }
        }
    }

    // --- 3. Unit of Measure (UC-11) ---
    private void handleUOM(Scanner scanner) {
        System.out.println("\n--- Unit of Measures ---");
        System.out.println("1. Add UOM");
        System.out.println("2. List UOMs");
        System.out.print("Choice: ");
        if (scanner.nextLine().equals("1")) {
            System.out.print("ID: "); String id = scanner.nextLine();
            System.out.print("Name (e.g. Kilogram): "); String name = scanner.nextLine();
            System.out.print("Symbol (e.g. kg): "); String sym = scanner.nextLine();
            productService.addUnitMeasure(new UnitMeasure(id, name, sym));
            System.out.println("✅ UOM Added.");
        } else {
            for (UnitMeasure u : productService.getAllUnitMeasures()) {
                System.out.println(u.getUnitName() + " (" + u.getSymbol() + ")");
            }
        }
    }

    // --- 4. Warehouses (UC-13) ---
    private void handleWarehouses(Scanner scanner) {
        System.out.println("\n--- Warehouses ---");
        System.out.println("1. Add Warehouse");
        System.out.println("2. List Warehouses");
        System.out.print("Choice: ");
        if (scanner.nextLine().equals("1")) {
            System.out.print("Name: "); String name = scanner.nextLine();
            System.out.print("Description: "); String desc = scanner.nextLine();
            productService.addWarehouse(new Warehouse(name, false, desc));
            System.out.println("✅ Warehouse Added.");
        } else {
            for (Warehouse w : productService.getAllWarehouses()) {
                System.out.println("🏠 " + w.getName());
            }
        }
    }

    // --- 5. Stock Count (UC-12) ---
    private void handleStock(Scanner scanner) {
        System.out.println("\n--- Stock Counts ---");
        System.out.println("1. New Stock Count");
        System.out.println("2. List History");
        System.out.print("Choice: ");
        if (scanner.nextLine().equals("1")) {
            System.out.print("Count ID: "); String id = scanner.nextLine();
            System.out.print("Warehouse Name: "); String wh = scanner.nextLine();
            System.out.print("Date: "); String date = scanner.nextLine();
            productService.addStockCount(new StockCount(id, wh, "Pending", date));
            System.out.println("✅ Stock Count Started.");
        } else {
            for (StockCount s : productService.getAllStockCounts()) {
                System.out.println(s.getCountId() + " | " + s.getWarehouseName() + " | " + s.getDate());
            }
        }
    }
}