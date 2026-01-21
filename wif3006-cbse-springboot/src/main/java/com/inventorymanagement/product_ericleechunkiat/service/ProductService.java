package com.inventorymanagement.product_ericleechunkiat.service;

import com.inventorymanagement.product_ericleechunkiat.model.*;
import com.inventorymanagement.product_ericleechunkiat.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    // Inject ALL Repositories here
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

    // ================= GROUP LOGIC =================
    public List<ProductGroup> getAllGroups() { return groupRepository.findAll(); }

    public ProductGroup addGroup(ProductGroup group) {
        if (groupRepository.existsByGroupNameIgnoreCase(group.getGroupName())) {
            throw new RuntimeException("Error: Group '" + group.getGroupName() + "' already exists.");
        }
        return groupRepository.save(group);
    }

    public ProductGroup updateGroup(String id, ProductGroup newDetails) {
        return groupRepository.findById(id).map(g -> {
            g.setGroupName(newDetails.getGroupName());
            g.setDescription(newDetails.getDescription());
            return groupRepository.save(g);
        }).orElseThrow(() -> new RuntimeException("Group not found"));
    }

    public void deleteGroup(String id) {
        if (!productRepository.findByProductGroupId(id).isEmpty()) {
            throw new RuntimeException("Cannot Delete: Group is in use.");
        }
        groupRepository.deleteById(id);
    }

    // ================= UOM LOGIC =================
    public List<UnitMeasure> getAllUOMs() { return uomRepository.findAll(); }

    public UnitMeasure addUOM(UnitMeasure uom) {
        if (uomRepository.existsByUnitNameIgnoreCase(uom.getUnitName())) {
            throw new RuntimeException("Error: UOM '" + uom.getUnitName() + "' already exists.");
        }
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

    public Warehouse addWarehouse(Warehouse warehouse) {
        if (warehouseRepository.existsByNameIgnoreCase(warehouse.getName())) {
            throw new RuntimeException("Error: Warehouse '" + warehouse.getName() + "' already exists.");
        }
        return warehouseRepository.save(warehouse);
    }

    public Warehouse updateWarehouse(String name, Warehouse newDetails) {
        return warehouseRepository.findById(name).map(w -> {
            w.setDescription(newDetails.getDescription());
            return warehouseRepository.save(w);
        }).orElseThrow(() -> new RuntimeException("Warehouse not found"));
    }

    public void deleteWarehouse(String name) { warehouseRepository.deleteById(name); }

    // ================= STOCK COUNT LOGIC =================
    public List<StockCount> getAllStockCounts() { return stockRepository.findAll(); }
    public StockCount createStockCount(StockCount s) { return stockRepository.save(s); }
}