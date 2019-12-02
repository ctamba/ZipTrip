package com.example.ziptrip;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ziptrip.recyclerviews.ShoppingListAdapter;
import com.example.ziptrip.recyclerviews.ShoppingListItem;
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

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TripAtAGlanceActivity extends AppCompatActivity implements OnMapReadyCallback {
    // Firebase variables
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String TAG = "TripAtAGlanceActivity";

    // Retrieve intent
    Intent glanceIntent;

    // Map components
    GoogleMap userMap;
    LatLng currentLocation, destinationLocation;

    // Activity componants
    TextView tripDestination, friendList, tripName, billTotalTv, leaderTv, driverTv;
    Button tripDetailsBtn, tripStopsBtn, reminderBtn, editRolesBtn;
    ImageButton addFriendBtn, shoppingListBtn;

    // Retrieving list of friends
    StringBuilder fullNameList = new StringBuilder();
    StringBuilder driverList = new StringBuilder();
    List<String> friends;
    List<String> drivers;

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
        leaderTv = (TextView)findViewById(R.id.leaderUserTv);
        driverTv = (TextView)findViewById(R.id.driverUserTv);
        tripDetailsBtn = (Button)findViewById(R.id.detailsBtn);
        tripStopsBtn = (Button)findViewById(R.id.viewStopsBtn);
        reminderBtn = (Button)findViewById(R.id.setReminderBtn);
        editRolesBtn = (Button)findViewById(R.id.editRolesBtn);
        addFriendBtn = (ImageButton)findViewById(R.id.addFriendImgBtn);
        shoppingListBtn = (ImageButton)findViewById(R.id.expandListImgBtn);
        sView = findViewById(R.id.recentItemsRecyclerView);
        sManager = new LinearLayoutManager(TripAtAGlanceActivity.this);

        // Get the SupportMapFragment and request notification when map is ready to be used
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.glanceMap);
        mapFragment.getMapAsync(this);

        // Onclick listeners
        addFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create intent to pass username to next activity
                Intent friendIntent = new Intent(getApplicationContext(), AddFriendActivity.class);
                friendIntent.putExtra("username", glanceIntent.getStringExtra("username"));
                friendIntent.putExtra("tripname", tripName.getText().toString());
                friendIntent.putExtra("tripId", glanceIntent.getStringExtra("tripId"));
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

        reminderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create intent to send to reminder activity
                Intent reminderIntent = new Intent(getApplicationContext(), SetReminderActivity.class);
                reminderIntent.putExtra("tripname", tripName.getText().toString());
                startActivity(reminderIntent);
            }
        });

        editRolesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create intent to send to edit roles activity
                Intent roleIntent = new Intent(getApplicationContext(), EditRolesActivity.class);
                roleIntent.putExtra("tripId", glanceIntent.getStringExtra("tripId"));
                roleIntent.putExtra("username", glanceIntent.getStringExtra("username"));
                startActivity(roleIntent);
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
                    driverTv.setText("");
                    StringBuilder emptyName = new StringBuilder();
                    StringBuilder emptyDrivers = new StringBuilder();
                    fullNameList = emptyName;
                    driverList = emptyDrivers;
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        userMap = googleMap;

        // Get latlang for the start and end destination
        db.collection("trips").document(glanceIntent.getStringExtra("tripId")).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful() && task != null){
                            Map<String, Double> startMap = (Map<String, Double>)task.getResult().get("startlocation");
                            Map<String, Double> destMap = (Map<String, Double>)task.getResult().get("destinationlocation");
                            LatLng startLatLng = new LatLng(startMap.get("latitude"), startMap.get("longitude"));
                            LatLng destLatLng = new LatLng(destMap.get("latitude"), destMap.get("longitude"));

                            // Add markers
                            Marker startMarker = userMap.addMarker(new MarkerOptions().position(startLatLng));
                            Marker destMarker = userMap.addMarker(new MarkerOptions().position(destLatLng));

                            // Zoom map
                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            builder.include(startMarker.getPosition());
                            builder.include(destMarker.getPosition());
                            LatLngBounds mapBounds = builder.build();
                            userMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBounds, 100));

                            // Draw path between points
                            String url = getUrl(startLatLng, destLatLng);
                            TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                            taskRequestDirections.execute(url);
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
                    loadRoles();
                }
                else{
                    Log.e(TAG, "Error retrieving firebase trip data.");
                }
            }
        });
    }

    private void loadRoles(){
        // Get role info from document using trip id from intent
        db.collection("trips").document(glanceIntent.getStringExtra("tripId"))
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful() && task.getResult() != null){
                    DocumentSnapshot trip = task.getResult();

                    // Set textview info
                    getLeaderName(trip.get("leader").toString());
                    // Get drivers and append info
                    drivers = (List<String>)trip.get("drivers");
                    getDriverNames();

                }
            }
        });
    }

    private void getLeaderName(String username){
        db.collection("users").document(username).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful() && task.getResult() != null){
                            leaderTv.setText(task.getResult().get("firstname") + " " + task.getResult().get("lastname"));
                        }
                    }
                });
    }

    private void getDriverNames(){
        for(String driver : drivers){
            db.collection("users").document(driver).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful() && task.getResult() != null){
                                driverList.append(task.getResult().get("firstname") + " " + task.getResult().get("lastname") + "\n");
                            }
                            driverTv.setText(driverList.toString());
                        }
                    });
        }
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

    // Class used to send async task to get directions
    public class TaskRequestDirections extends AsyncTask<String, Void, String> {

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
                Toast.makeText(TripAtAGlanceActivity.this, "Directions were unable to be found", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
