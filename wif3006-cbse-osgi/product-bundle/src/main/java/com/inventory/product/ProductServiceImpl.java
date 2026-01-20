package com.inventory.product;

import com.inventory.api.product.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Component(service = ProductService.class, immediate = true)
public class ProductServiceImpl implements ProductService {

    private MongoClient mongoClient;
    private MongoDatabase database;

    @Activate
    public void activate() {
        String uri = System.getProperty("mongodb.uri");
        if (uri != null) {
            try {
                mongoClient = MongoClients.create(uri);
                database = mongoClient.getDatabase("inventory_db_osgi");
                System.out.println("✅ Product Module Connected to MongoDB!");
            } catch (Exception e) {
                System.err.println("❌ MongoDB Connection Failed: " + e.getMessage());
            }
        }
    }

    @Deactivate
    public void deactivate() {
        if (mongoClient != null) mongoClient.close();
    }

    // --- Helper to get collection safely ---
    private MongoCollection<Document> getCollection(String name) {
        if (database == null) return null;
        return database.getCollection(name);
    }

    // --- UC-09: Product ---
    @Override
    public void addProduct(Product p) {
        MongoCollection<Document> col = getCollection("products");
        if (col != null) {
            Document doc = new Document("id", p.getId())
                    .append("name", p.getName())
                    .append("price", p.getPrice());
            col.insertOne(doc);
        }
    }

    @Override
    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        MongoCollection<Document> col = getCollection("products");
        if (col != null) {
            for (Document doc : col.find()) {
                Product p = new Product();
                p.setId(doc.getString("id"));
                p.setName(doc.getString("name"));
                if (doc.get("price") instanceof Double) p.setPrice(doc.getDouble("price"));
                list.add(p);
            }
        }
        return list;
    }

    @Override
    public void deleteProduct(String id) {
        MongoCollection<Document> col = getCollection("products");
        if (col != null) {
            col.deleteOne(new Document("id", id));
            System.out.println("Deleted Product: " + id);
        }
    }

    // --- UC-10: Product Group ---
    @Override
    public void addProductGroup(ProductGroup g) {
        MongoCollection<Document> col = getCollection("product_groups");
        if (col != null) {
            col.insertOne(new Document("id", g.getGroupId())
                    .append("name", g.getGroupName())
                    .append("desc", g.getDescription()));
        }
    }

    @Override
    public List<ProductGroup> getAllProductGroups() {
        List<ProductGroup> list = new ArrayList<>();
        MongoCollection<Document> col = getCollection("product_groups");
        if (col != null) {
            for (Document d : col.find()) {
                list.add(new ProductGroup(d.getString("id"), d.getString("name"), d.getString("desc")));
            }
        }
        return list;
    }

    // --- UC-11: Unit Measure ---
    @Override
    public void addUnitMeasure(UnitMeasure u) {
        MongoCollection<Document> col = getCollection("unit_measures");
        if (col != null) {
            col.insertOne(new Document("id", u.getUomId())
                    .append("name", u.getUnitName())
                    .append("symbol", u.getSymbol()));
        }
    }

    @Override
    public List<UnitMeasure> getAllUnitMeasures() {
        List<UnitMeasure> list = new ArrayList<>();
        MongoCollection<Document> col = getCollection("unit_measures");
        if (col != null) {
            for (Document d : col.find()) {
                list.add(new UnitMeasure(d.getString("id"), d.getString("name"), d.getString("symbol")));
            }
        }
        return list;
    }

    // --- UC-13: Warehouse ---
    @Override
    public void addWarehouse(Warehouse w) {
        MongoCollection<Document> col = getCollection("warehouses");
        if (col != null) {
            col.insertOne(new Document("name", w.getName())
                    .append("isSystem", w.isSystemWarehouse())
                    .append("desc", w.getDescription()));
        }
    }

    @Override
    public List<Warehouse> getAllWarehouses() {
        List<Warehouse> list = new ArrayList<>();
        MongoCollection<Document> col = getCollection("warehouses");
        if (col != null) {
            for (Document d : col.find()) {
                list.add(new Warehouse(d.getString("name"),
                        d.getBoolean("isSystem", false),
                        d.getString("desc")));
            }
        }
        return list;
    }

    // --- UC-12: Stock Count ---
    @Override
    public void addStockCount(StockCount s) {
        MongoCollection<Document> col = getCollection("stock_counts");
        if (col != null) {
            col.insertOne(new Document("id", s.getCountId())
                    .append("warehouse", s.getWarehouseName())
                    .append("status", s.getStatus())
                    .append("date", s.getDate()));
        }
    }

    @Override
    public List<StockCount> getAllStockCounts() {
        List<StockCount> list = new ArrayList<>();
        MongoCollection<Document> col = getCollection("stock_counts");
        if (col != null) {
            for (Document d : col.find()) {
                list.add(new StockCount(d.getString("id"), d.getString("warehouse"),
                        d.getString("status"), d.getString("date")));
            }
        }
        return list;
    }

    @Override
    public void showMenu(Scanner scanner) {
        // Handled by ProductMenu.java
    }
}