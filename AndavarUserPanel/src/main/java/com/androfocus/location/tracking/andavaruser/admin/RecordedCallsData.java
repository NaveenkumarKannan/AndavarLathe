package com.androfocus.location.tracking.andavaruser.admin;

public class RecordedCallsData {
    String name,file_url,duration,date,time,file_name,location_address;

    public RecordedCallsData(String name, String file_url, String duration, String date, String time, String file_name, String location_address) {
        this.name = name;
        this.file_url = file_url;
        this.duration = duration;
        this.date = date;
        this.time = time;
        this.file_name = file_name;
        this.location_address = location_address;
    }

    public String getLocation_address() {
        return location_address;
    }

    public void setLocation_address(String location_address) {
        this.location_address = location_address;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFile_url() {
        return file_url;
    }

    public void setFile_url(String file_url) {
        this.file_url = file_url;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
