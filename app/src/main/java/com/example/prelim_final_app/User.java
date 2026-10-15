package com.example.prelim_final_app;

public class User {
    private String uid;
    private String username;
    private String email;
    private String birthdate;
    private String phone;

    public User() {
        // Required for Firebase Realtime Database
    }

    public User(String uid, String username, String email, String birthdate, String phone) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.birthdate = birthdate;
        this.phone = phone;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
