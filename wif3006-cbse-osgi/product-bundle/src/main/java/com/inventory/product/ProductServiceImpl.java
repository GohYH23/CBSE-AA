package com.inventory.product;

import com.inventory.api.product.*;
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
import java.util.logging.Logger;
import java.util.logging.Level;

@Component(service = ProductService.class, immediate = true)
public class ProductServiceImpl implements ProductService {

    private MongoClient mongoClient;
    private MongoDatabase database;

    @Activate
    public void activate() {
        Logger.getLogger("org.mongodb.driver").setLevel(Level.SEVERE);
        Logger.getLogger("com.mongodb").setLevel(Level.SEVERE);

        String uri = System.getProperty("mongodb.uri");
        if (uri != null) {
            try {
                mongoClient = MongoClients.create(uri);
                database = mongoClient.getDatabase("inventory_db_osgi");
                System.out.println("✅ Product Module Connected to MongoDB.");
            } catch (Exception e) {
                System.err.println("❌ MongoDB Connection Failed: " + e.getMessage());
            }
        }
    }

    @Deactivate
    public void deactivate() {
        if (mongoClient != null) mongoClient.close();
    }

    // Safe helper to get collection
    private MongoCollection<Document> getCollection(String name) {
        return (database != null) ? database.getCollection(name) : null;
    }

    // --- PRODUCTS ---
    @Override
    public void addProduct(Product p) {
        MongoCollection<Document> col = getCollection("products");
        if (col != null) {
            Document doc = new Document("id", p.getId())
                    .append("name", p.getName())
                    .append("price", p.getPrice())
                    .append("groupId", p.getProductGroupId())
                    .append("uomId", p.getUomId());
            col.insertOne(doc);
        }
    }

    @Override
    public void updateProduct(Product p) {
        MongoCollection<Document> col = getCollection("products");
        if (col != null) {
            col.updateOne(Filters.eq("id", p.getId()),
                    Updates.combine(
                            Updates.set("name", p.getName()),
                            Updates.set("price", p.getPrice()),
                            Updates.set("groupId", p.getProductGroupId()),
                            Updates.set("uomId", p.getUomId())
                    ));
        }
    }

    @Override
    public void deleteProduct(String id) {
        MongoCollection<Document> col = getCollection("products");
        if (col != null) col.deleteOne(Filters.eq("id", id));
    }

    @Override
    public Product getProduct(String id) {
        MongoCollection<Document> col = getCollection("products");
        if (col != null) {
            Document d = col.find(Filters.eq("id", id)).first();
            if (d != null) {
                Product p = new Product();
                p.setId(d.getString("id"));
                p.setName(d.getString("name"));
                if (d.get("price") instanceof Double) p.setPrice(d.getDouble("price"));
                p.setProductGroupId(d.getString("groupId"));
                p.setUomId(d.getString("uomId"));
                return p;
            }
        }
        return null;
    }

    @Override
    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        MongoCollection<Document> col = getCollection("products");
        if (col != null) {
            for (Document d : col.find()) {
                Product p = new Product();
                p.setId(d.getString("id"));
                p.setName(d.getString("name"));
                if (d.get("price") instanceof Double) p.setPrice(d.getDouble("price"));
                p.setProductGroupId(d.getString("groupId"));
                p.setUomId(d.getString("uomId"));
                list.add(p);
            }
        }
        return list;
    }

    // --- GROUPS ---
    @Override
    public void addProductGroup(ProductGroup g) {
        MongoCollection<Document> col = getCollection("product_groups");
        if (col != null) {
            col.insertOne(new Document("id", g.getGroupId())
                    .append("name", g.getGroupName()).append("desc", g.getDescription()));
        }
    }
    @Override
    public void deleteProductGroup(String id) {
        MongoCollection<Document> col = getCollection("product_groups");
        if (col != null) col.deleteOne(Filters.eq("id", id));
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

    // --- UOM ---
    @Override
    public void addUnitMeasure(UnitMeasure u) {
        MongoCollection<Document> col = getCollection("unit_measures");
        if (col != null) {
            col.insertOne(new Document("id", u.getUomId())
                    .append("name", u.getUnitName()).append("symbol", u.getSymbol()));
        }
    }
    @Override
    public void deleteUnitMeasure(String id) {
        MongoCollection<Document> col = getCollection("unit_measures");
        if (col != null) col.deleteOne(Filters.eq("id", id));
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

    // --- WAREHOUSE ---
    @Override
    public void addWarehouse(Warehouse w) {
        MongoCollection<Document> col = getCollection("warehouses");
        if (col != null) {
            col.insertOne(new Document("name", w.getName())
                    .append("isSystem", w.isSystemWarehouse()).append("desc", w.getDescription()));
        }
    }
    @Override
    public void deleteWarehouse(String name) {
        MongoCollection<Document> col = getCollection("warehouses");
        if (col != null) col.deleteOne(Filters.eq("name", name));
    }
    @Override
    public List<Warehouse> getAllWarehouses() {
        List<Warehouse> list = new ArrayList<>();
        MongoCollection<Document> col = getCollection("warehouses");
        if (col != null) {
            for (Document d : col.find()) {
                list.add(new Warehouse(d.getString("name"), d.getBoolean("isSystem", false), d.getString("desc")));
            }
        }
        return list;
    }

    // --- STOCK ---
    @Override
    public void addStockCount(StockCount s) {
        MongoCollection<Document> col = getCollection("stock_counts");
        if (col != null) {
            col.insertOne(new Document("id", s.getCountId())
                    .append("warehouse", s.getWarehouseName()).append("status", s.getStatus()).append("date", s.getDate()));
        }
    }
    @Override
    public List<StockCount> getAllStockCounts() {
        List<StockCount> list = new ArrayList<>();
        MongoCollection<Document> col = getCollection("stock_counts");
        if (col != null) {
            for (Document d : col.find()) {
                list.add(new StockCount(d.getString("id"), d.getString("warehouse"), d.getString("status"), d.getString("date")));
            }
        }
        return list;
    }

    @Override
    public void showMenu(Scanner scanner) {}
}