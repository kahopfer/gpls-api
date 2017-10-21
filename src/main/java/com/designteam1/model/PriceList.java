package com.designteam1.model;

public class PriceList {
    private String _id;
    private String itemName;
    private String itemValue;
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

    public String getItemValue() {
        return itemValue;
    }

    public void setItemValue(String itemValue) {
        this.itemValue = itemValue;
    }

    public Boolean getItemExtra() {
        return itemExtra;
    }

    public void setItemExtra(Boolean itemExtra) {
        this.itemExtra = itemExtra;
    }
}
