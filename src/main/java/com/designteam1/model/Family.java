package com.designteam1.model;

import java.util.List;

public class Family {
    private String _id;
    private String familyName;
    private List<String> students;
    private List<String> guardians;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public List<String> getStudents() {
        return students;
    }

    public void setStudents(List<String> students) {
        this.students = students;
    }

    public List<String> getGuardians() {
        return guardians;
    }

    public void setGuardians(List<String> guardians) {
        this.guardians = guardians;
    }
}
