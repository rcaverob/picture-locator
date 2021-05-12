package com.example.picture_locator.Models;

public class User {

    private String userName;
    private String rank;
    private int scores;
    private String profileImgUri;

    public User() {

    }

    public User(String userName, String rank, int scores, String uri) {
        this.userName = userName;
        this.rank = rank;
        this.scores = scores;
        this.profileImgUri = uri;
    }

    public String getProfileImgUri() {
        return profileImgUri;
    }

    public void setProfileImgUri(String profileImgUri) {
        this.profileImgUri = profileImgUri;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public int getScores() {
        return scores;
    }

    public void setScores(int scores) {
        this.scores = scores;
    }
}
