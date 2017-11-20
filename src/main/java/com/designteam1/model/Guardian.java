package com.designteam1.model;

public class Guardian {
    private String _id;
    private String fname;
    private String lname;
    private String mi;
    private String relationship;
    private String primPhone;
    private String secPhone;
    private String email;
    private String familyUnitID;
    private boolean active;

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

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getPrimPhone() {
        return primPhone;
    }

    public void setPrimPhone(String primPhone) {
        this.primPhone = primPhone;
    }

    public String getSecPhone() {
        return secPhone;
    }

    public void setSecPhone(String secPhone) {
        this.secPhone = secPhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFamilyUnitID() {
        return familyUnitID;
    }

    public void setFamilyUnitID(String familyUnitID) {
        this.familyUnitID = familyUnitID;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
