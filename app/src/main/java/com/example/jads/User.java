package com.example.jads;

public class User {

    private String firstName, lastName, email, password, ratingScore, nbOfRatings, profileUrl, phoneNumber, fcmToken;

    public String getRatingScore() {
        return ratingScore;
    }

    public void setRatingScore(String ratingScore) {
        this.ratingScore = ratingScore;
    }

    public String getNbOfRatings() {
        return nbOfRatings;
    }

    public void setNbOfRatings(String nbOfRatings) {
        this.nbOfRatings = nbOfRatings;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public User(String firstName, String lastName, String email, String password, String ratingScore, String nbOfRatings, String profileUrl, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.ratingScore = ratingScore;
        this.nbOfRatings = nbOfRatings;
        this.profileUrl = profileUrl;
        this.phoneNumber = phoneNumber;
    }

    public User() {
    }
}
