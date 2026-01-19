package com.inventory.salesorder;

import com.inventory.api.salesorder.SalesOrder;
import com.inventory.api.salesorder.SalesOrderService;
import com.inventory.api.purchaseorder.OrderItem; // Reusing the OrderItem model

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class SalesReturnMenu {
    private final SalesOrderService salesOrderService;
    private final Scanner scanner;
    private boolean running = true;

    // Status Constants matching your requirements
    private static final String STATUS_CONFIRMED = "CONFIRMED";
    private static final String STATUS_ARCHIVED = "ARCHIVED";
    private static final String STATUS_RETURNED = "RETURNED";

    public SalesReturnMenu(SalesOrderService salesOrderService, Scanner scanner) {
        this.salesOrderService = salesOrderService;
        this.scanner = scanner;
    }

    public void showSalesReturnMenu() {
        while (running) {
            System.out.println("\n========================================");
            System.out.println("          SALES RETURN MANAGEMENT       ");
            System.out.println("========================================");
            System.out.println("1. View Sales Returns");
            System.out.println("2. Add Sales Return");
            System.out.println("3. Edit Sales Return");
            System.out.println("4. Delete Sales Return (Revert)");
            System.out.println("5. Exit to Main Menu");
            System.out.print("Select an option (1-5): ");

            try {
                String input = scanner.nextLine().trim();
                if (!isValidMenuInput(input)) {
                    System.out.println("Invalid option. Please enter a number between 1 and 5.");
                    continue;
                }

                switch (input) {
                    case "1": viewSalesReturn(); break;
                    case "2": addSalesReturn(); break;
                    case "3": editSalesReturn(); break;
                    case "4": deleteSalesReturn(); break;
                    case "5":
                        System.out.println("Returning to main menu...");
                        running = false;
                        break;
                }
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
        }
    }

    private boolean isValidMenuInput(String input) {
        try {
            int choice = Integer.parseInt(input);
            return choice >= 1 && choice <= 5;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean checkCancel(String input) {
        return input.equalsIgnoreCase("cancel");
    }

    private void viewSalesReturn() {
        List<SalesOrder> returnedOrders = salesOrderService.getAllSalesOrders().stream()
                .filter(order -> order.getOrderStatus().equalsIgnoreCase(STATUS_RETURNED))
                .collect(Collectors.toList());

        System.out.println("\n--- Sales Return List ---");
        if (returnedOrders.isEmpty()) {
            System.out.println("(No sales return records found)");
            return;
        }

        System.out.println("-".repeat(120));
        System.out.printf("%-12s | %-15s | %-20s | %-15s | %-12s%n", 
            "Return Date", "Order ID", "Customer", "Original Status", "Total Refund");
        System.out.println("-".repeat(120));

        for (SalesOrder order : returnedOrders) {
            System.out.printf("%-12s | %-15d | %-20s | %-15s | $%-11.2f%n",
                LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                order.getOrderId(),
                order.getCustomer(),
                order.getOrderStatus(),
                order.getTotalPrice());
        }
        System.out.println("-".repeat(120));
    }

    private void addSalesReturn() {
        System.out.println("\n--- Add Sales Return ---");
        System.out.println("(Enter 'cancel' at any time to return to menu)");

        // Only CONFIRMED or ARCHIVED orders can be returned
        List<SalesOrder> eligibleOrders = salesOrderService.getAllSalesOrders().stream()
                .filter(o -> o.getOrderStatus().equalsIgnoreCase(STATUS_CONFIRMED) || o.getOrderStatus().equalsIgnoreCase(STATUS_ARCHIVED))
                .collect(Collectors.toList());

        if (eligibleOrders.isEmpty()) {
            System.out.println("No CONFIRMED or ARCHIVED sales orders found to return.");
            return;
        }

        printOrderTable(eligibleOrders);
        int orderId = readInteger("\nEnter Order ID to return", true);
        if (orderId == -1) return;

        SalesOrder orderToReturn = salesOrderService.getSalesOrderById(orderId);
        if (orderToReturn == null) {
            System.out.println("Order not found.");
            return;
        }

        System.out.print("\nDo you want to return this order for customer " + orderToReturn.getCustomer() + "? (Y/N): ");
        String confirm = readYesNo();
        if (confirm != null && confirm.startsWith("Y")) {
            orderToReturn.setOrderStatus(STATUS_RETURNED);
            salesOrderService.updateSalesOrder(orderId, orderToReturn);
            System.out.println("✅ Sales return processed successfully!");
        }
    }

    private void editSalesReturn() {
        System.out.println("\n--- Edit Sales Return ---");
        List<SalesOrder> returnedOrders = salesOrderService.getAllSalesOrders().stream()
                .filter(order -> order.getOrderStatus().equalsIgnoreCase(STATUS_RETURNED))
                .collect(Collectors.toList());

        if (returnedOrders.isEmpty()) {
            System.out.println("No return records to edit.");
            return;
        }

        printOrderTable(returnedOrders);
        int orderId = readInteger("\nEnter Order ID to edit", true);
        if (orderId == -1) return;

        SalesOrder orderToEdit = salesOrderService.getSalesOrderById(orderId);
        if (orderToEdit == null) return;

        System.out.println("\nEditing Return for #" + orderId);
        String customer = readString("Update Customer Name", true, orderToEdit.getCustomer(), true);
        if (customer.equals("CANCEL_SIGNAL")) return;

        orderToEdit.setCustomer(customer);
        salesOrderService.updateSalesOrder(orderId, orderToEdit);
        System.out.println("✅ Return record updated.");
    }

    private void deleteSalesReturn() {
        System.out.println("\n--- Delete Sales Return (Revert to Confirmed) ---");
        List<SalesOrder> returnedOrders = salesOrderService.getAllSalesOrders().stream()
                .filter(o -> o.getOrderStatus().equalsIgnoreCase(STATUS_RETURNED))
                .collect(Collectors.toList());

        if (returnedOrders.isEmpty()) {
            System.out.println("No return records found.");
            return;
        }

        printOrderTable(returnedOrders);
        int orderId = readInteger("\nEnter Order ID to revert back to CONFIRMED", true);
        if (orderId == -1) return;

        SalesOrder order = salesOrderService.getSalesOrderById(orderId);
        if (order != null) {
            System.out.print("Revert status to CONFIRMED? (Y/N): ");
            String confirm = readYesNo();
            if (confirm != null && confirm.startsWith("Y")) {
                order.setOrderStatus(STATUS_CONFIRMED);
                salesOrderService.updateSalesOrder(orderId, order);
                System.out.println("✅ Return deleted. Order is now CONFIRMED again.");
            }
        }
    }

    // --- HELPER METHODS (Matching your GoodsReceiveMenu format) ---

    private void printOrderTable(List<SalesOrder> orders) {
        System.out.println("-".repeat(110));
        System.out.printf("%-8s | %-20s | %-12s | %-15s | %-12s%n", 
            "ID", "Customer", "Date", "Status", "Total Price");
        System.out.println("-".repeat(110));
        for (SalesOrder o : orders) {
            System.out.printf("%-8d | %-20s | %-12s | %-15s | $%-11.2f%n",
                o.getOrderId(), o.getCustomer(), o.getOrderDate(), o.getOrderStatus(), o.getTotalPrice());
        }
        System.out.println("-".repeat(110));
    }

    private String readYesNo() {
        while (true) {
            System.out.print("Enter Y/N: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (checkCancel(input)) return null;
            if (input.equals("Y") || input.equals("YES") || input.equals("N") || input.equals("NO")) return input;
            System.out.println("Please enter Y or N.");
        }
    }

    private int readInteger(String prompt, boolean allowCancel) {
        while (true) {
            System.out.print(prompt + ": ");
            String input = scanner.nextLine().trim();
            if (allowCancel && checkCancel(input)) return -1;
            try { return Integer.parseInt(input); } 
            catch (NumberFormatException e) { System.out.println("Invalid number."); }
        }
    }

    private String readString(String prompt, boolean allowEmpty, String defaultValue, boolean allowCancel) {
        while (true) {
            String p = allowEmpty ? prompt + " [" + defaultValue + "]: " : prompt + ": ";
            System.out.print(p);
            String input = scanner.nextLine().trim();
            if (allowCancel && checkCancel(input)) return "CANCEL_SIGNAL";
            if (input.isEmpty() && allowEmpty) return defaultValue;
            if (input.isEmpty()) { System.out.println("Cannot be empty."); continue; }
            return input;
        }
    }
}