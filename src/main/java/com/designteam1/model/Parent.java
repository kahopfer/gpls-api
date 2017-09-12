package com.designteam1.model;

public class Parent {
    private String _id;
    private String firstName;
    private String lastName;
    private String[] enrolledChildren;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String[] getEnrolledChildren() {
        return enrolledChildren;
    }

    public void setEnrolledChildren(String[] enrolledChildren) {
        this.enrolledChildren = enrolledChildren;
    }
}
