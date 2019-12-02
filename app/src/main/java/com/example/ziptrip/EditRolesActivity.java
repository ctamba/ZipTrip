package com.example.ziptrip;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.w3c.dom.Text;

import java.util.List;

public class EditRolesActivity extends AppCompatActivity {
    // Firebase components
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String TAG = "Role Activity";

    StringBuilder driverList = new StringBuilder();
    List<String> drivers;
    Intent roleIntent;
    boolean hasPermissions = false;

    // Activity components
    EditText changeLeaderInput, changeDriverInput;
    TextView currentLeaderTv, currentDriversTv;
    Button changeLeaderBtn, addDriverBtn, removeDriverBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_roles);

        changeLeaderInput = (EditText)findViewById(R.id.changeLeaderEt);
        changeDriverInput = (EditText)findViewById(R.id.changeDriverEt);
        currentLeaderTv = (TextView)findViewById(R.id.currentLeaderTv);
        currentDriversTv = (TextView)findViewById(R.id.driverListTv);
        changeLeaderBtn = (Button)findViewById(R.id.changeLeaderBtn);
        addDriverBtn = (Button)findViewById(R.id.addDriverBtn);
        removeDriverBtn = (Button)findViewById(R.id.delDriverBtn);

        // Retrieve intent
        roleIntent = getIntent();

        changeLeaderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // check to see if the user has permissions
                if(hasPermissions == true){
                    // change driver in database if user exists
                    db.collection("users").document(changeLeaderInput.getText().toString())
                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful() && task.getResult() != null){
                                // user exists, update leader
                                updateLeader();
                            }
                        }
                    });
                }
                else{
                    Toast.makeText(EditRolesActivity.this, "You do not have permissions to change this field!", Toast.LENGTH_SHORT);
                }
            }
        });

        addDriverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hasPermissions == true){
                    // check to see if inputted user exists, if so, update db
                    db.collection("users").document(changeDriverInput.getText().toString())
                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful() && task.getResult() != null){
                                // user exists, add driver to driver list and append
                                addDriver(changeDriverInput.getText().toString());
                            }
                        }
                    });
                }
                else{
                    Toast.makeText(EditRolesActivity.this, "You do not have permissions to change this field!", Toast.LENGTH_SHORT);
                }
            }
        });

        removeDriverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hasPermissions == true){
                    // check to see if inputted user exists, if so, update db
                    db.collection("users").document(changeDriverInput.getText().toString())
                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful() && task.getResult() != null){
                                // user exists,
                                removeDriver(changeDriverInput.getText().toString());
                            }
                        }
                    });
                }
                else{
                    Toast.makeText(EditRolesActivity.this, "You do not have permissions to change this field!", Toast.LENGTH_SHORT);
                }
            }
        });

        // Load data if any data changes
        DocumentReference tripRef = db.collection("trips").document(roleIntent.getStringExtra("tripId"));
        tripRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot != null && documentSnapshot.exists()){
                    Log.i(TAG, "In the listener method");
                    // Get role permissions
                    checkPermissions();

                    currentDriversTv.setText("");
                    StringBuilder emptyDrivers = new StringBuilder();
                    driverList = emptyDrivers;
                    loadRoles();
                }
            }
        });
    }

    private void loadRoles(){
        // Get role info from document using trip id from intent
        db.collection("trips").document(roleIntent.getStringExtra("tripId"))
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
                            currentLeaderTv.setText(task.getResult().get("firstname") + " " + task.getResult().get("lastname")
                                    + " (" + task.getResult().get("username") + ")");
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
                                driverList.append(task.getResult().get("firstname") + " " + task.getResult().get("lastname")
                                        + " (" + task.getResult().get("username") + ")" + "\n");
                            }
                            currentDriversTv.setText(driverList.toString());
                        }
                    });
        }
    }

    private void checkPermissions(){
        // retrieve trip information for field "leader"
        db.collection("trips").document(roleIntent.getStringExtra("tripId"))
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().get("leader").toString().equals(roleIntent.getStringExtra("username"))){
                        Log.i(TAG, "user has admin permissions");
                        hasPermissions = true;
                    }
                }
            }
        });
    }

    public void updateLeader(){
        db.collection("trips").document(roleIntent.getStringExtra("tripId"))
                .update("leader", changeLeaderInput.getText().toString())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Document was successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "Error writing document");
                    }
                });
    }

    private void addDriver(final String newDriver){
        // Get list of drivers and then append new driver, update
        db.collection("trips").document(roleIntent.getStringExtra("tripId"))
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful() && task.getResult() != null){
                    if(!drivers.contains(newDriver)){
                        drivers = (List<String>)task.getResult().get("drivers");
                        drivers.add(newDriver);

                        // update database
                        updateDriverDoc();
                    }
                }
                else{
                    Toast.makeText(EditRolesActivity.this, "User is already a driver", Toast.LENGTH_SHORT);
                }
            }
        });
    }

    private void removeDriver(final String newDriver){
        // Get list of drivers and then append new driver, update
        db.collection("trips").document(roleIntent.getStringExtra("tripId"))
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful() && task.getResult() != null){
                    drivers = (List<String>)task.getResult().get("drivers");
                    if(drivers.contains(newDriver)){
                        drivers.remove(drivers.indexOf(newDriver));

                        // update database
                        updateDriverDoc();
                    }
                    else{
                        Toast.makeText(EditRolesActivity.this, "User is not currently a driver", Toast.LENGTH_SHORT);
                    }
                }
            }
        });
    }

    private void updateDriverDoc(){
        db.collection("trips").document(roleIntent.getStringExtra("tripId"))
                .update("drivers", drivers)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Document snapshot has been written");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "Error in writing document");
                    }
                });
    }
}
