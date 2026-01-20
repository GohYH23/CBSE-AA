package com.inventory.product;

import com.inventory.api.product.Product;
import com.inventory.api.product.ProductService;
import java.util.Scanner;

public class ProductMenu {
    private final ProductService productService;
    private final Scanner scanner;

    public ProductMenu(ProductService productService, Scanner scanner) {
        this.productService = productService;
        this.scanner = scanner;
    }

    public void showMenu() {
        while (true) {
            System.out.println("\n=== PRODUCT MANAGEMENT ===");
            System.out.println("1. List All Products");
            System.out.println("2. Add New Product");
            System.out.println("3. Back to Main Menu");
            System.out.print("Select option: ");

            String input = scanner.nextLine();

            switch (input) {
                case "1":
                    listProducts();
                    break;
                case "2":
                    addProduct();
                    break;
                case "3":
                    return; // Go back
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void listProducts() {
        System.out.println("\n--- Product List ---");
        for (Product p : productService.getAllProducts()) {
            System.out.println(p.getId() + " | " + p.getName() + " | $" + p.getPrice());
        }
    }

    private void addProduct() {
        System.out.println("\n--- Add Product ---");

        System.out.print("Enter ID: ");
        String id = scanner.nextLine();

        System.out.print("Enter Name: ");
        String name = scanner.nextLine();

        System.out.print("Enter Price: ");
        double price = Double.parseDouble(scanner.nextLine());

        // Create and Save
        Product newProduct = new Product();
        newProduct.setId(id);
        newProduct.setName(name);
        newProduct.setPrice(price);

        productService.addProduct(newProduct);
        System.out.println("✅ Product Saved!");
    }
}