package com.example.ziptrip.recyclerviews;

public class AddFriendItem {
    private String username;
    private String fname;
    private String lname;
    private String tripId;

    public AddFriendItem(String username, String fname, String lname, String tripId){
        this.username = username;
        this.fname = fname;
        this.lname = lname;
        this.tripId = tripId;
    }

    public String getUsername() {
        return username;
    }

    public String getFname() {
        return fname;
    }

    public String getLname() {
        return lname;
    }

    public String getTripId() {
        return tripId;
    }
}
