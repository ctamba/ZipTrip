package com.example.ziptrip;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.ziptrip.ui.login.LoginActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.Distribution;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity{

    // Initializing database
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String TAG = "DashboardActivity";
    LinearLayout tripListLayout;
    View row;

    GoogleMap userMap;
    Intent dashIntent;
    private Toolbar menuBar;
    FloatingActionButton addTripBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Getting intent
        dashIntent = getIntent();

        // Configuring menu bar
        menuBar = findViewById(R.id.toolbar);
        addTripBtn = (FloatingActionButton)findViewById(R.id.addTripBtn);
        setSupportActionBar(menuBar);

        // Retrieving layout
        tripListLayout = (LinearLayout)findViewById(R.id.tripListLayout);

        // Retrieving trips
        retrieveTrips();


        FloatingActionButton fab = findViewById(R.id.addTripBtn);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                // Create intent to add trip activity
                Intent addTripIntent = new Intent(getApplicationContext(), CreateTripActivity.class);
                addTripIntent.putExtra("email", dashIntent.getStringExtra("email"));
                startActivity(addTripIntent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        // Clear any currently inflated objects
        if(row != null){
            tripListLayout.removeAllViews();

            // Append with new db information
            retrieveTrips();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        // Inflate/add the menu to the toolbar if it exists
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        // Handles click depending on what the user clicks on in the action bar
        int id = item.getItemId();

        if(id == R.id.menu_profile){
            Intent profileIntent = new Intent(getApplicationContext(), ProfilePage.class);
            profileIntent.putExtra("email", dashIntent.getStringExtra("email"));
            startActivity(profileIntent);
            return true;
        }
        if(id == R.id.settingsBtn){
            Intent logOutIntent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(logOutIntent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void retrieveTrips(){
        // get the map of documents in the trip collection, store in a list
        db.collection("trips").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    List<String> tripList = new ArrayList<>();

                    // Get list of trips and add to list
                    for(QueryDocumentSnapshot trip : task.getResult()){
                        if(trip.getId().contains(dashIntent.getStringExtra("email"))) {
                            tripList.add(trip.getId());
                        }
                    }

                    // inflate trips if list is not empty
                    int index = 0;
                    if(!tripList.isEmpty()){
                        for(String trip : tripList){
                            makeTripGUI(trip, index);
                            index++;
                        }
                    }
                }
                else{
                    Log.d(TAG, "Error loading trips");
                }
            }
        });

    }

    public void makeTripGUI(final String tripId, int index){
        int count = index;

        // Get view components and set the text
        DocumentReference docRef = db.collection("trips").document(tripId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    // Create a new row by inflating the new_trip_view layout file
                    LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                    // View components
                    row = li.inflate(R.layout.new_trip_view, null);
                    TextView tripName = row.findViewById(R.id.tripNameLabel);
                    TextView attendees = row.findViewById(R.id.attendeesTv);
                    TextView destination = row.findViewById(R.id.destinationTv);

                    DocumentSnapshot tripInfo = task.getResult();
                    if (tripInfo.exists()) {
                        tripName.setText(tripInfo.getString("tripname"));
                        destination.setText(tripInfo.getString("destination"));
                        attendees.setText(tripInfo.getString("friends"));
                    } else {
                        Log.d(TAG, "No such document");
                    }

                    // Append view to the layout
                    tripListLayout.addView(row);

                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

    }

//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        userMap = googleMap;
//
//        // Add a marker in current location and move the camera.
//        LatLng currentLocation = new LatLng(33.937842, -84.519933);
//        userMap.addMarker(new MarkerOptions().position(currentLocation).title("Marker in Sydney"));
//        userMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
//    }

}
