package com.inventorymanagement.product_ericleechunkiat.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "unit_measures")
public class UnitMeasure {

    @Id
    private String id;
    private String unitName;
    private String symbol;

    // --- CONSTRUCTORS ---
    public UnitMeasure() {}

    public UnitMeasure(String id, String unitName, String symbol) {
        this.id = id;
        this.unitName = unitName;
        this.symbol = symbol;
    }

    // --- GETTERS AND SETTERS ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
}