package com.example.picture_locator.Models;


import java.util.ArrayList;
import java.util.List;

public class Quizbank {

    private String userName;
    private String imageUrl;
    private LatLng locationCoord;
    private String addressName;
    private String locationsAnswered;
    private String usernamesAnswered;

    public Quizbank() {
    }

    public Quizbank(String userName, String imageUrl, LatLng locationCoord, String addressName) {
        this.userName = userName;
        this.imageUrl = imageUrl;
        this.locationCoord = locationCoord;
        this.addressName = addressName;

        this.locationsAnswered = "";
        this.usernamesAnswered = "";
    }

    public String getLocationsAnswered() {
        return locationsAnswered;
    }

    public void setLocationsAnswered(String locationsAnswered) {
        this.locationsAnswered = locationsAnswered;
    }

    public String getUsernamesAnswered() {
        return usernamesAnswered;
    }

    public void setUsernamesAnswered(String usernamesAnswered) {
        this.usernamesAnswered = usernamesAnswered;
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

    public void addGuess(String userName, com.google.android.gms.maps.model.LatLng coords){
        usernamesAnswered += userName + " ";
        locationsAnswered += (coords.latitude+" "+coords.longitude+ " ");
    }

}
