package com.inventory.product;

import com.inventory.api.product.model.*;
import com.inventory.api.product.service.ProductService;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component(service = ProductService.class, immediate = true)
public class ProductServiceImpl implements ProductService {

    private MongoClient mongoClient;
    private MongoDatabase database;

    // 👇 THESE FIELDS MUST EXIST SO THE TEST CAN "SEE" THEM
    private MongoCollection<Document> productCollection;
    private MongoCollection<Document> productGroupCollection;
    private MongoCollection<Document> unitMeasureCollection;
    private MongoCollection<Document> warehouseCollection;
    private MongoCollection<Document> stockCountCollection;

    @Activate
    public void activate() {
        Logger.getLogger("org.mongodb.driver").setLevel(Level.SEVERE);
        Logger.getLogger("com.mongodb").setLevel(Level.SEVERE);

        String uri = System.getProperty("mongodb.uri");
        // Fallback for local testing if Property is missing
        if (uri == null) uri = "mongodb+srv://inventory_admin:inventory_admin@cluster0.cwcexht.mongodb.net/?retryWrites=true&w=majority";

        try {
            mongoClient = MongoClients.create(uri);
            database = mongoClient.getDatabase("inventory_db_osgi");

            // 👇 INITIALIZE THE FIELDS HERE (Crucial for testing)
            productCollection = database.getCollection("products");
            productGroupCollection = database.getCollection("product_groups");
            unitMeasureCollection = database.getCollection("unit_measures");
            warehouseCollection = database.getCollection("warehouses");
            stockCountCollection = database.getCollection("stock_counts");

            System.out.println("✅ Product Module Connected to MongoDB.");
        } catch (Exception e) {
            System.err.println("❌ MongoDB Connection Failed: " + e.getMessage());
        }
    }

    @Deactivate
    public void deactivate() {
        if (mongoClient != null) mongoClient.close();
    }

    // --- PRODUCTS ---
    @Override
    public void addProduct(Product p) {
        Document doc = new Document("id", p.getId())
                .append("name", p.getName())
                .append("price", p.getPrice())
                .append("groupId", p.getProductGroupId())
                .append("uomId", p.getUomId());
        // Use the field directly
        productCollection.insertOne(doc);
    }

    @Override
    public void updateProduct(Product p) {
        productCollection.updateOne(Filters.eq("id", p.getId()),
                Updates.combine(
                        Updates.set("name", p.getName()),
                        Updates.set("price", p.getPrice()),
                        Updates.set("groupId", p.getProductGroupId()),
                        Updates.set("uomId", p.getUomId())
                ));
    }

    @Override
    public void deleteProduct(String id) {
        productCollection.deleteOne(Filters.eq("id", id));
    }

    @Override
    public Product getProduct(String id) {
        Document d = productCollection.find(Filters.eq("id", id)).first();
        if (d != null) {
            Product p = new Product();
            p.setId(d.getString("id"));
            p.setName(d.getString("name"));
            if (d.get("price") instanceof Double) p.setPrice(d.getDouble("price"));
            p.setProductGroupId(d.getString("groupId"));
            p.setUomId(d.getString("uomId"));
            return p;
        }
        return null;
    }

    @Override
    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        for (Document d : productCollection.find()) {
            Product p = new Product();
            p.setId(d.getString("id"));
            p.setName(d.getString("name"));
            if (d.get("price") instanceof Double) p.setPrice(d.getDouble("price"));
            p.setProductGroupId(d.getString("groupId"));
            p.setUomId(d.getString("uomId"));
            list.add(p);
        }
        return list;
    }

    // --- GROUPS ---
    @Override
    public void addProductGroup(ProductGroup g) {
        productGroupCollection.insertOne(new Document("id", g.getGroupId())
                .append("name", g.getGroupName()).append("desc", g.getDescription()));
    }

    @Override
    public void updateProductGroup(ProductGroup g) {
        productGroupCollection.updateOne(Filters.eq("id", g.getGroupId()),
                Updates.combine(
                        Updates.set("name", g.getGroupName()),
                        Updates.set("desc", g.getDescription())
                ));
    }

    @Override
    public void deleteProductGroup(String id) {
        productGroupCollection.deleteOne(Filters.eq("id", id));
    }

    @Override
    public List<ProductGroup> getAllProductGroups() {
        List<ProductGroup> list = new ArrayList<>();
        for (Document d : productGroupCollection.find()) {
            list.add(new ProductGroup(d.getString("id"), d.getString("name"), d.getString("desc")));
        }
        return list;
    }

    // --- UOM ---
    @Override
    public void addUnitMeasure(UnitMeasure u) {
        unitMeasureCollection.insertOne(new Document("id", u.getUomId())
                .append("name", u.getUnitName()).append("symbol", u.getSymbol()));
    }

    @Override
    public void updateUnitMeasure(UnitMeasure u) {
        unitMeasureCollection.updateOne(Filters.eq("id", u.getUomId()),
                Updates.combine(
                        Updates.set("name", u.getUnitName()),
                        Updates.set("symbol", u.getSymbol())
                ));
    }

    @Override
    public void deleteUnitMeasure(String id) {
        unitMeasureCollection.deleteOne(Filters.eq("id", id));
    }

    @Override
    public List<UnitMeasure> getAllUnitMeasures() {
        List<UnitMeasure> list = new ArrayList<>();
        for (Document d : unitMeasureCollection.find()) {
            list.add(new UnitMeasure(d.getString("id"), d.getString("name"), d.getString("symbol")));
        }
        return list;
    }

    // --- WAREHOUSE ---
    @Override
    public void addWarehouse(Warehouse w) {
        warehouseCollection.insertOne(new Document("name", w.getName())
                .append("isSystem", w.isSystemWarehouse()).append("desc", w.getDescription()));
    }

    @Override
    public void updateWarehouse(Warehouse w) {
        warehouseCollection.updateOne(Filters.eq("name", w.getName()),
                Updates.combine(
                        Updates.set("desc", w.getDescription()),
                        Updates.set("isSystem", w.isSystemWarehouse())
                ));
    }

    @Override
    public void deleteWarehouse(String name) {
        warehouseCollection.deleteOne(Filters.eq("name", name));
    }

    @Override
    public List<Warehouse> getAllWarehouses() {
        List<Warehouse> list = new ArrayList<>();
        for (Document d : warehouseCollection.find()) {
            list.add(new Warehouse(d.getString("name"), d.getBoolean("isSystem", false), d.getString("desc")));
        }
        return list;
    }

    // --- STOCK ---
    @Override
    public void addStockCount(StockCount s) {
        stockCountCollection.insertOne(new Document("id", s.getCountId())
                .append("warehouse", s.getWarehouseName()).append("status", s.getStatus()).append("date", s.getDate()));
    }

    @Override
    public List<StockCount> getAllStockCounts() {
        List<StockCount> list = new ArrayList<>();
        for (Document d : stockCountCollection.find()) {
            list.add(new StockCount(d.getString("id"), d.getString("warehouse"), d.getString("status"), d.getString("date")));
        }
        return list;
    }

    // 👇 NEWLY ADDED METHOD TO FIX THE ERROR
    @Override
    public String completeStockCount(String countId) {
        try {
            // Update the document where "id" matches the countId
            // Note: We use "id" here because addStockCount() uses "id" as the key
            com.mongodb.client.result.UpdateResult result = stockCountCollection.updateOne(
                    Filters.eq("id", countId),
                    Updates.set("status", "Completed")
            );

            if (result.getMatchedCount() > 0) {
                return "✅ Stock Count " + countId + " marked as Completed.";
            } else {
                return "❌ Error: Stock Count ID not found.";
            }
        } catch (Exception e) {
            return "❌ Database Error: " + e.getMessage();
        }
    }

    @Override
    public void showMenu(Scanner scanner) {
        // Handled by Menu Bundle
    }
}