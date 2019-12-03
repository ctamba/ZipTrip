package com.example.ziptrip;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class DetailsActivity extends AppCompatActivity {
    // Firebase instance
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    TextView tripName, tripDest, startDate, endDate;
    Intent detailsIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        tripName = (TextView)findViewById(R.id.detailsTripNameTv);
        tripDest = (TextView)findViewById(R.id.tripDestTv);
        startDate = (TextView)findViewById(R.id.tripStartDateTv);
        endDate = (TextView)findViewById(R.id.tripEndDateTv);

        detailsIntent = getIntent();

        // load data
        loadData();
    }

    private void loadData(){
        db.collection("trips").document(detailsIntent.getStringExtra("tripId")).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful() && task.getResult() != null){
                            DocumentSnapshot trip = task.getResult();

                            tripName.setText(trip.get("tripname").toString());
                            tripDest.setText(trip.get("destination").toString());
                            startDate.setText(trip.get("startdate").toString());
                            endDate.setText(trip.get("enddate").toString());
                        }
                    }
                });
    }
}
