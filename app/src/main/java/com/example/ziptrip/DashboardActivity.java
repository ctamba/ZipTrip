package com.example.ziptrip;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.internal.FlowLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    // Initializing database
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String TAG = "DashboardActivity";

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

        //Checking to see if there are any trips available
        Task<QuerySnapshot> docRef = db.collection("users").get();
        List<DocumentSnapshot> collections = docRef.getResult().getDocuments();
        for(DocumentSnapshot trip : collections){
            if(trip.getId().contains(dashIntent.getStringExtra("email"))){
                //inflate thingy and it's data here
            }
        }


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
            Intent intent = new Intent(getApplicationContext(), ProfilePage.class);
            intent.putExtra("email", dashIntent.getStringExtra("email"));
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
