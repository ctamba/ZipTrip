package com.example.ziptrip;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.ziptrip.recyclerviews.ShoppingListAdapter;
import com.example.ziptrip.recyclerviews.ShoppingListItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class TripAtAGlanceActivity extends AppCompatActivity {
    // Firebase variables
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String TAG = "TripAtAGlanceActivity";

    // Retrieve intent
    Intent glanceIntent;

    // Activity componants
    TextView tripDestination, friendList, tripName, billTotalTv;
    Button tripDetailsBtn, tripStopsBtn;
    ImageButton addFriendBtn, shoppingListBtn;

    // Retrieving list of friends
    StringBuilder fullNameList = new StringBuilder();
    List<String> friends;

    // Recycler view components
    private RecyclerView sView;
    private RecyclerView.Adapter sAdapter;
    private RecyclerView.LayoutManager sManager;
    ArrayList<ShoppingListItem> itemList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_at_aglance);

        // Retrieve intent
        glanceIntent = getIntent();

        // Initialize componants
        tripDestination = (TextView)findViewById(R.id.tripDestinationTv);
        friendList = (TextView)findViewById(R.id.friendListTv);
        tripName = (TextView)findViewById(R.id.tripNameGlanceLabel);
        billTotalTv = (TextView)findViewById(R.id.billTotalTv);
        tripDetailsBtn = (Button)findViewById(R.id.detailsBtn);
        tripStopsBtn = (Button)findViewById(R.id.viewStopsBtn);
        addFriendBtn = (ImageButton)findViewById(R.id.addFriendImgBtn);
        shoppingListBtn = (ImageButton)findViewById(R.id.expandListImgBtn);
        sView = findViewById(R.id.recentItemsRecyclerView);
        sManager = new LinearLayoutManager(TripAtAGlanceActivity.this);

        // Onclick listeners
        addFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create intent to pass username to next activity
                Intent friendIntent = new Intent(getApplicationContext(), AddFriendActivity.class);
                friendIntent.putExtra("username", glanceIntent.getStringExtra("username"));
                friendIntent.putExtra("tripname", tripName.getText().toString());
                startActivity(friendIntent);
            }
        });

        shoppingListBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                // Create intent and send to shopping list activity
                Intent shoppingIntent = new Intent(getApplicationContext(), ShoppingListActivity.class);
                shoppingIntent.putExtra("tripId", glanceIntent.getStringExtra("tripId"));
                shoppingIntent.putExtra("username", glanceIntent.getStringExtra("username"));
                startActivity(shoppingIntent);
            }
        });

        // Load data if any data changes
        DocumentReference tripRef = db.collection("trips").document(glanceIntent.getStringExtra("tripId"));
        tripRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot != null && documentSnapshot.exists()){
                    Log.i(TAG, "In the listener method");
                    friendList.setText("");
                    StringBuilder emptySb = new StringBuilder();
                    fullNameList = emptySb;
                    loadData();
                }
            }
        });

        // Load data if shopping list is updated
        db.collection("trips").document(glanceIntent.getStringExtra("tripId")).collection("shoppinglist")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if(queryDocumentSnapshots != null){
                            Log.i(TAG, "Resetting and querying recycler view");
                            // Reset recycler view
                            itemList.clear();
                            sAdapter = new ShoppingListAdapter(itemList);
                            sView.setLayoutManager(sManager);
                            sView.setAdapter(sAdapter);
                            queryRecentItems();

                            // Update bill total
                            getBillTotal();
                        }
                    }
                });
    }

    private void loadData(){
        //Grab trip id from intent
        String tripId = glanceIntent.getStringExtra("tripId");
        Log.i(TAG, "Retrieving data for tripid: " + tripId);

        // Retrieving doc data from firebase
        DocumentReference tripRef = db.collection("trips").document(tripId);
        tripRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    // Set values for all textviews
                    tripName.setText(task.getResult().get("tripname").toString());
                    tripDestination.setText("Destination: " + task.getResult().get("destination").toString());
                    friends = (List<String>)task.getResult().get("friends");
                    Log.i(TAG, "Friend list contents: " + friends);
                    loadFriends();
                }
                else{
                    Log.e(TAG, "Error retrieving firebase trip data.");
                }
            }
        });
    }

    private void loadFriends(){
        for(final String username : friends){
            // get reference in firebase
            DocumentReference friendRef = db.collection("users").document(username);
            friendRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        String firstname = task.getResult().get("firstname").toString();
                        String lastname = task.getResult().get("lastname").toString();
                        String fullname = firstname + " " + lastname + " (" + username + ")";
                        fullNameList.append(fullname + "\n");
                    }

                    friendList.setText(fullNameList.toString());
                }
            });
        }
    }

    private void queryRecentItems(){
        Log.i(TAG, "Inside queryRecentItems()");
        CollectionReference shoppingListRef = db.collection("trips").document(glanceIntent.getStringExtra("tripId"))
                .collection("shoppinglist");
        Query listQuery = shoppingListRef.orderBy("itemname").limit(3);
        listQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot item : task.getResult()){
                        Log.i(TAG, "Item found: " + item.getId());
                        String itemName = item.getId();
                        getRecentItemInfo(itemName);
                    }
                }
            }
        });
    }

    private void getRecentItemInfo(String itemName){
        Log.i(TAG, "Querying item");
        // Query item info and place into recycler view
        db.collection("trips").document(glanceIntent.getStringExtra("tripId")).collection("shoppinglist").document(itemName)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    Log.i(TAG, "item found: " + task.getResult().get("itemname").toString());
                    String itemName = task.getResult().get("itemname").toString();
                    String price = task.getResult().get("price").toString();
                    String buyer = task.getResult().get("buyer").toString();
                    ShoppingListItem itemToAdd = new ShoppingListItem(itemName, buyer, price, glanceIntent.getStringExtra("tripId"));
                    itemList.add(itemToAdd);

                    // Add to recycler view
                    sAdapter = new ShoppingListAdapter(itemList);
                    sView.setLayoutManager(sManager);
                    sView.setAdapter(sAdapter);
                }
            }
        });
    }

    private void getBillTotal(){
        // get collection items
        db.collection("trips").document(glanceIntent.getStringExtra("tripId")).collection("shoppinglist")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    Double totalPrice = 0.00;
                    List<DocumentSnapshot> itemList = task.getResult().getDocuments();
                    for(DocumentSnapshot item : itemList){
                        totalPrice += Double.valueOf(item.get("price").toString());
                    }
                    String roundedPrice = String.format("$%.2f", totalPrice);
                    billTotalTv.setText(roundedPrice);
                }
            }
        });
    }
}
