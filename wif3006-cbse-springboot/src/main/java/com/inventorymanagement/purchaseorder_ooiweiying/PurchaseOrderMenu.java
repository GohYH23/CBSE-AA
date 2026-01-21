// Name: Ooi Wei Ying
// Student ID: 22056924

package com.inventorymanagement.purchaseorder_ooiweiying;

import com.inventorymanagement.purchaseorder_ooiweiying.model.OrderItem;
import com.inventorymanagement.purchaseorder_ooiweiying.model.PurchaseOrder;
import com.inventorymanagement.purchaseorder_ooiweiying.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

@Component
public class PurchaseOrderMenu {
    
    @Autowired
    private PurchaseOrderService purchaseOrderService;
    
    private static final List<String> VALID_STATUSES = List.of("pending", "shipping", "received", "cancelled", "returned");
    private static final List<String> EDITABLE_STATUSES = List.of("pending", "shipping", "cancelled");
    
    // =================== MAIN MENU ===================
    
    public void start(Scanner scanner) {
        boolean back = false;
        
        while (!back) {
            System.out.println("\n===========================");
            System.out.println("  PURCHASE MANAGEMENT MODULE");
            System.out.println("===========================");
            System.out.println("1. Purchase Order Menu");
            System.out.println("2. Goods Receive Menu");
            System.out.println("3. Purchase Return Menu");
            System.out.println("4. Exit to Main Menu");
            System.out.print("Enter choice: ");
            
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    showPurchaseOrderSubMenu(scanner);
                    break;
                case "2":
                    showGoodsReceiveSubMenu(scanner);
                    break;
                case "3":
                    showPurchaseReturnSubMenu(scanner);
                    break;
                case "4":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option. Please try again (1-4)");
            }
        }
    }
    
    // =================== PURCHASE ORDER SUB-MENU ===================
    
    private void showPurchaseOrderSubMenu(Scanner scanner) {
        boolean stay = true;
        while (stay) {
            System.out.println("\n========================================");
            System.out.println("      PURCHASE ORDER MANAGEMENT        ");
            System.out.println("========================================");
            System.out.println("1. View Purchase Orders");
            System.out.println("2. Add Purchase Order");
            System.out.println("3. Edit Purchase Order");
            System.out.println("4. Delete Purchase Order");
            System.out.println("5. View Purchase Reports");
            System.out.println("6. Back");
            System.out.print("Select an option (1-6): ");
            
            String input = scanner.nextLine().trim();
            
            if (!isValidMenuInput(input, 1, 6)) {
                System.out.println("Invalid option. Please enter a number between 1 and 6.");
                continue;
            }
            
            switch (input) {
                case "1":
                    viewPurchaseOrders();
                    break;
                case "2":
                    addPurchaseOrder(scanner);
                    break;
                case "3":
                    editPurchaseOrder(scanner);
                    break;
                case "4":
                    deletePurchaseOrder(scanner);
                    break;
                case "5":
                    viewPurchaseReports(scanner);
                    break;
                case "6":
                    stay = false;
                    break;
            }
        }
    }
    
    // =================== HELPER METHODS ===================
    
    private boolean isValidMenuInput(String input, int min, int max) {
        try {
            int choice = Integer.parseInt(input);
            return choice >= min && choice <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private boolean checkCancel(String input) {
        return input.equalsIgnoreCase("cancel");
    }
    
    private LocalDate readOrderDate(boolean allowEmpty, LocalDate defaultValue, Scanner scanner) {
        while (true) {
            String prompt = allowEmpty && defaultValue != null 
                ? "Enter Order Date (YYYY-MM-DD) [" + defaultValue.format(DateTimeFormatter.ISO_LOCAL_DATE) + "]: "
                : "Enter Order Date (YYYY-MM-DD): ";
            System.out.print(prompt);
            String dateStr = scanner.nextLine().trim();
            
            if (checkCancel(dateStr)) {
                return null;
            }
            
            if (dateStr.isEmpty() && allowEmpty && defaultValue != null) {
                return defaultValue;
            }
            
            if (dateStr.isEmpty() && !allowEmpty) {
                System.out.println("Order date cannot be empty.");
                continue;
            }
            
            try {
                LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
                LocalDate today = LocalDate.now();
                
                if (date.isAfter(today)) {
                    System.out.println("Order date cannot be in the future. Please enter today's date or a past date.");
                    continue;
                }
                
                return date;
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD (e.g., 2024-01-15)");
            }
        }
    }
    
    private String readString(String prompt, boolean allowEmpty, String defaultValue, boolean allowCancel, Scanner scanner) {
        while (true) {
            String fullPrompt = allowEmpty && defaultValue != null 
                ? prompt + " [" + defaultValue + "]: "
                : prompt + ": ";
            System.out.print(fullPrompt);
            String input = scanner.nextLine().trim();
            
            if (allowCancel && checkCancel(input)) {
                return "CANCEL_SIGNAL";
            }
            
            if (input.isEmpty() && allowEmpty && defaultValue != null) {
                return defaultValue;
            }
            
            if (input.isEmpty() && !allowEmpty) {
                System.out.println("This field cannot be empty.");
                continue;
            }
            
            return input;
        }
    }
    
    private int readInteger(String prompt, boolean allowCancel, Scanner scanner) {
        while (true) {
            System.out.print(prompt + ": ");
            String input = scanner.nextLine().trim();
            
            if (allowCancel && checkCancel(input)) {
                return -1;
            }
            
            try {
                int value = Integer.parseInt(input);
                if (value <= 0) {
                    System.out.println("Quantity must be greater than 0.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter an integer (whole number only).");
            }
        }
    }
    
    private double readDouble(String prompt, boolean allowCancel, Scanner scanner) {
        while (true) {
            System.out.print(prompt + ": $");
            String input = scanner.nextLine().trim();
            
            if (allowCancel && checkCancel(input)) {
                return -1;
            }
            
            try {
                double value = Double.parseDouble(input);
                if (value <= 0) {
                    System.out.println("Price must be greater than 0.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a decimal or integer number only.");
            }
        }
    }
    
    // =================== PURCHASE ORDER OPERATIONS ===================
    
    private void viewPurchaseOrders() {
        List<PurchaseOrder> orders = purchaseOrderService.getAllPurchaseOrders();
        
        System.out.println("\n--- Purchase Order List ---");
        if (orders.isEmpty()) {
            System.out.println("(No purchase orders found)");
            return;
        }
        
        System.out.println("--------------------------------------------------------------------------------------------------------");
        System.out.printf("%-8s | %-12s | %-15s | %-20s | %-15s | %-12s%n", 
            "Order ID", "Order Date", "Order Number", "Vendor", "Status", "Total Price");
        System.out.println("--------------------------------------------------------------------------------------------------------");
        
        for (PurchaseOrder order : orders) {
            System.out.printf("%-8d | %-12s | %-15s | %-20s | %-15s | $%-11.2f%n",
                order.getOrderId(),
                order.getOrderDate() != null ? order.getOrderDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "N/A",
                order.getOrderNumber(),
                order.getVendor(),
                order.getOrderStatus(),
                order.getTotalPrice());
        }
        System.out.println("--------------------------------------------------------------------------------------------------------");
    }
    
    private void addPurchaseOrder(Scanner scanner) {
        System.out.println("\n--- Add New Purchase Order ---");
        System.out.println("(Enter 'cancel' at any time to cancel and return to menu)");
        
        try {
            LocalDate orderDate = readOrderDate(false, null, scanner);
            if (orderDate == null) {
                System.out.println("Operation cancelled. Returning to Purchase Order menu.");
                return;
            }
            
            String vendor = readString("Enter Vendor", false, null, true, scanner);
            if (vendor.equals("CANCEL_SIGNAL")) {
                System.out.println("Operation cancelled. Returning to Purchase Order menu.");
                return;
            }
            
            List<OrderItem> orderItems = new ArrayList<>();
            System.out.println("\n--- Add Order Items (Enter 'done' when finished, 'cancel' to cancel) ---");
            System.out.println("Note: Each purchase order must have at least one item.");
            boolean addingItems = true;
            int itemCount = 1;
            
            while (addingItems) {
                System.out.println("\nItem #" + itemCount + ":");
                System.out.print("  Item Name/Description: ");
                String itemName = scanner.nextLine().trim();
                
                if (checkCancel(itemName)) {
                    System.out.println("Operation cancelled. Returning to Purchase Order menu.");
                    return;
                }
                
                if (itemName.equalsIgnoreCase("done")) {
                    if (orderItems.isEmpty()) {
                        System.out.println("\n⚠️  ERROR: Each purchase order must have at least one item.");
                        System.out.println("Please add at least one item before finishing, or enter 'cancel' to cancel the operation.");
                        continue;
                    }
                    addingItems = false;
                    break;
                }
                
                if (itemName.isEmpty()) {
                    System.out.println("Item name cannot be empty. Please try again or enter 'cancel' to cancel.");
                    continue;
                }
                
                int quantity = readInteger("  Quantity", true, scanner);
                if (quantity == -1) {
                    System.out.println("Operation cancelled. Returning to Purchase Order menu.");
                    return;
                }
                
                double pricePerItem = readDouble("  Price per Item", true, scanner);
                if (pricePerItem == -1) {
                    System.out.println("Operation cancelled. Returning to Purchase Order menu.");
                    return;
                }
                
                OrderItem item = new OrderItem(itemName, quantity, pricePerItem);
                orderItems.add(item);
                System.out.printf("  ✅ Added: %s x %d @ $%.2f each = $%.2f total%n",
                    itemName, quantity, pricePerItem, item.getTotalPrice());
                itemCount++;
            }
            
            System.out.println("\n--- Order Status ---");
            System.out.println("Note: Order status is automatically set to 'pending'.");
            
            PurchaseOrder newOrder = new PurchaseOrder();
            newOrder.setOrderDate(orderDate);
            int nextId = purchaseOrderService.getNextOrderId();
            newOrder.setOrderId(nextId);
            newOrder.setOrderNumber(purchaseOrderService.generateOrderNumber(nextId));
            newOrder.setVendor(vendor);
            newOrder.setOrderItems(orderItems);
            newOrder.setOrderStatus("pending");
            
            PurchaseOrder savedOrder = purchaseOrderService.createPurchaseOrder(newOrder);
            
            System.out.println("\n✅ Purchase Order created successfully!");
            System.out.println("\n--- Purchase Order Details ---");
            System.out.println(savedOrder.toDisplayString());
            System.out.printf("Total Price: $%.2f%n", savedOrder.getTotalPrice());
            
            System.out.println("\n--- Updated Purchase Order List ---");
            viewPurchaseOrders();
            
        } catch (Exception e) {
            System.out.println("Error adding purchase order: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void editPurchaseOrder(Scanner scanner) {
        System.out.println("\n--- Edit Purchase Order ---");
        System.out.println("(Enter 'cancel' at any time to cancel and return to menu)");
        
        List<PurchaseOrder> orders = purchaseOrderService.getAllPurchaseOrders();
        if (orders.isEmpty()) {
            System.out.println("No purchase orders found.");
            return;
        }
        
        System.out.println("\n--- Available Purchase Orders ---");
        viewPurchaseOrders();
        
        PurchaseOrder orderToEdit = null;
        while (orderToEdit == null) {
            System.out.print("\nEnter Purchase Order ID to edit (or 'cancel' to cancel): ");
            String idStr = scanner.nextLine().trim();
            
            if (checkCancel(idStr)) {
                System.out.println("Operation cancelled. Returning to Purchase Order menu.");
                return;
            }
            
            try {
                int orderId = Integer.parseInt(idStr);
                Optional<PurchaseOrder> orderOpt = purchaseOrderService.getPurchaseOrderByOrderId(orderId);
                if (orderOpt.isEmpty()) {
                    System.out.println("The record was not found. Please enter a valid order ID.");
                } else {
                    orderToEdit = orderOpt.get();
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid order ID. Please enter a number.");
            }
        }
        
        System.out.println("\n--- Edit Purchase Order #" + orderToEdit.getOrderId() + " ---");
        System.out.println("(Press Enter to keep current value, or enter 'cancel' to cancel)\n");
        
        try {
            LocalDate orderDate = readOrderDate(true, orderToEdit.getOrderDate(), scanner);
            if (orderDate == null) {
                System.out.println("Operation cancelled. Returning to Purchase Order menu.");
                return;
            }
            
            String vendor = readString("Enter Vendor", true, orderToEdit.getVendor(), true, scanner);
            if (vendor.equals("CANCEL_SIGNAL")) {
                System.out.println("Operation cancelled. Returning to Purchase Order menu.");
                return;
            }
            
            System.out.println("\n--- Edit Order Items ---");
            System.out.println("Current items:");
            List<OrderItem> currentItems = orderToEdit.getOrderItems();
            for (int i = 0; i < currentItems.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + currentItems.get(i));
            }
            
            System.out.print("Edit items? (yes/no) [no]: ");
            String editItemsResponse = scanner.nextLine().trim().toLowerCase();
            
            if (checkCancel(editItemsResponse)) {
                System.out.println("Operation cancelled. Returning to Purchase Order menu.");
                return;
            }
            
            List<OrderItem> orderItems;
            if (editItemsResponse.equals("yes") || editItemsResponse.equals("y")) {
                System.out.print("Edit individual items or re-enter all items? (individual/all) [all]: ");
                String editMode = scanner.nextLine().trim().toLowerCase();
                
                if (checkCancel(editMode)) {
                    System.out.println("Operation cancelled. Returning to Purchase Order menu.");
                    return;
                }
                
                if (editMode.equals("individual") || editMode.equals("i")) {
                    orderItems = editIndividualItems(currentItems, scanner);
                    if (orderItems == null) {
                        System.out.println("Operation cancelled. Returning to Purchase Order menu.");
                        return;
                    }
                } else {
                    orderItems = reenterAllItems(scanner);
                    if (orderItems == null) {
                        System.out.println("Operation cancelled. Returning to Purchase Order menu.");
                        return;
                    }
                }
            } else {
                orderItems = orderToEdit.getOrderItems();
            }
            
            String currentStatus = orderToEdit.getOrderStatus().toLowerCase();
            String orderStatus;
            
            if (currentStatus.equals("cancelled")) {
                System.out.println("\n--- Order Status ---");
                System.out.println("⚠️  WARNING: This order is CANCELLED and cannot be changed to pending or shipping status.");
                System.out.println("Cancelled orders are irreversible.");
                System.out.println("Status will remain as 'cancelled'.");
                orderStatus = currentStatus;
            } else if (currentStatus.equals("received") || currentStatus.equals("returned")) {
                System.out.println("\n--- Order Status ---");
                System.out.println("Note: Current status is '" + currentStatus + "', which cannot be changed here.");
                System.out.println("Status will remain as '" + currentStatus + "'.");
                System.out.println("To change status from '" + currentStatus + "', use:");
                if (currentStatus.equals("received")) {
                    System.out.println("  - 'Manage Purchase Return' to change to 'returned'");
                    System.out.println("  - 'Manage Goods Receive' → Delete to change back to 'shipping'");
                } else {
                    System.out.println("  - 'Manage Purchase Return' → Delete to change back to 'received'");
                }
                orderStatus = currentStatus;
            } else {
                System.out.println("\n--- Order Status ---");
                System.out.println("Current status: " + currentStatus);
                
                if (currentStatus.equals("pending")) {
                    System.out.println("Available options:");
                    System.out.println("  - shipping (reversible)");
                    System.out.println("  - cancelled (irreversible - requires confirmation)");
                } else if (currentStatus.equals("shipping")) {
                    System.out.println("Available options:");
                    System.out.println("  - pending (reversible)");
                    System.out.println("  - cancelled (irreversible - requires confirmation)");
                }
                
                System.out.print("\nEnter new status (or press Enter to keep current): ");
                String input = scanner.nextLine().trim().toLowerCase();
                
                if (checkCancel(input)) {
                    System.out.println("Operation cancelled. Returning to Purchase Order menu.");
                    return;
                }
                
                if (input.isEmpty()) {
                    orderStatus = currentStatus;
                } else {
                    boolean validTransition = false;
                    boolean requiresConfirmation = false;
                    
                    if (currentStatus.equals("pending")) {
                        if (input.equals("shipping")) {
                            validTransition = true;
                            requiresConfirmation = false;
                        } else if (input.equals("cancelled")) {
                            validTransition = true;
                            requiresConfirmation = true;
                        }
                    } else if (currentStatus.equals("shipping")) {
                        if (input.equals("pending")) {
                            validTransition = true;
                            requiresConfirmation = false;
                        } else if (input.equals("cancelled")) {
                            validTransition = true;
                            requiresConfirmation = true;
                        }
                    }
                    
                    if (!validTransition) {
                        System.out.println("Invalid status transition. Status must remain as '" + currentStatus + "'.");
                        orderStatus = currentStatus;
                    } else {
                        if (requiresConfirmation) {
                            System.out.print("⚠️  WARNING: This action is IRREVERSIBLE. Are you sure you want to cancel this order? (Y/N): ");
                            String confirm = scanner.nextLine().trim().toUpperCase();
                            if (confirm.equals("Y") || confirm.equals("YES")) {
                                orderStatus = input;
                                System.out.println("Order will be marked as cancelled.");
                            } else {
                                System.out.println("Cancellation cancelled. Status will remain as '" + currentStatus + "'.");
                                orderStatus = currentStatus;
                            }
                        } else {
                            orderStatus = input;
                        }
                    }
                }
            }
            
            PurchaseOrder updatedOrder = new PurchaseOrder();
            updatedOrder.setOrderId(orderToEdit.getOrderId());
            updatedOrder.setOrderDate(orderDate);
            updatedOrder.setOrderNumber(orderToEdit.getOrderNumber());
            updatedOrder.setVendor(vendor);
            updatedOrder.setOrderItems(orderItems);
            updatedOrder.setOrderStatus(orderStatus);
            
            Optional<PurchaseOrder> savedOpt = purchaseOrderService.getPurchaseOrderByOrderId(orderToEdit.getOrderId());
            if (savedOpt.isPresent()) {
                PurchaseOrder savedOrder = purchaseOrderService.updatePurchaseOrder(savedOpt.get().getId(), updatedOrder);
                
                if (savedOrder != null) {
                    System.out.println("\n✅ Purchase Order updated successfully!");
                    System.out.println("\n--- Updated Purchase Order Details ---");
                    System.out.println(savedOrder.toDisplayString());
                    System.out.printf("Total Price: $%.2f%n", savedOrder.getTotalPrice());
                } else {
                    System.out.println("Error updating purchase order.");
                }
            }
            
        } catch (Exception e) {
            System.out.println("Error editing purchase order: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void deletePurchaseOrder(Scanner scanner) {
        System.out.println("\n--- Delete Purchase Order ---");
        System.out.println("(Enter 'cancel' at any time to cancel and return to menu)");
        
        List<PurchaseOrder> orders = purchaseOrderService.getAllPurchaseOrders();
        if (orders.isEmpty()) {
            System.out.println("No purchase orders found.");
            return;
        }
        
        System.out.println("\n--- Available Purchase Orders ---");
        viewPurchaseOrders();
        
        PurchaseOrder orderToDelete = null;
        while (orderToDelete == null) {
            System.out.print("\nEnter Purchase Order ID to delete (or 'cancel' to cancel): ");
            String idStr = scanner.nextLine().trim();
            
            if (checkCancel(idStr)) {
                System.out.println("Operation cancelled. Returning to Purchase Order menu.");
                return;
            }
            
            try {
                int orderId = Integer.parseInt(idStr);
                Optional<PurchaseOrder> orderOpt = purchaseOrderService.getPurchaseOrderByOrderId(orderId);
                if (orderOpt.isEmpty()) {
                    System.out.println("Order ID not found. Please enter a valid ID.");
                } else {
                    orderToDelete = orderOpt.get();
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid order ID. Please enter a number.");
            }
        }
        
        System.out.println("\n--- Purchase Order Details ---");
        System.out.println(orderToDelete.toDisplayString());
        System.out.print("\nDo you want to delete this purchase order: {" + orderToDelete.getOrderId() + "} [Y/N/Cancel]? ");
        String confirm = scanner.nextLine().trim().toUpperCase();
        
        if (checkCancel(confirm) || confirm.equals("CANCEL")) {
            System.out.println("Delete action cancelled.");
            return;
        }
        
        if (confirm.equals("Y") || confirm.equals("YES")) {
            boolean deleted = purchaseOrderService.deletePurchaseOrderByOrderId(orderToDelete.getOrderId());
            if (deleted) {
                System.out.println("✅ Order deleted successfully!");
                System.out.println("\n--- Updated Purchase Order List ---");
                viewPurchaseOrders();
            } else {
                System.out.println("Error deleting order.");
            }
        } else {
            System.out.println("Delete action cancelled.");
        }
    }
    
    private void viewPurchaseReports(Scanner scanner) {
        System.out.println("\n--- View Purchase Reports ---");
        
        List<PurchaseOrder> orders = purchaseOrderService.getAllPurchaseOrders();
        
        if (orders.isEmpty()) {
            System.out.println("(No purchase orders found)");
            return;
        }
        
        List<PurchaseOrder> currentOrders = new ArrayList<>(orders);
        currentOrders.sort((o1, o2) -> Integer.compare(o1.getOrderId(), o2.getOrderId()));
        
        displayFullReport(currentOrders);
        
        while (true) {
            List<PurchaseOrder> filteredAndSorted = askForSortingAndFiltering(currentOrders, scanner);
            if (filteredAndSorted == null) {
                return;
            }
            
            currentOrders = filteredAndSorted;
            displayFullReport(filteredAndSorted);
        }
    }
    
    private List<OrderItem> editIndividualItems(List<OrderItem> currentItems, Scanner scanner) {
        List<OrderItem> orderItems = new ArrayList<>(currentItems);
        
        System.out.println("\n--- Edit Individual Items ---");
        System.out.println("(Enter 'done' to finish editing, 'cancel' to cancel)");
        
        while (true) {
            System.out.print("\nEnter item name to edit: ");
            String itemNameToEdit = scanner.nextLine().trim();
            
            if (checkCancel(itemNameToEdit)) {
                return null;
            }
            
            if (itemNameToEdit.equalsIgnoreCase("done")) {
                break;
            }
            
            if (itemNameToEdit.isEmpty()) {
                System.out.println("Item name cannot be empty.");
                continue;
            }
            
            OrderItem itemToEdit = null;
            int itemIndex = -1;
            for (int i = 0; i < orderItems.size(); i++) {
                if (orderItems.get(i).getItemName().equalsIgnoreCase(itemNameToEdit)) {
                    itemToEdit = orderItems.get(i);
                    itemIndex = i;
                    break;
                }
            }
            
            if (itemToEdit == null) {
                System.out.println("Item not found. Please enter a valid item name.");
                continue;
            }
            
            System.out.println("\nEditing item: " + itemToEdit);
            System.out.println("(Press Enter to keep current value, or enter 'cancel' to cancel)");
            
            String newItemName = readString("  Item Name/Description", true, itemToEdit.getItemName(), true, scanner);
            if (newItemName.equals("CANCEL_SIGNAL")) {
                return null;
            }
            
            System.out.print("  Quantity [" + itemToEdit.getQuantity() + "]: ");
            String qtyStr = scanner.nextLine().trim();
            int newQuantity;
            if (checkCancel(qtyStr)) {
                return null;
            }
            if (qtyStr.isEmpty()) {
                newQuantity = itemToEdit.getQuantity();
            } else {
                try {
                    newQuantity = Integer.parseInt(qtyStr);
                    if (newQuantity <= 0) {
                        System.out.println("Quantity must be greater than 0. Keeping current value.");
                        newQuantity = itemToEdit.getQuantity();
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid quantity. Keeping current value.");
                    newQuantity = itemToEdit.getQuantity();
                }
            }
            
            System.out.print("  Price per Item [$" + itemToEdit.getPricePerItem() + "]: ");
            String priceStr = scanner.nextLine().trim();
            double newPrice;
            if (checkCancel(priceStr)) {
                return null;
            }
            if (priceStr.isEmpty()) {
                newPrice = itemToEdit.getPricePerItem();
            } else {
                try {
                    newPrice = Double.parseDouble(priceStr);
                    if (newPrice <= 0) {
                        System.out.println("Price must be greater than 0. Keeping current value.");
                        newPrice = itemToEdit.getPricePerItem();
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid price. Keeping current value.");
                    newPrice = itemToEdit.getPricePerItem();
                }
            }
            
            OrderItem updatedItem = new OrderItem(newItemName, newQuantity, newPrice);
            orderItems.set(itemIndex, updatedItem);
            System.out.printf("  ✅ Updated: %s x %d @ $%.2f each = $%.2f total%n",
                newItemName, newQuantity, newPrice, updatedItem.getTotalPrice());
        }
        
        return orderItems;
    }
    
    private List<OrderItem> reenterAllItems(Scanner scanner) {
        List<OrderItem> orderItems = new ArrayList<>();
        System.out.println("\n--- Re-enter All Order Items (Enter 'done' when finished, 'cancel' to cancel) ---");
        boolean addingItems = true;
        int itemCount = 1;
        
        while (addingItems) {
            System.out.println("\nItem #" + itemCount + ":");
            System.out.print("  Item Name/Description: ");
            String itemName = scanner.nextLine().trim();
            
            if (checkCancel(itemName)) {
                return null;
            }
            
            if (itemName.equalsIgnoreCase("done")) {
                if (orderItems.isEmpty()) {
                    System.out.println("Please add at least one item. Type item name to continue or 'cancel' to cancel.");
                    continue;
                }
                addingItems = false;
                break;
            }
            
            if (itemName.isEmpty()) {
                System.out.println("Item name cannot be empty. Please try again or enter 'cancel' to cancel.");
                continue;
            }
            
            int quantity = readInteger("  Quantity", true, scanner);
            if (quantity == -1) {
                return null;
            }
            
            double pricePerItem = readDouble("  Price per Item", true, scanner);
            if (pricePerItem == -1) {
                return null;
            }
            
            OrderItem item = new OrderItem(itemName, quantity, pricePerItem);
            orderItems.add(item);
            System.out.printf("  ✅ Added: %s x %d @ $%.2f each = $%.2f total%n",
                itemName, quantity, pricePerItem, item.getTotalPrice());
            itemCount++;
        }
        
        return orderItems;
    }
    
    private List<PurchaseOrder> askForSortingAndFiltering(List<PurchaseOrder> orders, Scanner scanner) {
        List<PurchaseOrder> result = new ArrayList<>(orders);
        
        System.out.println("\n--- Sorting and Filtering Options ---");
        System.out.println("1. Filter by Status");
        System.out.println("2. Sort by Order ID");
        System.out.println("3. Sort by Order Date");
        System.out.println("4. Sort by Vendor");
        System.out.println("5. Sort by Total Price");
        System.out.println("6. Exit to Purchase Order Menu");
        System.out.print("Select an option (1-6): ");
        
        String option = scanner.nextLine().trim();
        
        if (!isValidMenuInput(option, 1, 6)) {
            System.out.println("Invalid option. Please enter a number between 1 and 6.");
            return result;
        }
        
        int choice = Integer.parseInt(option);
        
        switch (choice) {
            case 1:
                result = filterByStatus(result, scanner);
                break;
            case 2:
                result = applySorting(result, "orderId", scanner);
                break;
            case 3:
                result = applySorting(result, "orderDate", scanner);
                break;
            case 4:
                result = applySorting(result, "vendor", scanner);
                break;
            case 5:
                result = applySorting(result, "totalPrice", scanner);
                break;
            case 6:
                return null;
        }
        
        return result;
    }
    
    private List<PurchaseOrder> filterByStatus(List<PurchaseOrder> orders, Scanner scanner) {
        List<String> selectedStatuses = new ArrayList<>();
        
        System.out.println("\n--- Filter by Status ---");
        System.out.println("Available statuses: pending, shipping, received, cancelled, returned");
        System.out.println("You can enter multiple statuses, one per line");
        System.out.println("Enter 'done' when finished, or 'cancel' to return:");
        
        while (true) {
            System.out.print("Enter status (or 'done' to finish, 'cancel' to return): ");
            String status = scanner.nextLine().trim().toLowerCase();
            
            if (checkCancel(status)) {
                return orders;
            }
            
            if (status.equals("done")) {
                if (selectedStatuses.isEmpty()) {
                    System.out.println("No status selected. Please enter at least one status, 'done' to finish, or 'cancel' to return.");
                    continue;
                }
                break;
            }
            
            if (status.isEmpty()) {
                System.out.println("Status cannot be empty. Please enter a valid status, 'done', or 'cancel'.");
                continue;
            }
            
            if (VALID_STATUSES.contains(status)) {
                if (!selectedStatuses.contains(status)) {
                    selectedStatuses.add(status);
                    System.out.println("✅ Added status: " + status);
                } else {
                    System.out.println("Status already selected. Please enter another status or 'done'.");
                }
            } else {
                System.out.println("Invalid status. Please enter one of: pending, shipping, received, cancelled, returned, 'done', or 'cancel'.");
            }
        }
        
        List<PurchaseOrder> filtered = orders.stream()
            .filter(order -> selectedStatuses.contains(order.getOrderStatus().toLowerCase()))
            .collect(Collectors.toList());
        
        if (filtered.isEmpty()) {
            System.out.println("No orders found with selected status(es): " + String.join(", ", selectedStatuses));
            return filtered;
        }
        
        System.out.println("✅ Filtered by status(es): " + String.join(", ", selectedStatuses) + 
            " (" + filtered.size() + " order(s))");
        return filtered;
    }
    
    private List<PurchaseOrder> applySorting(List<PurchaseOrder> orders, String sortField, Scanner scanner) {
        while (true) {
            System.out.print("\nSort order - A: Ascending, D: Descending, or 'cancel' to return (A/D/cancel): ");
            String sortOrder = scanner.nextLine().trim();
            
            if (checkCancel(sortOrder)) {
                return orders;
            }
            
            sortOrder = sortOrder.toUpperCase();
            
            if (sortOrder.equals("A") || sortOrder.equals("D")) {
                boolean ascending = sortOrder.equals("A");
                List<PurchaseOrder> sorted = new ArrayList<>(orders);
                
                switch (sortField) {
                    case "orderId":
                        sorted.sort((o1, o2) -> ascending 
                            ? Integer.compare(o1.getOrderId(), o2.getOrderId())
                            : Integer.compare(o2.getOrderId(), o1.getOrderId()));
                        System.out.println("Sorted by Order ID (" + (ascending ? "ascending" : "descending") + ")");
                        break;
                    case "orderDate":
                        sorted.sort((o1, o2) -> {
                            if (o1.getOrderDate() == null) return ascending ? -1 : 1;
                            if (o2.getOrderDate() == null) return ascending ? 1 : -1;
                            return ascending
                                ? o1.getOrderDate().compareTo(o2.getOrderDate())
                                : o2.getOrderDate().compareTo(o1.getOrderDate());
                        });
                        System.out.println("Sorted by Order Date (" + (ascending ? "ascending" : "descending") + ")");
                        break;
                    case "vendor":
                        sorted.sort((o1, o2) -> ascending
                            ? o1.getVendor().compareToIgnoreCase(o2.getVendor())
                            : o2.getVendor().compareToIgnoreCase(o1.getVendor()));
                        System.out.println("Sorted by Vendor (" + (ascending ? "ascending" : "descending") + ")");
                        break;
                    case "totalPrice":
                        sorted.sort((o1, o2) -> ascending
                            ? Double.compare(o1.getTotalPrice(), o2.getTotalPrice())
                            : Double.compare(o2.getTotalPrice(), o1.getTotalPrice()));
                        System.out.println("Sorted by Total Price (" + (ascending ? "ascending" : "descending") + ")");
                        break;
                }
                
                return sorted;
            } else {
                System.out.println("Invalid input. Please enter A for Ascending, D for Descending, or 'cancel' to return.");
            }
        }
    }
    
    private void displayFullReport(List<PurchaseOrder> orders) {
        System.out.println("\n==========================================================================================================");
        System.out.println("                          PURCHASE ORDER REPORT - DETAILED VIEW");
        System.out.println("==========================================================================================================");
        
        for (PurchaseOrder order : orders) {
            System.out.println("\n" + "=".repeat(110));
            System.out.printf("Order ID: %-8d | Order Number: %-15s | Date: %-12s | Vendor: %-25s | Status: %-15s%n",
                order.getOrderId(),
                order.getOrderNumber(),
                order.getOrderDate() != null ? order.getOrderDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "N/A",
                order.getVendor(),
                order.getOrderStatus());
            System.out.println("=".repeat(110));
            
            System.out.println("\nDate Information:");
            System.out.println("-".repeat(110));
            System.out.printf("%-20s: %-15s | %-20s: %-15s%n",
                "Order Date",
                order.getOrderDate() != null ? order.getOrderDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "N/A",
                "Shipping Date",
                order.getShippingDate() != null ? order.getShippingDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "N/A");
            System.out.printf("%-20s: %-15s | %-20s: %-15s%n",
                "Received Date",
                order.getReceivedDate() != null ? order.getReceivedDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "N/A",
                "Returned Date",
                order.getReturnedDate() != null ? order.getReturnedDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "N/A");
            System.out.printf("%-20s: %-15s%n",
                "Cancelled Date",
                order.getCancelledDate() != null ? order.getCancelledDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "N/A");
            System.out.println("-".repeat(110));
            
            List<OrderItem> items = order.getOrderItems();
            if (items.isEmpty()) {
                System.out.println("No items in this order.");
            } else {
                System.out.println("\nItem Details:");
                System.out.println("-".repeat(110));
                System.out.printf("%-5s | %-30s | %-12s | %-15s | %-15s | %-15s%n",
                    "No.", "Item Name/Description", "Quantity", "Price per Item", "Item Total", "Notes");
                System.out.println("-".repeat(110));
                
                for (int i = 0; i < items.size(); i++) {
                    OrderItem item = items.get(i);
                    System.out.printf("%-5d | %-30s | %-12d | $%-14.2f | $%-14.2f | %-15s%n",
                        i + 1,
                        item.getItemName(),
                        item.getQuantity(),
                        item.getPricePerItem(),
                        item.getTotalPrice(),
                        "-");
                }
                System.out.println("-".repeat(110));
            }
            
            System.out.printf("\n%85s: $%.2f%n", "Total Order Amount", order.getTotalPrice());
            System.out.println("=".repeat(110));
        }
        
        System.out.println("\n" + "=".repeat(110));
        System.out.println("REPORT SUMMARY");
        System.out.println("=".repeat(110));
        System.out.printf("Total Orders: %d%n", orders.size());
        double totalValue = orders.stream().mapToDouble(PurchaseOrder::getTotalPrice).sum();
        System.out.printf("Total Value: $%.2f%n", totalValue);
        System.out.println("=".repeat(110));
    }
    
    // =================== GOODS RECEIVE SUB-MENU ===================
    
    private void showGoodsReceiveSubMenu(Scanner scanner) {
        boolean stay = true;
        while (stay) {
            System.out.println("\n========================================");
            System.out.println("        GOODS RECEIVE MANAGEMENT        ");
            System.out.println("========================================");
            System.out.println("1. View Goods Receive");
            System.out.println("2. Add Goods Receive");
            System.out.println("3. Edit Goods Receive");
            System.out.println("4. Delete Goods Receive");
            System.out.println("5. Back");
            System.out.print("Select an option (1-5): ");
            
            String input = scanner.nextLine().trim();
            
            if (!isValidMenuInput(input, 1, 5)) {
                System.out.println("Invalid option. Please enter a number between 1 and 5.");
                continue;
            }
            
            switch (input) {
                case "1":
                    viewGoodsReceive();
                    break;
                case "2":
                    addGoodsReceive(scanner);
                    break;
                case "3":
                    editGoodsReceive(scanner);
                    break;
                case "4":
                    deleteGoodsReceive(scanner);
                    break;
                case "5":
                    stay = false;
                    break;
            }
        }
    }
    
    private void viewGoodsReceive() {
        List<PurchaseOrder> receivedOrders = purchaseOrderService.getAllPurchaseOrders().stream()
            .filter(order -> order.getOrderStatus().equalsIgnoreCase("received"))
            .collect(Collectors.toList());
        
        System.out.println("\n--- Goods Receive List ---");
        if (receivedOrders.isEmpty()) {
            System.out.println("(No goods receive records found)");
            return;
        }
        
        System.out.println("------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-12s | %-8s | %-15s | %-12s | %-20s | %-15s%n", 
            "Receive Date", "Order ID", "Order Number", "Order Date", "Vendor", "Order Status");
        System.out.println("------------------------------------------------------------------------------------------------------------------------");
        
        for (PurchaseOrder order : receivedOrders) {
            String receiveDate = order.getReceivedDate() != null 
                ? order.getReceivedDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
                : "N/A";
            
            System.out.printf("%-12s | %-8d | %-15s | %-12s | %-20s | %-15s%n",
                receiveDate,
                order.getOrderId(),
                order.getOrderNumber(),
                order.getOrderDate() != null ? order.getOrderDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "N/A",
                order.getVendor(),
                order.getOrderStatus());
        }
        System.out.println("------------------------------------------------------------------------------------------------------------------------");
    }
    
    private void addGoodsReceive(Scanner scanner) {
        System.out.println("\n--- Add Goods Receive ---");
        System.out.println("(Enter 'cancel' at any time to cancel and return to menu)");
        
        List<PurchaseOrder> shippingOrders = purchaseOrderService.getAllPurchaseOrders().stream()
            .filter(order -> order.getOrderStatus().equalsIgnoreCase("shipping"))
            .collect(Collectors.toList());
        
        if (shippingOrders.isEmpty()) {
            System.out.println("No purchase orders with 'shipping' status found.");
            return;
        }
        
        System.out.println("\n--- Purchase Orders in Shipping Status ---");
        System.out.println("--------------------------------------------------------------------------------------------------------");
        System.out.printf("%-8s | %-15s | %-12s | %-20s | %-15s | %-12s%n", 
            "Order ID", "Order Number", "Order Date", "Vendor", "Status", "Total Price");
        System.out.println("--------------------------------------------------------------------------------------------------------");
        
        for (PurchaseOrder order : shippingOrders) {
            System.out.printf("%-8d | %-15s | %-12s | %-20s | %-15s | $%-11.2f%n",
                order.getOrderId(),
                order.getOrderNumber(),
                order.getOrderDate() != null ? order.getOrderDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "N/A",
                order.getVendor(),
                order.getOrderStatus(),
                order.getTotalPrice());
        }
        System.out.println("--------------------------------------------------------------------------------------------------------");
        
        PurchaseOrder orderToReceive = null;
        while (orderToReceive == null) {
            System.out.print("\nEnter Order ID to receive (or 'cancel' to cancel): ");
            String idStr = scanner.nextLine().trim();
            
            if (checkCancel(idStr)) {
                System.out.println("Operation cancelled. Returning to Goods Receive menu.");
                return;
            }
            
            try {
                int orderId = Integer.parseInt(idStr);
                Optional<PurchaseOrder> orderOpt = purchaseOrderService.getPurchaseOrderByOrderId(orderId);
                
                if (orderOpt.isEmpty()) {
                    System.out.println("Order ID not found. Please enter a valid order ID.");
                } else if (!orderOpt.get().getOrderStatus().equalsIgnoreCase("shipping")) {
                    System.out.println("Error: Order ID " + orderId + " is not in 'shipping' status. Only orders with 'shipping' status can be received.");
                    System.out.println("Current status: " + orderOpt.get().getOrderStatus());
                } else {
                    orderToReceive = orderOpt.get();
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid order ID. Please enter a number.");
            }
        }
        
        System.out.println("\n--- Purchase Order Details ---");
        System.out.println(orderToReceive.toDisplayString());
        System.out.print("\nDo you want to receive this order? (Y/N): ");
        String confirm = scanner.nextLine().trim().toUpperCase();
        
        if (confirm.equals("Y") || confirm.equals("YES")) {
            PurchaseOrder updatedOrder = new PurchaseOrder();
            updatedOrder.setOrderId(orderToReceive.getOrderId());
            updatedOrder.setOrderDate(orderToReceive.getOrderDate());
            updatedOrder.setOrderNumber(orderToReceive.getOrderNumber());
            updatedOrder.setVendor(orderToReceive.getVendor());
            updatedOrder.setOrderItems(orderToReceive.getOrderItems());
            updatedOrder.setOrderStatus("received");
            
            Optional<PurchaseOrder> savedOpt = purchaseOrderService.getPurchaseOrderByOrderId(orderToReceive.getOrderId());
            if (savedOpt.isPresent()) {
                PurchaseOrder savedOrder = purchaseOrderService.updatePurchaseOrder(savedOpt.get().getId(), updatedOrder);
                
                if (savedOrder != null) {
                    System.out.println("✅ Goods receive record created successfully!");
                    System.out.println("Order status changed from 'shipping' to 'received'.");
                    System.out.println("Received date: " + savedOrder.getReceivedDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
                    
                    System.out.println("\n--- Updated Goods Receive List ---");
                    viewGoodsReceive();
                } else {
                    System.out.println("Error creating goods receive record.");
                }
            }
        } else {
            System.out.println("Operation cancelled.");
        }
    }
    
    private void editGoodsReceive(Scanner scanner) {
        System.out.println("\n--- Edit Goods Receive ---");
        System.out.println("(Enter 'cancel' at any time to cancel and return to menu)");
        
        List<PurchaseOrder> receivedOrders = purchaseOrderService.getAllPurchaseOrders().stream()
            .filter(order -> order.getOrderStatus().equalsIgnoreCase("received"))
            .collect(Collectors.toList());
        
        if (receivedOrders.isEmpty()) {
            System.out.println("No goods receive records found.");
            return;
        }
        
        System.out.println("\n--- Goods Receive List ---");
        System.out.println("------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-12s | %-8s | %-15s | %-12s | %-20s | %-15s%n", 
            "Receive Date", "Order ID", "Order Number", "Order Date", "Vendor", "Order Status");
        System.out.println("------------------------------------------------------------------------------------------------------------------------");
        
        for (PurchaseOrder order : receivedOrders) {
            String receiveDate = order.getReceivedDate() != null 
                ? order.getReceivedDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
                : "N/A";
            
            System.out.printf("%-12s | %-8d | %-15s | %-12s | %-20s | %-15s%n",
                receiveDate,
                order.getOrderId(),
                order.getOrderNumber(),
                order.getOrderDate() != null ? order.getOrderDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "N/A",
                order.getVendor(),
                order.getOrderStatus());
        }
        System.out.println("------------------------------------------------------------------------------------------------------------------------");
        
        PurchaseOrder orderToEdit = null;
        while (orderToEdit == null) {
            System.out.print("\nEnter Order ID to edit (or 'cancel' to cancel): ");
            String idStr = scanner.nextLine().trim();
            
            if (checkCancel(idStr)) {
                System.out.println("Operation cancelled. Returning to Goods Receive menu.");
                return;
            }
            
            try {
                int orderId = Integer.parseInt(idStr);
                Optional<PurchaseOrder> orderOpt = purchaseOrderService.getPurchaseOrderByOrderId(orderId);
                
                if (orderOpt.isEmpty()) {
                    System.out.println("Order ID not found. Please enter a valid order ID.");
                } else if (!orderOpt.get().getOrderStatus().equalsIgnoreCase("received")) {
                    System.out.println("Error: Order ID " + orderId + " is not in 'received' status. Only orders with 'received' status can be edited here.");
                    System.out.println("Current status: " + orderOpt.get().getOrderStatus());
                } else {
                    orderToEdit = orderOpt.get();
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid order ID. Please enter a number.");
            }
        }
        
        System.out.println("\n--- Edit Purchase Order #" + orderToEdit.getOrderId() + " ---");
        System.out.println("(Press Enter to keep current value, or enter 'cancel' to cancel)");
        System.out.println("Note: Status will remain as 'received' and cannot be edited here.\n");
        
        try {
            LocalDate orderDate = readOrderDate(true, orderToEdit.getOrderDate(), scanner);
            if (orderDate == null) {
                System.out.println("Operation cancelled. Returning to Goods Receive menu.");
                return;
            }
            
            String vendor = readString("Enter Vendor", true, orderToEdit.getVendor(), true, scanner);
            if (vendor.equals("CANCEL_SIGNAL")) {
                System.out.println("Operation cancelled. Returning to Goods Receive menu.");
                return;
            }
            
            System.out.println("\n--- Edit Order Items ---");
            System.out.println("Current items:");
            List<OrderItem> currentItems = orderToEdit.getOrderItems();
            for (int i = 0; i < currentItems.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + currentItems.get(i));
            }
            
            System.out.print("Edit items? (yes/no) [no]: ");
            String editItemsResponse = scanner.nextLine().trim().toLowerCase();
            
            if (checkCancel(editItemsResponse)) {
                System.out.println("Operation cancelled. Returning to Goods Receive menu.");
                return;
            }
            
            List<OrderItem> orderItems;
            if (editItemsResponse.equals("yes") || editItemsResponse.equals("y")) {
                System.out.print("Edit individual items or re-enter all items? (individual/all) [all]: ");
                String editMode = scanner.nextLine().trim().toLowerCase();
                
                if (checkCancel(editMode)) {
                    System.out.println("Operation cancelled. Returning to Goods Receive menu.");
                    return;
                }
                
                if (editMode.equals("individual") || editMode.equals("i")) {
                    orderItems = editIndividualItems(currentItems, scanner);
                    if (orderItems == null) {
                        System.out.println("Operation cancelled. Returning to Goods Receive menu.");
                        return;
                    }
                } else {
                    orderItems = reenterAllItems(scanner);
                    if (orderItems == null) {
                        System.out.println("Operation cancelled. Returning to Goods Receive menu.");
                        return;
                    }
                }
            } else {
                orderItems = orderToEdit.getOrderItems();
            }
            
            PurchaseOrder updatedOrder = new PurchaseOrder();
            updatedOrder.setOrderId(orderToEdit.getOrderId());
            updatedOrder.setOrderDate(orderDate);
            updatedOrder.setOrderNumber(orderToEdit.getOrderNumber());
            updatedOrder.setVendor(vendor);
            updatedOrder.setOrderItems(orderItems);
            updatedOrder.setOrderStatus("received");
            
            Optional<PurchaseOrder> savedOpt = purchaseOrderService.getPurchaseOrderByOrderId(orderToEdit.getOrderId());
            if (savedOpt.isPresent()) {
                PurchaseOrder savedOrder = purchaseOrderService.updatePurchaseOrder(savedOpt.get().getId(), updatedOrder);
                
                if (savedOrder != null) {
                    System.out.println("\n✅ Purchase Order updated successfully!");
                    System.out.println("\n--- Updated Purchase Order Details ---");
                    System.out.println(savedOrder.toDisplayString());
                    
                    System.out.println("\n--- Updated Goods Receive List ---");
                    viewGoodsReceive();
                } else {
                    System.out.println("Error updating purchase order.");
                }
            }
            
        } catch (Exception e) {
            System.out.println("Error editing goods receive: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void deleteGoodsReceive(Scanner scanner) {
        System.out.println("\n--- Delete Goods Receive ---");
        System.out.println("(Enter 'cancel' at any time to cancel and return to menu)");
        
        List<PurchaseOrder> receivedOrders = purchaseOrderService.getAllPurchaseOrders().stream()
            .filter(order -> order.getOrderStatus().equalsIgnoreCase("received"))
            .collect(Collectors.toList());
        
        if (receivedOrders.isEmpty()) {
            System.out.println("No goods receive records found.");
            return;
        }
        
        System.out.println("\n--- Goods Receive List ---");
        System.out.println("------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-12s | %-8s | %-15s | %-12s | %-20s | %-15s%n", 
            "Receive Date", "Order ID", "Order Number", "Order Date", "Vendor", "Order Status");
        System.out.println("------------------------------------------------------------------------------------------------------------------------");
        
        for (PurchaseOrder order : receivedOrders) {
            String receiveDate = order.getReceivedDate() != null 
                ? order.getReceivedDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
                : "N/A";
            
            System.out.printf("%-12s | %-8d | %-15s | %-12s | %-20s | %-15s%n",
                receiveDate,
                order.getOrderId(),
                order.getOrderNumber(),
                order.getOrderDate() != null ? order.getOrderDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "N/A",
                order.getVendor(),
                order.getOrderStatus());
        }
        System.out.println("------------------------------------------------------------------------------------------------------------------------");
        
        PurchaseOrder orderToDelete = null;
        while (orderToDelete == null) {
            System.out.print("\nEnter Order ID to delete (or 'cancel' to cancel): ");
            String idStr = scanner.nextLine().trim();
            
            if (checkCancel(idStr)) {
                System.out.println("Operation cancelled. Returning to Goods Receive menu.");
                return;
            }
            
            try {
                int orderId = Integer.parseInt(idStr);
                Optional<PurchaseOrder> orderOpt = purchaseOrderService.getPurchaseOrderByOrderId(orderId);
                
                if (orderOpt.isEmpty()) {
                    System.out.println("Order ID not found. Please enter a valid order ID.");
                } else if (!orderOpt.get().getOrderStatus().equalsIgnoreCase("received")) {
                    System.out.println("Error: Order ID " + orderId + " is not in 'received' status. Only orders with 'received' status can be deleted here.");
                    System.out.println("Current status: " + orderOpt.get().getOrderStatus());
                } else {
                    orderToDelete = orderOpt.get();
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid order ID. Please enter a number.");
            }
        }
        
        System.out.println("\n--- Purchase Order Details ---");
        System.out.println(orderToDelete.toDisplayString());
        System.out.print("\nDo you want to delete this goods receive record? (Y/N): ");
        System.out.println("(Note: This will change the order status from 'received' back to 'shipping')");
        
        String confirm = scanner.nextLine().trim().toUpperCase();
        
        if (confirm.equals("Y") || confirm.equals("YES")) {
            PurchaseOrder updatedOrder = new PurchaseOrder();
            updatedOrder.setOrderId(orderToDelete.getOrderId());
            updatedOrder.setOrderDate(orderToDelete.getOrderDate());
            updatedOrder.setOrderNumber(orderToDelete.getOrderNumber());
            updatedOrder.setVendor(orderToDelete.getVendor());
            updatedOrder.setOrderItems(orderToDelete.getOrderItems());
            updatedOrder.setOrderStatus("shipping");
            
            Optional<PurchaseOrder> savedOpt = purchaseOrderService.getPurchaseOrderByOrderId(orderToDelete.getOrderId());
            if (savedOpt.isPresent()) {
                PurchaseOrder savedOrder = purchaseOrderService.updatePurchaseOrder(savedOpt.get().getId(), updatedOrder);
                
                if (savedOrder != null) {
                    System.out.println("✅ Goods receive record deleted successfully!");
                    System.out.println("Order status changed from 'received' to 'shipping'.");
                    
                    System.out.println("\n--- Updated Goods Receive List ---");
                    viewGoodsReceive();
                } else {
                    System.out.println("Error deleting goods receive record.");
                }
            }
        } else {
            System.out.println("Delete action cancelled.");
        }
    }
    
    // =================== PURCHASE RETURN SUB-MENU ===================
    
    private void showPurchaseReturnSubMenu(Scanner scanner) {
        boolean stay = true;
        while (stay) {
            System.out.println("\n========================================");
            System.out.println("      PURCHASE RETURN MANAGEMENT        ");
            System.out.println("========================================");
            System.out.println("1. View Purchase Return");
            System.out.println("2. Add Purchase Return");
            System.out.println("3. Edit Purchase Return");
            System.out.println("4. Delete Purchase Return");
            System.out.println("5. Back");
            System.out.print("Select an option (1-5): ");
            
            String input = scanner.nextLine().trim();
            
            if (!isValidMenuInput(input, 1, 5)) {
                System.out.println("Invalid option. Please enter a number between 1 and 5.");
                continue;
            }
            
            switch (input) {
                case "1":
                    viewPurchaseReturn();
                    break;
                case "2":
                    addPurchaseReturn(scanner);
                    break;
                case "3":
                    editPurchaseReturn(scanner);
                    break;
                case "4":
                    deletePurchaseReturn(scanner);
                    break;
                case "5":
                    stay = false;
                    break;
            }
        }
    }
    
    private void viewPurchaseReturn() {
        List<PurchaseOrder> returnedOrders = purchaseOrderService.getAllPurchaseOrders().stream()
            .filter(order -> order.getOrderStatus().equalsIgnoreCase("returned"))
            .collect(Collectors.toList());
        
        System.out.println("\n--- Purchase Return List ---");
        if (returnedOrders.isEmpty()) {
            System.out.println("(No purchase return records found)");
            return;
        }
        
        System.out.println("------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-12s | %-12s | %-15s | %-20s | %-15s%n", 
            "Return Date", "Receive Date", "Order Number", "Vendor", "Order Status");
        System.out.println("------------------------------------------------------------------------------------------------------------------------");
        
        for (PurchaseOrder order : returnedOrders) {
            String returnDate = order.getReturnedDate() != null 
                ? order.getReturnedDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
                : "N/A";
            String receiveDate = order.getReceivedDate() != null 
                ? order.getReceivedDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
                : "N/A";
            
            System.out.printf("%-12s | %-12s | %-15s | %-20s | %-15s%n",
                returnDate,
                receiveDate,
                order.getOrderNumber(),
                order.getVendor(),
                order.getOrderStatus());
        }
        System.out.println("------------------------------------------------------------------------------------------------------------------------");
    }
    
    private void addPurchaseReturn(Scanner scanner) {
        System.out.println("\n--- Add Purchase Return ---");
        System.out.println("(Enter 'cancel' at any time to cancel and return to menu)");
        
        List<PurchaseOrder> receivedOrders = purchaseOrderService.getAllPurchaseOrders().stream()
            .filter(order -> order.getOrderStatus().equalsIgnoreCase("received"))
            .collect(Collectors.toList());
        
        if (receivedOrders.isEmpty()) {
            System.out.println("No purchase orders with 'received' status found.");
            return;
        }
        
        System.out.println("\n--- Purchase Orders in Received Status ---");
        System.out.println("------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-8s | %-15s | %-12s | %-20s | %-15s | %-12s%n", 
            "Order ID", "Order Number", "Order Date", "Vendor", "Status", "Total Price");
        System.out.println("------------------------------------------------------------------------------------------------------------------------");
        
        for (PurchaseOrder order : receivedOrders) {
            System.out.printf("%-8d | %-15s | %-12s | %-20s | %-15s | $%-11.2f%n",
                order.getOrderId(),
                order.getOrderNumber(),
                order.getOrderDate() != null ? order.getOrderDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "N/A",
                order.getVendor(),
                order.getOrderStatus(),
                order.getTotalPrice());
        }
        System.out.println("------------------------------------------------------------------------------------------------------------------------");
        
        PurchaseOrder orderToReturn = null;
        while (orderToReturn == null) {
            System.out.print("\nEnter Order ID to return (or 'cancel' to cancel): ");
            String idStr = scanner.nextLine().trim();
            
            if (checkCancel(idStr)) {
                System.out.println("Operation cancelled. Returning to Purchase Return menu.");
                return;
            }
            
            try {
                int orderId = Integer.parseInt(idStr);
                Optional<PurchaseOrder> orderOpt = purchaseOrderService.getPurchaseOrderByOrderId(orderId);
                
                if (orderOpt.isEmpty()) {
                    System.out.println("Order ID not found. Please enter a valid order ID.");
                } else if (!orderOpt.get().getOrderStatus().equalsIgnoreCase("received")) {
                    System.out.println("Error: Order ID " + orderId + " is not in 'received' status. Only orders with 'received' status can be returned.");
                    System.out.println("Current status: " + orderOpt.get().getOrderStatus());
                } else {
                    orderToReturn = orderOpt.get();
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid order ID. Please enter a number.");
            }
        }
        
        System.out.println("\n--- Purchase Order Details ---");
        System.out.println(orderToReturn.toDisplayString());
        System.out.print("\nDo you want to return this order? (Y/N): ");
        System.out.println("(Note: This will change the order status from 'received' to 'returned')");
        
        String confirm = scanner.nextLine().trim().toUpperCase();
        
        if (confirm.equals("Y") || confirm.equals("YES")) {
            PurchaseOrder updatedOrder = new PurchaseOrder();
            updatedOrder.setOrderId(orderToReturn.getOrderId());
            updatedOrder.setOrderDate(orderToReturn.getOrderDate());
            updatedOrder.setOrderNumber(orderToReturn.getOrderNumber());
            updatedOrder.setVendor(orderToReturn.getVendor());
            updatedOrder.setOrderItems(orderToReturn.getOrderItems());
            updatedOrder.setOrderStatus("returned");
            updatedOrder.setReceivedDate(orderToReturn.getReceivedDate());
            
            Optional<PurchaseOrder> savedOpt = purchaseOrderService.getPurchaseOrderByOrderId(orderToReturn.getOrderId());
            if (savedOpt.isPresent()) {
                PurchaseOrder savedOrder = purchaseOrderService.updatePurchaseOrder(savedOpt.get().getId(), updatedOrder);
                
                if (savedOrder != null) {
                    System.out.println("✅ Purchase return record created successfully!");
                    System.out.println("Order status changed from 'received' to 'returned'.");
                    System.out.println("Returned date: " + savedOrder.getReturnedDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
                    
                    System.out.println("\n--- Updated Purchase Return List ---");
                    viewPurchaseReturn();
                } else {
                    System.out.println("Error creating purchase return record.");
                }
            }
        } else {
            System.out.println("Operation cancelled.");
        }
    }
    
    private void editPurchaseReturn(Scanner scanner) {
        System.out.println("\n--- Edit Purchase Return ---");
        System.out.println("(Enter 'cancel' at any time to cancel and return to menu)");
        
        List<PurchaseOrder> returnedOrders = purchaseOrderService.getAllPurchaseOrders().stream()
            .filter(order -> order.getOrderStatus().equalsIgnoreCase("returned"))
            .collect(Collectors.toList());
        
        if (returnedOrders.isEmpty()) {
            System.out.println("No purchase return records found.");
            return;
        }
        
        System.out.println("\n--- Purchase Return List ---");
        System.out.println("------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-12s | %-12s | %-15s | %-20s | %-15s%n", 
            "Return Date", "Receive Date", "Order Number", "Vendor", "Order Status");
        System.out.println("------------------------------------------------------------------------------------------------------------------------");
        
        for (PurchaseOrder order : returnedOrders) {
            String returnDate = order.getReturnedDate() != null 
                ? order.getReturnedDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
                : "N/A";
            String receiveDate = order.getReceivedDate() != null 
                ? order.getReceivedDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
                : "N/A";
            
            System.out.printf("%-12s | %-12s | %-15s | %-20s | %-15s%n",
                returnDate,
                receiveDate,
                order.getOrderNumber(),
                order.getVendor(),
                order.getOrderStatus());
        }
        System.out.println("------------------------------------------------------------------------------------------------------------------------");
        
        PurchaseOrder orderToEdit = null;
        while (orderToEdit == null) {
            System.out.print("\nEnter Order ID to edit (or 'cancel' to cancel): ");
            String idStr = scanner.nextLine().trim();
            
            if (checkCancel(idStr)) {
                System.out.println("Operation cancelled. Returning to Purchase Return menu.");
                return;
            }
            
            try {
                int orderId = Integer.parseInt(idStr);
                Optional<PurchaseOrder> orderOpt = purchaseOrderService.getPurchaseOrderByOrderId(orderId);
                
                if (orderOpt.isEmpty()) {
                    System.out.println("Order ID not found. Please enter a valid order ID.");
                } else if (!orderOpt.get().getOrderStatus().equalsIgnoreCase("returned")) {
                    System.out.println("Error: Order ID " + orderId + " is not in 'returned' status. Only orders with 'returned' status can be edited here.");
                    System.out.println("Current status: " + orderOpt.get().getOrderStatus());
                } else {
                    orderToEdit = orderOpt.get();
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid order ID. Please enter a number.");
            }
        }
        
        System.out.println("\n--- Edit Purchase Order #" + orderToEdit.getOrderId() + " ---");
        System.out.println("(Press Enter to keep current value, or enter 'cancel' to cancel)");
        System.out.println("Note: Status will remain as 'returned' and cannot be edited here.\n");
        
        try {
            LocalDate orderDate = readOrderDate(true, orderToEdit.getOrderDate(), scanner);
            if (orderDate == null) {
                System.out.println("Operation cancelled. Returning to Purchase Return menu.");
                return;
            }
            
            String vendor = readString("Enter Vendor", true, orderToEdit.getVendor(), true, scanner);
            if (vendor.equals("CANCEL_SIGNAL")) {
                System.out.println("Operation cancelled. Returning to Purchase Return menu.");
                return;
            }
            
            System.out.println("\n--- Edit Order Items ---");
            System.out.println("Current items:");
            List<OrderItem> currentItems = orderToEdit.getOrderItems();
            for (int i = 0; i < currentItems.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + currentItems.get(i));
            }
            
            System.out.print("Edit items? (yes/no) [no]: ");
            String editItemsResponse = scanner.nextLine().trim().toLowerCase();
            
            if (checkCancel(editItemsResponse)) {
                System.out.println("Operation cancelled. Returning to Purchase Return menu.");
                return;
            }
            
            List<OrderItem> orderItems;
            if (editItemsResponse.equals("yes") || editItemsResponse.equals("y")) {
                System.out.print("Edit individual items or re-enter all items? (individual/all) [all]: ");
                String editMode = scanner.nextLine().trim().toLowerCase();
                
                if (checkCancel(editMode)) {
                    System.out.println("Operation cancelled. Returning to Purchase Return menu.");
                    return;
                }
                
                if (editMode.equals("individual") || editMode.equals("i")) {
                    orderItems = editIndividualItems(currentItems, scanner);
                    if (orderItems == null) {
                        System.out.println("Operation cancelled. Returning to Purchase Return menu.");
                        return;
                    }
                } else {
                    orderItems = reenterAllItems(scanner);
                    if (orderItems == null) {
                        System.out.println("Operation cancelled. Returning to Purchase Return menu.");
                        return;
                    }
                }
            } else {
                orderItems = orderToEdit.getOrderItems();
            }
            
            PurchaseOrder updatedOrder = new PurchaseOrder();
            updatedOrder.setOrderId(orderToEdit.getOrderId());
            updatedOrder.setOrderDate(orderDate);
            updatedOrder.setOrderNumber(orderToEdit.getOrderNumber());
            updatedOrder.setVendor(vendor);
            updatedOrder.setOrderItems(orderItems);
            updatedOrder.setOrderStatus("returned");
            updatedOrder.setReceivedDate(orderToEdit.getReceivedDate());
            updatedOrder.setReturnedDate(orderToEdit.getReturnedDate());
            
            Optional<PurchaseOrder> savedOpt = purchaseOrderService.getPurchaseOrderByOrderId(orderToEdit.getOrderId());
            if (savedOpt.isPresent()) {
                PurchaseOrder savedOrder = purchaseOrderService.updatePurchaseOrder(savedOpt.get().getId(), updatedOrder);
                
                if (savedOrder != null) {
                    System.out.println("\n✅ Purchase Order updated successfully!");
                    System.out.println("\n--- Updated Purchase Order Details ---");
                    System.out.println(savedOrder.toDisplayString());
                    
                    System.out.println("\n--- Updated Purchase Return List ---");
                    viewPurchaseReturn();
                } else {
                    System.out.println("Error updating purchase order.");
                }
            }
            
        } catch (Exception e) {
            System.out.println("Error editing purchase return: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void deletePurchaseReturn(Scanner scanner) {
        System.out.println("\n--- Delete Purchase Return ---");
        System.out.println("(Enter 'cancel' at any time to cancel and return to menu)");
        
        List<PurchaseOrder> returnedOrders = purchaseOrderService.getAllPurchaseOrders().stream()
            .filter(order -> order.getOrderStatus().equalsIgnoreCase("returned"))
            .collect(Collectors.toList());
        
        if (returnedOrders.isEmpty()) {
            System.out.println("No purchase return records found.");
            return;
        }
        
        System.out.println("\n--- Purchase Return List ---");
        System.out.println("------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-12s | %-12s | %-15s | %-20s | %-15s%n", 
            "Return Date", "Receive Date", "Order Number", "Vendor", "Order Status");
        System.out.println("------------------------------------------------------------------------------------------------------------------------");
        
        for (PurchaseOrder order : returnedOrders) {
            String returnDate = order.getReturnedDate() != null 
                ? order.getReturnedDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
                : "N/A";
            String receiveDate = order.getReceivedDate() != null 
                ? order.getReceivedDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
                : "N/A";
            
            System.out.printf("%-12s | %-12s | %-15s | %-20s | %-15s%n",
                returnDate,
                receiveDate,
                order.getOrderNumber(),
                order.getVendor(),
                order.getOrderStatus());
        }
        System.out.println("------------------------------------------------------------------------------------------------------------------------");
        
        PurchaseOrder orderToDelete = null;
        while (orderToDelete == null) {
            System.out.print("\nEnter Order ID to delete (or 'cancel' to cancel): ");
            String idStr = scanner.nextLine().trim();
            
            if (checkCancel(idStr)) {
                System.out.println("Operation cancelled. Returning to Purchase Return menu.");
                return;
            }
            
            try {
                int orderId = Integer.parseInt(idStr);
                Optional<PurchaseOrder> orderOpt = purchaseOrderService.getPurchaseOrderByOrderId(orderId);
                
                if (orderOpt.isEmpty()) {
                    System.out.println("Order ID not found. Please enter a valid order ID.");
                } else if (!orderOpt.get().getOrderStatus().equalsIgnoreCase("returned")) {
                    System.out.println("Error: Order ID " + orderId + " is not in 'returned' status. Only orders with 'returned' status can be deleted here.");
                    System.out.println("Current status: " + orderOpt.get().getOrderStatus());
                } else {
                    orderToDelete = orderOpt.get();
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid order ID. Please enter a number.");
            }
        }
        
        System.out.println("\n--- Purchase Order Details ---");
        System.out.println(orderToDelete.toDisplayString());
        System.out.print("\nDo you want to delete this purchase return record? (Y/N): ");
        System.out.println("(Note: This will change the order status from 'returned' back to 'received')");
        
        String confirm = scanner.nextLine().trim().toUpperCase();
        
        if (confirm.equals("Y") || confirm.equals("YES")) {
            PurchaseOrder updatedOrder = new PurchaseOrder();
            updatedOrder.setOrderId(orderToDelete.getOrderId());
            updatedOrder.setOrderDate(orderToDelete.getOrderDate());
            updatedOrder.setOrderNumber(orderToDelete.getOrderNumber());
            updatedOrder.setVendor(orderToDelete.getVendor());
            updatedOrder.setOrderItems(orderToDelete.getOrderItems());
            updatedOrder.setOrderStatus("received");
            updatedOrder.setReceivedDate(orderToDelete.getReceivedDate());
            updatedOrder.setReturnedDate(null);
            
            Optional<PurchaseOrder> savedOpt = purchaseOrderService.getPurchaseOrderByOrderId(orderToDelete.getOrderId());
            if (savedOpt.isPresent()) {
                PurchaseOrder savedOrder = purchaseOrderService.updatePurchaseOrder(savedOpt.get().getId(), updatedOrder);
                
                if (savedOrder != null) {
                    System.out.println("✅ Purchase return record deleted successfully!");
                    System.out.println("Order status changed from 'returned' to 'received'.");
                    
                    System.out.println("\n--- Updated Purchase Return List ---");
                    viewPurchaseReturn();
                } else {
                    System.out.println("Error deleting purchase return record.");
                }
            }
        } else {
            System.out.println("Delete action cancelled.");
        }
    }
}
