package com.inventory.purchaseorder;

import com.inventory.api.purchaseorder.PurchaseOrder;
import com.inventory.api.purchaseorder.PurchaseOrderService;
import com.inventory.api.purchaseorder.OrderItem;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PurchaseOrderMenu {
    private final PurchaseOrderService purchaseOrderService;
    private final Scanner scanner;
    private boolean running = true;
    
    private static final List<String> VALID_STATUSES = List.of("pending", "shipping", "received", "cancelled", "returned");
    private static final List<String> EDITABLE_STATUSES = List.of("pending", "shipping", "cancelled"); // Statuses that can be set in Edit Purchase Order
    
    public PurchaseOrderMenu(PurchaseOrderService purchaseOrderService, Scanner scanner) {
        this.purchaseOrderService = purchaseOrderService;
        this.scanner = scanner;
    }
    
    public void showPurchaseOrderMenu() {
        while (running) {
            System.out.println("\n========================================");
            System.out.println("      PURCHASE ORDER MANAGEMENT        ");
            System.out.println("========================================");
            System.out.println("1. View Purchase Orders");
            System.out.println("2. Add Purchase Order");
            System.out.println("3. Edit Purchase Order");
            System.out.println("4. Delete Purchase Order");
            System.out.println("5. View Purchase Reports");
            System.out.println("6. Exit to Main Menu");
            System.out.print("Select an option (1-6): ");
            
            try {
                String input = scanner.nextLine().trim();
                
                // Validate menu input - only numeric values 1-6
                if (!isValidMenuInput(input)) {
                    System.out.println("Invalid option. Please enter a number between 1 and 6.");
                    continue;
                }
                
                switch (input) {
                    case "1":
                        viewPurchaseOrders();
                        break;
                    case "2":
                        addPurchaseOrder();
                        break;
                    case "3":
                        editPurchaseOrder();
                        break;
                    case "4":
                        deletePurchaseOrder();
                        break;
                    case "5":
                        viewPurchaseReports();
                        break;
                    case "6":
                        System.out.println("Returning to main menu...");
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid option, please try again.");
                }
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
        }
    }
    
    private boolean isValidMenuInput(String input) {
        try {
            int choice = Integer.parseInt(input);
            return choice >= 1 && choice <= 6;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private boolean checkCancel(String input) {
        return input.equalsIgnoreCase("cancel");
    }
    
    private LocalDate readOrderDate(boolean allowEmpty, LocalDate defaultValue) {
        while (true) {
            String prompt = allowEmpty && defaultValue != null 
                ? "Enter Order Date (YYYY-MM-DD) [" + defaultValue.format(DateTimeFormatter.ISO_LOCAL_DATE) + "]: "
                : "Enter Order Date (YYYY-MM-DD): ";
            System.out.print(prompt);
            String dateStr = scanner.nextLine().trim();
            
            if (checkCancel(dateStr)) {
                return null; // Signal cancellation
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
                
                // Validate: date should not be in the future
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
    
    private String readString(String prompt, boolean allowEmpty, String defaultValue, boolean allowCancel) {
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
    
    private int readInteger(String prompt, boolean allowCancel) {
        while (true) {
            System.out.print(prompt + ": ");
            String input = scanner.nextLine().trim();
            
            if (allowCancel && checkCancel(input)) {
                return -1; // Signal cancellation
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
    
    private double readDouble(String prompt, boolean allowCancel) {
        while (true) {
            System.out.print(prompt + ": $");
            String input = scanner.nextLine().trim();
            
            if (allowCancel && checkCancel(input)) {
                return -1; // Signal cancellation
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
    
    private String readStatus(boolean allowEmpty, String defaultValue, boolean allowCancel) {
        while (true) {
            String prompt = allowEmpty && defaultValue != null
                ? "Enter Order Status (pending/shipping/received/cancelled/returned) [" + defaultValue + "]"
                : "Enter Order Status (pending/shipping/received/cancelled/returned)";
            System.out.print(prompt + ": ");
            String input = scanner.nextLine().trim().toLowerCase();
            
            if (allowCancel && checkCancel(input)) {
                return "CANCEL_SIGNAL";
            }
            
            if (input.isEmpty() && allowEmpty && defaultValue != null) {
                return defaultValue;
            }
            
            if (input.isEmpty() && !allowEmpty) {
                return "pending"; // Default to pending
            }
            
            if (!VALID_STATUSES.contains(input)) {
                System.out.println("Invalid status. Please enter one of: pending, shipping, received, cancelled, returned");
                continue;
            }
            
            return input;
        }
    }
    
    // Restricted status method for Edit Purchase Order (cannot set to received or returned)
    private String readStatusForEdit(boolean allowEmpty, String defaultValue, boolean allowCancel) {
        while (true) {
            String prompt;
            if (allowEmpty && defaultValue != null) {
                // Check if default value is received/returned - if so, inform user it can't be kept
                if (defaultValue.equalsIgnoreCase("received") || defaultValue.equalsIgnoreCase("returned")) {
                    prompt = "Enter Order Status (pending/shipping/cancelled) [Note: Current status '" + defaultValue + "' cannot be kept]";
                } else {
                    prompt = "Enter Order Status (pending/shipping/cancelled) [" + defaultValue + "]";
                }
            } else {
                prompt = "Enter Order Status (pending/shipping/cancelled)";
            }
            
            System.out.print(prompt + ": ");
            String input = scanner.nextLine().trim().toLowerCase();
            
            if (allowCancel && checkCancel(input)) {
                return "CANCEL_SIGNAL";
            }
            
            if (input.isEmpty() && allowEmpty && defaultValue != null) {
                // Validate default value - if it's received/returned, don't allow it
                if (defaultValue.equalsIgnoreCase("received") || defaultValue.equalsIgnoreCase("returned")) {
                    System.out.println("Error: Status '" + defaultValue + "' cannot be kept here. Please enter a new status (pending/shipping/cancelled).");
                    System.out.println("To change status to 'received', use Manage Goods Receive.");
                    System.out.println("To change status to 'returned', use Manage Purchase Return.");
                    continue;
                }
                return defaultValue;
            }
            
            if (input.isEmpty() && !allowEmpty) {
                return "pending"; // Default to pending
            }
            
            // Validate against editable statuses only
            if (!EDITABLE_STATUSES.contains(input)) {
                if (input.equals("received")) {
                    System.out.println("Status 'received' cannot be set here. Please use 'Manage Goods Receive' to change status to received.");
                    System.out.println("Valid statuses for editing: pending, shipping, cancelled");
                } else if (input.equals("returned")) {
                    System.out.println("Status 'returned' cannot be set here. Please use 'Manage Purchase Return' to change status to returned.");
                    System.out.println("Valid statuses for editing: pending, shipping, cancelled");
                } else {
                    System.out.println("Invalid status. Please enter one of: pending, shipping, cancelled");
                }
                continue;
            }
            
            return input;
        }
    }
    
    private String generateOrderNumber(int orderId) {
        return String.format("PO-%03d", orderId);
    }
    
    private void viewPurchaseOrders() {
        List<PurchaseOrder> orders = purchaseOrderService.getAllPurchaseOrders();
        
        System.out.println("\n--- Purchase Order List ---");
        if (orders.isEmpty()) {
            System.out.println("(No purchase orders found)");
            return;
        }
        
        // Display table header
        System.out.println("--------------------------------------------------------------------------------------------------------");
        System.out.printf("%-8s | %-12s | %-15s | %-20s | %-15s | %-12s%n", 
            "Order ID", "Order Date", "Order Number", "Vendor", "Status", "Total Price");
        System.out.println("--------------------------------------------------------------------------------------------------------");
        
        // Display each order
        for (PurchaseOrder order : orders) {
            System.out.printf("%-8d | %-12s | %-15s | %-20s | %-15s | $%-11.2f%n",
                order.getOrderId(),
                order.getOrderDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                order.getOrderNumber(),
                order.getVendor(),
                order.getOrderStatus(),
                order.getTotalPrice());
        }
        System.out.println("--------------------------------------------------------------------------------------------------------");
    }
    
    private void addPurchaseOrder() {
        System.out.println("\n--- Add New Purchase Order ---");
        System.out.println("(Enter 'cancel' at any time to cancel and return to menu)");
        
        try {
            // Order Date
            LocalDate orderDate = readOrderDate(false, null);
            if (orderDate == null) {
                System.out.println("Operation cancelled. Returning to Purchase Order menu.");
                return;
            }
            
            // Vendor
            String vendor = readString("Enter Vendor", false, null, true);
            if (vendor.equals("CANCEL_SIGNAL")) {
                System.out.println("Operation cancelled. Returning to Purchase Order menu.");
                return;
            }
            
            // Order Items (loop)
            List<OrderItem> orderItems = new ArrayList<>();
            System.out.println("\n--- Add Order Items (Enter 'done' when finished, 'cancel' to cancel) ---");
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
                
                // Quantity (integer only)
                int quantity = readInteger("  Quantity", true);
                if (quantity == -1) {
                    System.out.println("Operation cancelled. Returning to Purchase Order menu.");
                    return;
                }
                
                // Price per item (decimal or integer)
                double pricePerItem = readDouble("  Price per Item", true);
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
            
            // Order Status (restricted - cannot set to received or returned in Add)
            System.out.println("\n--- Order Status ---");
            System.out.println("Note: Status can only be set to: pending, shipping, or cancelled.");
            System.out.println("'received' status can only be set through 'Manage Goods Receive' module.");
            System.out.println("'returned' status can only be set through 'Manage Purchase Return' module.");
            
            String orderStatus = readStatusForEdit(false, null, true);
            if (orderStatus.equals("CANCEL_SIGNAL")) {
                System.out.println("Operation cancelled. Returning to Purchase Order menu.");
                return;
            }
            
            // Create and save purchase order
            PurchaseOrder newOrder = new PurchaseOrder();
            newOrder.setOrderDate(orderDate);
            int nextId = purchaseOrderService.getNextOrderId();
            newOrder.setOrderNumber(generateOrderNumber(nextId)); // Auto-generate order number
            newOrder.setVendor(vendor);
            newOrder.setOrderItems(orderItems);
            newOrder.setOrderStatus(orderStatus);
            
            PurchaseOrder savedOrder = purchaseOrderService.addPurchaseOrder(newOrder);
            
            // Display summary
            System.out.println("\n✅ Purchase Order created successfully!");
            System.out.println("\n--- Purchase Order Details ---");
            System.out.println(savedOrder.toDisplayString());
            System.out.printf("Total Price: $%.2f%n", savedOrder.getTotalPrice());
            
            // Display updated list
            System.out.println("\n--- Updated Purchase Order List ---");
            viewPurchaseOrders();
            
        } catch (Exception e) {
            System.out.println("Error adding purchase order: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void editPurchaseOrder() {
        System.out.println("\n--- Edit Purchase Order ---");
        System.out.println("(Enter 'cancel' at any time to cancel and return to menu)");
        
        // Display all orders first
        List<PurchaseOrder> orders = purchaseOrderService.getAllPurchaseOrders();
        if (orders.isEmpty()) {
            System.out.println("No purchase orders found.");
            return;
        }
        
        System.out.println("\n--- Available Purchase Orders ---");
        viewPurchaseOrders();
        
        // Get order ID to edit
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
                orderToEdit = purchaseOrderService.getPurchaseOrderById(orderId);
                if (orderToEdit == null) {
                    System.out.println("The record was not found. Please enter a valid order ID.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid order ID. Please enter a number.");
            }
        }
        
        // Edit fields
        System.out.println("\n--- Edit Purchase Order #" + orderToEdit.getOrderId() + " ---");
        System.out.println("(Press Enter to keep current value, or enter 'cancel' to cancel)\n");
        
        try {
            // Order Date
            LocalDate orderDate = readOrderDate(true, orderToEdit.getOrderDate());
            if (orderDate == null) {
                System.out.println("Operation cancelled. Returning to Purchase Order menu.");
                return;
            }
            
            // Vendor
            String vendor = readString("Enter Vendor", true, orderToEdit.getVendor(), true);
            if (vendor.equals("CANCEL_SIGNAL")) {
                System.out.println("Operation cancelled. Returning to Purchase Order menu.");
                return;
            }
            
            // Order Items
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
                // Ask if user wants to edit individual items or re-enter all
                System.out.print("Edit individual items or re-enter all items? (individual/all) [all]: ");
                String editMode = scanner.nextLine().trim().toLowerCase();
                
                if (checkCancel(editMode)) {
                    System.out.println("Operation cancelled. Returning to Purchase Order menu.");
                    return;
                }
                
                if (editMode.equals("individual") || editMode.equals("i")) {
                    // Edit individual items
                    orderItems = editIndividualItems(currentItems);
                    if (orderItems == null) {
                        System.out.println("Operation cancelled. Returning to Purchase Order menu.");
                        return;
                    }
                } else {
                    // Re-enter all items
                    orderItems = new ArrayList<>();
                    System.out.println("\n--- Re-enter All Order Items (Enter 'done' when finished, 'cancel' to cancel) ---");
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
                        
                        int quantity = readInteger("  Quantity", true);
                        if (quantity == -1) {
                            System.out.println("Operation cancelled. Returning to Purchase Order menu.");
                            return;
                        }
                        
                        double pricePerItem = readDouble("  Price per Item", true);
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
                }
            } else {
                orderItems = orderToEdit.getOrderItems();
            }
            
            // Order Status (restricted - cannot set to received or returned)
            String currentStatus = orderToEdit.getOrderStatus().toLowerCase();
            String orderStatus;
            
            // If current status is received or returned, user cannot change it here
            if (currentStatus.equals("received") || currentStatus.equals("returned")) {
                System.out.println("\nNote: Current status is '" + currentStatus + "', which cannot be changed here.");
                System.out.println("Status will remain as '" + currentStatus + "'.");
                System.out.println("To change status from '" + currentStatus + "', use:");
                if (currentStatus.equals("received")) {
                    System.out.println("  - 'Manage Purchase Return' to change to 'returned'");
                    System.out.println("  - 'Manage Goods Receive' → Delete to change back to 'shipping'");
                } else {
                    System.out.println("  - 'Manage Purchase Return' → Delete to change back to 'received'");
                }
                orderStatus = currentStatus; // Keep the original status
            } else {
                // Current status is pending, shipping, or cancelled - can be changed
                System.out.println("\nNote: Status can only be changed to: pending, shipping, or cancelled.");
                System.out.println("To change status to 'received', use 'Manage Goods Receive'.");
                System.out.println("To change status to 'returned', use 'Manage Purchase Return'.");
                
                String newStatus = readStatusForEdit(true, orderToEdit.getOrderStatus(), true);
                if (newStatus.equals("CANCEL_SIGNAL")) {
                    System.out.println("Operation cancelled. Returning to Purchase Order menu.");
                    return;
                }
                orderStatus = newStatus;
            }
            
            // Create updated order (order number stays the same - auto-generated)
            PurchaseOrder updatedOrder = new PurchaseOrder();
            updatedOrder.setOrderId(orderToEdit.getOrderId());
            updatedOrder.setOrderDate(orderDate);
            updatedOrder.setOrderNumber(orderToEdit.getOrderNumber()); // Keep existing order number
            updatedOrder.setVendor(vendor);
            updatedOrder.setOrderItems(orderItems);
            updatedOrder.setOrderStatus(orderStatus);
            
            PurchaseOrder savedOrder = purchaseOrderService.updatePurchaseOrder(orderToEdit.getOrderId(), updatedOrder);
            
            if (savedOrder != null) {
                System.out.println("\n✅ Purchase Order updated successfully!");
                System.out.println("\n--- Updated Purchase Order Details ---");
                System.out.println(savedOrder.toDisplayString());
                System.out.printf("Total Price: $%.2f%n", savedOrder.getTotalPrice());
            } else {
                System.out.println("Error updating purchase order.");
            }
            
        } catch (Exception e) {
            System.out.println("Error editing purchase order: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private List<OrderItem> editIndividualItems(List<OrderItem> currentItems) {
        List<OrderItem> orderItems = new ArrayList<>(currentItems);
        
        System.out.println("\n--- Edit Individual Items ---");
        System.out.println("(Enter 'done' to finish editing, 'cancel' to cancel)");
        
        while (true) {
            System.out.print("\nEnter item name to edit: ");
            String itemNameToEdit = scanner.nextLine().trim();
            
            if (checkCancel(itemNameToEdit)) {
                return null; // Signal cancellation
            }
            
            if (itemNameToEdit.equalsIgnoreCase("done")) {
                break;
            }
            
            if (itemNameToEdit.isEmpty()) {
                System.out.println("Item name cannot be empty.");
                continue;
            }
            
            // Find the item
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
            
            // Edit the item fields one by one
            System.out.println("\nEditing item: " + itemToEdit);
            System.out.println("(Press Enter to keep current value, or enter 'cancel' to cancel)");
            
            // Item Name
            String newItemName = readString("  Item Name/Description", true, itemToEdit.getItemName(), true);
            if (newItemName.equals("CANCEL_SIGNAL")) {
                return null;
            }
            
            // Quantity
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
            
            // Price per item
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
            
            // Update the item
            OrderItem updatedItem = new OrderItem(newItemName, newQuantity, newPrice);
            orderItems.set(itemIndex, updatedItem);
            System.out.printf("  ✅ Updated: %s x %d @ $%.2f each = $%.2f total%n",
                newItemName, newQuantity, newPrice, updatedItem.getTotalPrice());
        }
        
        return orderItems;
    }
    
    private void deletePurchaseOrder() {
        System.out.println("\n--- Delete Purchase Order ---");
        System.out.println("(Enter 'cancel' at any time to cancel and return to menu)");
        
        // Display all orders
        List<PurchaseOrder> orders = purchaseOrderService.getAllPurchaseOrders();
        if (orders.isEmpty()) {
            System.out.println("No purchase orders found.");
            return;
        }
        
        System.out.println("\n--- Available Purchase Orders ---");
        viewPurchaseOrders();
        
        // Get order ID to delete
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
                orderToDelete = purchaseOrderService.getPurchaseOrderById(orderId);
                if (orderToDelete == null) {
                    System.out.println("Order ID not found. Please enter a valid ID.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid order ID. Please enter a number.");
            }
        }
        
        // Display order details and confirm
        System.out.println("\n--- Purchase Order Details ---");
        System.out.println(orderToDelete.toDisplayString());
        System.out.print("\nDo you want to delete this purchase order: {" + orderToDelete.getOrderId() + "} [Y/N/Cancel]? ");
        String confirm = scanner.nextLine().trim().toUpperCase();
        
        if (checkCancel(confirm) || confirm.equals("CANCEL")) {
            System.out.println("Delete action cancelled.");
            return;
        }
        
        if (confirm.equals("Y") || confirm.equals("YES")) {
            boolean deleted = purchaseOrderService.deletePurchaseOrder(orderToDelete.getOrderId());
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
    
    private void viewPurchaseReports() {
        System.out.println("\n--- View Purchase Reports ---");
        
        List<PurchaseOrder> orders = purchaseOrderService.getAllPurchaseOrders();
        
        if (orders.isEmpty()) {
            System.out.println("(No purchase orders found)");
            return;
        }
        
        // Default: Sort by Order ID ascending
        List<PurchaseOrder> currentOrders = new ArrayList<>(orders);
        currentOrders.sort((o1, o2) -> Integer.compare(o1.getOrderId(), o2.getOrderId()));
        
        // Display full report with item details first (default sorted)
        displayFullReport(currentOrders);
        
        // Ask for sorting/filtering options
        while (true) {
            List<PurchaseOrder> filteredAndSorted = askForSortingAndFiltering(currentOrders);
            if (filteredAndSorted == null) {
                // User selected exit
                return;
            }
            
            // Update current orders to maintain filter/sort state
            currentOrders = filteredAndSorted;
            
            // Display full report with item details
            displayFullReport(filteredAndSorted);
        }
    }
    
    private List<PurchaseOrder> askForSortingAndFiltering(List<PurchaseOrder> orders) {
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
        
        // Validate input - only integer 1-6
        if (!isValidMenuInputForReport(option)) {
            System.out.println("Invalid option. Please enter a number between 1 and 6.");
            return result;
        }
        
        int choice = Integer.parseInt(option);
        
        switch (choice) {
            case 1:
                // Filter by status
                result = filterByStatus(result);
                break;
            case 2:
                // Sort by Order ID
                result = applySorting(result, "orderId");
                break;
            case 3:
                // Sort by Order Date
                result = applySorting(result, "orderDate");
                break;
            case 4:
                // Sort by Vendor
                result = applySorting(result, "vendor");
                break;
            case 5:
                // Sort by Total Price
                result = applySorting(result, "totalPrice");
                break;
            case 6:
                // Exit to Purchase Order Menu
                return null;
        }
        
        return result;
    }
    
    private boolean isValidMenuInputForReport(String input) {
        try {
            int choice = Integer.parseInt(input);
            return choice >= 1 && choice <= 6;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private List<PurchaseOrder> filterByStatus(List<PurchaseOrder> orders) {
        List<String> selectedStatuses = new ArrayList<>();
        
        System.out.println("\n--- Filter by Status ---");
        System.out.println("Available statuses: pending, shipping, received, cancelled, returned");
        System.out.println("You can enter multiple statuses, one per line");
        System.out.println("Enter 'done' when finished, or 'cancel' to return:");
        
        while (true) {
            System.out.print("Enter status (or 'done' to finish, 'cancel' to return): ");
            String status = scanner.nextLine().trim().toLowerCase();
            
            // Check for cancel
            if (checkCancel(status)) {
                return orders; // Return original orders (no filter applied)
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
                System.out.println("Invalid status. Please enter one of: pending, shipping, delivered, cancelled, returned, 'done', or 'cancel'.");
            }
        }
        
        List<PurchaseOrder> filtered = orders.stream()
            .filter(order -> selectedStatuses.contains(order.getOrderStatus().toLowerCase()))
            .collect(java.util.stream.Collectors.toList());
        
        if (filtered.isEmpty()) {
            System.out.println("No orders found with selected status(es): " + String.join(", ", selectedStatuses));
            return filtered;
        }
        
        System.out.println("✅ Filtered by status(es): " + String.join(", ", selectedStatuses) + 
            " (" + filtered.size() + " order(s))");
        return filtered;
    }
    
    private List<PurchaseOrder> applySorting(List<PurchaseOrder> orders, String sortField) {
        while (true) {
            System.out.print("\nSort order - A: Ascending, D: Descending, or 'cancel' to return (A/D/cancel): ");
            String sortOrder = scanner.nextLine().trim();
            
            // Check for cancel
            if (checkCancel(sortOrder)) {
                return orders; // Return original orders (no sorting applied)
            }
            
            sortOrder = sortOrder.toUpperCase();
            
            // Validate input - only A or D
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
                        sorted.sort((o1, o2) -> ascending
                            ? o1.getOrderDate().compareTo(o2.getOrderDate())
                            : o2.getOrderDate().compareTo(o1.getOrderDate()));
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
            // Order Header
            System.out.println("\n" + "=".repeat(110));
            System.out.printf("Order ID: %-8d | Order Number: %-15s | Date: %-12s | Vendor: %-25s | Status: %-15s%n",
                order.getOrderId(),
                order.getOrderNumber(),
                order.getOrderDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                order.getVendor(),
                order.getOrderStatus());
            System.out.println("=".repeat(110));
            
            // Item Details Table
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
            
            // Order Summary
            System.out.printf("\n%85s: $%.2f%n", "Total Order Amount", order.getTotalPrice());
            System.out.println("=".repeat(110));
        }
        
        // Summary Statistics
        System.out.println("\n" + "=".repeat(110));
        System.out.println("REPORT SUMMARY");
        System.out.println("=".repeat(110));
        System.out.printf("Total Orders: %d%n", orders.size());
        double totalValue = orders.stream().mapToDouble(PurchaseOrder::getTotalPrice).sum();
        System.out.printf("Total Value: $%.2f%n", totalValue);
        System.out.println("=".repeat(110));
    }
    
    public void setRunning(boolean running) {
        this.running = running;
    }
}
