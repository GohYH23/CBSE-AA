package com.inventory.api.product;

import java.util.List;
import java.util.Scanner; // Don't forget to import Scanner!

public interface ProductService {

    // Products
    void addProduct(Product product);
    List<Product> getAllProducts();
    void deleteProduct(String id);

    // Groups
    void addProductGroup(ProductGroup group);
    List<ProductGroup> getAllProductGroups();

    // UOM
    void addUnitMeasure(UnitMeasure uom);
    List<UnitMeasure> getAllUnitMeasures();

    // Warehouses
    void addWarehouse(Warehouse warehouse);
    List<Warehouse> getAllWarehouses();

    // Stock Count (UC-12)
    void addStockCount(StockCount stock);
    List<StockCount> getAllStockCounts();

    void showMenu(Scanner scanner);
}