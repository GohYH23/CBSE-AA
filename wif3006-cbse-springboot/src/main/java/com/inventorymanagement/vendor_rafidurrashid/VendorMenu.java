package com.inventorymanagement.vendor_rafidurrashid;

import com.inventorymanagement.salesorder_wongxiuhuan.model.*;
import com.inventorymanagement.salesorder_wongxiuhuan.service.SalesOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@Component
public class VendorMenu {

    @Autowired
    private SalesOrderService salesOrderService;

    private Scanner scanner = new Scanner(System.in);
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ==================== MAIN MENU ====================
    public void displayMainMenu() {
        boolean exit = false;
        
        while (!exit) {
            System.out.println("\n╔══════════════════════════════════════════╗");
            System.out.println("║       SALES ORDER MANAGEMENT SYSTEM      ║");
            System.out.println("╠══════════════════════════════════════════╣");
            System.out.println("║  1. Manage Sales Orders                  ║");
            System.out.println("║  2. Manage Delivery Orders               ║");
            System.out.println("║  3. Manage Sales Returns                 ║");
            System.out.println("║  4. View Sales Reports                   ║");
            System.out.println("║  5. Exit to Main Menu                    ║");
            System.out.println("╚══════════════════════════════════════════╝");
            System.out.print("\nEnter your choice (1-5): ");
            
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    manageSalesOrders();
                    break;
                case "2":
                    manageDeliveryOrders();
                    break;
                case "3":
                    manageSalesReturns();
                    break;
                case "4":
                    viewSalesReports();
                    break;
                case "5":
                    exit = true;
                    System.out.println("\nReturning to main menu...");
                    break;
                default:
                    System.out.println("\n❌ Invalid choice! Please enter a number between 1-5.");
            }
        }
    }

    // ==================== SALES ORDERS MANAGEMENT ====================
    private void manageSalesOrders() {
        boolean back = false;
        
        while (!back) {
            System.out.println("\n╔══════════════════════════════════════════╗");
            System.out.println("║         MANAGE SALES ORDERS              ║");
            System.out.println("╠══════════════════════════════════════════╣");
            System.out.println("║  1. View All Sales Orders                ║");
            System.out.println("║  2. Create New Sales Order               ║");
            System.out.println("║  3. View Sales Order Details             ║");
            System.out.println("║  4. Update Sales Order                   ║");
            System.out.println("║  5. Delete Sales Order                   ║");
            System.out.println("║  6. Manage Order Items                   ║");
            System.out.println("║  7. Back to Main Menu                    ║");
            System.out.println("╚══════════════════════════════════════════╝");
            System.out.print("\nEnter your choice (1-7): ");
            
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    viewAllSalesOrders();
                    break;
                case "2":
                    createSalesOrder();
                    break;
                case "3":
                    viewSalesOrderDetails();
                    break;
                case "4":
                    updateSalesOrder();
                    break;
                case "5":
                    deleteSalesOrder();
                    break;
                case "6":
                    manageOrderItems();
                    break;
                case "7":
                    back = true;
                    break;
                default:
                    System.out.println("\n❌ Invalid choice! Please enter a number between 1-7.");
            }
        }
    }

    private void viewAllSalesOrders() {
        List<SalesOrder> orders = salesOrderService.getAllSalesOrders();
        
        if (orders.isEmpty()) {
            System.out.println("\n📭 No sales orders found.");
            return;
        }
        
        System.out.println("\n══════════════════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("                                         SALES ORDERS LIST");
        System.out.println("══════════════════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.printf("%-4s %-18s %-15s %-25s %-12s %-15s %-10s%n", 
            "No.", "Order Number", "Date", "Customer", "Status", "Total Amount", "Items");
        System.out.println("──────────────────────────────────────────────────────────────────────────────────────────────────────────");
        
        int counter = 1;
        for (SalesOrder order : orders) {
            String customerName = salesOrderService.getCustomerNameById(order.getCustomerId());
            if (customerName.length() > 22) {
                customerName = customerName.substring(0, 19) + "...";
            }
            
            List<SalesOrderItem> items = salesOrderService.getItemsByOrderId(order.getId());
            BigDecimal total = calculateOrderTotal(order, items);
            
            System.out.printf("%-4d %-18s %-15s %-25s %-12s %-15.2f %-10d%n",
                counter++,
                order.getOrderNumber(),
                order.getOrderDate().format(dateFormatter),
                customerName,
                order.getOrderStatus(),
                total,
                items.size());
        }
        System.out.println("══════════════════════════════════════════════════════════════════════════════════════════════════════════");
    }

    private void createSalesOrder() {
        System.out.println("\n══════════════════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("                                         CREATE NEW SALES ORDER");
        System.out.println("══════════════════════════════════════════════════════════════════════════════════════════════════════════");
        
        // Step 1: Customer Selection
        System.out.print("\nEnter Customer Name: ");
        String customerName = scanner.nextLine().trim();
        String customerId = salesOrderService.getCustomerIdByName(customerName);
        
        if (customerId == null) {
            System.out.println("\n❌ Customer not found!");
            System.out.print("Would you like to: \n1. Try another name \n2. Cancel \nChoice: ");
            String option = scanner.nextLine().trim();
            if (option.equals("1")) {
                createSalesOrder();
            }
            return;
        }
        
        // Step 2: Order Date
        LocalDate orderDate = getDateInput("Order Date (yyyy-MM-dd)", LocalDate.now());
        
        // Step 3: Tax Selection
        String taxId = selectTax();
        
        // Step 4: Order Details
        System.out.print("Order Status (PENDING/CONFIRMED/PROCESSING/COMPLETED/CANCELLED) [PENDING]: ");
        String status = scanner.nextLine().trim();
        if (status.isEmpty()) status = "PENDING";
        
        System.out.print("Description (optional): ");
        String description = scanner.nextLine().trim();
        
        // Step 5: Create Order
        SalesOrder order = new SalesOrder();
        order.setOrderDate(orderDate);
        order.setCustomerId(customerId);
        order.setTaxId(taxId);
        order.setOrderStatus(status.toUpperCase());
        order.setDescription(description);
        
        try {
            SalesOrder createdOrder = salesOrderService.createSalesOrder(order);
            System.out.println("\n✅ Sales Order created successfully!");
            System.out.println("   Order Number: " + createdOrder.getOrderNumber());
            
            // Ask to add items
            System.out.print("\nWould you like to add items to this order? (yes/no): ");
            String addItems = scanner.nextLine().trim();
            if (addItems.equalsIgnoreCase("yes")) {
                addItemsToOrder(createdOrder.getId());
            }
        } catch (Exception e) {
            System.out.println("\n❌ Error creating sales order: " + e.getMessage());
        }
    }

    private void viewSalesOrderDetails() {
        System.out.print("\nEnter Sales Order Number: ");
        String orderNumber = scanner.nextLine().trim();
        
        Optional<SalesOrder> orderOpt = salesOrderService.getSalesOrderByNumber(orderNumber);
        if (orderOpt.isEmpty()) {
            System.out.println("\n❌ Sales Order not found!");
            return;
        }
        
        SalesOrder order = orderOpt.get();
        List<SalesOrderItem> items = salesOrderService.getItemsByOrderId(order.getId());
        
        System.out.println("\n══════════════════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("                                      SALES ORDER DETAILS");
        System.out.println("══════════════════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("   Order Number   : " + order.getOrderNumber());
        System.out.println("   Order Date     : " + order.getOrderDate().format(dateFormatter));
        System.out.println("   Customer       : " + salesOrderService.getCustomerNameById(order.getCustomerId()));
        System.out.println("   Status         : " + order.getOrderStatus());
        System.out.println("   Description    : " + (order.getDescription() != null ? order.getDescription() : "N/A"));
        
        if (order.getTaxId() != null) {
            BigDecimal taxRate = salesOrderService.getTaxRateById(order.getTaxId());
            System.out.println("   Tax Rate       : " + taxRate + "%");
        }
        
        System.out.println("\n──────────────────────────────────────────────────────────────────────────────────────────────────────────");
        System.out.println("                                          ORDER ITEMS");
        System.out.println("──────────────────────────────────────────────────────────────────────────────────────────────────────────");
        
        if (items.isEmpty()) {
            System.out.println("   No items in this order.");
        } else {
            System.out.printf("%-4s %-25s %-15s %-10s %-12s %-15s%n", 
                "No.", "Product", "Product No.", "Quantity", "Unit Price", "Total");
            System.out.println("──────────────────────────────────────────────────────────────────────────────────────────────────────────");
            
            int counter = 1;
            BigDecimal subtotal = BigDecimal.ZERO;
            
            for (SalesOrderItem item : items) {
                String productName = salesOrderService.getProductNameById(item.getProductId());
                BigDecimal itemTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                subtotal = subtotal.add(itemTotal);
                
                System.out.printf("%-4d %-25s %-15s %-10d %-12.2f %-15.2f%n",
                    counter++,
                    productName,
                    item.getProductNumber() != null ? item.getProductNumber() : "N/A",
                    item.getQuantity(),
                    item.getUnitPrice(),
                    itemTotal);
            }
            
            System.out.println("──────────────────────────────────────────────────────────────────────────────────────────────────────────");
            System.out.printf("%-66s %-15.2f%n", "Subtotal:", subtotal);
            
            if (order.getTaxId() != null) {
                BigDecimal taxRate = salesOrderService.getTaxRateById(order.getTaxId());
                BigDecimal taxAmount = subtotal.multiply(taxRate).divide(BigDecimal.valueOf(100));
                System.out.printf("%-66s %-15.2f%n", "Tax (" + taxRate + "%):", taxAmount);
                System.out.printf("%-66s %-15.2f%n", "Grand Total:", subtotal.add(taxAmount));
            } else {
                System.out.printf("%-66s %-15.2f%n", "Grand Total:", subtotal);
            }
        }
        System.out.println("══════════════════════════════════════════════════════════════════════════════════════════════════════════");
    }

    private void updateSalesOrder() {
        System.out.print("\nEnter Sales Order Number to update: ");
        String orderNumber = scanner.nextLine().trim();
        
        Optional<SalesOrder> orderOpt = salesOrderService.getSalesOrderByNumber(orderNumber);
        if (orderOpt.isEmpty()) {
            System.out.println("\n❌ Sales Order not found!");
            return;
        }
        
        SalesOrder order = orderOpt.get();
        System.out.println("\n══════════════════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("                                         UPDATE SALES ORDER");
        System.out.println("══════════════════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("   Current Order: " + order.getOrderNumber());
        System.out.println("   Customer: " + salesOrderService.getCustomerNameById(order.getCustomerId()));
        System.out.println("   Status: " + order.getOrderStatus());
        System.out.println("\n   Enter new values (press Enter to keep current value):");
        
        // Update Customer
        String currentCustomer = salesOrderService.getCustomerNameById(order.getCustomerId());
        System.out.print("\n   Customer Name [" + currentCustomer + "]: ");
        String newCustomer = scanner.nextLine().trim();
        if (!newCustomer.isEmpty()) {
            String newCustomerId = salesOrderService.getCustomerIdByName(newCustomer);
            if (newCustomerId != null) {
                order.setCustomerId(newCustomerId);
            } else {
                System.out.println("   ⚠️  Customer not found. Keeping current customer.");
            }
        }
        
        // Update Order Date
        System.out.print("   Order Date [" + order.getOrderDate().format(dateFormatter) + "]: ");
        String dateInput = scanner.nextLine().trim();
        if (!dateInput.isEmpty()) {
            try {
                order.setOrderDate(LocalDate.parse(dateInput, dateFormatter));
            } catch (DateTimeParseException e) {
                System.out.println("   ⚠️  Invalid date format. Keeping current date.");
            }
        }
        
        // Update Tax
        System.out.print("   Update tax? (yes/no): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("yes")) {
            String newTaxId = selectTax();
            order.setTaxId(newTaxId);
        }
        
        // Update Status
        System.out.print("   Status [" + order.getOrderStatus() + "]: ");
        String status = scanner.nextLine().trim();
        if (!status.isEmpty()) {
            order.setOrderStatus(status.toUpperCase());
        }
        
        // Update Description
        String currentDesc = order.getDescription() != null ? order.getDescription() : "";
        System.out.print("   Description [" + currentDesc + "]: ");
        String description = scanner.nextLine().trim();
        if (!description.isEmpty()) {
            order.setDescription(description);
        }
        
        try {
            salesOrderService.updateSalesOrder(order);
            System.out.println("\n✅ Sales Order updated successfully!");
        } catch (Exception e) {
            System.out.println("\n❌ Error updating sales order: " + e.getMessage());
        }
    }

    private void deleteSalesOrder() {
        System.out.print("\nEnter Sales Order Number to delete: ");
        String orderNumber = scanner.nextLine().trim();
        
        Optional<SalesOrder> orderOpt = salesOrderService.getSalesOrderByNumber(orderNumber);
        if (orderOpt.isEmpty()) {
            System.out.println("\n❌ Sales Order not found!");
            return;
        }
        
        SalesOrder order = orderOpt.get();
        
        System.out.println("\n⚠️  WARNING: This action cannot be undone!");
        System.out.println("   Order to delete: " + order.getOrderNumber());
        System.out.println("   Customer: " + salesOrderService.getCustomerNameById(order.getCustomerId()));
        System.out.println("   Items: " + salesOrderService.getItemsByOrderId(order.getId()).size());
        
        System.out.print("\nAre you sure you want to delete this order? (yes/no): ");
        String confirmation = scanner.nextLine().trim();
        
        if (confirmation.equalsIgnoreCase("yes")) {
            try {
                salesOrderService.deleteSalesOrder(order.getId());
                System.out.println("\n✅ Sales Order deleted successfully!");
            } catch (Exception e) {
                System.out.println("\n❌ Error deleting sales order: " + e.getMessage());
            }
        } else {
            System.out.println("\n❌ Deletion cancelled.");
        }
    }

    // ==================== ORDER ITEMS MANAGEMENT ====================
    private void manageOrderItems() {
        System.out.print("\nEnter Sales Order Number: ");
        String orderNumber = scanner.nextLine().trim();
        
        Optional<SalesOrder> orderOpt = salesOrderService.getSalesOrderByNumber(orderNumber);
        if (orderOpt.isEmpty()) {
            System.out.println("\n❌ Sales Order not found!");
            return;
        }
        
        String orderId = orderOpt.get().getId();
        boolean back = false;
        
        while (!back) {
            System.out.println("\n╔══════════════════════════════════════════╗");
            System.out.println("║      MANAGE ORDER ITEMS - " + orderNumber + "     ║");
            System.out.println("╠══════════════════════════════════════════╣");
            System.out.println("║  1. View Items                           ║");
            System.out.println("║  2. Add Item                             ║");
            System.out.println("║  3. Update Item                          ║");
            System.out.println("║  4. Remove Item                          ║");
            System.out.println("║  5. Back                                 ║");
            System.out.println("╚══════════════════════════════════════════╝");
            System.out.print("\nEnter your choice (1-5): ");
            
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    viewOrderItems(orderId);
                    break;
                case "2":
                    addItemToOrder(orderId);
                    break;
                case "3":
                    updateOrderItem(orderId);
                    break;
                case "4":
                    removeOrderItem(orderId);
                    break;
                case "5":
                    back = true;
                    break;
                default:
                    System.out.println("\n❌ Invalid choice! Please enter a number between 1-5.");
            }
        }
    }

    private void viewOrderItems(String orderId) {
        List<SalesOrderItem> items = salesOrderService.getItemsByOrderId(orderId);
        
        if (items.isEmpty()) {
            System.out.println("\n📭 No items found in this order.");
            return;
        }
        
        System.out.println("\n══════════════════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("                                          ORDER ITEMS");
        System.out.println("══════════════════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.printf("%-4s %-25s %-15s %-10s %-12s %-15s%n", 
            "No.", "Product", "Product No.", "Quantity", "Unit Price", "Total");
        System.out.println("──────────────────────────────────────────────────────────────────────────────────────────────────────────");
        
        int counter = 1;
        BigDecimal total = BigDecimal.ZERO;
        
        for (SalesOrderItem item : items) {
            String productName = salesOrderService.getProductNameById(item.getProductId());
            BigDecimal itemTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(itemTotal);
            
            System.out.printf("%-4d %-25s %-15s %-10d %-12.2f %-15.2f%n",
                counter++,
                productName,
                item.getProductNumber() != null ? item.getProductNumber() : "N/A",
                item.getQuantity(),
                item.getUnitPrice(),
                itemTotal);
        }
        
        System.out.println("──────────────────────────────────────────────────────────────────────────────────────────────────────────");
        System.out.printf("%-66s %-15.2f%n", "Total:", total);
        System.out.println("══════════════════════════════════════════════════════════════════════════════════════════════════════════");
    }

    private void addItemToOrder(String orderId) {
        System.out.println("\n══════════════════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("                                          ADD ITEM");
        System.out.println("══════════════════════════════════════════════════════════════════════════════════════════════════════════");
        
        System.out.print("\nEnter Product Name: ");
        String productName = scanner.nextLine().trim();
        String productId = salesOrderService.getProductIdByName(productName);
        
        if (productId == null) {
            System.out.println("\n❌ Product not found!");
            return;
        }
        
        BigDecimal productPrice = salesOrderService.getProductPriceById(productId);
        System.out.println("   Product Price: $" + productPrice);
        
        System.out.print("   Use this price? (yes/no) [yes]: ");
        String usePrice = scanner.nextLine().trim();
        
        BigDecimal unitPrice = productPrice;
        if (usePrice.equalsIgnoreCase("no")) {
            System.out.print("   Enter custom unit price: ");
            try {
                unitPrice = new BigDecimal(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("   ⚠️  Invalid price. Using product price: $" + productPrice);
                unitPrice = productPrice;
            }
        }
        
        System.out.print("   Enter Quantity: ");
        int quantity;
        try {
            quantity = Integer.parseInt(scanner.nextLine().trim());
            if (quantity <= 0) {
                System.out.println("   ⚠️  Quantity must be positive. Setting to 1.");
                quantity = 1;
            }
        } catch (NumberFormatException e) {
            System.out.println("   ⚠️  Invalid quantity. Setting to 1.");
            quantity = 1;
        }
        
        System.out.print("   Product Number (optional): ");
        String productNumber = scanner.nextLine().trim();
        
        SalesOrderItem item = new SalesOrderItem();
        item.setSalesOrderId(orderId);
        item.setProductId(productId);
        item.setUnitPrice(unitPrice);
        item.setQuantity(quantity);
        item.setProductNumber(productNumber.isEmpty() ? null : productNumber);
        
        try {
            salesOrderService.addSalesOrderItem(item);
            System.out.println("\n✅ Item added successfully!");
        } catch (Exception e) {
            System.out.println("\n❌ Error adding item: " + e.getMessage());
        }
    }

    private void addItemsToOrder(String orderId) {
        boolean addMore = true;
        
        while (addMore) {
            addItemToOrder(orderId);
            
            System.out.print("\nAdd another item? (yes/no): ");
            String choice = scanner.nextLine().trim();
            addMore = choice.equalsIgnoreCase("yes");
        }
    }

    // ==================== DELIVERY ORDERS MANAGEMENT ====================
    private void manageDeliveryOrders() {
        boolean back = false;
        
        while (!back) {
            System.out.println("\n╔══════════════════════════════════════════╗");
            System.out.println("║        MANAGE DELIVERY ORDERS            ║");
            System.out.println("╠══════════════════════════════════════════╣");
            System.out.println("║  1. View All Delivery Orders             ║");
            System.out.println("║  2. Create Delivery Order                ║");
            System.out.println("║  3. Update Delivery Order                ║");
            System.out.println("║  4. Delete Delivery Order                ║");
            System.out.println("║  5. Back to Main Menu                    ║");
            System.out.println("╚══════════════════════════════════════════╝");
            System.out.print("\nEnter your choice (1-5): ");
            
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    viewAllDeliveryOrders();
                    break;
                case "2":
                    createDeliveryOrder();
                    break;
                case "3":
                    updateDeliveryOrder();
                    break;
                case "4":
                    deleteDeliveryOrder();
                    break;
                case "5":
                    back = true;
                    break;
                default:
                    System.out.println("\n❌ Invalid choice! Please enter a number between 1-5.");
            }
        }
    }

    // ==================== SALES RETURNS MANAGEMENT ====================
    private void manageSalesReturns() {
        boolean back = false;
        
        while (!back) {
            System.out.println("\n╔══════════════════════════════════════════╗");
            System.out.println("║          MANAGE SALES RETURNS            ║");
            System.out.println("╠══════════════════════════════════════════╣");
            System.out.println("║  1. View All Sales Returns               ║");
            System.out.println("║  2. Create Sales Return                  ║");
            System.out.println("║  3. Update Sales Return                  ║");
            System.out.println("║  4. Delete Sales Return                  ║");
            System.out.println("║  5. Back to Main Menu                    ║");
            System.out.println("╚══════════════════════════════════════════╝");
            System.out.print("\nEnter your choice (1-5): ");
            
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    viewAllSalesReturns();
                    break;
                case "2":
                    createSalesReturn();
                    break;
                case "3":
                    updateSalesReturn();
                    break;
                case "4":
                    deleteSalesReturn();
                    break;
                case "5":
                    back = true;
                    break;
                default:
                    System.out.println("\n❌ Invalid choice! Please enter a number between 1-5.");
            }
        }
    }

    // ==================== SALES REPORTS ====================
    private void viewSalesReports() {
        boolean back = false;
        
        while (!back) {
            System.out.println("\n╔══════════════════════════════════════════╗");
            System.out.println("║              SALES REPORTS               ║");
            System.out.println("╠══════════════════════════════════════════╣");
            System.out.println("║  1. Sales Summary Report                 ║");
            System.out.println("║  2. Daily Sales Report                   ║");
            System.out.println("║  3. Customer Sales Report                ║");
            System.out.println("║  4. Product Sales Report                 ║");
            System.out.println("║  5. Back to Main Menu                    ║");
            System.out.println("╚══════════════════════════════════════════╝");
            System.out.print("\nEnter your choice (1-5): ");
            
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    viewSalesSummary();
                    break;
                case "2":
                    viewDailySales();
                    break;
                case "3":
                    viewCustomerSales();
                    break;
                case "4":
                    viewProductSales();
                    break;
                case "5":
                    back = true;
                    break;
                default:
                    System.out.println("\n❌ Invalid choice! Please enter a number between 1-5.");
            }
        }
    }

    // ==================== HELPER METHODS ====================
    private LocalDate getDateInput(String prompt, LocalDate defaultValue) {
        System.out.print(prompt + " [" + defaultValue.format(dateFormatter) + "]: ");
        String dateStr = scanner.nextLine().trim();
        
        if (dateStr.isEmpty()) {
            return defaultValue;
        }
        
        try {
            return LocalDate.parse(dateStr, dateFormatter);
        } catch (DateTimeParseException e) {
            System.out.println("⚠️  Invalid date format. Using default: " + defaultValue.format(dateFormatter));
            return defaultValue;
        }
    }

    private String selectTax() {
        List<Tax> taxes = salesOrderService.getAllTaxes();
        
        if (taxes.isEmpty()) {
            System.out.println("No taxes available.");
            return null;
        }
        
        System.out.println("\nSelect Tax Rate:");
        System.out.println("0. No Tax");
        for (int i = 0; i < taxes.size(); i++) {
            Tax tax = taxes.get(i);
            System.out.printf("%d. %s - %.2f%%%n", i + 1, tax.getTaxName(), tax.getTaxRate());
        }
        
        System.out.print("Enter choice (0-" + taxes.size() + "): ");
        String choice = scanner.nextLine().trim();
        
        try {
            int taxChoice = Integer.parseInt(choice);
            if (taxChoice == 0) {
                return null;
            } else if (taxChoice > 0 && taxChoice <= taxes.size()) {
                return taxes.get(taxChoice - 1).getId();
            }
        } catch (NumberFormatException e) {
            // Invalid input
        }
        
        System.out.println("⚠️  Invalid choice. No tax will be applied.");
        return null;
    }

    private BigDecimal calculateOrderTotal(SalesOrder order, List<SalesOrderItem> items) {
        BigDecimal subtotal = BigDecimal.ZERO;
        
        for (SalesOrderItem item : items) {
            subtotal = subtotal.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        
        if (order.getTaxId() != null) {
            BigDecimal taxRate = salesOrderService.getTaxRateById(order.getTaxId());
            BigDecimal taxAmount = subtotal.multiply(taxRate).divide(BigDecimal.valueOf(100));
            return subtotal.add(taxAmount);
        }
        
        return subtotal;
    }

    // ==================== STUB METHODS (to be implemented) ====================
    private void updateOrderItem(String orderId) {
        System.out.println("\n⚠️  Update Order Item - To be implemented");
    }

    private void removeOrderItem(String orderId) {
        System.out.println("\n⚠️  Remove Order Item - To be implemented");
    }

    private void viewAllDeliveryOrders() {
        System.out.println("\n⚠️  View All Delivery Orders - To be implemented");
    }

    private void createDeliveryOrder() {
        System.out.println("\n⚠️  Create Delivery Order - To be implemented");
    }

    private void updateDeliveryOrder() {
        System.out.println("\n⚠️  Update Delivery Order - To be implemented");
    }

    private void deleteDeliveryOrder() {
        System.out.println("\n⚠️  Delete Delivery Order - To be implemented");
    }

    private void viewAllSalesReturns() {
        System.out.println("\n⚠️  View All Sales Returns - To be implemented");
    }

    private void createSalesReturn() {
        System.out.println("\n⚠️  Create Sales Return - To be implemented");
    }

    private void updateSalesReturn() {
        System.out.println("\n⚠️  Update Sales Return - To be implemented");
    }

    private void deleteSalesReturn() {
        System.out.println("\n⚠️  Delete Sales Return - To be implemented");
    }

    private void viewSalesSummary() {
        System.out.println("\n⚠️  Sales Summary Report - To be implemented");
    }

    private void viewDailySales() {
        System.out.println("\n⚠️  Daily Sales Report - To be implemented");
    }

    private void viewCustomerSales() {
        System.out.println("\n⚠️  Customer Sales Report - To be implemented");
    }

    private void viewProductSales() {
        System.out.println("\n⚠️  Product Sales Report - To be implemented");
    }
}