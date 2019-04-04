package com.androfocus.location.tracking.andavaruser.admin;

import android.graphics.Bitmap;

public class CheckInDataAdmin {
    String name,location,time,date,video_name,video_url,userName;
    String bitmapPhoto;
    public CheckInDataAdmin(String name, String location, String time, String date, String video_name, String video_url, String userName,String bitmapPhoto) {
        this.name = name;
        this.location = location;
        this.time = time;
        this.date = date;
        this.video_name = video_name;
        this.video_url = video_url;
        this.userName = userName;
        this.bitmapPhoto = bitmapPhoto;
    }

    public String getBitmapPhoto() {
        return bitmapPhoto;
    }

    public void setBitmapPhoto(String bitmapPhoto) {
        this.bitmapPhoto = bitmapPhoto;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getVideo_name() {
        return video_name;
    }

    public void setVideo_name(String video_name) {
        this.video_name = video_name;
    }

    public String getVideo_url() {
        return video_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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
}
