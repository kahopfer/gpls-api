package com.designteam1.model;

import org.bson.types.Decimal128;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class Invoice {
    private String _id;
    private String familyID;
    private List<String> lineItemsID;
    private Decimal128 totalCost;
    private boolean paid;
    private Date invoiceFromDate;
    private Date invoiceToDate;
    private Date invoiceDate;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getFamilyID() {
        return familyID;
    }

    public void setFamilyID(String familyID) {
        this.familyID = familyID;
    }

    public List<String> getLineItemsID() {
        return lineItemsID;
    }

    public void setLineItemsID(List<String> lineItemsID) {
        this.lineItemsID = lineItemsID;
    }

    public BigDecimal getTotalCost() {
        if (this.totalCost != null) {
            return totalCost.bigDecimalValue();
        } else {
            return null;
        }
    }

    public void setTotalCost(BigDecimal totalCost) {
        if (totalCost != null) {
            this.totalCost = new Decimal128(totalCost);
        }
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public Date getInvoiceFromDate() {
        return invoiceFromDate;
    }

    public void setInvoiceFromDate(Date invoiceFromDate) {
        this.invoiceFromDate = invoiceFromDate;
    }

    public Date getInvoiceToDate() {
        return invoiceToDate;
    }

    public void setInvoiceToDate(Date invoiceToDate) {
        this.invoiceToDate = invoiceToDate;
    }

    public Date getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(Date invoiceDate) {
        this.invoiceDate = invoiceDate;
    }
}
