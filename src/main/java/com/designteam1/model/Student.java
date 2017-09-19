package com.designteam1.model;

import java.util.Date;
import java.util.List;

public class Student {
    private String _id;
    private String fname;
    private String lname;
    private String mi;
    private Date birthdate;
    //TODO: Maybe create an enum for allergyCode
    private List<String> allergyCode;
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

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public List<String> getAllergyCode() {
        return allergyCode;
    }

    public void setAllergyCode(List<String> allergyCode) {
        this.allergyCode = allergyCode;
    }

    public String getFamilyUnitID() {
        return familyUnitID;
    }

    public void setFamilyUnitID(String familyUnitID) {
        this.familyUnitID = familyUnitID;
    }
}
