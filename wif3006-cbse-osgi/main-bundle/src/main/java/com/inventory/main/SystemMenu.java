package com.inventory.main;

import com.inventory.api.ModuleMenu;
import org.osgi.service.component.annotations.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Component(immediate = true)
public class SystemMenu {

    // This list automatically fills up with "CustomerMenu", "SalesMenu", etc.
    @Reference(
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            bind = "bindMenu",
            unbind = "unbindMenu"
    )
    private volatile List<ModuleMenu> modules = new ArrayList<>();

    public void bindMenu(ModuleMenu menu) {
        synchronized(modules) {
            // Check if we already have this specific instance
            if (modules.contains(menu)) return;

            modules.removeIf(m -> m.getModuleName().equals(menu.getModuleName()));

            modules.add(menu);

            System.out.println("Module Registered: " + menu.getModuleName());
        }
    }
    public void unbindMenu(ModuleMenu menu) {
        modules.remove(menu);
        System.out.println("Module Removed: " + menu.getModuleName());
    }

    @Activate
    public void activate() {
        // Run in a separate thread so we don't block the system start
        new Thread(this::showMainMenu).start();
    }

    private void showMainMenu() {
        try { Thread.sleep(3000); } catch (Exception e) {} // Wait for others to load

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n===========================");
            System.out.println("   INVENTORY SYSTEM MAIN   ");
            System.out.println("===========================");

            if (modules.isEmpty()) {
                System.out.println("   (No modules loaded yet...)");
            } else {
                for (int i = 0; i < modules.size(); i++) {
                    System.out.println((i + 1) + ". " + modules.get(i).getModuleName());
                }
            }
            System.out.println("0. Exit");
            System.out.print("Select Module: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());
                if (choice == 0) System.exit(0);

                if (choice > 0 && choice <= modules.size()) {
                    // JUMP into the sub-menu!
                    modules.get(choice - 1).start();
                }
            } catch (Exception e) {
                System.out.println("Invalid input");
            }
        }
    }
}