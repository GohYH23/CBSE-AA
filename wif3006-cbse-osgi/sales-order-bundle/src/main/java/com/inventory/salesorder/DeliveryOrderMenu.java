package com.inventory.salesorder;

import com.inventory.api.salesorder.SalesOrder;
import com.inventory.api.salesorder.SalesOrderItem; 
import com.inventory.api.salesorder.SalesOrderService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class DeliveryOrderMenu {
    private final SalesOrderService salesOrderService;
    private final Scanner scanner;
    private boolean running = true;
    
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String STATUS_CONFIRMED = "CONFIRMED";
    private static final String STATUS_ARCHIVED = "ARCHIVED";

    public DeliveryOrderMenu(SalesOrderService salesOrderService, Scanner scanner) {
        this.salesOrderService = salesOrderService;
        this.scanner = scanner;
    }

    public void showDeliveryOrderMenu() {
        while (running) {
            System.out.println("\n========================================");
            System.out.println("       DELIVERY ORDER MANAGEMENT        ");
            System.out.println("========================================");
            System.out.println("1. View Delivery Orders");
            System.out.println("2. Create New Delivery Order (DRAFT)");
            System.out.println("3. Confirm Delivery Order (DRAFT -> CONFIRMED)");
            System.out.println("4. Archive Delivery Order (CONFIRMED -> ARCHIVED)");
            System.out.println("5. Cancel Delivery Order");
            System.out.println("6. Exit to Main Menu");
            System.out.print("Select an option (1-6): ");

            try {
                String input = scanner.nextLine().trim();
                switch (input) {
                    case "1": viewOrders(); break;
                    case "2": addOrder(); break;
                    case "3": updateStatus(STATUS_DRAFT, STATUS_CONFIRMED, "Confirm"); break;
                    case "4": updateStatus(STATUS_CONFIRMED, STATUS_ARCHIVED, "Archive"); break;
                    case "5": cancelOrder(); break;
                    case "6":
                        System.out.println("Returning to main menu...");
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid option. Please enter 1-6.");
                }
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
        }
    }

    private void viewOrders() {
        List<SalesOrder> orders = salesOrderService.getAllSalesOrders();
        System.out.println("\n--- Delivery Orders List ---");
        if (orders.isEmpty()) {
            System.out.println("(No delivery orders found)");
            return;
        }
        printOrderTable(orders);
    }

    private void addOrder() {
        System.out.println("\n--- Create New Delivery Order ---");
        
        System.out.print("Customer Name (or 'cancel'): ");
        String customer = scanner.nextLine().trim();
        if (checkCancel(customer)) return;

        LocalDate orderDate = readOrderDate("Delivery Date (YYYY-MM-DD)", true);
        if (orderDate == null) return;

        // FIXED: Changed OrderItem to SalesOrderItem
        List<SalesOrderItem> items = manageOrderItems();
        if (items == null || items.isEmpty()) return;

        SalesOrder newOrder = new SalesOrder();
        newOrder.setCustomer(customer);
        newOrder.setOrderDate(orderDate);
        newOrder.setOrderItems(items); // Use the setter name defined in your API
        newOrder.setStatus(STATUS_DRAFT);

        salesOrderService.addSalesOrder(newOrder);
        System.out.println("✅ Delivery Order created as DRAFT.");
    }

    private void updateStatus(String currentStatus, String nextStatus, String actionName) {
        List<SalesOrder> validOrders = salesOrderService.getAllSalesOrders().stream()
            .filter(o -> o.getStatus().equalsIgnoreCase(currentStatus))
            .collect(Collectors.toList());

        if (validOrders.isEmpty()) {
            System.out.println("No orders currently in " + currentStatus + " status to " + actionName + ".");
            return;
        }

        printOrderTable(validOrders);
        int id = readInteger("\nEnter Order ID to " + actionName, true);
        if (id == -1) return;

        SalesOrder order = salesOrderService.getSalesOrderById(id);
        if (order != null && order.getStatus().equalsIgnoreCase(currentStatus)) {
            order.setStatus(nextStatus);
            salesOrderService.updateSalesOrder(id, order);
            System.out.println("✅ Order #" + id + " successfully moved to " + nextStatus + ".");
        } else {
            System.out.println("❌ Invalid Order ID or status transition.");
        }
    }

    private void cancelOrder() {
        System.out.println("\n--- Cancel Delivery Order ---");
        List<SalesOrder> cancellable = salesOrderService.getAllSalesOrders().stream()
            .filter(o -> !o.getStatus().equalsIgnoreCase(STATUS_CANCELLED) && !o.getStatus().equalsIgnoreCase(STATUS_ARCHIVED))
            .collect(Collectors.toList());

        if (cancellable.isEmpty()) {
            System.out.println("No active orders available to cancel.");
            return;
        }

        printOrderTable(cancellable);
        int id = readInteger("Enter Order ID to CANCEL", true);
        if (id == -1) return;

        SalesOrder order = salesOrderService.getSalesOrderById(id);
        if (order != null) {
            order.setStatus(STATUS_CANCELLED);
            salesOrderService.updateSalesOrder(id, order);
            System.out.println("✅ Order #" + id + " has been CANCELLED.");
        }
    }

    // FIXED: Changed return type to List<SalesOrderItem>
    private List<SalesOrderItem> manageOrderItems() {
        List<SalesOrderItem> items = new ArrayList<>();
        System.out.println("Enter items (type 'done' when finished):");
        while (true) {
            System.out.print("  Item Name: ");
            String name = scanner.nextLine().trim();
            if (checkCancel(name)) return null;
            if (name.equalsIgnoreCase("done")) break;

            int qty = readInteger("  Quantity", true);
            if (qty == -1) return null;

            double price = readDouble("  Price per Item", true);
            if (price == -1) return null;

            // FIXED: Using SalesOrderItem
            items.add(new SalesOrderItem(name, qty, price));
        }
        return items;
    }

    private void printOrderTable(List<SalesOrder> orders) {
        System.out.println("-".repeat(80));
        System.out.printf("%-5s | %-15s | %-12s | %-15s | %-10s%n", "ID", "Customer", "Date", "Total", "Status");
        System.out.println("-".repeat(80));
        for (SalesOrder o : orders) {
            System.out.printf("%-5d | %-15s | %-12s | $%-14.2f | %-10s%n",
                o.getOrderId(), o.getCustomer(), o.getOrderDate(), o.getTotalPrice(), o.getStatus());
        }
        System.out.println("-".repeat(80));
    }

    private boolean checkCancel(String input) { return input.equalsIgnoreCase("cancel"); }

    private int readInteger(String prompt, boolean allowCancel) {
        while (true) {
            System.out.print(prompt + (allowCancel ? " (or 'cancel')" : "") + ": ");
            String input = scanner.nextLine().trim();
            if (allowCancel && checkCancel(input)) return -1;
            try { return Integer.parseInt(input); } 
            catch (NumberFormatException e) { System.out.println("Invalid number."); }
        }
    }

    private double readDouble(String prompt, boolean allowCancel) {
        while (true) {
            System.out.print(prompt + (allowCancel ? " (or 'cancel')" : "") + ": ");
            String input = scanner.nextLine().trim();
            if (allowCancel && checkCancel(input)) return -1;
            try { return Double.parseDouble(input); } 
            catch (NumberFormatException e) { System.out.println("Invalid price."); }
        }
    }

    private LocalDate readOrderDate(String prompt, boolean allowCancel) {
        while (true) {
            System.out.print(prompt + (allowCancel ? " (or 'cancel')" : "") + ": ");
            String input = scanner.nextLine().trim();
            if (allowCancel && checkCancel(input)) return null;
            try { return LocalDate.parse(input, DateTimeFormatter.ISO_LOCAL_DATE); } 
            catch (DateTimeParseException e) { System.out.println("Invalid format (Use YYYY-MM-DD)."); }
        }
    }
}