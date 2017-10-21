package com.designteam1.model;

import java.util.Date;
import java.util.List;

public class LineItem {
    private String _id;
    private String familyID;
    private String studentID;
    private Date checkIn;
    private Date checkOut;
//    private String serviceType;
    private List<ExtraItem> extraItems;
    private double earlyInLateOutFee;
    private double lineTotalCost;
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

//    public String getServiceType() {
//        return serviceType;
//    }
//
//    public void setServiceType(String serviceType) {
//        this.serviceType = serviceType;
//    }

    public List<ExtraItem> getExtraItems() {
        return extraItems;
    }

    public void setExtraItems(List<ExtraItem> extraItems) {
        this.extraItems = extraItems;
    }

    public double getEarlyInLateOutFee() {
        return earlyInLateOutFee;
    }

    public void setEarlyInLateOutFee(double earlyInLateOutFee) {
        this.earlyInLateOutFee = earlyInLateOutFee;
    }

    public double getLineTotalCost() {
        return lineTotalCost;
    }

    public void setLineTotalCost(double lineTotalCost) {
        this.lineTotalCost = lineTotalCost;
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
}
