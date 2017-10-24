package com.designteam1.model;

import org.bson.types.Decimal128;

import java.math.BigDecimal;

public class PriceList {
    private String _id;
    private String itemName;
    private Decimal128 itemValue;
    private Boolean itemExtra;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

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

    public Boolean getItemExtra() {
        return itemExtra;
    }

    public void setItemExtra(Boolean itemExtra) {
        this.itemExtra = itemExtra;
    }
}
