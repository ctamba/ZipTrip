package com.example.ziptrip.recyclerviews;

public class ShoppingListItem {
    private String itemName, buyer, price, tripId;
    //double price;

    public ShoppingListItem(String itemName, String buyer, String price, String tripId){
        this.itemName = itemName;
        this.price = price;
        this.buyer = buyer;
        this.tripId = tripId;
    }

    public String getItemName() {
        return itemName;
    }

    public String getPrice() {
        return price;
    }

    public String getBuyer() {
        return buyer;
    }

    public String getTripId() {
        return tripId;
    }
}
