package com.inventory.product;

import com.inventory.api.product.Product;
import com.inventory.api.product.ProductService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component(service = ProductService.class, immediate = true)
public class ProductServiceImpl implements ProductService {

    private static final String DATA_DIR = "data";
    private static final String DATA_FILE = "products.dat";

    private final List<Product> productList = new ArrayList<>();
    // Locks prevent data corruption if two people access it at the same time
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private File dataFile;

    @Activate
    public void activate() {
        System.out.println("✅ Product Module Started.");

        // 1. Setup Data Storage
        try {
            Path dataDirPath = Paths.get(DATA_DIR);
            if (!Files.exists(dataDirPath)) {
                Files.createDirectories(dataDirPath);
            }
            dataFile = new File(DATA_DIR, DATA_FILE);

            // 2. Load existing data
            loadProducts();
            System.out.println("   Loaded " + productList.size() + " products from storage.");

        } catch (Exception e) {
            System.err.println("   ⚠️ Warning: Could not initialize data storage: " + e.getMessage());
        }
    }

    @Deactivate
    public void deactivate() {
        System.out.println("❌ Stopping Product Module...");
        saveProducts(); // Save before quitting
    }

    @Override
    public void addProduct(Product product) {
        lock.writeLock().lock(); // Lock for writing
        try {
            productList.add(product);
            saveProducts(); // Auto-save
            System.out.println("OSGi: Product added - " + product.getName());
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public List<Product> getAllProducts() {
        lock.readLock().lock(); // Lock for reading
        try {
            return new ArrayList<>(productList); // Return a copy to be safe
        } finally {
            lock.readLock().unlock();
        }
    }

    // --- The Menu Connection (Crucial for the Launcher) ---
    // Make sure your Interface (ProductService) has: void showMenu(Scanner scanner);
    @Override
    public void showMenu(Scanner scanner) {
        ProductMenu menu = new ProductMenu(this, scanner);
        menu.showMenu();
    }

    // --- Persistence Helper Methods (Like your friend's code) ---
    private void saveProducts() {
        if (dataFile == null) return;
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(dataFile)))) {
            oos.writeObject(productList);
            oos.flush();
        } catch (IOException e) {
            System.err.println("   ⚠️ Error saving products: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadProducts() {
        if (dataFile == null || !dataFile.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(dataFile)))) {
            List<Product> loaded = (List<Product>) ois.readObject();
            productList.clear();
            productList.addAll(loaded);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("   ⚠️ Error loading products: " + e.getMessage());
        }
    }
}