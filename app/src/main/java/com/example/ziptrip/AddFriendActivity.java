package com.example.ziptrip;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ziptrip.recyclerviews.AddFriendAdapter;
import com.example.ziptrip.recyclerviews.AddFriendItem;
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

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddFriendActivity extends AppCompatActivity {
    // Database components
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Recycler View components
    private RecyclerView rView;
    private RecyclerView.Adapter rAdapter;
    private RecyclerView.LayoutManager rManager;
    ArrayList<AddFriendItem> friendList = new ArrayList<>();
    List<String> currentTrips;
    List<String> currentFriends;
    List<String> friends = new ArrayList<>(); // list of friend usernames

    // Activity components
    EditText usernameInput;
    Button searchUsernameBtn;
    Intent friendIntent;
    String tripId;

    String TAG = "Adding friend activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        usernameInput = (EditText)findViewById(R.id.usernameEt);
        searchUsernameBtn = (Button)findViewById(R.id.searchUserBtn);
        friendIntent = getIntent();

        rView = findViewById(R.id.addFriendsRv);
        rManager = new LinearLayoutManager(AddFriendActivity.this);

        searchUsernameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFriend(usernameInput.getText().toString());
            }
        });

        DocumentReference tripRef = db.collection("trips").document(friendIntent.getStringExtra("tripId"));
        tripRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot != null && documentSnapshot.exists()){
                    Log.i(TAG, "In the listener method");
                    friendList.clear();
                    friends.clear();
                    rAdapter = new AddFriendAdapter(friendList);

                    rView.setLayoutManager(rManager);
                    rView.setAdapter(rAdapter);
                    loadData();
                }
            }
        });
    }

    // Load data into view
    private void loadData(){
        db.collection("trips").document(friendIntent.getStringExtra("tripId"))
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    friends = (List<String>)task.getResult().get("friends");

                    // for each friend, append a card to the recycler view
                    for(String friend : friends){
                        loadFriends(friend);
                    }

                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void loadFriends(String username){
        final DocumentReference friend = db.collection("users").document(username);

        // Query to see if friend exists
        friend.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot friendInfo = task.getResult();
                    if (friendInfo.exists()) {
                        Log.i("Adding friend", "Updating friend to recycler view");
                        // Get first and last name to display on the recycler view card
                        String fname = friendInfo.getString("firstname");
                        String lname = friendInfo.getString("lastname");
                        String username = friendInfo.getString("username");
                        AddFriendItem friend = new AddFriendItem(username, fname, lname, friendIntent.getStringExtra("tripId"));
                        friendList.add(friend);

                        // Update the recycler view here by adding all items to adapter
                        rAdapter = new AddFriendAdapter(friendList);

                        rView.setLayoutManager(rManager);
                        rView.setAdapter(rAdapter);

                    } else {
                        Log.d(TAG, "No matching document/friend found");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void addFriend(String username) {
        final DocumentReference friend = db.collection("users").document(username);

        // Query to see if friend exists
        friend.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot friendInfo = task.getResult();
                    if (friendInfo.exists()) {
                        Log.i("Adding friend", "Friend has been found");

                        // Runs if username exists. Add friend to array list
                        if (!friends.contains(friendInfo.getString("username"))) {
                            friends.add(friendInfo.getString("username"));

                            // Get first and last name to display on the recycler view card
                            String fname = friendInfo.getString("firstname");
                            String lname = friendInfo.getString("lastname");
                            String username = friendInfo.getString("username");
                            AddFriendItem friend = new AddFriendItem(username, fname, lname, friendIntent.getStringExtra("tripId"));
                            friendList.add(friend);

                            // Update the recycler view here by adding all items to adapter
                            rAdapter = new AddFriendAdapter(friendList);

                            rView.setLayoutManager(rManager);
                            rView.setAdapter(rAdapter);

                            addUserToTrip(friendIntent.getStringExtra("tripId"), username);
                            addTripsToUsers(username, friendIntent.getStringExtra("tripname"));
                        }
                    } else {
                        Log.d(TAG, "No matching document/friend found");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public void addTripsToUsers(final String username, final String tripName){
        // Store current trips on use account
        db.collection("users").document(username).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            currentTrips = (List<String>)task.getResult().get("trips");
                            Log.i(TAG, "List of trips before appending: " + currentTrips.toString());
                            currentTrips.add(tripName);
                            Log.i(TAG, "Full list of trips on file: " + currentTrips.toString());

                            // Update the user
                            updateUser(username);
                        }
                    }
                });
    }

    public void updateUser(String username){
        // Create map to update users document
        Map<String, Object> updatedList = new HashMap<>();
        updatedList.put("trips", currentTrips);

        // get user doc and update the friends field
        db.collection("users").document(username).update(updatedList);
    }

    // you changed this
    public void addUserToTrip(final String tripId, final String username){
        db.collection("trips").document(tripId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                currentFriends = (List<String>)task.getResult().get("friends");
                currentFriends.add(username);
                Log.i(TAG, "Full list of friends on file: " + currentFriends.toString());
                updateTrip(tripId);
            }
        });
    }

    private void updateTrip(String tripId){
        // Create map to update users document
        Map<String, Object> updatedList = new HashMap<>();
        updatedList.put("friends", currentFriends);

        // get user doc and update the friends field
        db.collection("trips").document(tripId).update(updatedList);
    }

}
