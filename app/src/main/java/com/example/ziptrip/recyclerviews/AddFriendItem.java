package com.example.ziptrip.recyclerviews;

public class AddFriendItem {
    private String username;
    private String fname;
    private String lname;

    public AddFriendItem(String username, String fname, String lname){
        this.username = username;
        this.fname = fname;
        this.lname = lname;
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
}
