package com.designteam1.model;

import org.bson.types.Decimal128;

import java.math.BigDecimal;

public class ExtraItem {
    private String itemName;
    private Decimal128 itemValue;

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public BigDecimal getItemValue() {
        return itemValue.bigDecimalValue();
    }

    public void setItemValue(BigDecimal itemValue) {
        this.itemValue = new Decimal128(itemValue);
    }
}
