package com.example.ziptrip;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.List;

public class TripAtAGlanceActivity extends AppCompatActivity {
    // Firebase variables
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String TAG = "TripAtAGlanceActivity";

    // Retrieve intent
    Intent glanceIntent;

    // Activity componants
    TextView tripDestination, friendList, tripName;
    Button tripDetailsBtn, tripStopsBtn;
    ImageButton addFriendBtn, shoppingListBtn;

    // Retrieving list of friends
    StringBuilder fullNameList = new StringBuilder();
    List<String> friends;

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
        tripDetailsBtn = (Button)findViewById(R.id.detailsBtn);
        tripStopsBtn = (Button)findViewById(R.id.viewStopsBtn);
        addFriendBtn = (ImageButton)findViewById(R.id.addFriendImgBtn);
        shoppingListBtn = (ImageButton)findViewById(R.id.expandListImgBtn);

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
    }

    public void loadData(){
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

    public void loadFriends(){
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
}
