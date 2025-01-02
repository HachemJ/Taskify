package com.example.jads;

public class User {

    String firstName, lastName, email, username, password, ratingScore, nbOfRatings, profileUrl, phoneNumber;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public User(String firstName, String lastName, String email, String username, String password, String ratingScore, String nbOfRatings, String profileUrl, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.password = password;
        this.ratingScore = ratingScore;
        this.nbOfRatings = nbOfRatings;
        this.profileUrl = profileUrl;
        this.phoneNumber = phoneNumber;
    }

    public User() {
    }
}
