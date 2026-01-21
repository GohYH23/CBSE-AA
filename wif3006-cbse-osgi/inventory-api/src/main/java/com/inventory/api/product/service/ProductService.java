package com.inventory.api.product.service;

import com.inventory.api.product.model.*;

import java.util.List;
import java.util.Scanner;

public interface ProductService {
    // Product
    void addProduct(Product product);
    void updateProduct(Product product); // <-- NEW
    void deleteProduct(String id);
    List<Product> getAllProducts();
    Product getProduct(String id);       // <-- NEW

    // ... inside ProductService interface ...

    // Group
    void addProductGroup(ProductGroup group);
    void updateProductGroup(ProductGroup group); // <--- NEW
    void deleteProductGroup(String id);
    List<ProductGroup> getAllProductGroups();

    // UOM
    void addUnitMeasure(UnitMeasure uom);
    void updateUnitMeasure(UnitMeasure uom);     // <--- NEW
    void deleteUnitMeasure(String id);
    List<UnitMeasure> getAllUnitMeasures();

    // Warehouse
    void addWarehouse(Warehouse warehouse);
    void updateWarehouse(Warehouse warehouse);   // <--- NEW
    void deleteWarehouse(String name);
    List<Warehouse> getAllWarehouses();

    // Stock
    void addStockCount(StockCount stock);
    List<StockCount> getAllStockCounts();

    void showMenu(Scanner scanner);
}