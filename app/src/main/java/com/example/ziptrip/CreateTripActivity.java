package com.example.ziptrip;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ziptrip.fragments.DatePickerFragment;
import com.example.ziptrip.recyclerviews.AddFriendAdapter;
import com.example.ziptrip.recyclerviews.AddFriendItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CreateTripActivity extends AppCompatActivity implements OnMapReadyCallback {
    // Initializing database
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Intent tripIntent;

    // UI Components
    String TAG = "TripCreate_Map";
    List<String> friends = new ArrayList<>(); // contains usernames
    List<String> drivers = new ArrayList<>(); // contains usernames, initially empty
    List<String> currentTrips; // contains trip names, NOT TRIPIDS

    GoogleMap userMap;
    Button createTripBtn, addFriendBtn;
    EditText startDateInput, endDateInput, tripNameInput, destinationInput, searchFriendInput;
    LatLng currentLocation;
    Marker destinationMarker;

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
        createTripBtn.setEnabled(false);
        addFriendBtn = (Button)findViewById(R.id.addFriendBtn);
        startDateInput = (EditText)findViewById(R.id.startDateInput);
        endDateInput = (EditText)findViewById(R.id.endDateInput);
        tripNameInput = (EditText)findViewById(R.id.tripNameInput);
        destinationInput = (EditText)findViewById(R.id.destinationInput);
        searchFriendInput = (EditText)findViewById(R.id.searchFriendEt);

        // Setting text changed listeners
        startDateInput.addTextChangedListener(watcher);
        endDateInput.addTextChangedListener(watcher);
        tripNameInput.addTextChangedListener(watcher);
        destinationInput.addTextChangedListener(watcher);
        searchFriendInput.addTextChangedListener(watcher);

        tripIntent = getIntent();

        // Get the SupportMapFragment and request notification when map is ready to be used
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        addFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(searchFriendInput.getText().toString() != null && !searchFriendInput.getText().toString().equals("")){
                    addFriend(searchFriendInput.getText().toString());
                }
            }
        });

        // When create trip btn clicked, send to db
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
                trip.put("startlocation", currentLocation);
                trip.put("destinationlocation", new LatLng(destinationMarker.getPosition().latitude, destinationMarker.getPosition().longitude));

                // Concatenate unique tripID
                final String tripId = tripIntent.getStringExtra("username") + "-" + tripNameInput.getText().toString();

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
        userMap.addMarker(new MarkerOptions().position(currentLocation));
        userMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 10));
    }

    public void onSearch(View view) {
        String destinationLocation = destinationInput.getText().toString();
        List<Address> addressList = null;

        // Clear if there is currently a marker on map
        if(destinationMarker != null){
            destinationMarker.remove();
            userMap.clear();
            currentLocation = new LatLng(33.937842, -84.519933);
            userMap.addMarker(new MarkerOptions().position(currentLocation));
        }

        if(destinationLocation != null || !destinationLocation.equals("")){
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(destinationLocation, 5);

            } catch (IOException e) {
                e.printStackTrace();
            }

            Address address = addressList.get(0);
            LatLng coordinates = new LatLng(address.getLatitude(), address.getLongitude());

            // Save the marker
            MarkerOptions destinationOptions = new MarkerOptions().position(coordinates).title("Destination");
            destinationMarker = userMap.addMarker(destinationOptions);

            //userMap.addMarker(new MarkerOptions().position(coordinates).title("Marker"));
            // Zoom map
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(currentLocation);
            builder.include(destinationMarker.getPosition());
            LatLngBounds mapBounds = builder.build();
            userMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBounds, 100));

            // Code to display driving path, get url first, then get path
            String url = getUrl(currentLocation, coordinates);
            TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
            taskRequestDirections.execute(url);
        }
    }

    // Used for google maps query
    private String getUrl(LatLng origin, LatLng dest){
        String originStr = "origin=" + origin.latitude + "," + origin.longitude;
        String destStr = "destination=" + dest.latitude + "," + dest.longitude;
        String sensorStr = "sensor=false";
        String modeStr = "mode=driving";
        String keyStr = "key=" + getString(R.string.google_maps_key);
        String parameters = originStr + "&" + destStr + "&" + sensorStr + "&" + modeStr + "&" + keyStr;

        // Creating url to request
        String url = "https://maps.googleapis.com/maps/api/directions/json?" + parameters;
        return url;
    }

    // Used to get directions from two points using url created
    private String requestDirections(String reqUrl) throws IOException {
        String responseString = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try{
            URL url = new URL(reqUrl);
            httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.connect();

            // Get the response result
            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while((line = bufferedReader.readLine()) != null){
                stringBuffer.append(line);
            }

            responseString = stringBuffer.toString();
            bufferedReader.close();
            inputStreamReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(inputStream != null){
               inputStream.close();
            }
            httpURLConnection.disconnect();
        }
        return responseString;
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
                            String tripId = tripIntent.getStringExtra("username") + "-" + tripNameInput.getText().toString();
                            AddFriendItem friend = new AddFriendItem(username, fname, lname, tripId);
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

    // Class used to send async task to get directions
    public class TaskRequestDirections extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {
            String responseString = "";
            try{
                responseString = requestDirections(strings[0]);
            } catch (Exception e){
                e.printStackTrace();
            }

            return responseString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // Parse json result
            TaskParser taskParser = new TaskParser();
            taskParser.execute(s);
        }
    }

    public class TaskParser extends AsyncTask<String, Void, List<List<HashMap<String, String>>>>{

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject = null;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionsParser directionsParser = new DirectionsParser();
                routes = directionsParser.parse(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            // Get list route and display it into the map
            ArrayList points = null;
            PolylineOptions polylineOptions = null;

            for(List<HashMap<String, String>> path : lists){
                points = new ArrayList();
                polylineOptions = new PolylineOptions();

                for(HashMap<String, String> point : path){
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lon"));
                    points.add(new LatLng(lat, lng));
                }

                polylineOptions.addAll(points);
                polylineOptions.width(10);
                polylineOptions.color(Color.rgb(72, 153, 88));
                polylineOptions.geodesic(true);
            }

            if(polylineOptions != null){
                userMap.addPolyline(polylineOptions);
            }
            else{
                Toast.makeText(CreateTripActivity.this, "Directions were unable to be found", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private final TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        { }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {}
        @Override
        public void afterTextChanged(Editable s) {
            if (startDateInput.getText().toString().length() == 0 || endDateInput.getText().toString().length() == 0 ||
                    tripNameInput.getText().toString().length() == 0 || destinationInput.getText().toString().length() == 0 ||
                    searchFriendInput.toString().trim().length() == 0) {
                createTripBtn.setEnabled(false);
            } else {
                createTripBtn.setEnabled(true);
            }
        }
    };
}

