package com.example.ziptrip;

import android.content.Intent;
import android.os.Bundle;

import com.example.ziptrip.recyclerviews.RetrieveTripItem;
import com.example.ziptrip.recyclerviews.RetrieveTripsAdapter;
import com.example.ziptrip.ui.login.LoginActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DashboardActivity extends AppCompatActivity{

    // Initializing database
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String TAG = "DashboardActivity";

    GoogleMap userMap;
    Intent dashIntent;
    private Toolbar menuBar;
    FloatingActionButton addTripBtn;

    // Recycler View components
    private RecyclerView tView;
    private RecyclerView.Adapter tAdapter;
    private RecyclerView.LayoutManager tManager;
    ArrayList<RetrieveTripItem> tripList = new ArrayList<>();

    // For notification
    public static final String NOTIFICATION_CHANNEL_ID = "10001" ;
    private final static String default_notification_channel_id = "default" ;
    final Calendar myCalendar = Calendar. getInstance () ;


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

        tView = findViewById(R.id.tripRecylerView);
        tManager = new LinearLayoutManager(DashboardActivity.this);

        FloatingActionButton fab = findViewById(R.id.addTripBtn);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                // Create intent to add trip activity
                Intent addTripIntent = new Intent(getApplicationContext(), CreateTripActivity.class);
                addTripIntent.putExtra("username", dashIntent.getStringExtra("username"));
                startActivity(addTripIntent);
            }
        });

        // Load and change data when data changes
        DocumentReference userRef = db.collection("users").document(dashIntent.getStringExtra("username"));
        userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot != null && documentSnapshot.exists()){
                    // Clear all views and reload
                    tripList.clear();
                    tAdapter = new RetrieveTripsAdapter(tripList);
                    tView.setLayoutManager(tManager);
                    tView.setAdapter(tAdapter);

                    retrieveTrips();
                }
            }
        });
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
            profileIntent.putExtra("username", dashIntent.getStringExtra("username"));
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

    public void onTripCardClick(View view){
        final TextView tripNameTv = (TextView)view.findViewById(R.id.tripNameTv);
        // start intent inside query
        // Query trips that have current user listed as a friend
        db.collection("trips").whereArrayContains("friends", dashIntent.getStringExtra("username"))
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots.getDocuments() != null){
                    for(QueryDocumentSnapshot trip : queryDocumentSnapshots){
                        // If the trip name matches one in database, send intent for that trip id
                        if(trip.get("tripname").equals(tripNameTv.getText().toString())){
                            String tripId = trip.getId();
                            Intent tripAtAGlanceIntent = new Intent(getApplicationContext(), TripAtAGlanceActivity.class);
                            tripAtAGlanceIntent.putExtra("tripId", tripId);
                            tripAtAGlanceIntent.putExtra("username", dashIntent.getStringExtra("username"));
                            startActivity(tripAtAGlanceIntent);
                        }
                    }
                }
            }
        });
    }

    public void retrieveTrips(){
        // Query trips that have current user listed as a friend
        db.collection("trips").whereArrayContains("friends", dashIntent.getStringExtra("username"))
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots.getDocuments() != null){
                    for(QueryDocumentSnapshot trip : queryDocumentSnapshots){
                        makeTripGUI(trip.getId());
                    }
                }
            }
        });
    }

    public void makeTripGUI(final String tripId){

        // Get single String view componants
        DocumentReference docRef = db.collection("trips").document(tripId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                String tripName = "";
                String tripDestination = "";
                List<String> friends;

                if (task.isSuccessful()) {
                    tripName = task.getResult().get("tripname").toString();
                    tripDestination = task.getResult().get("destination").toString();
                    friends = (List<String>)task.getResult().get("friends");

                    RetrieveTripItem trip = new RetrieveTripItem(tripName, tripDestination, friends);
                    tripList.add(trip);

                    // Update the recycler view here by adding all items to adapter
                    tAdapter = new RetrieveTripsAdapter(tripList);

                    tView.setLayoutManager(tManager);
                    tView.setAdapter(tAdapter);

                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

}
