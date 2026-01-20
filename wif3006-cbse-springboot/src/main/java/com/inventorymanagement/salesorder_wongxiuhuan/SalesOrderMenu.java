package com.inventorymanagement.salesorder_wongxiuhuan;

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
public class SalesOrderMenu {

    @Autowired
    private SalesOrderService salesOrderService;

    private String promptForUpdate(Scanner scanner, String label, String currentValue) {
        System.out.print(label + " [" + currentValue + "]: ");
        String input = scanner.nextLine();
        return input.trim().isEmpty() ? currentValue : input;
    }

    public void start(Scanner scanner) {
        boolean back = false;
        while (!back) {
            System.out.println("\n===========================");
            System.out.println("   SALES ORDER MODULE MENU    ");
            System.out.println("===========================");
            System.out.println("1. Manage Sales Orders");
            System.out.println("2. View Sales Report");
            System.out.println("3. Manage Delivery Orders");
            System.out.println("4. Manage Sales Returns");
            System.out.println("5. Back to Main Menu");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1": handleSalesOrderSubMenu(scanner); break;
                case "2": handleSalesReportSubMenu(scanner); break;
                case "3": handleDeliveryOrderSubMenu(scanner); break;
                case "4": handleSalesReturnSubMenu(scanner); break;
                case "5": back = true; break;
                default: System.out.println("Invalid option. Please try again (1-5)");
            }
        }
    }

    // --- 1. SALES ORDER SUB-MENU ---
    private void handleSalesOrderSubMenu(Scanner scanner) {
        boolean stay = true;
        while (stay) {
            System.out.println("\n--- MANAGE SALES ORDERS ---");
            System.out.println("1. View All Sales Orders");
            System.out.println("2. Add New Sales Order");
            System.out.println("3. Edit Sales Order");
            System.out.println("4. Delete Sales Order");
            System.out.println("5. Manage Sales Order Items");
            System.out.println("6. Back");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    viewAllSalesOrders();
                    break;
                case "2":
                    performAddSalesOrder(scanner);
                    break;
                case "3":
                    performEditSalesOrder(scanner);
                    break;
                case "4":
                    performDeleteSalesOrder(scanner);
                    break;
                case "5":
                    handleSalesOrderItemsSubMenu(scanner);
                    break;
                case "6":
                    stay = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again (1-6)");
            }
        }
    }

    private void viewAllSalesOrders() {
        List<SalesOrder> orders = salesOrderService.getAllSalesOrders();
        if (orders.isEmpty()) {
            System.out.println("No sales orders found.");
        } else {
            System.out.println("\n--- Sales Order List ---");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            System.out.printf("%-15s %-12s %-20s %-12s %-15s %-20s %-20s%n",
                    "Order Number", "Order Date", "Customer", "Tax (%)", "Status", "Created Date", "Updated Date");
            System.out.println("-------------------------------------------------------------------------------------------------------------------------------");

            for (SalesOrder order : orders) {
                String orderDate = (order.getOrderDate() != null) ? order.getOrderDate().toString() : "N/A";
                String customer = salesOrderService.getCustomerNameById(order.getCustomerId());
                String taxRate = (order.getTaxId() != null) ? salesOrderService.getTaxRateById(order.getTaxId()).toString() : "0";
                String createdDate = (order.getCreatedDate() != null) ? order.getCreatedDate().format(formatter) : "N/A";
                String updatedDate = (order.getUpdatedDate() != null) ? order.getUpdatedDate().format(formatter) : "-";

                System.out.printf("%-15s %-12s %-20s %-12s %-15s %-20s %-20s%n",
                        order.getOrderNumber(),
                        orderDate,
                        customer,
                        taxRate,
                        order.getOrderStatus(),
                        createdDate,
                        updatedDate);
            }
        }
    }

    private void performAddSalesOrder(Scanner scanner) {
        System.out.println("\n--- Add New Sales Order ---");
        
        System.out.print("Enter Order Date (yyyy-MM-dd): ");
        String dateStr = scanner.nextLine();
        LocalDate orderDate;
        try {
            orderDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            System.out.println("❌ Invalid date format. Using today's date.");
            orderDate = LocalDate.now();
        }

        System.out.print("Enter Customer Name: ");
        String customerName = scanner.nextLine();
        String customerId = salesOrderService.getCustomerIdByName(customerName);
        if (customerId == null) {
            System.out.println("❌ Customer not found. Please create customer first.");
            return;
        }

        System.out.print("Enter Tax ID (or press Enter to skip): ");
        String taxIdStr = scanner.nextLine();
        String taxId = taxIdStr.trim().isEmpty() ? null : taxIdStr;

        System.out.print("Enter Order Status (PENDING/CONFIRMED/PROCESSING/COMPLETED/CANCELLED): ");
        String status = scanner.nextLine();

        System.out.print("Enter Description: ");
        String description = scanner.nextLine();

        SalesOrder order = new SalesOrder();
        order.setOrderDate(orderDate);
        order.setCustomerId(customerId);
        order.setTaxId(taxId);
        order.setOrderStatus(status.isEmpty() ? "PENDING" : status.toUpperCase());
        order.setDescription(description);

        SalesOrder created = salesOrderService.createSalesOrder(order);
        System.out.println("✅ Sales Order Created! Order Number: " + created.getOrderNumber());
        System.out.println("You can now add items to this order from 'Manage Sales Order Items' menu.");
    }

    private void performEditSalesOrder(Scanner scanner) {
        System.out.print("Enter Order Number to Edit: ");
        String orderNumber = scanner.nextLine();
        Optional<SalesOrder> orderOpt = salesOrderService.getSalesOrderByNumber(orderNumber);

        if (orderOpt.isEmpty()) {
            System.out.println("❌ Sales Order not found.");
            return;
        }

        SalesOrder order = orderOpt.get();
        System.out.println("Editing Sales Order: " + order.getOrderNumber() + " (Press Enter to keep current value)");

        // Update Order Date
        System.out.print("Order Date [" + order.getOrderDate() + "] (yyyy-MM-dd): ");
        String dateInput = scanner.nextLine();
        if (!dateInput.trim().isEmpty()) {
            try {
                order.setOrderDate(LocalDate.parse(dateInput, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            } catch (DateTimeParseException e) {
                System.out.println("⚠️ Invalid date format. Keeping old date.");
            }
        }

        // Update Customer
        String currentCustomer = salesOrderService.getCustomerNameById(order.getCustomerId());
        String newCustomer = promptForUpdate(scanner, "Customer Name", currentCustomer);
        if (!newCustomer.equals(currentCustomer)) {
            String newCustomerId = salesOrderService.getCustomerIdByName(newCustomer);
            if (newCustomerId != null) {
                order.setCustomerId(newCustomerId);
            } else {
                System.out.println("⚠️ Customer not found. Keeping old customer.");
            }
        }

        // Update Status
        order.setOrderStatus(promptForUpdate(scanner, "Order Status", order.getOrderStatus()).toUpperCase());

        // Update Description
        order.setDescription(promptForUpdate(scanner, "Description", 
                order.getDescription() != null ? order.getDescription() : ""));

        salesOrderService.updateSalesOrder(order);
        System.out.println("✅ Sales Order updated successfully.");
    }

    private void performDeleteSalesOrder(Scanner scanner) {
        System.out.print("Enter Order Number to Delete: ");
        String orderNumber = scanner.nextLine();
        Optional<SalesOrder> orderOpt = salesOrderService.getSalesOrderByNumber(orderNumber);

        if (orderOpt.isEmpty()) {
            System.out.println("❌ Sales Order not found.");
            return;
        }

        System.out.print("Are you sure you want to delete order " + orderNumber + "? (yes/no): ");
        String confirm = scanner.nextLine();
        if (confirm.equalsIgnoreCase("yes")) {
            String message = salesOrderService.deleteSalesOrder(orderOpt.get().getId());
            System.out.println(message);
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    // --- SALES ORDER ITEMS SUB-MENU ---
    private void handleSalesOrderItemsSubMenu(Scanner scanner) {
        System.out.print("Enter Order Number: ");
        String orderNumber = scanner.nextLine();
        Optional<SalesOrder> orderOpt = salesOrderService.getSalesOrderByNumber(orderNumber);

        if (orderOpt.isEmpty()) {
            System.out.println("❌ Sales Order not found.");
            return;
        }

        String orderId = orderOpt.get().getId();
        boolean stay = true;

        while (stay) {
            System.out.println("\n--- MANAGE ITEMS FOR ORDER: " + orderNumber + " ---");
            System.out.println("1. View Order Items");
            System.out.println("2. Add Item");
            System.out.println("3. Edit Item");
            System.out.println("4. Delete Item");
            System.out.println("5. Back");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    viewSalesOrderItems(orderId);
                    break;
                case "2":
                    performAddSalesOrderItem(scanner, orderId);
                    break;
                case "3":
                    performEditSalesOrderItem(scanner, orderId);
                    break;
                case "4":
                    performDeleteSalesOrderItem(scanner, orderId);
                    break;
                case "5":
                    stay = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again (1-5)");
            }
        }
    }

    private void viewSalesOrderItems(String orderId) {
        List<SalesOrderItem> items = salesOrderService.getItemsByOrderId(orderId);
        if (items.isEmpty()) {
            System.out.println("No items found for this order.");
        } else {
            System.out.println("\n--- Sales Order Items ---");
            System.out.printf("%-4s %-20s %-15s %-12s %-10s %-15s%n",
                    "No.", "Product", "Product Number", "Unit Price", "Quantity", "Total");
            System.out.println("------------------------------------------------------------------------------");

            int i = 1;
            BigDecimal grandTotal = BigDecimal.ZERO;
            for (SalesOrderItem item : items) {
                String productName = salesOrderService.getProductNameById(item.getProductId());
                BigDecimal itemTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                grandTotal = grandTotal.add(itemTotal);

                System.out.printf("%-4d %-20s %-15s %-12s %-10d %-15s%n",
                        i++,
                        productName,
                        item.getProductNumber() != null ? item.getProductNumber() : "N/A",
                        item.getUnitPrice(),
                        item.getQuantity(),
                        itemTotal);
            }
            System.out.println("------------------------------------------------------------------------------");
            System.out.printf("%-62s %-15s%n", "Grand Total:", grandTotal);
        }
    }

    private void performAddSalesOrderItem(Scanner scanner, String orderId) {
        System.out.println("\n--- Add Sales Order Item ---");

        System.out.print("Enter Product Name: ");
        String productName = scanner.nextLine();
        String productId = salesOrderService.getProductIdByName(productName);
        if (productId == null) {
            System.out.println("❌ Product not found.");
            return;
        }

        System.out.print("Enter Unit Price: ");
        BigDecimal unitPrice;
        try {
            unitPrice = new BigDecimal(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid price format.");
            return;
        }

        System.out.print("Enter Quantity: ");
        int quantity;
        try {
            quantity = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid quantity.");
            return;
        }

        System.out.print("Enter Product Number (or press Enter to skip): ");
        String productNumber = scanner.nextLine();

        SalesOrderItem item = new SalesOrderItem();
        item.setSalesOrderId(orderId);
        item.setProductId(productId);
        item.setUnitPrice(unitPrice);
        item.setQuantity(quantity);
        item.setProductNumber(productNumber.trim().isEmpty() ? null : productNumber);

        salesOrderService.addSalesOrderItem(item);
        System.out.println("✅ Item added successfully!");
    }

    private void performEditSalesOrderItem(Scanner scanner, String orderId) {
        List<SalesOrderItem> items = salesOrderService.getItemsByOrderId(orderId);
        if (items.isEmpty()) {
            System.out.println("No items to edit.");
            return;
        }

        viewSalesOrderItems(orderId);
        System.out.print("Enter item number to edit: ");
        int itemIndex;
        try {
            itemIndex = Integer.parseInt(scanner.nextLine()) - 1;
            if (itemIndex < 0 || itemIndex >= items.size()) {
                System.out.println("❌ Invalid item number.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid input.");
            return;
        }

        SalesOrderItem item = items.get(itemIndex);
        System.out.println("Editing item (Press Enter to keep current value)");

        System.out.print("Unit Price [" + item.getUnitPrice() + "]: ");
        String priceInput = scanner.nextLine();
        if (!priceInput.trim().isEmpty()) {
            try {
                item.setUnitPrice(new BigDecimal(priceInput));
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Invalid price. Keeping old value.");
            }
        }

        System.out.print("Quantity [" + item.getQuantity() + "]: ");
        String qtyInput = scanner.nextLine();
        if (!qtyInput.trim().isEmpty()) {
            try {
                item.setQuantity(Integer.parseInt(qtyInput));
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Invalid quantity. Keeping old value.");
            }
        }

        salesOrderService.updateSalesOrderItem(item);
        System.out.println("✅ Item updated successfully!");
    }

    private void performDeleteSalesOrderItem(Scanner scanner, String orderId) {
        List<SalesOrderItem> items = salesOrderService.getItemsByOrderId(orderId);
        if (items.isEmpty()) {
            System.out.println("No items to delete.");
            return;
        }

        viewSalesOrderItems(orderId);
        System.out.print("Enter item number to delete: ");
        int itemIndex;
        try {
            itemIndex = Integer.parseInt(scanner.nextLine()) - 1;
            if (itemIndex < 0 || itemIndex >= items.size()) {
                System.out.println("❌ Invalid item number.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid input.");
            return;
        }

        SalesOrderItem item = items.get(itemIndex);
        System.out.print("Are you sure you want to delete this item? (yes/no): ");
        String confirm = scanner.nextLine();
        if (confirm.equalsIgnoreCase("yes")) {
            salesOrderService.deleteSalesOrderItem(item.getId());
            System.out.println("✅ Item deleted successfully!");
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    // --- 2. SALES REPORT SUB-MENU ---
    private void handleSalesReportSubMenu(Scanner scanner) {
        System.out.println("\n--- SALES REPORT ---");
        List<SalesOrder> orders = salesOrderService.getAllSalesOrders();
        
        if (orders.isEmpty()) {
            System.out.println("No sales data available.");
            return;
        }

        System.out.println("\n--- Grouped Sales Report ---");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        System.out.printf("%-15s %-20s %-10s %-15s%n",
                "Order Number", "Customer", "Items", "Total Amount");
        System.out.println("------------------------------------------------------------------------");

        BigDecimal overallTotal = BigDecimal.ZERO;
        for (SalesOrder order : orders) {
            List<SalesOrderItem> items = salesOrderService.getItemsByOrderId(order.getId());
            String customer = salesOrderService.getCustomerNameById(order.getCustomerId());
            
            BigDecimal orderTotal = BigDecimal.ZERO;
            for (SalesOrderItem item : items) {
                orderTotal = orderTotal.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }

            // Apply tax if exists
            if (order.getTaxId() != null) {
                BigDecimal taxRate = salesOrderService.getTaxRateById(order.getTaxId());
                BigDecimal taxAmount = orderTotal.multiply(taxRate).divide(BigDecimal.valueOf(100));
                orderTotal = orderTotal.add(taxAmount);
            }

            overallTotal = overallTotal.add(orderTotal);

            System.out.printf("%-15s %-20s %-10d %-15s%n",
                    order.getOrderNumber(),
                    customer,
                    items.size(),
                    orderTotal);
        }
        System.out.println("------------------------------------------------------------------------");
        System.out.printf("%-46s %-15s%n", "Overall Total:", overallTotal);

        // Detailed view option
        System.out.print("\nView detailed items for an order? (Enter Order Number or 'no'): ");
        String input = scanner.nextLine();
        if (!input.equalsIgnoreCase("no")) {
            Optional<SalesOrder> orderOpt = salesOrderService.getSalesOrderByNumber(input);
            if (orderOpt.isPresent()) {
                viewDetailedSalesReport(orderOpt.get());
            } else {
                System.out.println("❌ Order not found.");
            }
        }
    }

    private void viewDetailedSalesReport(SalesOrder order) {
        System.out.println("\n--- Detailed Report for Order: " + order.getOrderNumber() + " ---");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        System.out.println("Customer: " + salesOrderService.getCustomerNameById(order.getCustomerId()));
        System.out.println("Order Date: " + order.getOrderDate().format(formatter));
        System.out.println("Status: " + order.getOrderStatus());
        
        List<SalesOrderItem> items = salesOrderService.getItemsByOrderId(order.getId());
        
        System.out.println("\n--- Items ---");
        System.out.printf("%-20s %-15s %-12s %-10s %-15s%n",
                "Product", "Product Number", "Unit Price", "Quantity", "Total");
        System.out.println("------------------------------------------------------------------------------");

        BigDecimal subtotal = BigDecimal.ZERO;
        for (SalesOrderItem item : items) {
            String productName = salesOrderService.getProductNameById(item.getProductId());
            BigDecimal itemTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(itemTotal);

            System.out.printf("%-20s %-15s %-12s %-10d %-15s%n",
                    productName,
                    item.getProductNumber() != null ? item.getProductNumber() : "N/A",
                    item.getUnitPrice(),
                    item.getQuantity(),
                    itemTotal);
        }

        System.out.println("------------------------------------------------------------------------------");
        System.out.printf("%-58s %-15s%n", "Subtotal:", subtotal);

        if (order.getTaxId() != null) {
            BigDecimal taxRate = salesOrderService.getTaxRateById(order.getTaxId());
            BigDecimal taxAmount = subtotal.multiply(taxRate).divide(BigDecimal.valueOf(100));
            System.out.printf("%-58s %-15s (%s%%)%n", "Tax:", taxAmount, taxRate);
            System.out.printf("%-58s %-15s%n", "Total:", subtotal.add(taxAmount));
        } else {
            System.out.printf("%-58s %-15s%n", "Total:", subtotal);
        }
    }

    // --- 3. DELIVERY ORDER SUB-MENU ---
    private void handleDeliveryOrderSubMenu(Scanner scanner) {
        boolean stay = true;
        while (stay) {
            System.out.println("\n--- MANAGE DELIVERY ORDERS ---");
            System.out.println("1. View All Delivery Orders");
            System.out.println("2. Add New Delivery Order");
            System.out.println("3. Edit Delivery Order");
            System.out.println("4. Delete Delivery Order");
            System.out.println("5. Back");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    viewAllDeliveryOrders();
                    break;
                case "2":
                    performAddDeliveryOrder(scanner);
                    break;
                case "3":
                    performEditDeliveryOrder(scanner);
                    break;
                case "4":
                    performDeleteDeliveryOrder(scanner);
                    break;
                case "5":
                    stay = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again (1-5)");
            }
        }
    }

    private void viewAllDeliveryOrders() {
        List<DeliveryOrder> orders = salesOrderService.getAllDeliveryOrders();
        if (orders.isEmpty()) {
            System.out.println("No delivery orders found.");
        } else {
            System.out.println("\n--- Delivery Order List ---");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            System.out.printf("%-18s %-15s %-18s %-15s %-20s %-20s%n",
                    "Delivery Number", "Delivery Date", "Sales Order", "Status", "Created Date", "Updated Date");
            System.out.println("---------------------------------------------------------------------------------------------------------------------------");

            for (DeliveryOrder order : orders) {
                String deliveryDate = (order.getDeliveryDate() != null) ? order.getDeliveryDate().toString() : "N/A";
                String salesOrderNum = salesOrderService.getSalesOrderNumberById(order.getSalesOrderId());
                String createdDate = (order.getCreatedDate() != null) ? order.getCreatedDate().format(formatter) : "N/A";
                String updatedDate = (order.getUpdatedDate() != null) ? order.getUpdatedDate().format(formatter) : "-";

                System.out.printf("%-18s %-15s %-18s %-15s %-20s %-20s%n",
                        order.getDeliveryNumber(),
                        deliveryDate,
                        salesOrderNum,
                        order.getStatus(),
                        createdDate,
                        updatedDate);
            }
        }
    }

    private void performAddDeliveryOrder(Scanner scanner) {
        System.out.println("\n--- Add New Delivery Order ---");
        
        System.out.print("Enter Delivery Date (yyyy-MM-dd): ");
        String dateStr = scanner.nextLine();
        LocalDate deliveryDate;
        try {
            deliveryDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            System.out.println("❌ Invalid date format. Using today's date.");
            deliveryDate = LocalDate.now();
        }

        System.out.print("Enter Sales Order Number: ");
        String salesOrderNum = scanner.nextLine();
        Optional<SalesOrder> salesOrderOpt = salesOrderService.getSalesOrderByNumber(salesOrderNum);
        if (salesOrderOpt.isEmpty()) {
            System.out.println("❌ Sales Order not found.");
            return;
        }

        System.out.print("Enter Status (PENDING/IN_TRANSIT/DELIVERED/CANCELLED): ");
        String status = scanner.nextLine();

        System.out.print("Enter Description: ");
        String description = scanner.nextLine();

        DeliveryOrder order = new DeliveryOrder();
        order.setDeliveryDate(deliveryDate);
        order.setSalesOrderId(salesOrderOpt.get().getId());
        order.setStatus(status.isEmpty() ? "PENDING" : status.toUpperCase());
        order.setDescription(description);

        DeliveryOrder created = salesOrderService.createDeliveryOrder(order);
        System.out.println("✅ Delivery Order Created! Delivery Number: " + created.getDeliveryNumber());
    }

    private void performEditDeliveryOrder(Scanner scanner) {
        System.out.print("Enter Delivery Number to Edit: ");
        String deliveryNumber = scanner.nextLine();
        Optional<DeliveryOrder> orderOpt = salesOrderService.getDeliveryOrderByNumber(deliveryNumber);

        if (orderOpt.isEmpty()) {
            System.out.println("❌ Delivery Order not found.");
            return;
        }

        DeliveryOrder order = orderOpt.get();
        System.out.println("Editing Delivery Order: " + order.getDeliveryNumber() + " (Press Enter to keep current value)");

        // Update Delivery Date
        System.out.print("Delivery Date [" + order.getDeliveryDate() + "] (yyyy-MM-dd): ");
        String dateInput = scanner.nextLine();
        if (!dateInput.trim().isEmpty()) {
            try {
                order.setDeliveryDate(LocalDate.parse(dateInput, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            } catch (DateTimeParseException e) {
                System.out.println("⚠️ Invalid date format. Keeping old date.");
            }
        }

        // Update Status
        order.setStatus(promptForUpdate(scanner, "Status", order.getStatus()).toUpperCase());

        // Update Description
        order.setDescription(promptForUpdate(scanner, "Description", 
                order.getDescription() != null ? order.getDescription() : ""));

        salesOrderService.updateDeliveryOrder(order);
        System.out.println("✅ Delivery Order updated successfully.");
    }

    private void performDeleteDeliveryOrder(Scanner scanner) {
        System.out.print("Enter Delivery Number to Delete: ");
        String deliveryNumber = scanner.nextLine();
        Optional<DeliveryOrder> orderOpt = salesOrderService.getDeliveryOrderByNumber(deliveryNumber);

        if (orderOpt.isEmpty()) {
            System.out.println("❌ Delivery Order not found.");
            return;
        }

        System.out.print("Are you sure you want to delete delivery order " + deliveryNumber + "? (yes/no): ");
        String confirm = scanner.nextLine();
        if (confirm.equalsIgnoreCase("yes")) {
            String message = salesOrderService.deleteDeliveryOrder(orderOpt.get().getId());
            System.out.println(message);
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    // --- 4. SALES RETURN SUB-MENU ---
    private void handleSalesReturnSubMenu(Scanner scanner) {
        boolean stay = true;
        while (stay) {
            System.out.println("\n--- MANAGE SALES RETURNS ---");
            System.out.println("1. View All Sales Returns");
            System.out.println("2. Add New Sales Return");
            System.out.println("3. Edit Sales Return");
            System.out.println("4. Delete Sales Return");
            System.out.println("5. Back");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    viewAllSalesReturns();
                    break;
                case "2":
                    performAddSalesReturn(scanner);
                    break;
                case "3":
                    performEditSalesReturn(scanner);
                    break;
                case "4":
                    performDeleteSalesReturn(scanner);
                    break;
                case "5":
                    stay = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again (1-5)");
            }
        }
    }

    private void viewAllSalesReturns() {
        List<SalesReturn> returns = salesOrderService.getAllSalesReturns();
        if (returns.isEmpty()) {
            System.out.println("No sales returns found.");
        } else {
            System.out.println("\n--- Sales Return List ---");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            System.out.printf("%-15s %-13s %-18s %-15s %-20s %-20s%n",
                    "Return Number", "Return Date", "Delivery Order", "Status", "Created Date", "Updated Date");
            System.out.println("-----------------------------------------------------------------------------------------------------------------");

            for (SalesReturn returnOrder : returns) {
                String returnDate = (returnOrder.getReturnDate() != null) ? returnOrder.getReturnDate().toString() : "N/A";
                String deliveryNum = salesOrderService.getDeliveryOrderNumberById(returnOrder.getDeliveryOrderId());
                String createdDate = (returnOrder.getCreatedDate() != null) ? returnOrder.getCreatedDate().format(formatter) : "N/A";
                String updatedDate = (returnOrder.getUpdatedDate() != null) ? returnOrder.getUpdatedDate().format(formatter) : "-";

                System.out.printf("%-15s %-13s %-18s %-15s %-20s %-20s%n",
                        returnOrder.getReturnNumber(),
                        returnDate,
                        deliveryNum,
                        returnOrder.getStatus(),
                        createdDate,
                        updatedDate);
            }
        }
    }

    private void performAddSalesReturn(Scanner scanner) {
        System.out.println("\n--- Add New Sales Return ---");
        
        System.out.print("Enter Return Date (yyyy-MM-dd): ");
        String dateStr = scanner.nextLine();
        LocalDate returnDate;
        try {
            returnDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            System.out.println("❌ Invalid date format. Using today's date.");
            returnDate = LocalDate.now();
        }

        System.out.print("Enter Delivery Order Number: ");
        String deliveryNum = scanner.nextLine();
        Optional<DeliveryOrder> deliveryOrderOpt = salesOrderService.getDeliveryOrderByNumber(deliveryNum);
        if (deliveryOrderOpt.isEmpty()) {
            System.out.println("❌ Delivery Order not found.");
            return;
        }

        System.out.print("Enter Status (PENDING/APPROVED/REJECTED/COMPLETED): ");
        String status = scanner.nextLine();

        System.out.print("Enter Description/Reason: ");
        String description = scanner.nextLine();

        SalesReturn salesReturn = new SalesReturn();
        salesReturn.setReturnDate(returnDate);
        salesReturn.setDeliveryOrderId(deliveryOrderOpt.get().getId());
        salesReturn.setStatus(status.isEmpty() ? "PENDING" : status.toUpperCase());
        salesReturn.setDescription(description);

        SalesReturn created = salesOrderService.createSalesReturn(salesReturn);
        System.out.println("✅ Sales Return Created! Return Number: " + created.getReturnNumber());
    }

    private void performEditSalesReturn(Scanner scanner) {
        System.out.print("Enter Return Number to Edit: ");
        String returnNumber = scanner.nextLine();
        Optional<SalesReturn> returnOpt = salesOrderService.getSalesReturnByNumber(returnNumber);

        if (returnOpt.isEmpty()) {
            System.out.println("❌ Sales Return not found.");
            return;
        }

        SalesReturn salesReturn = returnOpt.get();
        System.out.println("Editing Sales Return: " + salesReturn.getReturnNumber() + " (Press Enter to keep current value)");

        // Update Return Date
        System.out.print("Return Date [" + salesReturn.getReturnDate() + "] (yyyy-MM-dd): ");
        String dateInput = scanner.nextLine();
        if (!dateInput.trim().isEmpty()) {
            try {
                salesReturn.setReturnDate(LocalDate.parse(dateInput, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            } catch (DateTimeParseException e) {
                System.out.println("⚠️ Invalid date format. Keeping old date.");
            }
        }

        // Update Status
        salesReturn.setStatus(promptForUpdate(scanner, "Status", salesReturn.getStatus()).toUpperCase());

        // Update Description
        salesReturn.setDescription(promptForUpdate(scanner, "Description", 
                salesReturn.getDescription() != null ? salesReturn.getDescription() : ""));

        salesOrderService.updateSalesReturn(salesReturn);
        System.out.println("✅ Sales Return updated successfully.");
    }

    private void performDeleteSalesReturn(Scanner scanner) {
        System.out.print("Enter Return Number to Delete: ");
        String returnNumber = scanner.nextLine();
        Optional<SalesReturn> returnOpt = salesOrderService.getSalesReturnByNumber(returnNumber);

        if (returnOpt.isEmpty()) {
            System.out.println("❌ Sales Return not found.");
            return;
        }

        System.out.print("Are you sure you want to delete sales return " + returnNumber + "? (yes/no): ");
        String confirm = scanner.nextLine();
        if (confirm.equalsIgnoreCase("yes")) {
            String message = salesOrderService.deleteSalesReturn(returnOpt.get().getId());
            System.out.println(message);
        } else {
            System.out.println("Deletion cancelled.");
        }
    }
}