package com.inventory.api.product.model;
import java.io.Serializable;

public class UnitMeasure implements Serializable {
    private String uomId;
    private String unitName;
    private String symbol;

    public UnitMeasure() {}
    public UnitMeasure(String uomId, String unitName, String symbol) {
        this.uomId = uomId;
        this.unitName = unitName;
        this.symbol = symbol;
    }
    // Getters and Setters
    public String getUomId() { return uomId; }
    public void setUomId(String uomId) { this.uomId = uomId; }
    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    @Override public String toString() { return unitName + " (" + symbol + ")"; }
}