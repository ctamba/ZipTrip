package com.example.ziptrip;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.internal.FlowLayout;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class DashboardActivity extends AppCompatActivity {

    private Toolbar menuBar;
    FloatingActionButton addTripBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Configuring menu bar
        menuBar = findViewById(R.id.toolbar);
        addTripBtn = (FloatingActionButton)findViewById(R.id.addTripBtn);
        setSupportActionBar(menuBar);

        FloatingActionButton fab = findViewById(R.id.addTripBtn);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                // Create intent to add trip activity
                Intent addTripIntent = new Intent(getApplicationContext(), CreateTripActivity.class);
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
            Toast.makeText(this, "Profile clicked!",Toast.LENGTH_SHORT);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
