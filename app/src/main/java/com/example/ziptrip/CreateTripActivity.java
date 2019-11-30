package com.example.ziptrip;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ziptrip.fragments.DatePickerFragment;
import com.example.ziptrip.googlemaps.DownloadUrl;
import com.example.ziptrip.googlemaps.GetDirectionsData;
import com.example.ziptrip.recyclerviews.AddFriendAdapter;
import com.example.ziptrip.recyclerviews.AddFriendItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.model.Document;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.ziptrip.util.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;

public class CreateTripActivity extends AppCompatActivity implements OnMapReadyCallback {
    // Initializing database
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Intent tripIntent;

    // UI Components
    String TAG = "TripCreate_Map";
    List<String> friends = new ArrayList<>(); // contains usernames
    List<String> drivers;
    List<String> currentTrips; // contains trip names, NOT TRIPIDS

    GoogleMap userMap;
    Button createTripBtn, setStartDateBtn, setEndDateBtn, addFriendBtn;
    EditText startDateInput, endDateInput, tripNameInput, destinationInput, searchFriendInput;
    LatLng currentLocation;

    // Calendar attributes
    Calendar calendar;
    Date startDate;
    DatePickerDialog datePicker;

    // Recycler View components
    private RecyclerView rView;
    private RecyclerView.Adapter rAdapter;
    private RecyclerView.LayoutManager rManager;
    ArrayList<AddFriendItem> friendList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_trip);

        createTripBtn = (Button)findViewById(R.id.createTripBtn);
        setStartDateBtn = (Button)findViewById(R.id.startDateBtn);
        setEndDateBtn = (Button)findViewById(R.id.endDateBtn);
        addFriendBtn = (Button)findViewById(R.id.addFriendBtn);
        startDateInput = (EditText)findViewById(R.id.startDateInput);
        endDateInput = (EditText)findViewById(R.id.endDateInput);
        tripNameInput = (EditText)findViewById(R.id.tripNameInput);
        destinationInput = (EditText)findViewById(R.id.destinationInput);
        searchFriendInput = (EditText)findViewById(R.id.searchFriendEt);

        tripIntent = getIntent();

        // Get the SupportMapFragment and request notification when map is ready to be used
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        addFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFriend(searchFriendInput.getText().toString());
            }
        });

        // When create trip btn clicked, send to db -- DOES NOT WORK WHEN ADDING ARRAYS
        createTripBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                // Add current user to friends list in order to register trip
                friends.add(tripIntent.getStringExtra("username"));

                // setting data to send to db, <String: Key, Object: Value>
                Map<String,Object> trip = new HashMap<>();
                trip.put("tripname", tripNameInput.getText().toString());
                trip.put("destination", destinationInput.getText().toString());
                trip.put("startdate", startDateInput.getText().toString());
                trip.put("enddate", endDateInput.getText().toString());
                trip.put("leader", tripIntent.getStringExtra("username"));
                trip.put("friends", friends);
                trip.put("drivers", drivers);

                // Concatenate unique tripID
                String tripId = tripIntent.getStringExtra("username") + "-" + tripNameInput.getText().toString();

                // Add a new document with a generated ID (can customize later)
                db.collection("trips").document(tripId)
                        .set(trip)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully written!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error writing document", e);
                            }
                        });

                // Add shopping list collection to trip -- LOL THIS DOESNT WORK BUT DOES NOT THROW ERROR
                db.collection("trips").document(tripId).collection("shoppinglist");

                // Add trip to the "trips" field of each friend that was added
                for(String friend : friends){
                    addTripsToUsers(friend, tripNameInput.getText().toString());
                }
//
                CreateTripActivity.this.finish();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        userMap = googleMap;

        // Add a marker in current location and move the camera.
        currentLocation = new LatLng(33.937842, -84.519933);
        userMap.addMarker(new MarkerOptions().position(currentLocation).title("Marker in Sydney"));
        userMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
    }

    public void onSearch(View view) throws IOException {
        String location = destinationInput.getText().toString();
        List<Address> addressList = null;

        if(location != null || !location.equals("")){
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 5);

            } catch (IOException e) {
                e.printStackTrace();
            }

            Address address = addressList.get(0);
            LatLng coordinates = new LatLng(address.getLatitude(), address.getLongitude());
            userMap.addMarker(new MarkerOptions().position(coordinates).title("Marker"));
            userMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 5));

            /*
            // Code to display driving path
            String url = getUrl(currentLocation, coordinates, "driving");
            // Creating object to store map and url
            Object[] dataTransfer = new Object[2];
            dataTransfer[0] = userMap;
            dataTransfer[1] = url;
            GetDirectionsData getDirectionsData = new GetDirectionsData();
            getDirectionsData.execute(dataTransfer);
             */
        }
    }

    // Used for google maps query
    private String getUrl(LatLng origin, LatLng dest, String directionsMode){
        String originStr = "origin=" + origin.latitude + "," + origin.longitude;
        String destStr = "destination=" + dest.latitude + "," + dest.longitude;
        String parameters = originStr + "&" + destStr + "&" + directionsMode;

        String url = "https://maps.googleapis.com/maps/api/directions/json?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    private void addFriend(String username){
        Log.i("Adding friend", "Button clicked, in addFriend method");
        final DocumentReference friend = db.collection("users").document(username);

        // Query to see if friend exists
        friend.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot friendInfo = task.getResult();
                    if (friendInfo.exists()) {
                        Log.i("Adding friend", "Friend has been found");

                        // Runs if username exists. Add friend to array list
                        if(!friends.contains(friendInfo.getString("username"))){
                            friends.add(friendInfo.getString("username"));

                            // Get first and last name to display on the recycler view card
                            String fname = friendInfo.getString("firstname");
                            String lname = friendInfo.getString("lastname");
                            String username = friendInfo.getString("username");
                            AddFriendItem friend = new AddFriendItem(username, fname, lname);
                            friendList.add(friend);

                            // Update the recycler view here by adding all items to adapter ERROR HERE
                            rView = findViewById(R.id.addFriendsRecyclerView);
                            rView.setHasFixedSize(true);
                            rManager = new LinearLayoutManager(CreateTripActivity.this);
                            rAdapter = new AddFriendAdapter(friendList);

                            rView.setLayoutManager(rManager);
                            rView.setAdapter(rAdapter);
                        }
                    }
                    else {
                        Log.d(TAG, "No matching document/friend found");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        Log.i("Adding friend", "Leaving add friend method.");
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
}

