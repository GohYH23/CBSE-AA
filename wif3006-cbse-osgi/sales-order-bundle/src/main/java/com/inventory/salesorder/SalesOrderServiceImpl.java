package com.inventory.salesorder;

import com.inventory.api.salesorder.SalesOrder;
import com.inventory.api.salesorder.SalesOrderService;
import com.inventory.api.salesorder.SalesOrderItem;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.util.Scanner;
import java.util.stream.Collectors;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Service Implementation for Sales Order Management (UC-07).
 * Manages SalesOrder entities and provides UI entry points.
 */
@Component(service = SalesOrderService.class, immediate = true)
public class SalesOrderServiceImpl implements SalesOrderService {
    
    private SalesOrderMenu currentMenu;
    
    private static final String DATA_DIR = "data";
    private static final String DATA_FILE = "sales-orders.dat";
    private final List<SalesOrder> salesOrders = new ArrayList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private File dataFile;
    
    @Activate
    public void activate() {
        System.out.println("✅ Sales Order Component Started.");
        
        // Ensure data directory exists
        try {
            Path dataDir = Paths.get(DATA_DIR);
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }
            
            dataFile = new File(DATA_DIR, DATA_FILE);
            loadSalesOrders();
            
            System.out.println("    Loaded " + salesOrders.size() + " sales order(s) from storage.");
        } catch (Exception e) {
            System.err.println("    ⚠️ Warning: Could not initialize sales storage: " + e.getMessage());
        }
    }
    
    @Deactivate
    public void deactivate() {
        System.out.println("❌ Stopping Sales Order Component...");
        saveSalesOrders(); // Final save before component shutdown
    }
    
    @Override
    public List<SalesOrder> getAllSalesOrders() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(salesOrders);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public SalesOrder getSalesOrderById(int orderId) {
        lock.readLock().lock();
        try {
            return salesOrders.stream()
                .filter(so -> so.getOrderId() == orderId)
                .findFirst()
                .orElse(null);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public SalesOrder addSalesOrder(SalesOrder salesOrder) {
        lock.writeLock().lock();
        try {
            if (salesOrder.getOrderId() == 0) {
                salesOrder.setOrderId(getNextOrderId());
            }
            
            salesOrders.add(salesOrder);
            saveSalesOrders();
            return salesOrder;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public SalesOrder updateSalesOrder(int orderId, SalesOrder updatedOrder) {
        lock.writeLock().lock();
        try {
            for (int i = 0; i < salesOrders.size(); i++) {
                if (salesOrders.get(i).getOrderId() == updatedOrder.getOrderId()) {
                    SalesOrder existingOrder = salesOrders.get(i);
                    
                    // Business Logic: Track shipment and delivery dates automatically
                    if (updatedOrder.getOrderStatus().equalsIgnoreCase("shipped")) {
                        if (!existingOrder.getOrderStatus().equalsIgnoreCase("shipped")) {
                            updatedOrder.setShippedDate(LocalDate.now());
                        } else {
                            updatedOrder.setShippedDate(existingOrder.getShippedDate());
                        }
                    } else if (updatedOrder.getOrderStatus().equalsIgnoreCase("delivered")) {
                        if (!existingOrder.getOrderStatus().equalsIgnoreCase("delivered")) {
                            updatedOrder.setDeliveryDate(LocalDate.now());
                        } else {
                            updatedOrder.setDeliveryDate(existingOrder.getDeliveryDate());
                        }
                        // Keep shipped date from previous status
                        updatedOrder.setShippedDate(existingOrder.getShippedDate());
                    }
                    
                    salesOrders.set(i, updatedOrder);
                    saveSalesOrders();
                    return updatedOrder;
                }
            }
            return null;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public boolean deleteSalesOrder(int orderId) {
        lock.writeLock().lock();
        try {
            boolean removed = salesOrders.removeIf(so -> so.getOrderId() == orderId);
            if (removed) {
                saveSalesOrders();
            }
            return removed;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public int getNextOrderId() {
        lock.readLock().lock();
        try {
            return salesOrders.stream()
                .mapToInt(SalesOrder::getOrderId)
                .max()
                .orElse(0) + 1;
        } finally {
            lock.readLock().unlock();
        }
    }

    // --- UI Entry Points ---

    public void showSalesOrderMenu(Scanner scanner) {
        if (currentMenu == null) {
            currentMenu = new SalesOrderMenu(this, scanner);
        }
        currentMenu.showSalesOrderMenu();
    }
    
    public void showSalesReturnMenu(Scanner scanner) {
        // Entry point for UC-09: Manage Sales Return
        SalesReturnMenu salesReturnMenu = new SalesReturnMenu(this, scanner);
        salesReturnMenu.showSalesReturnMenu();
    }
    
    // --- Persistence Logic ---
    
    private void saveSalesOrders() {
        if (dataFile == null) return;
        
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(dataFile)))) {
            oos.writeObject(salesOrders);
            oos.flush();
        } catch (IOException e) {
            System.err.println("   ⚠️ Error saving sales orders: " + e.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadSalesOrders() {
        if (dataFile == null || !dataFile.exists()) return;
        
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(dataFile)))) {
            List<SalesOrder> loaded = (List<SalesOrder>) ois.readObject();
            salesOrders.clear();
            salesOrders.addAll(loaded);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("   ⚠️ Error loading sales orders: " + e.getMessage());
        }
    }

    @Override
    public boolean salesOrderExists(int orderId) {
        return salesOrders.stream().anyMatch(o -> o.getOrderId() == orderId);
    }
}