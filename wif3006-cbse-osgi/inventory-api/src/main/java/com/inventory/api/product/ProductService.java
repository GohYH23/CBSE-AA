package com.inventory.api.product;

import java.util.List;
import java.util.Scanner;

public interface ProductService {
    // Product
    void addProduct(Product product);
    void updateProduct(Product product); // <-- NEW
    void deleteProduct(String id);
    List<Product> getAllProducts();
    Product getProduct(String id);       // <-- NEW

    // Group
    void addProductGroup(ProductGroup group);
    void deleteProductGroup(String id);  // <-- NEW
    List<ProductGroup> getAllProductGroups();

    // UOM
    void addUnitMeasure(UnitMeasure uom);
    void deleteUnitMeasure(String id);   // <-- NEW
    List<UnitMeasure> getAllUnitMeasures();

    // Warehouse
    void addWarehouse(Warehouse warehouse);
    void deleteWarehouse(String name);   // <-- NEW
    List<Warehouse> getAllWarehouses();

    // Stock
    void addStockCount(StockCount stock);
    List<StockCount> getAllStockCounts();

    void showMenu(Scanner scanner);
}