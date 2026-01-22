package com.inventorymanagement.product_ericleechunkiat.service;

import com.inventorymanagement.product_ericleechunkiat.model.*;
import com.inventorymanagement.product_ericleechunkiat.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired private ProductRepository productRepository;
    @Autowired private ProductGroupRepository groupRepository;
    @Autowired private UnitMeasureRepository uomRepository;
    @Autowired private WarehouseRepository warehouseRepository;
    @Autowired private StockCountRepository stockRepository;

    // ================= PRODUCT LOGIC =================
    public List<Product> getAllProducts() { return productRepository.findAll(); }
    public Optional<Product> getProductById(String id) { return productRepository.findById(id); }

    public Product addProduct(Product product) {
        if (productRepository.existsByNameIgnoreCase(product.getName())) {
            throw new RuntimeException("Error: Product '" + product.getName() + "' already exists.");
        }

        // Optional: If you want Products to be 1, 2, 3 as well, add the logic here.
        // For now, we let the Menu or MongoDB handle Product IDs, or we can use the same logic below.

        return productRepository.save(product);
    }

    public Product updateProduct(String id, Product newDetails) {
        return productRepository.findById(id).map(p -> {
            p.setName(newDetails.getName());
            p.setPrice(newDetails.getPrice());
            p.setProductGroupId(newDetails.getProductGroupId());
            p.setUomId(newDetails.getUomId());
            return productRepository.save(p);
        }).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public void deleteProduct(String id) { productRepository.deleteById(id); }

    // ================= GROUP LOGIC (UPDATED FOR 1, 2, 3 IDs) =================

    public List<ProductGroup> getAllProductGroups() { return groupRepository.findAll(); }

    public Optional<ProductGroup> getProductGroupById(String id) {
        return groupRepository.findById(id);
    }

    // UPDATED: Auto-Increment ID Logic
    public String createProductGroup(String name, String description) {
        if (groupRepository.existsByGroupNameIgnoreCase(name)) {
            return "❌ Error: Group '" + name + "' already exists.";
        }

        // Logic: Find the highest ID and add 1
        List<ProductGroup> allGroups = groupRepository.findAll();
        int nextId = 1; // Start at 1 if list is empty

        if (!allGroups.isEmpty()) {
            int maxId = allGroups.stream()
                    .mapToInt(g -> {
                        try {
                            return Integer.parseInt(g.getId());
                        } catch (NumberFormatException e) {
                            return 0; // Ignore weird IDs
                        }
                    })
                    .max()
                    .orElse(0);
            nextId = maxId + 1;
        }

        ProductGroup group = new ProductGroup();
        group.setId(String.valueOf(nextId)); // Sets ID to "1", "2", etc.
        group.setGroupName(name);
        group.setDescription(description);
        groupRepository.save(group);
        return "✅ Group Added with ID: " + nextId;
    }

    public String updateProductGroup(String id, String newName, String newDesc) {
        Optional<ProductGroup> existingGroup = groupRepository.findById(id);

        if (existingGroup.isPresent()) {
            ProductGroup group = existingGroup.get();
            group.setGroupName(newName);
            group.setDescription(newDesc);
            groupRepository.save(group);
            return "✅ Group Updated Successfully.";
        } else {
            return "❌ Error: Group ID not found.";
        }
    }

    public String deleteProductGroup(String id) {
        if (!productRepository.findByProductGroupId(id).isEmpty()) {
            return "⚠️ Cannot Delete: Group is in use.";
        }
        if (groupRepository.existsById(id)) {
            groupRepository.deleteById(id);
            return "✅ Group Deleted.";
        }
        return "❌ Group not found.";
    }

    // ================= UOM LOGIC (UPDATED FOR 1, 2, 3 IDs) =================
    public List<UnitMeasure> getAllUOMs() { return uomRepository.findAll(); }

    // UPDATED: Auto-Increment ID Logic for UOM
    public UnitMeasure addUOM(UnitMeasure uom) {
        if (uomRepository.existsByUnitNameIgnoreCase(uom.getUnitName())) {
            throw new RuntimeException("Error: UOM '" + uom.getUnitName() + "' already exists.");
        }

        // Logic: Find the highest ID and add 1
        List<UnitMeasure> allUoms = uomRepository.findAll();
        int nextId = 1;

        if (!allUoms.isEmpty()) {
            int maxId = allUoms.stream()
                    .mapToInt(u -> {
                        try {
                            return Integer.parseInt(u.getId());
                        } catch (NumberFormatException e) {
                            return 0;
                        }
                    })
                    .max()
                    .orElse(0);
            nextId = maxId + 1;
        }

        uom.setId(String.valueOf(nextId)); // Sets ID to "1", "2", etc.
        return uomRepository.save(uom);
    }

    public UnitMeasure updateUOM(String id, UnitMeasure newDetails) {
        return uomRepository.findById(id).map(u -> {
            u.setUnitName(newDetails.getUnitName());
            u.setSymbol(newDetails.getSymbol());
            return uomRepository.save(u);
        }).orElseThrow(() -> new RuntimeException("UOM not found"));
    }

    public void deleteUOM(String id) {
        if (!productRepository.findByUomId(id).isEmpty()) {
            throw new RuntimeException("Cannot Delete: UOM is in use.");
        }
        uomRepository.deleteById(id);
    }

    // ================= WAREHOUSE LOGIC =================
    public List<Warehouse> getAllWarehouses() { return warehouseRepository.findAll(); }
    public Warehouse addWarehouse(Warehouse w) { return warehouseRepository.save(w); }
    public void deleteWarehouse(String id) { warehouseRepository.deleteById(id); }

    // ================= STOCK COUNT LOGIC =================
    public List<StockCount> getAllStockCounts() { return stockRepository.findAll(); }
    public StockCount createStockCount(StockCount s) { return stockRepository.save(s); }
}