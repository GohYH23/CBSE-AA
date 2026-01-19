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

public class PurchaseReturnMenu {
    private final PurchaseOrderService purchaseOrderService;
    private final Scanner scanner;
    private boolean running = true;
    
    public PurchaseReturnMenu(PurchaseOrderService purchaseOrderService, Scanner scanner) {
        this.purchaseOrderService = purchaseOrderService;
        this.scanner = scanner;
    }
    
    public void showPurchaseReturnMenu() {
        while (running) {
            System.out.println("\n========================================");
            System.out.println("      PURCHASE RETURN MANAGEMENT        ");
            System.out.println("========================================");
            System.out.println("1. View Purchase Return");
            System.out.println("2. Add Purchase Return");
            System.out.println("3. Edit Purchase Return");
            System.out.println("4. Delete Purchase Return");
            System.out.println("5. Exit to Main Menu");
            System.out.print("Select an option (1-5): ");
            
            try {
                String input = scanner.nextLine().trim();
                
                // Validate menu input - only numeric values 1-5
                if (!isValidMenuInput(input)) {
                    System.out.println("Invalid option. Please enter a number between 1 and 5.");
                    continue;
                }
                
                switch (input) {
                    case "1":
                        viewPurchaseReturn();
                        break;
                    case "2":
                        addPurchaseReturn();
                        break;
                    case "3":
                        editPurchaseReturn();
                        break;
                    case "4":
                        deletePurchaseReturn();
                        break;
                    case "5":
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
            return choice >= 1 && choice <= 5;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private boolean checkCancel(String input) {
        return input.equalsIgnoreCase("cancel");
    }
    
    private void viewPurchaseReturn() {
        // Get all purchase orders with "returned" status
        List<PurchaseOrder> returnedOrders = purchaseOrderService.getAllPurchaseOrders().stream()
            .filter(order -> order.getOrderStatus().equalsIgnoreCase("returned"))
            .collect(java.util.stream.Collectors.toList());
        
        System.out.println("\n--- Purchase Return List ---");
        if (returnedOrders.isEmpty()) {
            System.out.println("(No purchase return records found)");
            return;
        }
        
        // Display table: Return Date, Receive Date, Order Number, Vendor, Order Status
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
    
    private void addPurchaseReturn() {
        System.out.println("\n--- Add Purchase Return ---");
        System.out.println("(Enter 'cancel' at any time to cancel and return to menu)");
        
        // Get purchase orders with "received" status (only received orders can be returned)
        List<PurchaseOrder> receivedOrders = purchaseOrderService.getAllPurchaseOrders().stream()
            .filter(order -> order.getOrderStatus().equalsIgnoreCase("received"))
            .collect(java.util.stream.Collectors.toList());
        
        if (receivedOrders.isEmpty()) {
            System.out.println("No purchase orders with 'received' status found.");
            return;
        }
        
        // Display received orders in table format (without item details)
        System.out.println("\n--- Purchase Orders in Received Status ---");
        System.out.println("------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-8s | %-15s | %-12s | %-20s | %-15s | %-12s%n", 
            "Order ID", "Order Number", "Order Date", "Vendor", "Status", "Total Price");
        System.out.println("------------------------------------------------------------------------------------------------------------------------");
        
        for (PurchaseOrder order : receivedOrders) {
            System.out.printf("%-8d | %-15s | %-12s | %-20s | %-15s | $%-11.2f%n",
                order.getOrderId(),
                order.getOrderNumber(),
                order.getOrderDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                order.getVendor(),
                order.getOrderStatus(),
                order.getTotalPrice());
        }
        System.out.println("------------------------------------------------------------------------------------------------------------------------");
        
        // Get order ID to return
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
                // Validate order exists and is in received status
                PurchaseOrder order = purchaseOrderService.getPurchaseOrderById(orderId);
                
                if (order == null) {
                    System.out.println("Order ID not found. Please enter a valid order ID.");
                } else if (!order.getOrderStatus().equalsIgnoreCase("received")) {
                    System.out.println("Error: Order ID " + orderId + " is not in 'received' status. Only orders with 'received' status can be returned.");
                    System.out.println("Current status: " + order.getOrderStatus());
                } else {
                    orderToReturn = order;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid order ID. Please enter a number.");
            }
        }
        
        // Display order details and confirm
        System.out.println("\n--- Purchase Order Details ---");
        System.out.println(orderToReturn.toDisplayString());
        System.out.print("\nDo you want to return this order? (Y/N): ");
        System.out.println("(Note: This will change the order status from 'received' to 'returned')");
        
        String confirm = readYesNo();
        if (confirm == null) {
            System.out.println("Operation cancelled. Returning to Purchase Return menu.");
            return;
        }
        
        if (confirm.equals("Y") || confirm.equals("YES")) {
            // Update order status to "returned" and set returned date
            PurchaseOrder updatedOrder = new PurchaseOrder();
            updatedOrder.setOrderId(orderToReturn.getOrderId());
            updatedOrder.setOrderDate(orderToReturn.getOrderDate());
            updatedOrder.setOrderNumber(orderToReturn.getOrderNumber());
            updatedOrder.setVendor(orderToReturn.getVendor());
            updatedOrder.setOrderItems(orderToReturn.getOrderItems());
            updatedOrder.setOrderStatus("returned");
            updatedOrder.setReceivedDate(orderToReturn.getReceivedDate()); // Keep received date
            updatedOrder.setReturnedDate(LocalDate.now()); // Set returned date to today
            
            PurchaseOrder savedOrder = purchaseOrderService.updatePurchaseOrder(orderToReturn.getOrderId(), updatedOrder);
            
            if (savedOrder != null) {
                System.out.println("✅ Purchase return record created successfully!");
                System.out.println("Order status changed from 'received' to 'returned'.");
                System.out.println("Returned date: " + savedOrder.getReturnedDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
                
                // Display updated purchase return list
                System.out.println("\n--- Updated Purchase Return List ---");
                viewPurchaseReturn();
            } else {
                System.out.println("Error creating purchase return record.");
            }
        } else {
            System.out.println("Operation cancelled.");
        }
    }
    
    private void editPurchaseReturn() {
        System.out.println("\n--- Edit Purchase Return ---");
        System.out.println("(Enter 'cancel' at any time to cancel and return to menu)");
        
        // Get all returned orders
        List<PurchaseOrder> returnedOrders = purchaseOrderService.getAllPurchaseOrders().stream()
            .filter(order -> order.getOrderStatus().equalsIgnoreCase("returned"))
            .collect(java.util.stream.Collectors.toList());
        
        if (returnedOrders.isEmpty()) {
            System.out.println("No purchase return records found.");
            return;
        }
        
        // Display purchase return list (without item details)
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
        
        // Get order ID to edit
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
                // Validate order exists and is in returned status
                PurchaseOrder order = purchaseOrderService.getPurchaseOrderById(orderId);
                
                if (order == null) {
                    System.out.println("Order ID not found. Please enter a valid order ID.");
                } else if (!order.getOrderStatus().equalsIgnoreCase("returned")) {
                    System.out.println("Error: Order ID " + orderId + " is not in 'returned' status. Only orders with 'returned' status can be edited here.");
                    System.out.println("Current status: " + order.getOrderStatus());
                } else {
                    orderToEdit = order;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid order ID. Please enter a number.");
            }
        }
        
        // Edit order (same flow as edit goods receive, but status cannot be changed)
        System.out.println("\n--- Edit Purchase Order #" + orderToEdit.getOrderId() + " ---");
        System.out.println("(Press Enter to keep current value, or enter 'cancel' to cancel)");
        System.out.println("Note: Status will remain as 'returned' and cannot be edited here.\n");
        
        try {
            // Order Date
            LocalDate orderDate = readOrderDate(true, orderToEdit.getOrderDate());
            if (orderDate == null) {
                System.out.println("Operation cancelled. Returning to Purchase Return menu.");
                return;
            }
            
            // Vendor
            String vendor = readString("Enter Vendor", true, orderToEdit.getVendor(), true);
            if (vendor.equals("CANCEL_SIGNAL")) {
                System.out.println("Operation cancelled. Returning to Purchase Return menu.");
                return;
            }
            
            // Order Items - similar to purchase order edit
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
                    orderItems = editIndividualItems(currentItems);
                    if (orderItems == null) {
                        System.out.println("Operation cancelled. Returning to Purchase Return menu.");
                        return;
                    }
                } else {
                    orderItems = reenterAllItems();
                    if (orderItems == null) {
                        System.out.println("Operation cancelled. Returning to Purchase Return menu.");
                        return;
                    }
                }
            } else {
                orderItems = orderToEdit.getOrderItems();
            }
            
            // Create updated order - status stays "returned"
            PurchaseOrder updatedOrder = new PurchaseOrder();
            updatedOrder.setOrderId(orderToEdit.getOrderId());
            updatedOrder.setOrderDate(orderDate);
            updatedOrder.setOrderNumber(orderToEdit.getOrderNumber());
            updatedOrder.setVendor(vendor);
            updatedOrder.setOrderItems(orderItems);
            updatedOrder.setOrderStatus("returned"); // Always "returned"
            updatedOrder.setReceivedDate(orderToEdit.getReceivedDate()); // Keep existing received date
            updatedOrder.setReturnedDate(orderToEdit.getReturnedDate()); // Keep existing returned date
            
            PurchaseOrder savedOrder = purchaseOrderService.updatePurchaseOrder(orderToEdit.getOrderId(), updatedOrder);
            
            if (savedOrder != null) {
                System.out.println("\n✅ Purchase Order updated successfully!");
                System.out.println("\n--- Updated Purchase Order Details ---");
                System.out.println(savedOrder.toDisplayString());
                
                // Display updated list
                System.out.println("\n--- Updated Purchase Return List ---");
                viewPurchaseReturn();
            } else {
                System.out.println("Error updating purchase order.");
            }
            
        } catch (Exception e) {
            System.out.println("Error editing purchase return: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void deletePurchaseReturn() {
        System.out.println("\n--- Delete Purchase Return ---");
        System.out.println("(Enter 'cancel' at any time to cancel and return to menu)");
        
        // Get all returned orders
        List<PurchaseOrder> returnedOrders = purchaseOrderService.getAllPurchaseOrders().stream()
            .filter(order -> order.getOrderStatus().equalsIgnoreCase("returned"))
            .collect(java.util.stream.Collectors.toList());
        
        if (returnedOrders.isEmpty()) {
            System.out.println("No purchase return records found.");
            return;
        }
        
        // Display purchase return list (without item details)
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
        
        // Get order ID to delete
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
                // Validate order exists and is in returned status
                PurchaseOrder order = purchaseOrderService.getPurchaseOrderById(orderId);
                
                if (order == null) {
                    System.out.println("Order ID not found. Please enter a valid order ID.");
                } else if (!order.getOrderStatus().equalsIgnoreCase("returned")) {
                    System.out.println("Error: Order ID " + orderId + " is not in 'returned' status. Only orders with 'returned' status can be deleted here.");
                    System.out.println("Current status: " + order.getOrderStatus());
                } else {
                    orderToDelete = order;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid order ID. Please enter a number.");
            }
        }
        
        // Display order details and confirm
        System.out.println("\n--- Purchase Order Details ---");
        System.out.println(orderToDelete.toDisplayString());
        System.out.print("\nDo you want to delete this purchase return record? (Y/N): ");
        System.out.println("(Note: This will change the order status from 'returned' back to 'received')");
        
        String confirm = readYesNo();
        if (confirm == null) {
            System.out.println("Operation cancelled. Returning to Purchase Return menu.");
            return;
        }
        
        if (confirm.equals("Y") || confirm.equals("YES")) {
            // Update order status from "returned" back to "received" and clear returned date
            PurchaseOrder updatedOrder = new PurchaseOrder();
            updatedOrder.setOrderId(orderToDelete.getOrderId());
            updatedOrder.setOrderDate(orderToDelete.getOrderDate());
            updatedOrder.setOrderNumber(orderToDelete.getOrderNumber());
            updatedOrder.setVendor(orderToDelete.getVendor());
            updatedOrder.setOrderItems(orderToDelete.getOrderItems());
            updatedOrder.setOrderStatus("received"); // Change back to received
            updatedOrder.setReceivedDate(orderToDelete.getReceivedDate()); // Keep received date
            updatedOrder.setReturnedDate(null); // Clear returned date
            
            PurchaseOrder savedOrder = purchaseOrderService.updatePurchaseOrder(orderToDelete.getOrderId(), updatedOrder);
            
            if (savedOrder != null) {
                System.out.println("✅ Purchase return record deleted successfully!");
                System.out.println("Order status changed from 'returned' to 'received'.");
                
                // Display updated list
                System.out.println("\n--- Updated Purchase Return List ---");
                viewPurchaseReturn();
            } else {
                System.out.println("Error deleting purchase return record.");
            }
        } else {
            System.out.println("Delete action cancelled.");
        }
    }
    
    // Helper methods
    private String readYesNo() {
        while (true) {
            String input = scanner.nextLine().trim();
            
            if (checkCancel(input)) {
                return null;
            }
            
            input = input.toUpperCase();
            if (input.equals("Y") || input.equals("YES") || input.equals("N") || input.equals("NO")) {
                return input;
            }
            
            System.out.println("Invalid input. Please enter Y for Yes or N for No (or 'cancel' to cancel).");
            System.out.print("Enter Y/N: ");
        }
    }
    
    private LocalDate readOrderDate(boolean allowEmpty, LocalDate defaultValue) {
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
    
    private double readDouble(String prompt, boolean allowCancel) {
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
    
    private List<OrderItem> editIndividualItems(List<OrderItem> currentItems) {
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
            
            String newItemName = readString("  Item Name/Description", true, itemToEdit.getItemName(), true);
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
            
            // Update the item
            OrderItem updatedItem = new OrderItem(newItemName, newQuantity, newPrice);
            orderItems.set(itemIndex, updatedItem);
            System.out.printf("  ✅ Updated: %s x %d @ $%.2f each = $%.2f total%n",
                newItemName, newQuantity, newPrice, updatedItem.getTotalPrice());
        }
        
        return orderItems;
    }
    
    private List<OrderItem> reenterAllItems() {
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
            
            int quantity = readInteger("  Quantity", true);
            if (quantity == -1) {
                return null;
            }
            
            double pricePerItem = readDouble("  Price per Item", true);
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
    
    public void setRunning(boolean running) {
        this.running = running;
    }
}
