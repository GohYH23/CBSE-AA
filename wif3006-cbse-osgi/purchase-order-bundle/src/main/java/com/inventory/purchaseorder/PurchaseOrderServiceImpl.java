package com.inventory.purchaseorder;

import com.inventory.api.purchaseorder.PurchaseOrder;
import com.inventory.api.purchaseorder.PurchaseOrderService;
import com.inventory.api.purchaseorder.OrderItem;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component(service = PurchaseOrderService.class, immediate = true)
public class PurchaseOrderServiceImpl implements PurchaseOrderService {
    
    private PurchaseOrderMenu currentMenu;
    
    private static final String DATA_DIR = "data";
    private static final String DATA_FILE = "purchase-orders.dat";
    private final List<PurchaseOrder> purchaseOrders = new ArrayList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private File dataFile;
    
    @Activate
    public void activate() {
        System.out.println("✅ Purchase Order Component Started.");
        
        // Create data directory if it doesn't exist
        try {
            Path dataDir = Paths.get(DATA_DIR);
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }
            
            dataFile = new File(DATA_DIR, DATA_FILE);
            
            // Load existing data
            loadPurchaseOrders();
            
            System.out.println("   Loaded " + purchaseOrders.size() + " purchase order(s) from storage.");
        } catch (Exception e) {
            System.err.println("   ⚠️ Warning: Could not initialize data storage: " + e.getMessage());
            System.err.println("   Starting with empty purchase order list.");
        }
    }
    
    @Deactivate
    public void deactivate() {
        System.out.println("❌ Stopping Purchase Order Component...");
        savePurchaseOrders(); // Save on shutdown
    }
    
    @Override
    public List<PurchaseOrder> getAllPurchaseOrders() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(purchaseOrders);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public PurchaseOrder getPurchaseOrderById(int orderId) {
        lock.readLock().lock();
        try {
            return purchaseOrders.stream()
                .filter(po -> po.getOrderId() == orderId)
                .findFirst()
                .orElse(null);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public PurchaseOrder addPurchaseOrder(PurchaseOrder purchaseOrder) {
        lock.writeLock().lock();
        try {
            // Generate ID if not set
            if (purchaseOrder.getOrderId() == 0) {
                purchaseOrder.setOrderId(getNextOrderId());
            }
            
            purchaseOrders.add(purchaseOrder);
            savePurchaseOrders();
            return purchaseOrder;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public PurchaseOrder updatePurchaseOrder(int orderId, PurchaseOrder updatedOrder) {
        lock.writeLock().lock();
        try {
            for (int i = 0; i < purchaseOrders.size(); i++) {
                if (purchaseOrders.get(i).getOrderId() == orderId) {
                    PurchaseOrder existingOrder = purchaseOrders.get(i);
                    updatedOrder.setOrderId(orderId); // Ensure ID matches
                    
                    // Handle received date: set when status changes to "received"
                    if (updatedOrder.getOrderStatus().equalsIgnoreCase("received")) {
                        if (existingOrder.getOrderStatus().equalsIgnoreCase("received")) {
                            // Already received - keep existing received date
                            updatedOrder.setReceivedDate(existingOrder.getReceivedDate());
                        } else {
                            // Status changed to received - set received date to today
                            updatedOrder.setReceivedDate(LocalDate.now());
                        }
                        // Clear returned date when status changes to received
                        updatedOrder.setReturnedDate(null);
                    } else if (updatedOrder.getOrderStatus().equalsIgnoreCase("returned")) {
                        // Handle returned date: set when status changes to "returned"
                        if (existingOrder.getOrderStatus().equalsIgnoreCase("returned")) {
                            // Already returned - keep existing returned date
                            updatedOrder.setReturnedDate(existingOrder.getReturnedDate());
                        } else {
                            // Status changed to returned - set returned date to today
                            updatedOrder.setReturnedDate(LocalDate.now());
                        }
                        // Keep received date if exists
                        updatedOrder.setReceivedDate(existingOrder.getReceivedDate());
                    } else {
                        // Not received or returned - clear both dates
                        updatedOrder.setReceivedDate(null);
                        updatedOrder.setReturnedDate(null);
                    }
                    
                    purchaseOrders.set(i, updatedOrder);
                    savePurchaseOrders();
                    return updatedOrder;
                }
            }
            return null; // Not found
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    // Get purchase orders by status
    public List<PurchaseOrder> getPurchaseOrdersByStatus(String status) {
        lock.readLock().lock();
        try {
            return purchaseOrders.stream()
                .filter(po -> po.getOrderStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public boolean deletePurchaseOrder(int orderId) {
        lock.writeLock().lock();
        try {
            boolean removed = purchaseOrders.removeIf(po -> po.getOrderId() == orderId);
            if (removed) {
                savePurchaseOrders();
            }
            return removed;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public boolean purchaseOrderExists(int orderId) {
        lock.readLock().lock();
        try {
            return purchaseOrders.stream().anyMatch(po -> po.getOrderId() == orderId);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public int getNextOrderId() {
        lock.readLock().lock();
        try {
            if (purchaseOrders.isEmpty()) {
                return 1;
            }
            return purchaseOrders.stream()
                .mapToInt(PurchaseOrder::getOrderId)
                .max()
                .orElse(0) + 1;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    // Method to show menu (called from main menu)
    public void showPurchaseOrderMenu(Scanner scanner) {
        if (currentMenu != null) {
            currentMenu.setRunning(true);
        } else {
            currentMenu = new PurchaseOrderMenu(this, scanner);
        }
        currentMenu.showPurchaseOrderMenu();
    }
    
    // Method to show Goods Receive menu (called from main menu)
    public void showGoodsReceiveMenu(Scanner scanner) {
        GoodsReceiveMenu goodsReceiveMenu = new GoodsReceiveMenu(this, scanner);
        goodsReceiveMenu.showGoodsReceiveMenu();
    }
    
    // Method to show Purchase Return menu (called from main menu)
    public void showPurchaseReturnMenu(Scanner scanner) {
        PurchaseReturnMenu purchaseReturnMenu = new PurchaseReturnMenu(this, scanner);
        purchaseReturnMenu.showPurchaseReturnMenu();
    }
    
    // --- Persistence Methods ---
    
    private void savePurchaseOrders() {
        if (dataFile == null) return;
        
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(dataFile)))) {
            oos.writeObject(purchaseOrders);
            oos.flush();
        } catch (IOException e) {
            System.err.println("   ⚠️ Error saving purchase orders: " + e.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadPurchaseOrders() {
        if (dataFile == null || !dataFile.exists()) {
            return;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(dataFile)))) {
            List<PurchaseOrder> loaded = (List<PurchaseOrder>) ois.readObject();
            purchaseOrders.clear();
            purchaseOrders.addAll(loaded);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("   ⚠️ Error loading purchase orders: " + e.getMessage());
            // Continue with empty list
        }
    }
}
