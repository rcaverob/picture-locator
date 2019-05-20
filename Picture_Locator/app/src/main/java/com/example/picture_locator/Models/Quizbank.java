package com.example.picture_locator.Models;

import com.google.android.gms.maps.model.LatLng;

public class Quizbank {
    private String userName;
    private String imageUrl;
    private LatLng locationCoord;
    private String addressName;

    public Quizbank() {
    }

    public Quizbank(String userName, String imageUrl, LatLng locationCoord, String addressName) {
        this.userName = userName;
        this.imageUrl = imageUrl;
        this.locationCoord = locationCoord;
        this.addressName = addressName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LatLng getLocationCoord() {
        return locationCoord;
    }

    public void setLocationCoord(LatLng locationCoord) {
        this.locationCoord = locationCoord;
    }

    public String getAddressName() {
        return addressName;
    }

    public void setAddressName(String addressName) {
        this.addressName = addressName;
    }
}
