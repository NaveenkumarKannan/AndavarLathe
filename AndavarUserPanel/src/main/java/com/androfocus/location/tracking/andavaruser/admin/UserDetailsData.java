package com.androfocus.location.tracking.andavaruser.admin;

public class UserDetailsData {
    String user_id,name,email,phNo,enable;

    public UserDetailsData(String user_id, String name, String email, String phNo, String enable) {
        this.user_id = user_id;
        this.name = name;
        this.email = email;
        this.phNo = phNo;
        this.enable = enable;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhNo() {
        return phNo;
    }

    public void setPhNo(String phNo) {
        this.phNo = phNo;
    }

    public String getEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        this.enable = enable;
    }
}
