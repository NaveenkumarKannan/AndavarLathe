package com.androfocus.location.tracking.andavaruser.admin;

public class TrackedLocationsData {
    String user_name,location_address,time,date,lattitude,longitude;

    public TrackedLocationsData(String user_name, String location_address, String time, String date, String lattitude, String longitude) {
        this.user_name = user_name;
        this.location_address = location_address;
        this.time = time;
        this.date = date;
        this.lattitude = lattitude;
        this.longitude = longitude;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getLocation_address() {
        return location_address;
    }

    public void setLocation_address(String location_address) {
        this.location_address = location_address;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLattitude() {
        return lattitude;
    }

    public void setLattitude(String lattitude) {
        this.lattitude = lattitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
