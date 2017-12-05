package com.designteam1.model;

import org.bson.types.Decimal128;

import java.math.BigDecimal;
import java.util.Date;

public class LineItem {
    private String _id;
    private String familyID;
    private String studentID;
    private boolean extraItem;
    private Date checkIn;
    private Date checkOut;
    private String serviceType;
    private Decimal128 earlyInLateOutFee;
    private Decimal128 lineTotalCost;
    private String checkInBy;
    private String checkOutBy;
    private String notes;
    private String invoiceID;

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

    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public Date getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(Date checkIn) {
        this.checkIn = checkIn;
    }

    public Date getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(Date checkOut) {
        this.checkOut = checkOut;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public BigDecimal getEarlyInLateOutFee() {
        if (this.earlyInLateOutFee != null) {
            return earlyInLateOutFee.bigDecimalValue();
        } else {
            return null;
        }
    }

    public void setEarlyInLateOutFee(BigDecimal earlyInLateOutFee) {
        if (earlyInLateOutFee != null) {
            this.earlyInLateOutFee = new Decimal128(earlyInLateOutFee);
        }
    }

    public BigDecimal getLineTotalCost() {
        if (this.lineTotalCost != null) {
            return lineTotalCost.bigDecimalValue();
        } else {
            return null;
        }
    }

    public void setLineTotalCost(BigDecimal lineTotalCost) {
        if (lineTotalCost != null) {
            this.lineTotalCost = new Decimal128(lineTotalCost);
        }
    }

    public String getCheckInBy() {
        return checkInBy;
    }

    public void setCheckInBy(String checkInBy) {
        this.checkInBy = checkInBy;
    }

    public String getCheckOutBy() {
        return checkOutBy;
    }

    public void setCheckOutBy(String checkOutBy) {
        this.checkOutBy = checkOutBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getInvoiceID() {
        return invoiceID;
    }

    public void setInvoiceID(String invoiceID) {
        this.invoiceID = invoiceID;
    }

    public boolean isExtraItem() {
        return extraItem;
    }

    public void setExtraItem(boolean extraItem) {
        this.extraItem = extraItem;
    }
}
