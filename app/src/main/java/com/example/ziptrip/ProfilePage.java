package com.example.ziptrip;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfilePage extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String TAG = "test";

    // View components
    TextView name, emailAddress, phoneNum, password;
    Button editProfileBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        editProfileBtn = (Button) findViewById(R.id.editProfileBtn);
        name = (TextView) findViewById(R.id.profileName);
        emailAddress = (TextView) findViewById(R.id.emailAddress);
        phoneNum = (TextView) findViewById(R.id.phoneNum);
        password = (TextView) findViewById(R.id.password);


        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), editProfile.class);
                String profileName = name.getText().toString();
                String email = emailAddress.getText().toString();
                String phone = phoneNum.getText().toString();
                String pass = password.getText().toString();
                Bundle extras = new Bundle();
                extras.putString("profile_name", profileName);
                extras.putString("email", email);
                extras.putString("phone", phone);
                extras.putString("pass", pass);
                intent.putExtras(extras);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onResume(){
        super.onResume();

        // Update db info
        Intent profileIntent = getIntent();
        Bundle profileIntentExtras = profileIntent.getExtras();
        String email = profileIntentExtras.getString("email");
        retrieveProfileInfo(email);
    }

    public void retrieveProfileInfo(String email){
        DocumentReference docRef = db.collection("users").document(email);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot profileInfo = task.getResult();
                    if (profileInfo.exists()) {
                        name.setText(profileInfo.getString("firstname") + " " + profileInfo.get("lastname"));
                        emailAddress.setText(profileInfo.getString("email"));
                        phoneNum.setText(profileInfo.getString("phone"));
                        password.setText(profileInfo.getString("password"));
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }
}