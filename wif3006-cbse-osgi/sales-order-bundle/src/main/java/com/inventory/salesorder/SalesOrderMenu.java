package com.inventory.salesorder;

import com.inventory.api.salesorder.SalesOrder;
import com.inventory.api.salesorder.SalesOrderService;
import com.inventory.api.salesorder.SalesOrderItem;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * UI Component for Sales Order Management (UC-07) and Reporting (UC-08).
 * Developed as part of the Sales Management Module.
 */
public class SalesOrderMenu {
    private final SalesOrderService salesOrderService;
    private final Scanner scanner;
    private boolean running = true;

    private static final List<String> VALID_STATUSES = List.of("pending", "processing", "shipped", "delivered", "cancelled");
    private static final List<String> EDITABLE_STATUSES = List.of("pending", "processing", "cancelled");

    public SalesOrderMenu(SalesOrderService salesOrderService, Scanner scanner) {
        this.salesOrderService = salesOrderService;
        this.scanner = scanner;
    }

    public void showSalesOrderMenu() {
        running = true;
        while (running) {
            System.out.println("\n========================================");
            System.out.println("        SALES ORDER MANAGEMENT          ");
            System.out.println("========================================");
            System.out.println("1. View Sales Orders");
            System.out.println("2. Add Sales Order");
            System.out.println("3. Edit Sales Order");
            System.out.println("4. Delete Sales Order");
            System.out.println("5. View Sales Reports");
            System.out.println("6. Exit to Main Menu");
            System.out.print("Select an option (1-6): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": viewSalesOrders(); break;
                case "2": addSalesOrder(); break;
                case "3": editSalesOrder(); break;
                case "4": deleteSalesOrder(); break;
                case "5": viewSalesReports(); break;
                case "6":
                        System.out.println("Returning to main menu...");
                        running = false;
                        break;
                default: System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void viewSalesOrders() {
        System.out.println("\n--- List of Sales Orders ---");
        List<SalesOrder> orders = salesOrderService.getAllSalesOrders();
        if (orders.isEmpty()) {
            System.out.println("No sales orders found.");
            return;
        }

        System.out.printf("%-5s | %-15s | %-12s | %-20s | %-12s | %-10s%n",
                "ID", "Order Number", "Date", "Customer", "Total", "Status");
        System.out.println("-".repeat(85));

        for (SalesOrder order : orders) {
            System.out.printf("%-5d | %-15s | %-12s | %-20s | $%-11.2f | %-10s%n",
                    order.getOrderId(),
                    order.getOrderNumber(),
                    order.getOrderDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    order.getCustomer(),
                    order.getTotalPrice(),
                    order.getOrderStatus());
        }
    }

    private void addSalesOrder() {
        System.out.println("\n--- Add New Sales Order ---");
        System.out.print("Order Number: ");
        String orderNum = scanner.nextLine().trim();
        if (checkCancel(orderNum)) return;

        LocalDate orderDate = null;
        while (orderDate == null) {
            System.out.print("Order Date (YYYY-MM-DD) [Leave blank for today]: ");
            String dateStr = scanner.nextLine().trim();
            if (checkCancel(dateStr)) return;
            if (dateStr.isEmpty()) {
                orderDate = LocalDate.now();
            } else {
                try {
                    orderDate = LocalDate.parse(dateStr); // Fixes the String to LocalDate error
                } catch (DateTimeParseException e) {
                    System.out.println("Invalid date format. Use YYYY-MM-DD.");
                }
            }
        }

        System.out.print("Customer Name: ");
        String customer = scanner.nextLine().trim();
        if (checkCancel(customer)) return;

        List<SalesOrderItem> items = collectOrderItems();
        if (items == null || items.isEmpty()) return;

        // Ensure constructor matches: ID, OrderNumber, Date, Customer, Items, Status
        SalesOrder newOrder = new SalesOrder(0, orderDate, orderNum, customer, items, "pending");
        salesOrderService.addSalesOrder(newOrder); 
        System.out.println("✅ Sales order added successfully!");
    }

    private void editSalesOrder() {
        System.out.println("\n--- Edit Sales Order ---");
        viewSalesOrders();
        
        System.out.print("\nEnter Sales Order ID to edit (or 'cancel'): ");
        String idStr = scanner.nextLine().trim();
        if (checkCancel(idStr)) return;

        try {
            int orderId = Integer.parseInt(idStr);
            SalesOrder order = salesOrderService.getSalesOrderById(orderId);
            if (order == null) {
                System.out.println("Order not found.");
                return;
            }

            System.out.print("New Customer [" + order.getCustomer() + "]: ");
            String customer = scanner.nextLine().trim();
            if (!customer.isEmpty()) order.setCustomer(customer);

            System.out.print("New Status: ");
            String status = scanner.nextLine().trim().toLowerCase();
            if (!status.isEmpty() && EDITABLE_STATUSES.contains(status)) {
                order.setOrderStatus(status);
            }

            // FIX: Passes both ID and Order object to match the Interface
            salesOrderService.updateSalesOrder(order.getOrderId(), order);
            System.out.println("✅ Order updated successfully.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
        }
    }

    private void deleteSalesOrder() {
        System.out.println("\n--- Delete Sales Order ---");
        viewSalesOrders();
        System.out.print("\nEnter Sales Order ID to delete (or 'cancel'): ");
        String idStr = scanner.nextLine().trim();
        if (checkCancel(idStr)) return;

        try {
            int orderId = Integer.parseInt(idStr);
            SalesOrder order = salesOrderService.getSalesOrderById(orderId);
            if (order != null) {
                System.out.print("Confirm deletion of order {" + order.getOrderId() + "} [Y/N]? ");
                if (scanner.nextLine().trim().equalsIgnoreCase("Y")) {
                    salesOrderService.deleteSalesOrder(orderId);
                    System.out.println("✅ Order deleted.");
                }
            } else {
                System.out.println("Order not found.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
        }
    }

    private void viewSalesReports() {
        List<SalesOrder> orders = salesOrderService.getAllSalesOrders();
        if (orders.isEmpty()) {
            System.out.println("No sales data available for reports.");
            return;
        }

        List<SalesOrder> currentView = new ArrayList<>(orders);
        while (true) {
            displayFullReport(currentView);
            System.out.println("\n--- Reporting Options ---");
            System.out.println("1. Filter by Status");
            System.out.println("2. Sort by Order ID");
            System.out.println("3. Sort by Customer");
            System.out.println("4. Sort by Total Value");
            System.out.println("5. Reset/Refresh");
            System.out.println("6. Back to Sales Menu");
            System.out.print("Selection: ");
            
            String opt = scanner.nextLine().trim();
            if (opt.equals("6")) break;
            
            switch (opt) {
                case "1": currentView = filterByStatus(currentView); break;
                case "2": currentView.sort((a,b) -> Integer.compare(a.getOrderId(), b.getOrderId())); break;
                case "3": currentView.sort((a,b) -> a.getCustomer().compareToIgnoreCase(b.getCustomer())); break;
                case "4": currentView.sort((a,b) -> Double.compare(b.getTotalPrice(), a.getTotalPrice())); break;
                case "5": currentView = new ArrayList<>(salesOrderService.getAllSalesOrders()); break;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private List<SalesOrder> filterByStatus(List<SalesOrder> orders) {
        System.out.print("Enter status to filter (" + String.join("/", VALID_STATUSES) + "): ");
        String status = scanner.nextLine().trim().toLowerCase();
        return orders.stream()
                .filter(o -> o.getOrderStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());
    }

    private void displayFullReport(List<SalesOrder> orders) {
        System.out.println("\n" + "=".repeat(110));
        System.out.println("                          SALES ORDER REPORT - DETAILED VIEW");
        System.out.println("=".repeat(110));
        double grandTotal = 0;
        for (SalesOrder order : orders) {
            System.out.printf("ID: %-5d | Num: %-12s | Date: %-10s | Customer: %-20s | Status: %-10s%n",
                    order.getOrderId(), order.getOrderNumber(), order.getOrderDate(), order.getCustomer(), order.getOrderStatus());
            System.out.println("-".repeat(110));
            for (SalesOrderItem item : order.getSalesOrderItems()) {
                System.out.printf("   > %-30s | Qty: %-5d | Price: $%-10.2f | Total: $%-10.2f%n",
                        item.getItemName(), item.getQuantity(), item.getPricePerItem(), item.getTotalPrice());
            }
            System.out.printf("%90s: $%.2f%n", "Order Subtotal", order.getTotalPrice());
            System.out.println("-".repeat(110));
            grandTotal += order.getTotalPrice();
        }
        System.out.printf("\nTOTAL ORDERS: %d | TOTAL REVENUE: $%.2f%n", orders.size(), grandTotal);
        System.out.println("=".repeat(110));
    }

    private List<SalesOrderItem> collectOrderItems() {
        List<SalesOrderItem> items = new ArrayList<>();
        while (true) {
            System.out.print("Item Name (or 'done' to finish): ");
            String name = scanner.nextLine().trim();
            if (name.equalsIgnoreCase("done")) break;
            if (checkCancel(name)) return null;

            System.out.print("Quantity: ");
            int qty = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Price per Unit: ");
            double price = Double.parseDouble(scanner.nextLine().trim());

            items.add(new SalesOrderItem(name, qty, price));
        }
        return items;
    }

    private boolean checkCancel(String input) {
        return input.equalsIgnoreCase("cancel");
    }
}