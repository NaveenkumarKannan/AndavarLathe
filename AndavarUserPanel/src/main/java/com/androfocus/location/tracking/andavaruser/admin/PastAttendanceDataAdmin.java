package com.androfocus.location.tracking.andavaruser.admin;

public class PastAttendanceDataAdmin {
    String start_time,end_time,date,duration,userName,locationStart,locationEnd;

    public PastAttendanceDataAdmin(String start_time, String end_time, String date, String duration, String userName, String locationStart, String locationEnd) {
        this.start_time = start_time;
        this.end_time = end_time;
        this.date = date;
        this.duration = duration;
        this.userName = userName;
        this.locationStart = locationStart;
        this.locationEnd = locationEnd;
    }


    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getLocationStart() {
        return locationStart;
    }

    public void setLocationStart(String locationStart) {
        this.locationStart = locationStart;
    }

    public String getLocationEnd() {
        return locationEnd;
    }

    public void setLocationEnd(String locationEnd) {
        this.locationEnd = locationEnd;
    }
}
