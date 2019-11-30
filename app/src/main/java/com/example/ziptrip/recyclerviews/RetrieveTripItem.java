package com.example.ziptrip.recyclerviews;

import java.util.List;

public class RetrieveTripItem {
    private String tripName, tripDestination;
    private List<String> attendees;

    public RetrieveTripItem(String tripName, String tripDestination, List<String> attendees){
        this.tripName = tripName;
        this.tripDestination = tripDestination;
        this.attendees = attendees;
    }

    public String getTripName() {
        return tripName;
    }

    public String getTripDestination() {
        return tripDestination;
    }

    public List<String> getAttendees() {
        return attendees;
    }
}
