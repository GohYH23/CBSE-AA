package com.inventorymanagement.product_ericleechunkiat.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "unit_measures")
public class UnitMeasure {
    @Id
    private String uomId;
    private String unitName;
    private String symbol;

    public UnitMeasure() {}

    public UnitMeasure(String uomId, String unitName, String symbol) {
        this.uomId = uomId;
        this.unitName = unitName;
        this.symbol = symbol;
    }

    public String getUomId() { return uomId; }
    public void setUomId(String uomId) { this.uomId = uomId; }
    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
}