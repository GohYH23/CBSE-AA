package com.inventory.salesorder;

import com.inventory.api.ModuleMenu;
import com.inventory.api.salesorder.service.SalesOrderService;
import com.inventory.api.salesorder.model.SalesOrder;
import com.inventory.api.salesorder.model.SalesOrderItem;
import com.inventory.api.salesorder.model.DeliveryOrder;
import com.inventory.api.salesorder.model.SalesReturn;
import com.inventory.api.salesorder.model.Tax;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@Component(service = ModuleMenu.class)
public class SalesOrderMenu implements ModuleMenu {

    @Reference
    private SalesOrderService salesOrderService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public String getModuleName() {
        return "Sales Order Management Module";
    }

    // Format LocalDateTime string to readable format
    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return "-";
        }
        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateStr);
            return dateTime.format(FORMATTER);
        } catch (Exception e) {
            return dateStr;
        }
    }

    // Format LocalDate to readable format
    private String formatLocalDate(LocalDate date) {
        if (date == null) {
            return "N/A";
        }
        return date.format(DATE_FORMATTER);
    }

    // Helper to allow skipping updates on Edit
    private String promptForUpdate(Scanner scanner, String label, String currentValue) {
        System.out.print(label + " [" + currentValue + "]: ");
        String input = scanner.nextLine();
        return input.trim().isEmpty() ? currentValue : input;
    }

    @Override
    public void start() {
        Scanner scanner = new Scanner(System.in);
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
            System.out.println("\n===========================");
            System.out.println("   MANAGE SALES ORDERS    ");
            System.out.println("===========================");
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
                    List<SalesOrder> orders = salesOrderService.getAllSalesOrders();
                    if (orders.isEmpty()) {
                        System.out.println("No sales orders found.");
                    } else {
                        System.out.println("\n--- Sales Order List ---");
                        System.out.printf("%-4s %-23s %-12s %-20s %-10s %-15s %-15s %-15s %-15s %-20s %-20s%n",
                                "No.", "Order Number", "Order Date", "Customer", "Tax (%)", "Before Tax", "Tax Amount", "After Tax", "Status", "Created At", "Edited At");
                        System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

                        int i = 1;
                        for (SalesOrder order : orders) {
                            String orderDate = formatLocalDate(order.getOrderDate());
                            String customer = salesOrderService.getCustomerNameById(order.getCustomerId());
                            
                            String taxRate = "0";
                            if (order.getTaxId() != null && !order.getTaxId().isEmpty()) {
                                BigDecimal rate = salesOrderService.getTaxRateById(order.getTaxId());
                                taxRate = (rate != null) ? rate.toString() : "0";
                            }

                            String created = formatDate(order.getCreatedAt());
                            String edited = formatDate(order.getEditedAt());
                            
                            BigDecimal beforeTax = order.getBeforeTaxAmount() != null ? order.getBeforeTaxAmount() : BigDecimal.ZERO;
                            BigDecimal taxAmt = order.getTaxAmount() != null ? order.getTaxAmount() : BigDecimal.ZERO;
                            BigDecimal afterTax = order.getAfterTaxAmount() != null ? order.getAfterTaxAmount() : BigDecimal.ZERO;

                            System.out.printf("%-4d %-23s %-12s %-20s %-10s %-15s %-15s %-15s %-15s %-20s %-20s%n",
                                    i++, order.getOrderNumber(), orderDate, customer, taxRate, 
                                    beforeTax, taxAmt, afterTax, order.getOrderStatus(), created, edited);
                        }
                    }
                    break;
                case "2":
                    performAddSalesOrder(scanner);
                    break;
                case "3":
                    performEditSalesOrder(scanner);
                    break;
                case "4":
                    System.out.print("Enter Order Number to Delete: ");
                    String delOrderNum = scanner.nextLine();
                    Optional<SalesOrder> delOpt = salesOrderService.getSalesOrderByNumber(delOrderNum);
                    if (delOpt.isPresent()) {
                        String message = salesOrderService.deleteSalesOrder(delOpt.get().getId());
                        System.out.println(message);
                    } else {
                        System.out.println("Sales Order trying to delete is not found.");
                    }
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

    private void performAddSalesOrder(Scanner scanner) {
        System.out.println("\n--- Add New Sales Order ---");
        
        System.out.print("Enter Order Date (yyyy-MM-dd): ");
        String dateStr = scanner.nextLine();
        LocalDate orderDate;
        try {
            orderDate = LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Using today's date.");
            orderDate = LocalDate.now();
        }

        System.out.print("Enter Customer Name: ");
        String customerName = scanner.nextLine();
        String customerId = salesOrderService.getCustomerIdByName(customerName);
        if (customerId == null) {
            System.out.println("Unable to proceed: Customer '" + customerName + "' not found. Create it first.");
            return;
        }

        List<Tax> taxes = salesOrderService.getAllTaxes();
        System.out.println("\nAvailable Taxes:");
        System.out.println("0. No Tax");
        for (int i = 0; i < taxes.size(); i++) {
            System.out.println((i + 1) + ". " + taxes.get(i).getTaxName() + 
                            " - " + taxes.get(i).getTaxRate() + "%");
        }

        System.out.print("Select tax (enter number): ");
        int taxChoice = scanner.nextInt();
        scanner.nextLine();

        String taxId = null;
        if (taxChoice > 0 && taxChoice <= taxes.size()) {
            Tax selectedTax = taxes.get(taxChoice - 1);
            taxId = selectedTax.getId();
        }

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
        order.setBeforeTaxAmount(BigDecimal.ZERO);
        order.setTaxAmount(BigDecimal.ZERO);
        order.setAfterTaxAmount(BigDecimal.ZERO);

        SalesOrder created = salesOrderService.createSalesOrder(order);
        System.out.println("Sales Order Created Successfully! Order Number: " + created.getOrderNumber());
        System.out.println("You can now add items to this order from 'Manage Sales Order Items' menu.");
    }

    private void performEditSalesOrder(Scanner scanner) {
        System.out.print("Enter Order Number to Edit: ");
        String orderNumber = scanner.nextLine();
        Optional<SalesOrder> orderOpt = salesOrderService.getSalesOrderByNumber(orderNumber);

        if (orderOpt.isEmpty()) {
            System.out.println("Sales Order not found.");
            return;
        }

        SalesOrder order = orderOpt.get();
        System.out.println("Editing Sales Order: " + order.getOrderNumber() + " (Press Enter to keep current value)");

        // Update Order Date
        System.out.print("Order Date [" + formatLocalDate(order.getOrderDate()) + "] (yyyy-MM-dd): ");
        String dateInput = scanner.nextLine();
        if (!dateInput.trim().isEmpty()) {
            try {
                order.setOrderDate(LocalDate.parse(dateInput, DATE_FORMATTER));
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Keeping old date.");
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
                System.out.println("Customer '" + newCustomer + "' not found. Keeping old customer.");
            }
        }

        List<Tax> taxes = salesOrderService.getAllTaxes();
        System.out.print("Select new tax (enter number, or press Enter to keep current): ");
        String taxInput = scanner.nextLine();
        if (!taxInput.isEmpty()) {
            int taxChoice = Integer.parseInt(taxInput);
            if (taxChoice == 0) {
                order.setTaxId(null);
            } else if (taxChoice > 0 && taxChoice <= taxes.size()) {
                Tax selectedTax = taxes.get(taxChoice - 1);
                order.setTaxId(selectedTax.getId());
            }
        }

        // Update Status
        order.setOrderStatus(promptForUpdate(scanner, "Order Status", order.getOrderStatus()).toUpperCase());

        // Update Description
        order.setDescription(promptForUpdate(scanner, "Description", 
                order.getDescription() != null ? order.getDescription() : ""));

        salesOrderService.updateSalesOrder(order);
        System.out.println("Sales Order updated successfully.");
    }

    // --- SALES ORDER ITEMS SUB-MENU ---
    private void handleSalesOrderItemsSubMenu(Scanner scanner) {
        System.out.print("Enter Order Number: ");
        String orderNumber = scanner.nextLine();
        Optional<SalesOrder> orderOpt = salesOrderService.getSalesOrderByNumber(orderNumber);

        if (orderOpt.isEmpty()) {
            System.out.println("Error: Sales Order '" + orderNumber + "' not found.");
            return;
        }

        String orderId = orderOpt.get().getId();
        boolean stay = true;

        while (stay) {
            System.out.println("\n===========================");
            System.out.println("   MANAGE ITEMS FOR ORDER: " + orderNumber + "    ");
            System.out.println("===========================");
            System.out.println("1. View Order Items");
            System.out.println("2. Add Item");
            System.out.println("3. Edit Item");
            System.out.println("4. Delete Item");
            System.out.println("5. Back");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine();

            if (choice.equals("5")) {
                stay = false;
                continue;
            }

            switch (choice) {
                case "1":
                    List<SalesOrderItem> items = salesOrderService.getItemsByOrderId(orderId);
                    if (items.isEmpty()) {
                        System.out.println("No items found for this order.");
                    } else {
                        System.out.println("\n--- Sales Order Items ---");
                        System.out.printf("%-4s %-30s %-12s %-10s %-15s %-20s %-20s%n",
                                "No.", "Product", "Unit Price", "Quantity", "Total", "Created At", "Edited At");
                        System.out.println("-------------------------------------------------------------------------------------------------------------------------------");

                        int i = 1;
                        BigDecimal grandTotal = BigDecimal.ZERO;
                        for (SalesOrderItem item : items) {
                            String productName = salesOrderService.getProductNameById(item.getProductId());
                            BigDecimal itemTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                            grandTotal = grandTotal.add(itemTotal);
                            String created = formatDate(item.getCreatedAt());
                            String edited = formatDate(item.getEditedAt());

                            System.out.printf("%-4d %-30s %-12s %-10d %-15s %-20s %-20s%n",
                                    i++, productName, item.getUnitPrice(), item.getQuantity(), 
                                    itemTotal, created, edited);
                        }
                        System.out.println("-------------------------------------------------------------------------------------------------------------------------------");
                        System.out.printf("%-59s %-15s%n", "Grand Total:", grandTotal);
                    }
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
                default:
                    System.out.println("Invalid option. Please try again (1-5)");
            }
        }
    }

    private void performAddSalesOrderItem(Scanner scanner, String orderId) {
        System.out.println("\n--- Add Sales Order Item ---");

        System.out.print("Enter Product Name: ");
        String productName = scanner.nextLine();
        String productId = salesOrderService.getProductIdByName(productName);
        if (productId == null) {
            System.out.println("Unable to proceed: Product '" + productName + "' not found. Create it first.");
            return;
        }

        // Auto-fetch product price
        BigDecimal productPrice = salesOrderService.getProductPriceById(productId);
        System.out.println("Product Price (from database): $" + productPrice);
        
        System.out.print("Use this price? (Y/N, default Y): ");
        String usePrice = scanner.nextLine();
        
        BigDecimal unitPrice = productPrice;
        if (usePrice.trim().equalsIgnoreCase("N")) {
            System.out.print("Enter Custom Unit Price: ");
            try {
                unitPrice = new BigDecimal(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid price format. Using product price: $" + productPrice);
                unitPrice = productPrice;
            }
        }

        System.out.print("Enter Quantity: ");
        int quantity;
        try {
            quantity = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid quantity.");
            return;
        }

        SalesOrderItem item = new SalesOrderItem();
        item.setSalesOrderId(orderId);
        item.setProductId(productId);
        item.setUnitPrice(unitPrice);
        item.setQuantity(quantity);

        salesOrderService.addSalesOrderItem(item);
        System.out.println("Item added successfully! Order totals will be recalculated.");
    }

    private void performEditSalesOrderItem(Scanner scanner, String orderId) {
        List<SalesOrderItem> items = salesOrderService.getItemsByOrderId(orderId);
        if (items.isEmpty()) {
            System.out.println("No items to edit.");
            return;
        }

        System.out.print("Enter Product Name to Edit: ");
        String productName = scanner.nextLine();
        
        Optional<SalesOrderItem> target = items.stream()
                .filter(item -> {
                    String pName = salesOrderService.getProductNameById(item.getProductId());
                    return pName.equalsIgnoreCase(productName);
                })
                .findFirst();

        if (target.isPresent()) {
            SalesOrderItem item = target.get();
            System.out.println("Editing item (Press Enter to keep current value)");

            System.out.print("Unit Price [" + item.getUnitPrice() + "]: ");
            String priceInput = scanner.nextLine();
            if (!priceInput.trim().isEmpty()) {
                try {
                    item.setUnitPrice(new BigDecimal(priceInput));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid price. Keeping old value.");
                }
            }

            System.out.print("Quantity [" + item.getQuantity() + "]: ");
            String qtyInput = scanner.nextLine();
            if (!qtyInput.trim().isEmpty()) {
                try {
                    item.setQuantity(Integer.parseInt(qtyInput));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid quantity. Keeping old value.");
                }
            }

            salesOrderService.updateSalesOrderItem(item);
            System.out.println("Item updated successfully!");
        } else {
            System.out.println("Item not found.");
        }
    }

    private void performDeleteSalesOrderItem(Scanner scanner, String orderId) {
        List<SalesOrderItem> items = salesOrderService.getItemsByOrderId(orderId);
        if (items.isEmpty()) {
            System.out.println("No items to delete.");
            return;
        }

        System.out.print("Enter Product Name to Delete: ");
        String productName = scanner.nextLine();
        
        Optional<SalesOrderItem> target = items.stream()
                .filter(item -> {
                    String pName = salesOrderService.getProductNameById(item.getProductId());
                    return pName.equalsIgnoreCase(productName);
                })
                .findFirst();

        if (target.isPresent()) {
            salesOrderService.deleteSalesOrderItem(target.get().getId());
            System.out.println("Item deleted successfully!");
        } else {
            System.out.println("Item not found.");
        }
    }

    // --- 2. SALES REPORT SUB-MENU ---
    private void handleSalesReportSubMenu(Scanner scanner) {
        System.out.println("\n===========================");
        System.out.println("   SALES REPORT    ");
        System.out.println("===========================");
        
        List<SalesOrder> orders = salesOrderService.getAllSalesOrders();
        
        if (orders.isEmpty()) {
            System.out.println("No sales data available.");
            return;
        }

        System.out.println("\n--- Grouped Sales Report ---");
        System.out.printf("%-23s %-20s %-10s %-15s%n",
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

            System.out.printf("%-23s %-20s %-10d %-15s%n",
                    order.getOrderNumber(), customer, items.size(), orderTotal);
        }
        System.out.println("------------------------------------------------------------------------");
        System.out.printf("%-54s %-15s%n", "Overall Total:", overallTotal);

        // Detailed view option
        System.out.print("\nView detailed items for an order? (Enter Order Number or 'no'): ");
        String input = scanner.nextLine();
        if (!input.equalsIgnoreCase("no")) {
            Optional<SalesOrder> orderOpt = salesOrderService.getSalesOrderByNumber(input);
            if (orderOpt.isPresent()) {
                viewDetailedSalesReport(orderOpt.get());
            } else {
                System.out.println("Order not found.");
            }
        }
    }

    private void viewDetailedSalesReport(SalesOrder order) {
        System.out.println("\n--- Detailed Report for Order: " + order.getOrderNumber() + " ---");
        
        System.out.println("Customer: " + salesOrderService.getCustomerNameById(order.getCustomerId()));
        System.out.println("Order Date: " + formatLocalDate(order.getOrderDate()));
        System.out.println("Status: " + order.getOrderStatus());
        
        List<SalesOrderItem> items = salesOrderService.getItemsByOrderId(order.getId());
        
        System.out.println("\n--- Items ---");
        System.out.printf("%-30s %-12s %-10s %-15s%n",
                "Product", "Unit Price", "Quantity", "Total");
        System.out.println("-----------------------------------------------------------------------");

        BigDecimal subtotal = BigDecimal.ZERO;
        for (SalesOrderItem item : items) {
            String productName = salesOrderService.getProductNameById(item.getProductId());
            BigDecimal itemTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(itemTotal);

            System.out.printf("%-30s %-12s %-10d %-15s%n",
                    productName, item.getUnitPrice(), item.getQuantity(), itemTotal);
        }

        System.out.println("-----------------------------------------------------------------------");
        System.out.printf("%-54s %-15s%n", "Subtotal:", subtotal);

        if (order.getTaxId() != null) {
            BigDecimal taxRate = salesOrderService.getTaxRateById(order.getTaxId());
            BigDecimal taxAmount = subtotal.multiply(taxRate).divide(BigDecimal.valueOf(100));
            System.out.printf("%-54s %-15s (%s%%)%n", "Tax:", taxAmount, taxRate);
            System.out.printf("%-54s %-15s%n", "Total:", subtotal.add(taxAmount));
        } else {
            System.out.printf("%-54s %-15s%n", "Total:", subtotal);
        }
    }

    // --- 3. DELIVERY ORDER SUB-MENU ---
    private void handleDeliveryOrderSubMenu(Scanner scanner) {
        boolean stay = true;
        while (stay) {
            System.out.println("\n===========================");
            System.out.println("   MANAGE DELIVERY ORDERS    ");
            System.out.println("===========================");
            System.out.println("1. View All Delivery Orders");
            System.out.println("2. Add New Delivery Order");
            System.out.println("3. Edit Delivery Order");
            System.out.println("4. Delete Delivery Order");
            System.out.println("5. Back");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    List<DeliveryOrder> orders = salesOrderService.getAllDeliveryOrders();
                    if (orders.isEmpty()) {
                        System.out.println("No delivery orders found.");
                    } else {
                        System.out.println("\n--- Delivery Order List ---");
                        System.out.printf("%-4s %-23s %-15s %-23s %-15s %-30s %-20s %-20s%n",
                                "No.", "Delivery Number", "Delivery Date", "Sales Order", "Status", "Description", "Created At", "Edited At");
                        System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------");

                        int i = 1;
                        for (DeliveryOrder order : orders) {
                            String deliveryDate = formatLocalDate(order.getDeliveryDate());
                            String salesOrderNum = salesOrderService.getSalesOrderNumberById(order.getSalesOrderId());
                            String description = (order.getDescription() != null && !order.getDescription().isEmpty()) 
                                    ? order.getDescription() : "N/A";
                            // Truncate description if too long
                            if (description.length() > 28) {
                                description = description.substring(0, 25) + "...";
                            }
                            String created = formatDate(order.getCreatedAt());
                            String edited = formatDate(order.getEditedAt());

                            System.out.printf("%-4d %-23s %-15s %-23s %-15s %-30s %-20s %-20s%n",
                                    i++, order.getDeliveryNumber(), deliveryDate, salesOrderNum, 
                                    order.getStatus(), description, created, edited);
                        }
                    }
                    break;
                case "2":
                    performAddDeliveryOrder(scanner);
                    break;
                case "3":
                    performEditDeliveryOrder(scanner);
                    break;
                case "4":
                    System.out.print("Enter Delivery Number to Delete: ");
                    String delNum = scanner.nextLine();
                    Optional<DeliveryOrder> delOpt = salesOrderService.getDeliveryOrderByNumber(delNum);
                    if (delOpt.isPresent()) {
                        String message = salesOrderService.deleteDeliveryOrder(delOpt.get().getId());
                        System.out.println(message);
                    } else {
                        System.out.println("Delivery Order trying to delete is not found.");
                    }
                    break;
                case "5":
                    stay = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again (1-5)");
            }
        }
    }

    private void performAddDeliveryOrder(Scanner scanner) {
        System.out.println("\n--- Add New Delivery Order ---");
        
        System.out.print("Enter Delivery Date (yyyy-MM-dd): ");
        String dateStr = scanner.nextLine();
        LocalDate deliveryDate;
        try {
            deliveryDate = LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Using today's date.");
            deliveryDate = LocalDate.now();
        }

        System.out.print("Enter Sales Order Number: ");
        String salesOrderNum = scanner.nextLine();
        Optional<SalesOrder> salesOrderOpt = salesOrderService.getSalesOrderByNumber(salesOrderNum);
        if (salesOrderOpt.isEmpty()) {
            System.out.println("Unable to proceed: Sales Order '" + salesOrderNum + "' not found. Create it first.");
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
        System.out.println("Delivery Order Created Successfully! Delivery Number: " + created.getDeliveryNumber());
    }

    private void performEditDeliveryOrder(Scanner scanner) {
        System.out.print("Enter Delivery Number to Edit: ");
        String deliveryNumber = scanner.nextLine();
        Optional<DeliveryOrder> orderOpt = salesOrderService.getDeliveryOrderByNumber(deliveryNumber);

        if (orderOpt.isEmpty()) {
            System.out.println("Delivery Order not found.");
            return;
        }

        DeliveryOrder order = orderOpt.get();
        System.out.println("Editing Delivery Order: " + order.getDeliveryNumber() + " (Press Enter to keep current value)");

        // Update Delivery Date
        System.out.print("Delivery Date [" + formatLocalDate(order.getDeliveryDate()) + "] (yyyy-MM-dd): ");
        String dateInput = scanner.nextLine();
        if (!dateInput.trim().isEmpty()) {
            try {
                order.setDeliveryDate(LocalDate.parse(dateInput, DATE_FORMATTER));
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Keeping old date.");
            }
        }

        // Update Status
        order.setStatus(promptForUpdate(scanner, "Status", order.getStatus()).toUpperCase());

        // Update Description
        order.setDescription(promptForUpdate(scanner, "Description", 
                order.getDescription() != null ? order.getDescription() : ""));

        salesOrderService.updateDeliveryOrder(order);
        System.out.println("Delivery Order updated successfully.");
    }

    // --- 4. SALES RETURN SUB-MENU ---
    private void handleSalesReturnSubMenu(Scanner scanner) {
        boolean stay = true;
        while (stay) {
            System.out.println("\n===========================");
            System.out.println("   MANAGE SALES RETURNS    ");
            System.out.println("===========================");
            System.out.println("1. View All Sales Returns");
            System.out.println("2. Add New Sales Return");
            System.out.println("3. Edit Sales Return");
            System.out.println("4. Delete Sales Return");
            System.out.println("5. Back");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    List<SalesReturn> returns = salesOrderService.getAllSalesReturns();
                    if (returns.isEmpty()) {
                        System.out.println("No sales returns found.");
                    } else {
                        System.out.println("\n--- Sales Return List ---");
                        System.out.printf("%-4s %-23s %-13s %-23s %-15s %-30s %-20s %-20s%n",
                                "No.", "Return Number", "Return Date", "Delivery Order", "Status", "Description", "Created At", "Edited At");
                        System.out.println("------------------------------------------------------------------------------------------------------------------------------------------");

                        int i = 1;
                        for (SalesReturn returnOrder : returns) {
                            String returnDate = formatLocalDate(returnOrder.getReturnDate());
                            String deliveryNum = salesOrderService.getDeliveryOrderNumberById(returnOrder.getDeliveryOrderId());
                            String description = (returnOrder.getDescription() != null && !returnOrder.getDescription().isEmpty()) 
                                    ? returnOrder.getDescription() : "N/A";
                            // Truncate description if too long
                            if (description.length() > 28) {
                                description = description.substring(0, 25) + "...";
                            }
                            String created = formatDate(returnOrder.getCreatedAt());
                            String edited = formatDate(returnOrder.getEditedAt());

                            System.out.printf("%-4d %-23s %-13s %-23s %-15s %-30s %-20s %-20s%n",
                                    i++, returnOrder.getReturnNumber(), returnDate, deliveryNum, 
                                    returnOrder.getStatus(), description, created, edited);
                        }
                    }
                    break;
                case "2":
                    performAddSalesReturn(scanner);
                    break;
                case "3":
                    performEditSalesReturn(scanner);
                    break;
                case "4":
                    System.out.print("Enter Return Number to Delete: ");
                    String delReturnNum = scanner.nextLine();
                    Optional<SalesReturn> delOpt = salesOrderService.getSalesReturnByNumber(delReturnNum);
                    if (delOpt.isPresent()) {
                        String message = salesOrderService.deleteSalesReturn(delOpt.get().getId());
                        System.out.println(message);
                    } else {
                        System.out.println("Sales Return trying to delete is not found.");
                    }
                    break;
                case "5":
                    stay = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again (1-5)");
            }
        }
    }

    private void performAddSalesReturn(Scanner scanner) {
        System.out.println("\n--- Add New Sales Return ---");
    
        System.out.print("Enter Return Date (yyyy-MM-dd): ");
        String dateStr = scanner.nextLine();
        LocalDate returnDate;
        try {
            returnDate = LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Using today's date.");
            returnDate = LocalDate.now();
        }

        System.out.print("Enter Delivery Order Number: ");
        String deliveryOrderNum = scanner.nextLine();
        Optional<DeliveryOrder> deliveryOrderOpt = salesOrderService.getDeliveryOrderByNumber(deliveryOrderNum);
        if (deliveryOrderOpt.isEmpty()) {
            System.out.println("Unable to proceed: Delivery Order '" + deliveryOrderNum + "' not found. Create it first.");
            return;
        }

        System.out.print("Enter Status (PENDING/PROCESSING/COMPLETED/CANCELLED): ");
        String status = scanner.nextLine();

        System.out.print("Enter Description: ");
        String description = scanner.nextLine();

        SalesReturn salesReturn = new SalesReturn();
        salesReturn.setReturnDate(returnDate);
        salesReturn.setDeliveryOrderId(deliveryOrderOpt.get().getId());
        salesReturn.setStatus(status.isEmpty() ? "PENDING" : status.toUpperCase());
        salesReturn.setDescription(description);

        SalesReturn created = salesOrderService.createSalesReturn(salesReturn);
        System.out.println("Sales Return Created Successfully! Return Number: " + created.getReturnNumber());
    }   

    private void performEditSalesReturn(Scanner scanner) {
        System.out.print("Enter Return Number to Edit: ");
        String returnNumber = scanner.nextLine();
        Optional<SalesReturn> returnOpt = salesOrderService.getSalesReturnByNumber(returnNumber);

        if (returnOpt.isEmpty()) {
            System.out.println("Sales Return not found.");
            return;
        }

        SalesReturn salesReturn = returnOpt.get();
        System.out.println("Editing Sales Return: " + salesReturn.getReturnNumber() + " (Press Enter to keep current value)");

        // Update Return Date
        System.out.print("Return Date [" + formatLocalDate(salesReturn.getReturnDate()) + "] (yyyy-MM-dd): ");
        String dateInput = scanner.nextLine();
        if (!dateInput.trim().isEmpty()) {
            try {
                salesReturn.setReturnDate(LocalDate.parse(dateInput, DATE_FORMATTER));
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Keeping old date.");
            }
        }

        // Update Status
        salesReturn.setStatus(promptForUpdate(scanner, "Status", salesReturn.getStatus()).toUpperCase());

        // Update Description
        salesReturn.setDescription(promptForUpdate(scanner, "Description", 
                salesReturn.getDescription() != null ? salesReturn.getDescription() : ""));

        salesOrderService.updateSalesReturn(salesReturn);
        System.out.println("Sales Return updated successfully.");
    }
}