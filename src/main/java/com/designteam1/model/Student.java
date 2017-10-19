package com.designteam1.model;

import java.util.Date;

public class Student {
    private String _id;
    private String fname;
    private String lname;
    private String mi;
//    private Date birthdate;
    private String notes;
    private boolean checkedIn;
    private String familyUnitID;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getMi() {
        return mi;
    }

    public void setMi(String mi) {
        this.mi = mi;
    }

//    public Date getBirthdate() {
//        return birthdate;
//    }
//
//    public void setBirthdate(Date birthdate) {
//        this.birthdate = birthdate;
//    }

    public String getFamilyUnitID() {
        return familyUnitID;
    }

    public void setFamilyUnitID(String familyUnitID) {
        this.familyUnitID = familyUnitID;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isCheckedIn() {
        return checkedIn;
    }

    public void setCheckedIn(boolean checkedIn) {
        this.checkedIn = checkedIn;
    }
}
