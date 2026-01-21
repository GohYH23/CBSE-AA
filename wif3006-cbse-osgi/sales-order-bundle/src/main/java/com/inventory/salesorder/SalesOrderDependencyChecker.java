package com.inventory.salesorder;

import com.inventory.api.customer.service.CustomerDependencyChecker;
import com.inventory.api.salesorder.model.SalesOrder;
import com.inventory.api.salesorder.service.SalesOrderService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

@Component(service = CustomerDependencyChecker.class, immediate = true)
public class SalesOrderDependencyChecker implements CustomerDependencyChecker {

    @Reference
    private SalesOrderService salesOrderService;

    private int dependencyCount = 0;

    @Override
    public boolean hasDependency(String customerId) {
        if (salesOrderService == null) return false;

        List<SalesOrder> allOrders = salesOrderService.getAllSalesOrders();
        int count = 0;

        if (allOrders != null) {
            for (SalesOrder order : allOrders) {
                if (customerId.equals(order.getCustomerId())) {
                    count++;
                }
            }
        }

        this.dependencyCount = count;

        return count > 0;
    }

    @Override
    public String getDependencyMessage() {
        return "Customer has " + dependencyCount + " existing Sales Order(s).";
    }
}