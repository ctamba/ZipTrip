package com.example.ziptrip;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ziptrip.fragments.DatePickerFragment;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.example.ziptrip.util.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;

public class CreateTripActivity extends AppCompatActivity implements OnMapReadyCallback {
    // Initializing database
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Intent tripIntent;

    // UI Components
    String TAG = "TripCreate_Map";
    String currentDateString;
    GoogleMap userMap;
    Button createTripBtn, setStartDateBtn, setEndDateBtn;
    EditText startDateInput, endDateInput, tripNameInput, destinationInput;

    // Calendar attributes
    Calendar calendar;
    Date startDate;
    DatePickerDialog datePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_trip);

        createTripBtn = (Button)findViewById(R.id.createTripBtn);
        setStartDateBtn = (Button)findViewById(R.id.startDateBtn);
        setEndDateBtn = (Button)findViewById(R.id.endDateBtn);
        startDateInput = (EditText)findViewById(R.id.startDateInput);
        endDateInput = (EditText)findViewById(R.id.endDateInput);
        tripNameInput = (EditText)findViewById(R.id.tripNameInput);
        destinationInput = (EditText)findViewById(R.id.destinationInput);

        tripIntent = getIntent();

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        createTripBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                // Testing sending data to db, <String: Key, String: Value>
                Map<String,String> trip = new HashMap<>();
                trip.put("tripname", tripNameInput.getText().toString());
                trip.put("destination", destinationInput.getText().toString());
                trip.put("startdate", startDateInput.getText().toString());
                trip.put("enddate", endDateInput.getText().toString());
                trip.put("friends", "test");
                trip.put("owner", tripIntent.getStringExtra("email"));

                // Concatenate unique tripID
                String tripId = tripIntent.getStringExtra("email") + "-" + tripNameInput.getText().toString();

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

                CreateTripActivity.this.finish();
            }
        });

//        // Setting the start and end date using a calendar view
//        setStartDateBtn.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view) {
//                DialogFragment datePicker = new DatePickerFragment();
//                datePicker.setArguments(getIntent().getExtras());
//                getSupportFragmentManager().beginTransaction()
//                        .add(android.R.id.content, datePicker, "startDate")
//                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//                        .addToBackStack(datePicker.getClass().getName())
//                        .commit();
//                datePicker.show(getSupportFragmentManager(), "date picker");
//            }
//        });
//
//        setEndDateBtn.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(getApplicationContext(), "HERE", Toast.LENGTH_SHORT).show();
//                DialogFragment datePicker = new DatePickerFragment();
//                datePicker.show(getSupportFragmentManager(), "date picker");
//            }
//        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // making map global
        userMap = googleMap;

        // Setting marker to syndey
        LatLng sydney = new LatLng(-33.852, 151.211);
        googleMap.addMarker(new MarkerOptions().position(sydney)
                .title("Marker in Sydney"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

//    @Override
//    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
//        Calendar c = Calendar.getInstance();
//        c.set(Calendar.YEAR, year);
//        c.set(Calendar.MONTH, month);
//        c.set(Calendar.DAY_OF_MONTH, day);
//        currentDateString = DateFormat.getDateInstance().format(c.getTime());
//
//        if(datePicker.getTag().toString().equals("startDate")){
//            startDateInput.setText(currentDateString);
//        }
//        else{
//            endDateInput.setText(currentDateString);
//        }
//
//    }
}

