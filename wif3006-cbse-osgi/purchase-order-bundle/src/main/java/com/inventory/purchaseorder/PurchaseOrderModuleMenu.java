// Name: Ooi Wei Ying
// Student ID: 22056924

package com.inventory.purchaseorder;

import com.inventory.api.ModuleMenu;
import com.inventory.api.purchaseorder.service.PurchaseOrderService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Scanner;

@Component(service = ModuleMenu.class)
public class PurchaseOrderModuleMenu implements ModuleMenu {

    @Reference
    private PurchaseOrderService purchaseOrderService;

    @Override
    public String getModuleName() {
        return "Purchase Management Module";
    }

    @Override
    public void start() {
        Scanner scanner = new Scanner(System.in);
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
                    // Access PurchaseOrderServiceImpl to call menu method
                    if (purchaseOrderService instanceof PurchaseOrderServiceImpl) {
                        ((PurchaseOrderServiceImpl) purchaseOrderService).showPurchaseOrderMenu(scanner);
                    } else {
                        System.out.println("Error: Purchase Order Service is not available.");
                    }
                    break;
                case "2":
                    if (purchaseOrderService instanceof PurchaseOrderServiceImpl) {
                        ((PurchaseOrderServiceImpl) purchaseOrderService).showGoodsReceiveMenu(scanner);
                    } else {
                        System.out.println("Error: Purchase Order Service is not available.");
                    }
                    break;
                case "3":
                    if (purchaseOrderService instanceof PurchaseOrderServiceImpl) {
                        ((PurchaseOrderServiceImpl) purchaseOrderService).showPurchaseReturnMenu(scanner);
                    } else {
                        System.out.println("Error: Purchase Order Service is not available.");
                    }
                    break;
                case "4":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option. Please try again (1-4)");
            }
        }
    }
}
